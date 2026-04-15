package com.newgen.iforms.staffHL;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.Log;

/**
 *
 * @author kathir.b
 */
public class TransUnionCommercial {

    APICommonMethods cm = new APICommonMethods();
    CommonFunctionality cf = new CommonFunctionality();
    PortalCommonMethods pcm = new PortalCommonMethods();

    public String getCommercialCIBILScore(IFormReference ifr, String enquiryAmount, String insertionOrderId) {

        Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> Inside getCommercialCIBILScore");

        String apiName = "CIBILCommercial";
        String Request = "";
        String Response = "";

        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String applicantType = "";

        try {
            String productCode = pcm.getProductCode(ifr);
            String subProductCode = pcm.getSubProductCode(ifr);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> productCode: " + productCode);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> subProductCode: " + subProductCode);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("ddMMyyyy");
            LocalDateTime now = LocalDateTime.now();
            String MonitoringDate = dtf.format(now);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> MonitoringDate: " + MonitoringDate);

            String firstName = "";
            String middleName = "";
            String lastName = "";
            String fullName = "";
            String customerCategory = "";
            String PAN = "";
            String teleNo = "";

            String AddressLine1 = "", AddressLine2 = "", AddressLine3 = "", City = "", State = "", Pincode = "";
            String class_of_activity = "";

            String Query1 = "";
            String Query2 = "";

            Query1 = "select a.firstname,a.middlename,a.lastname,b.kyc_no,c.entitytype,c.fullname,c.applicanttype,a.OFFTELNO"
                    + " from los_l_basic_info_i a, LOS_NL_KYC b,\n"
                    + "los_nl_basic_info c where a.f_key=b.f_key\n"
                    + "and a.f_key=c.f_key and c.pid='" + ProcessInstanceId + "' "
                    + "and c.insertionorderid='" + insertionOrderId + "' and b.KYC_ID in ('TID', 'TAXID')";

            Query2 = "select line1,line2,line3,city_town_village,state,pincode "
                    + "from LOS_NL_Address where F_KEY =(select F_KEY from los_nl_basic_info "
                    + " where PID ='" + ProcessInstanceId + "' and insertionorderid='" + insertionOrderId + "')\n"
                    + " and addresstype='P'";
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> Query1: " + Query1);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> Query2: " + Query2);

            List<List<String>> Result = ifr.getDataFromDB(Query1);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> Result: " + Result.toString());

            if (Result.size() > 0) {
                firstName = Result.get(0).get(0);
                middleName = Result.get(0).get(1);
                lastName = Result.get(0).get(2);
                applicantType = Result.get(0).get(6);
                teleNo = Result.get(0).get(7);

                if (lastName.equalsIgnoreCase("{}")) {
                    lastName = "";
                }
                if (lastName.equalsIgnoreCase("{")) {
                    lastName = "";
                }
                if (lastName.equalsIgnoreCase("}")) {
                    lastName = "";
                }
                PAN = Result.get(0).get(3);
                fullName = Result.get(0).get(5);
                customerCategory = Result.get(0).get(4);
                if (customerCategory.equalsIgnoreCase("NI")) {
                    customerCategory = "Proprietorship";
                } else if (customerCategory.equalsIgnoreCase("I")) {
                    customerCategory = "Individual";
                }
            }

            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> firstName: " + firstName);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> lastName: " + lastName);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> PAN: " + PAN);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> fullName: " + fullName);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> customerCategory: " + customerCategory);

            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> Query2: " + Query2);
            List<List<String>> Result1 = ifr.getDataFromDB(Query2);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> Result1: " + Result1.toString());

            if (Result1.size() > 0) {
                AddressLine1 = Result1.get(0).get(0);
                AddressLine2 = Result1.get(0).get(1);
                AddressLine3 = Result1.get(0).get(2);
                City = Result1.get(0).get(3);
                State = Result1.get(0).get(4);
                Pincode = Result1.get(0).get(5);
            }

            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> AddressLine1: " + AddressLine1);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> AddressLine2: " + AddressLine2);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> AddressLine3: " + AddressLine3);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> City: " + City);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> State: " + State);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> Pincode: " + Pincode);

            String enquiryPurpose = pcm.getParamValue(ifr, productCode, "ENQUIRYPURPOSE");
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> enquiryPurpose: " + enquiryPurpose);

            class_of_activity = pcm.getParamValue(ifr, productCode, "CLASSOFACTIVITY");
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> class_of_activity: " + class_of_activity);

            String enquiryType = pcm.getParamValue(ifr, productCode, "ENQUIRYTYPE");
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> enquiryType: " + enquiryType);

            String cmrFlag = pcm.getParamValue(ifr, productCode, "CMRFLAG");
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> cmrFlag: " + cmrFlag);

            Request = "{\n"
                    + "  \"request\": {\n"
                    + "    \"search_data\": {\n"
                    + "      \"general_fields\": {\n"
                    + "        \"enquiry_amount\": \"" + enquiryAmount + "\",\n"
                    + "        \"enquiry_purpose\": \"" + enquiryPurpose + "\",\n"
                    + "        \"enquiry_type\": \"" + enquiryType + "\",\n"
                    + "        \"type_of_entity\": \"" + customerCategory + "\",\n"
                    + "        \"class_of_activity\": \"" + class_of_activity + "\",\n"
                    + "        \"date_of_registration\": \"\",\n"
                    + "        \"cmr_flag\": \"" + cmrFlag + "\"\n"
                    + "      },\n"
                    + "      \"company_name\": {\n"
                    + "        \"name\": \"" + fullName + "\"\n"
                    + "      },\n"
                    + "      \"contact\": {\n"
                    + "        \"address\": [\n"
                    + "          {\n"
                    + "            \"addressLine1\": \"" + AddressLine1 + ", " + AddressLine2 + ", " + AddressLine3 + "\",\n"
                    + "            \"city\": \"" + City + "\",\n"
                    + "            \"state\": \"" + State + "\",\n"
                    + "            \"pinCode\": \"" + Pincode + "\",\n"
                    + "            \"addressType\": \"registered office\"\n"
                    + "          }\n"
                    + "        ]\n"
                    + "      },\n"
                    + "      \"id\": {\n"
                    + "        \"pan\": \"" + PAN + "\",\n"
                    + "        \"cin\": \"\",\n"
                    + "        \"crn\": \"\",\n"
                    + "        \"tin\": \"\"\n"
                    + "      },\n"
                    + "      \"telephone\": {\n"
                    + "        \"telephoneType\": \"\",\n"
                    + "        \"telephone_num\": \"" + teleNo + "\",\n"
                    + "        \"contact_area\": \"\",\n"
                    + "        \"contact_prefix\": \"\"\n"
                    + "      },\n"
                    + "      \"directors\": {\n"
                    + "        \"director\": [\n"
                    + "          {\n"
                    + "            \"name\": \"\",\n"
                    + "            \"relation_type\": \"\",\n"
                    + "            \"gender\": \"\",\n"
                    + "            \"pan\": \"\",\n"
                    + "            \"uid\": \"\",\n"
                    + "            \"voter_id\": \"\",\n"
                    + "            \"passport_num\": \"\",\n"
                    + "            \"driving_licence_id\": \"\",\n"
                    + "            \"din\": \"\",\n"
                    + "            \"dob\": \"\",\n"
                    + "            \"address\": {\n"
                    + "              \"addressType\": \"\",\n"
                    + "              \"addressLine0\": \"\",\n"
                    + "              \"city\": \"\",\n"
                    + "              \"state\": \"\",\n"
                    + "              \"pinCode\": \"\"\n"
                    + "            },\n"
                    + "            \"telephone\": [\n"
                    + "              {\n"
                    + "                \"telephoneType\": \"\",\n"
                    + "                \"telephone_num\": \"\",\n"
                    + "                \"contact_area\": \"\",\n"
                    + "                \"contact_prefix\": \"\"\n"
                    + "              },\n"
                    + "              {\n"
                    + "                \"telephoneType\": \"\",\n"
                    + "                \"telephone_num\": \"\",\n"
                    + "                \"contact_area\": \"\",\n"
                    + "                \"contact_prefix\": \"\"\n"
                    + "              }\n"
                    + "            ]\n"
                    + "          },\n"
                    + "          {\n"
                    + "            \"name\": \"\",\n"
                    + "            \"relation_type\": \"\",\n"
                    + "            \"gender\": \"\",\n"
                    + "            \"pan\": \"\",\n"
                    + "            \"uid\": \"\",\n"
                    + "            \"voter_id\": \"\",\n"
                    + "            \"passport_num\": \"\",\n"
                    + "            \"driving_licence_id\": \"\",\n"
                    + "            \"din\": \"\",\n"
                    + "            \"dob\": \"\",\n"
                    + "            \"address\": {\n"
                    + "              \"addressType\": \"\",\n"
                    + "              \"addressLine0\": \"\",\n"
                    + "              \"city\": \"\",\n"
                    + "              \"state\": \"\",\n"
                    + "              \"pinCode\": \"\"\n"
                    + "            },\n"
                    + "            \"telephone\": [\n"
                    + "              {\n"
                    + "                \"telephoneType\": \"\",\n"
                    + "                \"telephone_num\": \"\",\n"
                    + "                \"contact_area\": \"\",\n"
                    + "                \"contact_prefix\": \"\"\n"
                    + "              },\n"
                    + "              {\n"
                    + "                \"telephoneType\": \"\",\n"
                    + "                \"telephone_num\": \"\",\n"
                    + "                \"contact_area\": \"\",\n"
                    + "                \"contact_prefix\": \"\"\n"
                    + "              }\n"
                    + "            ]\n"
                    + "          }\n"
                    + "        ]\n"
                    + "      }\n"
                    + "    }\n"
                    + "  }\n"
                    + "}";

            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> Request: " + Request);
            Response = cm.getWebServiceResponse(ifr, apiName, Request);
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> Response: " + Response);
            String errorResponseCode = "";
            if (!Response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject resultObj = (JSONObject) parser.parse(Response);
                errorResponseCode = resultObj.get("responseCode").toString();
                Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> responseCode: " + errorResponseCode);
                
                JSONObject data = (JSONObject) resultObj.get("body");
                JSONObject base = (JSONObject) data.get("base");
                JSONObject responseReport = (JSONObject) base.get("responseReport");
                JSONObject productSec = (JSONObject) responseReport.get("productSec");
                JSONObject rankSec = (JSONObject) productSec.get("rankSec");
                
                String CMRScore = "";
                String ExclusionReason = "";
                if (!rankSec.isEmpty()) {
                    JSONArray rankVec = (JSONArray) rankSec.get("rankVec");
                    String rankNameToMatch = pcm.getParamValue(ifr, productCode, "RANKNAME");
                    Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> rankNameToMatch: " + rankNameToMatch);

                    for (int i = 0; i < rankVec.size(); i++) {
                        JSONObject rankVecobj = (JSONObject) rankVec.get(i);
                        Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> rankName: " + rankVecobj.get("rankName").toString());
                        Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> rankValue: " + rankVecobj.get("rankValue").toString());

                        if (rankVecobj.get("rankName").toString().equalsIgnoreCase(rankNameToMatch)) {
                            CMRScore = rankVecobj.get("rankValue").toString();
                            ExclusionReason = rankVecobj.get("exclusionReason").toString();
                            break;
                        }
                    }
                }
                CMRScore = (CMRScore.equalsIgnoreCase("")) ? "NA" : CMRScore;
                Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> CMRScore: " + CMRScore);

                String cicReportGenReq = pcm.getConstantValue(ifr, "TRANSUNION", "CICREPORTGENREQ");
                Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> cicReportGenReq: " + cicReportGenReq);

                if (cicReportGenReq.equalsIgnoreCase("Y")) {
                    if (!(cf.getJsonValue(data, "encodedBase64").equalsIgnoreCase(""))) {
                        String encodedB64 = cf.getJsonValue(data, "encodedBase64");
                        Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> Encoded string: " + encodedB64);
                        String generateReportStatus = cm.generateReport(ifr, ProcessInstanceId, "Commercial",
                                encodedB64, "NGREPORTTOOL_TRANSUNION");
                        Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> generateReportStatus: " + generateReportStatus);
                        cm.updateCICReportStatus(ifr, "CIBIL_COMMERCIAL", generateReportStatus, applicantType);
                    }
                }
                checkForKnockOff(ifr, Response, ProcessInstanceId, CMRScore, ExclusionReason, productCode,
                        subProductCode, applicantType, insertionOrderId);
                return RLOS_Constants.SUCCESS;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> Exception: " + e);
            Log.errorLog(ifr, "TransUnionCommercial:getCommercialCIBILScore -> Exception: " + e);
        } finally {
            cm.captureCICRequestResponse(ifr, ProcessInstanceId, "Transunion_Consumer", Request, Response, "", "", "",
                    (!insertionOrderId.equalsIgnoreCase("") ? insertionOrderId : applicantType));
        }
        return RLOS_Constants.ERROR;
    }

    public String checkForKnockOff(IFormReference ifr, String jsonResponse,
            String PID, String CmScore, String ExclusionReason, String productCode, String subProductCode,
            String applicantType, String insertionOrderId) {
        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> Inside checkForKnockOff");

        String assetClassify = "NA";
        String statusCF = "NA";
        String overDue = "NA";
        String suitFiled = "NA";
        String writeOFF = "NA";
        String settledCF = "NA";
        String contracts = "NA";
        String dpd31 = "NA";
        String dpd61 = "NA";
        String dpd91 = "NA";
        String dpd180 = "NA";
        String acOrdc = "NA";
        String cfType = "NA";
        String loanEXP = "12";
        String ACorDPDCombined = "";
        String CFMonthCombined = "";
        String dpd1combined = "";
        String dpd31combined = "";
        String dpd61combined = "";
        String dpd91combined = "";
        String dpd180combined = "";
        String enqmonth1 = "";
        String enqmonth2 = "";
        String enqmonth4 = "";
        String enqmonth7 = "";

        int assetClassifycount = 0;
        int statuscount = 0;
        int overDuecount = 0;
        int suitFiledcount = 0;
        int writeOFFcount = 0;
        int settledcount = 0;
        int dpd31count = 0;
        int dpd61count = 0;
        int dpd91count = 0;
        int contractscount = 0;
        int dpd180count = 0;
        int acOrdccount = 0;
        int cfTypecount = 0;

        try {
            String excludedCICDpdType = pcm.getParamConfig(ifr, productCode, subProductCode, "COMMERCIALCONF", "CICDPDASSETTYPE");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> excludedCICDpdType: " + excludedCICDpdType);

            String excludedCICCreditStatus = pcm.getParamConfig(ifr, productCode, subProductCode, "COMMERCIALCONF", "CICCREDIT");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> excludedCICCreditStatus: " + excludedCICCreditStatus);

            String excludedCICSuitFiled = pcm.getParamConfig(ifr, productCode, subProductCode, "COMMERCIALCONF", "CICSUITFILED");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> excludedCICSuitFiled: " + excludedCICSuitFiled);

            String excludedCICAccType = pcm.getParamConfig(ifr, productCode, subProductCode, "COMMERCIALCONF", "CICACCTTYPE");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> excludedCICAccType: " + excludedCICAccType);

            String regex = "\\d+";
            JSONParser parser = new JSONParser();
            JSONObject responseMain = (JSONObject) parser.parse(jsonResponse);
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> responseMain: " + responseMain);
            JSONObject bodyObj = (JSONObject) responseMain.get("body");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> bodyObj: " + bodyObj);
            JSONObject baseObj = (JSONObject) bodyObj.get("base");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> baseObj: " + baseObj);
            JSONObject responseReportObj = (JSONObject) baseObj.get("responseReport");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> responseReportObj: " + responseReportObj);
            JSONObject productSecObj = (JSONObject) responseReportObj.get("productSec");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> productSecObj: " + productSecObj);

            // Enquiries Total
            JSONObject enquirySummarySecObj = (JSONObject) productSecObj.get("enquirySummarySec");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> enquirySummarySecObj: " + enquirySummarySecObj);
            JSONObject enquiryTotalObj = (JSONObject) enquirySummarySecObj.get("enquiryTotal");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> enquiryTotalObj: " + enquiryTotalObj);
            JSONObject noOfEnquiriesObj = (JSONObject) enquiryTotalObj.get("noOfEnquiries");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> noOfEnquiriesObj: " + noOfEnquiriesObj);
            enqmonth1 = (String) noOfEnquiriesObj.get("month1");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> enqmonth1: " + enqmonth1);
            enqmonth2 = (String) noOfEnquiriesObj.get("month2to3");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> enqmonth2: " + enqmonth2);
            enqmonth4 = (String) noOfEnquiriesObj.get("month4to6");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> enqmonth4: " + enqmonth4);
            enqmonth7 = (String) noOfEnquiriesObj.get("month7to12");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> enqmonth7: " + enqmonth7);

            // Knockoff
            JSONObject creditFacilityDetailsasBorrowerSecVecObj = (JSONObject) productSecObj
                    .getOrDefault("creditFacilityDetailsasBorrowerSecVec", "");
            Log.consoleLog(ifr,
                    "TransUnionCommercial:checkForKnockOff -> creditFacilityDetailsasBorrowerSecVecObj: " + creditFacilityDetailsasBorrowerSecVecObj);
            JSONArray creditFacilityDetailsasBorrowerSec = (JSONArray) creditFacilityDetailsasBorrowerSecVecObj
                    .get("creditFacilityDetailsasBorrowerSec");
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> creditFacilityDetailsasBorrowerSec: " + creditFacilityDetailsasBorrowerSec);
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> creditFacilityDetailsasBorrowerSec.size() is: "
                    + creditFacilityDetailsasBorrowerSec.size());

            for (int i = 0; i < creditFacilityDetailsasBorrowerSec.size(); i++) {
                JSONObject obj = (JSONObject) creditFacilityDetailsasBorrowerSec.get(i);
                Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff ->  obj" + i + ": " + obj);

                JSONObject creditFacilityCurrentDetailsVecobj = (JSONObject) obj.get("creditFacilityCurrentDetailsVec");
                Log.consoleLog(ifr,
                        "TransUnionCommercial:checkForKnockOff -> creditFacilityCurrentDetailsVecobj: " + creditFacilityCurrentDetailsVecobj);

                JSONObject creditFacilityCurrentDetailsobj = (JSONObject) creditFacilityCurrentDetailsVecobj
                        .get("creditFacilityCurrentDetails");
                Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> creditFacilityCurrentDetailsobj: " + creditFacilityCurrentDetailsobj);

                String assetClassificationDaysPastDueDpd = (String) creditFacilityCurrentDetailsobj
                        .get("assetClassificationDaysPastDueDpd");
                Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> assetClassificationDaysPastDueDpd: " + assetClassificationDaysPastDueDpd);

                String faciltyType = (String) creditFacilityCurrentDetailsobj.getOrDefault("cfType", "");
                Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> faciltyType: " + faciltyType);

                String bankName = (String) creditFacilityCurrentDetailsobj.getOrDefault("cfMember", "");
                Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> bankName: " + bankName);

                if (!(bankName.trim()).equalsIgnoreCase("canara bank")) {
                    Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> INSIDE NOT GOLD LOAN CONDITION");

                    String loanExpiryDt = "";
                    JSONObject datesObj = (JSONObject) creditFacilityCurrentDetailsobj.get("dates");
                    SimpleDateFormat dateFormatOfClosedDate = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
                    SimpleDateFormat dateFormatOfClosedDate1 = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
                    Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> loanExpiryDt: "
                            + datesObj.containsKey("loanExpiryDt"));
                    Date dClose = new Date();

                    if (datesObj.containsKey("loanExpiryDt")) {
                        loanExpiryDt = (String) datesObj.get("loanExpiryDt");
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> loanExpiryDt: " + loanExpiryDt);
                        dClose = dateFormatOfClosedDate1.parse(loanExpiryDt);
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> dClose: " + dClose);

                        Date d12 = new Date();
                        Date curDate = new Date();
                        Calendar cal = Calendar.getInstance();
                        String currDate = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
                        cal.add(Calendar.MONTH, -12);
                        String past12Month = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
                        d12 = dateFormatOfClosedDate.parse(past12Month);
                        curDate = dateFormatOfClosedDate.parse(currDate);
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> d12: " + d12);
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> curDate: " + curDate);

                        // Loan Expiry
                        if (dClose.after(curDate)) {
                            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> dClos after");
                            loanEXP = "13";
                        } else if (dClose.before(curDate)) {
                            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> dClose before");
                            loanEXP = "12";
                        } else {
                            loanEXP = "13";
                        }

                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> assetClassificationDaysPastDueDpd: " + assetClassificationDaysPastDueDpd);
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> excludedCICDpdType: " + excludedCICDpdType);

                        // AssestClassification
                        boolean AssetStatus = checkKnockOffDPDCICFilterStatus(ifr, productCode,
                                assetClassificationDaysPastDueDpd, excludedCICDpdType);
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> AssetStatus: " + AssetStatus);
                        if (AssetStatus) {
                            acOrdccount = acOrdccount + 1;
                        }

                        // Status
                        String status = (String) creditFacilityCurrentDetailsobj.get("status");
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> status: " + status);
                        boolean suitStatus = checkKnockOffstatusCICFilterStatus(ifr, status, excludedCICSuitFiled);
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> suitStatus: " + suitStatus);
                        if (suitStatus) {
                            statuscount = statuscount + 1;
                        }
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> statuscount: " + statuscount);

                        // Overdue
                        JSONObject amount = (JSONObject) creditFacilityCurrentDetailsobj.get("amount");
                        String overdue = (String) amount.get("overdue");
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> overdue: " + overdue);
                        if (overdue != null && !"".equalsIgnoreCase(overdue) && Integer.parseInt(overdue) > 0) {
                            overDuecount = overDuecount + 1;
                        }

                        // Suitfiled
                        String suitFiledAmt = (String) amount.get("suitFiledAmt");
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> suitFiledAmt: " + suitFiledAmt);
                        if (suitFiledAmt != null && !"".equalsIgnoreCase(suitFiledAmt)
                                && Integer.parseInt(suitFiledAmt) > 0) {
                            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> suitFiledAmt:" + suitFiledAmt);
                            suitFiledcount = suitFiledcount + 1;
                        }

                        // writeOFF
                        String writtenOFF = (String) amount.get("writtenOFF");
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> writtenOFF:" + writtenOFF);
                        if (writtenOFF != null && !"".equalsIgnoreCase(writtenOFF)
                                && Integer.parseInt(writtenOFF) > 0) {
                            writeOFFcount = writeOFFcount + 1;
                        }

                        // settled
                        String settled = (String) amount.get("settled");
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> settled:" + settled);
                        if (settled != null && !"".equalsIgnoreCase(settled) && Integer.parseInt(settled) > 0) {
                            settledcount = settledcount + 1;
                        }

                        // Contracts
                        String contractsClassifiedAsNPA = (String) amount.get("contractsClassifiedAsNPA");
                        if (contractsClassifiedAsNPA != null && !"".equalsIgnoreCase(contractsClassifiedAsNPA)
                                && "Yes".equalsIgnoreCase(contractsClassifiedAsNPA)) {
                            contractscount = contractscount + 1;
                        }

                        // Credit Facility
                        JSONObject creditFacilityOverdueDetailsVec = (JSONObject) obj
                                .get("creditFacilityOverdueDetailsVec");
                        JSONObject creditFacilityOverdueDetails = (JSONObject) creditFacilityOverdueDetailsVec
                                .get("creditFacilityOverdueDetails");

                        // DPD91
                        // Compile the regex pattern
                        Pattern pattern = Pattern.compile(regex);
                        if (!dpd91combined.equalsIgnoreCase("")) {
                            dpd91combined = dpd91combined + ",";
                        }
                        String DPD91to180amt = (String) creditFacilityOverdueDetails.get("DPD91to180amt");

                        // Create a Matcher object
                        Matcher matcherDPD91to180amt = pattern.matcher(DPD91to180amt);// matcherDPD91to180amt.find()
                        if (!DPD91to180amt.equalsIgnoreCase("")) {
                            dpd91combined = dpd91combined + DPD91to180amt;
                        }

                        if (DPD91to180amt != null && !"".equalsIgnoreCase(DPD91to180amt.trim())
                                && matcherDPD91to180amt.find() && Integer.parseInt(DPD91to180amt.trim()) > 0) {
                            dpd91count = dpd91count + 1;
                        }

                        // DPD180
                        if (!dpd180combined.equalsIgnoreCase("")) {
                            dpd180combined = dpd180combined + ",";
                        }
                        String DPDabove180amt = (String) creditFacilityOverdueDetails.get("DPDabove180amt");
                        Matcher matcherDPDabove180amt = pattern.matcher(DPDabove180amt);//

                        if (!DPDabove180amt.equalsIgnoreCase("")) {
                            dpd180combined = dpd180combined + DPDabove180amt;
                        }
                        if (DPDabove180amt != null && !"".equalsIgnoreCase(DPDabove180amt.trim())
                                && matcherDPDabove180amt.find() && Integer.parseInt(DPDabove180amt.trim()) > 0) {
                            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> inside positive of DPDabove180amt");
                            dpd180count = dpd180count + 1;
                        }

                        // DPD61
                        if (!dpd61combined.equalsIgnoreCase("")) {
                            dpd61combined = dpd61combined + ",";
                        }
                        String DPD61t090amt = (String) creditFacilityOverdueDetails.get("DPD61t090amt");
                        Matcher matcherDPD61t090amt = pattern.matcher(DPD61t090amt);// matcherDPD61t090amt.find()
                        if (!DPD61t090amt.equalsIgnoreCase("")) {
                            dpd61combined = dpd61combined + DPD61t090amt;
                        }
                        if (DPD61t090amt != null && !"".equalsIgnoreCase(DPD61t090amt.trim())
                                && matcherDPD61t090amt.find() && Integer.parseInt(DPD61t090amt.trim()) > 0) {
                            dpd61count = dpd61count + 1;
                        }
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> dpd61count: " + dpd61count);

                        // DPD31
                        if (!dpd31combined.equalsIgnoreCase("")) {
                            dpd31combined = dpd31combined + ",";
                        }
                        String DPD31to60amt = (String) creditFacilityOverdueDetails.get("DPD31to60amt");
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> DPD31to60amt:" + DPD31to60amt);
                        Matcher matcherDPD31to60amt = pattern.matcher(DPD31to60amt);// matcherDPD31to60amt.find()
                        if (!DPD31to60amt.equalsIgnoreCase("")) {
                            dpd31combined = dpd31combined + DPD31to60amt;
                        }

                        // DPD1
                        if (!dpd1combined.equalsIgnoreCase("")) {
                            dpd1combined = dpd1combined + ",";
                        }
                        String DPD1tO30amt = (String) creditFacilityOverdueDetails.get("DPD61t090amt");
                        if (!DPD1tO30amt.equalsIgnoreCase("")) {
                            dpd1combined = dpd1combined + DPD1tO30amt;
                        }

                        // ACorDC
                        JSONObject CFHistoryforACOrDPDVec = (JSONObject) obj.get("CFHistoryforACOrDPDVec");
                        if (CFHistoryforACOrDPDVec == null) {
                            CFHistoryforACOrDPDVec = (JSONObject) obj.get("CFHistoryforACOrDPDupto24MonthsVec");
                        }
                        JSONArray CFHistoryforACOrDPD = (JSONArray) CFHistoryforACOrDPDVec.get("CFHistoryforACOrDPD");
                        if (CFHistoryforACOrDPD == null) {
                            CFHistoryforACOrDPD = (JSONArray) CFHistoryforACOrDPDVec
                                    .get("CFHistoryforACOrDPDupto24Months");
                        }

                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> CFHistoryforACOrDPD: " + CFHistoryforACOrDPD);
                        for (int j = 0; j < CFHistoryforACOrDPD.size(); j++) {
                            // AcorDCcombined
                            if (!ACorDPDCombined.equalsIgnoreCase("")) {
                                ACorDPDCombined = ACorDPDCombined + ",";
                            }
                            JSONObject obj2 = (JSONObject) CFHistoryforACOrDPD.get(j);
                            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> CFHistoryforACOrDPD obj2: " + obj2);
                            String ACorDPD = (String) obj2.get("ACorDPD");
                            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> ACorDPD: " + ACorDPD);
                            if (!ACorDPD.equalsIgnoreCase("")) {
                                ACorDPDCombined = ACorDPDCombined + ACorDPD;
                            }
                            boolean acdcStatus = checkKnockOffDPDCICFilterStatus(ifr, productCode, ACorDPD, excludedCICDpdType);
                            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> acdcStatus: " + acdcStatus);
                            if (acdcStatus) {
                                acOrdccount = acOrdccount + 1;
                            }
                            // Month combined
                            if (!CFMonthCombined.equalsIgnoreCase("")) {
                                CFMonthCombined = CFMonthCombined + ",";
                            }
                            String cf_month = (String) obj2.get("month");
                            if (!ACorDPD.equalsIgnoreCase("")) {
                                CFMonthCombined = CFMonthCombined + cf_month;
                            }

                        }
                        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> acOrdccount: " + acOrdccount);
                        if (loanEXP.equalsIgnoreCase("13")) {
                            // Account Type
                            if (!("Gold Loan".equalsIgnoreCase(faciltyType.trim()))
                                    && !("Current Account".equalsIgnoreCase(faciltyType.trim()))) {
                                boolean acdcStatus = checkKnockOffCICFilterStatus(ifr, faciltyType, excludedCICAccType);
                                Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> faciltyType: " + faciltyType);
                                Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> excludedCICAccType: " + excludedCICAccType);
                                Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> acdcStatus: " + acdcStatus);
                                if (acdcStatus) {
                                    Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> AssetStatus: " + acdcStatus);
                                    cfTypecount = cfTypecount + 1;
                                    Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> cfTypecount: " + cfTypecount);
                                }
                            }
                        }
                    }
                }
            }

            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> ACorDCCombined: " + ACorDPDCombined);
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> CFMonthCombined: " + CFMonthCombined);

            // BRMS Classification
            if (assetClassifycount > 0) {
                assetClassify = "Yes";
            } else {
                assetClassify = "No";
            }

            if (statuscount > 0) {
                statusCF = "Yes";
            } else {
                statusCF = "No";
            }

            if (overDuecount > 0) {
                overDue = "Yes";
            } else {
                overDue = "No";
            }

            if (suitFiledcount > 0) {
                suitFiled = "Yes";
            } else {
                suitFiled = "No";
            }

            if (writeOFFcount > 0) {
                writeOFF = "Yes";
            } else {
                writeOFF = "No";
            }

            if (settledcount > 0) {
                settledCF = "Yes";
            } else {
                settledCF = "No";
            }

            if (dpd31count > 0) {
                dpd31 = "Yes";
            } else {
                dpd31 = "No";
            }

            if (dpd61count > 0) {
                dpd61 = "Yes";
            } else {
                dpd61 = "No";
            }

            if (dpd91count > 0) {
                dpd91 = "Yes";
            } else {
                dpd91 = "No";
            }

            if (contractscount > 0) {
                contracts = "Yes";
            } else {
                contracts = "No";
            }

            if (dpd180count > 0) {
                dpd180 = "Yes";
            } else {
                dpd180 = "No";
            }

            if (acOrdccount > 0) {
                acOrdc = "Yes";
            } else {
                acOrdc = "No";
            }

            if (contractscount > 0) {
                contracts = "Yes";
            } else {
                contracts = "No";
            }

            if (cfTypecount > 0) {
                cfType = "Yes";
            } else {
                cfType = "No";
            }

            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> "
                    + "assetClassify: " + assetClassify + ", "
                    + "statusCF: " + statusCF + ", "
                    + "overDue: " + overDue + ", "
                    + "suitFiled: " + suitFiled + ", "
                    + "writeOFF: " + writeOFF + ", "
                    + "settledCF: " + settledCF + ", "
                    + "dpd31: " + dpd31 + ", "
                    + "dpd61: " + dpd61 + ", "
                    + "dpd91: " + dpd91 + ", "
                    + "contracts: " + contracts + ", "
                    + "dpd180: " + dpd180 + ", "
                    + "acOrdc: " + acOrdc + ", "
                    + "cfType: " + cfType);

        } catch (Exception e) {
            Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> Exception: " + e);
            Log.errorLog(ifr, "TransUnionCommercial:checkForKnockOff -> Exception: " + e);
        }

        String query = "insert into LOS_TRN_CREDITHISTORY(PID,CIC_SCORE,"
                + "OVERALL_OVERDUEAMT,BUREAUTYPE,BUREAUCODE,SERVICECODE,WRITEOFF_STATUS,"
                + "APPLICANTTYPE,APPLICANTID,PAYMENTHISTORY,DTINSERTED) "
                + "values('" + PID + "','" + CmScore + "','" + overDue + "','EXT','CBCOM','CMC','" + writeOFF + "','"
                + applicantType + "','" + insertionOrderId + "','" + acOrdc
                + "',SYSDATE)";

        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> Insert query: " + query);
        int queryResult = ifr.saveDataInDB(query);
        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> Insert queryResult: " + queryResult);

        String insquery = "insert into LOS_CAN_IBPS_BUREAUCHECK(PROCESSINSTANCEID,EXP_CBSCORE,"
                + "BUREAUTYPE,"
                + "APPLICANT_TYPE, APPLICANT_UID) " + "values('" + PID + "','"
                + CmScore + "','CBCOM','"
                + applicantType + "','" + insertionOrderId + "')";
        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> Insert query2: " + insquery);
        int queryResult2 = ifr.saveDataInDB(insquery);
        Log.consoleLog(ifr, "TransUnionCommercial:checkForKnockOff -> Insert queryResult2: " + queryResult2);

        return RLOS_Constants.SUCCESS;
    }

    public boolean checkKnockOffCICFilterStatus(IFormReference ifr, String accountType, String excludedAccnts) {
        Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffAccFilterStatus -> Inside checkKnockOffAccFilterStatus");
        Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffAccFilterStatus -> accountType: " + accountType);

        String[] excludedAccounts = excludedAccnts.split(",");
        for (String accnt : excludedAccounts) {
            if (accnt.equals(accountType)) {
                Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffAccFilterStatus -> excludedAccounts: " + accnt);
                return true;
            }
        }
        Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffAccFilterStatus -> Filter Condition not satisfied");
        return false;
    }

    public boolean checkKnockOffDPDCICFilterStatus(IFormReference ifr, String productCode, String accountType, String excludedAccnts) {
        Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffDPDCICFilterStatus -> Inside checkKnockOffDPDCICFilterStatus");
        Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffDPDCICFilterStatus -> accountType: " + accountType);

        String dpdMinimumPeriod = pcm.getParamValue(ifr, "TRANSUNIONCONFDPD", productCode);
        String[] excludedAccounts = excludedAccnts.split(",");
        if (accountType.contains("Days Past Due") && !accountType.equalsIgnoreCase("999 or above Days Past Due")) {
            String[] AccttypeArrays = accountType.split(" Days Past Due");
            accountType = AccttypeArrays[0];
            if (Integer.parseInt(accountType) > Integer.parseInt(dpdMinimumPeriod)) {
                return true;
            }
        }

        for (String accnt : excludedAccounts) {
            if (accnt.equals(accountType)) {
                Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffDPDCICFilterStatus -> excludedAccounts: " + accnt);
                return true;
            }
        }
        Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffDPDCICFilterStatus -> Filter Condition not satisfied");
        return false;

    }

    public boolean checkKnockOffstatusCICFilterStatus(IFormReference ifr, String accountType, String excludedAccnts) {
        Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffAccFilterStatus -> Inside checkKnockOffAccFilterStatus");
        Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffAccFilterStatus -> accountType: " + accountType);

        if (accountType.contains(",")) {
            String[] account = accountType.split(",");
            for (String mainString : account) {
                String[] excludedAccounts = excludedAccnts.split(",");
                for (String accnt : excludedAccounts) {
                    if (accnt.equals(mainString)) {
                        Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffAccFilterStatus -> excludedAccounts: " + accnt);
                        return true;
                    }
                }
            }
        } else {
            String[] excludedAccounts = excludedAccnts.split(",");
            for (String accnt : excludedAccounts) {
                if (accnt.equals(accountType)) {
                    Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffAccFilterStatus -> excludedAccounts: " + accnt);
                    return true;
                }
            }
        }
        Log.consoleLog(ifr, "TransUnionCommercial:checkKnockOffAccFilterStatus -> Filter Condition not satisfied..");
        return false;
    }
}