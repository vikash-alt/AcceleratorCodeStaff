package com.newgen.iforms.staffHL;

import com.newgen.iforms.commons.CommonFunctionality;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.dlp.integration.cbs.AadharVault;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class HighmarkAPI {

	CommonFunctionality cf = new CommonFunctionality();
	PortalCommonMethods pcm = new PortalCommonMethods();
	APICommonMethods cm = new APICommonMethods();
	CRGGenerator crg= new CRGGenerator();
	public String getHighMarkCIBILScore(IFormReference ifr, String ProcessInstanceId, String aadharNo,
			String productType, String loanAmount, String applicantType)
					throws ParseException {
		Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->inside HighMark: ");
		String apiName = "HighMark";
		Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->apiName: " + apiName);
		Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->inside aadharNo: "+aadharNo);
		String request = "";
		String responseBody = "";
		 
		String installmentAmount = "";
		int consolidated_emiAmnt = 0;
		int totalNonEMICount = 0;
		String combinedPaymentHistory = "";
		String writtenOffSettledStatus = "";
		String writeOff = "No";
		String settled = "No";
		String NPA = "No";
		int dpdCount = 0;
		String DPD = "";

		String Value = "";
		String insertionOrderid = "";
		int age=0;

		String productCode = pcm.getProductCode(ifr);
		Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->productCode: " + productCode);
		String subProductCode = pcm.getSubProductCode(ifr);
		Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->subProductCode: " + subProductCode);

		try {

			String firstName = "", lastName = "", dateofBirth = "", panNumber = "", customerId = "", addressLine1 = "", addressLine2 = "",
					addressLine3 = "", city = "", state = "", pincode = "", gender = "", gendercode = "", stateCode = "", mobileNo = "", DOB = "", fatherName = "", EMAILID = "";

			String Query1 = "";
			String Query2 = "";
			if (productType.equalsIgnoreCase("PAPL")) {
				Query1 = "SELECT CUSTOMERID,CUSTOMERFIRSTNAME,CUSTOMERLASTNAME,PANNUMBER,DATEOFBIRTH,GENDER,MOBILENUMBER "
						+ "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";

				Query2 = "SELECT permaddress1,permaddress2,permaddress3,PermCity,PermState,PermZip "
						+ "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
				Log.consoleLog(ifr, "Query2: " + Query2);

			} else {
				
				
				 if (applicantType.contains("~")) {
					 insertionOrderid = applicantType.split("~")[1];
	                    applicantType = applicantType.split("~")[0];
				
					Query1 = "select a.customerid, b.firstname,b.lastname,c.kyc_Id,c.kyc_no,"
							+ " to_char(b.date_of_birth_hl,'dd-MM-YYYY')dob,b.gender,b.mobileno,b.FATHERNAME,b.EMAILID,a.INSERTIONORDERID "
							+ " from los_nl_basic_info a ,\n"
							+ "los_l_basic_info_i b, LOS_NL_KYC c \n"
							+ "where a.f_key = b.f_key and b.f_key=c.f_key "
							+ "and a.pid='" + ProcessInstanceId + "' and a.applicanttype='" + applicantType + "'";

					Query2 = "select line1,line2,line3,city_town_village,state,pincode "
							+ "from LOS_NL_Address where F_KEY =(select F_KEY from los_nl_basic_info "
							+ " where PID ='" + ProcessInstanceId + "' and applicanttype='" + applicantType + "')\n"
							+ " and addresstype='P'";
					Log.consoleLog(ifr, "Query2: " + Query2);
				}
				 
			}

			

			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Query1: " + Query1);
			List< List< String>> Result = ifr.getDataFromDB(Query1);
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->#Result: " + Result.toString());
			String pattern = ConfProperty.getCommonPropertyValue("NamePattern");
			//"^[A-Za-z ]+$";
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->#Result pattern: " + pattern);
			if (Result.size() > 0) {
				customerId = Result.get(0).get(0);
				firstName = Result.get(0).get(1).replaceAll("[ .]", "");
				lastName = Result.get(0).get(2).replaceAll("[ .]", "");

				if (lastName.equalsIgnoreCase("{}")) {
					lastName = "";
				}
				if (lastName.equalsIgnoreCase("{")) {
					lastName = "";
				}
				if (lastName.equalsIgnoreCase("}")) {
					lastName = "";
				}
				if (firstName.length() < 2) {
					Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->firstName: " + firstName);
					return RLOS_Constants.ERROR + "Name is less than 1 character ";
				} else if (!Pattern.matches(pattern, firstName)) {
					return RLOS_Constants.ERROR + "Name contains invalid special characters";
				}
				// panNumber = Result.get(0).get(3);
				dateofBirth = Result.get(0).get(5);
				Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->****dateofBirth: " + dateofBirth);
				gender = Result.get(0).get(6);
				mobileNo = Result.get(0).get(7);
				fatherName = Result.get(0).get(8);
				EMAILID = Result.get(0).get(9);
			}

			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Query2: " + Query2);
			List< List< String>> Result1 = ifr.getDataFromDB(Query2);
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Result1: " + Result1.toString());

			if (Result1.size() > 0) {
				addressLine1 = Result1.get(0).get(0);
				addressLine2 = Result1.get(0).get(1);
				addressLine3 = Result1.get(0).get(2);
				city = Result1.get(0).get(3);
				state = Result1.get(0).get(4);
				pincode = Result1.get(0).get(5);
			}



			// Process all KYC records
			panNumber = "";
			String voterId = "";
			for (int i = 0; i < Result.size(); i++) {
				List<String> row = Result.get(i);
				String kycType = row.get(3);
				String kycValue = row.get(4);
				Log.consoleLog(ifr, "Processing row " + i + " - KYC Type: " + kycType + ", Value: " + kycValue);

				if (kycValue != null && !kycValue.isEmpty()) {
					if ("TAXID".equalsIgnoreCase(kycType)) {
						panNumber = kycValue.trim();
						Log.consoleLog(ifr, "Found PAN Number: " + panNumber);
					} 
					else if ("VID".equalsIgnoreCase(kycType)) {
						voterId = kycValue.trim();
						Log.consoleLog(ifr, "Found Voter ID: " + voterId);
					}
					else if ("AA".equalsIgnoreCase(kycType)) {
						aadharNo = kycValue.trim();
						Log.consoleLog(ifr, "Found aadharNo Ref No: " + aadharNo);
						AadharVault objAV = new AadharVault();
						aadharNo = objAV.getDataByReferenceKey(ifr, aadharNo);
					}
					else if (panNumber.isEmpty() && kycValue.matches("[A-Za-z]{5}[0-9]{4}[A-Za-z]{1}")) {
						panNumber = kycValue.trim();
						Log.consoleLog(ifr, "Auto-detected PAN format: " + panNumber);
					}
				} else {
					Log.consoleLog(ifr, "Empty KYC value in row " + i);
				}
			}
			if(aadharNo.equalsIgnoreCase("ERROR"))
			{
				aadharNo="";
			}

			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->AddressLine1: " + addressLine1);
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->AddressLine2: " + addressLine2);
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->AddressLine3: " + addressLine3);
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->City: " + city);
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->State: " + state);
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Pincode: " + pincode);
			
			String stateCodeQ = "SELECT STATE_CODE FROM LOS_MST_STATE WHERE "
                    + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + state + "')) AND ROWNUM=1";
            Log.consoleLog(ifr, "stateCodeQ==>" + stateCodeQ);
            List< List< String>> stateCodeR = ifr.getDataFromDB(stateCodeQ);
            Log.consoleLog(ifr, "#stateCodeR===>" + stateCodeR.toString());
            if (stateCodeR.size() > 0) {
            	state = stateCodeR.get(0).get(0);
            }

			if (gender.equalsIgnoreCase("MALE")) {
				gender = "G01";
			} else if (gender.equalsIgnoreCase("FEMALE")) {
				gender = "G02";
			}
			String outputDate = "";
			int ageYears = 0;
			if (dateofBirth != null && !dateofBirth.trim().isEmpty()) {
				try {

					age=calculateAge(dateofBirth,ifr);


				} catch (Exception ex) {
					Log.consoleLog(ifr, "Exception in calculate Age: " + ex);// YYYY-MM-DD===>YYYYMMDD*/


				}
			}
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->outputDate: " + outputDate);// YYYY-MM-DD===>YYYYMMDD
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->ageYears: " + ageYears);// YYYY-MM-DD===>YYYYMMDD

			DOB = dateofBirth;
		
			Date Date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			String formattedDate = dateFormat.format(Date);

			Date PresentDate = new Date();
			SimpleDateFormat dateFormatt = new SimpleDateFormat("ddMMyyHHmmssSSS");
			String formatDate = dateFormatt.format(PresentDate);

			LocalDate currentDate = LocalDate.now();

			String excludedEMIAccnts = pcm.getParamConfig(ifr, productCode, subProductCode, "HIGHMARKCONF", "EMIACCTTYPE");
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->excludedEMIAccnts: " + excludedEMIAccnts);
			String excludedOwners = pcm.getParamConfig(ifr, productCode, subProductCode, "HIGHMARKCONF", "EMIOWNERTYPE");
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->excludedOwners: " + excludedOwners);
			String writtenoffSettled = pcm.getParamConfig(ifr, productCode, subProductCode, "HIGHMARKCONF", "WRITTENOFFSETTLED");
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->writtenoffSettled: " + writtenoffSettled);


			String mobileStr = String.valueOf(mobileNo);
			String cleanedMobileNo;

			if (mobileStr.startsWith("91") && mobileStr.length() == 12) {
				cleanedMobileNo = mobileStr.substring(2);
			} else {
				cleanedMobileNo = mobileStr;
			}

			String InquiryPurpose="";
			String loanSelected = pcm.getLoanSelected(ifr);
			if(loanSelected.equalsIgnoreCase("Canara Budget") 
					|| loanSelected.equalsIgnoreCase("Canara Pension")
					|| loanSelected.equalsIgnoreCase("Vehicle Loan") ||
					productType.equalsIgnoreCase("PAPL") ||
					loanSelected.equalsIgnoreCase("PAPL") )
			{
				InquiryPurpose="Personal Loan";
				InquiryPurpose=pcm.getConstantValue(ifr, "HIGHMARK", InquiryPurpose);
			}
			subProductCode = pcm.getSubProductCode(ifr);

			if(subProductCode.equalsIgnoreCase("CANTWO"))
			{
				loanSelected="Two-wheeler Loan";
				InquiryPurpose=pcm.getConstantValue(ifr, "HIGHMARK", loanSelected);
			}
			if(subProductCode.equalsIgnoreCase("CANFOUR"))
			{
				loanSelected="Auto Loan";
				InquiryPurpose=pcm.getConstantValue(ifr, "HIGHMARK", loanSelected);
			}

			String uniqueValue = ProcessInstanceId.replace("LOS-", "");
			//String uniqueValue = ProcessInstanceId;
			
			uniqueValue = uniqueValue.replaceFirst("^0+(?!$)", "");
			uniqueValue = uniqueValue.replaceFirst("^0+", "");
			String unique = formattedDate;
			
			uniqueValue=uniqueValue+unique.replaceAll("[^a-zA-Z0-9 ]", "");
			uniqueValue = "LOS" + uniqueValue.replace(" ", "");
			if (panNumber == null || panNumber.isEmpty()) {
				Log.consoleLog(ifr, "No PAN number found in KYC records");
				panNumber = "";
			}
			if (voterId == null || voterId.isEmpty()) {
				Log.consoleLog(ifr, "No Voter ID found in KYC records");
				voterId = "";
			}
			if(panNumber==null ||  panNumber.equals(""))
			{
				panNumber="";
				Log.consoleLog(ifr,"inside if Valid PAN for processing: " + panNumber);
			}
			else if ( panNumber.length() >= 4 && panNumber.charAt(3) == 'P') {

				//panNumber=panNumber;
				Log.consoleLog(ifr,"Valid PAN for processing: " + panNumber);
			}
			else
			{
				Log.consoleLog(ifr,"Valid PAN for processing: " + "PAN number Not Listed ");
				return RLOS_Constants.ERROR;
			}
			
			   String mbrid = pcm.getConstantValue(ifr, "HIGHMARK", "MBRID"); ///NAB0000095
			   String kendraId = pcm.getConstantValue(ifr, "HIGHMARK", "KENDRA_ID"); 
			   String credt_rpt_trn_id = pcm.getConstantValue(ifr, "HIGHMARK", "CREDT_RPT_TRN_ID"); 
			   String credt_rpt_id = pcm.getConstantValue(ifr, "HIGHMARK", "CREDT_RPT_ID"); 
			   String TBranch = cm.GetHomeBranchCode(ifr, ProcessInstanceId, "");
			   TBranch = TBranch.replaceFirst("^0+(?!$)", "");
			   
			   
			   
			   StringBuilder idsBlock = new StringBuilder();

			   if ((voterId != null && !voterId.trim().isEmpty()) ||
			       (aadharNo != null && !aadharNo.trim().isEmpty()) ||
			       (panNumber != null && !panNumber.trim().isEmpty())) {

			       idsBlock.append("                    \"IDS\": {\r\n")
			               .append("                        \"ID\": [\r\n");

			       boolean commaNeeded = false;

			       if (voterId != null && !voterId.trim().isEmpty()) {
			           idsBlock.append("                            {\r\n")
			                   .append("                                \"TYPE\": \"ID02\",\r\n")
			                   .append("                                \"VALUE\": \"").append(voterId).append("\"\r\n")
			                   .append("                            }");
			           commaNeeded = true;
			       }

			       if (aadharNo != null && !aadharNo.trim().isEmpty()) {
			           if (commaNeeded) idsBlock.append(",\r\n");
			           idsBlock.append("                            {\r\n")
			                   .append("                                \"TYPE\": \"ID03\",\r\n")
			                   .append("                                \"VALUE\": \"").append(aadharNo).append("\"\r\n")
			                   .append("                            }");
			           commaNeeded = true;
			       }

			       if (panNumber != null && !panNumber.trim().isEmpty()) {
			           if (commaNeeded) idsBlock.append(",\r\n");
			           idsBlock.append("                            {\r\n")
			                   .append("                                \"TYPE\": \"ID07\",\r\n")
			                   .append("                                \"VALUE\": \"").append(panNumber).append("\"\r\n")
			                   .append("                            }");
			       }

			       idsBlock.append("\r\n                        ]\r\n")
			               .append("                    },\r\n");
			   }

			    request =
			       "{\r\n"
			     + "    \"REQUEST-REQUEST-FILE\": {\r\n"
			     + "        \"HEADER-SEGMENT\": {\r\n"
			     + "            \"AUTH-FLG\": \"Y\",\r\n"
			     + "            \"AUTH-TITLE\": \"USER\",\r\n"
			     + "            \"CONSUMER\": {\r\n"
			     + "                \"INDV\": true,\r\n"
			     + "                \"SCORE\": true\r\n"
			     + "            },\r\n"
			     + "            \"INQ-DT-TM\": \"" + formattedDate + "\",\r\n"
			     + "            \"IOI\": true,\r\n"
			     + "            \"LOS-NAME\": \"\",\r\n"
			     + "            \"LOS-VENDER\": \" \",\r\n"
			     + "            \"LOS-VERSION\": 1,\r\n"
			     + "            \"MEMBER-PRE-OVERRIDE\": \"N\",\r\n"
			     + "            \"REQ-SERVICE-TYPE\": \"\",\r\n"
			     + "            \"MFI\": {\r\n"
			     + "                \"GROUP\": true,\r\n"
			     + "                \"INDV\": true,\r\n"
			     + "                \"SCORE\": false\r\n"
			     + "            },\r\n"
			     + "            \"REQ-ACTN-TYP\": \"SUBMIT\",\r\n"
			     + "            \"REQ-VOL-TYP\": \"C01\",\r\n"
			     + "            \"RES-FRMT\": \"JSON/HTML\",\r\n"
			     + "            \"RES-FRMT-EMBD\": \"Y\",\r\n"
			     + "            \"SUB-MBR-ID\": \"CANARA BANK\",\r\n"
			     + "            \"TEST-FLG\": \"N\"\r\n"
			     + "        },\r\n"
			     + "        \"INQUIRY\": [\r\n"
			     + "            {\r\n"
			     + "                \"ADDRESS-SEGMENT\": {\r\n"
			     + "                    \"ADDRESS\": [\r\n"
			     + "                        {\r\n"
			     + "                            \"ADDRESS-1\": \"" + addressLine1 + "\",\r\n"
			     + "                            \"CITY\": \"" + city + "\",\r\n"
			     + "                            \"PIN\": " + pincode + ",\r\n"
			     + "                            \"STATE\": \"" + state + "\",\r\n"
			     + "                            \"TYPE\": \"D01\"\r\n"
			     + "                        },\r\n"
			     + "                        {\r\n"
			     + "                            \"ADDRESS-1\": \"" + addressLine1 + "\",\r\n"
			     + "                            \"CITY\": \"" + city + "\",\r\n"
			     + "                            \"PIN\": " + pincode + ",\r\n"
			     + "                            \"STATE\": \"" + state + "\",\r\n"
			     + "                            \"TYPE\": \"D02\"\r\n"
			     + "                        }\r\n"
			     + "                    ]\r\n"
			     + "                },\r\n"
			     + "                \"APPLICANT-SEGMENT\": {\r\n"
			     + "                    \"APPLICANT-NAME\": {\r\n"
			     + "                        \"NAME1\": \"" + firstName + "\",\r\n"
			     + "                        \"NAME2\": \"\",\r\n"
			     + "                        \"NAME3\": \"" + lastName + "\"\r\n"
			     + "                    },\r\n"
			     + "                    \"DOB\": {\r\n"
			     + "                        \"AGE\": \"" + age + "\",\r\n"
			     + "                        \"AGE-AS-ON\": \"\",\r\n"
			     + "                        \"DOB-DATE\": \"" + DOB + "\"\r\n"
			     + "                    },\r\n"
			     + "                    \"EMAILS\": {\r\n"
			     + "                        \"EMAIL\": [\r\n"
			     + "                            \"" + EMAILID + "\",\r\n"
			     + "                            \"" + EMAILID + "\"\r\n"
			     + "                        ]\r\n"
			     + "                    },\r\n"
			     + "                    \"GENDER\": \"" + gender + "\",\r\n"
			     + idsBlock.toString()  // ✅ Inserted conditional IDS block here
			     + "                    \"KEY-PERSON\": {\r\n"
			     + "                        \"NAME\": \"\",\r\n"
			     + "                        \"TYPE\": \"\"\r\n"
			     + "                    },\r\n"
			     + "                    \"NOMINEE\": {\r\n"
			     + "                        \"NAME\": \"\",\r\n"
			     + "                        \"TYPE\": \"\"\r\n"
			     + "                    },\r\n"
			     + "                    \"PHONES\": {\r\n"
			     + "                        \"PHONE\": [\r\n"
			     + "                            {\r\n"
			     + "                                \"TELE-NO\": " + mobileNo + ",\r\n"
			     + "                                \"TELE-NO-TYPE\": \"P03\"\r\n"
			     + "                            },\r\n"
			     + "                            {\r\n"
			     + "                                \"TELE-NO\": " + cleanedMobileNo + ",\r\n"
			     + "                                \"TELE-NO-TYPE\": \"P01\"\r\n"
			     + "                            }\r\n"
			     + "                        ]\r\n"
			     + "                    },\r\n"
			     + "                    \"RELATIONS\": {\r\n"
			     + "                        \"RELATION\": [\r\n"
			     + "                            {\r\n"
			     + "                                \"NAME\": \"K01\",\r\n"
			     + "                                \"TYPE\": \"" + fatherName + "\"\r\n"
			     + "                            },\r\n"
			     + "                            {\r\n"
			     + "                                \"NAME\": \"K02\",\r\n"
			     + "                                \"TYPE\": \"" + fatherName + "\"\r\n"
			     + "                            }\r\n"
			     + "                        ]\r\n"
			     + "                    }\r\n"
			     + "                },\r\n"
			     + "                \"APPLICATION-SEGMENT\": {\r\n"
			     + "                    \"BRANCH-ID\": " + TBranch + ",\r\n"
			     + "                    \"CREDIT-INQUIRY-STAGE\": \"PRE-DISB\",\r\n"
			     + "                    \"CREDT-INQ-PURPS-TYP\": \"ACCT-ORIG\",\r\n"
			     + "                    \"CREDT-INQ-PURPS-TYP-DESC\": \"" + InquiryPurpose + "\",\r\n"
			     + "                    \"CREDT-REQ-TYP\": \"INDV\",\r\n"
			     + "                    \"CREDT-RPT-ID\": " + credt_rpt_id + ",\r\n"
			     + "                    \"CREDT-RPT-TRN-DT-TM\": \"" + formattedDate + "\",\r\n"
			     + "                    \"CREDT-RPT-TRN-ID\": " + credt_rpt_trn_id + ",\r\n"
			     + "                    \"INQUIRY-UNIQUE-REF-NO\": \"" + uniqueValue + "\",\r\n"
			     + "                    \"KENDRA-ID\": " + kendraId + ",\r\n"
			     + "                    \"LOAN-AMOUNT\": " + loanAmount + ",\r\n"
			     + "                    \"LOS-APP-ID\": \"" + ProcessInstanceId + "\",\r\n"
			     + "                    \"MBR-ID\": \"" + mbrid + "\"\r\n"
			     + "                }\r\n"
			     + "            }\r\n"
			     + "        ]\r\n"
			     + "    }\r\n"
			     + "}";

			   System.out.println(request);
			   Log.consoleLog(ifr, "request: " + request);

			


			responseBody = cm.getWebServiceResponse(ifr, apiName, request);
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Response: " + responseBody);

			Log.consoleLog(ifr, "HIGHMARK API RESPONSE==>" + responseBody);
			String cicReportGenReq = pcm.getConstantValue(ifr, "HIGHMARK", "CICREPORTGENREQ");
			if (!responseBody.equalsIgnoreCase("{}")) {

				JSONParser parser = new JSONParser();
				JSONObject OutputJSON = (JSONObject) parser.parse(responseBody);

				JSONObject body = (JSONObject) OutputJSON.get("body");
				Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->body:" + body.toString());
				

				JSONObject INDV_REPORT_FILE = (JSONObject) body.get("INDV-REPORT-FILE");
				Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->INDV_REPORT_FILE: " + INDV_REPORT_FILE.toString());

				JSONArray INDV_REPORTS = (JSONArray) INDV_REPORT_FILE.get("INDV-REPORTS");
				Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->INDV_REPORTS: " + INDV_REPORTS.toString());
				
				for (int INDVReportDataLstCount = 0; INDVReportDataLstCount < INDV_REPORTS
						.size(); INDVReportDataLstCount++) {
					JSONObject INDVReportDataVal = (JSONObject) INDV_REPORTS.get(INDVReportDataLstCount);

					JSONObject INDV_REPORT = (JSONObject) INDVReportDataVal.get("INDV-REPORT");
					
					Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->INDV_REPORT: " + INDV_REPORT.toString());

					JSONArray responses = (JSONArray) INDV_REPORT.get("RESPONSES");
					JSONObject printableReport = (JSONObject)INDV_REPORT.get("PRINTABLE-REPORT");
					
					
					if (cicReportGenReq.equalsIgnoreCase("Y")) {
                        //if (!(cf.getJsonValue(bodyObj, "encodedBase64").equalsIgnoreCase(""))) {
                            //String encodedB64 = cf.getJsonValue(bodyObj, "encodedBase64");
                            String encodedB64 = printableReport.get("CONTENT").toString();
                            String generateReportStatus = cm.generateReport(ifr, ProcessInstanceId, "HIGHMARK", encodedB64, "NGREPORTTOOL_HIGHMARK");
                            Log.consoleLog(ifr, "generateReportStatus==>" + generateReportStatus);
                            cm.updateCICReportStatus(ifr, "HIGHMARK", generateReportStatus, applicantType);
					}
					// Loop through RESPONSES and extract LOAN-DETAILS
					for (Object responseObj : responses) {
						JSONObject response = (JSONObject) responseObj;
						JSONObject loanDetails = (JSONObject) response.get("LOAN-DETAILS");
						if (loanDetails != null) {
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->loanDetails: " + loanDetails.toJSONString());
							String accountStatus = loanDetails.get("ACCOUNT-STATUS").toString();
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->accountStatus: " + accountStatus);
							String accountType = loanDetails.get("ACCT-TYPE").toString();
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->accountType: " + accountType);
							String ownershipInd = loanDetails.get("OWNERSHIP-IND").toString();
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->ownershipInd: " + ownershipInd);
							String dateReported = loanDetails.get("DATE-REPORTED").toString();
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->dateReported: " + dateReported);
							if (loanDetails.containsKey("INSTALLMENT-AMT")
									&& loanDetails.get("INSTALLMENT-AMT") != null) {
								installmentAmount = loanDetails.get("INSTALLMENT-AMT").toString();
							}
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->installmentAmount: " + installmentAmount);
							String paymentHistory = loanDetails.get("COMBINED-PAYMENT-HISTORY").toString();
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->paymentHistory: " + paymentHistory);

							//payment history (dpd)
							if ("Individual".equalsIgnoreCase(ownershipInd) || "Joint".equalsIgnoreCase(ownershipInd)) {
								String[] entries = paymentHistory.split("\\|");
								LocalDate today = LocalDate.now();
								LocalDate twelveMonthsAgo = today.minus(12, ChronoUnit.MONTHS);
								// Month mapping (Move outside loop)
								Map<String, Integer> monthMap = new HashMap<>();
								monthMap.put("Jan", 1);
								monthMap.put("Feb", 2);
								monthMap.put("Mar", 3);
								monthMap.put("Apr", 4);
								monthMap.put("May", 5);
								monthMap.put("Jun", 6);
								monthMap.put("Jul", 7);
								monthMap.put("Aug", 8);
								monthMap.put("Sep", 9);
								monthMap.put("Oct", 10);
								monthMap.put("Nov", 11);
								monthMap.put("Dec", 12);

								for (String entry : entries) {
									Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->entry: " + entry);
									String[] monthYearValue = entry.split(":");
									String monthStr = monthYearValue[0];
									// Further split "2022,000/STD" to extract only the year
									String[] yearAndValue = monthYearValue[1].split(",");
									int year = Integer.parseInt(yearAndValue[0]); // Extracting year only
									int month = monthMap.getOrDefault(monthStr, -1);
									LocalDate inputDate1 = LocalDate.of(year, month, 1);
									// Check if input date is within the last 12 months
									boolean isWithinLast12Months = !inputDate1.isBefore(twelveMonthsAgo) && !inputDate1.isAfter(today);
									if (isWithinLast12Months) {
										Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Date is within 12 months::: ");
										Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->yearAndValue[1] " + yearAndValue[1]);
										String[] paymenthistory = yearAndValue[1].split("/");
										combinedPaymentHistory = combinedPaymentHistory + paymenthistory[0];
										Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->paymentHistory " + paymentHistory);
										dpdCount++;
									}
								}
							}
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->combinedPaymentHistory: " + combinedPaymentHistory);

							//emi
							boolean emiStatus = checkEMIKnockOffAccFilterStatus(ifr, accountType,
									excludedEMIAccnts, ownershipInd, excludedOwners, accountStatus);
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->emiStatus: " + emiStatus);
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->InstallmentAmount: " + installmentAmount);
							if (emiStatus) {
								if (!installmentAmount.isEmpty()) {
									int amount = Integer.parseInt(installmentAmount.replaceAll("[^0-9]", ""));
									if (amount < 0) {
										totalNonEMICount++;
									}
									consolidated_emiAmnt += amount;
								}
							}
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->consolidated_emiAmnt: " + consolidated_emiAmnt);

							//history of setteled and histroy of writeoff
							if ("WRITTEN-OFF".equalsIgnoreCase(accountStatus) || "SETTLED".equalsIgnoreCase(accountStatus)) {
								Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->inisde accountstatus: " + accountStatus);
								if (loanDetails.containsKey("WRITTEN-OFF_SETTLED-STATUS")) {
									writtenOffSettledStatus = loanDetails.get("WRITTEN-OFF_SETTLED-STATUS").toString();
								}
								boolean writtenOffStatus = checkWrittenoffSettledStatus(ifr, writtenOffSettledStatus,
										writtenoffSettled);
								Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->writtenOffStatus: " + writtenOffStatus);
								if (writtenOffStatus) {
									DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
									LocalDate givenDate = LocalDate.parse(dateReported, formatter);
									LocalDate today = LocalDate.now();
									long monthsBetween = ChronoUnit.MONTHS.between(givenDate, today);
									if (Math.abs(monthsBetween) <= 60) {
										Log.consoleLog(ifr, "The date is within 60 months from today.");
										if ("Individual".equalsIgnoreCase(ownershipInd) || "Joint".equalsIgnoreCase(ownershipInd)) {
											writeOff = "Yes";
											settled = "Yes";
										}
									}

								}
							}
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->writeOff: " + writeOff);
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->settled: " + settled);

							//histroy of NPA
							if ("Individual".equalsIgnoreCase(ownershipInd) || "Guarantor".equalsIgnoreCase(ownershipInd)) {
								String[] entries = paymentHistory.split("\\|");
								LocalDate today = LocalDate.now();
								LocalDate sixtyMonthsAgo = today.minus(60, ChronoUnit.MONTHS);
								// Month mapping (Move outside loop)
								Map<String, Integer> monthMap = new HashMap<>();
								monthMap.put("Jan", 1);
								monthMap.put("Feb", 2);
								monthMap.put("Mar", 3);
								monthMap.put("Apr", 4);
								monthMap.put("May", 5);
								monthMap.put("Jun", 6);
								monthMap.put("Jul", 7);
								monthMap.put("Aug", 8);
								monthMap.put("Sep", 9);
								monthMap.put("Oct", 10);
								monthMap.put("Nov", 11);
								monthMap.put("Dec", 12);

								for (String entry : entries) {
									Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->entry: " + entry);
									String[] monthYearValue = entry.split(":");
									if (monthYearValue.length < 2) {
										Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Invalid entry format: " + entry);
										continue;
									}
									String monthStr = monthYearValue[0];
									// Further split "2022,000/STD" to extract only the year
									String[] yearAndValue = monthYearValue[1].split(",");
									if (yearAndValue.length < 1) {
										Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Invalid format for year: " + monthYearValue[1]);
										continue;
									}

									int year;
									try {
										year = Integer.parseInt(yearAndValue[0]); // Extracting year only
									} catch (NumberFormatException e) {
										Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Error parsing year from: " + yearAndValue[0]);
										continue;
									}
									int month = monthMap.getOrDefault(monthStr, -1);
									if (month == -1) {
										Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Invalid month name: " + monthStr);
										continue;
									}
									LocalDate inputDate1 = LocalDate.of(year, month, 1);
									// Check if input date is within the last 12 months
									boolean isWithinLast60Months = !inputDate1.isBefore(sixtyMonthsAgo) && !inputDate1.isAfter(today);
									if (isWithinLast60Months) {
										Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Date is within 60 months::: ");
										Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->yearAndValue[0] " + yearAndValue[1]);
										String[] paymenthistory = yearAndValue[1].split("/");
										String NPAcheck = paymenthistory[1];
										Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->NPAcheck:: " + NPAcheck);
										if ("SUB".endsWith(NPAcheck) || "DBT".equalsIgnoreCase(NPAcheck) || "LOS".equalsIgnoreCase(NPAcheck)) {
											Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->NPA is found: ");
											NPA = "Yes";
										}
									}
								}
							}
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->NPA: " + NPA);
						}
					}

					JSONArray ScoreDetails = (JSONArray) INDV_REPORT.get("SCORES");
					Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->ScoreDetails: " + ScoreDetails);

					for (int ScoreDetailsCount = 0; ScoreDetailsCount < ScoreDetails.size(); ScoreDetailsCount++) {
						JSONObject ScoreDetailsObj = (JSONObject) ScoreDetails.get(ScoreDetailsCount);
						if (ScoreDetailsObj.containsKey("SCORE-VALUE") && ScoreDetailsObj.get("SCORE-VALUE") != null) {
							Value = ScoreDetailsObj.get("SCORE-VALUE").toString();
							Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Value: " + Value);
						}

					}
				}

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				LocalDateTime dateTime = LocalDateTime.now();
				String enqDate = dateTime.format(formatter);
				String newToCredit = "No";
				if (dpdCount > 0) {
					DPD = String.valueOf(dpdCount);
				}

				String qryCICDataUpdate1 = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,"
						+ "CICNPACHECK,CICOVERDUE,WRITEOFF,"
						+ "BUREAUTYPE,TOTEMIAMOUNT,PAYHISTORYCOMBINED,"
						+ "APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,"
						+ "GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED,APPLICANT_UID) "
						+ "values('" + ProcessInstanceId + "','" + Value + "',"
						+ "'" + NPA + "','" + DPD + "','" + writeOff + "',"
						+ "'HM',"
						+ "'" + consolidated_emiAmnt + "',"
						+ "'" + combinedPaymentHistory + "',"
						+ "'" + applicantType + "',"
						+ "'" + totalNonEMICount + "','" + settled + "','"
						+ NPA + "','" + NPA + "','" + settled + "',"
						+ "SYSDATE,'" + insertionOrderid + "')";
				Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Insert query: " + qryCICDataUpdate1);
				ifr.saveDataInDB(qryCICDataUpdate1);

				String query = "INSERT INTO LOS_TRN_CREDITHISTORY (PID,CUSTOMERID,MOBILENO,PRODUCTCODE,"
						+ "APPREFNO,APPLICANTTYPE,APPLICANTID,\n"
						+ "BUREAUTYPE,BUREAUCODE,SERVICECODE,LAP_EXIST,\n"
						+ "CIC_SCORE,TOTAL_EMIAMOUNT,NEWTOCREDITYN,DTINSERTED,DTUPDATED)\n"
						+ "VALUES('" + ProcessInstanceId + "',"
						+ "'','" + mobileNo + "','VL','" + pcm.getApplicationRefNumber(ifr) + "','" + applicantType + "','" + insertionOrderid + "',\n"
						+ "'EXT','EF','CNS','" + "" + "','" + Value + "',"
						//+ "'" + NPA + "','" + dpd + "','" + writeOFF + "','"
						+ "'" + "" + "','" + newToCredit + "',SYSDATE,SYSDATE)";
				Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Insert query for LOS_TRN_CREDITHISTORY: " + query);
				int queryResult = ifr.saveDataInDB(query);
				Log.consoleLog(ifr, "HighmarkAPI:getHighMark****->queryResult: " + queryResult);
				Log.consoleLog(ifr, "applicantType()():applicantType****->applicantType: " + applicantType);
				Log.consoleLog(ifr, "HighmarkAPI:getHighMark****->insertionOrderid: " + insertionOrderid);
				crg.crgGenHighMark(ifr,responseBody,ProcessInstanceId,apiName,applicantType,insertionOrderid);
				return Value;

			} else {
				Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->No response from Server..");
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Exception/HighMark===>" + e);
			Log.errorLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Exception/HighMark===>" + e);
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Exception StackTrace:::" + errors);

		} finally {
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Capture Equifax REquest and Response Calling!!!!!");
			cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Highmark_API", request, responseBody, "", "", "",
					applicantType);
		}
		return RLOS_Constants.ERROR;
	}

	private boolean checkEMIKnockOffAccFilterStatus(IFormReference ifr, String accountType,
			String excludedAccnts, String ownerType, String excludedOwners, String accountStatus) {

		Log.consoleLog(ifr, "HighmarkAPI:checkEMIKnockOffAccFilterStatus->#checkEMIKnockOffAccFilterStatus started...");
		Log.consoleLog(ifr, "HighmarkAPI:checkEMIKnockOffAccFilterStatus->accountType: " + accountType);
		Log.consoleLog(ifr, "HighmarkAPI:checkEMIKnockOffAccFilterStatus->ownerType: " + ownerType);
		Log.consoleLog(ifr, "HighmarkAPI:checkEMIKnockOffAccFilterStatus->excludedOwners: " + excludedOwners);

		if ("Individual".equalsIgnoreCase(ownerType)) {
			ownerType = "1";
		} else if ("Authorized User".equalsIgnoreCase(ownerType)) {
			ownerType = "2";
		} else if ("Guarantor".equalsIgnoreCase(ownerType)) {
			ownerType = "3";
		} else if ("Joint".equalsIgnoreCase(ownerType)) {
			ownerType = "4";
		} else {
			ownerType = "5";
		}
		if ("Active".equalsIgnoreCase(accountStatus)) {
			Log.consoleLog(ifr, "HighmarkAPI:checkEMIKnockOffAccFilterStatus->open status: " + accountStatus);
			String[] excludedAccounts = excludedAccnts.split(",");
			for (String accnt : excludedAccounts) {
				if (accnt.equals(accountType)) {
					Log.consoleLog(ifr, "HighmarkAPI:checkEMIKnockOffAccFilterStatus->excludedAccounts: " + accnt);
					return false;
				}
			}
			String[] excludedOwnerTypes = excludedOwners.split(",");
			for (String typeOfOwner : excludedOwnerTypes) {
				if (typeOfOwner.equals(ownerType)) {
					Log.consoleLog(ifr, "HighmarkAPI:checkEMIKnockOffAccFilterStatus->excludedOwnerTypes: " + typeOfOwner);
					return false;
				}
			}
			return true;
		} else {
			Log.consoleLog(ifr, "HighmarkAPI:checkEMIKnockOffAccFilterStatus->open status: " + accountStatus);
			return false;
		}
	}

	private boolean checkWrittenoffSettledStatus(IFormReference ifr, String writtenOffTag,
			String excludedWrittenOff) {

		Log.consoleLog(ifr, "HighmarkAPI:checkWrittenoffSettledStatus->#checkWrittenoffSettledStatus started...");
		Log.consoleLog(ifr, "HighmarkAPI:checkWrittenoffSettledStatus->writtenOffTag: " + writtenOffTag);
		Log.consoleLog(ifr, "HighmarkAPI:checkWrittenoffSettledStatus->excludedWrittenOff: " + excludedWrittenOff);

		String[] excludedWrittenTypes = excludedWrittenOff.split(",");
		for (String typeWrittenOff : excludedWrittenTypes) {
			if (typeWrittenOff.equals(writtenOffTag)) {
				Log.consoleLog(ifr, "HighmarkAPI:checkWrittenoffSettledStatus->writtenOffTag: " + writtenOffTag);
				return false;
			}
		}
		return true;
	}



	public int calculateAge(String dateOfBirth,IFormReference ifr  ) {
		String[] possibleFormats = {
				"yyyy-MM-dd HH:mm:ss",
				"yyyy-MM-dd'T'HH:mm:ss",
				"yyyy-MM-dd",
				"dd-MM-yyyy",
				"dd/MM/yyyy",
				"MM/dd/yyyy",
				"dd MMM yyyy",
				"dd-MM-yyyy HH:mm:ss"
		};

		Date birthDate = null;

		for (String format : possibleFormats) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				sdf.setLenient(false);
				birthDate = sdf.parse(dateOfBirth);
				break;
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception in date of birth: " + e);
			}
		}

		if (birthDate == null) {
			Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Invalid date format: " + dateOfBirth);
			return -1;
		}


		SimpleDateFormat outputFormatter = new SimpleDateFormat("dd-MM-yyyy");
		String outputDate = outputFormatter.format(birthDate);
		Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->outputDate: " + outputDate);


		Calendar birthCal = Calendar.getInstance();
		birthCal.setTime(birthDate);

		Calendar today = Calendar.getInstance();
		int ageYears = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);

		if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) {
			ageYears--;
		}

		Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->ageYears: " + ageYears);
		return ageYears;
	}
	
	public String getHighMarkCIBILScore2(IFormReference ifr, String ProcessInstanceId, String aadharNo,
            String productType, String loanAmount, String applicantType)
            throws ParseException { // Added By Vikash Mehta
        Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->inside HighMark: ");
        String apiName = "HighMark";
        Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->apiName: " + apiName);
        Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->inside aadharNo: " + aadharNo);
        String request = "";
        String responseBody = "";
        boolean NoCIC = true;
        String installmentAmount = "";
        int consolidated_emiAmnt = 0;
        int totalNonEMICount = 0;
        String combinedPaymentHistory = "";
        String writtenOffSettledStatus = "";
        String writeOff = "No";
        String settled = "No";
        String NPA = "No";
        int dpdCount = 0;
        String DPD = "";

        String Value = "";
        String insertionOrderid = "";
        int age = 0;


        try {

            String firstName = "", lastName = "", dateofBirth = "", panNumber = "", customerId = "", addressLine1 = "", addressLine2 = "",
                    addressLine3 = "", city = "", state = "", pincode = "", gender = "", gendercode = "", stateCode = "", mobileNo = "", DOB = "", fatherName = "", EMAILID = "";

            String Query1 = "";
            String Query2 = "";
            String MobileNumber="";
            String cleanedMobileNo="";

            String intertionOrderIDArray[] = applicantType.split("~");
            Log.consoleLog(ifr, "intertionOrderIDArray:: "+intertionOrderIDArray);

            String finalIntOrderID = intertionOrderIDArray[1].trim();
            Log.consoleLog(ifr, "finalIntOrderID:: "+finalIntOrderID);

            String CASKeysQuery = "select a.mobileno, b.customerid from LOS_L_BASIC_INFO_I a join los_nl_basic_info b on a.f_key=b.f_key and b.pid='"+ProcessInstanceId+"' and b.insertionorderid='"+finalIntOrderID+"' and b.applicanttype in ('CB', 'G')";
            Log.consoleLog(ifr, "CASKeysQuery:: "+CASKeysQuery);
            List<List<String>> CASKeysQueryRes = cf.mExecuteQuery(ifr, CASKeysQuery, "Get mobile number and customer Id of CB:");
            Log.consoleLog(ifr, "CASKeysQueryRes:: "+CASKeysQueryRes);
            if (!CASKeysQueryRes.isEmpty()) {
                MobileNumber =  CASKeysQueryRes.get(0).get(0);
                Log.consoleLog(ifr, "MobileNumber CASKeysQueryRes:: "+MobileNumber);
                customerId =  CASKeysQueryRes.get(0).get(1);
                Log.consoleLog(ifr, "customerId CASKeysQueryRes:: "+customerId);
            }
            if (MobileNumber != null && MobileNumber.length() > 10) {
                cleanedMobileNo = MobileNumber.substring(MobileNumber.length() - 10);
            }
            CustomerAccountSummary cas = new CustomerAccountSummary();
            HashMap<String, String> customerdetails = new HashMap<>();
            customerdetails.put("MobileNumber", cleanedMobileNo);
            customerdetails.put("customerId", customerId);
            Log.consoleLog(ifr, "customerdetails:: "+customerdetails);
            String CASResponse = cas.getCustomerAccountSummary(ifr, customerdetails);
            if(CASResponse.equalsIgnoreCase(RLOS_Constants.ERROR)){
                return "Fetching Customer Account Summary Unsuccessful";
            }
            Log.consoleLog(ifr, "CASResponse:: "+CASResponse);
            JSONParser jparser = new JSONParser();
            JSONObject CASObject = (JSONObject) jparser.parse(CASResponse);
            String tempFirstName = CASObject.get("CustomerFirstName").toString();
            Log.consoleLog(ifr, "tempFirstName:: "+tempFirstName);
            String tempLastName = CASObject.get("CustomerLastName").toString();
            Log.consoleLog(ifr, "tempLastName:: "+tempLastName);
            String tempPan = CASObject.get("PanNumber").toString();
            Log.consoleLog(ifr, "tempPan:: "+tempPan);
            String tempDateOfBirth = CASObject.get("DateOfBirth").toString();
            Log.consoleLog(ifr, "tempDateOfBirth:: "+tempDateOfBirth);
            String tempGender = CASObject.get("CustomerSex").toString();
            Log.consoleLog(ifr, "tempGender:: "+tempGender);
            String tempAadhar = CASObject.get("AadharNo").toString();
            Log.consoleLog(ifr, "tempAadhar:: "+tempAadhar);
            String tempFatherName = CASObject.get("fatherName").toString();
            Log.consoleLog(ifr, "tempFatherName:: "+tempFatherName);
            String tempEmailID = CASObject.get("emailID").toString();
            Log.consoleLog(ifr, "tempEmailID:: "+tempEmailID);

//            Query1 = "SELECT CUSTOMERID,CUSTOMERFIRSTNAME,CUSTOMERLASTNAME,PANNUMBER,DATEOFBIRTH,GENDER,MOBILENUMBER,FATHERNAME,EMAILID "
//                    + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";

            Query2 = "select a.LINE1,a.LINE2,a.LINE3,a.CITY_TOWN_VILLAGE,a.STATE,a.PINCODE from LOS_NL_ADDRESS a join los_nl_basic_info b on a.f_key=b.f_key and b.pid='"+ProcessInstanceId+"' and b.insertionorderid='"+finalIntOrderID+"' and b.applicanttype in ('CB', 'G')  and a.ADDRESSTYPE='P'";


            String pattern = ConfProperty.getCommonPropertyValue("NamePattern");
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->#Result pattern: " + pattern);

            firstName = tempFirstName.replaceAll("[ .]", "");
            lastName = tempLastName.replaceAll("[ .]", "");

            if (lastName.equalsIgnoreCase("{}")) {
                lastName = "";
            }
            if (lastName.equalsIgnoreCase("{")) {
                lastName = "";
            }
            if (lastName.equalsIgnoreCase("}")) {
                lastName = "";
            }
            if (firstName.length() < 2) {
                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->firstName: " + firstName);
                return RLOS_Constants.ERROR + "Name is less than 1 character ";
            } else if (!Pattern.matches(pattern, firstName)) {
                return RLOS_Constants.ERROR + "Name contains invalid special characters";
            }
            dateofBirth = tempDateOfBirth;
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->****dateofBirth: " + dateofBirth);
            gender = tempGender;
            mobileNo = MobileNumber;
            fatherName = tempFatherName;
            EMAILID = tempEmailID;

            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Query2: " + Query2);
            List< List< String>> Result1 = ifr.getDataFromDB(Query2);
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Result1: " + Result1.toString());

            if (Result1.size() > 0) {
                addressLine1 = Result1.get(0).get(0);
                addressLine2 = Result1.get(0).get(1);
                addressLine3 = Result1.get(0).get(2);
                city = Result1.get(0).get(3);
                state = Result1.get(0).get(4);
                pincode = Result1.get(0).get(5);
            }

            // Process all KYC records
            aadharNo=tempAadhar;
            panNumber = tempPan;
            String voterId = "";


            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->AddressLine1: " + addressLine1);
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->AddressLine2: " + addressLine2);
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->AddressLine3: " + addressLine3);
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->City: " + city);
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->State: " + state);
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Pincode: " + pincode);

            String stateCodeQ = "SELECT STATE_CODE FROM LOS_MST_STATE WHERE "
                    + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + state + "')) AND ROWNUM=1";
            Log.consoleLog(ifr, "stateCodeQ==>" + stateCodeQ);
            List< List< String>> stateCodeR = ifr.getDataFromDB(stateCodeQ);
            Log.consoleLog(ifr, "#stateCodeR===>" + stateCodeR.toString());
            if (stateCodeR.size() > 0) {
                state = stateCodeR.get(0).get(0);
            }

            if (gender.equalsIgnoreCase("MALE")) {
                gender = "G01";
            } else if (gender.equalsIgnoreCase("FEMALE")) {
                gender = "G02";
            }
            String outputDate = "";
            int ageYears = 0;
            if (dateofBirth != null && !dateofBirth.trim().isEmpty()) {
                try {

                    age = calculateAge(dateofBirth, ifr);

                } catch (Exception ex) {
                    Log.consoleLog(ifr, "Exception in calculate Age: " + ex);// YYYY-MM-DD===>YYYYMMDD*/

                }
            }
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->outputDate: " + outputDate);// YYYY-MM-DD===>YYYYMMDD
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->ageYears: " + ageYears);// YYYY-MM-DD===>YYYYMMDD

            DOB = dateofBirth;

            Date Date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String formattedDate = dateFormat.format(Date);

            Date PresentDate = new Date();
            SimpleDateFormat dateFormatt = new SimpleDateFormat("ddMMyyHHmmssSSS");

            String excludedEMIAccnts = "Auto Overdraft,Loan Against Shares / Securities,Gold Loan,Credit Card,Charge Card,Fleet Card,Loan against Card,Loan Against Bank Deposits,OD on Savings Account,Non-Funded Credit Facility,Business Non-Funded Credit Facility General,Business Non-Funded Credit Facility-Priority Sector- Small Business,Business Non-Funded Credit Facility-Priority Sector-Agriculture,Business Non-Funded Credit Facility-Priority Sector-Others,Business Loan Against Bank Deposits,Telco Wireless,Telco Broadband,Telco Landline,Secured Credit Card,Corporate Credit Card,Kisan Credit Card,Loan on Credit Card,Prime Minister Jaan Dhan Yojana - Overdraft,JLG Group,JLG Individual";
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->excludedEMIAccnts: " + excludedEMIAccnts);
            String excludedOwners = "2,3,5";
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->excludedOwners: " + excludedOwners);
            String writtenoffSettled = "Restructured Loan,Restructured Loan,Account Sold,Account Purchased,Account Purchased and Restructured";
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->writtenoffSettled: " + writtenoffSettled);


            String InquiryPurpose = "";
            
            InquiryPurpose = pcm.getConstantValue(ifr, "HIGHMARK", "Housing Loan");
            if(InquiryPurpose.isEmpty()){
                 return RLOS_Constants.ERROR;
            }
            String uniqueValue = ProcessInstanceId.replace("HLOS-", "");
            SimpleDateFormat formatter_unq = new SimpleDateFormat("ddMMyyyyHHmmssSSS"); // SSS for milliseconds
            String uniqueDate = formatter_unq.format(new Date()); // use current time
            Log.consoleLog(ifr, "No Voter ID found in KYC records");

            String unique = uniqueDate.replaceAll("[^a-zA-Z0-9 ]", "");
            uniqueValue = "canaranewHLOS" + uniqueValue + unique.replace(" ", "");
            if (panNumber == null || panNumber.isEmpty()) {
                Log.consoleLog(ifr, "No PAN number found in KYC records");
                panNumber = "";
            }
            if (voterId == null || voterId.isEmpty()) {
                Log.consoleLog(ifr, "No Voter ID found in KYC records");
                voterId = "";
            }
            if (panNumber == null || panNumber.equals("")) {
                panNumber = "";
                Log.consoleLog(ifr, "inside if Valid PAN for processing: " + panNumber);
            } else if (panNumber.length() >= 4 && panNumber.charAt(3) == 'P') {

                panNumber = panNumber;
                Log.consoleLog(ifr, "Valid PAN for processing: " + panNumber);
            } else {
                Log.consoleLog(ifr, "Valid PAN for processing: " + "PAN number Not Listed ");
                return RLOS_Constants.ERROR;
            }

            String mbrid = pcm.getConstantValue(ifr, "HIGHMARK", "MBRID"); ///NAB0000095
            String kendraId = pcm.getConstantValue(ifr, "HIGHMARK", "KENDRA_ID");
            String credt_rpt_trn_id = pcm.getConstantValue(ifr, "HIGHMARK", "CREDT_RPT_TRN_ID");
            String credt_rpt_id = pcm.getConstantValue(ifr, "HIGHMARK", "CREDT_RPT_ID");
            String TBranch = cm.GetHomeBranchCodeSHL(ifr);
            TBranch = TBranch.replaceFirst("^0+(?!$)", "");

            StringBuilder idsBlock = new StringBuilder();

            if ((voterId != null && !voterId.trim().isEmpty())
                    || (aadharNo != null && !aadharNo.trim().isEmpty())
                    || (panNumber != null && !panNumber.trim().isEmpty())) {

                idsBlock.append("                    \"IDS\": {\r\n")
                        .append("                        \"ID\": [\r\n");

                boolean commaNeeded = false;

                if (voterId != null && !voterId.trim().isEmpty()) {
                    idsBlock.append("                            {\r\n")
                            .append("                                \"TYPE\": \"ID02\",\r\n")
                            .append("                                \"VALUE\": \"").append(voterId).append("\"\r\n")
                            .append("                            }");
                    commaNeeded = true;
                }

                if (aadharNo != null && !aadharNo.trim().isEmpty()) {
                    if (commaNeeded) {
                        idsBlock.append(",\r\n");
                    }
                    idsBlock.append("                            {\r\n")
                            .append("                                \"TYPE\": \"ID03\",\r\n")
                            .append("                                \"VALUE\": \"").append(aadharNo).append("\"\r\n")
                            .append("                            }");
                    commaNeeded = true;
                }

                if (panNumber != null && !panNumber.trim().isEmpty()) {
                    if (commaNeeded) {
                        idsBlock.append(",\r\n");
                    }
                    idsBlock.append("                            {\r\n")
                            .append("                                \"TYPE\": \"ID07\",\r\n")
                            .append("                                \"VALUE\": \"").append(panNumber).append("\"\r\n")
                            .append("                            }");
                }

                idsBlock.append("\r\n                        ]\r\n")
                        .append("                    },\r\n");
            }

            request
                    = "{\r\n"
                    + "    \"REQUEST-REQUEST-FILE\": {\r\n"
                    + "        \"HEADER-SEGMENT\": {\r\n"
                    + "            \"AUTH-FLG\": \"Y\",\r\n"
                    + "            \"AUTH-TITLE\": \"USER\",\r\n"
                    + "            \"CONSUMER\": {\r\n"
                    + "                \"INDV\": true,\r\n"
                    + "                \"SCORE\": true\r\n"
                    + "            },\r\n"
                    + "            \"INQ-DT-TM\": \"" + formattedDate + "\",\r\n"
                    + "            \"IOI\": true,\r\n"
                    + "            \"LOS-NAME\": \"\",\r\n"
                    + "            \"LOS-VENDER\": \" \",\r\n"
                    + "            \"LOS-VERSION\": 1,\r\n"
                    + "            \"MEMBER-PRE-OVERRIDE\": \"N\",\r\n"
                    + "            \"REQ-SERVICE-TYPE\": \"\",\r\n"
                    + "            \"MFI\": {\r\n"
                    + "                \"GROUP\": true,\r\n"
                    + "                \"INDV\": true,\r\n"
                    + "                \"SCORE\": false\r\n"
                    + "            },\r\n"
                    + "            \"REQ-ACTN-TYP\": \"SUBMIT\",\r\n"
                    + "            \"REQ-VOL-TYP\": \"C01\",\r\n"
                    + "            \"RES-FRMT\": \"JSON/HTML\",\r\n"
                    + "            \"RES-FRMT-EMBD\": \"Y\",\r\n"
                    + "            \"SUB-MBR-ID\": \"CANARA BANK\",\r\n"
                    + "            \"TEST-FLG\": \"N\"\r\n"
                    + "        },\r\n"
                    + "        \"INQUIRY\": [\r\n"
                    + "            {\r\n"
                    + "                \"ADDRESS-SEGMENT\": {\r\n"
                    + "                    \"ADDRESS\": [\r\n"
                    + "                        {\r\n"
                    + "                            \"ADDRESS-1\": \"" + addressLine1 + "\",\r\n"
                    + "                            \"CITY\": \"" + city + "\",\r\n"
                    + "                            \"PIN\": " + pincode + ",\r\n"
                    + "                            \"STATE\": \"" + state + "\",\r\n"
                    + "                            \"TYPE\": \"D01\"\r\n"
                    + "                        },\r\n"
                    + "                        {\r\n"
                    + "                            \"ADDRESS-1\": \"" + addressLine1 + "\",\r\n"
                    + "                            \"CITY\": \"" + city + "\",\r\n"
                    + "                            \"PIN\": " + pincode + ",\r\n"
                    + "                            \"STATE\": \"" + state + "\",\r\n"
                    + "                            \"TYPE\": \"D02\"\r\n"
                    + "                        }\r\n"
                    + "                    ]\r\n"
                    + "                },\r\n"
                    + "                \"APPLICANT-SEGMENT\": {\r\n"
                    + "                    \"APPLICANT-NAME\": {\r\n"
                    + "                        \"NAME1\": \"" + firstName + "\",\r\n"
                    + "                        \"NAME2\": \"\",\r\n"
                    + "                        \"NAME3\": \"" + lastName + "\"\r\n"
                    + "                    },\r\n"
                    + "                    \"DOB\": {\r\n"
                    + "                        \"AGE\": \"" + age + "\",\r\n"
                    + "                        \"AGE-AS-ON\": \"\",\r\n"
                    + "                        \"DOB-DATE\": \"" + DOB + "\"\r\n"
                    + "                    },\r\n"
                    + "                    \"EMAILS\": {\r\n"
                    + "                        \"EMAIL\": [\r\n"
                    + "                            \"" + EMAILID + "\",\r\n"
                    + "                            \"" + EMAILID + "\"\r\n"
                    + "                        ]\r\n"
                    + "                    },\r\n"
                    + "                    \"GENDER\": \"" + gender + "\",\r\n"
                    + idsBlock.toString()
                    + "                    \"KEY-PERSON\": {\r\n"
                    + "                        \"NAME\": \"\",\r\n"
                    + "                        \"TYPE\": \"\"\r\n"
                    + "                    },\r\n"
                    + "                    \"NOMINEE\": {\r\n"
                    + "                        \"NAME\": \"\",\r\n"
                    + "                        \"TYPE\": \"\"\r\n"
                    + "                    },\r\n"
                    + "                    \"PHONES\": {\r\n"
                    + "                        \"PHONE\": [\r\n"
                    + "                            {\r\n"
                    + "                                \"TELE-NO\": " + mobileNo + ",\r\n"
                    + "                                \"TELE-NO-TYPE\": \"P03\"\r\n"
                    + "                            },\r\n"
                    + "                            {\r\n"
                    + "                                \"TELE-NO\": " + cleanedMobileNo + ",\r\n"
                    + "                                \"TELE-NO-TYPE\": \"P01\"\r\n"
                    + "                            }\r\n"
                    + "                        ]\r\n"
                    + "                    },\r\n"
                    + "                    \"RELATIONS\": {\r\n"
                    + "                        \"RELATION\": [\r\n"
                    + "                            {\r\n"
                    + "                                \"NAME\": \"K01\",\r\n"
                    + "                                \"TYPE\": \"" + fatherName + "\"\r\n"
                    + "                            },\r\n"
                    + "                            {\r\n"
                    + "                                \"NAME\": \"K02\",\r\n"
                    + "                                \"TYPE\": \"" + fatherName + "\"\r\n"
                    + "                            }\r\n"
                    + "                        ]\r\n"
                    + "                    }\r\n"
                    + "                },\r\n"
                    + "                \"APPLICATION-SEGMENT\": {\r\n"
                    + "                    \"BRANCH-ID\": " + TBranch + ",\r\n"
                    + "                    \"CREDIT-INQUIRY-STAGE\": \"PRE-DISB\",\r\n"
                    + "                    \"CREDT-INQ-PURPS-TYP\": \"ACCT-ORIG\",\r\n"
                    + "                    \"CREDT-INQ-PURPS-TYP-DESC\": \"" + InquiryPurpose + "\",\r\n"
                    + "                    \"CREDT-REQ-TYP\": \"INDV\",\r\n"
                    + "                    \"CREDT-RPT-ID\": " + credt_rpt_id + ",\r\n"
                    + "                    \"CREDT-RPT-TRN-DT-TM\": \"" + formattedDate + "\",\r\n"
                    + "                    \"CREDT-RPT-TRN-ID\": " + credt_rpt_trn_id + ",\r\n"
                    + "                    \"INQUIRY-UNIQUE-REF-NO\": \"" + uniqueValue + "\",\r\n"
                    + "                    \"KENDRA-ID\": " + kendraId + ",\r\n"
                    + "                    \"LOAN-AMOUNT\": " + loanAmount + ",\r\n"
                    + "                    \"LOS-APP-ID\": \"" + ProcessInstanceId + "\",\r\n"
                    + "                    \"MBR-ID\": \"" + mbrid + "\"\r\n"
                    + "                }\r\n"
                    + "            }\r\n"
                    + "        ]\r\n"
                    + "    }\r\n"
                    + "}";

            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->request: " + request);


            responseBody = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Response: " + responseBody);

            Log.consoleLog(ifr, "HIGHMARK API RESPONSE==>" + responseBody);
            String cicReportGenReq = pcm.getConstantValue(ifr, "HIGHMARK", "CICREPORTGENREQ");
            if (!responseBody.equalsIgnoreCase("{}")) {

                JSONParser parser = new JSONParser();
                JSONObject OutputJSON = (JSONObject) parser.parse(responseBody);

                JSONObject body = (JSONObject) OutputJSON.get("body");
                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->body:" + body.toString());

                JSONObject INDV_REPORT_FILE = (JSONObject) body.get("INDV-REPORT-FILE");
                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->INDV_REPORT_FILE: " + INDV_REPORT_FILE.toString());

                JSONArray INDV_REPORTS = (JSONArray) INDV_REPORT_FILE.get("INDV-REPORTS");
                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->INDV_REPORTS: " + INDV_REPORTS.toString());

                for (int INDVReportDataLstCount = 0; INDVReportDataLstCount < INDV_REPORTS
                        .size(); INDVReportDataLstCount++) {
                    JSONObject INDVReportDataVal = (JSONObject) INDV_REPORTS.get(INDVReportDataLstCount);

                    JSONObject INDV_REPORT = (JSONObject) INDVReportDataVal.get("INDV-REPORT");

                    Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->INDV_REPORT: " + INDV_REPORT.toString());
                    if (cicReportGenReq.equalsIgnoreCase("Y")) {
                        JSONObject printableReport = (JSONObject) INDV_REPORT.get("PRINTABLE-REPORT");
                        //if (!(cf.getJsonValue(bodyObj, "encodedBase64").equalsIgnoreCase(""))) {
                        //String encodedB64 = cf.getJsonValue(bodyObj, "encodedBase64");
                        String encodedB64 = printableReport.get("CONTENT").toString();
                        String generateReportStatus = cm.generateReport(ifr, ProcessInstanceId, "HIGHMARK", encodedB64, "NGREPORTTOOL_HIGHMARK");
                        Log.consoleLog(ifr, "generateReportStatus==>" + generateReportStatus);
                        cm.updateCICReportStatus(ifr, "HIGHMARK", generateReportStatus, applicantType);
                    }

                    if (INDV_REPORT.containsKey("RESPONSES")) {
                        JSONArray responses = (JSONArray) INDV_REPORT.get("RESPONSES");
                        NoCIC = false;

                        // Loop through RESPONSES and extract LOAN-DETAILS
                        for (Object responseObj : responses) {
                            JSONObject response = (JSONObject) responseObj;
                            JSONObject loanDetails = (JSONObject) response.get("LOAN-DETAILS");
                            if (loanDetails != null) {
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->loanDetails: " + loanDetails.toJSONString());
                                String accountStatus = loanDetails.get("ACCOUNT-STATUS").toString();
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->accountStatus: " + accountStatus);
                                String accountType = loanDetails.get("ACCT-TYPE").toString();
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->accountType: " + accountType);
                                String ownershipInd = loanDetails.get("OWNERSHIP-IND").toString();
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->ownershipInd: " + ownershipInd);
                                String dateReported = loanDetails.get("DATE-REPORTED").toString();
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->dateReported: " + dateReported);
                                if (loanDetails.containsKey("INSTALLMENT-AMT")
                                        && loanDetails.get("INSTALLMENT-AMT") != null) {
                                    installmentAmount = loanDetails.get("INSTALLMENT-AMT").toString();
                                }
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->installmentAmount: " + installmentAmount);
                                String paymentHistory = loanDetails.get("COMBINED-PAYMENT-HISTORY").toString();
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->paymentHistory: " + paymentHistory);

                                //payment history (dpd)
                                if ("Individual".equalsIgnoreCase(ownershipInd) || "Joint".equalsIgnoreCase(ownershipInd)) {
                                    String[] entries = paymentHistory.split("\\|");
                                    LocalDate today = LocalDate.now();
                                    LocalDate twelveMonthsAgo = today.minus(12, ChronoUnit.MONTHS);
                                    // Month mapping (Move outside loop)
                                    Map<String, Integer> monthMap = new HashMap<>();
                                    monthMap.put("Jan", 1);
                                    monthMap.put("Feb", 2);
                                    monthMap.put("Mar", 3);
                                    monthMap.put("Apr", 4);
                                    monthMap.put("May", 5);
                                    monthMap.put("Jun", 6);
                                    monthMap.put("Jul", 7);
                                    monthMap.put("Aug", 8);
                                    monthMap.put("Sep", 9);
                                    monthMap.put("Oct", 10);
                                    monthMap.put("Nov", 11);
                                    monthMap.put("Dec", 12);

                                    for (String entry : entries) {
                                        Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->entry: " + entry);
                                        String[] monthYearValue = entry.split(":");
                                        String monthStr = monthYearValue[0];
                                        // Further split "2022,000/STD" to extract only the year
                                        String[] yearAndValue = monthYearValue[1].split(",");
                                        int year = Integer.parseInt(yearAndValue[0]); // Extracting year only
                                        int month = monthMap.getOrDefault(monthStr, -1);
                                        LocalDate inputDate1 = LocalDate.of(year, month, 1);
                                        // Check if input date is within the last 12 months
                                        boolean isWithinLast12Months = !inputDate1.isBefore(twelveMonthsAgo) && !inputDate1.isAfter(today);
                                        if (isWithinLast12Months) {
                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Date is within 12 months::: ");
                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->yearAndValue[1] " + yearAndValue[1]);
                                            String[] paymenthistory = yearAndValue[1].split("/");
                                            combinedPaymentHistory = combinedPaymentHistory + paymenthistory[0];
                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->paymentHistory " + paymentHistory);
                                            dpdCount++;
                                        }
                                    }
                                }
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->combinedPaymentHistory: " + combinedPaymentHistory);

                                //emi
                                boolean emiStatus = checkEMIKnockOffAccFilterStatus(ifr, accountType,
                                        excludedEMIAccnts, ownershipInd, excludedOwners, accountStatus);
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->emiStatus: " + emiStatus);
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->InstallmentAmount: " + installmentAmount);
                                if (emiStatus) {
                                    if (!installmentAmount.isEmpty()) {
                                        int amount = Integer.parseInt(installmentAmount.replaceAll("[^0-9]", ""));
                                        if (amount < 0) {
                                            totalNonEMICount++;
                                        }
                                        consolidated_emiAmnt += amount;
                                    }
                                }
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->consolidated_emiAmnt: " + consolidated_emiAmnt);

                                //history of setteled and histroy of writeoff
                                if ("WRITTEN-OFF".equalsIgnoreCase(accountStatus) || "SETTLED".equalsIgnoreCase(accountStatus)) {
                                    Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->inisde accountstatus: " + accountStatus);
                                    if (loanDetails.containsKey("WRITTEN-OFF_SETTLED-STATUS")) {
                                        writtenOffSettledStatus = loanDetails.get("WRITTEN-OFF_SETTLED-STATUS").toString();
                                    }
                                    boolean writtenOffStatus = checkWrittenoffSettledStatus(ifr, writtenOffSettledStatus,
                                            writtenoffSettled);
                                    Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->writtenOffStatus: " + writtenOffStatus);
                                    if (writtenOffStatus) {
                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                        LocalDate givenDate = LocalDate.parse(dateReported, formatter);
                                        LocalDate today = LocalDate.now();
                                        long monthsBetween = ChronoUnit.MONTHS.between(givenDate, today);
                                        if (Math.abs(monthsBetween) <= 60) {
                                            Log.consoleLog(ifr, "The date is within 60 months from today.");
                                            if ("Individual".equalsIgnoreCase(ownershipInd) || "Joint".equalsIgnoreCase(ownershipInd)) {
                                                writeOff = "Yes";
                                                settled = "Yes";
                                            }
                                        }

                                    }
                                }
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->writeOff: " + writeOff);
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->settled: " + settled);

                                //histroy of NPA
                                if ("Individual".equalsIgnoreCase(ownershipInd) || "Guarantor".equalsIgnoreCase(ownershipInd)) {
                                    String[] entries = paymentHistory.split("\\|");
                                    LocalDate today = LocalDate.now();
                                    LocalDate sixtyMonthsAgo = today.minus(60, ChronoUnit.MONTHS);
                                    // Month mapping (Move outside loop)
                                    Map<String, Integer> monthMap = new HashMap<>();
                                    monthMap.put("Jan", 1);
                                    monthMap.put("Feb", 2);
                                    monthMap.put("Mar", 3);
                                    monthMap.put("Apr", 4);
                                    monthMap.put("May", 5);
                                    monthMap.put("Jun", 6);
                                    monthMap.put("Jul", 7);
                                    monthMap.put("Aug", 8);
                                    monthMap.put("Sep", 9);
                                    monthMap.put("Oct", 10);
                                    monthMap.put("Nov", 11);
                                    monthMap.put("Dec", 12);

                                    for (String entry : entries) {
                                        Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->entry: " + entry);
                                        String[] monthYearValue = entry.split(":");
                                        if (monthYearValue.length < 2) {
                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Invalid entry format: " + entry);
                                            continue;
                                        }
                                        String monthStr = monthYearValue[0];
                                        // Further split "2022,000/STD" to extract only the year
                                        String[] yearAndValue = monthYearValue[1].split(",");
                                        if (yearAndValue.length < 1) {
                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Invalid format for year: " + monthYearValue[1]);
                                            continue;
                                        }

                                        int year;
                                        try {
                                            year = Integer.parseInt(yearAndValue[0]); // Extracting year only
                                        } catch (NumberFormatException e) {
                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Error parsing year from: " + yearAndValue[0]);
                                            continue;
                                        }
                                        int month = monthMap.getOrDefault(monthStr, -1);
                                        if (month == -1) {
                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Invalid month name: " + monthStr);
                                            continue;
                                        }
                                        LocalDate inputDate1 = LocalDate.of(year, month, 1);
                                        // Check if input date is within the last 12 months
                                        boolean isWithinLast60Months = !inputDate1.isBefore(sixtyMonthsAgo) && !inputDate1.isAfter(today);
                                        if (isWithinLast60Months) {
                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Date is within 60 months::: ");
                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->yearAndValue[0] " + yearAndValue[1]);
                                            String[] paymenthistory = yearAndValue[1].split("/");
                                            String NPAcheck = paymenthistory[1];
                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->NPAcheck:: " + NPAcheck);
                                            if ("SUB".endsWith(NPAcheck) || "DBT".equalsIgnoreCase(NPAcheck) || "LOS".equalsIgnoreCase(NPAcheck)) {
                                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->NPA is found: ");
                                                NPA = "Yes";
                                            }
                                        }
                                    }
                                }
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->NPA: " + NPA);
                            }
                        }

                        JSONArray ScoreDetails = (JSONArray) INDV_REPORT.get("SCORES");
                        Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->ScoreDetails: " + ScoreDetails);

                        for (int ScoreDetailsCount = 0; ScoreDetailsCount < ScoreDetails.size(); ScoreDetailsCount++) {
                            JSONObject ScoreDetailsObj = (JSONObject) ScoreDetails.get(ScoreDetailsCount);
                            if (ScoreDetailsObj.containsKey("SCORE-VALUE") && ScoreDetailsObj.get("SCORE-VALUE") != null) {
                                Value = ScoreDetailsObj.get("SCORE-VALUE").toString();
                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Value: " + Value);
                            }

                        }
                    } else {
                        NoCIC = true;
                        Value = "0";
                    }
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDateTime dateTime = LocalDateTime.now();
                String enqDate = dateTime.format(formatter);
                String newToCredit = "No";
                if (dpdCount > 0) {
                    DPD = String.valueOf(dpdCount);
                }

                String qryCICDataUpdate1 = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,"
                        + "CICNPACHECK,CICOVERDUE,WRITEOFF,"
                        + "BUREAUTYPE,TOTEMIAMOUNT,PAYHISTORYCOMBINED,"
                        + "APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,"
                        + "GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED,APPLICANT_UID) "
                        + "values('" + ProcessInstanceId + "','" + Value + "',"
                        + "'" + NPA + "','" + DPD + "','" + writeOff + "',"
                        + "'HM',"
                        + "'" + consolidated_emiAmnt + "',"
                        + "'" + combinedPaymentHistory + "',"
                        + "'" + applicantType + "',"
                        + "'" + totalNonEMICount + "','" + settled + "','"
                        + NPA + "','" + NPA + "','" + settled + "',"
                        + "SYSDATE,'" + insertionOrderid + "')";
                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Insert query: " + qryCICDataUpdate1);
                ifr.saveDataInDB(qryCICDataUpdate1);

//                String query = "INSERT INTO LOS_TRN_CREDITHISTORY (PID,CUSTOMERID,MOBILENO,PRODUCTCODE,"
//                        + "APPREFNO,APPLICANTTYPE,APPLICANTID,\n"
//                        + "BUREAUTYPE,BUREAUCODE,SERVICECODE,LAP_EXIST,\n"
//                        + "CIC_SCORE,TOTAL_EMIAMOUNT,NEWTOCREDITYN,DTINSERTED,DTUPDATED)\n"
//                        + "VALUES('" + ProcessInstanceId + "',"
//                        + "'','" + mobileNo + "','"+productCode+"','" + pcm.getApplicationRefNumber(ifr) + "','" + applicantType + "','" + insertionOrderid + "',\n"
//                        + "'EXT','EF','CNS','" + "" + "','" + Value + "',"
//                        //+ "'" + NPA + "','" + dpd + "','" + writeOFF + "','"
//                        + "'" + "" + "','" + newToCredit + "',SYSDATE,SYSDATE)";
//                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Insert query for LOS_TRN_CREDITHISTORY: " + query);
//                int queryResult = ifr.saveDataInDB(query);
//                Log.consoleLog(ifr, "HighmarkAPI:getHighMark****->queryResult: " + queryResult);
                Log.consoleLog(ifr, "applicantType()():applicantType****->applicantType: " + applicantType);
                Log.consoleLog(ifr, "HighmarkAPI:getHighMark****->insertionOrderid: " + insertionOrderid);
                crg.crgGenHighMark(ifr, responseBody, ProcessInstanceId, apiName, applicantType, insertionOrderid, NoCIC);
                return Value;

            } else {
                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->No response from Server..");
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Exception/HighMark===>" + e);
            Log.errorLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Exception/HighMark===>" + e);
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Exception StackTrace:::" + errors);

        } finally {
            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Capture Equifax REquest and Response Calling!!!!!");
            cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Highmark_API", request, responseBody, "", "", "",
                    applicantType);
        }
        return RLOS_Constants.ERROR;
    }
	

//	public String getHighMarkCIBILScore2(IFormReference ifr, String ProcessInstanceId, String aadharNo,
//            String productType, String loanAmount, String applicantType)
//            throws ParseException {
//        Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->inside HighMark: ");
//        String apiName = "HighMark";
//        Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->apiName: " + apiName);
//        Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->inside aadharNo: " + aadharNo);
//        String request = "";
//        String responseBody = "";
//        boolean NoCIC = true;
//        String installmentAmount = "";
//        int consolidated_emiAmnt = 0;
//        int totalNonEMICount = 0;
//        String combinedPaymentHistory = "";
//        String writtenOffSettledStatus = "";
//        String writeOff = "No";
//        String settled = "No";
//        String NPA = "No";
//        int dpdCount = 0;
//        String DPD = "";
//
//        String Value = "";
//        String insertionOrderid = "";
//        int age = 0;
//
//
//        try {
//
//            String firstName = "", lastName = "", dateofBirth = "", panNumber = "", customerId = "", addressLine1 = "", addressLine2 = "",
//                    addressLine3 = "", city = "", state = "", pincode = "", gender = "", gendercode = "", stateCode = "", mobileNo = "", DOB = "", fatherName = "", EMAILID = "";
//
//            String Query1 = "";
//            String Query2 = "";
//
//            Query1 = "SELECT CUSTOMERID,CUSTOMERFIRSTNAME,CUSTOMERLASTNAME,PANNUMBER,DATEOFBIRTH,GENDER,MOBILENUMBER,FATHERNAME,EMAILID "
//                    + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
//
//            Query2 = "SELECT permaddress1,permaddress2,permaddress3,PermCity,PermState,PermZip "
//                    + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
//
//
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Query1: " + Query1);
//            List< List< String>> Result = ifr.getDataFromDB(Query1);
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->#Result: " + Result.toString());
//            String pattern = ConfProperty.getCommonPropertyValue("NamePattern");
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->#Result pattern: " + pattern);
//            if (Result.size() > 0) {
//                customerId = Result.get(0).get(0);
//                firstName = Result.get(0).get(1).replaceAll("[ .]", "");
//                lastName = Result.get(0).get(2).replaceAll("[ .]", "");
//
//                if (lastName.equalsIgnoreCase("{}")) {
//                    lastName = "";
//                }
//                if (lastName.equalsIgnoreCase("{")) {
//                    lastName = "";
//                }
//                if (lastName.equalsIgnoreCase("}")) {
//                    lastName = "";
//                }
//                if (firstName.length() < 2) {
//                    Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->firstName: " + firstName);
//                    return RLOS_Constants.ERROR + "Name is less than 1 character ";
//                } else if (!Pattern.matches(pattern, firstName)) {
//                    return RLOS_Constants.ERROR + "Name contains invalid special characters";
//                }
//                dateofBirth = Result.get(0).get(4);
//                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->****dateofBirth: " + dateofBirth);
//                gender = Result.get(0).get(5);
//                mobileNo = Result.get(0).get(6);
//                fatherName = Result.get(0).get(7);
//                EMAILID = Result.get(0).get(8);
//            }
//
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Query2: " + Query2);
//            List< List< String>> Result1 = ifr.getDataFromDB(Query2);
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Result1: " + Result1.toString());
//
//            if (Result1.size() > 0) {
//                addressLine1 = Result1.get(0).get(0);
//                addressLine2 = Result1.get(0).get(1);
//                addressLine3 = Result1.get(0).get(2);
//                city = Result1.get(0).get(3);
//                state = Result1.get(0).get(4);
//                pincode = Result1.get(0).get(5);
//            }
//
//            // Process all KYC records
//            panNumber = "";
//            String voterId = "";
//            for (int i = 0; i < Result.size(); i++) {
//                List<String> row = Result.get(i);
//                String kycType = row.get(3);
//                String kycValue = row.get(4);
//                Log.consoleLog(ifr, "Processing row " + i + " - KYC Type: " + kycType + ", Value: " + kycValue);
//
//                if (kycValue != null && !kycValue.isEmpty()) {
//                    if ("TAXID".equalsIgnoreCase(kycType)) {
//                        panNumber = kycValue.trim();
//                        Log.consoleLog(ifr, "Found PAN Number: " + panNumber);
//                    } else if ("VID".equalsIgnoreCase(kycType)) {
//                        voterId = kycValue.trim();
//                        Log.consoleLog(ifr, "Found Voter ID: " + voterId);
//                    } else if ("AA".equalsIgnoreCase(kycType)) {
//                        aadharNo = kycValue.trim();
//                        Log.consoleLog(ifr, "Found aadharNo Ref No: " + aadharNo);
//                        AadharVault objAV = new AadharVault();
//                        aadharNo = objAV.getDataByReferenceKey(ifr, aadharNo);
//                    } else if (panNumber.isEmpty() && kycValue.matches("[A-Za-z]{5}[0-9]{4}[A-Za-z]{1}")) {
//                        panNumber = kycValue.trim();
//                        Log.consoleLog(ifr, "Auto-detected PAN format: " + panNumber);
//                    }
//                } else {
//                    Log.consoleLog(ifr, "Empty KYC value in row " + i);
//                }
//            }
//            if (aadharNo.equalsIgnoreCase("ERROR")) {
//                aadharNo = "";
//            }
//
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->AddressLine1: " + addressLine1);
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->AddressLine2: " + addressLine2);
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->AddressLine3: " + addressLine3);
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->City: " + city);
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->State: " + state);
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Pincode: " + pincode);
//
//            String stateCodeQ = "SELECT STATE_CODE FROM LOS_MST_STATE WHERE "
//                    + "UPPER(TRIM(STATE_NAME))=UPPER(TRIM('" + state + "')) AND ROWNUM=1";
//            Log.consoleLog(ifr, "stateCodeQ==>" + stateCodeQ);
//            List< List< String>> stateCodeR = ifr.getDataFromDB(stateCodeQ);
//            Log.consoleLog(ifr, "#stateCodeR===>" + stateCodeR.toString());
//            if (stateCodeR.size() > 0) {
//                state = stateCodeR.get(0).get(0);
//            }
//
//            if (gender.equalsIgnoreCase("MALE")) {
//                gender = "G01";
//            } else if (gender.equalsIgnoreCase("FEMALE")) {
//                gender = "G02";
//            }
//            String outputDate = "";
//            int ageYears = 0;
//            if (dateofBirth != null && !dateofBirth.trim().isEmpty()) {
//                try {
//
//                    age = calculateAge(dateofBirth, ifr);
//
//                } catch (Exception ex) {
//                    Log.consoleLog(ifr, "Exception in calculate Age: " + ex);// YYYY-MM-DD===>YYYYMMDD*/
//
//                }
//            }
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->outputDate: " + outputDate);// YYYY-MM-DD===>YYYYMMDD
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->ageYears: " + ageYears);// YYYY-MM-DD===>YYYYMMDD
//
//            DOB = dateofBirth;
//
//            Date Date = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//            String formattedDate = dateFormat.format(Date);
//
//            Date PresentDate = new Date();
//            SimpleDateFormat dateFormatt = new SimpleDateFormat("ddMMyyHHmmssSSS");
//
//            String excludedEMIAccnts = "Auto Overdraft,Loan Against Shares / Securities,Gold Loan,Credit Card,Charge Card,Fleet Card,Loan against Card,Loan Against Bank Deposits,OD on Savings Account,Non-Funded Credit Facility,Business Non-Funded Credit Facility General,Business Non-Funded Credit Facility-Priority Sector- Small Business,Business Non-Funded Credit Facility-Priority Sector-Agriculture,Business Non-Funded Credit Facility-Priority Sector-Others,Business Loan Against Bank Deposits,Telco Wireless,Telco Broadband,Telco Landline,Secured Credit Card,Corporate Credit Card,Kisan Credit Card,Loan on Credit Card,Prime Minister Jaan Dhan Yojana - Overdraft,JLG Group,JLG Individual";
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->excludedEMIAccnts: " + excludedEMIAccnts);
//            String excludedOwners = "2,3,5";
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->excludedOwners: " + excludedOwners);
//            String writtenoffSettled = "Restructured Loan,Restructured Loan,Account Sold,Account Purchased,Account Purchased and Restructured";
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->writtenoffSettled: " + writtenoffSettled);
//
//            String mobileStr = String.valueOf(mobileNo);
//            String cleanedMobileNo;
//
//            if (mobileStr.startsWith("91") && mobileStr.length() == 12) {
//                cleanedMobileNo = mobileStr.substring(2);
//            } else {
//                cleanedMobileNo = mobileStr;
//            }
//
//            String InquiryPurpose = "";
//            
//            InquiryPurpose = pcm.getConstantValue(ifr, "HIGHMARK", "Housing Loan");
//            if(InquiryPurpose.isEmpty()){
//                 return RLOS_Constants.ERROR;
//            }
//            String uniqueValue = ProcessInstanceId.replace("HLOS-", "");
//            SimpleDateFormat formatter_unq = new SimpleDateFormat("ddMMyyyyHHmmssSSS"); // SSS for milliseconds
//            String uniqueDate = formatter_unq.format(new Date()); // use current time
//            Log.consoleLog(ifr, "No Voter ID found in KYC records");
//
//            String unique = uniqueDate.replaceAll("[^a-zA-Z0-9 ]", "");
//            uniqueValue = "canaranewHLOS" + uniqueValue + unique.replace(" ", "");
//            if (panNumber == null || panNumber.isEmpty()) {
//                Log.consoleLog(ifr, "No PAN number found in KYC records");
//                panNumber = "";
//            }
//            if (voterId == null || voterId.isEmpty()) {
//                Log.consoleLog(ifr, "No Voter ID found in KYC records");
//                voterId = "";
//            }
//            if (panNumber == null || panNumber.equals("")) {
//                panNumber = "";
//                Log.consoleLog(ifr, "inside if Valid PAN for processing: " + panNumber);
//            } else if (panNumber.length() >= 4 && panNumber.charAt(3) == 'P') {
//
//                panNumber = panNumber;
//                Log.consoleLog(ifr, "Valid PAN for processing: " + panNumber);
//            } else {
//                Log.consoleLog(ifr, "Valid PAN for processing: " + "PAN number Not Listed ");
//                return RLOS_Constants.ERROR;
//            }
//
//            String mbrid = pcm.getConstantValue(ifr, "HIGHMARK", "MBRID"); ///NAB0000095
//            String kendraId = pcm.getConstantValue(ifr, "HIGHMARK", "KENDRA_ID");
//            String credt_rpt_trn_id = pcm.getConstantValue(ifr, "HIGHMARK", "CREDT_RPT_TRN_ID");
//            String credt_rpt_id = pcm.getConstantValue(ifr, "HIGHMARK", "CREDT_RPT_ID");
//            String TBranch = cm.GetHomeBranchCodeSHL(ifr);
//            TBranch = TBranch.replaceFirst("^0+(?!$)", "");
//
//            StringBuilder idsBlock = new StringBuilder();
//
//            if ((voterId != null && !voterId.trim().isEmpty())
//                    || (aadharNo != null && !aadharNo.trim().isEmpty())
//                    || (panNumber != null && !panNumber.trim().isEmpty())) {
//
//                idsBlock.append("                    \"IDS\": {\r\n")
//                        .append("                        \"ID\": [\r\n");
//
//                boolean commaNeeded = false;
//
//                if (voterId != null && !voterId.trim().isEmpty()) {
//                    idsBlock.append("                            {\r\n")
//                            .append("                                \"TYPE\": \"ID02\",\r\n")
//                            .append("                                \"VALUE\": \"").append(voterId).append("\"\r\n")
//                            .append("                            }");
//                    commaNeeded = true;
//                }
//
//                if (aadharNo != null && !aadharNo.trim().isEmpty()) {
//                    if (commaNeeded) {
//                        idsBlock.append(",\r\n");
//                    }
//                    idsBlock.append("                            {\r\n")
//                            .append("                                \"TYPE\": \"ID03\",\r\n")
//                            .append("                                \"VALUE\": \"").append(aadharNo).append("\"\r\n")
//                            .append("                            }");
//                    commaNeeded = true;
//                }
//
//                if (panNumber != null && !panNumber.trim().isEmpty()) {
//                    if (commaNeeded) {
//                        idsBlock.append(",\r\n");
//                    }
//                    idsBlock.append("                            {\r\n")
//                            .append("                                \"TYPE\": \"ID07\",\r\n")
//                            .append("                                \"VALUE\": \"").append(panNumber).append("\"\r\n")
//                            .append("                            }");
//                }
//
//                idsBlock.append("\r\n                        ]\r\n")
//                        .append("                    },\r\n");
//            }
//
//            request
//                    = "{\r\n"
//                    + "    \"REQUEST-REQUEST-FILE\": {\r\n"
//                    + "        \"HEADER-SEGMENT\": {\r\n"
//                    + "            \"AUTH-FLG\": \"Y\",\r\n"
//                    + "            \"AUTH-TITLE\": \"USER\",\r\n"
//                    + "            \"CONSUMER\": {\r\n"
//                    + "                \"INDV\": true,\r\n"
//                    + "                \"SCORE\": true\r\n"
//                    + "            },\r\n"
//                    + "            \"INQ-DT-TM\": \"" + formattedDate + "\",\r\n"
//                    + "            \"IOI\": true,\r\n"
//                    + "            \"LOS-NAME\": \"\",\r\n"
//                    + "            \"LOS-VENDER\": \" \",\r\n"
//                    + "            \"LOS-VERSION\": 1,\r\n"
//                    + "            \"MEMBER-PRE-OVERRIDE\": \"N\",\r\n"
//                    + "            \"REQ-SERVICE-TYPE\": \"\",\r\n"
//                    + "            \"MFI\": {\r\n"
//                    + "                \"GROUP\": true,\r\n"
//                    + "                \"INDV\": true,\r\n"
//                    + "                \"SCORE\": false\r\n"
//                    + "            },\r\n"
//                    + "            \"REQ-ACTN-TYP\": \"SUBMIT\",\r\n"
//                    + "            \"REQ-VOL-TYP\": \"C01\",\r\n"
//                    + "            \"RES-FRMT\": \"JSON/HTML\",\r\n"
//                    + "            \"RES-FRMT-EMBD\": \"Y\",\r\n"
//                    + "            \"SUB-MBR-ID\": \"CANARA BANK\",\r\n"
//                    + "            \"TEST-FLG\": \"N\"\r\n"
//                    + "        },\r\n"
//                    + "        \"INQUIRY\": [\r\n"
//                    + "            {\r\n"
//                    + "                \"ADDRESS-SEGMENT\": {\r\n"
//                    + "                    \"ADDRESS\": [\r\n"
//                    + "                        {\r\n"
//                    + "                            \"ADDRESS-1\": \"" + addressLine1 + "\",\r\n"
//                    + "                            \"CITY\": \"" + city + "\",\r\n"
//                    + "                            \"PIN\": " + pincode + ",\r\n"
//                    + "                            \"STATE\": \"" + state + "\",\r\n"
//                    + "                            \"TYPE\": \"D01\"\r\n"
//                    + "                        },\r\n"
//                    + "                        {\r\n"
//                    + "                            \"ADDRESS-1\": \"" + addressLine1 + "\",\r\n"
//                    + "                            \"CITY\": \"" + city + "\",\r\n"
//                    + "                            \"PIN\": " + pincode + ",\r\n"
//                    + "                            \"STATE\": \"" + state + "\",\r\n"
//                    + "                            \"TYPE\": \"D02\"\r\n"
//                    + "                        }\r\n"
//                    + "                    ]\r\n"
//                    + "                },\r\n"
//                    + "                \"APPLICANT-SEGMENT\": {\r\n"
//                    + "                    \"APPLICANT-NAME\": {\r\n"
//                    + "                        \"NAME1\": \"" + firstName + "\",\r\n"
//                    + "                        \"NAME2\": \"\",\r\n"
//                    + "                        \"NAME3\": \"" + lastName + "\"\r\n"
//                    + "                    },\r\n"
//                    + "                    \"DOB\": {\r\n"
//                    + "                        \"AGE\": \"" + age + "\",\r\n"
//                    + "                        \"AGE-AS-ON\": \"\",\r\n"
//                    + "                        \"DOB-DATE\": \"" + DOB + "\"\r\n"
//                    + "                    },\r\n"
//                    + "                    \"EMAILS\": {\r\n"
//                    + "                        \"EMAIL\": [\r\n"
//                    + "                            \"" + EMAILID + "\",\r\n"
//                    + "                            \"" + EMAILID + "\"\r\n"
//                    + "                        ]\r\n"
//                    + "                    },\r\n"
//                    + "                    \"GENDER\": \"" + gender + "\",\r\n"
//                    + idsBlock.toString() // ✅ Inserted conditional IDS block here
//                    + "                    \"KEY-PERSON\": {\r\n"
//                    + "                        \"NAME\": \"\",\r\n"
//                    + "                        \"TYPE\": \"\"\r\n"
//                    + "                    },\r\n"
//                    + "                    \"NOMINEE\": {\r\n"
//                    + "                        \"NAME\": \"\",\r\n"
//                    + "                        \"TYPE\": \"\"\r\n"
//                    + "                    },\r\n"
//                    + "                    \"PHONES\": {\r\n"
//                    + "                        \"PHONE\": [\r\n"
//                    + "                            {\r\n"
//                    + "                                \"TELE-NO\": " + mobileNo + ",\r\n"
//                    + "                                \"TELE-NO-TYPE\": \"P03\"\r\n"
//                    + "                            },\r\n"
//                    + "                            {\r\n"
//                    + "                                \"TELE-NO\": " + cleanedMobileNo + ",\r\n"
//                    + "                                \"TELE-NO-TYPE\": \"P01\"\r\n"
//                    + "                            }\r\n"
//                    + "                        ]\r\n"
//                    + "                    },\r\n"
//                    + "                    \"RELATIONS\": {\r\n"
//                    + "                        \"RELATION\": [\r\n"
//                    + "                            {\r\n"
//                    + "                                \"NAME\": \"K01\",\r\n"
//                    + "                                \"TYPE\": \"" + fatherName + "\"\r\n"
//                    + "                            },\r\n"
//                    + "                            {\r\n"
//                    + "                                \"NAME\": \"K02\",\r\n"
//                    + "                                \"TYPE\": \"" + fatherName + "\"\r\n"
//                    + "                            }\r\n"
//                    + "                        ]\r\n"
//                    + "                    }\r\n"
//                    + "                },\r\n"
//                    + "                \"APPLICATION-SEGMENT\": {\r\n"
//                    + "                    \"BRANCH-ID\": " + TBranch + ",\r\n"
//                    + "                    \"CREDIT-INQUIRY-STAGE\": \"PRE-DISB\",\r\n"
//                    + "                    \"CREDT-INQ-PURPS-TYP\": \"ACCT-ORIG\",\r\n"
//                    + "                    \"CREDT-INQ-PURPS-TYP-DESC\": \"" + InquiryPurpose + "\",\r\n"
//                    + "                    \"CREDT-REQ-TYP\": \"INDV\",\r\n"
//                    + "                    \"CREDT-RPT-ID\": " + credt_rpt_id + ",\r\n"
//                    + "                    \"CREDT-RPT-TRN-DT-TM\": \"" + formattedDate + "\",\r\n"
//                    + "                    \"CREDT-RPT-TRN-ID\": " + credt_rpt_trn_id + ",\r\n"
//                    + "                    \"INQUIRY-UNIQUE-REF-NO\": \"" + uniqueValue + "\",\r\n"
//                    + "                    \"KENDRA-ID\": " + kendraId + ",\r\n"
//                    + "                    \"LOAN-AMOUNT\": " + loanAmount + ",\r\n"
//                    + "                    \"LOS-APP-ID\": \"" + ProcessInstanceId + "\",\r\n"
//                    + "                    \"MBR-ID\": \"" + mbrid + "\"\r\n"
//                    + "                }\r\n"
//                    + "            }\r\n"
//                    + "        ]\r\n"
//                    + "    }\r\n"
//                    + "}";
//
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->request: " + request);
//
//
//            responseBody = cm.getWebServiceResponse(ifr, apiName, request);
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Response: " + responseBody);
//
//            Log.consoleLog(ifr, "HIGHMARK API RESPONSE==>" + responseBody);
//            String cicReportGenReq = pcm.getConstantValue(ifr, "HIGHMARK", "CICREPORTGENREQ");
//            if (!responseBody.equalsIgnoreCase("{}")) {
//
//                JSONParser parser = new JSONParser();
//                JSONObject OutputJSON = (JSONObject) parser.parse(responseBody);
//
//                JSONObject body = (JSONObject) OutputJSON.get("body");
//                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->body:" + body.toString());
//
//                JSONObject INDV_REPORT_FILE = (JSONObject) body.get("INDV-REPORT-FILE");
//                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->INDV_REPORT_FILE: " + INDV_REPORT_FILE.toString());
//
//                JSONArray INDV_REPORTS = (JSONArray) INDV_REPORT_FILE.get("INDV-REPORTS");
//                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->INDV_REPORTS: " + INDV_REPORTS.toString());
//
//                for (int INDVReportDataLstCount = 0; INDVReportDataLstCount < INDV_REPORTS
//                        .size(); INDVReportDataLstCount++) {
//                    JSONObject INDVReportDataVal = (JSONObject) INDV_REPORTS.get(INDVReportDataLstCount);
//
//                    JSONObject INDV_REPORT = (JSONObject) INDVReportDataVal.get("INDV-REPORT");
//
//                    Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->INDV_REPORT: " + INDV_REPORT.toString());
//                    if (cicReportGenReq.equalsIgnoreCase("Y")) {
//                        JSONObject printableReport = (JSONObject) INDV_REPORT.get("PRINTABLE-REPORT");
//                        //if (!(cf.getJsonValue(bodyObj, "encodedBase64").equalsIgnoreCase(""))) {
//                        //String encodedB64 = cf.getJsonValue(bodyObj, "encodedBase64");
//                        String encodedB64 = printableReport.get("CONTENT").toString();
//                        String generateReportStatus = cm.generateReport(ifr, ProcessInstanceId, "HIGHMARK", encodedB64, "NGREPORTTOOL_HIGHMARK");
//                        Log.consoleLog(ifr, "generateReportStatus==>" + generateReportStatus);
//                        cm.updateCICReportStatus(ifr, "HIGHMARK", generateReportStatus, applicantType);
//                    }
//
//                    if (INDV_REPORT.containsKey("RESPONSES")) {
//                        JSONArray responses = (JSONArray) INDV_REPORT.get("RESPONSES");
//                        NoCIC = false;
//
//                        // Loop through RESPONSES and extract LOAN-DETAILS
//                        for (Object responseObj : responses) {
//                            JSONObject response = (JSONObject) responseObj;
//                            JSONObject loanDetails = (JSONObject) response.get("LOAN-DETAILS");
//                            if (loanDetails != null) {
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->loanDetails: " + loanDetails.toJSONString());
//                                String accountStatus = loanDetails.get("ACCOUNT-STATUS").toString();
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->accountStatus: " + accountStatus);
//                                String accountType = loanDetails.get("ACCT-TYPE").toString();
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->accountType: " + accountType);
//                                String ownershipInd = loanDetails.get("OWNERSHIP-IND").toString();
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->ownershipInd: " + ownershipInd);
//                                String dateReported = loanDetails.get("DATE-REPORTED").toString();
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->dateReported: " + dateReported);
//                                if (loanDetails.containsKey("INSTALLMENT-AMT")
//                                        && loanDetails.get("INSTALLMENT-AMT") != null) {
//                                    installmentAmount = loanDetails.get("INSTALLMENT-AMT").toString();
//                                }
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->installmentAmount: " + installmentAmount);
//                                String paymentHistory = loanDetails.get("COMBINED-PAYMENT-HISTORY").toString();
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->paymentHistory: " + paymentHistory);
//
//                                //payment history (dpd)
//                                if ("Individual".equalsIgnoreCase(ownershipInd) || "Joint".equalsIgnoreCase(ownershipInd)) {
//                                    String[] entries = paymentHistory.split("\\|");
//                                    LocalDate today = LocalDate.now();
//                                    LocalDate twelveMonthsAgo = today.minus(12, ChronoUnit.MONTHS);
//                                    // Month mapping (Move outside loop)
//                                    Map<String, Integer> monthMap = new HashMap<>();
//                                    monthMap.put("Jan", 1);
//                                    monthMap.put("Feb", 2);
//                                    monthMap.put("Mar", 3);
//                                    monthMap.put("Apr", 4);
//                                    monthMap.put("May", 5);
//                                    monthMap.put("Jun", 6);
//                                    monthMap.put("Jul", 7);
//                                    monthMap.put("Aug", 8);
//                                    monthMap.put("Sep", 9);
//                                    monthMap.put("Oct", 10);
//                                    monthMap.put("Nov", 11);
//                                    monthMap.put("Dec", 12);
//
//                                    for (String entry : entries) {
//                                        Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->entry: " + entry);
//                                        String[] monthYearValue = entry.split(":");
//                                        String monthStr = monthYearValue[0];
//                                        // Further split "2022,000/STD" to extract only the year
//                                        String[] yearAndValue = monthYearValue[1].split(",");
//                                        int year = Integer.parseInt(yearAndValue[0]); // Extracting year only
//                                        int month = monthMap.getOrDefault(monthStr, -1);
//                                        LocalDate inputDate1 = LocalDate.of(year, month, 1);
//                                        // Check if input date is within the last 12 months
//                                        boolean isWithinLast12Months = !inputDate1.isBefore(twelveMonthsAgo) && !inputDate1.isAfter(today);
//                                        if (isWithinLast12Months) {
//                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Date is within 12 months::: ");
//                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->yearAndValue[1] " + yearAndValue[1]);
//                                            String[] paymenthistory = yearAndValue[1].split("/");
//                                            combinedPaymentHistory = combinedPaymentHistory + paymenthistory[0];
//                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->paymentHistory " + paymentHistory);
//                                            dpdCount++;
//                                        }
//                                    }
//                                }
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->combinedPaymentHistory: " + combinedPaymentHistory);
//
//                                //emi
//                                boolean emiStatus = checkEMIKnockOffAccFilterStatus(ifr, accountType,
//                                        excludedEMIAccnts, ownershipInd, excludedOwners, accountStatus);
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->emiStatus: " + emiStatus);
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->InstallmentAmount: " + installmentAmount);
//                                if (emiStatus) {
//                                    if (!installmentAmount.isEmpty()) {
//                                        int amount = Integer.parseInt(installmentAmount.replaceAll("[^0-9]", ""));
//                                        if (amount < 0) {
//                                            totalNonEMICount++;
//                                        }
//                                        consolidated_emiAmnt += amount;
//                                    }
//                                }
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->consolidated_emiAmnt: " + consolidated_emiAmnt);
//
//                                //history of setteled and histroy of writeoff
//                                if ("WRITTEN-OFF".equalsIgnoreCase(accountStatus) || "SETTLED".equalsIgnoreCase(accountStatus)) {
//                                    Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->inisde accountstatus: " + accountStatus);
//                                    if (loanDetails.containsKey("WRITTEN-OFF_SETTLED-STATUS")) {
//                                        writtenOffSettledStatus = loanDetails.get("WRITTEN-OFF_SETTLED-STATUS").toString();
//                                    }
//                                    boolean writtenOffStatus = checkWrittenoffSettledStatus(ifr, writtenOffSettledStatus,
//                                            writtenoffSettled);
//                                    Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->writtenOffStatus: " + writtenOffStatus);
//                                    if (writtenOffStatus) {
//                                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//                                        LocalDate givenDate = LocalDate.parse(dateReported, formatter);
//                                        LocalDate today = LocalDate.now();
//                                        long monthsBetween = ChronoUnit.MONTHS.between(givenDate, today);
//                                        if (Math.abs(monthsBetween) <= 60) {
//                                            Log.consoleLog(ifr, "The date is within 60 months from today.");
//                                            if ("Individual".equalsIgnoreCase(ownershipInd) || "Joint".equalsIgnoreCase(ownershipInd)) {
//                                                writeOff = "Yes";
//                                                settled = "Yes";
//                                            }
//                                        }
//
//                                    }
//                                }
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->writeOff: " + writeOff);
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->settled: " + settled);
//
//                                //histroy of NPA
//                                if ("Individual".equalsIgnoreCase(ownershipInd) || "Guarantor".equalsIgnoreCase(ownershipInd)) {
//                                    String[] entries = paymentHistory.split("\\|");
//                                    LocalDate today = LocalDate.now();
//                                    LocalDate sixtyMonthsAgo = today.minus(60, ChronoUnit.MONTHS);
//                                    // Month mapping (Move outside loop)
//                                    Map<String, Integer> monthMap = new HashMap<>();
//                                    monthMap.put("Jan", 1);
//                                    monthMap.put("Feb", 2);
//                                    monthMap.put("Mar", 3);
//                                    monthMap.put("Apr", 4);
//                                    monthMap.put("May", 5);
//                                    monthMap.put("Jun", 6);
//                                    monthMap.put("Jul", 7);
//                                    monthMap.put("Aug", 8);
//                                    monthMap.put("Sep", 9);
//                                    monthMap.put("Oct", 10);
//                                    monthMap.put("Nov", 11);
//                                    monthMap.put("Dec", 12);
//
//                                    for (String entry : entries) {
//                                        Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->entry: " + entry);
//                                        String[] monthYearValue = entry.split(":");
//                                        if (monthYearValue.length < 2) {
//                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Invalid entry format: " + entry);
//                                            continue;
//                                        }
//                                        String monthStr = monthYearValue[0];
//                                        // Further split "2022,000/STD" to extract only the year
//                                        String[] yearAndValue = monthYearValue[1].split(",");
//                                        if (yearAndValue.length < 1) {
//                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Invalid format for year: " + monthYearValue[1]);
//                                            continue;
//                                        }
//
//                                        int year;
//                                        try {
//                                            year = Integer.parseInt(yearAndValue[0]); // Extracting year only
//                                        } catch (NumberFormatException e) {
//                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Error parsing year from: " + yearAndValue[0]);
//                                            continue;
//                                        }
//                                        int month = monthMap.getOrDefault(monthStr, -1);
//                                        if (month == -1) {
//                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Invalid month name: " + monthStr);
//                                            continue;
//                                        }
//                                        LocalDate inputDate1 = LocalDate.of(year, month, 1);
//                                        // Check if input date is within the last 12 months
//                                        boolean isWithinLast60Months = !inputDate1.isBefore(sixtyMonthsAgo) && !inputDate1.isAfter(today);
//                                        if (isWithinLast60Months) {
//                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Date is within 60 months::: ");
//                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->yearAndValue[0] " + yearAndValue[1]);
//                                            String[] paymenthistory = yearAndValue[1].split("/");
//                                            String NPAcheck = paymenthistory[1];
//                                            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->NPAcheck:: " + NPAcheck);
//                                            if ("SUB".endsWith(NPAcheck) || "DBT".equalsIgnoreCase(NPAcheck) || "LOS".equalsIgnoreCase(NPAcheck)) {
//                                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->NPA is found: ");
//                                                NPA = "Yes";
//                                            }
//                                        }
//                                    }
//                                }
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->NPA: " + NPA);
//                            }
//                        }
//
//                        JSONArray ScoreDetails = (JSONArray) INDV_REPORT.get("SCORES");
//                        Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->ScoreDetails: " + ScoreDetails);
//
//                        for (int ScoreDetailsCount = 0; ScoreDetailsCount < ScoreDetails.size(); ScoreDetailsCount++) {
//                            JSONObject ScoreDetailsObj = (JSONObject) ScoreDetails.get(ScoreDetailsCount);
//                            if (ScoreDetailsObj.containsKey("SCORE-VALUE") && ScoreDetailsObj.get("SCORE-VALUE") != null) {
//                                Value = ScoreDetailsObj.get("SCORE-VALUE").toString();
//                                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Value: " + Value);
//                            }
//
//                        }
//                    } else {
//                        NoCIC = true;
//                        Value = "0";
//                    }
//                }
//
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//                LocalDateTime dateTime = LocalDateTime.now();
//                String enqDate = dateTime.format(formatter);
//                String newToCredit = "No";
//                if (dpdCount > 0) {
//                    DPD = String.valueOf(dpdCount);
//                }
//
//                String qryCICDataUpdate1 = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,"
//                        + "CICNPACHECK,CICOVERDUE,WRITEOFF,"
//                        + "BUREAUTYPE,TOTEMIAMOUNT,PAYHISTORYCOMBINED,"
//                        + "APPLICANT_TYPE,TOTNONEMICOUNT,SETTLEDHISTORY,SRNPAINP,"
//                        + "GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST,DTINSERTED,APPLICANT_UID) "
//                        + "values('" + ProcessInstanceId + "','" + Value + "',"
//                        + "'" + NPA + "','" + DPD + "','" + writeOff + "',"
//                        + "'HM',"
//                        + "'" + consolidated_emiAmnt + "',"
//                        + "'" + combinedPaymentHistory + "',"
//                        + "'" + applicantType + "',"
//                        + "'" + totalNonEMICount + "','" + settled + "','"
//                        + NPA + "','" + NPA + "','" + settled + "',"
//                        + "SYSDATE,'" + insertionOrderid + "')";
//                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Insert query: " + qryCICDataUpdate1);
//                ifr.saveDataInDB(qryCICDataUpdate1);
//
////                String query = "INSERT INTO LOS_TRN_CREDITHISTORY (PID,CUSTOMERID,MOBILENO,PRODUCTCODE,"
////                        + "APPREFNO,APPLICANTTYPE,APPLICANTID,\n"
////                        + "BUREAUTYPE,BUREAUCODE,SERVICECODE,LAP_EXIST,\n"
////                        + "CIC_SCORE,TOTAL_EMIAMOUNT,NEWTOCREDITYN,DTINSERTED,DTUPDATED)\n"
////                        + "VALUES('" + ProcessInstanceId + "',"
////                        + "'','" + mobileNo + "','"+productCode+"','" + pcm.getApplicationRefNumber(ifr) + "','" + applicantType + "','" + insertionOrderid + "',\n"
////                        + "'EXT','EF','CNS','" + "" + "','" + Value + "',"
////                        //+ "'" + NPA + "','" + dpd + "','" + writeOFF + "','"
////                        + "'" + "" + "','" + newToCredit + "',SYSDATE,SYSDATE)";
////                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Insert query for LOS_TRN_CREDITHISTORY: " + query);
////                int queryResult = ifr.saveDataInDB(query);
////                Log.consoleLog(ifr, "HighmarkAPI:getHighMark****->queryResult: " + queryResult);
//                Log.consoleLog(ifr, "applicantType()():applicantType****->applicantType: " + applicantType);
//                Log.consoleLog(ifr, "HighmarkAPI:getHighMark****->insertionOrderid: " + insertionOrderid);
//                crg.crgGenHighMark(ifr, responseBody, ProcessInstanceId, apiName, applicantType, insertionOrderid, NoCIC);
//                return Value;
//
//            } else {
//                Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->No response from Server..");
//            }
//
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Exception/HighMark===>" + e);
//            Log.errorLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Exception/HighMark===>" + e);
//            StringWriter errors = new StringWriter();
//            e.printStackTrace(new PrintWriter(errors));
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Exception StackTrace:::" + errors);
//
//        } finally {
//            Log.consoleLog(ifr, "HighmarkAPI:getHighMarkCIBILScore->Capture Equifax REquest and Response Calling!!!!!");
//            cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Highmark_API", request, responseBody, "", "", "",
//                    applicantType);
//        }
//        return RLOS_Constants.ERROR;
//    }
}
