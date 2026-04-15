/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor
 */
package com.newgen.iforms.acceleratorCode;

import ISPack.CPISDocumentTxn;
import ISPack.ISUtil.JPDBRecoverDocData;
import ISPack.ISUtil.JPISException;
import ISPack.ISUtil.JPISIsIndex;
import Jdts.Client.JtsConnection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.newgen.dlp.docgen.gen.GenerateDocument;
import com.newgen.dmsapi.DMSInputXml;
import com.newgen.dmsapi.DMSXmlResponse;
import com.newgen.iforms.AccAPIS.GenerateOTPAPI;
import com.newgen.iforms.AccAPIS.NegativeDBGridAPI;
import com.newgen.iforms.AccAPIS.ValidateOTPAPI;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.commonXMLAPI.UploadCreateWI;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.xmlapi.IFormCallBroker;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CommonMethods {
    
    CommonFunctionality cf = new CommonFunctionality();
    final String[] units = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
    final String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

    public void mEmailSMSTrigger(IFormReference ifr, String Template, String EmailId, String MobileNo, String partyType) {
        Log.consoleLog(ifr, "Inside mEmailSMSTrigger");
        try {
            String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
            if (Template.equalsIgnoreCase("BUREAU_CONSENT_BTNCLICK")) {
                Log.consoleLog(ifr, "Inside BUREAU_CONSENT_BTNCLICK");
                String query = "insert into ng_rlos_email_sms (EMAILTO,EMAILSUBJECT,EMAILBODY,MOBILENUMBER,"
                        + "EMAILREQUIRED,SMSREQUIRED,SMSCONTENT,EMAILCC,DOCINDEX, DOCUMENTNAME,PROCESSNAME,"
                        + "PARKINGBRANCH,WINO,APPLICANTTYPE) "
                        + "values('" + EmailId + "','retailbureauconsenttriggeralertsubject','retailbureauconsenttriggeralertemailbody',"
                        + "'" + MobileNo + "','Y','Y','retailbureauconsenttriggeralertsms','',null,null, '" + ifr.getProcessName()
                        + "','','" + pid + "','" + partyType + "')";
                Log.consoleLog(ifr, "common::" + query);
                int retval1 = ifr.saveDataInDB(query);
                Log.consoleLog(ifr, "retval1::" + retval1);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mEmailSMSTrigger :: " + e);
            Log.errorLog(ifr, "Exception in mEmailSMSTrigger :: " + e);
        }
    }

    public void mUpdateBureauConsent(IFormReference ifr, String PID, String appType) {
        String insertquery = ConfProperty.getQueryScript("UPDATEBureauConsent").replaceAll("#PID#", PID).replaceAll("#PARTYTYPE#", appType);
        Log.consoleLog(ifr, "insert query1 is " + insertquery);
        int retval = ifr.saveDataInDB(insertquery);
        Log.consoleLog(ifr, "retval1 " + retval);
    }

    public void mInsertBureauConsent(IFormReference ifr, String PID, String appType) {
        String sDeleteQuery = ConfProperty.getQueryScript("DELETEBureauConsent").replaceAll("#PID#", PID).replaceAll("#PARTYTYPE#", appType);
        ifr.saveDataInDB(sDeleteQuery);
        String insertquery = ConfProperty.getQueryScript("INSERTBureauConsent").replaceAll("#PID#", PID).replaceAll("#PARTYTYPE#", appType);
        Log.consoleLog(ifr, "insert query1 is " + insertquery);
        int retval = ifr.saveDataInDB(insertquery);
        Log.consoleLog(ifr, "retval1 " + retval);

    }

    public void mSetBureauVerified(IFormReference ifr) {
        ifr.setValue("QNL_BUREAU_CONSENT_ConsentVerifiedOn", cf.getCurrentDate(ifr));
        ifr.setValue("QNL_BUREAU_CONSENT_ConsentVerifiedBy", ifr.getUserName());
    }

    public String mBureauOTP(IFormReference ifr, String type) {
        Log.consoleLog(ifr, "Inside mAccClickBureauOTP");
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String appType = (String) ifr.getValue("QNL_BUREAU_CONSENT_PartyType");
            Log.consoleLog(ifr, "appType:: " + appType);
            String entityQuery = ConfProperty.getQueryScript("BASICINFOFETCH").replaceAll("#PARTYTYPE#", appType);
            Log.consoleLog(ifr, "entityQuery::" + entityQuery);
            List<List<String>> entity = ifr.getDataFromDB(entityQuery);
            Log.consoleLog(ifr, "entity::" + entity.toString());
            String getQuery = "";
            if (entity.size() > 0) {
                if (entity.get(0).get(0).equalsIgnoreCase("I")) {
                    getQuery = ConfProperty.getQueryScript("BASICINFOI").replaceAll("#PARTYTYPE#", appType);
                } else {
                    getQuery = ConfProperty.getQueryScript("BASICINFONI").replaceAll("#PARTYTYPE#", appType);
                }
            }
            Log.consoleLog(ifr, "getQuery::" + getQuery);
            List<List<String>> getDataList = ifr.getDataFromDB(getQuery);
            Log.consoleLog(ifr, "getDataList::" + getDataList);
            if (getDataList.size() > 0) {
                String fullName = getDataList.get(0).get(2);
                Log.consoleLog(ifr, "fullName::" + fullName);
                String mobileNo = getDataList.get(0).get(1).trim();
                Log.consoleLog(ifr, "mobileNo::" + mobileNo);
                String emailId = getDataList.get(0).get(0).trim();
                Log.consoleLog(ifr, "mobileNo::" + mobileNo);
                if (mobileNo.equalsIgnoreCase("")) {
                    return "Kindly add Email Id and Mobile Number for '" + fullName + "'";
                } else {
                    String selectQuery = ConfProperty.getQueryScript("BureauConsent").replaceAll("#PID#", PID).replaceAll("#PARTYTYPE#", appType);
                    Log.consoleLog(ifr, "selectQuery::" + selectQuery);
                    List<List<String>> bureauList = ifr.getDataFromDB(selectQuery);
                    Log.consoleLog(ifr, "bureauList:" + bureauList);
                    String ConsentFlag = "";
                    if (bureauList.size() > 0) {
                        Log.consoleLog(ifr, "Row already present in table");
                        ConsentFlag = bureauList.get(0).get(0);
                    }
                    if (ConsentFlag.equalsIgnoreCase("Y")) {
                        ifr.setValue("QNL_BUREAU_CONSENT_ConsentReceived", "Accepted");
                        mSetBureauVerified(ifr);
                        return "Consent Already Recieved";
                    } else {
                        String result = "";
                        if (type.equalsIgnoreCase("GenerateOTP")) {
                            GenerateOTPAPI goa = new GenerateOTPAPI();
                            result = goa.mCallGenerateOTpAPI(ifr, mobileNo);
                            if (result != null && result.equalsIgnoreCase("SUCCESS")) {
                                mInsertBureauConsent(ifr, PID, appType);
                                ifr.setValue("QNL_BUREAU_CONSENT_ConsentReceived", "Initiated");
                                ifr.setStyle("QNL_BUREAU_CONSENT_Methodology", "disable", "true");
                                return result;
                            }
                        } else if (type.equalsIgnoreCase("ValidateOTP")) {
                            if (bureauList.size() <= 0) {
                                return "Please Click on Generate OTP Button!";
                            }
                            ValidateOTPAPI voa = new ValidateOTPAPI();
                            result = voa.mCallValidateOTpAPI(ifr, mobileNo);
                            if (result != null && result.equalsIgnoreCase("SUCCESS")) {
                                mUpdateBureauConsent(ifr, PID, appType);
                                ifr.setValue("QNL_BUREAU_CONSENT_ConsentReceived", "Accepted");
                                ifr.setStyle("QNL_BUREAU_CONSENT_Methodology", "disable", "true");
                                return result;
                            }
                        } else if (type.equalsIgnoreCase("Portal")) {
                            if (bureauList.size() > 0) {
                                return "Portal Consent Already Raised!";
                            }
                            mInsertBureauConsent(ifr, PID, appType);
                            mEmailSMSTrigger(ifr, "BUREAU_CONSENT_BTNCLICK", emailId, mobileNo, appType);
                            ifr.setValue("QNL_BUREAU_CONSENT_ConsentReceived", "Initiated");
                            ifr.setStyle("QNL_BUREAU_CONSENT_Methodology", "disable", "true");
                            return "SUCCESS";
                        }
                        return result;
                    }
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccClickBureauOTP::" + e);
            Log.errorLog(ifr, "Exception in mAccClickBureauOTP::" + e);
        }
        return AcceleratorConstants.TRYCATCHERROR;
    }

    public String mValidatePANAadharDetails(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside mValidatePANAadharDetails");
        try {
            String sQuery = ConfProperty.getQueryScript("BASICINFOFETCH1").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
            Log.consoleLog(ifr, "sQuery::" + sQuery);
            List<List<String>> ApplicantResult = ifr.getDataFromDB(sQuery);
            Log.consoleLog(ifr, "ApplicantResult::" + ApplicantResult);
            for (int i = 0; i < ApplicantResult.size(); i++) {
                int kycCount = 0;
                if (ApplicantResult.get(i).get(0).equalsIgnoreCase("I")) {
                    sQuery = ConfProperty.getQueryScript("AADHARCHECK").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId()).
                            replaceAll("#PARTYTYPE#", ApplicantResult.get(i).get(1));
                    Log.consoleLog(ifr, "sQuery::" + sQuery);
                    List<List<String>> KYCResult = ifr.getDataFromDB(sQuery);
                    Log.consoleLog(ifr, "KYCResult::" + KYCResult);
                    if (KYCResult.size() > 0) {
                        kycCount++;
                    }
                } else {
                    kycCount++;
                }
                Log.consoleLog(ifr, "kycCount :i:" + kycCount);
                if (kycCount < 1) {
                    return "false";
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mValidatePANAadharDetails" + e.getMessage());
            Log.errorLog(ifr, "Exception in mValidatePANAadharDetails" + e.getMessage());
            return AcceleratorConstants.TRYCATCHERROR;
        }
        return "";
    }

    public String mCheckRecievedConsent(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mCheckRecievedConsent");
        try {
            String appType = ifr.getValue("DD_CB_APPTYPE_ID").toString();
            Log.consoleLog(ifr, "appType::" + appType);
            String getQuery = ConfProperty.getQueryScript("BureauConsent1").replaceAll("#PARTYTYPE#", appType);
            Log.consoleLog(ifr, "getQuery::" + getQuery);
            List<List<String>> getQueryList = ifr.getDataFromDB(getQuery);
            Log.consoleLog(ifr, "getQueryList::" + getQueryList);
            if (getQueryList != null && !getQueryList.isEmpty()) {
                String consentFlag = getQueryList.get(0).get(0);
                Log.consoleLog(ifr, "consentFlag::" + consentFlag);
                if (consentFlag.equalsIgnoreCase("Y")) {
                    ifr.setValue("ConsentRcvd", "Yes");
                } else if (consentFlag.equalsIgnoreCase("N")) {
                    ifr.setValue("ConsentRcvd", "No");
                } else {
                    ifr.setValue("ConsentRcvd", "");
                }
            } else {
                ifr.setValue("ConsentRcvd", "");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mCheckRecievedConsent" + e.getMessage());
            Log.errorLog(ifr, "Exception in mCheckRecievedConsent" + e.getMessage());
            return AcceleratorConstants.TRYCATCHERROR;
        }
        return "";
    }

    public String mFetchRBIDefaultersNegDB(IFormReference ifr, String applicant) {
        Log.consoleLog(ifr, "Inside mFetchRBIDefaultersNegDB");
        try {
            JSONArray NegDBGrid = ifr.getDataFromGrid("ALV_NEGATIVE_DB_DEFAULTER");
            Log.consoleLog(ifr, "NegDBGrid:: " + NegDBGrid.toString());
            ArrayList customerArr = new ArrayList();
            for (int i = 0; i < NegDBGrid.size(); i++) {
                customerArr.add(ifr.getTableCellValue("ALV_NEGATIVE_DB_DEFAULTER", i, "QNL_NEGATIVE_DB_DEFAULTER_PartyType"));
            }
            if (customerArr.contains(applicant)) {
            } else {
                NegativeDBGridAPI ndbga = new NegativeDBGridAPI();
                String result = ndbga.mCallNegativeDefaulterAPI(ifr, applicant);
                if (!(result.equalsIgnoreCase("SUCCESS"))) {
                    return result;
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mFetchRBIDefaulters" + e.getMessage());
            Log.errorLog(ifr, "Exception in mFetchRBIDefaulters" + e.getMessage());
            return AcceleratorConstants.TRYCATCHERROR;
        }
        return "";
    }

    public String mAddCBReportDocVal(IFormReference ifr) {
        try {
            JSONArray cbGridArray = ifr.getDataFromGrid("ALV_CB_Details");
            Log.consoleLog(ifr, "cbGridArray-> " + cbGridArray);
            int size = ifr.getDataFromGrid("ALV_CB_Details").size();
            Log.consoleLog(ifr, "ALV_CB_Details Size:" + size);

            for (int i = 0; i < size; i++) {
                if (ifr.getValue("DD_CB_APPTYPE_ID").toString().equalsIgnoreCase(ifr.getTableCellValue("ALV_CB_Details", i, "QNL_CB_Details_Applicant_Type").toString())
                        && ifr.getValue("DD_CB_BUREAU_ID").toString().equalsIgnoreCase(ifr.getTableCellValue("ALV_CB_Details", i, "QNL_CB_Details_CB_Type").toString())) {
                    long day_count = cf.getCurrentDayDifference(ifr, ifr.getTableCellValue("ALV_CB_Details", i, "QNL_CB_Details_Report_Enquiry_Date").toString(), "dd/MM/yyyy");
                    if (day_count > 30) {
                    } else {
                        return "CB Report Already added";
                    }
                }
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception occur inside mAddCBReportDocVal:" + e);
            Log.errorLog(ifr, "Exception occur inside mAddCBReportDocVal:" + e);
            return AcceleratorConstants.TRYCATCHERROR;
        }
        return "";
    }

    public DMSXmlResponse getFolderProperty(IFormReference ifr, String folderIndex, String creationDate,
            String dataclassFlag) {
        Log.consoleLog(ifr, "************* INSIDE getFolderProperty**************");
        DMSInputXml objInputXml = new DMSInputXml();
        String inputXml = objInputXml.getGetFolderPropertyXml(ifr.getCabinetName(),
                ifr.getObjGeneralData().getM_strDMSSessionId(), creationDate, folderIndex, dataclassFlag);
        Log.consoleLog(ifr, "inputXml--" + inputXml);
        try {
            String outputXml = IFormCallBroker.execute(inputXml, ifr.getObjGeneralData().getM_strJTSIP(),
                    Integer.parseInt(ifr.getObjGeneralData().getM_strJTSPORT()));
            DMSXmlResponse objResponse = new DMSXmlResponse(outputXml);
            Log.consoleLog(ifr, "outputXml--" + outputXml);
            return objResponse;
        } catch (Exception ex) {
            Log.consoleLog(ifr, "Exception Inside getFolderProperty--" + ex);
            Log.errorLog(ifr, "Exception inside getFolderProperty--" + ex);
        }
        return null;
    }

    //doc add
    public String addDocument(IFormReference ifr, String folderIndex, File document, String imageServerIp,
            String imageServerPort, String imgType, String docType, String OPXmlControl) {
        try {
            DMSXmlResponse objResponse;
            String outputXml = "";
            String inputXml = "";
            DMSInputXml objInputXml = new DMSInputXml();
            Log.consoleLog(ifr, "************* INSIDE ADDDOCUMENT TO SMS**************");
            JtsConnection jtsConn = null;
            JPISIsIndex IsIndex = new JPISIsIndex();
            JPDBRecoverDocData oRecoverDocData = new JPDBRecoverDocData();
            objResponse = getFolderProperty(ifr, folderIndex, null, "N");
            String volumeIndex = objResponse.getVal("ImageVolumeIndex");
            Log.consoleLog(ifr, "volumeIndex--" + volumeIndex);

            try {
                CPISDocumentTxn.AddDocument_MT(jtsConn, imageServerIp, (short) Integer.parseInt(imageServerPort),
                        ifr.getCabinetName(), Short.parseShort(volumeIndex), document.getAbsolutePath().toString(),
                        oRecoverDocData, "", IsIndex);
            } catch (JPISException ex) {
                Log.consoleLog(ifr, " JPISException : " + ex.getMessage());
                Log.errorLog(ifr, " JPISException : " + ex.getMessage());
            }
            String isIndex = String.valueOf(IsIndex.m_nDocIndex) + "#" + String.valueOf(IsIndex.m_sVolumeId);
            Log.consoleLog(ifr, "isIndex--" + isIndex);
            String documentIndex = String.valueOf(IsIndex.m_nDocIndex);
            Log.consoleLog(ifr, "documentIndex--" + documentIndex);
            long byte_size = document.length();
            String strDocumentSize = new Long(byte_size).toString();
            Log.consoleLog(ifr, "strDocumentSize--" + strDocumentSize);
            int dotIndex = document.getName().lastIndexOf(".");
            Log.consoleLog(ifr, "dotIndex--" + dotIndex);
            String fileNameWExt = document.getName();
            Log.consoleLog(ifr, "fileNameWExt--" + fileNameWExt);
            String fileName = fileNameWExt.substring(0, dotIndex);
            Log.consoleLog(ifr, "fileName--" + fileName);
            String fileExtension = fileNameWExt.substring(dotIndex + 1, fileNameWExt.length());
            Log.consoleLog(ifr, "fileExtension--" + fileExtension);
            Log.consoleLog(ifr, "************* INSIDE ADDDOC_TO_FOLDER **************");
            inputXml = objInputXml.getAddDocumentXml(ifr.getCabinetName(),
                    ifr.getObjGeneralData().getM_strDMSSessionId(), null, /* PARENT FOLDER INDEX */ folderIndex,
                    /* NO OF PAGES */ "4", /* ACCESS TYPE */ "S", /* DOCUMENT NAME */ docType,
                    /* CREATION DATETIME */ null, /* EXPIRYDATETIME */ null, /* VERSION FLAG */ "Y",
                    /* DOCUMENT TYPE - NON IMAGE */ imgType, /* DOC SIZE */ strDocumentSize, /* CREATED BY APP */ null,
                    /* CREATED BY APPNAME */ fileExtension, /* ISINDEX */ isIndex, /* TEXTISINDEX */ null,
                    /* ODMADocumentIndex */ null, /* Comment */ null, /* Author */ null, /* OwnerIndex */ null,
                    /* EnableLog */ "Y", /* FTSFlag */ null, /* DataDefinition */ null, /* Keywords */ null);
            Log.consoleLog(ifr, "ADDDOCUMENT INPUT XML:" + inputXml);
            try {
                outputXml = IFormCallBroker.execute(inputXml, ifr.getObjGeneralData().getM_strJTSIP(),
                        Integer.parseInt(ifr.getObjGeneralData().getM_strJTSPORT()));
                Log.consoleLog(ifr, "OUTPUT XML:" + outputXml);
                objResponse = new DMSXmlResponse(outputXml);
                Log.consoleLog(ifr, "objResponse.getVal(\"Status\")--" + objResponse.getVal("Status"));
                if (!OPXmlControl.isEmpty()) {
                    ifr.setValue(OPXmlControl, outputXml);
                }
                return objResponse.getVal("DocumentIndex");
//            return objResponse.getVal("Status");

            } catch (Exception exc) {
                Log.consoleLog(ifr, "Exception caught > " + exc);
                Log.errorLog(ifr, "Exception caught > " + exc);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception caught in Add Document> " + e);
            Log.errorLog(ifr, "Exception caught in Add Document> " + e);
        }
        return "";
    } //doc add end

    public void mcheckerlist(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "Inside mcheckerlist");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String checkerquery = ConfProperty.getQueryScript("mcheckerlist");
            List<List<String>> checkerqueryres = cf.mExecuteQuery(ifr, checkerquery, "checkerquery");
            String valcount = ConfProperty.getQueryScript("mcheckerlist1");
            valcount = valcount.replaceAll("#PID#", PID);
            List<List<String>> valcountresult = cf.mExecuteQuery(ifr, valcount, "checkerquery");
            Log.consoleLog(ifr, "valcount " + valcountresult);
            if ((!checkerqueryres.isEmpty()) && (valcountresult.get(0).get(0).equals("0"))) {
                JSONArray GenDocGrid1 = new JSONArray();
                for (List<String> tmp : checkerqueryres) {
                    JSONObject GenDocGridData1 = new JSONObject();
                    GenDocGridData1.put("Description", tmp.get(0));
                    GenDocGrid1.add(GenDocGridData1);
                }
                Log.consoleLog(ifr, "GenDocGrid:" + GenDocGrid1);
                ifr.addDataToGrid("LV_CheckList", GenDocGrid1);
            } else {
                Log.consoleLog(ifr, "No Docs available for this WS to generate");
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in loadGenerateDocs" + e);
            Log.errorLog(ifr, "Exception in loadGenerateDocs" + e);
        }
    }

    public void mLoadProduct(IFormReference ifr) {
        cf.mSetClearComboValue(ifr, "QNL_LOS_PROPOSED_FACILITY_Product,QNL_LOS_PROPOSED_FACILITY_SubProduct,QNL_LOS_PROPOSED_FACILITY_Variant,"
                + "QNL_LOS_PROPOSED_FACILITY_LoanPurpose,QNL_LOS_PROPOSED_FACILITY_SchemeId,QNL_LOS_PROPOSED_FACILITY_BehaviourId,"
                + "QNL_LOS_PROPOSED_FACILITY_Concession,QNL_LOS_PROPOSED_FACILITY_EffectiveROI,QNL_LOS_PROPOSED_FACILITY_ROIType,QNL_LOS_PROPOSED_FACILITY_ROI,QNL_LOS_PROPOSED_FACILITY_MCLR,QNL_LOS_PROPOSED_FACILITY_Spread,QNL_LOS_PROPOSED_FACILITY_Premium");
        String Product = ConfProperty.getQueryScript("Product");
        Product = Product.replaceAll("#purposalCode#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ProposalType").toString());
        cf.loadComboValues(ifr, "QNL_LOS_PROPOSED_FACILITY_Product", Product, "Product Combo Population:", "LV");
    }

    public void mLoadSubProduct(IFormReference ifr) {
        cf.mSetClearComboValue(ifr, "QNL_LOS_PROPOSED_FACILITY_SubProduct,QNL_LOS_PROPOSED_FACILITY_Variant,"
                + "QNL_LOS_PROPOSED_FACILITY_LoanPurpose,QNL_LOS_PROPOSED_FACILITY_SchemeId,QNL_LOS_PROPOSED_FACILITY_BehaviourId,"
                + "QNL_LOS_PROPOSED_FACILITY_Concession,QNL_LOS_PROPOSED_FACILITY_EffectiveROI,QNL_LOS_PROPOSED_FACILITY_ROIType,QNL_LOS_PROPOSED_FACILITY_ROI,QNL_LOS_PROPOSED_FACILITY_MCLR,QNL_LOS_PROPOSED_FACILITY_Spread,QNL_LOS_PROPOSED_FACILITY_Premium");
        String SubProduct = ConfProperty.getQueryScript("SubProduct");
        SubProduct = SubProduct.replaceAll("#productCode#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Product").toString());
        cf.loadComboValues(ifr, "QNL_LOS_PROPOSED_FACILITY_SubProduct", SubProduct, "Sub Product Combo Population:", "LV");
    }

    public void mLoadPurpose(IFormReference ifr) {
        cf.mSetClearComboValue(ifr, "QNL_LOS_PROPOSED_FACILITY_LoanPurpose,QNL_LOS_PROPOSED_FACILITY_Variant,"
                + "QNL_LOS_PROPOSED_FACILITY_SchemeId,QNL_LOS_PROPOSED_FACILITY_BehaviourId,QNL_LOS_PROPOSED_FACILITY_Concession,QNL_LOS_PROPOSED_FACILITY_EffectiveROI,QNL_LOS_PROPOSED_FACILITY_ROIType,QNL_LOS_PROPOSED_FACILITY_ROI,QNL_LOS_PROPOSED_FACILITY_MCLR,QNL_LOS_PROPOSED_FACILITY_Spread,QNL_LOS_PROPOSED_FACILITY_Premium");
        String SubProduct = ConfProperty.getQueryScript("Purpose");
        SubProduct = SubProduct.replaceAll("#productCode#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Product").toString()).
                replaceAll("#subProductCode#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SubProduct").toString());
        cf.loadComboValues(ifr, "QNL_LOS_PROPOSED_FACILITY_LoanPurpose", SubProduct, "Purpose Combo Population:", "LV");
    }

    public String mLoadVariant(IFormReference ifr) {
        cf.mSetClearComboValue(ifr, "QNL_LOS_PROPOSED_FACILITY_Variant,QNL_LOS_PROPOSED_FACILITY_SchemeId,QNL_LOS_PROPOSED_FACILITY_BehaviourId,QNL_LOS_PROPOSED_FACILITY_Concession,QNL_LOS_PROPOSED_FACILITY_EffectiveROI,QNL_LOS_PROPOSED_FACILITY_ROIType,QNL_LOS_PROPOSED_FACILITY_ROI,QNL_LOS_PROPOSED_FACILITY_MCLR,QNL_LOS_PROPOSED_FACILITY_Spread,QNL_LOS_PROPOSED_FACILITY_Premium");
        String Variant = ConfProperty.getQueryScript("Variant");
        Log.consoleLog(ifr, "LOAN PURPOSE");
        Variant = Variant.replaceAll("#productCode#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Product").toString())
                .replaceAll("#subProductCode#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SubProduct").toString())
                .replaceAll("#purposeCode#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_LoanPurpose").toString());
        Log.consoleLog(ifr, "VARIANT:" + Variant);
        //cf.loadComboValues(ifr, "QNL_LOS_PROPOSED_FACILITY_Variant", Variant, "Variant Combo Population:", "LV");
        List<List<String>> list = cf.mExecuteQuery(ifr, Variant, "Variant");
        if (list != null && list.size() > 0) {
            if (list.size() == 1) {
                ifr.addItemInCombo("QNL_LOS_PROPOSED_FACILITY_Variant", list.get(0).get(0), list.get(0).get(1));
                ifr.setValue("QNL_LOS_PROPOSED_FACILITY_Variant", list.get(0).get(1));
                return new AcceleratorActivityManagerCode().mAcconChangePopulateProductDetails(ifr, "QNL_LOS_PROPOSED_FACILITY_Variant", "", "");
            } else {
                for (int i = 0; i < list.size(); i++) {
                    String label = list.get(i).get(0);
                    String value = list.get(i).get(1);
                    ifr.addItemInCombo("QNL_LOS_PROPOSED_FACILITY_Variant", label, value);
                }
            }
        }
        return "";
    }
//modified by logaraj on 24/07/2024
    public void mLoadSchemeId(IFormReference ifr) {
                    String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
                    PortalCommonMethods pcc=new PortalCommonMethods();
        cf.mSetClearComboValue(ifr, "QNL_LOS_PROPOSED_FACILITY_SchemeId,QNL_LOS_PROPOSED_FACILITY_BehaviourId");
        String query = ConfProperty.getQueryScript("Scheme");
        query = query.replaceAll("#productCode#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Product").toString())
                .replaceAll("#subProductCode#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SubProduct").toString())
                .replaceAll("#purposeCode#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_LoanPurpose").toString())
                .replaceAll("#Variant#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Variant").toString());
        cf.mGetLabelAndSetToField(ifr, query, "Sceheme Setting:", "QNL_LOS_PROPOSED_FACILITY_SchemeId");
        String schemeId = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString();
        pcc.UpdateSchemeIDCommon(ifr, schemeId, PID);

    }

    public void mLoadBehaiourId(IFormReference ifr) {
        String query = ConfProperty.getQueryScript("BehaviourId");//ADD
        query = query.replaceAll("#schemeId#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString());
        cf.mGetLabelAndSetToField(ifr, query, "Behaviour id Setting:", "QNL_LOS_PROPOSED_FACILITY_BehaviourId");
    }

    public void mLoadTenure(IFormReference ifr) {
        cf.mSetClearComboValue(ifr, "QNL_LOS_PROPOSED_FACILITY_Tenure");
        String Tenure = ConfProperty.getQueryScript("Tenure");
        Tenure = Tenure.replaceAll("#schemeId#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString());
        String minAmount = "";
        String maxAmount = "";
        String multiple = "";
        List<List<String>> Tenurelist = cf.mExecuteQuery(ifr, Tenure, "Tenure Range Taking:");
        if (Tenurelist.size() > 0) {
            for (int i = 0; i < Tenurelist.size(); i++) {
                minAmount = Tenurelist.get(i).get(0);
                maxAmount = Tenurelist.get(i).get(1);
                multiple = Tenurelist.get(i).get(2);
            }
        }
        BigDecimal minAmount1 = new BigDecimal(minAmount.equalsIgnoreCase("") ? "0" : minAmount);
        BigDecimal maxAmount1 = new BigDecimal(maxAmount.equalsIgnoreCase("") ? "0" : maxAmount);
        for (int i = minAmount1.intValue(); i <= maxAmount1.intValue(); i++) {
            if (i % Integer.parseInt(multiple) == 0) {
                ifr.addItemInCombo("QNL_LOS_PROPOSED_FACILITY_Tenure", String.valueOf(i), String.valueOf(i));
            }
        }
    }

    public void mLoadFacilityType(IFormReference ifr) {
        cf.mSetClearComboValue(ifr, "QNL_LOS_PROPOSED_FACILITY_FacilityType");
        String query = ConfProperty.getQueryScript("Facility");//ADD
        query = query.replaceAll("#schemeId#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString());
        cf.loadComboValues(ifr, "QNL_LOS_PROPOSED_FACILITY_FacilityType", query, "Facility type Setting:", "L");
    }

    public String mLoadSchedulecode(IFormReference ifr, String GENDER, String SalaryTieUp, String CANARARETAILGRADE) {

        ifr.clearCombo("QNL_LOS_PROPOSED_FACILITY_ScheduleCode");
        Log.consoleLog(ifr, "Inside ScheduleCode:" + CANARARETAILGRADE + "/n GENDER " + GENDER + "/n SalaryTieUp " + SalaryTieUp);
        if (ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROIType").toString().equalsIgnoreCase("Floating")) {
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_FRP", "visible", "false");
        } else {
            ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_FRP", "visible", "true");

        }
        String query = ConfProperty.getQueryScript("LOANSCHEDULECODE").replaceAll("#SCHEMEID#",
                ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString()).replaceAll("#ROITYPE#",
                ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROIType").toString()).replaceAll("#GENDER#", GENDER).replaceAll("#CRG#",
                CANARARETAILGRADE).replaceAll("#SALARY_TIEUP#", SalaryTieUp);

        Log.consoleLog(ifr, "Inside ScheduleCode:" + query);
        String Scheduletype = "";
        List<List<String>> sccode = cf.mExecuteQuery(ifr, query, "Execute query for scheduled code ");

        if (!sccode.isEmpty()) {
            for (int i = 0; i < sccode.size(); i++) {
                String SchedulCode = sccode.get(i).get(0);
                Scheduletype = sccode.get(i).get(1);
                Log.consoleLog(ifr, "Scheduletype ::" + Scheduletype);

                Log.consoleLog(ifr, "SchedulCode ::" + SchedulCode);

                ifr.addItemInCombo("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", Scheduletype, Scheduletype);
                Log.consoleLog(ifr, "sccode.size( ::" + sccode.size());
                if (sccode.size() < 2) {
                    Log.consoleLog(ifr, "PartyType ::" + SchedulCode);
                    ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", Scheduletype);
                    ifr.setStyle("QNL_LOS_PROPOSED_FACILITY_ScheduleCode", "disable", "true");

                }

            }

        }
        return "";
    }

    public void mLoadROIType(IFormReference ifr) {
        cf.mSetClearComboValue(ifr, "QNL_LOS_PROPOSED_FACILITY_ROIType");
        String query = ConfProperty.getQueryScript("ROIType");//ADD
        query = query.replaceAll("#schemeId#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString());
        cf.loadComboValues(ifr, "QNL_LOS_PROPOSED_FACILITY_ROIType", query, "ROI type Setting:", "L");
    }

    public String validateConsession(IFormReference ifr, String effectiveroi) {
        String query = ConfProperty.getQueryScript("ROI");//ADD
        query = query.replaceAll("#schemeId#", ifr.getValue("QNL_LOS_PROPOSED_FACILITY_SchemeId").toString());
        List<List<String>> list = cf.mExecuteQuery(ifr, query, "ROI Details:");
        String roitype = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROIType").toString();
        String minroi = "";
        String maxroi = "";
        String spread = "";
        if (list.size() > 0) {
            minroi = list.get(0).get(0);
            maxroi = list.get(0).get(1);
            spread = list.get(0).get(2);
        }
        String message = "";
        if (roitype.equalsIgnoreCase("Floating")) {
            message = "Concession cannot be greater than spread!";
        } else {
            message = "Concession cannot be greater than ROI!";
            spread = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROI").toString();
        }
        String msg = checkConcession(ifr, spread, message);
        if (!(msg.equalsIgnoreCase(""))) {
            return msg;
        }
        msg = checkROI(ifr, minroi, maxroi, effectiveroi);
        return msg;
    }

    public String checkConcession(IFormReference ifr, String value, String message) {
        Log.consoleLog(ifr, "Checking concession:");
        String concession = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Concession").toString();
        BigDecimal Concession = new BigDecimal(concession.equalsIgnoreCase("") ? "0" : concession);
        BigDecimal Spread = new BigDecimal(value.equalsIgnoreCase("") ? "0" : value);
        JSONObject mes = new JSONObject();
        if ((Concession.compareTo(Spread) > 0)) {
            mes.put("showMessage", cf.showMessage(ifr, "QNL_LOS_PROPOSED_FACILITY_Concession", "error", message));
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_Concession", "");
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_EffectiveROI", "");
            return mes.toString();
        }
        return "";
    }

    public String checkROI(IFormReference ifr, String minroi, String maxroi, String effectiveroi) {
        Log.consoleLog(ifr, "Checking ROI:");
        BigDecimal minimumROI = new BigDecimal(minroi.equalsIgnoreCase("") ? "0" : minroi);
        BigDecimal maxmumROI = new BigDecimal(maxroi.equalsIgnoreCase("") ? "0" : maxroi);
        BigDecimal effectiveROI = new BigDecimal(effectiveroi.equalsIgnoreCase("") ? "0" : effectiveroi);
        JSONObject message = new JSONObject();
        if ((effectiveROI.compareTo(minimumROI) >= 0) && (effectiveROI.compareTo(maxmumROI) <= 0)) {

        } else {
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_EffectiveROI", "");
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_Concession", "");
            message.put("showMessage", cf.showMessage(ifr, "QNL_LOS_PROPOSED_FACILITY_EffectiveROI", "error", "Effective ROI is not in Range of " + minimumROI + "-" + maxmumROI + ""));
            return message.toString();
        }
        return "";
    }

    public void InsertIntoGeneralCommentsCmplxTbl(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside GeneralComments History");
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ConfProperty.getCommonPropertyValue("AvailableDateTime"));
            String dateDisplayed = sdf.format(new Date());
            dateDisplayed = cf.getStringFormattedDate(ifr, dateDisplayed);
            String receiving_date = ifr.getValue("Receiving_Date").toString();
            Log.consoleLog(ifr, "Current Date Time" + dateDisplayed);
            Log.consoleLog(ifr, "Receiving Date Time" + receiving_date);
            long day_count = 0;
            try {
                Date date1 = cf.parseDate(ifr, dateDisplayed, false);
                Date date2 = cf.parseDate(ifr, receiving_date, false);
                long diff = date1.getTime() - date2.getTime();
                day_count = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
                Log.consoleLog(ifr, "Days: " + day_count);
            } catch (Exception e) {
                Log.consoleLog(ifr, "Exception in InsertIntoGeneralCommentsCmplxTbl: " + e);
                Log.errorLog(ifr, "Exception in InsertIntoGeneralCommentsCmplxTbl: " + e);
            }
            String decision = ifr.getValue("DecisionValue").toString();
            String DecisionQuery = ConfProperty.getQueryScript("Decision");
            DecisionQuery = DecisionQuery.replaceAll("#decision#", decision).replaceAll("#processName#", ifr.getProcessName())
                    .replaceAll("#ActivityName#", ifr.getActivityName());
            Log.consoleLog(ifr, "DecisionQuery: " + DecisionQuery);
            String decisionValue = "";
            List<List<String>> DecisionList = cf.mExecuteQuery(ifr, DecisionQuery, "");
            if (DecisionList.size() > 0) {
                decisionValue = DecisionList.get(0).get(0);
            }

            JSONObject jsonObj = new JSONObject();
            String Decision_Remarks = ifr.getValue("Decision_Remarks").toString();
            jsonObj.put("QNL_Action_History_Remarks", Decision_Remarks);
            jsonObj.put("QNL_Action_History_Decision", decisionValue);
            String user = ifr.getUserName();
            jsonObj.put("QNL_Action_History_SubmitUser", user);
            jsonObj.put("QNL_Action_History_SubmitDate", dateDisplayed);
            String Activityname = ifr.getActivityName();
            jsonObj.put("QNL_Action_History_SubmitStageName", Activityname);
            String receiving_Date = ifr.getValue("Receiving_Date").toString();
            jsonObj.put("QNL_Action_History_Receiving_Date", receiving_Date);
            String day_count1 = String.valueOf(day_count);
            jsonObj.put("QNL_Action_History_Total_Days", day_count1);
            String RejectionCategory = ifr.getValue("RejectCategoryDecision").toString();
            jsonObj.put("QNL_Action_History_Rejection_Category", RejectionCategory);
            String SubCategory = ifr.getValue("RejectSubCategoryDecision").toString();
            jsonObj.put("QNL_Action_History_Rejection_SubCategory", SubCategory);
            JSONArray jsonArrayObj = new JSONArray();
            jsonArrayObj.add(jsonObj);
            Log.consoleLog(ifr, "jsonObjArr" + jsonArrayObj.toString());
            ifr.addDataToGrid("ALV_Action_History", jsonArrayObj);
            ifr.setValue("Receiving_Date", dateDisplayed);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in InsertIntoGeneralCommentsCmplxTbl ::: General Contract " + e);
            Log.errorLog(ifr, "Exception in InsertIntoGeneralCommentsCmplxTbl ::: General Contract " + e);
        }
    }

    public String mAddDocumentSubProcess(IFormReference ifr, String value, String control, String queryType) {
        try {
            Log.consoleLog(ifr, "Inside mAddDocumentSubProcess Documents in checklist" + value);
            String query = ConfProperty.getQueryScript(queryType).replaceAll("#insertionorderid#", value);
            List<List<String>> documents = cf.mExecuteQuery(ifr, query, "Documents");
            ifr.clearTable(control);
            if (documents.size() > 0) {
                JSONArray docarray = new JSONArray();
                for (int i = 0; i < documents.size(); i++) {
                    JSONObject docobj = new JSONObject();
                    docobj.put("Document Name", documents.get(i).get(0));
                    docobj.put("Document Type", documents.get(i).get(1));
                    docobj.put("Doc ID", documents.get(i).get(2));
                    docarray.add(docobj);
                }
                Log.consoleLog(ifr, "Inside mAddDocumentSubProcess Documents" + docarray);
                ifr.addDataToGrid(control, docarray);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mAddDocumentSubProcess:" + e);
            Log.errorLog(ifr, "Exception In mAddDocumentSubProcess:" + e);
        }
        return "";
    }

    public void setFeeChargesCalculation(IFormReference ifr) {
        String amt = ifr.getValue("QNL_FEE_CHARGES_Amount").toString();
        String con = ifr.getValue("QNL_FEE_CHARGES_Concession").toString();
        BigDecimal Amount = new BigDecimal(amt.equalsIgnoreCase("") ? "0" : amt);
        BigDecimal concession = new BigDecimal(con.equalsIgnoreCase("") ? "0" : con);
        BigDecimal constant = new BigDecimal(100);
        BigDecimal ConcAmt = (Amount.multiply(concession)).divide(constant, 2, RoundingMode.HALF_UP);
        Log.consoleLog(ifr, "ConcAmt:" + ConcAmt);
        BigDecimal NetAmt = Amount.subtract(ConcAmt);
        Log.consoleLog(ifr, "NetAmt:" + NetAmt);
        ifr.setValue("QNL_FEE_CHARGES_ConcessionAmt", String.valueOf(NetAmt));
        String gst = ifr.getValue("QNL_FEE_CHARGES_GST").toString();
        Log.consoleLog(ifr, "gst:" + gst);
        BigDecimal GSTAmt = new BigDecimal(gst.equalsIgnoreCase("") ? "0" : gst);
        Log.consoleLog(ifr, "GSTAmt:" + GSTAmt);
        BigDecimal Gsttotal = (NetAmt.multiply(GSTAmt)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        Log.consoleLog(ifr, "Gsttotal Amount:" + Gsttotal);
        BigDecimal total = NetAmt.add(Gsttotal);
        Log.consoleLog(ifr, "total Amount:" + total);
        ifr.setValue("QNL_FEE_CHARGES_fee_amount", String.valueOf(total));
        String query = ConfProperty.getQueryScript("FeeChargesAmountFetching").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId())
                .replaceAll("#FeeCode#", ifr.getValue("QNL_FEE_CHARGES_FeeCode").toString());
        List<List<String>> result = cf.mExecuteQuery(ifr, query, "FeeChargesProductFetching:");
        if (result.size() > 0) {
            BigDecimal MinAmount = new BigDecimal(result.get(0).get(0).equalsIgnoreCase("") ? "0" : result.get(0).get(0));
            BigDecimal MaxAmount = new BigDecimal(result.get(0).get(1).equalsIgnoreCase("") ? "0" : result.get(0).get(1));
            if (total.compareTo(MaxAmount) > 0) {
                ifr.setValue("QNL_FEE_CHARGES_Total", String.valueOf(MaxAmount));
            } else if (total.compareTo(MinAmount) < 0) {
                ifr.setValue("QNL_FEE_CHARGES_Total", String.valueOf(MinAmount));
            } else {
                ifr.setValue("QNL_FEE_CHARGES_Total", String.valueOf(total));
            }
        }
    }

    public String mCommonValueFromTable(IFormReference ifr, String QueryName) {
        try {
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = ConfProperty.getQueryScript(QueryName).replaceAll("#PID#", PID);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, QueryName + " Query:");
            String value = result.get(0).get(0);
            if (value.equalsIgnoreCase("") || value.equalsIgnoreCase("null")) {
                value = "0";
            }
            Log.consoleLog(ifr, "value:" + value);
            return value;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mCommonValueFromTable " + QueryName + ":" + e);
            Log.errorLog(ifr, "Exception in mCommonValueFromTable " + QueryName + ":" + e);
        }
        return "";
    }

    public String mCommonChangeOutstandingAmount(IFormReference ifr, String OutCheckFieldID,
            String OutAmtFieldID, String totalAmountFieldId) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mCommonChangeOutstandingAmount");
            String amt = ifr.getValue(totalAmountFieldId).toString();
            String outstandingAmount = ifr.getValue(OutCheckFieldID).toString();
            BigDecimal Amount = new BigDecimal(amt.equalsIgnoreCase("") ? "0" : amt);
            BigDecimal OSA = new BigDecimal(outstandingAmount.equalsIgnoreCase("") ? "0" : outstandingAmount);
            BigDecimal OutstandingAmount = OSA.subtract(Amount);
            if (OutstandingAmount.compareTo(BigDecimal.ZERO) >= 0) {
                ifr.setValue(OutAmtFieldID, String.valueOf(OutstandingAmount));
            } else {
                ifr.setValue(totalAmountFieldId, "");
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, totalAmountFieldId, "error", "Amount should be less than Outstanding Amount"));
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mCommonChangeOutstandingAmount:" + e);
            Log.errorLog(ifr, "Exception In mCommonChangeOutstandingAmount:" + e);
        }
        return "";
    }

    public String mCommonlistViewLoadCollection(IFormReference ifr, String QueryName, String totalAmtFieldID, String GridID,
            String AmtFieldID, String OutFieldID, String OutCheckFieldID) {//Checked
        try {
            Log.consoleLog(ifr, "Inside mCommonlistViewLoadCollection");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String sumQuery = ConfProperty.getQueryScript(QueryName).replaceAll("#PID#", PID);
            List<List<String>> sumQueryList = cf.mExecuteQuery(ifr, sumQuery, "Total charges amount");
            if (sumQueryList.size() > 0) {
                Log.consoleLog(ifr, "Total charge amount: " + sumQueryList.get(0).get(0));
                ifr.setValue(totalAmtFieldID, sumQueryList.get(0).get(0));
                BigDecimal totalCharge = new BigDecimal(sumQueryList.get(0).get(0));
                int size = ifr.getDataFromGrid(GridID).size();
                BigDecimal totalAmt = new BigDecimal("0");
                for (int i = 0; i < size; i++) {
                    totalAmt = totalAmt.add(new BigDecimal(ifr.getTableCellValue(GridID, i, AmtFieldID).toString()));
                }
                totalCharge = totalCharge.subtract(totalAmt);
                ifr.setValue(OutFieldID, String.valueOf(totalCharge));
                BigDecimal totalAmount = new BigDecimal(ifr.getValue(AmtFieldID).toString().equalsIgnoreCase("") ? "0" : ifr.getValue(AmtFieldID).toString());
                ifr.setValue(OutCheckFieldID, String.valueOf(totalCharge.add(totalAmount)));
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mCommonlistViewLoadCollection:" + e);
            Log.errorLog(ifr, "Exception In mCommonlistViewLoadCollection:" + e);
        }
        return "";
    }

    public void mSetDeviationLevel(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside mSetDeviationLevel:");
        try {
            String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
            String DeviationLevelValue = ConfProperty.getQueryScript("DeviationLevelValue").replaceAll("#PID#", PID);
            List<List<String>> DeviationLevelValueResult = cf.mExecuteQuery(ifr, DeviationLevelValue, "Query for DeviationLevelValue");
            String DevLevel = "";
            if (DeviationLevelValueResult.size() > 0) {
                DevLevel = DeviationLevelValueResult.get(0).get(0);
            }
            String ApprovalMatrixLevelValue = ConfProperty.getQueryScript("ApprovalMatrixLevelValue").replaceAll("#PID#", PID);
            List<List<String>> ApprovalMatrixLevelValueResult = cf.mExecuteQuery(ifr, ApprovalMatrixLevelValue, "Query for ApprovalMatrixLevelValue");
            String Applevel = "";
            if (ApprovalMatrixLevelValueResult.size() > 0) {
                Applevel = ApprovalMatrixLevelValueResult.get(0).get(0);
            }
            String FinalMaxLevel = "";
            if (DevLevel.equalsIgnoreCase("")) {
                if (Applevel.equalsIgnoreCase("")) {
                } else {
                    FinalMaxLevel = Applevel;
                }
            } else {
                if (Applevel.equalsIgnoreCase("")) {
                    FinalMaxLevel = DevLevel;
                } else {
                    BigDecimal DeviationLevel = new BigDecimal(DevLevel);
                    BigDecimal Approvallevel = new BigDecimal(Applevel);
                    if (DeviationLevel.compareTo(Approvallevel) > 0) {
                        FinalMaxLevel = DevLevel;
                    } else {
                        FinalMaxLevel = Applevel;
                    }
                }
            }
            Log.consoleLog(ifr, "FinalDeviationLevel value" + FinalMaxLevel);
            ifr.setValue("DevMaxLevel", FinalMaxLevel);
            ifr.setValue("DevCurrentLevel", "");
            String query = ConfProperty.getQueryScript("FindDevLevelMin").replaceAll("#DEVLEVEL#", FinalMaxLevel);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for FindDevLevelMin");
            if (result.size() > 0) {
                ifr.setValue("DevCurrentLevel", result.get(0).get(0));
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mSetDeviationLevel:" + e);
            Log.errorLog(ifr, "Exception In mSetDeviationLevel:" + e);
        }
    }

    public void mSetCurrentDeviationLevel(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside mSetCurrentDeviationLevel:");
        try {
            String maxlevel = ifr.getValue("DevMaxLevel").toString();
            String currentlevel = ifr.getValue("DevCurrentLevel").toString();
            if (ifr.getValue("Decision").toString().equalsIgnoreCase("S")) {
                String query = ConfProperty.getQueryScript("FindDevLevelMinMax").replaceAll("#DEVMaxLEVEL#", maxlevel)
                        .replaceAll("#DEVCurrentLEVEL#", currentlevel);
                List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for FindDevLevelMin");
                if (result.size() > 0) {
                    ifr.setValue("DevCurrentLevel", result.get(0).get(0));
                }
            } else if (ifr.getValue("Decision").toString().equalsIgnoreCase("SB")) {
                String query = ConfProperty.getQueryScript("FindDevLevelLessCurrent")
                        .replaceAll("#DEVCurrentLEVEL#", currentlevel);
                List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for FindDevLevelMin");
                if (result.size() > 0) {
                    ifr.setValue("DevCurrentLevel", result.get(0).get(0));
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mSetCurrentDeviationLevel:" + e);
            Log.errorLog(ifr, "Exception In mSetCurrentDeviationLevel:" + e);
        }
    }

    public String mCheckCommittee(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside mCheckCommittee:");
        try {
            String maxlevel = ifr.getValue("DevMaxLevel").toString();
            String currentlevel = ifr.getValue("DevCurrentLevel").toString();
            if (maxlevel.equalsIgnoreCase(currentlevel)) {
                Boolean checkflag = true;
                String query = ConfProperty.getQueryScript("CheckCommitteeAvailable").replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId());
                List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for FindDevLevelName:");
                if (result.size() > 0) {
                    String CommitteeStatus = result.get(0).get(0);
                    if ((CommitteeStatus.equalsIgnoreCase("Pending") || CommitteeStatus.equalsIgnoreCase("Initiated"))
                            && ifr.getValue("DecisionValue").toString().equalsIgnoreCase("S")) {
                    } else if (CommitteeStatus.equalsIgnoreCase("Initiated")) {
                    } else {
                        checkflag = false;
                    }
                }
                if (checkflag) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", cf.showMessage(ifr, "QNL_BASIC_INFO_ApplicantType", "error", "Committee Workflow Not Completed!"));
                    return message.toString();
                } else {
                    ifr.setValue("CommitteeStatus", "Done");
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mCheckCommittee:" + e);
            Log.errorLog(ifr, "Exception In mCheckCommittee:" + e);
        }
        return "";
    }

    public void mSetChildCommiteeStatus(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside mSetChildCommiteeStatus:");
        try {
            String query = ConfProperty.getQueryScript("FetchInsertionOrderIDCommittee").
                    replaceAll("#PID#", ifr.getObjGeneralData().getM_strProcessInstanceId())
                    .replaceAll("#CHILDWORKITEM#", ifr.getObjGeneralData().getM_strWorkitemId());
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for FetchInsertionOrderIDCommittee:");
            if (result.size() > 0) {
                query = ConfProperty.getQueryScript("updateLOSSubCommittee").
                        replaceAll("#INSERTIONORDERID#", result.get(0).get(0)).replaceAll("#CHILDWORKITEM#", ifr.getObjGeneralData().getM_strWorkitemId())
                        .replaceAll("#COMMITTEESTATUS#", "Completed");
                int resultCount = ifr.saveDataInDB(query);
                Log.consoleLog(ifr, "resultCount" + resultCount);
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mSetChildCommiteeStatus:" + e);
            Log.errorLog(ifr, "Exception In mSetChildCommiteeStatus:" + e);
        }
    }

    public void mImplNumberToWorkds(IFormReference ifr) {
        if (ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt").toString().isEmpty()) {
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords", "");
        } else {
            String getamount = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmt").toString();
            ifr.setValue("QL_LEAD_DET_LoanAmount", getamount);
            float amount = Float.parseFloat(getamount);
            int data = (int) amount;
            String result = convert(data);
            ifr.setValue("QNL_LOS_PROPOSED_FACILITY_ReqLoanAmtWords", result);
        }
    }

    public String convert(int n) {
        if (n < 0) {
            return "Minus " + convert(-n);
        } else if (n < 20) {
            return units[n];
        } else if (n < 100) {
            return tens[n / 10] + ((n % 10 != 0) ? " " : "") + units[n % 10];
        } else if (n < 1000) {
            return units[n / 100] + " Hundred" + ((n % 100 != 0) ? " " : "") + convert(n % 100);
        } else if (n < 100000) {
            return convert(n / 1000) + " Thousand" + ((n % 10000 != 0) ? " " : "") + convert(n % 1000);
        } else if (n < 10000000) {
            return convert(n / 100000) + " Lakh" + ((n % 100000 != 0) ? " " : "") + convert(n % 100000);
        }
        return convert(n / 10000000) + " Crore" + ((n % 10000000 != 0) ? " " : "") + convert(n % 10000000);
    }

    public String mChangeConcession(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside ImplconChangeConcession:");
        String concession = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_Concession").toString();
        String campconcession = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ConsessionRoi").toString();
        String roi = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_ROI").toString();
        String criteriaConcession = ifr.getValue("QNL_LOS_PROPOSED_FACILITY_TotalConcessionAsPerCriteria").toString();
        BigDecimal ROI = new BigDecimal(roi.equalsIgnoreCase("") ? "0" : roi);
        BigDecimal Concession = new BigDecimal(concession.equalsIgnoreCase("") ? "0" : concession);
        BigDecimal CampConcession = new BigDecimal(campconcession.equalsIgnoreCase("") ? "0" : campconcession);
        BigDecimal CriteriaConcession = new BigDecimal(criteriaConcession.equalsIgnoreCase("") ? "0" : criteriaConcession);
        BigDecimal Effectiverate = ROI.subtract(Concession).subtract(CampConcession).subtract(CriteriaConcession);
        ifr.setValue("QNL_LOS_PROPOSED_FACILITY_EffectiveROI", String.valueOf(Effectiverate));
        return validateConsession(ifr, String.valueOf(Effectiverate));
    }

    public void mSetNetIncome(IFormReference ifr, String grossIncID, String deductID, String NetIncID) {
        try {
            String GMI = ifr.getValue(grossIncID).toString();
            Log.consoleLog(ifr, "Gross Income" + GMI);
            String MD = ifr.getValue(deductID).toString();
            Log.consoleLog(ifr, "Deduction Income" + MD);
            BigDecimal GMI1 = new BigDecimal(GMI.equalsIgnoreCase("") ? "0.0" : GMI);
            BigDecimal MD1 = new BigDecimal(MD.equalsIgnoreCase("") ? "0.0" : MD);
            BigDecimal NMI = GMI1.subtract(MD1);
            Log.consoleLog(ifr, "NET INCOME" + NMI);
            ifr.setValue(NetIncID, String.valueOf(NMI));
            ifr.setStyle(NetIncID, "disable", "true");
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mSetNetIncome method" + e);
            Log.errorLog(ifr, "Error in mSetNetIncome method" + e);
        }
    }

    public String mTotalExpCheck(IFormReference ifr, String FieldID) {
        Log.consoleLog(ifr, "inside mTotalExpCheck");
        String messageValue = "";
        String ageF = ifr.getValue("QNL_BASIC_INFO_CL_BASIC_INFO_I_Age").toString();
        if (ageF.equalsIgnoreCase("")) {
            messageValue = "Please Enter the Age First";
            ifr.setValue(FieldID, "");
        } else {
            String occpType = ifr.getValue(FieldID).toString();
            Log.consoleLog(ifr, "value of occpType= " + occpType);
            int yrs = Integer.parseInt(occpType);
            Log.consoleLog(ifr, "value of yrs= " + yrs);
            int age = Integer.parseInt(ageF);
            Log.consoleLog(ifr, "value of age= " + age);
            if (yrs >= age) {
                messageValue = "Total Work Experince Cannot be Greater or less than the Age of Employee";
                ifr.setValue(FieldID, "");
            }
        }
        if (!(messageValue.equalsIgnoreCase(""))) {
            JSONObject message = new JSONObject();
            message.put("showMessage", cf.showMessage(ifr, "QNL_BASIC_INFO_CNL_OCCUPATION_INFO_NoYrsInBusiness", "error", messageValue));
            return message.toString();
        }
        return "";
    }

    public String mExecuteBRMSMargin_LTV(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "Inside mImplBRMSMargin_LTV:");
            String WI = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String collateralCost = ifr.getValue("QL_TOP_COLLATDET_Cost_of_collateral").toString();
            if (collateralCost.equalsIgnoreCase("")) {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "QL_TOP_COLLATDET_Cost_of_collateral", "error", "Please Enter Cost of Collateral"));
                return message.toString();
            }
            String query = ConfProperty.getQueryScript("FetchingProductFacility").replaceAll("#WI#", WI);
            List<List<String>> data = cf.mExecuteQuery(ifr, query, "Proposed query:");
            String SCHEMEID, loanamt, EFFECTIVEROI, TENURE;
            if (data.size() > 0) {
                SCHEMEID = data.get(0).get(0);
                loanamt = data.get(0).get(1);
                EFFECTIVEROI = data.get(0).get(2);
                TENURE = data.get(0).get(3);
            } else {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "QL_DEAL_LTV", "error", "Please Enter Data in Proposed Grid"));
                return message.toString();
            }
            query = ConfProperty.getQueryScript("FetchingOccupationInfo").replaceAll("#PID#", WI);
            data = cf.mExecuteQuery(ifr, query, "FetchingOccupationInfo query:");
            String occupationtype = "";
            BigDecimal monthlyIncome = new BigDecimal("0");
            BigDecimal netAnnualIncome = new BigDecimal("0");
            BigDecimal grossMonthlyIncome = new BigDecimal("0");
            BigDecimal grossAnnualIncome = new BigDecimal("0");
            BigDecimal deductionIncome = new BigDecimal("0");
            BigDecimal deductionAnnualIncome = new BigDecimal("0");
            if (data.size() > 0) {
                for (int i = 0; i < data.size(); i++) {
                    String minc = data.get(i).get(2);
                    String gminc = data.get(i).get(4);
                    String dedinc = data.get(i).get(6);
                    String monannualinc = data.get(i).get(3);
                    String grossannualminc = data.get(i).get(5);
                    String dedannualinc = data.get(i).get(7);
                    if (data.get(i).get(0).equalsIgnoreCase("B")) {
                        occupationtype = data.get(i).get(1);
                    }
                    if (data.get(i).get(1).equalsIgnoreCase("Salaried") || data.get(i).get(1).equalsIgnoreCase("PEN")) {
                        monthlyIncome = monthlyIncome.add(new BigDecimal(minc.equalsIgnoreCase("") ? "0" : minc));
                        grossMonthlyIncome = grossMonthlyIncome.add(new BigDecimal(gminc.equalsIgnoreCase("") ? "0" : gminc));
                        deductionIncome = deductionIncome.add(new BigDecimal(dedinc.equalsIgnoreCase("") ? "0" : dedinc));
                    } else {
                        netAnnualIncome = netAnnualIncome.add(new BigDecimal(monannualinc.equalsIgnoreCase("") ? "0" : monannualinc));
                        grossAnnualIncome = grossAnnualIncome.add(new BigDecimal(grossannualminc.equalsIgnoreCase("") ? "0" : grossannualminc));
                        deductionAnnualIncome = deductionAnnualIncome.add(new BigDecimal(dedannualinc.equalsIgnoreCase("") ? "0" : dedannualinc));
                    }
                }
            } else {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "QL_DEAL_LTV", "error", "Please Enter Data in Party Details"));
                return message.toString();
            }
            netAnnualIncome = netAnnualIncome.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
            monthlyIncome = monthlyIncome.add(netAnnualIncome);
            grossAnnualIncome = grossAnnualIncome.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
            grossMonthlyIncome = grossMonthlyIncome.add(grossAnnualIncome);
            deductionAnnualIncome = deductionAnnualIncome.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
            deductionIncome = deductionIncome.add(deductionAnnualIncome);
            String messageValue = "";
            if (occupationtype.equalsIgnoreCase("")) {
                messageValue = "Please Enter Occupation for Main Applicant in Party Details!";
            } else {
                JSONObject result = cf.executeBRMSRule(ifr, "Margin_LTV", SCHEMEID + "," + loanamt);
                Log.consoleLog(ifr, "BRMS Result of  Margin_LTV:" + result);
                if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                    String margin = cf.getJsonValue(result, "margin");
                    String ltv = cf.getJsonValue(result, "ltv");
                    Log.consoleLog(ifr, "margin is=  " + margin + " & LTV is= " + ltv);
                    ifr.setValue("QL_DEAL_Margin", margin);
                    ifr.setValue("QL_DEAL_LTV", ltv);

                    result = cf.executeBRMSRule(ifr, "PermissibleDeduction", SCHEMEID + "," + occupationtype + "," + monthlyIncome);
                    Log.consoleLog(ifr, "BRMS Result of  PermissibleDeduction:" + result);
                    if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                        String permdeduction = cf.getJsonValue(result, "limit");
                        Log.consoleLog(ifr, "permissible limit is=  " + permdeduction);
                        ifr.setValue("QL_DEAL_PermissibleDeduction", permdeduction);
                        double d = Double.parseDouble(permdeduction) / 100;
                        permdeduction = Double.toString(d);
                        result = cf.executeBRMSRule(ifr, "AmountForDeduction", grossMonthlyIncome + "," + permdeduction + "," + deductionIncome);
                        Log.consoleLog(ifr, "BRMS Result of AmountForDeduction:" + result);
                        if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                            String amtDedction = cf.getJsonValue(result, "deductedresult");
                            Log.consoleLog(ifr, "amtDedction is=  " + amtDedction);
                            ifr.setValue("QL_DEAL_AmountAvailableforDeduction", amtDedction);

                            double principal = 100000;
                            double rate = Double.parseDouble(EFFECTIVEROI);
                            double time = Double.parseDouble(TENURE);
                            rate = rate / (12 * 100);
                            double emi = (principal * (rate * Math.pow(1 + rate, time))) / (Math.pow(1 + rate, time) - 1);
                            BigDecimal emiperlc = BigDecimal.valueOf(emi);
                            double d1 = Double.parseDouble(ltv) / 100;
                            ltv = Double.toString(d1);
                            result = cf.executeBRMSRule(ifr, "IndicativeEligibility", amtDedction + "," + collateralCost + "," + emiperlc + "," + grossMonthlyIncome + "," + ltv + "," + monthlyIncome + "," + loanamt);
                            Log.consoleLog(ifr, "BRMS Result of IndicativeEligibility:" + result);
                            if (cf.getJsonValue(result, "status").equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
                                String indiEligLimit = cf.getJsonValue(result, "output");
                                Log.consoleLog(ifr, "indiEligLimit is=  " + indiEligLimit);
                                String outA = cf.getJsonValue(result, "inout1");
                                String outD = cf.getJsonValue(result, "inout4");
                                Log.consoleLog(ifr, "OutA is=  " + outA);
                                Log.consoleLog(ifr, "OutD is=  " + outD);
                                BigDecimal A = new BigDecimal(outA);
                                BigDecimal D = new BigDecimal(outD);
                                BigDecimal min = A.min(D);
                                Log.consoleLog(ifr, "Minimum= " + min);
                                long number = Double.valueOf(String.valueOf(min)).longValue();
                                ifr.setValue("QL_DEAL_DSCR", String.valueOf(number));
                                ifr.setValue("QL_DEAL_IndicativeEligibleLimit", indiEligLimit);
                            } else {
                                messageValue = AcceleratorConstants.TRYCATCHERRORBRMS;
                            }
                        } else {
                            messageValue = AcceleratorConstants.TRYCATCHERRORBRMS;
                        }
                    } else {
                        messageValue = AcceleratorConstants.TRYCATCHERRORBRMS;
                    }
                } else {
                    messageValue = AcceleratorConstants.TRYCATCHERRORBRMS;
                }
            }
            if (!(messageValue.equalsIgnoreCase(""))) {
                JSONObject message = new JSONObject();
                ifr.setValue("QL_DEAL_Margin", "");
                ifr.setValue("QL_DEAL_LTV", "");
                ifr.setValue("QL_DEAL_PermissibleDeduction", "");
                ifr.setValue("QL_DEAL_AmountAvailableforDeduction", "");
                ifr.setValue("QL_DEAL_IndicativeEligibleLimit", "");
                message.put("showMessage", cf.showMessage(ifr, "QL_DEAL_LTV", "error", messageValue));
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception Inside mImplBRMSMargin_LTV" + e);
            Log.errorLog(ifr, "Exception Inside mImplBRMSMargin_LTV" + e);
        }
        return "";
    }

    public String mAccClickDAMCSubProcess(IFormReference ifr) {
        try {
            Log.consoleLog(ifr, "Inside mAccClickDAMCSubProcess");
            String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "PID : " + PID);
            String documentQuery = ConfProperty.getQueryScript("DAMCDOCUMENTQUERY").replaceAll("#PID#", PID);
            List<List<String>> result = cf.mExecuteQuery(ifr, documentQuery, "Query for InsertionOrderId");
            if (result.size() > 0) {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "There is no document uploaded for some rows!"));
                return message.toString();
            }

            documentQuery = ConfProperty.getQueryScript("DAMCDOCUMENTFETCHINGQUERY").replaceAll("#PID#", PID);
            result = cf.mExecuteQuery(ifr, documentQuery, "Query for InsertionOrderId");
            if (result.size() > 0) {
                Log.consoleLog(ifr, "DAMC processDefId  ====");
                String attributes = "<PARENTWINO>" + PID + "</PARENTWINO>";
                Log.consoleLog(ifr, "attributes  ====" + attributes);
                UploadCreateWI ucwi = new UploadCreateWI();
                String processDefId = ConfProperty.getCommonPropertyValue("DAMCSubProcessDefId");
                Log.consoleLog(ifr, "DAMC processDefId  ====" + processDefId);
                String pid = ucwi.uploadWI(ifr, attributes, processDefId, "1");
                if (pid.isEmpty()) {
                    JSONObject message = new JSONObject();
                    message.put("showMessage", this.cf.showMessage(ifr, "", "error", "SubProcess Not created Please Try Again..!"));
                    return message.toString();
                } else {
                    for (int i = 0; i < result.size(); i++) {
                        String dcoumentName = result.get(i).get(0);
                        String dmsName = result.get(i).get(1);
                        String DOCID = result.get(i).get(2);
                        String INSERTIONORDERID = result.get(i).get(3);
                        String remarks = result.get(i).get(4);
                        String FINALSTATUS = result.get(i).get(5);
                        String SUBWINO = result.get(i).get(6);
                        String documentInsertQuey = ConfProperty.getQueryScript("DAMCDOCINSERTQUERY").replaceAll("#DOCUMENTNAME#", dcoumentName)
                                .replaceAll("#DOCUMENTTYPE#", dmsName).replaceAll("#DOCID#", DOCID).replaceAll("#ParentInsertID#", INSERTIONORDERID)
                                .replaceAll("#Remarks#", remarks).replaceAll("#DECISION#", FINALSTATUS).replaceAll("#CHILD_WI_NO#", SUBWINO).
                                replaceAll("#PID#", pid);
                        Log.consoleLog(ifr, "documentInsertQuey query  ====" + documentInsertQuey);
                        int result1 = ifr.saveDataInDB(documentInsertQuey);
                        Log.consoleLog(ifr, "result  ====" + result1);
                    }
                }
            } else {
                JSONObject message = new JSONObject();
                message.put("showMessage", cf.showMessage(ifr, "BTN_SUBMIT", "error", "There is no document present for this WI please upload in grid to proceed!"));
                return message.toString();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mAccClickDAMCSubProcess method" + e);
            Log.errorLog(ifr, "Exception in mAccClickDAMCSubProcess method" + e);
        }
        return "";
    }

    public String generatedoc(IFormReference ifr, String docid, String journeyName) {
        try {
            Log.consoleLog(ifr, "inside docGen  : ");
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode userNode = objectMapper.createObjectNode();
            Log.consoleLog(ifr, "userNode: " + userNode.toString());
            userNode.put("Mode", "SINGLE");
            userNode.put("DocID", docid);
            userNode.put("callFrom", "Backoffice");
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
            Log.consoleLog(ifr, "userDoc: " + userDoc.get("MESSAGE"));
            //Log.consoleLog(ifr, "userDoc: test " + userDoc.get(1));
            return userDoc.get("MESSAGE").toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in docGen " + e);
            Log.errorLog(ifr, "Error occured in docGen " + e);
            return "1";
        }

    }

    public String generatedocCB(IFormReference ifr, String docid, String journeyName) {
        try {
            Log.consoleLog(ifr, "inside docGen  : ");
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode userNode = objectMapper.createObjectNode();
            Log.consoleLog(ifr, "userNode: " + userNode.toString());
            userNode.put("Mode", "SINGLE");
            userNode.put("DocID", docid);
            userNode.put("callFrom", "Backoffice");
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
             Log.consoleLog(ifr, "userDoc: DocIndex " + userDoc.get("DocIndex").asText() );
            if (!userDoc.get("DocIndex").asText().isEmpty())
            {
            Log.consoleLog(ifr, "userDoc: " + userDoc.get("MESSAGE").asText() );
            Log.consoleLog(ifr, "userDoc: " + userDoc.get("DocIndex").asText() );
            //Log.consoleLog(ifr, "userDoc: test " + userDoc.get(1));
            return userDoc.get("MESSAGE").asText() + "," + userDoc.get("DocIndex").asText();
            }
            else{
              return "Error,GenerationFailed"+userDoc.get("MESSAGE").asText();
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Error occured in docGen " + e);
            Log.errorLog(ifr, "Error occured in docGen " + e);
            return "Error,GenerationFailed"+e.getMessage();
        }

    }

    //Added by Ahmed on 03-06-2024 for getting Minimum Bureadu Score
    public String getMaxTotalEMIAmount(IFormReference ifr) {

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String qry = "SELECT TOTEMIAMOUNT,BUREAUTYPE FROM LOS_CAN_IBPS_BUREAUCHECK "
                + "WHERE PROCESSINSTANCEID = '" + processInstanceId + "' "
                + "AND TOTEMIAMOUNT = (SELECT MAX(TOTEMIAMOUNT) "
                + "FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID = '" + processInstanceId + "')";
        Log.consoleLog(ifr, "qry:" + qry);
        List< List< String>> Result = ifr.getDataFromDB(qry);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());

        if (Result.size() > 0) {
            String totalEMIAmnt = Result.get(0).get(0);
            String bureauType = Result.get(0).get(1);
            return bureauType + "-" + totalEMIAmnt;
        }

        return RLOS_Constants.ERROR;
    }

    //added by hemanth for doc genaration portal on 06/06/2024
    public String mGenerateDoc(IFormReference ifr, String control, String event, String value) {

        PortalCommonMethods pcm = new PortalCommonMethods();
        try {

            Log.consoleLog(ifr, "inside mGenerateDoc::");
            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String ActivityName = ifr.getActivityName();
            String schemeID = pcm.mGetSchemeID(ifr, processInstanceId);
            Log.consoleLog(ifr, "ProcessInstanceId==>" + processInstanceId);
            String queryL = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", processInstanceId);
            String loan_selected = "";
            List<List<String>> loanSelected = cf.mExecuteQuery(ifr, queryL, "Execute query for fetching loan selected ");
            if (!loanSelected.isEmpty()) {
                loan_selected = loanSelected.get(0).get(0);
            }
            // String mDocGenProceed = ifr.getValue("P_OutwardDocument_Button").toString();
            int count = ifr.getDataFromGrid("ALV_GENERATE_DOCUMENT").size();
            String docName = "";
            String returnMessageFromDocGen = "";
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    docName = ifr.getTableCellValue("ALV_GENERATE_DOCUMENT", i, 3);
                    Log.consoleLog(ifr, "docName ==>" + docName);
                    returnMessageFromDocGen = generatedocCB(ifr, docName, "BUDGET");
                    if (returnMessageFromDocGen.equalsIgnoreCase(RLOS_Constants.ERROR)) {
                        return pcm.returnErrorcustmessage(ifr, "Document Generation Failed ");
                    }
                }
            } else {
                returnMessageFromDocGen = "Document is not added!";//need exact message
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception" + e);
            Log.errorLog(ifr, "Excetion" + e);
            return pcm.returnErrorHold(ifr);
        }
        return "";
    }

    public void mSetDeviationLevelCB(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside mSetDeviationLevelCB:");
        try {
            String PID = (ifr.getObjGeneralData()).getM_strProcessInstanceId();
            String DeviationLevelValue = ConfProperty.getQueryScript("DeviationLevelValue").replaceAll("#PID#", PID);
            List<List<String>> DeviationLevelValueResult = cf.mExecuteQuery(ifr, DeviationLevelValue, "Query for DeviationLevelValue");
            String DevLevel = "0";
            if (DeviationLevelValueResult.size() > 0 && !(DeviationLevelValueResult.get(0).get(0).equalsIgnoreCase(""))) {
                DevLevel = DeviationLevelValueResult.get(0).get(0);
            }
            ifr.setValue("cdevminlevel", "0");
            ifr.setValue("cdevmaxlevel", DevLevel);
            ifr.setValue("cdevcurrentlevel", "0");
            String query = "select HIERARCHYLEVEL from LOS_M_ORGANISATIONAL_HIERARCHY where ORGANISATIONLEVEL='RO'";
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for FindDevLevelMin");
            if (result.size() > 0) {
                ifr.setValue("cdevcurrentlevel", result.get(0).get(0));
                ifr.setValue("cdevminlevel", result.get(0).get(0));
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mSetDeviationLevel:" + e);
            Log.errorLog(ifr, "Exception In mSetDeviationLevel:" + e);
        }
    }

    public void mSetCurrentDeviationLevelCB(IFormReference ifr) {
        Log.consoleLog(ifr, "Inside mSetCurrentDeviationLevel:");
        try {
            String maxlevel = ifr.getValue("cdevmaxlevel").toString();
            String currentlevel = ifr.getValue("cdevcurrentlevel").toString();
            if (ifr.getValue("DecisionValue").toString().equalsIgnoreCase("S") || ifr.getValue("DecisionValue").toString().equalsIgnoreCase("A")) {
                ifr.setValue("cdevcurrentlevel", "" + (Integer.parseInt(currentlevel) + 1));
                /*String query = "select DeviationLevelCode from LOS_NL_DeviationBRMS where DeviationLevelCode>= '" + currentlevel + "' and DeviationLevelCode<= '" + maxlevel + "' and PID='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "' order by DeviationLevelCode ASC";
                List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for FindDevLevelMin");
                if (result.size() > 0) {
                    if (result.get(0).get(0).equalsIgnoreCase(currentlevel)) {
                        ifr.setValue("cdevcurrentlevel", "" + (Integer.parseInt(result.get(0).get(0)) + 1));
                    } else {
                        ifr.setValue("cdevcurrentlevel", result.get(0).get(0));
                    }
                }*/
            } else if (ifr.getValue("DecisionValue").toString().equalsIgnoreCase("SB")) {
                ifr.setValue("cdevcurrentlevel", "" + (Integer.parseInt(currentlevel) - 1));
                /*String query = "select DeviationLevelCode from LOS_NL_DeviationBRMS where DeviationLevelCode<='" + currentlevel + "' and PID='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "' order by DeviationLevelCode desc";
                List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for FindDevLevelMin");
                if (result.size() > 0) {
                    if (result.get(0).get(0).equalsIgnoreCase(currentlevel)) {
                        ifr.setValue("cdevcurrentlevel", "" + (Integer.parseInt(result.get(0).get(0)) - 1));
                    } else {
                        ifr.setValue("cdevcurrentlevel", result.get(0).get(0));
                    }
                }*/
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception In mSetCurrentDeviationLevel:" + e);
            Log.errorLog(ifr, "Exception In mSetCurrentDeviationLevel:" + e);
        }
    }

    //Added by Aravindh on 19-06-2024 for getting Minimum Bureau Score For ApplicantType
    public String getMaxTotalEMIAmountCICDatas(IFormReference ifr, String ApplicantType) {

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String qry = "SELECT BUREAUTYPE, APPLICANT_TYPE, TOTEMIAMOUNT, EXP_CBSCORE, NVL(REGEXP_REPLACE(PAYHISTORYCOMBINED, '[^0-9]', ''), 0) AS replaced_column FROM LOS_CAN_IBPS_BUREAUCHECK "
                + "WHERE PROCESSINSTANCEID = '" + processInstanceId + "' "
                + " AND APPLICANT_TYPE = '" + ApplicantType + "' "
                + "AND TOTEMIAMOUNT = (SELECT MAX(TOTEMIAMOUNT) "
                + "FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID = '" + processInstanceId + "' AND APPLICANT_TYPE ='" + ApplicantType + "')";
        Log.consoleLog(ifr, "qry:" + qry);
        List< List< String>> Result = ifr.getDataFromDB(qry);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());

        if (Result.size() > 0) {
            String bureauType = Result.get(0).get(0);
            String AppclType = Result.get(0).get(1);
            String totalEMIAmnt = Result.get(0).get(2);
            String EXP_CBSCORE = Result.get(0).get(3);
            String PAYHISTORYCOMBINED = Result.get(0).get(4);

            return bureauType + "-" + AppclType + "-" + totalEMIAmnt + "-" + EXP_CBSCORE + "-" + PAYHISTORYCOMBINED;
        }

        return RLOS_Constants.ERROR;
    }

    //Added by Vandana on 02-07-2024 for getting Minimum Bureau Score For ApplicantType
    public String getMaxCICScoreDatas(IFormReference ifr, String ApplicantType) {

        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String qry = "SELECT BUREAUTYPE, APPLICANT_TYPE, TOTEMIAMOUNT, EXP_CBSCORE, NVL(REGEXP_REPLACE(PAYHISTORYCOMBINED, '[^0-9]', ''), 0) AS replaced_column ,SRNPAINP,SETTLEDHISTORY,GUARANTORNPAINP,GUARANTORWRITEOFFSETTLEDHIST FROM LOS_CAN_IBPS_BUREAUCHECK "
                + "WHERE PROCESSINSTANCEID = '" + processInstanceId + "' "
                + " AND APPLICANT_TYPE = '" + ApplicantType + "' "
                + "AND EXP_CBSCORE = (SELECT MIN(EXP_CBSCORE) "
                + "FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID = '" + processInstanceId + "' AND APPLICANT_TYPE ='" + ApplicantType + "')";
        Log.consoleLog(ifr, "qry:" + qry);
        List< List< String>> Result = ifr.getDataFromDB(qry);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());

        if (Result.size() > 0) {
            String bureauType = Result.get(0).get(0);
            String AppclType = Result.get(0).get(1);
            String totalEMIAmnt = Result.get(0).get(2);
            String EXP_CBSCORE = Result.get(0).get(3);
            String PAYHISTORYCOMBINED = Result.get(0).get(4);
            String npa_ip = Result.get(0).get(5);
            String settleHistory = Result.get(0).get(6);
            String GUARANTORNPAINP = Result.get(0).get(7);
            String GUARANTORWRITEOFFSETTLEDHIST = Result.get(0).get(8);
            return bureauType + "-" + AppclType + "-" + totalEMIAmnt + "-" + EXP_CBSCORE + "-" + PAYHISTORYCOMBINED + "-" + npa_ip + "-" + settleHistory + "-" + GUARANTORNPAINP + "-" + GUARANTORWRITEOFFSETTLEDHIST;
        }

        return RLOS_Constants.ERROR;
    }

    public static int calculateAge(LocalDate dob) {
        LocalDate curDate = LocalDate.now();
        if ((dob != null) && (curDate != null)) {
            return Period.between(dob, curDate).getYears();
        } else {
            return 0;
        }
    }
    
    public String getRetailExposureTotalFcr(IFormReference ifr) throws ParseException {
        Log.consoleLog(ifr, "inside getRetailExposureTotalFcr");
        String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String Data_Query = ConfProperty.getQueryScript("getRetailExposureTotalFcr").replaceAll("#WINAME#", ProcessInsanceId);
        List<List<String>> DataList = cf.mExecuteQuery(ifr, Data_Query, "getRetailExposureTotalFcr : ");
        Log.consoleLog(ifr, "Data_Query : " + DataList);
        String response = "";
        if (DataList.size() > 0) {
            response = DataList.get(0).get(0);
            Log.consoleLog(ifr, "response==>" + response);
        }
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(response);
 
        JSONObject body = (JSONObject) jsonObject.get("body");
        Log.consoleLog(ifr, "body : " + body);
        JSONObject customerResponse = (JSONObject) body.get("CustomerResponse");
        Log.consoleLog(ifr, "customerResponse : " + customerResponse);
        JSONObject xfaceCustomerBasicInquiryDTO = (JSONObject) customerResponse.get("XfaceCustomerBasicInquiryDTO");
        Log.consoleLog(ifr, "xfaceCustomerBasicInquiryDTO : " + xfaceCustomerBasicInquiryDTO);
        // Retrieve the RetailExposureTotalFcr value
        String retailExposureTotalFcr = xfaceCustomerBasicInquiryDTO.get("RetailExposureTotalFcr").toString();
        Log.consoleLog(ifr, "retailExposureTotalFcr : " + retailExposureTotalFcr);
 
        return retailExposureTotalFcr;
    }

    public String getClobSnippetData(IFormReference ifr, String inputString) {

        try {

            if (inputString == null) {
                return "TO_CLOB('')";
            } else {

                Log.consoleLog(ifr, "Data.length()=>" + inputString.length());
                // String inputString = "Your input string goes here...";
                int maxLength = 4000;
                String Segment = "";
                if (inputString.length() > maxLength) {
                    int numChunks = (int) Math.ceil((double) inputString.length() / maxLength);
                    String[] chunks = new String[numChunks];

                    for (int i = 0; i < numChunks; i++) {
                        int startIndex = i * maxLength;
                        int endIndex = Math.min((i + 1) * maxLength, inputString.length());
                        chunks[i] = inputString.substring(startIndex, endIndex);
                    }

                    // Process or print the chunks as needed
                    for (String chunk : chunks) {
                        if (!Segment.equalsIgnoreCase("")) {
                            Segment = Segment + "||";
                        }

                        //   System.out.println(chunk.length() + "==>Chunk: " + chunk);
                        Segment = Segment + "TO_CLOB('" + chunk.replace("'", "") + "')";

                    }
                } else {
                    System.out.println("Input string is not greater than 4000 characters.");
                    Segment = Segment + "TO_CLOB('" + inputString.replace("'", "") + "')";
                }

                //System.out.println("Segment==>" + Segment);
                return Segment;

            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception==>" + e);
        }

        ///throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        return "";
        ///throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

		public String getConfigValue(IFormReference ifr, String productCode, String subProductCode, String paramType,
			String paramName) {
		String query = "select PARAMVALUE from los_mst_configs where " + "productcode='" + productCode
				+ "' AND PARAMTYPE='" + paramType + "' AND PARAMNAME='" + paramName + "'";
		List<List<String>> result = ifr.getDataFromDB(query);
		if (result.size() > 0 && result.get(0).get(0) != null) {
			return result.get(0).get(0);
		}
		return "0";
	}

	    public String getConstantValue(IFormReference ifr, String constType, String constName) {
	        String query = "SELECT CONSTVALUE FROM LOS_MST_CONSTANTS WHERE CONSTTYPE='" + constType + "' AND CONSTNAME='" + constName + "'";
	        Log.consoleLog(ifr, "query" + query);
	        List<List<String>> result = ifr.getDataFromDB(query);
	        if (result.size() > 0 && result.get(0).get(0) != null) {
	            Log.consoleLog(ifr, "result" + result.get(0).get(0));
	            return result.get(0).get(0);
	        }
	        Log.consoleLog(ifr, "result" + result.get(0).get(0));
	        return "0";
	    }

	    public double parseToDouble(String input) {
	        if (input == null || input.trim().isEmpty() || "null".equalsIgnoreCase(input)) {
	            return 0.0; // or return 0.0 if you prefer
	        }
	        try {
	            return Double.parseDouble(input);
	        } catch (Exception e) {

	        }
	        return 0.0; // or return 0.0 if you prefer
	    }
     
}
