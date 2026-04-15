/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.cbs;

import com.newgen.dlp.commonobjects.ccm.CCMCommonMethods;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.hrms.LoanAmtInWords;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * package
 *
 * @author ahmed.zindha
 */
public class Ammortization {

    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();
    CCMCommonMethods apic = new CCMCommonMethods();

    public String ExecuteCBS_Ammortization(IFormReference ifr, String ProcessInstanceId,
            String loanAmount, String loanterm, String productType) {

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiName = "Ammortization";
        String serviceName = "CBS_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";
        try {

            Log.consoleLog(ifr, "Entered into ExecuteCBS_Ammortization...");

            String BankCode = pcm.getConstantValue(ifr, "CBSAMMOR", "BANKCODE");
            String Channel = pcm.getConstantValue(ifr, "CBSAMMOR", "CHANNEL");
            String UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
            String TBranch = pcm.getConstantValue(ifr, "CBSAMMOR", "TBRANCH");

            String productCode = "";
            String subProductCode = "";

            String strProductCode = "";
            String strScheduleCode = "2001";
            String accountVarianceRate="0";
            
            //  String straccountVarianceRate = "";//Newly added for Ammortization

            if (productType.equalsIgnoreCase("PAPL")) {
                productCode = "PL";
                subProductCode = "STP-PAPL";

                strProductCode = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "PRODUCTCODE");
                strScheduleCode = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "SCODE");

            } else if (productType.equalsIgnoreCase("BUDGET")) {
                productCode = "PL";
                subProductCode = "STP-CB";
                strScheduleCode = pcm.getScheduleCode(ifr);
                strProductCode = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "PRODUCTCODE");
                // strScheduleCode = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "SCODE");

            } else if (productType.equalsIgnoreCase("LAD")) {
                //productCode = "PL";
                //subProductCode = "STP-CB";

                String laonDetails = ConfProperty.getQueryScript("LADOPENLOANACCDETAILS").replaceAll("#winame#", ProcessInstanceId);
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
                    strScheduleCode = pcm.getConstantValue(ifr, "CBSLADODP", "SCODE");
                } else if (loanselected.equalsIgnoreCase("OD") && (loan_purpose.equalsIgnoreCase("AGRI"))) {
                    strProductCode = pcm.getConstantValue(ifr, "CBSLADODA", "PRODUCTCODE");
                    strScheduleCode = pcm.getConstantValue(ifr, "CBSLADODP", "SCODE");
                } else if (loanselected.equalsIgnoreCase("OD") && (loan_purpose.equalsIgnoreCase("BUS"))) {
                    strProductCode = pcm.getConstantValue(ifr, "CBSLADODB", "PRODUCTCODE");
                    strScheduleCode = pcm.getConstantValue(ifr, "CBSLADODP", "SCODE");
                } else if (loanselected.equalsIgnoreCase("TL") && (loan_purpose.equalsIgnoreCase("RET"))) {
                    strProductCode = pcm.getConstantValue(ifr, "CBSLADVSLP", "PRODUCTCODE");
                    strScheduleCode = pcm.getConstantValue(ifr, "CBSLADVSLP", "SCODE");
                } else if (loanselected.equalsIgnoreCase("TL") && (loan_purpose.equalsIgnoreCase("AGRI"))) {
                    strProductCode = pcm.getConstantValue(ifr, "CBSLADVSLA", "PRODUCTCODE");
                    strScheduleCode = pcm.getConstantValue(ifr, "CBSLADVSLA", "SCODE");
                } else if (loanselected.equalsIgnoreCase("TL") && (loan_purpose.equalsIgnoreCase("BUS"))) {
                    strProductCode = pcm.getConstantValue(ifr, "CBSLADVSLB", "PRODUCTCODE");
                    strScheduleCode = pcm.getConstantValue(ifr, "CBSLADVSLB", "SCODE");
                }

            } else {//For VL & Pension--Temporray need to verify the same
                Log.consoleLog(ifr, "#Else Condition");
                productCode = pcm.getProductCode(ifr);
                subProductCode = pcm.getSubProductCode(ifr);
                Log.consoleLog(ifr, "#productCode=======>" + productCode);
                Log.consoleLog(ifr, "#subProductCode====>" + subProductCode);
                strScheduleCode = pcm.getScheduleCode(ifr);

                strProductCode = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "PRODUCTCODE");
                // strScheduleCode = pcm.getParamConfig(ifr, productCode, subProductCode, "LOANACCCREATE", "SCODE");

            }
            Log.consoleLog(ifr, "productCode=======>" + strProductCode);
            Log.consoleLog(ifr, "subProductCode====>" + strScheduleCode);

            String Request = "{\n"
                    + "  \"input\": {\n"
                    + "    \"SessionContext\": {\n"
                    + "      \"SupervisorContext\": {\n"
                    + "        \"PrimaryPassword\": \"\",\n"
                    + "        \"UserId\": \"\"\n"
                    + "      },\n"
                    + "      \"BankCode\": " + BankCode + ",\n"
                    + "      \"Channel\": \"" + Channel + "\",\n"
                    + "      \"ExternalBatchNumber\": \"\",\n"
                    + "      \"ExternalReferenceNo\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
                    + "      \"ExternalSystemAuditTrailNumber\": \"\",\n"
                    + "      \"LocalDateTimeText\": \"\",\n"
                    + "      \"OriginalReferenceNo\": \"\",\n"
                    + "      \"OverridenWarnings\": \"\",\n"
                    + "      \"PostingDateText\": \"\",\n"
                    + "      \"ServiceCode\": \"\",\n"
                    + "      \"SessionTicket\": \"\",\n"
                    + "      \"TransactionBranch\": \"" + TBranch + "\",\n"
                    + "      \"UserId\": \"" + UserId + "\",\n"
                    + "      \"UserReferenceNumber\": \"" + cm.getCBSExternalReferenceNo() + "\",\n"
                    + "      \"ValueDateText\": \"\"\n"
                    + "    },\n"
                    + "    \"accountVarianceRate\": " + accountVarianceRate + ",\n"
                    + "    \"productCode\": " + strProductCode + ",\n"
                    + "    \"scheduleType\": " + strScheduleCode + ",\n"
                    + "    \"rateChartCode\": 0,\n"
                    + "    \"loanAmount\": " + loanAmount + ",\n"
                    + "    \"loanTerm\": " + loanterm + ",\n"
                    + "    \"typeOfInterest\": 1,\n"
                    + "    \"disbursementDate\": \"\"\n"
                    + "  }\n"
                    + "}";
            // String query = "delete from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='" + ProcessInstanceId + "'";
            //Log.consoleLog(ifr, "query==>" + query);
            //ifr.saveDataInDB(query);
            //HashMap<String, String> requestHeader = new HashMap<>();
            String Response = cm.getWebServiceResponse(ifr, apiName, Request);
            Log.consoleLog(ifr, "Response===>" + Response);

            if (!Response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject resultObj = (JSONObject) parser.parse(Response);
                // JSONObject resultObj = new JSONObject(OutputJSON);

                String body = resultObj.get("body").toString();
                JSONObject bodyObj = (JSONObject) parser.parse(body);
                //     JSONObject bodyObj = new JSONObject(bodyJSON);

                String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
                Log.consoleLog(ifr, "CheckError===>" + CheckError);
                //CheckError = "true";
                PortalCommonMethods pcm = new PortalCommonMethods();
                if (CheckError.equalsIgnoreCase("true")) {

                    //Deleting the existing details available in the system.
                    String Query1 = "DELETE FROM LOS_STG_CBS_AMM_DEFN_DETAILS "
                            + "WHERE ProcessInstanceId='" + ProcessInstanceId + "'";
                    Log.consoleLog(ifr, "Query1===>" + Query1);
                    ifr.saveDataInDB(Query1);

                    String Query2 = "DELETE FROM LOS_STG_CBS_AMM_SCH_DETAILS "
                            + "WHERE ProcessInstanceId='" + ProcessInstanceId + "'";
                    Log.consoleLog(ifr, "Query2===>" + Query2);
                    ifr.saveDataInDB(Query2);
                    //Deleting the existing details available in the system.

                    String XfaceExtScheduleDefnDTO = bodyObj.get("XfaceExtScheduleDefnDTO").toString();
                    Log.consoleLog(ifr, "XfaceExtScheduleDefnDTO==>" + XfaceExtScheduleDefnDTO);

                    JSONArray XfaceExtScheduleDefnDTOJSON = (JSONArray) parser.parse(XfaceExtScheduleDefnDTO);
                    Log.consoleLog(ifr, "XfaceExtScheduleDefnDTOJSON.size()==>" + XfaceExtScheduleDefnDTOJSON.size());
                    for (int i = 0; i < XfaceExtScheduleDefnDTOJSON.size(); i++) {
                        //  System.out.println("XfaceExtScheduleDefnDTOJSON==>" + XfaceExtScheduleDefnDTOJSON.get(i));
                        String InputString = XfaceExtScheduleDefnDTOJSON.get(i).toString();
                        Log.consoleLog(ifr, "InputString==>" + InputString);
                        JSONObject InputStringResponseJSON = (JSONObject) parser.parse(InputString);

                        String StageNumber = InputStringResponseJSON.get("StageNumber").toString();
                        String StageName = InputStringResponseJSON.get("StageName").toString();
                        String StageStartDate = InputStringResponseJSON.get("StageStartDate").toString();
                        String PrincipalAmount = InputStringResponseJSON.get("PrincipalAmount").toString();
                        String PrincipalPayments = InputStringResponseJSON.get("PrincipalPayments").toString();
                        String InterestPayments = InputStringResponseJSON.get("InterestPayments").toString();
                        String FirstPrincipalDueDate = InputStringResponseJSON.get("FirstPrincipalDueDate").toString();
                        String FirstInterestDueDate = InputStringResponseJSON.get("FirstInterestDueDate").toString();
                        String InstallmentAmount = InputStringResponseJSON.get("InstallmentAmount").toString();
                        String StageEndDate = InputStringResponseJSON.get("StageEndDate").toString();

                        String InQuery1 = "INSERT INTO LOS_STG_CBS_AMM_DEFN_DETAILS(ProcessInstanceId,StageNumber, "
                                + "StageName, StageStartDate, PrincipalAmount, PrincipalPayments, InterestPayments,"
                                + " FirstPrincipalDueDate, FirstInterestDueDate, InstallmentAmount, StageEndDate)"
                                + " VALUES("
                                + "'" + ProcessInstanceId + "',"
                                + "'" + StageNumber + "',"
                                + "'" + StageName + "',"
                                + "'" + StageStartDate + "',"
                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, PrincipalAmount) + "',"
                                + "'" + PrincipalPayments + "',"
                                + "'" + InterestPayments + "',"
                                + "'" + FirstPrincipalDueDate + "',"
                                + "'" + FirstInterestDueDate + "',"
                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, InstallmentAmount) + "',"
                                + "'" + StageEndDate + "')";
                        Log.consoleLog(ifr, "InQuery1==>" + InQuery1);
                        ifr.saveDataInDB(InQuery1);
                        LoanAmtInWords inWords=new LoanAmtInWords();
                        String valueInWords="";
                        try{
                        valueInWords= inWords.amtInWords(Double.parseDouble(InstallmentAmount));
                        }catch(Exception e){
                            
                        }
                       String updateQuery=" update  SLOS_STAFF_TRN set INSTALLMENT_AMOUNT_IN_WORDS='"+valueInWords+"' where  winame='"+processInstanceId+"'";
                       ifr.saveDataInDB(updateQuery);
                       Log.consoleLog(ifr, "valueInWords==>"+valueInWords);
                    }

                    String XfaceExtScheduleDtlsDTO = bodyObj.get("XfaceExtScheduleDtlsDTO").toString();

                    JSONArray XfaceExtScheduleDtlsDTOJSON = (JSONArray) parser.parse(XfaceExtScheduleDtlsDTO);
                    Log.consoleLog(ifr, "XfaceExtScheduleDtlsDTOJSON.size()==>" + XfaceExtScheduleDtlsDTOJSON.size());
                    //   query = "delete from LOS_STG_CBS_AMM_SCH_DETAILS where ProcessInstanceId='" + ProcessInstanceId + "'";
                    // Log.consoleLog(ifr, "query==>" + query);
                    //ifr.saveDataInDB(query);
                    for (int i = 0; i < XfaceExtScheduleDtlsDTOJSON.size(); i++) {
                        String InputString = XfaceExtScheduleDtlsDTOJSON.get(i).toString();
                        // System.out.println("InputString==>" + InputString);
                        JSONObject InputStringResponseJSON = (JSONObject) parser.parse(InputString);

                        String StageNumber = InputStringResponseJSON.get("StageNumber").toString();
                        String InstallmentNo = InputStringResponseJSON.get("InstallmentNo").toString();
                        String StartDate = InputStringResponseJSON.get("StartDate").toString();
                        String RepaymentDate = InputStringResponseJSON.get("RepaymentDate").toString();
                        String InterestRate = InputStringResponseJSON.get("InterestRate").toString();
                        String Principal = InputStringResponseJSON.get("Principal").toString();
                        String Interest = InputStringResponseJSON.get("Interest").toString();
                        String Charge = InputStringResponseJSON.get("Charge").toString();
                        String CapitalizedAmount = InputStringResponseJSON.get("CapitalizedAmount").toString();
                        String Installment = InputStringResponseJSON.get("Installment").toString();
                        String OutstandingBalance = InputStringResponseJSON.get("OutstandingBalance").toString();
                        String TotalInstallmentAmount = InputStringResponseJSON.get("TotalInstallmentAmount").toString();
                        String Days = InputStringResponseJSON.get("Days").toString();

                        String InQuery2 = "INSERT INTO LOS_STG_CBS_AMM_SCH_DETAILS(ProcessInstanceId,StageNumber, InstallmentNo, StartDate, RepaymentDate, InterestRate, Principal, Interest, Charge, CapitalizedAmount, Installment, OutstandingBalance, TotalInstallmentAmount, Days) VALUES("
                                + "'" + ProcessInstanceId + "',"
                                + "'" + StageNumber + "',"
                                + "'" + InstallmentNo + "',"
                                + "'" + StartDate + "',"
                                + "'" + RepaymentDate + "',"
                                + "'" + InterestRate + "',"
                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, Principal) + "',"
                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, Interest) + "',"
                                + "'" + Charge + "',"
                                + "'" + CapitalizedAmount + "',"
                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, Installment) + "',"
                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, OutstandingBalance) + "',"
                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, TotalInstallmentAmount) + "',"
                                + "'" + Days + "')";
                        Log.consoleLog(ifr, "InQuery2==>" + InQuery2);
                        ifr.saveDataInDB(InQuery2);
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

            if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
            } else {
                return apiStatus;
            }

//            String APIStatus = "";
//            String APIStatusSend = "";
//            if (ErrorMessage.equalsIgnoreCase("")) {
//                APIStatus = "SUCCESS";
//                APIStatusSend = RLOS_Constants.SUCCESS;
//            } else {
//                APIStatus = "FAIL";
//                APIStatusSend = RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, ErrorCode);
//            }
//
//            cm.CaptureRequestResponse(ifr, ProcessInstanceId, serviceName, Request, Response, ErrorCode, ErrorMessage, APIStatus);
//            
//            
            //return APIStatusSend;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/ExecuteCBS_Ammortization===>" + e);
            Log.errorLog(ifr, "Exception/ExecuteCBS_Ammortization===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
    }

	public String ExecuteCBS_AmmortizationHRMS(IFormReference ifr, String ProcessInstanceId, String loanAmount,
			String loanterm, String productCode, String loanType) {
		 String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
	        String apiName = "Ammortization";
	        String serviceName = "CBS_" + apiName;
	        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
	        String apiStatus = "";
	        String apiErrorCode = "";
	        String apiErrorMessage = "";
	        String request = "";
	        String response = "";
	        try {

	            Log.consoleLog(ifr, "Entered into ExecuteCBS_Ammortization...");


	            String BankCode = pcm.getConstantValue(ifr, "CBSAMMOR", "BANKCODE");
	            String Channel = pcm.getConstantValue(ifr, "CBSAMMOR", "CHANNEL");
	            String UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
	          
	            String TBranch = pcm.getConstantValue(ifr, "CBSAMMOR", "TBRANCH");

	          
	            String strScheduleCode = "2001";
	            String accountVarianceRate="0";
	            String rateChartCode="0";
	            String typeOfInterest="1";
	            String ioiPeriod= "";
	            
	           
				if (loanType.trim().toLowerCase().contains("dpn")) {
					ioiPeriod = pcm.getConstantValue(ifr, "STAFFDPN", "IOIPERIOD");
				} else if (loanType.trim().toLowerCase().contains("kavach")) {
					ioiPeriod = pcm.getConstantValue(ifr, "STAFFKAVACH", "IOIPERIOD");
				}
	            
	            String extUniqueRefId = generateUniqueID(17);
	            
	            String loanAmtRounded = pcm.mAccRoundOffvalue(ifr, loanAmount);

	                  request = "{\n"
	                    + "  \"input\": {\n"
	                    + "    \"SessionContext\": {\n"
	                    + "      \"SupervisorContext\": {\n"
	                    + "        \"PrimaryPassword\": \"\",\n"
	                    + "        \"UserId\": \"\"\n"
	                    + "      },\n"
	                    + "      \"BankCode\": " + BankCode + ",\n"
	                    + "      \"Channel\": \"" + Channel + "\",\n"
	                    + "      \"ExternalBatchNumber\": \"\",\n"
	                    + "      \"ExternalReferenceNo\": \"" + extUniqueRefId + "\",\n"
	                    + "      \"ExternalSystemAuditTrailNumber\": \"\",\n"
	                    + "      \"LocalDateTimeText\": \"\",\n"
	                    + "      \"OriginalReferenceNo\": \"\",\n"
	                    + "      \"OverridenWarnings\": \"\",\n"
	                    + "      \"PostingDateText\": \"\",\n"
	                    + "      \"ServiceCode\": \"\",\n"
	                    + "      \"SessionTicket\": \"\",\n"
	                    + "      \"TransactionBranch\": \"" + TBranch + "\",\n"
	                    + "      \"UserId\": \"" + UserId + "\",\n"
	                    + "      \"UserReferenceNumber\": \"" + extUniqueRefId + "\",\n"
	                    + "      \"ValueDateText\": \"\"\n"
	                    + "    },\n"
	                    + "    \"accountVarianceRate\": \"" + accountVarianceRate + "\",\n"
	                    + "    \"productCode\": \"" + productCode +  "\",\n"
	                    + "    \"scheduleType\": \"" + strScheduleCode + "\",\n"
	                    + "    \"rateChartCode\":\"" + rateChartCode + "\",\n"
	                    + "    \"loanAmount\": \"" + loanAmtRounded + "\",\n"
	                    + "    \"loanTerm\": \"" + loanterm + "\",\n"
	                    + "    \"typeOfInterest\": \"" +typeOfInterest+ "\",\n"
	                    + "    \"disbursementDate\": \"\",\n"
	                    + "    \"ioiPeriod\": \""+ioiPeriod+"\",\n"
	                    + "    \"salaryGenDateFlag\": \"\",\n"
	                    + "    \"salaryDayDatePart\": \"\",\n"
	                    + "    \"preferredRepayDatePart\": \"\"\n"
	                    + "  }\n"
	                    + "}";
	            // String query = "delete from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='" + ProcessInstanceId + "'";
	            //Log.consoleLog(ifr, "query==>" + query);
	            //ifr.saveDataInDB(query);
	            //HashMap<String, String> requestHeader = new HashMap<>();
	            response = cm.getWebServiceResponse(ifr, apiName, request);
	            Log.consoleLog(ifr, "Response===>" + response);

	            if (!response.equalsIgnoreCase("{}")) {
	                JSONParser parser = new JSONParser();
	                JSONObject resultObj = (JSONObject) parser.parse(response);
	                // JSONObject resultObj = new JSONObject(OutputJSON);

	                String body = resultObj.get("body").toString();
	                JSONObject bodyObj = (JSONObject) parser.parse(body);
	                //     JSONObject bodyObj = new JSONObject(bodyJSON);

	                String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
	                Log.consoleLog(ifr, "CheckError===>" + CheckError);
	                //CheckError = "true";
	                PortalCommonMethods pcm = new PortalCommonMethods();
	                if (CheckError.equalsIgnoreCase("true")) {

	                    //Deleting the existing details available in the system.
	                    String Query1 = "DELETE FROM LOS_STG_CBS_AMM_DEFN_DETAILS "
	                            + "WHERE ProcessInstanceId='" + ProcessInstanceId + "'";
	                    Log.consoleLog(ifr, "Query1===>" + Query1);
	                    ifr.saveDataInDB(Query1);

	                    String Query2 = "DELETE FROM LOS_STG_CBS_AMM_SCH_DETAILS "
	                            + "WHERE ProcessInstanceId='" + ProcessInstanceId + "'";
	                    Log.consoleLog(ifr, "Query2===>" + Query2);
	                    ifr.saveDataInDB(Query2);
	                    //Deleting the existing details available in the system.

	                    String XfaceExtScheduleDefnDTO = bodyObj.get("XfaceExtScheduleDefnDTO").toString();
	                    Log.consoleLog(ifr, "XfaceExtScheduleDefnDTO==>" + XfaceExtScheduleDefnDTO);

	                    JSONArray XfaceExtScheduleDefnDTOJSON = (JSONArray) parser.parse(XfaceExtScheduleDefnDTO);
	                    Log.consoleLog(ifr, "XfaceExtScheduleDefnDTOJSON.size()==>" + XfaceExtScheduleDefnDTOJSON.size());
	                    for (int i = 0; i < XfaceExtScheduleDefnDTOJSON.size(); i++) {
	                        //  System.out.println("XfaceExtScheduleDefnDTOJSON==>" + XfaceExtScheduleDefnDTOJSON.get(i));
	                        String InputString = XfaceExtScheduleDefnDTOJSON.get(i).toString();
	                        Log.consoleLog(ifr, "InputString==>" + InputString);
	                        JSONObject InputStringResponseJSON = (JSONObject) parser.parse(InputString);

	                        String StageNumber = InputStringResponseJSON.get("StageNumber").toString();
	                        String StageName = InputStringResponseJSON.get("StageName").toString();
	                        String StageStartDate = InputStringResponseJSON.get("StageStartDate").toString();
	                        String PrincipalAmount = InputStringResponseJSON.get("PrincipalAmount").toString();
	                        String PrincipalPayments = InputStringResponseJSON.get("PrincipalPayments").toString();
	                        String InterestPayments = InputStringResponseJSON.get("InterestPayments").toString();
	                        String FirstPrincipalDueDate = InputStringResponseJSON.get("FirstPrincipalDueDate").toString();
	                        String FirstInterestDueDate = InputStringResponseJSON.get("FirstInterestDueDate").toString();
	                        String InstallmentAmount = InputStringResponseJSON.get("InstallmentAmount").toString();
	                        String StageEndDate = InputStringResponseJSON.get("StageEndDate").toString();

	                        String InQuery1 = "INSERT INTO LOS_STG_CBS_AMM_DEFN_DETAILS(ProcessInstanceId,StageNumber, "
	                                + "StageName, StageStartDate, PrincipalAmount, PrincipalPayments, InterestPayments,"
	                                + " FirstPrincipalDueDate, FirstInterestDueDate, InstallmentAmount, StageEndDate)"
	                                + " VALUES("
	                                + "'" + ProcessInstanceId + "',"
	                                + "'" + StageNumber + "',"
	                                + "'" + StageName + "',"
	                                + "'" + StageStartDate + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, PrincipalAmount) + "',"
	                                + "'" + PrincipalPayments + "',"
	                                + "'" + InterestPayments + "',"
	                                + "'" + FirstPrincipalDueDate + "',"
	                                + "'" + FirstInterestDueDate + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, InstallmentAmount) + "',"
	                                + "'" + StageEndDate + "')";
	                        Log.consoleLog(ifr, "InQuery1==>" + InQuery1);
                                 LoanAmtInWords inWords=new LoanAmtInWords();
                                 String valueInWords="";
                        try{
                        valueInWords= inWords.amtInWords(Double.parseDouble(InstallmentAmount));
                        }catch(Exception e){
                            
                        }
                        
                       String updateQuery=" update  SLOS_STAFF_TRN set INSTALLMENT_AMOUNT_IN_WORDS='"+valueInWords+"' where  winame='"+processInstanceId+"'";
                       if(i==0)
                       ifr.saveDataInDB(updateQuery);
                       Log.consoleLog(ifr, "valueInWords==>"+valueInWords);
	                        ifr.saveDataInDB(InQuery1);
	                    }

	                    String XfaceExtScheduleDtlsDTO = bodyObj.get("XfaceExtScheduleDtlsDTO").toString();

	                    JSONArray XfaceExtScheduleDtlsDTOJSON = (JSONArray) parser.parse(XfaceExtScheduleDtlsDTO);
	                    Log.consoleLog(ifr, "XfaceExtScheduleDtlsDTOJSON.size()==>" + XfaceExtScheduleDtlsDTOJSON.size());
	                    //   query = "delete from LOS_STG_CBS_AMM_SCH_DETAILS where ProcessInstanceId='" + ProcessInstanceId + "'";
	                    // Log.consoleLog(ifr, "query==>" + query);
	                    //ifr.saveDataInDB(query);
	                    for (int i = 0; i < XfaceExtScheduleDtlsDTOJSON.size(); i++) {
	                        String InputString = XfaceExtScheduleDtlsDTOJSON.get(i).toString();
	                        // System.out.println("InputString==>" + InputString);
	                        JSONObject InputStringResponseJSON = (JSONObject) parser.parse(InputString);

	                        String StageNumber = InputStringResponseJSON.get("StageNumber").toString();
	                        String InstallmentNo = InputStringResponseJSON.get("InstallmentNo").toString();
	                        String StartDate = InputStringResponseJSON.get("StartDate").toString();
	                        String RepaymentDate = InputStringResponseJSON.get("RepaymentDate").toString();
	                        String InterestRate = InputStringResponseJSON.get("InterestRate").toString();
	                        String Principal = InputStringResponseJSON.get("Principal").toString();
	                        String Interest = InputStringResponseJSON.get("Interest").toString();
	                        String Charge = InputStringResponseJSON.get("Charge").toString();
	                        String CapitalizedAmount = InputStringResponseJSON.get("CapitalizedAmount").toString();
	                        String Installment = InputStringResponseJSON.get("Installment").toString();
	                        String OutstandingBalance = InputStringResponseJSON.get("OutstandingBalance").toString();
	                        String TotalInstallmentAmount = InputStringResponseJSON.get("TotalInstallmentAmount").toString();
	                        String Days = InputStringResponseJSON.get("Days").toString();

	                        String InQuery2 = "INSERT INTO LOS_STG_CBS_AMM_SCH_DETAILS(ProcessInstanceId,StageNumber, InstallmentNo, StartDate, RepaymentDate, InterestRate, Principal, Interest, Charge, CapitalizedAmount, Installment, OutstandingBalance, TotalInstallmentAmount, Days) VALUES("
	                                + "'" + ProcessInstanceId + "',"
	                                + "'" + StageNumber + "',"
	                                + "'" + InstallmentNo + "',"
	                                + "'" + StartDate + "',"
	                                + "'" + RepaymentDate + "',"
	                                + "'" + InterestRate + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, Principal) + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, Interest) + "',"
	                                + "'" + Charge + "',"
	                                + "'" + CapitalizedAmount + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, Installment) + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, OutstandingBalance) + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, TotalInstallmentAmount) + "',"
	                                + "'" + Days + "')";
	                        Log.consoleLog(ifr, "InQuery2==>" + InQuery2);
	                        ifr.saveDataInDB(InQuery2);
	                        
	                        String queryForInst="UPDATE STAFF_LOAN_AGREEMENT_DYNAMIC_INSTALLMENT SET SNO='"+InstallmentNo+"',DUE_DATE='"+RepaymentDate+"',PRICIPAL='"+Principal+"',INTEREST='"+Interest+"',AMOUNT_OF_INSTALLMENT='"+TotalInstallmentAmount+"' "
	    							+ "WHERE WINAME='"+processInstanceId+"'";
	    					ifr.saveDataInDB(queryForInst);
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

	            if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//	                return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
	            	 return RLOS_Constants.ERROR + ":" + apiErrorMessage;
	            } else {
	                return apiStatus;
	            }


	        } catch (Exception e) {
	            Log.consoleLog(ifr, "Exception/ExecuteCBS_Ammortization===>" + e);
	            Log.errorLog(ifr, "Exception/ExecuteCBS_Ammortization===>" + e);
	        } finally {
	            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
	                    apiErrorCode, apiErrorMessage, apiStatus);
	        }
	        return RLOS_Constants.ERROR + ":" + apiErrorMessage;
	        //return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
	}
	public String ExecuteCBS_AmmortizationHRMSVL(IFormReference ifr, String ProcessInstanceId, String loanAmount, String loanterm, String productCode,String strScheduleCode) {
		 String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
	        String apiName = "Ammortization";
	        String serviceName = "CBS_" + apiName;
	        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
	        String apiStatus = "";
	        String apiErrorCode = "";
	        String apiErrorMessage = "";
	        String request = "";
	        String response = "";
	        try {

	            Log.consoleLog(ifr, "Entered into ExecuteCBS_Ammortization...");


	            String BankCode = pcm.getConstantValue(ifr, "CBSAMMOR", "BANKCODE");
	            String Channel = pcm.getConstantValue(ifr, "CBSAMMOR", "CHANNEL");
	            String UserId = pcm.getConstantValue(ifr, "CBSLADFDENQ", "SC_USERID");
	          
	            String TBranch = pcm.getConstantValue(ifr, "CBSAMMOR", "TBRANCH");
	            
	            loanterm=loanterm.replaceFirst("\\..*$", "");

                   String queryRateChartCode="SELECT DISTINCT " +
                   "    a.cod_rate_chart, " +
                   "    (a.cod_rate_chart || '--' || a.nam_rate_chart) AS cbs_ratechartcode, " +
                   "    a.cod_prod," +
                   "    a.cod_sched_type," +
                   "    a.COD_RATE_REGIME" +
                   "  FROM LN_PROD_RATE_CHART_XREF a," +
                   "     LN_SCHED_TYPES b " +
                   "WHERE a.COD_PROD = b.COD_PROD " + 
                   "  AND a.COD_SCHED_TYPE = b.COD_SCHED_TYPE " +
                   "  AND a.COD_PROD = '"+productCode+"'" +
                   "  AND a.FLG_DELETE = 'N' " +
                   "  AND a.FLG_MNT_STATUS = 'A' " +
                   "  AND b.dat_sched_exp >= SYSDATE " +
                  "  AND a.COD_SCHED_TYPE = '"+strScheduleCode+"' " +
                  "  AND a.COD_RATE_REGIME = 'L'";
                   List<List<String>> resQueryRateChartCode=ifr.getDataFromDB(queryRateChartCode);
                   Log.consoleLog(ifr, "Rate chart code query..."+queryRateChartCode);
                   String morotoriam = "0";
					String queryMORATORIAM = "SELECT MORATORIAM FROM SLOS_STAFF_HOME_TRN where winame='"
							+ processInstanceId + "'";
					List<List<String>> resqueryMORATORIAM = ifr.getDataFromDB(queryMORATORIAM);
					Log.consoleLog(ifr, "resqueryMORATORIAM..." + resqueryMORATORIAM);
					if (!resqueryMORATORIAM.isEmpty()) {
						morotoriam = resqueryMORATORIAM.get(0).get(0);
					}
					morotoriam = "0";
                   
	            String accountVarianceRate="0";
	            String rateChartCode=resQueryRateChartCode.size()>0?resQueryRateChartCode.get(0).get(0):"0";
                   if(rateChartCode.equals("0")){
                       return "ERROR, rateChartCode is zero";
                   }

	            String typeOfInterest="Floating";
	            String extUniqueRefId = generateUniqueID(17);
	            
	            String loanAmtRounded = pcm.mAccRoundOffvalue(ifr, loanAmount);

	                  request = "{\n"
	                    + "  \"input\": {\n"
	                    + "    \"SessionContext\": {\n"
	                    + "      \"SupervisorContext\": {\n"
	                    + "        \"PrimaryPassword\": \"\",\n"
	                    + "        \"UserId\": \"\"\n"
	                    + "      },\n"
	                    + "      \"BankCode\": " + BankCode + ",\n"
	                    + "      \"Channel\": \"" + Channel + "\",\n"
	                    + "      \"ExternalBatchNumber\": \"\",\n"
	                    + "      \"ExternalReferenceNo\": \"" + extUniqueRefId + "\",\n"
	                    + "      \"ExternalSystemAuditTrailNumber\": \"\",\n"
	                    + "      \"LocalDateTimeText\": \"\",\n"
	                    + "      \"OriginalReferenceNo\": \"\",\n"
	                    + "      \"OverridenWarnings\": \"\",\n"
	                    + "      \"PostingDateText\": \"\",\n"
	                    + "      \"ServiceCode\": \"\",\n"
	                    + "      \"SessionTicket\": \"\",\n"
	                    + "      \"TransactionBranch\": \"" + TBranch + "\",\n"
	                    + "      \"UserId\": \"" + UserId + "\",\n"
	                    + "      \"UserReferenceNumber\": \"" + extUniqueRefId + "\",\n"
	                    + "      \"ValueDateText\": \"\"\n"
	                    + "    },\n"
	                    + "    \"accountVarianceRate\": \"" + accountVarianceRate + "\",\n" // 0
	                    + "    \"productCode\": \"" + productCode +  "\",\n" // 1
	                    + "    \"scheduleType\": \"" + strScheduleCode + "\",\n" // 1
	                    + "    \"rateChartCode\":\"" + rateChartCode + "\",\n" // 0
	                    + "    \"loanAmount\": \"" + loanAmtRounded + "\",\n"
	                    + "    \"loanTerm\": \"" + loanterm + "\",\n"
	                    + "    \"typeOfInterest\": 1,\n" // 0 for fixed 1 for floating
	                    + "    \"disbursementDate\": \"\",\n"
	                    + "    \"ioiPeriod\": \""+morotoriam+"\",\n"  // 3 months 6 , 9
	                    + "    \"salaryGenDateFlag\": \"\",\n"
	                    + "    \"salaryDayDatePart\": \"\",\n"
	                    + "    \"preferredRepayDatePart\": \"\"\n"
	                    + "  }\n"
	                    + "}";
	            // String query = "delete from LOS_STG_CBS_AMM_DEFN_DETAILS where ProcessInstanceId='" + ProcessInstanceId + "'";
	            //Log.consoleLog(ifr, "query==>" + query);
	            //ifr.saveDataInDB(query);
	            //HashMap<String, String> requestHeader = new HashMap<>();
	            response = cm.getWebServiceResponse(ifr, apiName, request);
	            Log.consoleLog(ifr, "Response===>" + response);

	            if (!response.equalsIgnoreCase("{}")) {
	                JSONParser parser = new JSONParser();
	                JSONObject resultObj = (JSONObject) parser.parse(response);
	                // JSONObject resultObj = new JSONObject(OutputJSON);

	                String body = resultObj.get("body").toString();
	                JSONObject bodyObj = (JSONObject) parser.parse(body);
	                //     JSONObject bodyObj = new JSONObject(bodyJSON);

	                String CheckError = cm.GetAPIErrorResponse(ifr, ProcessInstanceId, bodyObj);
	                Log.consoleLog(ifr, "CheckError===>" + CheckError);
	                //CheckError = "true";
	                PortalCommonMethods pcm = new PortalCommonMethods();
	                if (CheckError.equalsIgnoreCase("true")) {

	                    //Deleting the existing details available in the system.
	                    String Query1 = "DELETE FROM LOS_STG_CBS_AMM_DEFN_DETAILS "
	                            + "WHERE ProcessInstanceId='" + ProcessInstanceId + "'";
	                    Log.consoleLog(ifr, "Query1===>" + Query1);
	                    ifr.saveDataInDB(Query1);

	                    String Query2 = "DELETE FROM LOS_STG_CBS_AMM_SCH_DETAILS "
	                            + "WHERE ProcessInstanceId='" + ProcessInstanceId + "'";
	                    Log.consoleLog(ifr, "Query2===>" + Query2);
	                    ifr.saveDataInDB(Query2);
	                    //Deleting the existing details available in the system.

	                    String XfaceExtScheduleDefnDTO = bodyObj.get("XfaceExtScheduleDefnDTO").toString();
	                    Log.consoleLog(ifr, "XfaceExtScheduleDefnDTO==>" + XfaceExtScheduleDefnDTO);

	                    JSONArray XfaceExtScheduleDefnDTOJSON = (JSONArray) parser.parse(XfaceExtScheduleDefnDTO);
	                    Log.consoleLog(ifr, "XfaceExtScheduleDefnDTOJSON.size()==>" + XfaceExtScheduleDefnDTOJSON.size());
	                    for (int i = 0; i < XfaceExtScheduleDefnDTOJSON.size(); i++) {
	                        //  System.out.println("XfaceExtScheduleDefnDTOJSON==>" + XfaceExtScheduleDefnDTOJSON.get(i));
	                        String InputString = XfaceExtScheduleDefnDTOJSON.get(i).toString();
	                        Log.consoleLog(ifr, "InputString==>" + InputString);
	                        JSONObject InputStringResponseJSON = (JSONObject) parser.parse(InputString);

	                        String StageNumber = InputStringResponseJSON.get("StageNumber").toString();
	                        String StageName = InputStringResponseJSON.get("StageName").toString();
	                        String StageStartDate = InputStringResponseJSON.get("StageStartDate").toString();
	                        String PrincipalAmount = InputStringResponseJSON.get("PrincipalAmount").toString();
	                        String PrincipalPayments = InputStringResponseJSON.get("PrincipalPayments").toString();
	                        String InterestPayments = InputStringResponseJSON.get("InterestPayments").toString();
	                        String FirstPrincipalDueDate = InputStringResponseJSON.get("FirstPrincipalDueDate").toString();
	                        String FirstInterestDueDate = InputStringResponseJSON.get("FirstInterestDueDate").toString();
	                        String InstallmentAmount = InputStringResponseJSON.get("InstallmentAmount").toString();
	                        String StageEndDate = InputStringResponseJSON.get("StageEndDate").toString();

	                        String InQuery1 = "INSERT INTO LOS_STG_CBS_AMM_DEFN_DETAILS(ProcessInstanceId,StageNumber, "
	                                + "StageName, StageStartDate, PrincipalAmount, PrincipalPayments, InterestPayments,"
	                                + " FirstPrincipalDueDate, FirstInterestDueDate, InstallmentAmount, StageEndDate)"
	                                + " VALUES("
	                                + "'" + ProcessInstanceId + "',"
	                                + "'" + StageNumber + "',"
	                                + "'" + StageName + "',"
	                                + "'" + StageStartDate + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, PrincipalAmount) + "',"
	                                + "'" + PrincipalPayments + "',"
	                                + "'" + InterestPayments + "',"
	                                + "'" + FirstPrincipalDueDate + "',"
	                                + "'" + FirstInterestDueDate + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, InstallmentAmount) + "',"
	                                + "'" + StageEndDate + "')";
	                        Log.consoleLog(ifr, "InQuery1==>" + InQuery1);
                                LoanAmtInWords inWords=new LoanAmtInWords();
                                String valueInWords="";
                       try{
                       valueInWords= inWords.amtInWords(Double.parseDouble(InstallmentAmount));
                       }catch(Exception e){
                           
                       }
                       
                      String updateQuery=" update  SLOS_STAFF_TRN set INSTALLMENT_AMOUNT_IN_WORDS='"+valueInWords+"' where  winame='"+processInstanceId+"'";
                      if(i==0)
                      ifr.saveDataInDB(updateQuery);
                      Log.consoleLog(ifr, "valueInWords==>"+valueInWords);
	                        ifr.saveDataInDB(InQuery1);
	                    }

	                    String XfaceExtScheduleDtlsDTO = bodyObj.get("XfaceExtScheduleDtlsDTO").toString();

	                    JSONArray XfaceExtScheduleDtlsDTOJSON = (JSONArray) parser.parse(XfaceExtScheduleDtlsDTO);
	                    Log.consoleLog(ifr, "XfaceExtScheduleDtlsDTOJSON.size()==>" + XfaceExtScheduleDtlsDTOJSON.size());
	                    //   query = "delete from LOS_STG_CBS_AMM_SCH_DETAILS where ProcessInstanceId='" + ProcessInstanceId + "'";
	                    // Log.consoleLog(ifr, "query==>" + query);
	                    //ifr.saveDataInDB(query);
	                    for (int i = 0; i < XfaceExtScheduleDtlsDTOJSON.size(); i++) {
	                        String InputString = XfaceExtScheduleDtlsDTOJSON.get(i).toString();
	                        // System.out.println("InputString==>" + InputString);
	                        JSONObject InputStringResponseJSON = (JSONObject) parser.parse(InputString);

	                        String StageNumber = InputStringResponseJSON.get("StageNumber").toString();
	                        String InstallmentNo = InputStringResponseJSON.get("InstallmentNo").toString();
	                        String StartDate = InputStringResponseJSON.get("StartDate").toString();
	                        String RepaymentDate = InputStringResponseJSON.get("RepaymentDate").toString();
	                        String InterestRate = InputStringResponseJSON.get("InterestRate").toString();
	                        String Principal = InputStringResponseJSON.get("Principal").toString();
	                        String Interest = InputStringResponseJSON.get("Interest").toString();
	                        String Charge = InputStringResponseJSON.get("Charge").toString();
	                        String CapitalizedAmount = InputStringResponseJSON.get("CapitalizedAmount").toString();
	                        String Installment = InputStringResponseJSON.get("Installment").toString();
	                        String OutstandingBalance = InputStringResponseJSON.get("OutstandingBalance").toString();
	                        String TotalInstallmentAmount = InputStringResponseJSON.get("TotalInstallmentAmount").toString();
	                        String Days = InputStringResponseJSON.get("Days").toString();

	                        String InQuery2 = "INSERT INTO LOS_STG_CBS_AMM_SCH_DETAILS(ProcessInstanceId,StageNumber, InstallmentNo, StartDate, RepaymentDate, InterestRate, Principal, Interest, Charge, CapitalizedAmount, Installment, OutstandingBalance, TotalInstallmentAmount, Days) VALUES("
	                                + "'" + ProcessInstanceId + "',"
	                                + "'" + StageNumber + "',"
	                                + "'" + InstallmentNo + "',"
	                                + "'" + StartDate + "',"
	                                + "'" + RepaymentDate + "',"
	                                + "'" + InterestRate + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, Principal) + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, Interest) + "',"
	                                + "'" + Charge + "',"
	                                + "'" + CapitalizedAmount + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, Installment) + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, OutstandingBalance) + "',"
	                                + "'" + pcm.mCheckBigDecimalValueDecimal(ifr, TotalInstallmentAmount) + "',"
	                                + "'" + Days + "')";
	                        Log.consoleLog(ifr, "InQuery2==>" + InQuery2);
	                        ifr.saveDataInDB(InQuery2);
	                        
	                        String queryForInst="UPDATE STAFF_LOAN_AGREEMENT_DYNAMIC_INSTALLMENT SET SNO='"+InstallmentNo+"',DUE_DATE='"+RepaymentDate+"',PRICIPAL='"+Principal+"',INTEREST='"+Interest+"',AMOUNT_OF_INSTALLMENT='"+TotalInstallmentAmount+"' "
	    							+ "WHERE WINAME='"+processInstanceId+"'";
	    					ifr.saveDataInDB(queryForInst);
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

//	            if (apiErrorMessage.equalsIgnoreCase("")) {
//	                apiStatus = RLOS_Constants.SUCCESS;
//	            } else {
//	                apiStatus = RLOS_Constants.ERROR;
//	            }
//
//	            if (apiStatus.equalsIgnoreCase(RLOS_Constants.ERROR)) {
//	                return RLOS_Constants.ERROR + ":" + apiErrorMessage;
//	            } else {
//	                return apiStatus;
//	            }
//
//
//	        } catch (Exception e) {
//	            Log.consoleLog(ifr, "Exception/ExecuteCBS_Ammortization===>" + e);
//	            Log.errorLog(ifr, "Exception/ExecuteCBS_Ammortization===>" + e);
//	        } finally {
//	            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
//	                    apiErrorCode, apiErrorMessage, apiStatus);
//	        }
//	        return RLOS_Constants.ERROR + ":" + apic.getErrorCodeDescription(ifr, serviceName, apiErrorCode);
	            if (apiErrorMessage.equalsIgnoreCase("")) {
					apiStatus = RLOS_Constants.SUCCESS;
				} else {
					apiStatus = RLOS_Constants.ERROR;
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


	public static String generateUniqueID(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10)); // Appending digits from 0 to 9
        }

        return sb.toString();
    }
}
