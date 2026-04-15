package com.newgen.iforms.lad;

import com.newgen.dlp.integration.cbs.Advanced360EnquiryData;
import com.newgen.dlp.integration.cbs.Ammortization;
import com.newgen.dlp.integration.cbs.CIM09;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.dlp.integration.cbs.Demographic;
import com.newgen.dlp.integration.cbs.EMICalculator;
import com.newgen.dlp.integration.cbs.LoanAccountCreation;
import com.newgen.dlp.integration.cbs.TDEnquiry;
import com.newgen.dlp.integration.nesl.EsignIntegrationChannel;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormAPIHandler;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.BRMSRules;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Monesh Kumar
 */
public class LADPortalCustomCode {

    CommonFunctionality cf = new CommonFunctionality();
    PortalCommonMethods pcm = new PortalCommonMethods();
    CommonMethods objcm = new CommonMethods();
    String message = "You are Not Eligible! Kindly Contact Branch";

    public String autoPopulateIninitalDataCaptureLAD(IFormReference ifr, String value) {//Corrected
        Log.consoleLog(ifr, "inside autoPopulateInitialDataCapture LAD Data  : ");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String StepQuery = ConfProperty.getQueryScript("PSELECTStepQuery").replaceAll("#winame#", PID);
        List<List<String>> StepList = cf.mExecuteQuery(ifr, StepQuery, "navigationNextLoadLoanDetails Query:");
        String oldWorkstepName = "";
        if (!StepList.isEmpty()) {
            oldWorkstepName = StepList.get(0).get(0);
        }
        if (value.equalsIgnoreCase("Initial Data") && (oldWorkstepName.equalsIgnoreCase("") || oldWorkstepName.equalsIgnoreCase(value))) {
            ifr.clearTable("P_LAD_IDC_FD2");
            ifr.clearTable("P_LAD_IDC_FD1");
            ifr.setValue("P_LAD_IDC_SELECTFACILITY", "");
            ifr.setValue("P_LAD_IDC_SELECTPURPOSE", "");
        }
        pcm.setGetPortalStepName(ifr, value);
        HashMap<String, String> map = new HashMap<>();
        String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        map.put("MobileNumber", pcm.getMobileNumber(ifr));
        onNextClickODADMStatus(ifr);
        try {
            String addressLine1 = "";
            String addressLine2 = "";
            String EMAILID = "";
            Log.consoleLog(ifr, "inside autoPopulateInitialDataCapture LAD Data method");//            String exsCus = ifr.getValue("P_CB_OD_EXISTING_CUSTOMER").toString();
            Log.consoleLog(ifr, "ProcessInsanceId : " + ProcessInsanceId);
            String query = ConfProperty.getQueryScript("PORTALODADEMAIL").replaceAll("#winame#", ProcessInsanceId);
            Log.consoleLog(ifr, "Data Query for table  :" + query);
            List<List<String>> list1 = ifr.getDataFromDB(query);
            if (!list1.isEmpty()) {
                EMAILID = list1.get(0).get(0);
                Log.consoleLog(ifr, "EMAILID :" + EMAILID);
            }
            query = "Select FullName from los_nl_basic_info  where pid = '" + ProcessInsanceId + "'";
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "");
            if (result.size() > 0) {
                String customerName = result.get(0).get(0);
                ifr.setValue("P_PAPL_CUSTOMERNAME1", customerName);
            }
            String QueryP = ConfProperty.getQueryScript("PORTALODADPADDRESS").replaceAll("#winame#", ProcessInsanceId);
            Log.consoleLog(ifr, "Data QueryP for table  :" + QueryP);
            List<List<String>> listP = ifr.getDataFromDB(QueryP);
            if (listP.size() > 0) {
                for (int i = 0; i < listP.size(); i++) {
                    String line1 = listP.get(i).get(0);
                    String line2 = listP.get(i).get(1);
                    String line3 = listP.get(i).get(2);
                    String city = listP.get(i).get(3);
                    String sTATE = listP.get(i).get(4);
                    String PINCODE = listP.get(i).get(6);
                    if (listP.get(i).get(5).equalsIgnoreCase("P")) {
                        addressLine1 = line1 + " " + line2 + " " + line3 + " " + city + " " + sTATE + " " + PINCODE;
                    } else if (listP.get(i).get(5).equalsIgnoreCase("CA")) {
                        addressLine2 = line1 + " " + line2 + " " + line3 + " " + city + " " + sTATE + " " + PINCODE;
                    }
                }
            }
            ifr.setValue("P_LAD_IDC_PERMANENTADDRESS", addressLine1);
            ifr.setValue("P_LAD_IDC_COMMUNICATIONADDRESS", addressLine2);
            ifr.setValue("P_LAD_IDC_EMAIL", EMAILID);
            String getODADMobileDetails = "select MobileNo from los_l_basic_info_i  WHERE  "
                    + " f_key =  (SELECT f_key FROM los_nl_basic_info where PID = '" + ProcessInsanceId + "')";

            //Log.consoleLog(ifr, "inside getODADMobileDetails==> : " + getODADMobileDetails);
            List<List<String>> list2 = cf.mExecuteQuery(ifr, getODADMobileDetails, "getODADMobileDetails:");
            String MobileNo = "";
            Log.consoleLog(ifr, "inside mExecuteQuery getODADMobileDetails: ");
            if (list2.size() > 0) {
                MobileNo = list2.get(0).get(0);
                Log.consoleLog(ifr, "inside MobileNo==> : " + MobileNo);
            }
            ifr.setValue("P_ODAD_InitialMobileNo", MobileNo);
        } catch (Exception e) {
            Log.consoleLog(ifr, "inside exception autoPopulateIninitalDataCaptureLAD" + e);
            Log.consoleLog(ifr, "inside exception autoPopulateIninitalDataCaptureLAD" + e);
        }

        return "";
    }

    public String getFDAccountEnquiry(IFormReference ifr) {//Corrected
        try {
            Log.consoleLog(ifr, "Inside LADPortalCustomCode getFDAccountEnquiry : ");
            TDEnquiry objTDE = new TDEnquiry();
            return objTDE.getFDAccountEnquiryForOD(ifr, pcm.getCustomerIDCB(ifr, "B"));
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in getFDAccountEnquiry " + e);
            Log.errorLog(ifr, "Exception in getFDAccountEnquiry " + e);
            return pcm.returnError(ifr);
        }
    }

    public String onNextClickInitialDataODAD(IFormReference ifr, String SchemeId) {//Corrected
        Log.consoleLog(ifr, "onNextClickInitialDataODAD : ");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String codeMis1 = ifr.getValue("Portal_ODAD_EE_MINORITYSTATUS").toString();
            String codeMis2 = ifr.getValue("Portal_ODAD_EE_CASTE").toString();
            if (!codeMis1.equalsIgnoreCase("") && !codeMis2.equalsIgnoreCase("")) {
                String Response = updateMIS(ifr, codeMis1, codeMis2);
                if (Response.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                    try {
                        List<List<String>> list = cf.mExecuteQuery(ifr, "select miscodedesc from LOS_MST_MISCODE where MISCODE='" + codeMis1 + "' and ISACTIVE='Y'", "GeTMISCODEDETailS:codeMis1");
                        String codeMis1value = list.get(0).get(0);
                        list = cf.mExecuteQuery(ifr, "select miscodedesc from LOS_MST_MISCODE where MISCODE='" + codeMis2 + "' and ISACTIVE='Y'", "GeTMISCODEDETailS:codeMis2 ");
                        String codeMis1value2 = list.get(0).get(0);
                        String miscode = "update los_l_basic_info_i set religion='" + codeMis1value + "', caste='" + codeMis1value2 + "' where f_key = \n"
                                + "(select f_key from los_nl_basic_info where PID='" + PID + "'  and applicanttype='B' and rownum=1)";
                        cf.mExecuteQuery(ifr, miscode, "UpdateMISCODEDETailS ");
                    } catch (Exception e) {
                        Log.errorLog(ifr, "Exception in Updating MIS  in table  " + e);
                        return pcm.returnError(ifr);
                    }
                }
                if (Response.contains(RLOS_Constants.ERROR)) {

                    Log.errorLog(ifr, "Exception in Updating MIS  ");
                    return pcm.returnErrorcustmessage(ifr, Response);
                }
            }
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();

            Log.consoleLog(ifr, "PID : " + PID);
            String InPricipal = ConfProperty.getCommonPropertyValue("ODAD_Initial_Data_Capture_Portal");
            Log.consoleLog(ifr, "InPricipal" + InPricipal);
            String EmpBackoffmapp = ConfProperty.getCommonPropertyValue("ODAD_Initial_Data_Capture_BackOffice");
            Log.consoleLog(ifr, "EmpBackoffmapp" + EmpBackoffmapp);
            String[] Portal_mappings_ED = InPricipal.split(",");
            String[] sttingmapping_EA = EmpBackoffmapp.split(",");
            String[] srt_ED = new String[Portal_mappings_ED.length];
            for (int i = 0; i < Portal_mappings_ED.length; i++) {
                srt_ED[i] = ifr.getValue(Portal_mappings_ED[i]).toString();
            }
            for (int i = 0; i < sttingmapping_EA.length; i++) {
                jsonObject.put(sttingmapping_EA[i], srt_ED[i]);
            }
            jsonObject.put("QNL_LOS_PROPOSED_FACILITY-SchemeId", SchemeId);
            jsonArray.add(jsonObject);
            Log.consoleLog(ifr, "jsonArray1234 :" + jsonArray);
            String IndexQuery = ConfProperty.getQueryScript("ROWINDEXCOUNTINPRINAPPROVAL").replaceAll("#PID#", PID);
            Log.consoleLog(ifr, "populateInPrincipalApprovalCB query " + IndexQuery);
            List<List<String>> docIndexList = ifr.getDataFromDB(IndexQuery);
            Log.consoleLog(ifr, "populateInPrincipalApprovalCB docIndexList " + docIndexList);
            if (docIndexList.isEmpty()) {
                Log.consoleLog(ifr, "populateInPrincipalApprovalCB Info :");
                ((IFormAPIHandler) ifr).addDataToGrid("QNL_LOS_PROPOSED_FACILITY", jsonArray, true);
            } else {
                String query = "update LOS_NL_PROPOSED_FACILITY set LOANPURPOSE='" + ifr.getValue("P_LAD_IDC_SELECTPURPOSE")
                        + "',FACILITYTYPE='" + ifr.getValue("P_LAD_IDC_SELECTFACILITY") + "',SCHEMEID='" + SchemeId
                        + "' where pid='" + PID + "'";
                Log.consoleLog(ifr, "query:" + query);
                ifr.saveDataInDB(query);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in onNextClickInitialDataODAD");
        }
        return "";
    }

    public String onNextClickODADMStatus(IFormReference ifr) {//Corrected
        Log.consoleLog(ifr, "onNextClickODADMStatus : ");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {
            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

            Demographic objDemographic = new Demographic();
            String GetDemoGraphicData = objDemographic.getDemographic(ifr, PID, CustomerId);
            Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
            if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside error condition Demographic LAD");
                return pcm.returnErrorAPIThroughExecute(ifr);
            } else {
                Log.consoleLog(ifr, "inside non-error condition Demographic LAD");
                JSONParser jsonparser = new JSONParser();
                JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
                Log.consoleLog(ifr, obj.toString());
                String CustMisStatus = obj.get("CustMisStatus").toString();
                //  CustMisStatus="Incomplete";
                Log.consoleLog(ifr, "CustMisStatus Value" + CustMisStatus);
                if (CustMisStatus.equalsIgnoreCase("Incomplete")) {
                    ifr.setStyle("Portal_ODAD_EE_MINORITYSTATUS", "visible", "true");
                    ifr.setStyle("Portal_ODAD_EE_CASTE", "visible", "true");
                    String codMisCustCode1 = ifr.getValue("Portal_ODAD_EE_MINORITYSTATUS").toString();
                    String codMisCustCode2 = ifr.getValue("Portal_ODAD_EE_CASTE").toString();
                    if (codMisCustCode1.equalsIgnoreCase("")) {
                        return pcm.returnErrorThroughExecute(ifr, "Please Select Minority Status!");
                    }
                    if (codMisCustCode2.equalsIgnoreCase("")) {
                        return pcm.returnErrorThroughExecute(ifr, "Please Select Caste!");
                    }

                } else {
                    ifr.setStyle("Portal_ODAD_EE_MINORITYSTATUS", "visible", "false");
                    ifr.setStyle("Portal_ODAD_EE_CASTE", "visible", "false");
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in onNextClickODADMStatus");
        }
        return "";
    }

    public String updateMIS(IFormReference ifr, String codMisCustCode1, String codMisCustCode2) {
        String CustomerId = pcm.getCustomerIDCB(ifr, "B");
        String codMisCustCode3 = "";
        String codMisCustCode4 = "";
        String codMisCustCode5 = "";
        String codMisCustCode6 = "";
        String codMisCustCode7 = "";
        String codMisCustCode8 = "";
        String codMisCustCode9 = "";
        String codMisCustCode10 = "";
        String query = ConfProperty.getQueryScript("PORTALLADMISCODE");
        List<List<String>> result = cf.mExecuteQuery(ifr, query, "MISCODE:");
        if (result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                if (result.get(i).get(0).equalsIgnoreCase("CUST-TYPE")) {
                    codMisCustCode3 = result.get(i).get(1);
                } else if (result.get(i).get(0).equalsIgnoreCase("CUST-STAT")) {
                    codMisCustCode4 = result.get(i).get(1);
                } else if (result.get(i).get(0).equalsIgnoreCase("CUST-CATEGORY")) {
                    codMisCustCode5 = result.get(i).get(1);
                } else if (result.get(i).get(0).equalsIgnoreCase("CUST-SPECIAL-CATEGORY")) {
                    codMisCustCode6 = result.get(i).get(1);
                } else if (result.get(i).get(0).equalsIgnoreCase("WEAKER SECTOR")) {
                    codMisCustCode7 = result.get(i).get(1);
                } else if (result.get(i).get(0).equalsIgnoreCase("CUSTOMER PREFERRED LANGUAGE")) {
                    codMisCustCode8 = result.get(i).get(1);
                } else if (result.get(i).get(0).equalsIgnoreCase("INTERNAL RATING DETAILS")) {
                    codMisCustCode9 = result.get(i).get(1);
                } else if (result.get(i).get(0).equalsIgnoreCase("BSR CODE")) {
                    codMisCustCode10 = result.get(i).get(1);
                }
            }
        } else {
            return pcm.returnErrorAPIThroughExecute(ifr);
        }
        CIM09 cim09 = new CIM09();
        String response = cim09.updateCustMisValue(ifr, CustomerId,
                codMisCustCode1, codMisCustCode2, codMisCustCode3, codMisCustCode4, codMisCustCode5,
                codMisCustCode6, codMisCustCode7, codMisCustCode8, codMisCustCode9, codMisCustCode10);
        Log.consoleLog(ifr, "response==>" + response);
        if (response.contains(RLOS_Constants.ERROR)) {
            Log.consoleLog(ifr, "inside error condition CustMisStatus LAD");
            //  return pcm.returnErrorAPIThroughExecute(ifr);
            return RLOS_Constants.ERROR;
        } else {
            return RLOS_Constants.SUCCESS;
        }
        // return "";
    }

    public String mImpOnClickCheckBRMSRules(IFormReference ifr) {//Corrected
        Log.consoleLog(ifr, "Entered into mImpOnClickValidation:::");
        try {
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);
            String mobileno = pcm.getCurrentWiMobileNumber(ifr);
            Log.consoleLog(ifr, "mobileNo" + CustomerId);
            HashMap<String, String> map = new HashMap<>();
            map.put("MobileNumber", mobileno);
            map.put("CustomerId", CustomerId);
            String NRI = "";
            String Classification = "";
            String Age = "";
            String CustomerCategory = "";
            String LienAmount = "";
            String ProductCode = "OD";
            String OnlineAccount = "YES";
            String staffCheck = "NA";
            Log.consoleLog(ifr, "Calling cbs api at mImpOnClickValidation ");
            CustomerAccountSummary objCustSummary = new CustomerAccountSummary();
            String response = objCustSummary.getCustomerAccountSummary(ifr, map);
            Log.consoleLog(ifr, "response==>" + response);
            if (response.contains(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside error condition of cbs api LAD");
                return pcm.returnErrorAPIThroughExecute(ifr);
            } else {
                Log.consoleLog(ifr, "inside non-error condition cbs LAD");
                JSONParser jp = new JSONParser();
                JSONObject obj = (JSONObject) jp.parse(response);
                NRI = obj.get("NRI").toString();
                Log.consoleLog(ifr, "NRI value:" + NRI);
            }
            Log.consoleLog(ifr, "calling 360 api");
            Advanced360EnquiryData objCbs360 = new Advanced360EnquiryData();
            String response360 = objCbs360.executeAdvanced360Inquiry(ifr, ProcessInstanceId, CustomerId, "LAD");
            Log.consoleLog(ifr, "response==>" + response360);
            if (response360.contains(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside error condition 360API LAD");
                return pcm.returnErrorAPIThroughExecute(ifr);
            } else {
                Log.consoleLog(ifr, "inside non-error condition 360API LAD");
                JSONParser jsonparser = new JSONParser();
                JSONObject obj = (JSONObject) jsonparser.parse(response360);
                Log.consoleLog(ifr, obj.toString());
                Classification = obj.get("ClassifactionValue").toString();
                Log.consoleLog(ifr, "Classification Value" + Classification);
            }
            Log.consoleLog(ifr, "calling Demographic api");
            Demographic objDemographic = new Demographic();
            String GetDemoGraphicData = objDemographic.getDemographic(ifr, ProcessInstanceId, CustomerId);
            Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
            if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside error condition Demographic LAD");
                return pcm.returnErrorAPIThroughExecute(ifr);
            } else {
                Log.consoleLog(ifr, "inside non-error condition Demographic LAD");
                JSONParser jsonparser = new JSONParser();
                JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
                Log.consoleLog(ifr, obj.toString());
                Age = obj.get("Age").toString();
                Log.consoleLog(ifr, "Age Value" + Age);
                CustomerCategory = obj.get("CustomerCategory").toString();
                Log.consoleLog(ifr, "CustomerCategory Value" + CustomerCategory);
            }
            LienAmount = "0";
            String knockoffInParams = Classification + "," + NRI + "," + Age + "," + ProductCode + ","
                    + CustomerCategory + "," + LienAmount + "," + OnlineAccount + "," + staffCheck + "";
            Log.consoleLog(ifr, "KnockoffInParams " + knockoffInParams);
            String knockoffDecision = checkKnockOff(ifr, "Knock_of_LAD_Rule", knockoffInParams, "total_knockoff_lad_op");
            Log.consoleLog(ifr, "KnockoffDecision fetched" + knockoffDecision);
            Log.consoleLog(ifr, "KnockoffDecision case check" + knockoffDecision.equalsIgnoreCase("Proceed"));
            if (knockoffDecision.toUpperCase().equalsIgnoreCase("PROCEED")) {
                return "";
            } else {

                //Added by Sravani on 05-06-2024
                String upQuery = "UPDATE LOS_WIREFERENCE_TABLE SET APPLICATION_STATUS='REJECTED' "
                        + "WHERE WINAME='" + ProcessInstanceId + "'";
                Log.consoleLog(ifr, "upQuery:" + upQuery);
                ifr.saveDataInDB(upQuery);

                Log.consoleLog(ifr, "knockoffDecision fail" + knockoffDecision);
                return pcm.returnErrorThroughExecute(ifr, message);
            }
        } catch (ParseException e) {
            Log.consoleLog(ifr, "Inside mImpOnClickValidation " + e);
            Log.errorLog(ifr, "Inside mImpOnClickValidation " + e);
            return pcm.returnErrorAPIThroughExecute(ifr);
        }
    }

    public String checkKnockOff(IFormReference ifr, String RuleName, String values, String ValueTag) {//corrected
        try {
            BRMSRules jsonBRMSCall = new BRMSRules();
            JSONObject result = jsonBRMSCall.executeLOSBRMSRule(ifr, RuleName, values, ValueTag);
            Log.consoleLog(ifr, "The Json Object Received in checkKnockOff " + result);
            Log.consoleLog(ifr, "The tag =>" + ValueTag);

            //Added by Ahmed on on 05-06-2024 for BRMS Rule Issue Fixing on LAD           
            if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                String cibilOutput = cf.getJsonValue(result, ValueTag);
                Log.consoleLog(ifr, cibilOutput);
                return cibilOutput;
            } else {
                Log.consoleLog(ifr, "Error:" + AcceleratorConstants.TRYCATCHERRORBRMS);
                return "ERROR";
            }

//Commented by Ahmed  on 05-06-2024 for BRMS Rule Issue on LAD           
//            if (cf.getJsonValue(result, "Status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
//                String Output = result.get("Output").toString();
//                Log.consoleLog(ifr, "Output==>" + Output);
//                JSONParser parser = new JSONParser();
//                JSONObject OutputJSON = (JSONObject) parser.parse(Output);
//                String totalknockoffLad = OutputJSON.get("total_knockoff_lad").toString();
//                JSONObject totalknockoffLadJson = (JSONObject) parser.parse(totalknockoffLad);
//                Log.consoleLog(ifr, " The totalknockoffLadJson==>" + totalknockoffLadJson);
//                String totalKnockOffLadOp = cf.getJsonValue(totalknockoffLadJson, ValueTag);
//                Log.consoleLog(ifr, " The total_knockoff_lad_op ==>" + totalKnockOffLadOp);
//                return totalKnockOffLadOp;
//            } else {
//                Log.consoleLog(ifr, "Error:" + AcceleratorConstants.TRYCATCHERRORBRMS);
//                return "ERROR";
//            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception occured" + e);
        }
        return "";
    }

    public String getMonthDifference(IFormReference ifr, String givenDateString) {//Corrected
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate currentDate = LocalDate.now();
        String result;
        LocalDate givenDate = LocalDate.parse(givenDateString, formatter);
        Log.consoleLog(ifr, "The  given  date." + givenDate);
        if (givenDate.isAfter(currentDate)) {
            Log.consoleLog(ifr, "The given date is greater than the current date.");
            Period period = Period.between(givenDate, currentDate);
            long monthsDifference = Math.abs(period.toTotalMonths());
            result = String.valueOf(monthsDifference);
            Log.consoleLog(ifr, "Input Date: " + result);
        } else if (givenDate.isBefore(currentDate)) {
            Period period = Period.between(currentDate, givenDate);
            long monthsDifference = Math.abs(period.toTotalMonths());
            result = String.valueOf(monthsDifference);
            Log.consoleLog(ifr, "The given date is less than the current date." + result);
            result = "0";
        } else {
            result = "0";
            Log.consoleLog(ifr, "The given date is equal to the current date." + result);
        }
        return result;
    }

    public String mUserSeletedFD(IFormReference ifr, String control, String event, String value) {//corrected
        Log.consoleLog(ifr, "inside mUserSeletedFD");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {
            ifr.clearTable("P_LAD_IDC_FD2");
            String arrayValue[] = value.split("~");
            String indexValue1 = arrayValue[0];
            String selectedFDNumber = ifr.getTableCellValue("P_LAD_IDC_FD1", Integer.parseInt(indexValue1), 0);
            String selectedDepositeNo = ifr.getTableCellValue("P_LAD_IDC_FD1", Integer.parseInt(indexValue1), 1);
            String Query = "SELECT distinct FDNO,DEPOSITAMOUNT,ELIGIBLEAMOUNT,DEPOSITSTARTDATE,DEPOSITMATURITYDATE,"
                    + "DEPOSITRATE,depositnumber,TDROI from LOS_TRN_LAD_ELIGIBLE WHERE WI_NAME IN('" + PID + "') "
                    + "AND FDNO ='" + selectedFDNumber + "' " + "AND DEPOSITNUMBER = '" + selectedDepositeNo + "'";
            Log.consoleLog(ifr, "Query==>" + Query);
            List<List<String>> list = ifr.getDataFromDB(Query);
            if (list.size() > 0) {
                String fdNumber = list.get(0).get(0);
                String depositAmount = list.get(0).get(1);
                String eligibleAmount = list.get(0).get(2);
                String startDate = list.get(0).get(3);
                String maturityDate = list.get(0).get(4);
                String roi = list.get(0).get(5);
                String TDROI = list.get(0).get(7);
                String depositNumber = list.get(0).get(6);

                int num;
                try {
                    int indexValue = Integer.parseInt(indexValue1);
                    num = indexValue + 1;
                } catch (NumberFormatException e) {
                    Log.consoleLog(ifr, "Error: indexValue1 is not a valid integer.");
                    num = 0; // Set a default value
                }
                Log.consoleLog(ifr, "num : " + num);
                double checkEligibleAmount = Double.parseDouble(eligibleAmount);
                Log.consoleLog(ifr, "checkEligibleAmount : " + checkEligibleAmount);
                if (checkEligibleAmount < 5000) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, control, "error", "Selected FD is not eligible for Loan because eligible amount is less then 5000."));
                    String checkBoxCtrlID = control + "_" + String.valueOf(num).trim();
                    Log.consoleLog(ifr, "checkBoxCtrlID : " + checkBoxCtrlID);
                    message.put("ctrlID", checkBoxCtrlID);
                    return message.toString();
                }

                String getMonthDifference = getMonthDifference(ifr, maturityDate);
                if (getMonthDifference.equalsIgnoreCase("0")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, control, "error", "Selected FD is not eligible for loan because maturity date is less/equal than the current date."));
                    String checkBoxCtrlID = control + "_" + String.valueOf(num).trim();
                    Log.consoleLog(ifr, "checkBoxCtrlID : " + checkBoxCtrlID);
                    message.put("ctrlID", checkBoxCtrlID);
                    return message.toString();
                }
                JSONObject obj = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                obj.put("Term Deposit No", fdNumber);
                obj.put("Deposit Number", depositNumber);
                obj.put("Deposit Amount", depositAmount);
                obj.put("Eligible Amount", eligibleAmount);
                obj.put("Start Date", startDate);
                obj.put("Maturity Date", maturityDate);
                obj.put("Rate of Interest", TDROI);
                obj.put("OD Rate of Interest", roi);
                obj.put("Tenure", getMonthDifference);
                jsonArray.add(obj);
                ifr.addDataToGrid("P_LAD_IDC_FD2", jsonArray);
            }
        } catch (NumberFormatException e) {
            Log.consoleLog(ifr, "Exception/mUserSeletedFD" + e);
            Log.errorLog(ifr, "Exception/mUserSeletedFD" + e);
        }
        return "";
    }

    public String populateDataFinalEligibilityLAD(IFormReference ifr, String control, String event, String value) {//corrected
        Log.consoleLog(ifr, "inside populateDataFinalEligibilityLAD:");
        try {
            onNextClickODADMStatus(ifr);
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            pcm.setGetPortalStepName(ifr, value);
            String sliderLoanAmount;
            String setSliderValue;
            String queryCheckBox = ConfProperty.getQueryScript("PORTALCHECKBOXEE").replaceAll("#WINAME#", PID);
            Log.consoleLog(ifr, "queryCheckBox : " + queryCheckBox);
            List<List<String>> listbox = ifr.getDataFromDB(queryCheckBox);
            if (!listbox.isEmpty()) {
                String checkBox = listbox.get(0).get(0);
                if (checkBox.equalsIgnoreCase("true")) {
                    ifr.setValue("P_ODAD_FE_ADHAAR", "true");
                    ifr.setValue("P_ODAD_FE_LOAN_SIGN", "true");
                }
            }
            String tenure = "";
            String Queryfor = ConfProperty.getQueryScript("PORTALODADRATE").replaceAll("#winame#", PID);
            List<List<String>> list2 = cf.mExecuteQuery(ifr, Queryfor, "PORTALODADRATE:");
            if (!list2.isEmpty()) {
                String rateOfInterest = list2.get(0).get(0);
                tenure = list2.get(0).get(2);
                ifr.setValue("Portal_ODAD_EE_Tenure", tenure);
                ifr.setValue("Portal_ODAD_EE_ROI", rateOfInterest);
            }
            int tenurecheck = Integer.parseInt(tenure);
            String loanFacility = getFacilityType(ifr);
            if (loanFacility.equalsIgnoreCase("OD")) {
                if (tenurecheck > 12) {
                    ifr.setStyle("P_ODAD_Renewal", "visible", "true");
                } else {
                    ifr.setStyle("P_ODAD_Renewal", "visible", "false");
                }
            }

            String query1 = ConfProperty.getQueryScript("PORTALFINDSLIDERVALUELAD").replaceAll("#WINAME#", PID);
            Log.consoleLog(ifr, " query1 : " + query1);
            List<List<String>> list1 = ifr.getDataFromDB(query1);
            Log.consoleLog(ifr, "list1 : " + list1);
            if (!list1.isEmpty()) {
                sliderLoanAmount = list1.get(0).get(0);
                Log.consoleLog(ifr, "sliderLoanAmount " + sliderLoanAmount);
                double doubleValue = Double.parseDouble(sliderLoanAmount);
                long roundedValue = (long) (double) Math.round(doubleValue / 100) * 100;
                String roundedString = String.valueOf(roundedValue);
                Log.consoleLog(ifr, "roundedString : " + roundedString);
                ifr.setValue("Portal_ODAD_EE_LoanAmount", roundedString);
                setSliderValue = roundedString;
                return setSliderValue;
            } else {
                String delfValue = "1000000";
                ifr.setValue("Portal_ODAD_EE_LoanAmount", delfValue);
                setSliderValue = delfValue;
                return setSliderValue;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in autoPopulateRecieveMoneyData " + e);
            Log.errorLog(ifr, "Error occured in autoPopulateRecieveMoneyData " + e);
        }
        return "";
    }

    public String onClickNextFinalEligibleityOD(IFormReference ifr, String frameName) {//corrected
        Log.consoleLog(ifr, "inside onClickNextFinalEligibleityOD:");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            // String checkbox1 = ifr.getValue("P_ODAD_FE_ADHAAR").toString();
            //   String checkbox2 = ifr.getValue("P_ODAD_FE_LOAN_SIGN").toString();

            int tenureValue = Integer.parseInt(ifr.getValue("Portal_ODAD_EE_Tenure").toString());
            Log.consoleLog(ifr, "tenureValue:" + tenureValue);
            if (tenureValue > 12) {
                String loanFacility = getFacilityType(ifr);
                if (loanFacility.equalsIgnoreCase("OD")) {
                    if (ifr.getValue("P_ODAD_Renewal").toString().equalsIgnoreCase("")) {
                        return pcm.returnErrorThroughExecute(ifr, "Kindly Select Renewal!");
                    } else {
                        String query = ConfProperty.getQueryScript("PORTALLADRENEWALUPDATE").replaceAll("#WI_NAME#", PID);
                        Log.consoleLog(ifr, query);
                        ifr.saveDataInDB(query);
                    }
                }
                ifr.setValue("Portal_ODAD_EE_Tenure", "12");
            }

            String loanFacility = getFacilityType(ifr);
            Log.consoleLog(ifr, "loanFacility :" + loanFacility);
            String EMIAmount = "0";
            String loanAmount = ifr.getValue("Portal_ODAD_EE_LoanAmount").toString();
            String tenure = ifr.getValue("Portal_ODAD_EE_Tenure").toString();
            String roi = ifr.getValue("Portal_ODAD_EE_ROI").toString();

            if (loanFacility.equalsIgnoreCase("TL")) {

                String FrameSection = "FinalOffer";
                EMICalculator e = new EMICalculator();
                EMIAmount = e.getEmiCalculatorInstallment(ifr, PID, loanAmount, tenure, roi, FrameSection);

                // CBS_EMICalculator e = new CBS_EMICalculator();
                // EMIAmount = e.emiCalculatorInstallmentAPI(ifr, loanAmount, tenure, roi, frameName, "ODAD");
                EMIAmount = pcm.mAccRoundOffvalue(ifr, EMIAmount);
                Log.consoleLog(ifr, "EMIAmount : " + EMIAmount);

                Ammortization AMR = new Ammortization();
                String returnMessage = AMR.ExecuteCBS_Ammortization(ifr, PID, loanAmount, tenure, "LAD");
                Log.consoleLog(ifr, "returnMessage from Ammortization :" + returnMessage);
                if (returnMessage.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnErrorAPIThroughExecute(ifr);
                }
            }

            mSaveFinalEligibilityData(ifr, loanAmount, roi, tenure, EMIAmount, "");
            mGenerateDoc(ifr);
            EsignIntegrationChannel NESL = new EsignIntegrationChannel();
//            String returnMessage = NESL.redirectNESLRequest(ifr, loanFacility, "eSigning");
//            Log.consoleLog(ifr, "returnMessage from NESL :" + returnMessage);
//            if ((returnMessage.contains(RLOS_Constants.ERROR)) || (returnMessage.equalsIgnoreCase(""))
//                    || returnMessage.contains("showMessage")) {
//                return pcm.returnErrorAPIThroughExecute(ifr);
//            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception onClickNextFinalEligibleityOD : " + e);
            Log.errorLog(ifr, "Exception onClickNextFinalEligibleityOD : " + e);
            return pcm.returnErrorAPIThroughExecute(ifr);
        }
        return "";
    }

    public void mSaveFinalEligibilityData(IFormReference ifr, String loanAmount, String roi, String tenure,
            String EMIAmount, String SB_ACCOUNTNO) {//corrected
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String checkQuery = ConfProperty.getQueryScript("POATALLADRMDATACHECK").replaceAll("#WINAME#", PID);
        List<List<String>> checkList = cf.mExecuteQuery(ifr, checkQuery, "POATALLADRMDATACHECK Query:");
        if (checkList.size() <= 0) {
            String insertQuery = ConfProperty.getQueryScript("POATALLADRMDATAINSERT").replaceAll("#WINAME#", PID)
                    .replaceAll("#LOANAMOUNT#", loanAmount).replaceAll("#TENURE#", tenure).
                    replaceAll("#RATEOFINTEREST#", roi).replaceAll("#EMI#", EMIAmount).
                    replaceAll("#PROCESSINGFEE#", "").replaceAll("#SB_ACCOUNTNO#", SB_ACCOUNTNO).replaceAll("#RP_ACCOUNTNO#", SB_ACCOUNTNO);
            Log.consoleLog(ifr, "LADPortalCustomCode:autoPopulateRecieveMoneyDataLad->insertQuery: " + ConfProperty.getQueryScript("POATALLADRMDATAINSERT"));
            ifr.saveDataInDB(insertQuery);
        } else {
            String updateQuery = ConfProperty.getQueryScript("POATALLADRMDATAUPDATE").replaceAll("#WINAME#", PID).
                    replaceAll("#LOANAMOUNT#", loanAmount).replaceAll("#TENURE#", tenure).
                    replaceAll("#RATEOFINTEREST#", roi).replaceAll("#EMI#", EMIAmount).replaceAll("#PROCESSINGFEE#", "").
                    replaceAll("#SB_ACCOUNTNO#", SB_ACCOUNTNO).replaceAll("#RP_ACCOUNTNO#", SB_ACCOUNTNO);
            Log.consoleLog(ifr, "LADPortalCustomCode:autoPopulateRecieveMoneyDataLad->UpdateQuery : " + updateQuery);
            ifr.saveDataInDB(updateQuery);
        }
    }

    public void mGenerateDoc(IFormReference ifr) {//corrected
        String loanFacility = getFacilityType(ifr);
        Log.consoleLog(ifr, "LADPortalCustomCode->mGenerateDoc : for OD " + loanFacility);
        if (loanFacility.equalsIgnoreCase("OD")) {
            Log.consoleLog(ifr, "LADPortalCustomCode->mGenerateDoc :" + loanFacility);
            objcm.generatedoc(ifr, "KFS", "LAD");
            objcm.generatedoc(ifr, "NF_969", "LAD");
            objcm.generatedoc(ifr, "OD_REQLETTER", "LAD");
            objcm.generatedoc(ifr, "SanctionMemorandum", "LAD");
            objcm.generatedoc(ifr, "ProcessNote", "LAD");
        } else if (loanFacility.equalsIgnoreCase("TL")) {
            Log.consoleLog(ifr, "LADPortalCustomCode->mGenerateDoc : for TL " + loanFacility);
            objcm.generatedoc(ifr, "KFS_TL", "LAD");
            objcm.generatedoc(ifr, "NF_969", "LAD");
            objcm.generatedoc(ifr, "SanctionMemorandum_TL", "LAD");
            objcm.generatedoc(ifr, "ProcessNote_TL", "LAD");
        }
    }

    public String autoPopulateRecieveMoneyDataLad(IFormReference ifr, String control, String event, String value) {//corrected
        try {
            Log.consoleLog(ifr, "inside autoPopulateRecieveMoneyDataLad : ");
            pcm.setGetPortalStepName(ifr, value);
            String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "pid : " + pid);

            String CustomerId = pcm.getCustomerIDCB(ifr, "B");
            Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

            Advanced360EnquiryData objCbs360 = new Advanced360EnquiryData();
            String response360 = objCbs360.executeAdvanced360Inquiry(ifr, pid, CustomerId, "LAD");
            Log.consoleLog(ifr, "response==>" + response360);
            if (response360.contains(RLOS_Constants.ERROR)) {
                Log.consoleLog(ifr, "inside error condition 360API LAD");
                return pcm.returnError(ifr);
            } else {
                Log.consoleLog(ifr, "inside non-error condition 360API LAD");
                JSONParser jsonparser = new JSONParser();
                JSONObject obj = (JSONObject) jsonparser.parse(response360);
                Log.consoleLog(ifr, obj.toString());
                String sbAccountDetails = obj.get("AccountDetails").toString();
                Log.consoleLog(ifr, "sbAccountDetails Value" + sbAccountDetails);

                JSONParser parser = new JSONParser();
                JSONArray accountDetailsJSON = (JSONArray) parser.parse(sbAccountDetails);
                if (!accountDetailsJSON.isEmpty()) {
                    Log.consoleLog(ifr, "inside accountDetailsJSON");
                    for (int i = 0; i < accountDetailsJSON.size(); i++) {
                        String InputString = accountDetailsJSON.get(i).toString();
                        JSONObject InputStringResponseJSON = (JSONObject) parser.parse(InputString);
                        String AccountId = InputStringResponseJSON.get("AccountId").toString().trim();
                        String BranchCode = InputStringResponseJSON.get("BranchCode").toString();
                        String strAcctOpen = InputStringResponseJSON.get("DatAcctOpen").toString();
                        String strAcctbal = InputStringResponseJSON.get("AcyAmount").toString();
                        ifr.addItemInCombo("P_ODAD_SBACCOUNTNO", AccountId, AccountId + "-" + BranchCode + "-" + strAcctOpen + "-" + strAcctbal);
                    }
                }
            }

            String query = ConfProperty.getQueryScript("PORTALLADFINALELIGIBILITYDETAILS").replaceAll("#WINAME#", pid);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "PORTALLADFINALELIGIBILITYDETAILS:");
            if (result.size() > 0) {
                ifr.setValue("P_ODAD_RM_ROI", result.get(0).get(5));
                ifr.setValue("P_ODAD_NETDISBURSEMENTNO", result.get(0).get(0));
                ifr.setValue("P_ODAD_RM_TENURE", result.get(0).get(4));
                ifr.setValue("P_ODAD_RM_LOANAMOUNT", result.get(0).get(0));
                //ifr.setValue("P_ODAD_SBACCOUNTNO", result.get(0).get(2));
                //ifr.setValue("P_ODAD_REPAYMENTNO", result.get(0).get(3));
            }

        } catch (ParseException e) {
            Log.consoleLog(ifr, "Error occured in autoPopulateRecieveMoneyData " + e);
            Log.errorLog(ifr, "Error occured in autoPopulateRecieveMoneyData " + e);
            return pcm.returnError(ifr);
        }
        return "";
    }

    public String getLoanAccountCreation(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "Inside LADPortalCustomCode getLoanAccountCreation : ");
            String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String Productcode = "";
            String depositNo = "";
            String TDAccountNo = "";
            String maturityDate = "";
            String fdno = "select fdno,depositnumber,depositmaturitydate from los_trn_lad_loanopted where wi_name='" + ProcessInsanceId + "'";
            List<List<String>> list = cf.mExecuteQuery(ifr, fdno, "fdno:");
            if (!list.isEmpty()) {
                TDAccountNo = list.get(0).get(0);
                depositNo = list.get(0).get(1);
                SimpleDateFormat inputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                Date parsedDate = inputDateFormat.parse(list.get(0).get(2));
                maturityDate = dateFormat.format(parsedDate);;
            } else {
                Log.consoleLog(ifr, "Data Not Found!");
                return RLOS_Constants.ERROR;
            }
            String response = mImpOnClickCheckBRMSRules(ifr);
            if (response.contains(message)) {
                return message;
            } else if (!(response.equalsIgnoreCase(""))) {
                return RLOS_Constants.ERROR;
            }
            // ifr.addItemInCombo("P_ODAD_SBACCOUNTNO", "110050700999", "110050700999-1541");
            // ifr.setValue("P_ODAD_SBACCOUNTNO", "110050700999-1541");
            String Customerid = pcm.getCustomerIDCB(ifr, "B");
            String LoanAmount = ifr.getValue("P_ODAD_RM_LOANAMOUNT").toString();
            String Tenure = ifr.getValue("P_ODAD_RM_TENURE").toString();
            String linkedCasaAccountNo = ifr.getValue("P_ODAD_SBACCOUNTNO").toString().split("-")[0];
            Log.consoleLog(ifr, "linkedCasaAccountNo:" + linkedCasaAccountNo);

            //Commented by Ahmed for passing FDAccountNumbers`s BranchCode
//            String linkedCasaAccountNoBranch = ifr.getValue("P_ODAD_SBACCOUNTNO").toString().split("-")[1];
//            Log.consoleLog(ifr, "linkedCasaAccountNoBranch:" + linkedCasaAccountNoBranch);
            String selectedFDAccountBranchCode = getSelectedFDAccountBranchCode(ifr, TDAccountNo);
            Log.consoleLog(ifr, "selectedFDAccountBranchCode:" + selectedFDAccountBranchCode);
            if (selectedFDAccountBranchCode.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }

            Log.consoleLog(ifr, "Value to the apis  :getLoanAccountCreation " + ProcessInsanceId
                    + Customerid + LoanAmount + TDAccountNo + maturityDate + linkedCasaAccountNo);
            LoanAccountCreation objLoanCret = new LoanAccountCreation();

            String loanFacility = getFacilityType(ifr);
            Log.consoleLog(ifr, "loanFacility :" + loanFacility);
            String loanAccount = "";
            String finalTrnData = ConfProperty.getQueryScript("PORATLODADFINAL").replaceAll("#WINAME#", ProcessInsanceId);
            List<List<String>> finalTrn = cf.mExecuteQuery(ifr, finalTrnData, "PORATLODADFINAL Query:");
            if (finalTrn.size() > 0) {
                loanAccount = finalTrn.get(0).get(0);
            } else {
                if (loanFacility.equalsIgnoreCase("OD")) {
                    loanAccount = objLoanCret.createLADODLoanAccount(ifr, ProcessInsanceId, Productcode, Customerid,
                            LoanAmount, TDAccountNo, maturityDate, linkedCasaAccountNo, depositNo, selectedFDAccountBranchCode);
                } else if (loanFacility.equalsIgnoreCase("TL")) {
                    loanAccount = objLoanCret.createLADTLLoanAccount(ifr, ProcessInsanceId, Productcode, Customerid,
                            LoanAmount, Tenure, linkedCasaAccountNo, TDAccountNo, depositNo, selectedFDAccountBranchCode);
                } else {
                    Log.consoleLog(ifr, "getLoanAccountCreation ");
                    return RLOS_Constants.ERROR;
                }
            }
            if (loanAccount.equalsIgnoreCase("") || loanAccount.equalsIgnoreCase("null")
                    || loanAccount.contains(RLOS_Constants.ERROR)) {
                return loanAccount;
            }

            //Commented by Ahmed as per Naveen`s mail on 20-02-2024. 
//            CBS_FundTransfer CB8 = new CBS_FundTransfer();
//            String CBS_FundTransfer = CB8.CBS_FundTransfer(ifr, linkedCasaAccountNo + "-" + linkedCasaAccountNoBranch,
//                    "ODAD", LoanAmount, "", Tenure);
//            Log.consoleLog(ifr, "CBS_FundTransfer:" + CBS_FundTransfer);
//            if (CBS_FundTransfer.contains(RLOS_Constants.ERROR)) {
//                return RLOS_Constants.ERROR;
//            }
//            
            Log.consoleLog(ifr, "loanAccount value   :getLoanAccountCreation " + loanAccount);
            return loanAccount;
        } catch (java.text.ParseException e) {
            Log.consoleLog(ifr, "Exception in getLoanAccountCreation " + e);
            Log.errorLog(ifr, "Exception in getLoanAccountCreation " + e);
        }
        return RLOS_Constants.ERROR;
    }

    public void autoPopulateDataFinalODAD(IFormReference ifr, String control, String event, String value) {

        Log.consoleLog(ifr, "inside autoPopulateDataFinalODAD : ");
        String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        try {
            pcm.setGetPortalStepName(ifr, value);
            String winame = ifr.getObjGeneralData().getM_strProcessInstanceId();

            // Added by Shravani for future meturity date 19-02-2024
            String MaturityDate = "";
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            String formattedDate = currentDate.format(formatter);
            System.out.println("Current Date: " + formattedDate);
            Log.consoleLog(ifr, " : ");
            int monthsToAdd = 0;
            Log.consoleLog(ifr, "inside monthsToAdd : ");
            if (ifr.getValue("P_ODAD_RM_TENURE") != null && ifr.getValue("P_ODAD_RM_TENURE") != "") {
                Log.consoleLog(ifr, "inside if condition monthsToAdd : ");
            }
            {
                monthsToAdd = Integer.parseInt(ifr.getValue("P_ODAD_RM_TENURE").toString());
                Log.consoleLog(ifr, "monthsToAdd==> : " + monthsToAdd);
            }
            LocalDate futureDate = currentDate.plusMonths(monthsToAdd);
            String formattedFutureDate = futureDate.format(formatter);
            Log.consoleLog(ifr, "formattedFutureDate==>  : " + formattedFutureDate);
            ifr.setValue("P_ODAD_MaturityDate", formattedFutureDate);

            Log.consoleLog(ifr, "winame : " + winame);
            String query = ConfProperty.getQueryScript("PORATLODADFINAL").replaceAll("#WINAME#", winame);
            String loanAcc = "";
            String disbusedate = "";
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "PORATLODADFINAL:");
            if (result.size() > 0) {
                loanAcc = result.get(0).get(0);
                disbusedate = result.get(0).get(1);
                ifr.setValue("Portal_ODAD_Disbursement_L_AccountNumber", loanAcc);
                ifr.setValue("Portal_ODAD_Disbursement_L_DateDisbursement", disbusedate);
            }

            String getODADFinalDetails = "select SB_ACCOUNTNO,DISB_AMOUNT from LOS_TRN_LOANDETAILS "
                    + "where PID='" + ProcessInsanceId + "'";
            Log.consoleLog(ifr, "inside getODADFinalDetails : ");
            List<List<String>> list2 = cf.mExecuteQuery(ifr, getODADFinalDetails, "getODADFinalDetails:");
            String LoanAmount = "", CasaAccNo = "";
            Log.consoleLog(ifr, "inside mExecuteQuery : ");
            if (list2.size() > 0) {
                CasaAccNo = list2.get(0).get(0);
                LoanAmount = list2.get(0).get(1);
            }
            ifr.setValue("P_ODAD_CasaAccountNumber", CasaAccNo);
            ifr.setValue("P_ODAD_SanctionedAmt", LoanAmount);

            String queryStatus = ConfProperty.getQueryScript("FINALSTATUS").replaceAll("#WINAME#", ProcessInsanceId);
            Log.consoleLog(ifr, "queryStatus " + queryStatus);
            ifr.saveDataInDB(queryStatus);

            pcm.setValueInBackOffice(ifr, "FINAL_DISB_LAD", "FINAL_DISB_SetValue_Backoffice_LAD");
            ifr.setValue("QNL_LOS_TRAN_LAD_FINALDISB.LOANAMOUNT_NO", loanAcc);
            ifr.setValue("QNL_LOS_TRAN_LAD_FINALDISB.DISBURSEMENT_DATE", disbusedate);
            ifr.setValue("QNL_LOS_TRAN_LAD_FINALDISB.LOANAMOUNT", LoanAmount);
            ifr.setValue("QNL_LOS_TRAN_LAD_FINALDISB.CasaAccumber", CasaAccNo);
            ifr.setValue("QNL_LOS_TRAN_LAD_FINALDISB.MaturityDate", formattedFutureDate);

            String upQuery = "UPDATE LOS_WIREFERENCE_TABLE SET "
                    + "APPLICATION_STATUS='COMPLETED' WHERE WINAME='" + ProcessInsanceId + "'";
            Log.consoleLog(ifr, "upQuery:" + upQuery);
            ifr.saveDataInDB(upQuery);

        } catch (Exception e) {
            Log.consoleLog(ifr, " Exception in autoPopulateDataFinalODAD : " + e);
            Log.errorLog(ifr, " Exception in autoPopulateDataFinalODAD : " + e);
        }

    }

    public void mChangeSalaryAccountLAD(IFormReference ifr) {
        try {
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String AccountNo = ifr.getValue("P_ODAD_SBACCOUNTNO").toString().split("-")[0];
            String Strbranch = ifr.getValue("P_ODAD_SBACCOUNTNO").toString().split("-")[1];
            String StrAccountbala = ifr.getValue("P_ODAD_SBACCOUNTNO").toString().split("-")[3];
            String StrDataopen = ifr.getValue("P_ODAD_SBACCOUNTNO").toString().split("-")[2];
            Date dat = inputDateFormat.parse(StrDataopen);
            StrDataopen = outputDateFormat.format(dat);
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "AccountNo:" + AccountNo + StrDataopen + StrAccountbala + Strbranch);
            ifr.setValue("P_ODAD_REPAYMENTNO", AccountNo);
            String StrUpdate = "update los_trn_finaleligibility set SB_Accountno= '" + AccountNo + "',RP_Accountno='" + AccountNo + "',SavingAccountbal='" + StrAccountbala + "',\n"
                    + "Savingdateopened='" + StrDataopen + "',branchcode='" + Strbranch + "' where WINAME='" + PID + "'";
            cf.mExecuteQuery(ifr, StrUpdate, "Query for OTP Check:");
        } catch (Exception e) {
            Log.consoleLog(ifr, " Exception in mChangeSalaryAccountLAD : " + e);
            Log.errorLog(ifr, " Exception in mChangeSalaryAccountLAD : " + e);
        }

    }

    public String mValidateOTPRecieveMoneyODAD(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside mAccValidateOTPRecieveMoneyODAD : ");
        JSONObject re = new JSONObject();
        String enterOTP = ifr.getValue("P_ODAD_ENTEROTP").toString();
        Log.consoleLog(ifr, "enterOTP ; " + enterOTP);
        if (enterOTP.equalsIgnoreCase("")) {
            re.put("showMessage", cf.showMessage(ifr, "P_ODAD_RM_VALIDATE", "error", "Kindly Enter OTP"));
            return re.toString();
        }
        String sendOTP = "";
        String mobileNumber = pcm.getCurrentWiMobileNumber(ifr);
        String query = ConfProperty.getQueryScript("POTPQuery").replaceAll("#mobileNumber#", mobileNumber);
        Log.consoleLog(ifr, "query:" + query);
        List<List<String>> OTPD = cf.mExecuteQuery(ifr, query, "Query for OTP Check:");
        if (!OTPD.isEmpty()) {
            sendOTP = OTPD.get(0).get(0);
            Log.consoleLog(ifr, "sendOTP==>" + sendOTP);
        }
        if (enterOTP.equalsIgnoreCase(sendOTP)) {
            Log.consoleLog(ifr, "CBSFinalScreenValidation is calling.....");
            String loanAccountno = getLoanAccountCreation(ifr);
            Log.consoleLog(ifr, "loanAccountno==>" + loanAccountno);
            if (loanAccountno.equalsIgnoreCase(message)) {
                JSONObject returnRes = new JSONObject();
                returnRes.put("showMessage", cf.showMessage(ifr, "", "error", "" + message));
                return returnRes.toString();
            } else if (loanAccountno.equalsIgnoreCase("") || loanAccountno.equalsIgnoreCase("null")
                    || loanAccountno.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorcustmessage(ifr, loanAccountno);
            } else {
                re.put("NavigationNextClick", "true");
                return re.toString();
            }
        } else {
            re.put("showMessage", cf.showMessage(ifr, "P_ODAD_RM_VALIDATE", "error", "Kindly Enter Correct OTP"));
            return re.toString();
        }
    }

    public String getFacilityType(IFormReference ifr) {
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String query = "select FacilityType from LOS_NL_PROPOSED_FACILITY where pid='" + PID + "'";
        List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Mobile Number:");
        if (!list.isEmpty()) {
            return list.get(0).get(0);
        }
        return "";
    }

    //Commeneted by Ahmed on 01-07-2024 for making it as common functions
//    public String getSchemeIDODAD(IFormReference ifr) {
//        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
//        String query = "select SchemeID from LOS_NL_PROPOSED_FACILITY where pid='" + PID + "'";
//        List<List<String>> list = cf.mExecuteQuery(ifr, query, "Get Mobile Number:");
//        if (!list.isEmpty()) {
//            return list.get(0).get(0);
//        }
//        return "";
//    }
    private String getSelectedFDAccountBranchCode(IFormReference ifr, String FDAccountNo) {

        Log.consoleLog(ifr, "#getSelectedFDAccountBranchCode started..");

        try {

            String query = "SELECT RESPONSE FROM LOS_INTEGRATION_REQRES WHERE "
                    + "TRANSACTION_ID='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "' AND \n"
                    + "API_NAME='CBS_CustomerAccountSummary' AND API_STATUS='SUCCESS' AND ROWNUM=1";

            Log.consoleLog(ifr, "query===>" + query);
            List< List< String>> Result = ifr.getDataFromDB(query);
            Log.consoleLog(ifr, "#Result===>" + Result.toString());
            String responseFromDB = "";
            if (Result.size() > 0) {
                responseFromDB = Result.get(0).get(0);
            }

            Log.consoleLog(ifr, "responseFromDB===>" + responseFromDB);

            JSONParser parser = new JSONParser();
            JSONObject resultObj = (JSONObject) parser.parse(responseFromDB);
            String body = resultObj.get("body").toString();
            JSONObject bodyObj = (JSONObject) parser.parse(body);
            String CustomerAccountSummaryResponse = bodyObj.get("CustomerAccountSummaryResponse").toString();
            JSONObject CustomerAccountSummaryResponseObj = (JSONObject) parser.parse(CustomerAccountSummaryResponse);
            String TDDetailsDTO = CustomerAccountSummaryResponseObj.get("TDDetailsDTO").toString();

            JSONArray accountIdArray = (JSONArray) parser.parse(TDDetailsDTO);

            for (Object accountIdArrayObj : accountIdArray) {
                JSONObject accountObject = (JSONObject) accountIdArrayObj;
                Log.consoleLog(ifr, "accountObject=>" + accountObject);
                JSONObject accountObjectObj = (JSONObject) parser.parse(accountObject.toString());

                String accountId = accountObjectObj.get("AccountId").toString().trim().replaceAll("\\s+$", "").replace(" ", "");
                Log.consoleLog(ifr, "accountId=====>" + accountId);
                Log.consoleLog(ifr, "FDAccountNo===>" + FDAccountNo);

                if (accountId.trim().equalsIgnoreCase(FDAccountNo.trim())) {
                    Log.consoleLog(ifr, "accountId matched");
                    String branchCode = (String) accountObject.get("BranchCode");
                    Log.consoleLog(ifr, "branchCode==>" + branchCode);
                    return branchCode;
                } else {
                    Log.consoleLog(ifr, "accountId not matched");
                }
            }

            return RLOS_Constants.ERROR;

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception==>" + e);
        }
        return RLOS_Constants.ERROR;
    }
}
