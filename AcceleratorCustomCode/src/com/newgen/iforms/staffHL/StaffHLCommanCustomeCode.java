package com.newgen.iforms.staffHL;

import com.newgen.dlp.integration.brm.BRMCommonRules;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormAPIHandler;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.mvcbeans.model.wfobjects.WDGeneralData;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
public class StaffHLCommanCustomeCode {
    PortalCommonMethods pcm = new PortalCommonMethods();
    CommonFunctionality cf = new CommonFunctionality();
    BRMCommonRules brmsrules = new BRMCommonRules();
    HLExternalAPIs hlep = new HLExternalAPIs();
    public boolean isUserEntryExist(IFormReference ifr, String customerId) {
        Log.consoleLog(ifr, "isUserEntryExist");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String strCustquer = "select 1 from LOS_NL_BASIC_INFO where PID='"+PID+"' and CUSTOMERID='"+customerId+"'";
        List<List<String>> result = ifr.getDataFromDB(strCustquer);
        if (result.size() > 0 && !"0".equals(result.get(0).get(0))) {
            return true;
        } else {
            return false;
        }
    }
    public String populateDropDownForSelectedValue(IFormReference ifr, String selectedValue, String queryKey, String dropdownControl) {
        try {
            // Clear the dropdown control
            Boolean isValueExist = false;
            String dropdownSelectedValue = ifr.getValue(dropdownControl).toString();
            Log.consoleLog(ifr, "populateDropDownForSelectedValue dropdownSelectedValue:-> " + dropdownSelectedValue);
            Log.consoleLog(ifr, "selectedValue=" + selectedValue);
            ifr.clearCombo(dropdownControl);
            String[] selctedArray = selectedValue.split("#");
            // Replace placeholder in the dropdown query
            String query = ConfProperty.getQueryScript(queryKey);
            Log.consoleLog(ifr, "query initial" + query);
            Log.consoleLog(ifr, "selctedArray length" + selctedArray.length);
            if (selctedArray.length == 1) {
                query = query.replace("#SELECTEDVALUE#", selectedValue);
            } else {
                for (int i = 0; i < selctedArray.length; i++) {
                    String key = "#SELECTEDVALUE" + i + "#";
                    Log.consoleLog(ifr, "key:" + key + "value" + selctedArray[i]);
                    query = query.replace(key, selctedArray[i]);
                    Log.consoleLog(ifr, "query:" + i + " final query " + query);
                }
            }

            List<List<String>> dropdownDataResult = cf.mExecuteQuery(ifr, query, "Fetching dropdown data");
            for (List<String> row : dropdownDataResult) {
                String key = row.get(0);
                String val = row.get(1);
                Log.consoleLog(ifr, "populateDropDownForSelectedValue Key:-> " + key + " Value:-> " + val);
                ifr.addItemInCombo(dropdownControl, key, val);
                if (val.equalsIgnoreCase(dropdownSelectedValue)) {
                    Log.consoleLog(ifr, "populateDropDownForSelectedValue insed selected value match if");
                    ifr.setValue(dropdownControl, dropdownSelectedValue);
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error populating dropdown " + e.getMessage());
        }
        return "";
    }
    public String getDBValuesFromTempStaffHL(IFormReference ifr, JSONObject customerDetails, String Applicanttype) {
        String knockoffDecision = "";
        try {
            Log.consoleLog(ifr, "inside try block getDBValuesFromTempStaffHL::::");
            String AadharNo = customerDetails.get("AadharNo").toString();
            Log.consoleLog(ifr, "AadharNo value  : " + AadharNo);
            String AadharNoExist = AadharNo.equalsIgnoreCase("") ? "No" : "Yes";
            Log.consoleLog(ifr, "AadharNoExist value  : " + AadharNoExist);
            String PanNumber = customerDetails.get("PanNumber").toString();
            Log.consoleLog(ifr, "PanNumber value  : " + PanNumber);
            String PanExist = PanNumber.equalsIgnoreCase("") ? "No" : "Yes";
            Log.consoleLog(ifr, "PanExist  : " + PanExist);
            Log.consoleLog(ifr, " customerDetails.get(\"Classification\") " + customerDetails.get("Classification"));
            String Classification = "";
            if (customerDetails.get("Classification") != null) {
                Classification = customerDetails.get("Classification").toString();
                Log.consoleLog(ifr, "Classification value  : " + Classification);
            }
            Log.consoleLog(ifr, "Classification value  : " + Classification);
            String ClassificationExist = "";
            if (Classification != null && Classification.equalsIgnoreCase("Yes")) {
                ClassificationExist = "Yes";
            } else {
                ClassificationExist = "No";
            }
            Log.consoleLog(ifr, "ClassificationExist value  : " + ClassificationExist);

            String writeOffPresent = customerDetails.get("writeOffPresent").toString();
            Log.consoleLog(ifr, "writeOffPresent value  : " + writeOffPresent);
            String writeOffPresentExist = writeOffPresent.equalsIgnoreCase("Yes") ? "Yes" : "No";
            String MobileNo = customerDetails.get("MobileNumber").toString();
            Log.consoleLog(ifr, "MobileNumber value  : " + MobileNo);
            String Age = customerDetails.get("Age").toString();
            Log.consoleLog(ifr, "Age value  : " + Age);

            Log.consoleLog(ifr, "Applicanttype :: " + Applicanttype + ",CustomerAge::" + Age + ", AadharNoExist:: "
                    + AadharNoExist + " ,Pan:: " + PanExist + " ,ClassificationExist:: " + ClassificationExist
                    + " ,writeOffPresentExist:: " + writeOffPresentExist
            );
            String knockoffInParams = Applicanttype + "," + AadharNoExist + "," + PanExist + ","
                    + writeOffPresentExist + "," + ClassificationExist + "," + Age;

            String customerName = "";
            String CustomerFirstName = customerDetails.get("CustomerFirstName").toString();
            String customerMiddleName = customerDetails.get("CustomerMiddleName").toString();
            String customerLastName = customerDetails.get("CustomerLastName").toString();

            if ((customerMiddleName.equalsIgnoreCase("")) || (customerMiddleName.equalsIgnoreCase("null")) || (customerMiddleName == null)) {
                customerName = CustomerFirstName + " " + customerLastName;
            } else {
                customerName = CustomerFirstName + " " + customerMiddleName + " " + customerLastName;
            }

            knockoffDecision = checkKnockOff(ifr, knockoffInParams);
            Log.consoleLog(ifr, "knockoffDecision Staff HL ===>" + knockoffDecision);
            if (knockoffDecision.contains(RLOS_Constants.ERROR)) {
                return pcm.returnCustomErrorMessage(ifr, "KnockOff Check Failed");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in executeBRMSRule::getDBValuesFromTempHL::" + e);
            Log.errorLog(ifr, "Exception in executeBRMSRule::getDBValuesFromTempHL::" + e);
        }
        return knockoffDecision;
    }

    public String checkKnockOff(IFormReference ifr, String paramsData) {
        String kychl_op = "NOT ELIGIBLE";
        String writeoffhl_op = "NOT ELIGIBLE";
        String npahl_op = "NOT ELIGIBLE";
        String agehl_op = "NOT ELIGIBLE";
        String totalknockoffhl_op = "REJECT";
        try {
            String[] paramList = paramsData.split(",");
            String apptypehl_ip = paramList[0];
            Log.consoleLog(ifr, "inside check knockOff apptypehl: " + apptypehl_ip);
            String aadharhl_ip = paramList[1];
            Log.consoleLog(ifr, "inside check knockOff aadharhl: " + aadharhl_ip);
            String panhl_ip = paramList[2];
            Log.consoleLog(ifr, "inside check knockOff panhl: " + panhl_ip);
            String writeoffhl_ip = paramList[3];
            Log.consoleLog(ifr, "inside check knockOff writeoffhl: " + writeoffhl_ip);
            String npahl_ip = paramList[4];
            Log.consoleLog(ifr, "inside check knockOff npahl: " + npahl_ip);
            String agehl_ip = paramList[5];
            Log.consoleLog(ifr, "inside check knockOff agehl: " + agehl_ip);

            boolean isCBorG = apptypehl_ip.equalsIgnoreCase("CB") || apptypehl_ip.equalsIgnoreCase("G");
            int age = 0;
            try {
                age = Integer.parseInt(agehl_ip);
            } catch (NumberFormatException e) {
                Log.consoleLog(ifr, "Invalid age value:: " + agehl_ip);
            }

            if (isCBorG && aadharhl_ip.equalsIgnoreCase("Yes") &&
                    panhl_ip.equalsIgnoreCase("Yes")){
                kychl_op = "ELIGIBLE";
            }
            Log.consoleLog(ifr, "Staff HL Knock Off kychl_op:: "+kychl_op);

            if (isCBorG && writeoffhl_ip.equalsIgnoreCase("No")){
                writeoffhl_op = "ELIGIBLE";
            }
            Log.consoleLog(ifr, "Staff HL Knock Off writeoffhl_op:: "+writeoffhl_op);

            if (isCBorG && npahl_ip.equalsIgnoreCase("No")){
                npahl_op = "ELIGIBLE";
            }
            Log.consoleLog(ifr, "Staff HL Knock Off npahl_op:: "+npahl_op);

            if (isCBorG && age >= 21 && age <= 99){
                agehl_op = "ELIGIBLE";
            }
            Log.consoleLog(ifr, "Staff HL Knock Off agehl_op:: "+agehl_op);

            if ((apptypehl_ip.equalsIgnoreCase("CB")|| apptypehl_ip.equalsIgnoreCase("G")) &&
                    kychl_op.equalsIgnoreCase("ELIGIBLE") &&
                    writeoffhl_op.equalsIgnoreCase("ELIGIBLE") &&
                    npahl_op.equalsIgnoreCase("ELIGIBLE") &&
                    agehl_op.equalsIgnoreCase("ELIGIBLE")){
                totalknockoffhl_op = "APPROVE";
            }
            Log.consoleLog(ifr, "Staff HL KnockOff final Decision totalknockoffhl_op:: "+totalknockoffhl_op);

            return totalknockoffhl_op;

        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception during Staff HL KnockOff check: " + ex);
            Log.errorLog(ifr, "Exception during Staff HL KnockOff check: " + ex);

            return RLOS_Constants.ERROR + ":" + "Error in KnockOff Check Staff HL";
        }

    }
   
    public String fetchCICResponse(IFormReference ifr, String control) {
        Log.consoleLog(ifr, "Inside HLBkoffcCustomCode->fetchCICResponse:");
        JSONObject message = new JSONObject();
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->ProcessInstanceId: " + ProcessInstanceId);
        try {
            String insertionOrderID = ifr.getValue("DD_CB_APPTYPE_ID").toString();
            String bureauType = ifr.getValue("DD_CB_BUREAU_ID").toString();
            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->partyType: " + insertionOrderID);
            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->bureauType: " + bureauType);
            int gridSize = cf.getGridCount(ifr, "ALV_CB_Details");
            for (int i = 0; i < cf.getGridCount(ifr, "ALV_CB_Details"); i++) {
                if (insertionOrderID.equalsIgnoreCase(ifr.getTableCellValue("ALV_CB_Details", i, "QNL_CB_Details_Applicant_Type").toString()) && bureauType.equalsIgnoreCase(ifr.getTableCellValue("ALV_CB_Details", i, "QNL_CB_Details_CB_Type").toString())) {
                    message.put("showMessage", cf.showMessage(ifr, "", "error", "Credit Bureau for the Party Type already fetched."));
                    ifr.setValue("DD_CB_APPTYPE_ID", "");
                    ifr.setValue("DD_CB_BUREAU_ID", "");
                    return message.toString();
                }
            }
            String customerId = "";
            String fkey = "";
            String applicantType = "";
            String insertionorderid = "";
            String customerIdQuery = "SELECT CUSTOMERID,F_KEY,APPLICANTTYPE,insertionorderid FROM LOS_NL_BASIC_INFO WHERE PID = '" + ProcessInstanceId + "' AND INSERTIONORDERID = '" + insertionOrderID + "'and APPLICANTTYPE in ('CB', 'G')";
            List<List<String>> customerIdResult = cf.mExecuteQuery(ifr, customerIdQuery, "Fetching customerId: ");
            if (!customerIdResult.isEmpty()) {
                customerId = customerIdResult.get(0).get(0);
                Log.consoleLog(ifr, "FtechCIC fetched customerID:: "+customerId);
                fkey = customerIdResult.get(0).get(1);
                Log.consoleLog(ifr, "FtechCIC fetched customerID:: "+fkey);
                applicantType = customerIdResult.get(0).get(2);
                Log.consoleLog(ifr, "FtechCIC fetched customerID:: "+applicantType);
                insertionorderid = customerIdResult.get(0).get(3);
                Log.consoleLog(ifr, "FtechCIC fetched customerID:: "+insertionorderid);
            }
            String mobileNoQuery = "SELECT MOBILENO FROM LOS_L_BASIC_INFO_I WHERE F_KEY = '" + fkey + "'";
            List<List<String>> mobileNoResult = cf.mExecuteQuery(ifr, mobileNoQuery, "Fetching mobileno: ");
            String mobileNo = mobileNoResult.get(0).get(0);
            if (mobileNo.startsWith("91")) {
                mobileNo = mobileNo.substring(2); // Remove 91
                if (mobileNo.length() == 12 && mobileNo.startsWith("91")) {
                    mobileNo = mobileNo.substring(2);
                }
            }
            if (mobileNo.length() > 10) {
                mobileNo = mobileNo.substring(mobileNo.length() - 10);
            }
            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->MobileNo ==>" + mobileNo);
            String mobnoB = "";
            String Aadhar = "";
            String cust_id = "";
            CustomerAccountSummary objCustSummary = new CustomerAccountSummary();
            HashMap<String, String> map = new HashMap<>();
            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->mobnoB ==>" + mobnoB);

            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->Budget loan map value:::==>" + map);
            map.clear();
            map.put("MobileNumber", mobileNo);
            map.put("CustomerId", customerId);
            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->Budget.Map" + map);
            String cbsresp = objCustSummary.getCustomerAccountSummary(ifr, map);
            if (cbsresp.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorAPIThroughExecute(ifr, cbsresp);
            }
            JSONParser jp = new JSONParser();
            JSONObject cbsrespobj;
            cbsrespobj = (JSONObject) jp.parse(cbsresp);
            cust_id = cbsrespobj.get("CustomerID").toString();
            Aadhar = cbsrespobj.get("AadharNo").toString();
            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->Aadhar no::" + Aadhar);
            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->For bureau check:::");
            String productCode = pcm.mGetProductCode(ifr);
            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->ProductCode:" + productCode);
            String subProductCode = pcm.mGetSubProductCode(ifr);
            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->subProductCode:" + subProductCode);
            String ReqLoamAmount = "";
            String query = "select distinct(REQ_AMT_TOT_PLD) from slos_staff_home_trn where winame ='" + ProcessInstanceId + "'";
            List<List<String>> LoanAmountResult = ifr.getDataFromDB(query);
            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->#REQLOANAMT===>" + LoanAmountResult.toString());
            if (!LoanAmountResult.isEmpty()) {
                ReqLoamAmount = LoanAmountResult.get(0).get(0);
            }
            HashMap<String, String> arguments = new HashMap<>();
            arguments.put("applicantType", applicantType);
            arguments.put("customerId", customerId);
            arguments.put("mobileNo", mobileNo);
            arguments.put("insertionOrderId", insertionOrderID);
            String loanType = pcm.getLoanSelected(ifr);
            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->loanType: " + loanType);
            String cicParam = "";
            cicParam = applicantType + "~" + insertionorderid;
            Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->cicParam: " + cicParam);
            if ("CB".equalsIgnoreCase(bureauType)) {
                String cb = hlep.mCallBureauHL(ifr, "CB", Aadhar, cicParam, ReqLoamAmount);
                if (cb.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnCustomErrorMessage(ifr, cb);
                } else {
                    Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->CIBIL Passed Successfully :::");
                    autoPopulateBureauHL(ifr, "CB", arguments);
                }
            } else if ("EX".equalsIgnoreCase(bureauType)) {
                String cb = hlep.mCallBureauHL(ifr, "EX", Aadhar, cicParam, ReqLoamAmount);
                if (cb.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnCustomErrorMessage(ifr, cb);
                } else {
                    Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->Experian Passed Successfully :::");
                    autoPopulateBureauHL(ifr, "EX", arguments);
                }
            } else if ("EF".equalsIgnoreCase(bureauType)) {
                String cb = hlep.mCallBureauHL(ifr, "EF", Aadhar, cicParam, ReqLoamAmount);
                if (cb.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnCustomErrorMessage(ifr, cb);
                } else {
                    Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->Equifax Passed Successfully :::");
                    autoPopulateBureauHL(ifr, "EF", arguments);
                }
            } else if ("HM".equalsIgnoreCase(bureauType)) {
                String cb = hlep.mCallBureauHL(ifr, "HM", Aadhar, cicParam, ReqLoamAmount);
                if (cb.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnCustomErrorMessage(ifr, cb);
                } else {
                    Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->Highmark Passed Successfully :::");
                    autoPopulateBureauHL(ifr, "HM", arguments);
                }
            } else if ("CBCOM".equalsIgnoreCase(bureauType)) {
                String cbComResponse = hlep.mCallBureauHL(ifr, "CBCOM", Aadhar, cicParam, ReqLoamAmount);
                if (cbComResponse.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnCustomErrorMessage(ifr, cbComResponse);
                } else {
                    Log.consoleLog(ifr, "HLBkoffcCustomCode:fetchCICResponse->Commercial Passed Successfully :::");
                    autoPopulateBureauHL(ifr, "CBCOM", arguments);
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in HLBkoffcCustomCode->fetchCICResponse:" + e);
        }
        return "";
    }

    public void autoPopulateBureauHL(IFormReference ifr, String bureauType, HashMap<String, String> arguments) {
        try {
            Log.consoleLog(ifr, "HLCommanCustomCode:autoPopulateBureauVL->Inside autoPopulateBureau: ");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "HLCommanCustomCode:autoPopulateBureauVL->ProcessInstanceId: " + ProcessInstanceId);
            String applicantType = arguments.get("applicantType");
            String insertionorderID = arguments.get("insertionOrderId");

            String baisInfoTableQuery = "SELECT insertionOrderID,applicanttype FROM LOS_NL_BASIC_INFO "
                    + "WHERE PID='" + ProcessInstanceId + "' AND insertionorderid='" + insertionorderID + "'";
            List<List<String>> baisInfoTableData = cf.mExecuteQuery(ifr, baisInfoTableQuery, "HLCommanCustomCode:autoPopulateBureauVL->Fetching insertion order ID for given applicant type: ");

            for (int i = 0; i < baisInfoTableData.size(); i++) {
                String basicInfoInsertionOrderID = baisInfoTableData.get(i).get(0);
                String basicInfoapplicanttype = baisInfoTableData.get(i).get(1);

                String bureauTypeValue = "", expCBSCORE = "";
                String dataSaveBueroCheckGridQuery = "";
                String loanType = pcm.getLoanSelected(ifr);
                Log.consoleLog(ifr, "HLCommanCustomCode:fetchCICResponse->loanType: " + loanType);

                dataSaveBueroCheckGridQuery = "select distinct BureauType,Exp_CBSCORE from"
                        + " LOS_CAN_IBPS_BUREAUCHECK where ProcessInstanceId='" + ProcessInstanceId
                        + "' and BureauType='" + bureauType + "' and APPLICANT_TYPE like '%"+basicInfoInsertionOrderID+"%'";

                List<List<String>> dataSaveBuroCheckGridData = cf.mExecuteQuery(ifr,
                        dataSaveBueroCheckGridQuery, "HLCommanCustomCode:autoPopulateBureauVL->dataSaveBueroCheckGridData: ");
                if (!dataSaveBuroCheckGridData.isEmpty()) {
                    bureauTypeValue = dataSaveBuroCheckGridData.get(0).get(0);
                    expCBSCORE = dataSaveBuroCheckGridData.get(0).get(1);
                }

                Log.consoleLog(ifr, "HLCommanCustomCode:autoPopulateBureauVL->BureauType: " + bureauTypeValue + " Exp_CBSCORE: " + expCBSCORE);

                JSONArray arr = new JSONArray();
                JSONObject re = new JSONObject();

                // Different JSON keys based on activity name
                if ("Portal".equalsIgnoreCase(ifr.getActivityName())) {
                    re.put("QNL_CB_Details-Applicant_Type", basicInfoInsertionOrderID);
                    re.put("QNL_CB_Details-CB_Type", bureauTypeValue);
                    re.put("QNL_CB_Details-CB_Score", expCBSCORE);
                    re.put("QNL_CB_Details-ApplicantCode", basicInfoapplicanttype);
                } else {
                    re.put("QNL_CB_Details_Applicant_Type", basicInfoInsertionOrderID);
                    re.put("QNL_CB_Details_CB_Type", bureauTypeValue);
                    re.put("QNL_CB_Details_CB_Score", expCBSCORE);
                    re.put("QNL_CB_Details_ApplicantCode", basicInfoapplicanttype);
                }

                arr.add(re);

                if ("Portal".equalsIgnoreCase(ifr.getActivityName())) {
                    Log.consoleLog(ifr, "HLCommanCustomCode:autoPopulateBureauVL->Inside Portal condition: ");
                    ((IFormAPIHandler) ifr).addDataToGrid("QNL_CB_Details", arr, true);
                } else {
                    Log.consoleLog(ifr, "HLCommanCustomCode:autoPopulateBureauVL->Inside Back Office condition: ");
                    ifr.addDataToGrid("ALV_CB_Details", arr, true);
                }
                Log.consoleLog(ifr, "HLCommanCustomCode:autoPopulateBureauVL->End of grid population.");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "HLCommanCustomCode:autoPopulateBureauVL->Exception in autoPopulateBureau:::" + e);
        }
    }
    
    public String onLoadFetchBureauSHL(IFormReference ifr) {
        WDGeneralData Data = ifr.getObjGeneralData();
        String ProcessInstanceId = Data.getM_strProcessInstanceId();
        try {
            String fetchAllCP = "select INSERTIONORDERID, APPLICANTTYPE  from los_nl_basic_info where pid = '"+ProcessInstanceId+"'";
            List<List<String>> allCPData = cf.mExecuteQuery(ifr, fetchAllCP, "Fetching all the co-applicants");
            for (int i=0; i<allCPData.size(); i++){
                String insertionId = allCPData.get(i).get(0);
                String applicantType = allCPData.get(i).get(1);

                Log.consoleLog(ifr, "Inside triggerBureauConsentHL::");
                String bueroConsentTableQuery = "Select 1 from LOS_NL_BUREAU_CONSENT where PID='"+ProcessInstanceId+"' and PartyType = '"+insertionId+"'";
                List<List<String>> bueroConsentTableData = cf.mExecuteQuery(ifr, bueroConsentTableQuery, "Fetching all added consent");
                boolean isconsentExist = bueroConsentTableData.size() > 0 && "1".equals(bueroConsentTableData.get(0).get(0));

                if (!isconsentExist) {
                    Log.consoleLog(ifr, "ItriggerBureauConsentHL: Inside::" + applicantType);
                    String dataSavingINConsentGridQuery = "INSERT INTO LOS_NL_BUREAU_CONSENT (PID, InsertionOrderID, PartyType, Methodology, ConsentReceived) "
                            + "VALUES('" + ProcessInstanceId + "', S_LOS_NL_BUREAU_CONSENT.nextval, '" + insertionId + "', 'MU', 'Initiated')";
                    Log.consoleLog(ifr, "Insert triggerBureauConsentHL for consent: " + dataSavingINConsentGridQuery);
                    ifr.saveDataInDB(dataSavingINConsentGridQuery);
                }

            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in onLoadFetchBureauSHL::" + e);
            Log.errorLog(ifr, "Exception in onLoadFetchBureauSHL::" + e);
        }
        return "";
    }
    public String populateDropDownForSelectedValueSHL(IFormReference ifr, String selectedValue, String queryKey, String dropdownControl) {

        try {
            // Clear the dropdown control
            Boolean isValueExist = false;
            String dropdownSelectedValue = ifr.getValue(dropdownControl).toString();
            Log.consoleLog(ifr, "populateDropDownForSelectedValue dropdownSelectedValue:-> " + dropdownSelectedValue);
            Log.consoleLog(ifr, "selectedValue=" + selectedValue);
            if (ifr.getValue(dropdownControl).toString().trim().equalsIgnoreCase("")){
                ifr.clearCombo(dropdownControl);
            }
            String[] selctedArray = selectedValue.split("#");
            // Replace placeholder in the dropdown query
            String query = "Select OCCSUBTYPE, OCCSUBTYPECODE,subtypeorderid From LOS_M_OCCUPATION where OCCUPATIONCODE='"+selectedValue+"' and ISACTIVE='Y' order by subtypeorderid";
            Log.consoleLog(ifr, "query initial" + query);
            Log.consoleLog(ifr, "selctedArray length" + selctedArray.length);

            List<List<String>> dropdownDataResult = cf.mExecuteQuery(ifr, query, "Fetching dropdown data");

            for (List<String> row : dropdownDataResult) {
                String key = row.get(0);
                String val = row.get(1);
                Log.consoleLog(ifr, "populateDropDownForSelectedValue Key:-> " + key + " Value:-> " + val);
                ifr.addItemInCombo(dropdownControl, key, val);
                if (val.equalsIgnoreCase(dropdownSelectedValue)) {
                    Log.consoleLog(ifr, "populateDropDownForSelectedValue insed selected value match if");
                    ifr.setValue(dropdownControl, dropdownSelectedValue);
                }

            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Error populating dropdown " + e.getMessage());
        }

        return "";
    }
    public String populateDropDownForCollateralSHL(IFormReference ifr, String subProductCode, String dropdownControl) {
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
//            String securityType = "";
//            String checkSecTypeQ = "select SECURITY_TYPE from los_cam_collateral_details where pid='"+PID+"'";
//            List<List<String>> checkSecTypeRes = cf.mExecuteQuery(ifr, checkSecTypeQ, "Fetching security type");
//            if (!checkSecTypeRes.isEmpty()){
//                securityType = checkSecTypeRes.get(0).get(0);
//            }
//            Log.consoleLog(ifr, "Fetched Security Type:: "+securityType);
//            if (securityType.equalsIgnoreCase("") || securityType.trim().isEmpty()){
//            	Log.consoleLog(ifr, "Inside true condition");
//                ifr.clearCombo(dropdownControl);
//            }
            String query = "select distinct security_type, security_type from los_m_hl_collateral where PRODUCT_CODE='SHL' AND Sub_product_code = '"+subProductCode+"' union all select distinct security_type, security_type from los_m_hl_collateral where security_type='SELECT'";
            Log.consoleLog(ifr, "query initial" + query);

            List<List<String>> dropdownDataResult = cf.mExecuteQuery(ifr, query, "Fetching dropdown data");

            for (List<String> row : dropdownDataResult) {
                String key = row.get(0);
                String val = row.get(1);
                Log.consoleLog(ifr, "populateDropDownForSelectedValue Key:-> " + key + " Value:-> " + val);
                ifr.addItemInCombo(dropdownControl, key, val);
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Error populating dropdown " + e.getMessage());
        }
        return "";
    }
    public void changeFieldsMandatoryStatus(IFormReference ifr) {
        String activityName = ifr.getActivityName().trim();
        Log.consoleLog(ifr, "Activity Name:: "+activityName);
        if (activityName.equalsIgnoreCase("Staff_HL_Branch_Maker")) {
            Log.consoleLog(ifr, "Inside Staff_HL_Branch_Maker activity condition:: ");
            ifr.setStyle("HOUSE_NO_COLL_HL", "mandatory", "false");
            ifr.setStyle("HOUSE_NO_COLL_HL", "mandatory", "false");
            ifr.setStyle("FLOOR_COLL_HL", "mandatory", "false");
            ifr.setStyle("Q_HL_COLLATERAL_C_HL_COLLATERALDETAILS_ASSET_C_LATITUDE", "mandatory", "false");
            ifr.setStyle("Q_HL_COLLATERAL_C_HL_COLLATERALDETAILS_ASSET_C_LONGITUDE", "mandatory", "false");
            ifr.setStyle("REG_OFFICE_COLL_HL", "mandatory", "false");
            ifr.setStyle("REG_DATE_COLL_HL", "mandatory", "false");
            ifr.setStyle("AGRE_SA_DE_NO_COLL_HL", "mandatory", "false");
            ifr.setStyle("AGRE_SA_DE_DATE_COLL_HL", "mandatory", "false");
            ifr.setStyle("TOT_LEASE_COLL_HL", "mandatory", "false");
            ifr.setStyle("STATU_CLE_YN_COLL_HL", "mandatory", "false");
            ifr.setStyle("HOUSE_FIN_AGEN_COLL_HL", "mandatory", "false");
            ifr.setStyle("LEDTD_DATE_COLL_HL", "mandatory", "false");
//            ifr.setStyle("MARKET_VAL_COLL_HL", "mandatory", "false");
            ifr.setStyle("REALIZ_VAL_COLL_HL", "mandatory", "false");
            ifr.setStyle("DISTRESS_VAL_COLL_HL", "mandatory", "false");
            ifr.setStyle("UNIT_MARKET_VAL_COLL_HL", "mandatory", "false");
            ifr.setStyle("GUIDLINE_VAL_COLL_HL", "mandatory", "false");
            ifr.setStyle("UNIT_GUIDANCE_VAL_COLL_HL", "mandatory", "false");
            ifr.setStyle("DEED_DET_COLL_HL", "mandatory", "false");
            ifr.setStyle("PROB_DATE_COLL_HL", "mandatory", "false");
            ifr.setStyle("EMT_DOC_COLL_HL", "mandatory", "false");
            ifr.setStyle("TITLE_DEED_YN_COLL_HL", "mandatory", "false");
            ifr.setStyle("Q_HL_COLLATERAL_C_HL_COLLATERALDETAILS_ASSET_C_REASON", "mandatory", "false");
            ifr.setStyle("Q_HL_COLLATERAL_C_HL_COLLATERALDETAILS_ASSET_C_SALE_DEED_REG_NO", "mandatory", "false");
            ifr.setStyle("Q_HL_COLLATERAL_C_HL_COLLATERALDETAILS_ASSET_C_SALE_DEED_DATE", "mandatory", "false");
            ifr.setStyle("Q_HL_COLLATERAL_C_HL_COLLATERALDETAILS_ASSET_C_EMT_MODTD_REG_NO", "mandatory", "false");
            ifr.setStyle("Q_HL_COLLATERAL_C_HL_COLLATERALDETAILS_ASSET_C_EMT_MODTD_DATE", "mandatory", "false");
            ifr.setStyle("SUBSEQ_CHECK_COLL_HL", "mandatory", "false");
            ifr.setStyle("DUE_EMT_DATE_COLL_HL", "mandatory", "false");
            ifr.setStyle("DUE_SUBSEQ_DATE_COLL_HL", "mandatory", "false");
            ifr.setStyle("ACTUAL_EMT_DATE_COLL_HL", "mandatory", "false");
            ifr.setStyle("ACTUAL_SUBSEQ_DATE_COLL_HL", "mandatory", "false");
        }
    }

	
}
 
 
