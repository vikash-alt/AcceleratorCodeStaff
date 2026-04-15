/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.papl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.newgen.dlp.commonobjects.bso.LoanEligibilityCheck;
import com.newgen.dlp.docgen.gen.GenerateDocument;
import com.newgen.dlp.integration.brm.BRMCommonRules;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryData;
import com.newgen.dlp.integration.cbs.Ammortization;
import com.newgen.dlp.integration.cbs.CustomerAccountSummaryPAPL;
import com.newgen.dlp.integration.cbs.Demographic;
import com.newgen.dlp.integration.cbs.EMICalculator;
import com.newgen.dlp.integration.common.APIPreprocessor;
import com.newgen.dlp.integration.fintec.ConsumerAPI;
import com.newgen.dlp.integration.fintec.ExperianAPI;
import com.newgen.dlp.integration.nesl.EsignCommonMethods;
//import com.newgen.dlp.integration.cbs.SendEmail;
//import com.newgen.dlp.integration.cbs.SendSMS;
import com.newgen.dlp.integration.nesl.EsignIntegrationChannel;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.BRMSRules;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import static com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods.getDocumentInBase64Formate;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author m_gupta
 */
public class PAPLPortalCustomCode {

    PortalCommonMethods pcm = new PortalCommonMethods();
    CommonFunctionality cf = new CommonFunctionality();
    BRMSRules jsonBRMSCall = new BRMSRules();
    BRMCommonRules objbcr = new BRMCommonRules();
    LoanEligibilityCheck objlec = new LoanEligibilityCheck();

    public String autoPopulateAvailOfferData(IFormReference ifr, String value) {

        try {

            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

            //Added by Ahmed on 05-07-2024 for disabling the section based on NESL Triggered State
            EsignCommonMethods objEsign = new EsignCommonMethods();
            String triggeredStatus = objEsign.checkNESLTriggeredStatus(ifr);
            if (Integer.parseInt(triggeredStatus) > 0) {
                Log.consoleLog(ifr, "inside checkNESLTriggeredStatus: true");
                ifr.setStyle("P_PAPL_AVAILOFFER_SLIDER", "disable", "true");
                ifr.setStyle("P_PAPL_AVAILOFFER_READONLY2", "disable", "true");
                ifr.setStyle("P_PAPL_AVAILOFFER_BUTTONS2", "disable", "true");
                ifr.setStyle("rangeAvailOffer_Papl2_CustomControl1", "disable", "true");
                ifr.setStyle("rangeAvailOffer_Papl2_CustomControl2", "disable", "true");
            } else {
                Log.consoleLog(ifr, "inside autoPopulateAvailOfferData:");
                String currentStep = pcm.setGetPortalStepName(ifr, value);

                CustomerAccountSummaryPAPL CBS1 = new CustomerAccountSummaryPAPL();
                String status = CBS1.executeCustomerAccountSummary(ifr, processInstanceId);

                JSONParser parser = new JSONParser();
                JSONObject resultObj = (JSONObject) parser.parse(status);
                String apiStatus = resultObj.get("apiStatus").toString();
                Log.consoleLog(ifr, "apiStatus==>" + apiStatus);

                if (apiStatus.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnErrorcustmessage(ifr, apiStatus);
                }

                pcm.mImplApplicationNameRefrenceNum(ifr, "PAPL");
                pcm.mImplApplicationNameRefrenceNum_ProductTeamTesting(ifr);//Added for Product Team Testing.

                String mobileNumber = pcm.getMobileNumber(ifr);
                ifr.setValue("ApplicationNameD2", pcm.getCustomerName(ifr));
                String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
                Log.consoleLog(ifr, "schemeID:" + schemeID);
                if (!(mobileNumber.equalsIgnoreCase(""))) {

                    String getPreApprLoanQuery = ConfProperty.getQueryScript("getPreApprLoanQuery").replaceAll("#mobile_no#", mobileNumber);
                    List<List<String>> loanAmount = cf.mExecuteQuery(ifr, getPreApprLoanQuery, "Get PreApproved Details:");
                    String amount = "", tenure = "", roi = "";
                    if (loanAmount.size() > 0) {
                        amount = loanAmount.get(0).get(0);
                        tenure = loanAmount.get(0).get(2);
                        roi = loanAmount.get(0).get(3);
                        ifr.setValue("P_PAPL_LOAN_AMOUNT", amount);
                        ifr.setValue("P_PAPL_TENURE", tenure);
                        ifr.setValue("P_PAPL_RATEOFINTEREST", pcm.getROI(ifr, schemeID, ifr.getValue("AvailOffer_Papl2_combo1").toString()));
                    }
                    ifr.setStyle("P_PAPL_LOAN_AMOUNT", "readonly", "true");
                    ifr.setStyle("P_PAPL_TENURE", "readonly", "true");
                    ifr.setStyle("P_PAPL_RATEOFINTEREST", "readonly", "true");

                    String processingFee = pcm.getProcessingFee(ifr, schemeID, amount, " and A.FeeCode='CHR20'");
                    processingFee = pcm.mAccRoundOffvalue(ifr, processingFee);

                    ifr.setValue("P_PAPL_PROCESSINGFEE", processingFee);
                    ifr.setStyle("P_PAPL_PROCESSINGFEE", "readonly", "true");

                    String FrameSection = "AvailOffer";
                    EMICalculator e = new EMICalculator();
                    String EMIAmount = e.getEmiCalculatorInstallment(ifr, processInstanceId, amount, tenure, roi, FrameSection);

                    EMIAmount = pcm.mAccRoundOffvalue(ifr, EMIAmount);
                    ifr.setValue("P_PAPL_EMI", EMIAmount);
                    ifr.setStyle("P_PAPL_EMI", "readonly", "true");

                    String slidderValues = pcm.getSliderAmount(ifr);
                    if (slidderValues.equalsIgnoreCase("")) {
                        String setSliderValue = amount + "," + tenure + "," + currentStep;
                        pcm.mAccSetSliderValue(ifr, "", "", amount + "," + tenure);
                        return setSliderValue;
                    } else {
                        String slidderValuesarr[] = slidderValues.split("~");
                        String setSliderValue = slidderValuesarr[0] + "," + slidderValuesarr[1] + "," + currentStep;
                        pcm.mAccSetSliderValue(ifr, "", "", slidderValuesarr[0] + "," + slidderValuesarr[1]);
                        return setSliderValue;
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in autoPopulateAvailOfferData " + e);
            Log.errorLog(ifr, "Error occured in autoPopulateAvailOfferData " + e);
        }
        return "";
    }

    public String mClickAvailButton(IFormReference ifr) throws ParseException {
        Log.consoleLog(ifr, "Inside mClickAvailButton");

        // String cb = mCallBureau(ifr, "EX", "", "10000");
        JSONObject message = new JSONObject();
        try {
            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            //commented by sravani , terms and conditions is not required in avail offer screen
            /* String checkboxValue = ifr.getValue("AvailOffer_Papl2_checkbox1").toString();
            if (checkboxValue.equalsIgnoreCase("false")) {
                message.put("showMessage", cf.showMessage(ifr, "P_PAPL_AVAILOFFER", "error", "Kindly accept Terms & Condition"));
                return message.toString();
            }*/

            String roi = ifr.getValue("P_PAPL_RATEOFINTEREST").toString();
            String tenure;
            String loanAmount;
            String slidderValues = pcm.getSliderAmount(ifr);
            if (slidderValues.equalsIgnoreCase("")) {
                return pcm.returnError(ifr);
            } else {
                String slidderValuesarr[] = slidderValues.split("~");
                loanAmount = slidderValuesarr[0];
                tenure = slidderValuesarr[1];
            }

            //Added by Ahmed for Testing
            //Function modified by Ahmed on 12-06-2024 for Aadhar Vault Implementation
            CustomerAccountSummaryPAPL CBS1 = new CustomerAccountSummaryPAPL();
            String status = CBS1.executeCustomerAccountSummary(ifr, processInstanceId);

            if (status.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                return pcm.returnErrorcustmessage(ifr, status);
            }

            JSONParser parser = new JSONParser();
            JSONObject resultObj = (JSONObject) parser.parse(status);
            String apiStatus = cf.getJsonValue(resultObj, "apiStatus");
            Log.consoleLog(ifr, "apiStatus==>" + apiStatus);

            if (apiStatus.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorcustmessage(ifr, apiStatus);
            }
            /*
            if (status.contains(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
            }*/

            JSONParser jp = new JSONParser();
            JSONObject obj1 = (JSONObject) jp.parse(status);
            if (cf.getJsonValue(obj1, "apiStatus").contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorcustmessage(ifr, apiStatus);
            }
            String aadharNo = cf.getJsonValue(obj1, "aadharNo");
            Log.consoleLog(ifr, "aadharNo:" + aadharNo);
            String APIResponse = mGetAPIData(ifr);
            if (APIResponse.contains(RLOS_Constants.ERROR)) {
                return pcm.returnErrorcustmessage(ifr, apiStatus);
            }
            JSONObject obj = (JSONObject) jp.parse(APIResponse);
            String DOB = cf.getJsonValue(obj, "DOB");
            String Age = cf.getJsonValue(obj, "Age");
            String writeOffPresent = cf.getJsonValue(obj, "writeOffPresent");
            // String canaraBudgetProductCode = cf.getJsonValue(obj, "canaraBudgetProductCode");
            String count = cf.getJsonValue(obj, "count");
            String PAPLExist = cf.getJsonValue(obj, "PAPLExist");
            String Classification = cf.getJsonValue(obj, "Classification");
            String totalExp = cf.getJsonValue(obj, "totalExp");
            String grossSalary = cf.getJsonValue(obj, "grossSalary");
            String netSalar = cf.getJsonValue(obj, "netSalar");

            String knockoffDecision = PAPLKnockOff(ifr, DOB, Age, count, PAPLExist,
                    Classification, totalExp, grossSalary, writeOffPresent,
                    netSalar, roi, tenure, loanAmount, aadharNo);
            Log.consoleLog(ifr, "knockoffDecision:" + knockoffDecision);

            if (knockoffDecision.contains(RLOS_Constants.ERROR)) {
                return pcm.returnError(ifr);
                //   } else if ((knockoffDecision.contains("Approve")) || (knockoffDecision.contains("NA"))) {
            } else if (knockoffDecision.contains("Approve")) {

                Log.consoleLog(ifr, "knockoffDecision:" + knockoffDecision);
                String finalEligibility[] = knockoffDecision.split("~");
                String eligibleAmount = finalEligibility[1];

                String Query = ConfProperty.getQueryScript("PORTALUPDATESLAT").replaceAll("#loanAmount#",
                        eligibleAmount).replaceAll("#tenure#", tenure).replaceAll("#PID#", processInstanceId);
                Log.consoleLog(ifr, "Update LoanAmount And Tenure :" + Query);
                ifr.saveDataInDB(Query);

                String queryCheckbox = ConfProperty.getQueryScript("PORTALCHECKBOXVALUEAVAIL").replaceAll("#WINAME#", processInstanceId).replaceAll("#checkbox1#", ifr.getValue("AvailOffer_Papl2_checkbox1").toString());
                Log.consoleLog(ifr, "queryCheckbox " + queryCheckbox);
                ifr.saveDataInDB(queryCheckbox);

                pcm.setValueInBackOffice(ifr, "Avail_Offer", "Avail_Offer_Backoffice_fields_SetValue_Backoffice");
                ifr.setValue("QNL_LOS_TRAN_PAPL_AVAILOFFER.AMOUNTSLIDER", loanAmount);
                ifr.setValue("QNL_LOS_TRAN_PAPL_AVAILOFFER.TENURESLIDER", tenure);

                String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
                Log.consoleLog(ifr, "schemeID:" + schemeID);
                String processingFee = pcm.getProcessingFee(ifr, schemeID, eligibleAmount, " and A.FeeCode='CHR20'");
                processingFee = pcm.mAccRoundOffvalue(ifr, processingFee);

                //  CBS_EMICalculator e = new CBS_EMICalculator();
                //  String EMIAmount = e.emiCalculatorInstallmentAPI(ifr, eligibleAmount, tenure, roi, "AvailOffer", "PAPL");
                EMICalculator e = new EMICalculator();
                String EMIAmount = e.getEmiCalculatorInstallment(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), eligibleAmount, tenure, roi, "AvailOffer");

                EMIAmount = pcm.mAccRoundOffvalue(ifr, EMIAmount);

                ifr.setValue("QNL_LOS_TRAN_PAPL_FINALELIGIBILITY.LOANAMOUNT", eligibleAmount);
                ifr.setValue("QNL_LOS_TRAN_PAPL_FINALELIGIBILITY.TENURE", tenure);
                ifr.setValue("QNL_LOS_TRAN_PAPL_FINALELIGIBILITY.RATEOFINTEREST", roi);
                ifr.setValue("QNL_LOS_TRAN_PAPL_FINALELIGIBILITY.EMI", EMIAmount);
                ifr.setValue("QNL_LOS_TRAN_PAPL_FINALELIGIBILITY.PROCESSINGFEE", processingFee);

                message.put("NavigationNextClick", "true");
                Log.consoleLog(ifr, "message:" + message);
                return message.toString();
            } else {
                //  message.put("showMessage", cf.showMessage(ifr, "P_PAPL_AVAILOFFER", "error", "You are Not Eligible!"));
                //CBS_SMS.papl_SendSMS(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), "", "", "RETAIL", "4");
                message.put("showMessage", cf.showMessage(ifr, "P_PAPL_AVAILOFFER", "error", "Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank!"));
                //Added by Sravani on 05-06-2024 for Application Resuming Status
                String upQuery = "UPDATE LOS_WIREFERENCE_TABLE SET APPLICATION_STATUS='REJECTED' WHERE WINAME='" + processInstanceId + "'";
                Log.consoleLog(ifr, "upQuery:" + upQuery);
                ifr.saveDataInDB(upQuery);

                //Added by Ahmed on 10-05-2024 triggering MailContent from DLPCommonObjects=========
                String bodyParams = "";
                String subjectParams = "";
                String fileName = "";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
                String fileContent = "";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
                pcm.triggerCCMAPIs(ifr, processInstanceId, "PAPL", "4", bodyParams, subjectParams, fileName, fileContent);
                //Ended by Ahmed on 10-05-2024 triggering MailContent from DLPCommonObjects=========

                /*
                Email em = new Email();
                SMS sms = new SMS();
                WhatsApp wh = new WhatsApp();

                String emailId = pcm.getCurrentEmailId(ifr, "PAPL", "");
                String mobileNumber = pcm.getMobileNumber(ifr);
                em.sendEmail(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), emailId, "", "RETAIL", "RETAIL", "4");
                sms.sendSMS(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), mobileNumber, "", "", "RETAIL", "4");
                wh.sendWhatsAppMsg(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(), mobileNumber, "", "", "RETAIL", "4");
                 */
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Inside mImpOnClickAvailButton" + e);
            Log.errorLog(ifr, "Inside mImpOnClickAvailButton" + e);
        }
        return "";
    }

    public String PAPLKnockOff(IFormReference ifr, String DOB, String Age,
            String count, String PAPLExist, String Classification, String totalExposure, String grossInput,
            String writeOffPresent, String netSal, String roi, String tenure, String loanAmount, String aadharNo) throws ParseException, Exception {

        String ProductCode = pcm.mGetProductCode(ifr);
        Log.consoleLog(ifr, "ProductCode:" + ProductCode);

        String decision = mCallBRMSKnockOff(ifr, "KNOCKOFF_PAPLTEST", Age + "," + PAPLExist + "," + count
                + "," + grossInput + "," + Classification + "," + ProductCode + "," + totalExposure
                + "," + DOB + "," + writeOffPresent, "totalknockoff_output");

        Log.consoleLog(ifr, "decision1/KNOCKOFF_PAPLTEST" + decision);
        if (decision.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        } else if (decision.equalsIgnoreCase("Approve")) {
            String cb = mCallBureau(ifr, "CB", aadharNo, loanAmount);
            if (cb.contains(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }
            decision = objbcr.checkCICScore(ifr, "PL", "PAPL", "CB", "B");
            //mCheckCIBILScoreknockOff(ifr, "CB", ProductCode);//Code modified for CIBIL Score check alone

            Log.consoleLog(ifr, "decision1/CB" + decision);
            if (decision.contains(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
                // } else if ((decision.equalsIgnoreCase("Approve")) || (decision.equalsIgnoreCase("NA"))) {
            } else if (decision.equalsIgnoreCase("Approve")) {
                Log.consoleLog(ifr, "decision2:" + decision);

                String EX = mCallBureau(ifr, "EX", aadharNo, loanAmount);
                if (EX.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                }

                decision = objbcr.checkCICScore(ifr, "PL", "PAPL", "EX", "B");

                //   mCheckCIBILScoreknockOff(ifr, "EX", ProductCode);//Code modified for CIBIL Score check alone
                if (decision.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR;
                    //  } else if ((decision.equalsIgnoreCase("Approve")) || (decision.equalsIgnoreCase("NA"))) {
                } else if (decision.equalsIgnoreCase("Approve")) {

                    decision = objbcr.checkCICEligibility(ifr, ProductCode, "PAPL");
                    //mCheckBureauknockOff(ifr, ProductCode);

                    Log.consoleLog(ifr, "decision1/EX" + decision);
                    if (decision.contains(RLOS_Constants.ERROR)) {
                        return RLOS_Constants.ERROR;
                        // } else if ((decision.equalsIgnoreCase("Approve")) || (decision.equalsIgnoreCase("NA"))) {
                    } else if (decision.equalsIgnoreCase("Approve")) {

                        Log.consoleLog(ifr, "decision3:" + decision);
                        HashMap hm = new HashMap();
                        hm.put("netsal", String.valueOf(netSal));

                        //  String returnresponse = getMinBureauScore(ifr);//Commented by Ahmed on 03-06-2024 for Logic Change on Total NON EMI AMount
                        CommonMethods cm = new CommonMethods();
                        String returnresponse = cm.getMaxTotalEMIAmount(ifr);
                        if (returnresponse.contains(RLOS_Constants.ERROR)) {
                            return RLOS_Constants.ERROR;
                        }
                        String[] bSplitter = returnresponse.split("-");
                        String bureauType = bSplitter[0];
                        String totalEMIAmount = bSplitter[1];

                        Log.consoleLog(ifr, "bureauType===========>" + bureauType);
                        Log.consoleLog(ifr, "decisibureauScoreon==>" + totalEMIAmount);

                        //Commented by Ahmed on 03-06-2024 for Logic Change on Total NON EMI AMount
//                        //Added by Ahmed on 05-02-2024 for Cibil Obligation
//                        String Query = "SELECT TOTEMIAMOUNT FROM LOS_CAN_IBPS_BUREAUCHECK "
//                                + "WHERE PROCESSINSTANCEID='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "' "
//                                + "AND BUREAUTYPE='" + bureauType + "'";
//                        List< List< String>> Result = ifr.getDataFromDB(Query);
//                        Log.consoleLog(ifr, "#Result===>" + Result.toString());
//                        String cibiloblig = "0";
//                        if (Result.size() > 0) {
//                            cibiloblig = Result.get(0).get(0);
//                            if (cibiloblig.equalsIgnoreCase("No data Found")) {
//                                cibiloblig = "0";
//                            }
//                        }
//                        //Ened by Ahmed on 05-02-2024 for Cibil Obligation
                        hm.put("cibiloblig", totalEMIAmount);
                        hm.put("tenure", String.valueOf(tenure));
                        hm.put("roi", String.valueOf(roi));
                        hm.put("loanmax", String.valueOf("1000000"));
                        hm.put("loancap", String.valueOf("1000000"));
                        hm.put("loanoffer", String.valueOf(loanAmount));
                        hm.put("deduction", "0");
                        hm.put("gross", String.valueOf(netSal));
                        //String finalelig = pcm.getAmountForEligibilityCheck(ifr, hm);
                        String productCode = pcm.mGetProductCode(ifr);
                        Log.consoleLog(ifr, "ProductCode:" + productCode);
                        String subProductCode = pcm.mGetSubProductCode(ifr);
                        Log.consoleLog(ifr, "subProductCode:" + subProductCode);
                        // Added by prakash 02-05-2024  EligibilityCheck
                        String finalelig = objlec.getAmountForEligibilityCheck(ifr, productCode, subProductCode, hm);

                        Log.consoleLog(ifr, "finalelig===>" + finalelig);
                        if (finalelig.contains(RLOS_Constants.ERROR)) {
                            return RLOS_Constants.ERROR;
                        }

                        Log.consoleLog(ifr, "finalelig:" + finalelig);

                        decision = mCallBRMSKnockOff(ifr, "ELIGIBILITY_PAPLTEST", ProductCode + "," + finalelig, "validitycheckop");

                        Log.consoleLog(ifr, "decision:ELIGIBILITY_PAPLTEST" + decision);
                        if (decision.contains(RLOS_Constants.ERROR)) {
                            return RLOS_Constants.ERROR;
                        } else if (decision.equalsIgnoreCase("Eligible")) {
                            return "Approve~" + finalelig;
                        } else {
                            return "Reject";
                        }

                    }

                    // decision="Approve";
                } else {
                    return "Reject";
                }
            } else {
                return "Reject";
            }
        } else {
            return "Reject";
        }
        return "";
    }

    public String mCallBureau(IFormReference ifr, String BureauType, String aadharNo, String loanAmount) throws ParseException {
        Log.consoleLog(ifr, "Inside mCallBureau:");
        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String CountQuery;
        if (BureauType.equalsIgnoreCase("CB")) {
            CountQuery = "SELECT COUNT(*) FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BUREAUTYPE='CB'";
        } else {
            CountQuery = "SELECT COUNT(*) FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BUREAUTYPE='EX'";
        }
        Log.consoleLog(ifr, "CountQuery==>" + CountQuery);
        List< List< String>> Result = ifr.getDataFromDB(CountQuery);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());
        String Count = "0";
        if (Result.size() > 0) {
            Count = Result.get(0).get(0);
        }

        if (Integer.parseInt(Count) > 0) {
            return Count;
        }
        Log.consoleLog(ifr, "#1");
        //Code Logic Modified for Experian
        if (BureauType.equalsIgnoreCase("EX")) {
            ExperianAPI EXP = new ExperianAPI();
            String BureauScore = EXP.getExperianCIBILScore2(ifr, ProcessInstanceId, aadharNo, "PAPL", loanAmount, "B");
            Log.consoleLog(ifr, "BureauScore From Experian===>" + BureauScore);
            if (BureauScore.contains(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }
        } else if (BureauType.equalsIgnoreCase("CB")) {
            ConsumerAPI CB1 = new ConsumerAPI();
            String BureauScore = CB1.getConsumerCIBILScore(ifr, "PAPL", loanAmount, aadharNo, "B");
            Log.consoleLog(ifr, "BureauScore From Transunion==>" + BureauScore);
            if (BureauScore.contains(RLOS_Constants.ERROR)) {
                return RLOS_Constants.ERROR;
            }
        }

        return "";
    }
// Commented by Monesh on 13/04/24

    /*
    public String mCheckCIBILScoreknockOff(IFormReference ifr, String BureauType, String ProductCode) {

        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String CountQuery;
        if (BureauType.equalsIgnoreCase("CB")) {
            CountQuery = "SELECT EXP_CBSCORE FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BUREAUTYPE='CB'";
        } else {
            CountQuery = "SELECT EXP_CBSCORE FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BUREAUTYPE='EX'";
             String  CountQuery1 = "SELECT EXP_CBSCORE FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' and BUREAUTYPE='CB'";
            List<List<String>> Result1 = cf.mExecuteQuery(ifr, CountQuery1, "Count Query: cibil Check for Experian");
            cibiscore=  Result1.get(0).get(0);
        }
        Log.consoleLog(ifr, "CountQuery==>" + CountQuery);
        List<List<String>> Result = cf.mExecuteQuery(ifr, CountQuery, "Count Query:");
        String score;

        if (Result.size() > 0) {
            score = Result.get(0).get(0);
        } else {
            return RLOS_Constants.ERROR;
        }
        String decision;
        //Modifed by monesh on 12/04/2024 for handling CIC Immune Case

          if (!score.equalsIgnoreCase("I"))
        {
        if (BureauType.equalsIgnoreCase("CB")) {
            decision = mCallBRMSKnockOff(ifr, "CIBILTEST", score + "," + ProductCode, "cibil_output");
        } else {
            decision = mCallBRMSKnockOff(ifr, "EXPERIANTEST", ProductCode + "," + score + "," + score, "experian_output");
        }
        }
          else{
            decision="Approve";
        }
        Log.consoleLog(ifr, "decision==>" + decision);
        if (decision.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        } else {
            return decision;
        }
    }
     */
 /* public String mCheckBureauknockOff(IFormReference ifr, String ProductCode) {

        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String returnresponse = getMinBureauScore(ifr);
        if (returnresponse.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        }
        String[] bSplitter = returnresponse.split("-");
        String bureauType = bSplitter[0];
        String bureauScore = bSplitter[1];

        Log.consoleLog(ifr, "bureauType===========>" + bureauType);
        Log.consoleLog(ifr, "decisibureauScoreon==>" + bureauScore);

        String CountQuery = "SELECT EXP_CBSCORE,CICNPACHECK,CICOVERDUE,WRITEOFF FROM "
                + "LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "' "
                + "and BUREAUTYPE='" + bureauType + "'";

        List<List<String>> Result = cf.mExecuteQuery(ifr, CountQuery, "Count Query:");
        String score;
        String CICNPACHECK;
        String CICOVERDUE;
        String WRITEOFF;
        if (Result.size() > 0) {
            score = Result.get(0).get(0);
         //Modifed by monesh on 12/04/2024 for handling CIC Immune Case

                if (!score.equalsIgnoreCase("I"))
        {
            CICNPACHECK = Result.get(0).get(1);
            CICOVERDUE = Result.get(0).get(2);
            WRITEOFF = Result.get(0).get(3);
        }
          else {
            CICNPACHECK ="No";
            CICOVERDUE = "0";
            WRITEOFF = "No";
            }
        } else {
            return RLOS_Constants.ERROR;
        }
            
        String npaDecision = mCallBRMSKnockOff(ifr, "KNOCKOFF_CIBIL_EXPTEST", CICOVERDUE + "," + CICNPACHECK
                + "," + ProductCode + "," + WRITEOFF, "totalknockoff_output");
        if (npaDecision.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        } else if ((npaDecision.equalsIgnoreCase("Approve")) ) {
            return "Approve";
        } else {
            return "Reject";
        }

//        String decision;
//        if (BureauType.equalsIgnoreCase("CB")) {
//            decision = mCallBRMSKnockOff(ifr, "CIBILTEST", score + "," + ProductCode, "cibil_output");
//        } else {
//            decision = mCallBRMSKnockOff(ifr, "EXPERIANTEST", ProductCode + "," + score + "," + score, "experian_output");
//        }
        //Added by Ahmed..
//        Log.consoleLog(ifr, "decision==>" + decision);
//
//        if (decision.contains(RLOS_Constants.ERROR)) {
//            return RLOS_Constants.ERROR;
//        } else if ((decision.equalsIgnoreCase("Approve")) || (decision.equalsIgnoreCase("NA"))) {
//
//            Log.consoleLog(ifr, "#CIBIL Decision Not Approved/NA");
//
//            String bureauScore = getMinBureauScore(ifr);
//            if (bureauScore.equalsIgnoreCase("")) {
//                return RLOS_Constants.ERROR;
//            }
//
//            String bureauSplitter[] = bureauScore.split(",");
//            Log.consoleLog(ifr, "BureauType==>" + bureauSplitter[0]);
//            Log.consoleLog(ifr, "BureauType==>" + bureauSplitter[1]);
//
//            String npaDecision = mCallBRMSKnockOff(ifr, "KNOCKOFF_CIBIL_EXPTEST", CICOVERDUE + "," + CICNPACHECK
//                    + "," + ProductCode + "," + WRITEOFF, "totalknockoff_output");
//            if (npaDecision.contains(RLOS_Constants.ERROR)) {
//                return RLOS_Constants.ERROR;
//            } else if ((npaDecision.equalsIgnoreCase("Approve")) || (npaDecision.equalsIgnoreCase("NA"))) {
//                return "Approve";
//            } else {
//                return "Reject";
//            }
//        } else {
//            return "Reject";
//        }
    }
     */
    public String mCallBRMSKnockOff(IFormReference ifr, String RuleName, String values, String ValueTag) {
        JSONObject result = jsonBRMSCall.executeLOSBRMSRule(ifr, RuleName, values, ValueTag);
        Log.consoleLog(ifr, "BRMS Result:" + result);
        if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
            String cibilOutput = cf.getJsonValue(result, ValueTag);
            Log.consoleLog(ifr, cibilOutput);
            return cibilOutput;
        } else {
            Log.consoleLog(ifr, "Error:" + AcceleratorConstants.TRYCATCHERRORBRMS);
            return "ERROR";
        }
    }

    public void autoPopulateFinalEligibilityData(IFormReference ifr, String value) {
        try {
            Log.consoleLog(ifr, "autoPopulateFinalEligibilityData " + value);
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            pcm.setGetPortalStepName(ifr, value);
            pcm.mImplApplicationNameRefrenceNum(ifr, "PAPL");

            String queryCheckBox = ConfProperty.getQueryScript("PORTALCHECKBOXEE").replaceAll("#WINAME#", PID);
            Log.consoleLog(ifr, "queryCheckBox : " + queryCheckBox);
            List<List<String>> listbox = ifr.getDataFromDB(queryCheckBox);
            if (!listbox.isEmpty()) {
                String checkBox = listbox.get(0).get(0);
                if (checkBox.equalsIgnoreCase("true")) {
                    ifr.setValue("Portal_RB_Final_PAPL_1", "true");
                    ifr.setValue("Portal_RB_Final_PAPL_2", "true");
                }
            }
            ifr.setValue("P_PAPL_CUSTOMERNAME2", pcm.getCustomerName(ifr));
            String query = "select LoanAmount,Tenure,rateofInterest,EMI,Processingfee from LOS_TRAN_PAPL_FINALELIGIBILITY where WIName='" + PID + "'";
            cf.mGetLabelAndSetToField(ifr, query, "Get Data:", "P_PAPL_FE_LOANAMOUNT,P_PAPL_FE_TENURE,P_PAPL_FE_ROI"
                    + ",P_PAPL_FE_EMI,P_PAPL_FE_PROCESSINGFEE");
            ifr.setStyle("navigationBackBtn", "visible", "true");

            //=======================NESL F2F Code============================//
            String neslMode = pcm.getNESLModeQuery(ifr, "PAPL");
            if (neslMode.equalsIgnoreCase("Y")) {
                ifr.setStyle("papl_final_eligi2_button3", "visible", "true");
                ifr.setStyle("papl_final_eligi2_button4", "visible", "true");
            } else {
                ifr.setStyle("papl_final_eligi2_button3", "visible", "false");
                ifr.setStyle("papl_final_eligi2_button4", "visible", "false");
            }
            //=======================NESL F2F Code============================//

        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in autoPopulateFinalEligibilityData " + e);
            Log.errorLog(ifr, "Error occured in autoPopulateFinalEligibilityData " + e);
        }
    }

    public String populateFinalEligibilityDetail(IFormReference ifr, String control, String event, String value) {

        Log.consoleLog(ifr, "populateFinalEligibilityDetail : ");
        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String checkbox1 = ifr.getValue("Portal_RB_Final_PAPL_1").toString();
        String checkbox2 = ifr.getValue("Portal_RB_Final_PAPL_2").toString();
        if (checkbox1.equalsIgnoreCase("true") && (checkbox2.equalsIgnoreCase("true"))) {
            String queryCheckbox = ConfProperty.getQueryScript("PORTALCHECKBOXVALUE").replaceAll("#WINAME#", PID).replaceAll("#checkbox2#", checkbox2);
            Log.consoleLog(ifr, "queryCheckbox " + queryCheckbox);
            ifr.saveDataInDB(queryCheckbox);
        } else {
            JSONObject message = new JSONObject();
            message.put("showMessage", cf.showMessage(ifr, "P_PAPL_AVAILOFFER", "error", "Kindly Select Both Disclaimer and Consent!"));
            return message.toString();
        }

        try {
            String neslMode = pcm.getNESLModeQuery(ifr, "PAPL");
            Log.consoleLog(ifr, "neslMode===>" + neslMode);

            if (neslMode.equalsIgnoreCase("Y")) {
//
                EsignCommonMethods ecm = new EsignCommonMethods();
                String returnMessage = ecm.getNESLWorkflowStatus(ifr);
                Log.errorLog(ifr, "returnMessage:" + returnMessage);

                if (!returnMessage.equalsIgnoreCase("SUCCESS")) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "P_PAPL_AVAILOFFER", "error", returnMessage));
                    return message.toString();
                } else {
                    return "";
                }
                //return returnMessage;

            } else {

                Ammortization AMR = new Ammortization();
                String returnMessage = AMR.ExecuteCBS_Ammortization(ifr, PID, ifr.getValue("P_PAPL_FE_LOANAMOUNT").toString(),
                        ifr.getValue("P_PAPL_FE_TENURE").toString(), "PAPL");
                if (returnMessage.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnErrorcustmessage(ifr, returnMessage);
                }

                mGenerateDoc(ifr);

                EsignIntegrationChannel NESL = new EsignIntegrationChannel();
               // returnMessage = NESL.redirectNESLRequest(ifr, "PAPL", "eStamping");
                Log.errorLog(ifr, "returnMessage:" + returnMessage);

                if (returnMessage.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnErrorHold(ifr);
                } else if (returnMessage.contains("showMessage")) {
                    JSONParser jp = new JSONParser();
                    JSONObject obj = (JSONObject) jp.parse(returnMessage);
                    obj.put("eflag", "false");
                    return obj.toString();
                }

            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
            return pcm.returnErrorHold(ifr);
        }

        //=======================NESL F2F Ends here===============================
        return "";

    }

    public void mGenerateDoc(IFormReference ifr) {
        docGen(ifr, "KFS", "PAPL");
        docGen(ifr, "LoanAggrement", "PAPL");
        docGen(ifr, "SanctionLetter", "PAPL");
        docGen(ifr, "RepaymentLetter", "PAPL");
        docGen(ifr, "ProcessNote", "PAPL");
        docGen(ifr, "LoanApplication", "PAPL");
        docGen(ifr, "NF_991", "PAPL"); // Added by Praveen on 27-03-2024 (For Doc Generation)
    }

    public void docGen(IFormReference ifr, String docid, String journeyName) {
        try {
            Log.consoleLog(ifr, "inside docGen  : ");
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode userNode = objectMapper.createObjectNode();
            Log.consoleLog(ifr, "userNode: " + userNode.toString());
            userNode.put("Mode", "SINGLE");
            userNode.put("DocID", docid);
            userNode.put("callFrom", "Backoffice");
            userNode.put("journey", "Pre-Approved Personal Loan");
            userNode.put("journey", journeyName);
            userNode.put("referenceKey", ifr.getObjGeneralData().getM_strProcessInstanceId());
            userNode.put("Activity", ifr.getActivityName());
            userNode.put("Identifier", "N");
            userNode.put("RPSchedule", "N");
            userNode.put("TypeOfFecility", "Term Loan");
            userNode.put("InterestRate", 8.5);
            userNode.put("LoanTerm", "Y");
            userNode.put("LoanAmount", 15000);
            GenerateDocument dc = new GenerateDocument();
            JsonNode userDoc = dc.executeDocGenerator(ifr, userNode);
            Log.consoleLog(ifr, "userDoc: " + userDoc.toString());
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in docGen " + e);
            Log.errorLog(ifr, "Error occured in docGen " + e);
        }

    }

    public void autoPopulateRecieveMoneyData(IFormReference ifr, String value) {
        try {
            Log.consoleLog(ifr, "inside autoPopulateRecieveMoneyData : " + value);
            pcm.setGetPortalStepName(ifr, value);
            pcm.mImplApplicationNameRefrenceNum(ifr, "PAPL");
            ifr.setStyle("finalSubmitBtn", "visible", "false");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = "select LoanAmount,Tenure,rateofInterest,EMI,Processingfee from LOS_TRAN_PAPL_FINALELIGIBILITY where WIName='" + PID + "'";
            cf.mGetLabelAndSetToField(ifr, query, "Get Data:", "P_PAPL_RM_LOANAMOUNT,P_PAPL_RM_TENURE,P_PAPL_RM_ROI"
                    + ",P_PAPL_RM_EMI,P_PAPL_RM_PROCESSINGFEE");

            ifr.setStyle("P_PAPL_RM_ROI", "readonly", "true");
            ifr.setStyle("P_PAPL_RM_TENURE", "readonly", "true");
            ifr.setStyle("P_PAPL_RM_LOANAMOUNT", "readonly", "true");
            ifr.setStyle("P_PAPL_RM_EMI", "readonly", "true");
            ifr.setStyle("P_PAPL_RM_PROCESSINGFEE", "readonly", "true");

            ifr.setValue("P_PAPL_NETDISBURSEMENTNO", ifr.getValue("P_PAPL_RM_LOANAMOUNT").toString());
            String mobileNo = pcm.getMobileNumber(ifr);
            String accNoData = "select salaryacc_no from LOS_MST_PAPL where mobile_no ='" + mobileNo + "'";
            List<List<String>> loanAccNo = cf.mExecuteQuery(ifr, accNoData, "accNoData query : ");
            if (loanAccNo.size() > 0) {
                ifr.setValue("P_PAPL_SBACCOUNTNO", loanAccNo.get(0).get(0));
                ifr.setValue("P_PAPL_REPAYMENTNO", loanAccNo.get(0).get(0));
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in autoPopulateRecieveMoneyData " + e);
            Log.errorLog(ifr, "Error occured in autoPopulateRecieveMoneyData " + e);
        }
    }

    public void autoPopulateFinalScreenData(IFormReference ifr, String value) {
        try {
            Log.consoleLog(ifr, "inside autoPopulateFinalScreenData:");
            pcm.setGetPortalStepName(ifr, value);
            pcm.mImplApplicationNameRefrenceNum(ifr, "PAPL");
            Log.consoleLog(ifr, "Br1");
            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "Br2");
            String queryCheckBox = ConfProperty.getQueryScript("PORTALFEEDBACKCHECK").replaceAll("#WINAME#", processInstanceId);
            Log.consoleLog(ifr, "queryCheckBox : " + queryCheckBox);
            List<List<String>> listbox = ifr.getDataFromDB(queryCheckBox);
            if (!listbox.isEmpty()) {
                String checkBox = listbox.get(0).get(0);
                if (checkBox.equalsIgnoreCase("true")) {
                    ifr.setValue("USERFRIENDLY", "true");
                    ifr.setValue("FASTDISB", "true");
                    ifr.setValue("EASYPROCESS", "true");
                    ifr.setValue("OTHERREASON", "true");
                }
            }
            Log.consoleLog(ifr, "Br3");
            //

            String LoanAccountNumber_Query = "select LOAN_ACCOUNT_NO,SANCTION_AMOUNT,EMIDate,DISBDate from LOS_T_IBPS_LOAN_DETAILS "
                    + "where winame='" + processInstanceId + "' and rownum=1";
            List<List<String>> list1 = cf.mExecuteQuery(ifr, LoanAccountNumber_Query, "LoanAccountNumber_Query:");
            String LoanAccountNumber = "", LoanAmount = "", EMIDate = "", DISBDate = "";
            if (list1.size() > 0) {
                LoanAccountNumber = list1.get(0).get(0);
                LoanAmount = list1.get(0).get(1);
                EMIDate = list1.get(0).get(2);
                DISBDate = list1.get(0).get(3);
            }
            ifr.setValue("P_PAPL_FINALACCOUNTNO", LoanAccountNumber);
            ifr.setValue("P_PAPL_FINALLOANAMOUNT", LoanAmount);
            ifr.setValue("P_PAPL_SBLOANNO", getSalaryAccountNo(ifr));
            ifr.setValue("P_PAPL_FINAL_DATEOFDISBURSEMENT", DISBDate);
            ifr.setValue("P_PAPL_FINAL_EMIDATE", EMIDate);
            ifr.setStyle("navigationBackBtn", "visible", "false");

            String QueryRM = ConfProperty.getQueryScript("PORTALRMVALUEData1").replaceAll("#WINAME#", processInstanceId);
            List<List<String>> listRM = cf.mExecuteQuery(ifr, QueryRM, "QueryRM:");
            if (!listRM.isEmpty()) {
                String emi = listRM.get(0).get(0);
                emi = pcm.mAccRoundOffvalue(ifr, emi);
                ifr.setValue("P_PAPL_FS_EMI", emi);
            }

            String queryStatus = ConfProperty.getQueryScript("FINALSTATUS").replaceAll("#WINAME#", processInstanceId);
            Log.consoleLog(ifr, "queryStatus " + queryStatus);
            ifr.saveDataInDB(queryStatus);
            Log.consoleLog(ifr, "Br4");

            String docContent = pcm.downloadTestDocumentFromOD(ifr, processInstanceId);
            JSONParser parser = new JSONParser();
            JSONObject docContentObj = (JSONObject) parser.parse(docContent);

            Log.consoleLog(ifr, "File Attachment Sending Process....");
            String bodyParams = "";
            String subjectParams = "";
            String fileName = "SignedDocument.pdf";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
            String fileContent = docContentObj.get("docContent").toString();
            pcm.triggerCCMAPIs(ifr, processInstanceId, "PAPL", "5", bodyParams, subjectParams, fileName, fileContent);

            //Added by Sravani on 05-06-2024 for Application Resuming Status
            String upQuery = "UPDATE LOS_WIREFERENCE_TABLE SET APPLICATION_STATUS='COMPLETED' "
                    + "WHERE WINAME='" + processInstanceId + "'";
            Log.consoleLog(ifr, "upQuery:" + upQuery);
            ifr.saveDataInDB(upQuery);
            /*
            Email em = new Email();
            String emailId = pcm.getCurrentEmailId(ifr, "PAPL", "");
            em.sendEmail(ifr, ProcessInsanceId, emailId, "", "", "RETAIL", "5");
             */
            //
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in autoPopulateFinalScreenData " + e);
            Log.errorLog(ifr, "Error occured in autoPopulateFinalScreenData " + e);
        }
    }

    public void PopulateFinalScreenData(IFormReference ifr, String value) {
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String USERFRIENDLY = ifr.getValue("USERFRIENDLY").toString();
            String FASTDISB = ifr.getValue("FASTDISB").toString();
            String EASYPROCESS = ifr.getValue("EASYPROCESS").toString();
            String OTHERREASON = ifr.getValue("OTHERREASON").toString();
            if (USERFRIENDLY.equalsIgnoreCase("true") && (FASTDISB.equalsIgnoreCase("true") && (EASYPROCESS.equalsIgnoreCase("true") && (OTHERREASON.equalsIgnoreCase("true"))))) {
                String queryCheckbox = ConfProperty.getQueryScript("PORTALFEEDBACK").replaceAll("#WINAME#", PID).replaceAll("#USERFRIENDLY#", USERFRIENDLY).replaceAll("#FASTDISB#", FASTDISB).replaceAll("#EASYPROCESS#", EASYPROCESS).replaceAll("#OTHERREASON#", OTHERREASON);
                Log.consoleLog(ifr, "queryCheckbox " + queryCheckbox);
                ifr.saveDataInDB(queryCheckbox);
            }
        } catch (Exception e) {
            Log.errorLog(ifr, "Error occured in PopulateFinalScreenData " + e);
        }
    }

    //Function modified by Ahmed on 01-07-2024 for Preprocessor Validations
    public String CBSFinalScreenValidation(IFormReference ifr, String ProcessInsatnceId) {
        Log.consoleLog(ifr, "Entered into CBSFinalScreenValidation Screen...");
        try {
            APIPreprocessor objPreprocess = new APIPreprocessor();

            Log.consoleLog(ifr, "Checking details are available to execute CBS API`s..Please wait");
            String Query = "SELECT COUNT(*) FROM los_tran_papl_finaleligibility WHERE winame='" + ProcessInsatnceId + "'";
            List Result = cf.mExecuteQuery(ifr, Query, "Checking Data:");
            if (!Result.isEmpty()) {
                Log.consoleLog(ifr, "Details available to execute CBS API`s");
                String LoanAccountNumber = objPreprocess.execLoanAccountCreation(ifr, "PAPL");
                if (!(LoanAccountNumber.contains(RLOS_Constants.ERROR))) {
                    String CBSDisbursementEnquiry = objPreprocess.execDisbursementEnquiry(ifr, LoanAccountNumber, "PAPL");
                    if (!(CBSDisbursementEnquiry.contains(RLOS_Constants.ERROR))) {
                        String CBS_LoanDeduction = objPreprocess.execLoanDeduction(ifr, LoanAccountNumber, "PAPL");
                        if (!(CBS_LoanDeduction.contains(RLOS_Constants.ERROR))) {
                            String SessionId = objPreprocess.execComputeLoanSchedule(ifr, LoanAccountNumber, "PAPL");
                            if (!(SessionId.contains(RLOS_Constants.ERROR))) {
                                String CBS_GenerateLoanSchedule = objPreprocess.execGenerateLoanSchedule(ifr, LoanAccountNumber, SessionId, "PAPL");
                                if (!(CBS_GenerateLoanSchedule.contains(RLOS_Constants.ERROR))) {
                                    String CBS_SaveLoanSchedule = objPreprocess.execSaveLoanSchedule(ifr, LoanAccountNumber, SessionId, "PAPL");
                                    if (!(CBS_SaveLoanSchedule.contains(RLOS_Constants.ERROR))) {
                                        //CB7.CBS_RepaymentLoanSchedule(ifr, LoanAccountNumber);
                                        String CBS_BranchDisbursement = objPreprocess.execBranchDisbursement(ifr, LoanAccountNumber, "PAPL");
                                        if (!(CBS_BranchDisbursement.contains(RLOS_Constants.ERROR))) {
                                            String CBS_FundTransfer = objPreprocess.execFundTransfer(ifr,
                                                    getSalaryAccountNo(ifr), "PAPL", ifr.getValue("P_PAPL_RM_LOANAMOUNT").toString(), ifr.getValue("P_PAPL_RM_TENURE").toString());
                                            return CBS_FundTransfer;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception:" + e);
            Log.errorLog(ifr, "Exception:" + e);
        }
        return RLOS_Constants.ERROR;
    }

//    public String CBSFinalScreenValidation(IFormReference ifr, String ProcessInsatnceId) {
//        Log.consoleLog(ifr, "Entered into CBSFinalScreenValidation Screen...");
//        try {
//            CBS_LoanAccountCreation CB1 = new CBS_LoanAccountCreation();
//            CBS_DisbursementEnquiry CB2 = new CBS_DisbursementEnquiry();
//            CBS_LoanDeduction CB3 = new CBS_LoanDeduction();
//            CBS_ComputeLoanSchedule CB4 = new CBS_ComputeLoanSchedule();
//            CBS_GenerateLoanSchedule CB5 = new CBS_GenerateLoanSchedule();
//            CBS_SaveLoanSchedule CB6 = new CBS_SaveLoanSchedule();
//            CBS_BranchDisbursement CB7 = new CBS_BranchDisbursement();
//            CBS_FundTransfer CB8 = new CBS_FundTransfer();
//            CommonFunctionality cf = new CommonFunctionality();
//            Log.consoleLog(ifr, "Checking details are available to execute CBS API`s..Please wait");
//            String Query = "SELECT COUNT(*) FROM los_tran_papl_finaleligibility WHERE winame='" + ProcessInsatnceId + "'";
//            List Result = cf.mExecuteQuery(ifr, Query, "Checking Data:");
//            if (Result.size() > 0) {
//                Log.consoleLog(ifr, "Details available to execute CBS API`s");
//                String LoanAccountNumber = CB1.CBSLoanAccountCreation(ifr, ProcessInsatnceId, "PAPL");
//                if (!(LoanAccountNumber.contains(RLOS_Constants.ERROR))) {
//                    String CBSDisbursementEnquiry = CB2.CBSDisbursementEnquiry(ifr, LoanAccountNumber, ProcessInsatnceId, "PAPL");
//                    if (!(CBSDisbursementEnquiry.contains(RLOS_Constants.ERROR))) {
//                        String CBS_LoanDeduction = CB3.CBS_LoanDeduction(ifr, LoanAccountNumber, "PAPL");
//                        if (!(CBS_LoanDeduction.contains(RLOS_Constants.ERROR))) {
//                            String SessionId = CB4.CBS_ComputeLoanSchedule(ifr, LoanAccountNumber, "PAPL");
//                            if (!(SessionId.contains(RLOS_Constants.ERROR))) {
//                                String CBS_GenerateLoanSchedule = CB5.CBS_GenerateLoanSchedule(ifr, LoanAccountNumber, SessionId, "PAPL");
//                                if (!(CBS_GenerateLoanSchedule.contains(RLOS_Constants.ERROR))) {
//                                    String CBS_SaveLoanSchedule = CB6.CBS_SaveLoanSchedule(ifr, LoanAccountNumber, SessionId, "PAPL");
//                                    if (!(CBS_SaveLoanSchedule.contains(RLOS_Constants.ERROR))) {
//                                        //CB7.CBS_RepaymentLoanSchedule(ifr, LoanAccountNumber);
//                                        String CBS_BranchDisbursement = CB7.CBS_BranchDisbursement(ifr, LoanAccountNumber, "PAPL");
//                                        if (!(CBS_BranchDisbursement.contains(RLOS_Constants.ERROR))) {
//                                            String CBS_FundTransfer = CB8.CBS_FundTransfer(ifr,
//                                                    getSalaryAccountNo(ifr), "PAPL", ifr.getValue("P_PAPL_RM_LOANAMOUNT").toString(), ifr.getValue("P_PAPL_RM_TENURE").toString());
//                                            return CBS_FundTransfer;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "Exception:" + e);
//            Log.errorLog(ifr, "Exception:" + e);
//        }
//        return RLOS_Constants.ERROR;
//    }
    public String mGetAPIData(IFormReference ifr) throws ParseException {
        String CustomerId = pcm.getCustomerIDPAPL(ifr);
        Log.consoleLog(ifr, "CustomerId==>" + CustomerId);

        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Demographic API = new Demographic();
        String GetDemoGraphicData = API.getDemographic(ifr, ProcessInstanceId, CustomerId);
        Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
        if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
            return pcm.returnErrorcustmessage(ifr, GetDemoGraphicData);
        }
        String DOB;
        String Age;
        String writeOffPresent;
        if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
            return pcm.returnErrorcustmessage(ifr, GetDemoGraphicData);
        } else {
            JSONParser jsonparser = new JSONParser();
            JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
            DOB = cf.getJsonValue(obj, "DOB");
            Age = cf.getJsonValue(obj, "Age");
            writeOffPresent = cf.getJsonValue(obj, "writeOffPresent");
        }

        Advanced360EnquiryData API360 = new Advanced360EnquiryData();
        String response = API360.executeAdvanced360Inquiry(ifr, ProcessInstanceId, CustomerId, "PAPL");
        Log.consoleLog(ifr, "response==>" + response);
        String canaraBudgetProductCode = "";
        String count;
        String PAPLExist;
        String Classification;
        String totalExposure;
        if (response.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        } else {
            JSONParser jsonparser = new JSONParser();
            JSONObject obj = (JSONObject) jsonparser.parse(response);
            //canaraBudgetProductCode = cf.getJsonValue(obj, "ProductCode");
            count = cf.getJsonValue(obj, "count");
            PAPLExist = cf.getJsonValue(obj, "PAPLExist");
            Classification = cf.getJsonValue(obj, "Classification");
            totalExposure = cf.getJsonValue(obj, "totalExposure");
        }
        String mobileNumber = pcm.getMobileNumber(ifr);
        String query = "select NETSalary from LOS_MST_PAPL where mobile_no = '" + mobileNumber + "'";
        List<List<String>> result = cf.mExecuteQuery(ifr, query, "Fetching NetAmount");
        String netSalary = "";
        if (result.size() > 0) {
            netSalary = result.get(0).get(0);
        }
        BigDecimal netSal = pcm.mCheckBigDecimalValue(ifr, netSalary);
        String netSalPercentage = pcm.getConstantValue(ifr, "LOANELIGIBILITY", "NETSALPERCENT");
        BigDecimal grossSal = (netSal.divide(new BigDecimal(netSalPercentage), 2, RoundingMode.HALF_UP)).multiply(new BigDecimal("100"));
        String netSalar = String.valueOf((int) Float.parseFloat(String.valueOf(netSal)));
        Log.consoleLog(ifr, "netSalar:" + netSalar);

        String totalExp = String.valueOf((int) Float.parseFloat(String.valueOf(totalExposure)));
        Log.consoleLog(ifr, "totalExp:" + totalExp);

        String grossSalary = String.valueOf((int) Float.parseFloat(String.valueOf(grossSal)));
        Log.consoleLog(ifr, "grossSalary:" + grossSalary);
        JSONObject obj = new JSONObject();
        obj.put("DOB", DOB);
        obj.put("Age", Age);
        obj.put("writeOffPresent", writeOffPresent);
        //obj.put("canaraBudgetProductCode", canaraBudgetProductCode);
        obj.put("count", count);
        obj.put("PAPLExist", PAPLExist);
        obj.put("Classification", Classification);
        obj.put("netSalar", netSalar);
        obj.put("totalExp", totalExp);
        obj.put("grossSalary", grossSalary);
        return obj.toString();
    }

    public String getSalaryAccountNo(IFormReference ifr) {
        String MOBILENUMBER = pcm.getMobileNumber(ifr);
        String accNoData = "select salaryacc_no from LOS_MST_PAPL where mobile_no ='" + MOBILENUMBER + "'";
        List<List<String>> loanAccNo = cf.mExecuteQuery(ifr, accNoData, "accNoData:");
        if (loanAccNo.size() > 0) {
            return loanAccNo.get(0).get(0);
        }
        return "";
    }

    private String getMinBureauScore(IFormReference ifr) {

        String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String bureauChkQuery = "SELECT EXP_CBSCORE, bureautype\n"
                + "FROM LOS_CAN_IBPS_BUREAUCHECK\n"
                + "WHERE processinstanceid = '" + ProcessInstanceId + "'\n"
                + "AND EXP_CBSCORE = (\n"
                + "    SELECT MIN(EXP_CBSCORE)\n"
                + "    FROM LOS_CAN_IBPS_BUREAUCHECK\n"
                + "    WHERE processinstanceid = '" + ProcessInstanceId + "'\n"
                + ")";
        Log.consoleLog(ifr, "bureauChkQuery:" + bureauChkQuery);
        //  List<List<String>> bureauChk = cf.mExecuteQuery(ifr, bureauChkQuery, "bureauChkQuery:");
        List< List< String>> Result = ifr.getDataFromDB(bureauChkQuery);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());

        if (Result.size() > 0) {
            String bureauScore = Result.get(0).get(0);
            String bureauType = Result.get(0).get(1);
            return bureauType + "-" + bureauScore;
        }
        //L

        return RLOS_Constants.ERROR;
    }

    public String generateAndReviewDocument(IFormReference ifr, String productType) {

        Log.consoleLog(ifr, "#generateAndReviewDocument started...");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String docIndex = getReviewerDocIndex(ifr);
            Log.consoleLog(ifr, "docIndex==>" + docIndex);

            if (docIndex.equalsIgnoreCase("0")) {

                Log.consoleLog(ifr, "#If Loopp");
                String checkbox1 = ifr.getValue("Portal_RB_Final_PAPL_1").toString();
                String checkbox2 = ifr.getValue("Portal_RB_Final_PAPL_2").toString();
                if (checkbox1.equalsIgnoreCase("true") && (checkbox2.equalsIgnoreCase("true"))) {
                    String queryCheckbox = ConfProperty.getQueryScript("PORTALCHECKBOXVALUE").replaceAll("#WINAME#", PID).replaceAll("#checkbox2#", checkbox2);
                    Log.consoleLog(ifr, "queryCheckbox " + queryCheckbox);
                    ifr.saveDataInDB(queryCheckbox);
                } else {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "P_PAPL_AVAILOFFER", "error", "Kindly Select Both Disclaimer and Consent!"));
                    return message.toString();
                }

                Ammortization AMR = new Ammortization();
                String returnMessage = AMR.ExecuteCBS_Ammortization(ifr, PID, ifr.getValue("P_PAPL_FE_LOANAMOUNT").toString(),
                        ifr.getValue("P_PAPL_FE_TENURE").toString(), "PAPL");
                if (returnMessage.contains(RLOS_Constants.ERROR)) {
                    return pcm.returnErrorcustmessage(ifr, returnMessage);
                }

                mGenerateDoc(ifr);

                docIndex = getReviewerDocIndex(ifr);
                Log.consoleLog(ifr, "docIndex==>" + docIndex);

                String getDocContent = getDocBase64ReviewContent(ifr, docIndex);
                Log.consoleLog(ifr, "getDocContent==>" + getDocContent);
                return getDocContent;
            } else {
                Log.consoleLog(ifr, "#Elses Loopp");
                Log.consoleLog(ifr, "docIndex==>" + docIndex);
                String getDocContent = getDocBase64ReviewContent(ifr, docIndex);
                Log.consoleLog(ifr, "getDocContent==>" + getDocContent);
                return getDocContent;
                // return docIndex;
            }

            //return RLOS_Constants.SUCCESS;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/generateAndReviewDocument==>" + e);
            Log.errorLog(ifr, "Exception/generateAndReviewDocument==>" + e);
        }
        return RLOS_Constants.ERROR;
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public String getDocBase64ReviewContent(IFormReference ifr, String docIndex) {

        try {
            JSONObject json = null;
            JSONParser jsonparser = new JSONParser();
            JSONObject jobj = new JSONObject();

            // String url = "";
            String content, name, ext, response, status;

            String url = pcm.getConstantValue(ifr, "ODDOWNLOAD", "URL");
            Log.consoleLog(ifr, "#url===>" + url);

            //    String docIndex = "28714";
            String jsonin = "{\n" + "    \"NGOGetDocumentBDO\": {\n" + "        \"cabinetName\": \""
                    + ifr.getCabinetName() + "\",\n" + "        \"siteId\": \"1\",\n" + "        \"volumeId\": \"1\",\n"
                    + "        \"userName\": \"\",\n" + "        \"userPassword\": \"\",\n" + "        \"userDBId\": \""
                    + ifr.getObjGeneralData().getM_strDMSSessionId() + "\",\n" + "        \"locale\": \"en_US\",\n"
                    + "        \"passAlgoType\": \"\",\n" + "        \"encrFlag\": \"\",\n" + "        \"docIndex\": \""
                    + docIndex + "\"\n" + "    }\n" + "}";

            JSONObject jsonObject = (JSONObject) jsonparser.parse(jsonin);
            Log.consoleLog(ifr, "jsonObject :- " + jsonObject.toString());

            response = getDocumentInBase64Formate(ifr, jsonObject, url, ifr.getObjGeneralData().getM_strDMSSessionId());
            Log.consoleLog(ifr, "OD get Document response " + response);

            JSONParser parser = new JSONParser();
            JSONObject resultObj = (JSONObject) parser.parse(response);
            String NGOGetDocumentBDOResponse = resultObj.get("NGOGetDocumentBDOResponse").toString();
            JSONObject NGOGetDocumentBDOResponseObj = (JSONObject) parser.parse(NGOGetDocumentBDOResponse);
            String docContent = NGOGetDocumentBDOResponseObj.get("docContent").toString();
            return docContent;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getDocBase64ReviewContent==>" + e);
            Log.errorLog(ifr, "Exception/getDocBase64ReviewContent==>" + e);
        }
        return "";
    }

    public String initiateNESLRequest(IFormReference ifr, String productType) {

        Log.consoleLog(ifr, "#initiateNESLRequest started...");
        try {

            EsignIntegrationChannel NESL = new EsignIntegrationChannel();
           // String returnMessage = NESL.redirectNESLRequest(ifr, "STAFF", "eStamping");
//            Log.errorLog(ifr, "returnMessage:" + returnMessage);
//
//            if (returnMessage.contains(RLOS_Constants.ERROR)) {
//                return pcm.returnErrorHold(ifr);
//            } else if (returnMessage.contains("showMessage")) {
//                JSONParser jp = new JSONParser();
//                JSONObject obj = (JSONObject) jp.parse(returnMessage);
//                obj.put("eflag", "false");
//                return obj.toString();
//            }

            return RLOS_Constants.SUCCESS;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/initiateNESLRequest==>" + e);
            Log.errorLog(ifr, "Exception/initiateNESLRequest==>" + e);
        }
        return RLOS_Constants.ERROR;
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public String getReviewerDocIndex(IFormReference ifr) {

        String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String Query = "SELECT A.DOCUMENTINDEX FROM PDBDOCUMENT A,PDBDOCUMENTCONTENT B WHERE "
                + "B.PARENTFOLDERINDEX IN (\n"
                + "SELECT FOLDERINDEX FROM PDBFOLDER WHERE "
                + "NAME='" + PID + "' \n"
                + ") AND A.DOCUMENTINDEX=B.DOCUMENTINDEX AND A.NAME='KFS'  "
                + "AND ROWNUM=1 ORDER BY CREATEDDATETIME DESC";
        Log.consoleLog(ifr, "PID==>" + PID);
        Log.consoleLog(ifr, "Query==>" + Query);
        List< List< String>> Result = ifr.getDataFromDB(Query);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());

        String Count = "0";
        if (Result.size() > 0) {
            Count = Result.get(0).get(0);
        }
        return Count;
    }

    public String getAmmortizationAPIStatus(IFormReference ifr) {

        Log.consoleLog(ifr, "#getAmmortizationAPIStatus started...");
        try {

            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String Query = "SELECT COUNT(*) FROM LOS_INTEGRATION_CBS_STATUS WHERE "
                    + "TRANSACTION_ID='" + processInstanceId + "' \n"
                    + "AND API_NAME='CBS_Ammortization' AND API_STATUS='SUCCESS'";
            List< List< String>> Result = ifr.getDataFromDB(Query);
            Log.consoleLog(ifr, "#Result===>" + Result.toString());

            String count = "0";
            if (Result.size() > 0) {
                count = Result.get(0).get(0);
            }

            if (Integer.parseInt(count) > 0) {
                return RLOS_Constants.SUCCESS;
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/initiateNESLRequest==>" + e);
            Log.errorLog(ifr, "Exception/initiateNESLRequest==>" + e);
        }
        return RLOS_Constants.ERROR;
    }

    public String getODContainerHostURL(IFormReference ifr) {
        Log.consoleLog(ifr, "#getODContainerHostURL started...");
        String omnidocsHostURL = "";
        try {

            omnidocsHostURL = pcm.getConstantValue(ifr, "CBSACCSUM", "BANKCODE");

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/initiateNESLRequest==>" + e);
            Log.errorLog(ifr, "Exception/initiateNESLRequest==>" + e);
        }
        return omnidocsHostURL;
    }

}
