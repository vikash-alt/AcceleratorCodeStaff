/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.fintec;

import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.Log;
import com.newgen.dlp.integration.common.APICommonMethods;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.json.simple.parser.ParseException;

/**
 *
 * @author ahmed.zindha
 */
public class FraudCheck {

    APICommonMethods cm = new APICommonMethods();
    PortalCommonMethods pcm = new PortalCommonMethods();

    public String initiateIDVFraudCheck(IFormReference ifr, String journeyType,
            String enquiryAmount, String aadharNo, String applicantType) throws ParseException {

        Log.consoleLog(ifr, "#initiateIDVFraudCheck starting...");

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String apiName = "FraudCheck";
        String serviceName = "Transunion_" + apiName;
        Log.consoleLog(ifr, "apiName==>" + apiName + " || " + "serviceName==>" + serviceName);
        String apiStatus = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String request = "";
        String response = "";

        try {

            String mobileNumber = pcm.getMobileNumber(ifr);
            String firstName = "";
            String lastName = "";
            String birthDate = "";
            String gender = "";
            String gendercode = "";
            String parsedDate = "";
            String PAN = "";
            String addressLine1 = "", addressLine2 = "", addressLine3 = "", city = "",
                    state = "", pinCode = "", stateCodeNo = "";

            String query1 = "";
            String query2 = "";
            if (journeyType.equalsIgnoreCase("PAPL")) {
                query1 = "SELECT CUSTOMERFIRSTNAME,CUSTOMERLASTNAME,PANNUMBER,DATEOFBIRTH,GENDER,customerid "
                        + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + processInstanceId + "'";
                query2 = "SELECT permaddress1,permaddress2,permaddress3,PermCity,PermState,PermZip "
                        + "FROM LOS_T_CUSTOMER_ACCOUNT_SUMMARY WHERE WINAME='" + processInstanceId + "'";
            } else {
                query1 = "select a.firstname,a.lastname,b.kyc_no,to_char(a.dob,'dd-MM-YYYY')dob,a.gender,c.customerid from los_l_basic_info_i a, LOS_NL_KYC b,\n"
                        + "los_nl_basic_info c where a.f_key=b.f_key\n"
                        + "and a.f_key=c.f_key and c.pid='" + processInstanceId + "' "
                        + "and c.applicanttype='" + applicantType + "'";
                query2 = "select line1,line2,line3,city_town_village,state,pincode "
                        + "from LOS_NL_Address where F_KEY =(select F_KEY from los_nl_basic_info "
                        + " where PID ='" + processInstanceId + "' and applicanttype='" + applicantType + "')\n"
                        + " and addresstype='P'";
            }

            Log.consoleLog(ifr, "query1==>" + query1);
            Log.consoleLog(ifr, "query2==>" + query2);

            List< List< String>> result1 = ifr.getDataFromDB(query1);
            Log.consoleLog(ifr, "#result1===>" + result1.toString());

            if (!result1.isEmpty()) {
                firstName = result1.get(0).get(0);
                lastName = result1.get(0).get(1);

                if ("{}".equalsIgnoreCase(lastName)
                        || "{".equalsIgnoreCase(lastName)
                        || "}".equalsIgnoreCase(lastName)) {
                    lastName = "";
                }

                PAN = result1.get(0).get(2);
                birthDate = result1.get(0).get(3);
                gender = result1.get(0).get(4);
            }

            if (!birthDate.equalsIgnoreCase("")) {
                DateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date date = sdf.parse(birthDate);
                parsedDate = new SimpleDateFormat("ddMMyyyy").format(date);
                Log.consoleLog(ifr, "ParsedDate======>" + parsedDate);
            }

            if (gender.equalsIgnoreCase("Male")) {
                gendercode = "M";
            } else if (gender.equalsIgnoreCase("Female")) {
                gendercode = "F";
            } else {
                gendercode = "T";
            }

            Log.consoleLog(ifr, "firstName===>" + firstName);
            Log.consoleLog(ifr, "lastName====>" + lastName);
            Log.consoleLog(ifr, "PAN=========>" + PAN);
            Log.consoleLog(ifr, "birthDate===>" + birthDate);
            Log.consoleLog(ifr, "gender======>" + gender);
            Log.consoleLog(ifr, "gendercode==>" + gendercode);

            List< List< String>> result2 = ifr.getDataFromDB(query2);
            Log.consoleLog(ifr, "#result2===>" + result2.toString());

            if (!result2.isEmpty()) {
                addressLine1 = result2.get(0).get(0);
                addressLine2 = result2.get(0).get(1);
                addressLine3 = result2.get(0).get(2);
                city = result2.get(0).get(3);
                state = result2.get(0).get(4);
                pinCode = result2.get(0).get(5);
            }

            Log.consoleLog(ifr, "AddressLine1====>" + addressLine1);
            Log.consoleLog(ifr, "AddressLine2====>" + addressLine2);
            Log.consoleLog(ifr, "AddressLine3====>" + addressLine3);
            Log.consoleLog(ifr, "City============>" + city);
            Log.consoleLog(ifr, "State===========>" + state);
            Log.consoleLog(ifr, "Pincode=========>" + pinCode);

            String enquiryPurpose = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_ENQPURPOSE");//05
            String gstStateCode = pcm.getConstantValue(ifr, "TRANSUNION", "CIR_GSTSTATECODE");//01
            stateCodeNo = pcm.getSelectedStateCodeNo(ifr, state);
            if (stateCodeNo.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }

            request = "{\n"
                    + "    \"name\": {\n"
                    + "        \"firstName\": \"" + firstName + "\",\n"
                    + "        \"lastName\": \"" + lastName + "\",\n"
                    + "        \"middleName\": \"\"\n"
                    + "    },\n"
                    + "    \"gender\": {\n"
                    + "        \"value\": \"" + gendercode + "\"\n"
                    + "    },\n"
                    + "    \"dob\": {\n"
                    + "        \"value\": \"" + parsedDate + "\"\n"
                    + "    },\n"
                    + "    \"pan\": [\n"
                    + "        {\n"
                    + "            \"number\": \"" + PAN + "\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"passport\": [\n"
                    + "        {\n"
                    + "            \"number\": \"\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"voter\": [\n"
                    + "        {\n"
                    + "            \"number\": \"\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"dl\": [\n"
                    + "        {\n"
                    + "            \"number\": \"\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"rationcard\": [\n"
                    + "        {\n"
                    + "            \"number\": \"\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"account\": [\n"
                    + "        {\n"
                    + "            \"number\": \"\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"mobilePhone\": [\n"
                    + "        {\n"
                    + "            \"number\": \"" + mobileNumber + "\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"homePhone\": [\n"
                    + "        {\n"
                    + "            \"number\": \"\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"officePhone\": [\n"
                    + "        {\n"
                    + "            \"number\": \"\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"amount\": " + enquiryAmount + ",\n"
                    + "    \"purpose\": \"" + enquiryPurpose + "\",\n"
                    + "    \"gstStateCode\": \"" + gstStateCode + "\",\n"
                    + "    \"idVerificationFlag\": \"false\",\n"
                    + "    \"fiWaiverFlag\": \"false\",\n"
                    + "    \"residentAddress\": {\n"
                    + "        \"line1\": \"" + addressLine1 + "\",\n"
                    + "        \"line2\": \"" + addressLine2 + "\",\n"
                    + "        \"line3\": \"\",\n"
                    + "        \"line4\": \"\",\n"
                    + "        \"line5\": \"\",\n"
                    + "        \"city\": \"" + city + "\",\n"
                    + "        \"pinCode\": \"" + pinCode + "\",\n"
                    + "        \"stateCode\": \"" + stateCodeNo + "\"\n"
                    + "    }\n"
                    + "}";

            Log.consoleLog(ifr, "Request====>" + request);
            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response===>" + response);

            if (!response.equalsIgnoreCase("{}")) {
            } else {
                response = "No response from the server.";
                apiErrorMessage = "FAIL";
            }

            if (apiErrorMessage.equalsIgnoreCase("")) {
                apiStatus = RLOS_Constants.SUCCESS;
            } else {
                apiStatus = RLOS_Constants.ERROR;
            }
            return "";
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/initiateIDVFraudCheck===>" + e);
        } finally {
            cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response,
                    apiErrorCode, apiErrorMessage, apiStatus);
        }
        return RLOS_Constants.ERROR;
    }

}
