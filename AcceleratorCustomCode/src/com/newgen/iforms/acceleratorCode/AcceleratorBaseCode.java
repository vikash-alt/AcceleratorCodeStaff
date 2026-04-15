/*      
 '------------------------------------------------------------------------------
 '      NEWGEN SOFTWARE TECHNOLOGIES LIMITED
 '   Group                  : CIG
 '   Product/Project/Utility: RLOS_ACCELERATOR
 '   Module                 : RLOS
 '   File Name              : AcceleratorMethod
 '   Author                 : RLOS CORE TEAM
 '   Date written           : 11/01/2023
 '   Description            : LOSActivityBase Class for RLOS Team CODE
 '   ----------------------------------------------------------------------------
 '   CHANGE HISTORY
 '   ----------------------------------------------------------------------------
 '   Date:        Bug ID:   Change By :     Change Description
 '   ----------------------------------------------------------------------------
 */
package com.newgen.iforms.acceleratorCode;

import com.newgen.iforms.accInterface.AcceleratorMethod;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.staffGold.StaffGoldPortalCustomCode;
import com.newgen.iforms.staffHL.EligibilityAsPerLTVHL;
import com.newgen.iforms.staffHL.EligibilityAsPerNth;
import com.newgen.iforms.staffHL.EligibilityAsPerRequestedLoanAmt;
import com.newgen.iforms.staffHL.EligibilityContext;
import com.newgen.iforms.staffHL.EligibilityHandler;
import com.newgen.iforms.staffHL.SchemeEligibilityCheck;
import com.newgen.iforms.staffHL.StaffHLCommanCustomeCode;
import com.newgen.iforms.staffHL.StaffHLPortalCustomCode;
import com.newgen.iforms.staffKavach.StaffKavachPortalCustomCode;
import com.newgen.iforms.staffVL.StaffVLPortalCustomCode;
import com.newgen.iforms.EControl;
import com.newgen.iforms.FormDef;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.APIIntegrationPortal;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.portalAcceleratorCode.PortalCustomCode;
import com.newgen.iforms.portalAcceleratorCode.BRMSRules;
import com.newgen.mvcbeans.model.WorkdeskModel;
import java.io.File;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.newgen.commonlogger.NGUtil;
import com.newgen.dlp.commonobjects.bso.CreateApplicationNumber;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryHRMSData;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.dlp.integration.cbs.FundTransfer;
import com.newgen.dlp.integration.cbs.HRMS;
import com.newgen.dlp.integration.common.EligibilityCalculationInterface;
import com.newgen.dlp.integration.nesl.EsignCommonMethods;
import com.newgen.dlp.integration.nesl.EsignIntegrationChannel;
import com.newgen.dlp.integration.staff.common.EligibilityCalculation;
import com.newgen.dlp.integration.staff.common.uploadDocToOD;
import com.newgen.dlp.integration.staff.constants.AccelatorStaffConstant;

import com.newgen.iforms.budget.BudgetBkoffCustomCode;
import com.newgen.iforms.budget.BudgetPortalCustomCode;
import com.newgen.iforms.commonXMLAPI.DocumentDownload;
import com.newgen.iforms.commons.CommonFunctionality;

import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.homeloan.HLPortalCustomCode;
import com.newgen.iforms.hrms.HRMSPortalCustomCode;
import com.newgen.iforms.lad.LADPortalCustomCode;
import com.newgen.iforms.papl.PAPLPortalCustomCode;
import com.newgen.iforms.pension.PensionLoanPortalCustomCode;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.vl.VLBkoffcCustomCode;
import com.newgen.iforms.vl.VLPortalCustomCode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AcceleratorBaseCode implements AcceleratorMethod {

	PortalCommonMethods pcm = new PortalCommonMethods();
	CustomerAccountSummary cas = new CustomerAccountSummary();
	APIIntegrationPortal por = new APIIntegrationPortal();
	BRMSRules pbc = new BRMSRules();
	CreateApplicationNumber CAN = new CreateApplicationNumber();// Added by Ahmed on 13-12-2023
	EsignIntegrationChannel NESL = new EsignIntegrationChannel();// Added by Ahmed on 14-12-2023
	EsignCommonMethods NESLCM = new EsignCommonMethods();
	BudgetPortalCustomCode cbpcc = new BudgetPortalCustomCode();
	LADPortalCustomCode LADOBJ = new LADPortalCustomCode();
	PAPLPortalCustomCode ppcc = new PAPLPortalCustomCode();
	HRMSPortalCustomCode hpcc = new HRMSPortalCustomCode();
	PensionLoanPortalCustomCode plpc = new PensionLoanPortalCustomCode();
	CommonFunctionality cf = new CommonFunctionality();
	BudgetBkoffCustomCode bbcc = new BudgetBkoffCustomCode();
	public static String SanctionDate = "";
	VLPortalCustomCode vpcc = new VLPortalCustomCode();
	VLBkoffcCustomCode vbcc = new VLBkoffcCustomCode();
	PortalCustomCode pcc = new PortalCustomCode();
	HLPortalCustomCode hlpcc = new HLPortalCustomCode();
	HRMSPortalCustomCode hrmspcc = new HRMSPortalCustomCode();
	StaffHLPortalCustomCode shlpcc = new StaffHLPortalCustomCode();

	@Override

	public void beforeFormLoad(FormDef fd, IFormReference ifr) {
	}

	@Override
	public JSONObject setMaskedValue(IFormReference ifr, String string, String string1) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject executeServerEvent(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr,
				"Inside executeServerevent : control : " + control + " : event : " + event + " : value : " + value);
		String response = "";
		JSONObject obj = new JSONObject();
		StaffVLPortalCustomCode stfPcumtomcode = new StaffVLPortalCustomCode();
		 StaffGoldPortalCustomCode loanSummaryGold =new StaffGoldPortalCustomCode();

		switch (event) {
		case "click":

			switch (control) {
			
			case "OnclickFetchHRMSDataHL":
				StaffHLPortalCustomCode hrmsPortalCustomCodeHL = new StaffHLPortalCustomCode();
				String empIdHL = ifr.getValue("StaffNumber").toString();
				String hrmsPortalResponseHl = "";
				try {
					hrmsPortalResponseHl = hrmsPortalCustomCodeHL.staffDetailsHL(ifr, empIdHL);
					Log.consoleLog(ifr, "Object of hrmsPortalResponse" + hrmsPortalResponseHl);
				} catch (ParseException ex) {
					hrmsPortalResponseHl = "error,Techincal glitch";
				}
 
				obj.put("Response", hrmsPortalResponseHl);
				return obj;
				
			case "GoldCommonMethods":
				try {
					Log.consoleLog(ifr, "inside ALDRICommonMethods : start");
					StaffGoldPortalCustomCode gcm = new StaffGoldPortalCustomCode();
					String result = gcm.goldDisbursementMsg(ifr);
					//JSONObject jobj = new JSONObject();
					obj.put("Response", result);
					return obj;

				} catch (ArithmeticException ae) {
					NGUtil.writeErrorLog("ATS", "ATS", "Exception in ArithmeticException " + ae);
				} catch (ArrayIndexOutOfBoundsException aioe) {
					NGUtil.writeErrorLog("ATS", "ATS", "Exception in ArrayIndexOutOfBoundsException " + aioe);
				} catch (NumberFormatException nfe) {
					NGUtil.writeErrorLog("ATS", "ATS", "Exception in NumberFormatException " + nfe);
				} catch (Exception e) {
					Log.consoleLog(ifr, "Inside ALDRICommonMethods fragment Exception....." + e.getMessage());

				}
				break;
				
			case "isAppForSelfHL":
			    Log.consoleLog(ifr, "Inside isAppForSelfHL Case ...Starting");
			    StaffHLPortalCustomCode isAppForSelfHLObj = new StaffHLPortalCustomCode();
			    response = isAppForSelfHLObj.checkIsAppForSelfHL(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "Object of isAppForSelfHL.." + obj);
			    Log.consoleLog(ifr, "#End of isAppForSelfHL...");
			    return obj;
			    
			case "calculateFeesChargesGold":
			    Log.consoleLog(ifr, "#calculateFeesChargesGold...Starting");
			    StaffGoldPortalCustomCode calculateFeesChargesGoldObj =new StaffGoldPortalCustomCode();
			    try{
			        response = calculateFeesChargesGoldObj.getRequiredLoanAmtGold(ifr);
			    } catch (Exception e) {
			        Log.consoleLog(ifr, "Exception Occured while fetching feesh and charges:." + e);
			    }
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "Object of calculateFeesChargesGoldObj.." + obj);
			    Log.consoleLog(ifr, "#End of calculateFeesChargesGoldObj...");
			    return obj;
			    
			case "ApproveProcessStaffGold":
				try {
					Log.consoleLog(ifr, "Inside executeServerEvent :eSign_calling called:");

					//String tBranch = loanSummaryGold.getTransactionBranch(ifr);
//					String res = loanSummaryGold.populateLoanSummaryGold(ifr, tBranch);
//					if (res != RLOS_Constants.SUCCESS) {
//						String returnRespWrapper = returnRespWrapper(ifr, control, event, value, res, "false");
//						obj.put("Response", returnRespWrapper);
//					}
					String aprovalProcessRes = loanSummaryGold.approvalProcess(ifr);
					// String returnRespWrapper = returnRespWrapper(ifr, control, event, value,
					// aprovalProcessRes, "false");
					obj.put("Response", aprovalProcessRes);
					return obj;

				} catch (ArithmeticException ae) {
					NGUtil.writeErrorLog("ATS", "ATS", "Exception in ArithmeticException " + ae);
					Log.errorLog(ifr, "ApproveProcess/Exception in ArithmeticException " + ae);
				} catch (ArrayIndexOutOfBoundsException aioe) {
					NGUtil.writeErrorLog("ATS", "ATS", "Exception in ArrayIndexOutOfBoundsException " + aioe);
					Log.errorLog(ifr, "ApproveProcess/Exception in ArrayIndexOutOfBoundsException " + aioe);
				} catch (NumberFormatException nfe) {
					NGUtil.writeErrorLog("ATS", "ATS", "Exception in NumberFormatException " + nfe);
					Log.errorLog(ifr, "ApproveProcess/Exception in NumberFormatException " + nfe);
				}
				break;
				
				
			case "Combinationdetails":
				try {
					String response360 = loanSummaryGold.autoPopulateInitialdata(ifr,value);// Calling Advanced360
					obj.put("Response", response360);
					return obj;

				} catch (Exception e) {
					ifr.setStyle("validateBtnGOLD", "disable", "false");
					Log.consoleLog(ifr, "Combinationdetails Exception outer=====>" + e);

				}
				break;

			case "GetGoldBranchLevelNeslFlage":
				try {
					Log.consoleLog(ifr, "Inside executeServerEvent :GetGoldBranchLevelNeslFlage called:");
					String neslFlageResponse = loanSummaryGold.getGoldNeslFlageByBranch(ifr);
					// String returnRespWrapper = returnRespWrapper(ifr, control, event, value,
					// neslFlageResponse, "false");
					obj.put("Response", neslFlageResponse);
					return obj;

				} catch (ArithmeticException ae) {
					NGUtil.writeErrorLog("ATS", "ATS", "Exception in ArithmeticException " + ae);
					Log.errorLog(ifr, "GetGoldBranchLevelNeslFlage/Exception in ArithmeticException " + ae);
				} catch (ArrayIndexOutOfBoundsException aioe) {
					NGUtil.writeErrorLog("ATS", "ATS", "Exception in ArrayIndexOutOfBoundsException " + aioe);
					Log.errorLog(ifr,
							"GetGoldBranchLevelNeslFlage/Exception in ArrayIndexOutOfBoundsException " + aioe);
				} catch (NumberFormatException nfe) {
					NGUtil.writeErrorLog("ATS", "ATS", "Exception in NumberFormatException " + nfe);
					Log.errorLog(ifr, "GetGoldBranchLevelNeslFlage/Exception in NumberFormatException " + nfe);
				}
				break;
				
			case "CheckerDisbursementforGOLD":
                Log.consoleLog(ifr, "inside CheckerDisbursementforGOLD switch case ");
                StaffGoldPortalCustomCode checkerDisbursementforGOLD =new StaffGoldPortalCustomCode();
				String loanDisbursementstatus;
				try {
					loanDisbursementstatus = checkerDisbursementforGOLD.createLoanAndDisburse(ifr);
					Log.consoleLog(ifr, "loanDisbursementstatus: " + loanDisbursementstatus);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			    break;
			    
			case "getNesLCharges":
			    Log.consoleLog(ifr, "Inside getNesLCherges Case ...Starting");
			     HRMSPortalCustomCode getNesLCherge = new HRMSPortalCustomCode();
			    response = getNesLCherge.getNesLCherges(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "Object of getNesLCherges.." + obj);
			    Log.consoleLog(ifr, "#End of getNesLCherges...");
			    return obj;
			    
			case "checkDocToByPassed":
			    Log.consoleLog(ifr, "Inside checkDocToByPassed Case ...Starting");
			     HRMSPortalCustomCode checkDocToByPassed = new HRMSPortalCustomCode();
			    response = checkDocToByPassed.checkDocToByPas(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "Object of checkDocToByPassed.." + obj);
			    Log.consoleLog(ifr, "#End of checkDocToByPassed...");
			    return obj;
			    
			case "getFundTransferStatus":
			    Log.consoleLog(ifr, "Inside getFundTransferStatus Case ...Starting");
			     HRMSPortalCustomCode getFundTransferStatus = new HRMSPortalCustomCode();
			    response = getFundTransferStatus.getFundTransferStatuss(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "Object of getFundTransferStatus.." + obj);
			    Log.consoleLog(ifr, "#End of getFundTransferStatus...");
			    return obj;
			    
			    
			    

			case "isAppForSelfVLOD":
			    Log.consoleLog(ifr, "Inside isAppForSelfVLOD Case ...Starting");
			    StaffHLPortalCustomCode isAppForSelfVLODObj = new StaffHLPortalCustomCode();
			    response = isAppForSelfVLODObj.checkIsAppForSelfVLOD(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "Object of isAppForSelfVLOD.." + obj);
			    Log.consoleLog(ifr, "#End of isAppForSelfVLOD...");
			    return obj;
			
			case "DocumentView":
				try {
					Log.consoleLog(ifr, "Inter into DocumentView");
					String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
					String docindex_qry = "Select max(d.DOCUMENTINDEX) from PDBDOCUMENTCONTENT d inner join pdbfolder e on d.parentfolderindex = e.folderindex inner join PDBDOCUMENT f on d.documentindex = f.DOCUMENTINDEX WHERE f.name='"
							+ value + "' and e.NAME ='" + PID + "'";
					Log.consoleLog(ifr, "docindex_qry===>" + docindex_qry);
					List<List<String>> respon = ifr.getDataFromDB(docindex_qry);
					Log.consoleLog(ifr, "respons docindex_qry====>" + docindex_qry);
					if (!respon.isEmpty()) {
						String res = respon.get(0).get(0);
						Log.consoleLog(ifr, "result====>" + res);
						obj.put("Response", res);
						Log.consoleLog(ifr, "Object of DocumentView.." + obj);
						return obj;
					}
				} catch (Exception e) {
					Log.consoleLog(ifr, "Exception " + e);
				}
				
			case "onLoadCalculateCollateralSummarySHL":
			    Log.consoleLog(ifr, "Inside onLoadCalculateCollateralSummarySHL::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode CalculateCollateralSummaryObj =new StaffHLPortalCustomCode();
			    response = CalculateCollateralSummaryObj.calculateCollateralSummarySHL(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
				
			case "saveDataInPartyDetailGridFetchStaffHL":
			    Log.consoleLog(ifr, "Inside saveDataInPartyDetailGridFetchStaffHL::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode shlBkoffcCustomCode =new StaffHLPortalCustomCode();
			    response = shlBkoffcCustomCode.msaveDataInPartyDetailGridFetch(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			case "onLoadGetBranchCodeandNameSHL":
			    Log.consoleLog(ifr, "#onLoadGetBranchCodeandNameSHL...Starting");
			    response = shlpcc.autopopulate_headerDetailsSHL(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "Object of onLoadGetBranchCodeandNameSHL.." + obj);
			    Log.consoleLog(ifr, "#End of onLoadGetBranchCodeandNameSHL...");
			    return obj;
			    
			case "setUserID":
			    Log.consoleLog(ifr, "#setUserID...Starting");
			    StaffHLPortalCustomCode setUsID =new StaffHLPortalCustomCode();
			    setUsID.setUserID(ifr,value);
			    break;
			    
			case "availOfferDetails":
			    Log.consoleLog(ifr, "#availOfferDetails...Starting");
			    StaffGoldPortalCustomCode availOfferDetails =new StaffGoldPortalCustomCode();
			    availOfferDetails.availOfferDetails(ifr);
			    break;
			    
			case "goldJewelleryDetails":
			    Log.consoleLog(ifr, "#setUserID...Starting");
			    StaffGoldPortalCustomCode goldJewelleryDetails =new StaffGoldPortalCustomCode();
			    goldJewelleryDetails.goldJewelleryDetails(ifr);
			    break;
			    
			case "onLoadLoanSelected":
			    Log.consoleLog(ifr, "#onLoadLoanSelected...Starting");
			    StaffKavachPortalCustomCode onLoadLoanSelected =new StaffKavachPortalCustomCode();
			    onLoadLoanSelected.onLoadLoanSelectedKavach(ifr);
			    break;
			    
			case "setUserIDVL":
			    Log.consoleLog(ifr, "#setUserIDVL...Starting");
			    StaffVLPortalCustomCode setUsIDVL =new StaffVLPortalCustomCode();
			    setUsIDVL.setUserIDVL(ifr,value);
			    break;
			    
			case "setUserIDGold":
			    Log.consoleLog(ifr, "#setUserIDVL...Starting");
			    StaffGoldPortalCustomCode setUsIDGold =new StaffGoldPortalCustomCode();
			    setUsIDGold.setUserIDGold(ifr,value);
			    break;
			    
			case "setNomineeAddAsApplicantGold":
			    Log.consoleLog(ifr, "#setNomineeAddAsApplicantGold...Starting");
			    StaffHLPortalCustomCode setNomineeAddAsApplicantObj =new StaffHLPortalCustomCode();
			    response = setNomineeAddAsApplicantObj.setNomineeAddAsApplicant(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "Object of setNomineeAddAsApplicantGold.." + obj);
			    Log.consoleLog(ifr, "#End of setNomineeAddAsApplicantGold...");
			    return obj;
			    
			case "setUserIDSL":
			    Log.consoleLog(ifr, "#setUserID...Starting");
			    HRMSPortalCustomCode setUsIDHRMs =new HRMSPortalCustomCode();
			    setUsIDHRMs.setUserIDSL(ifr,value);
			    break;
			    
			case "checkLoanAccNum":
			    Log.consoleLog(ifr, "#checkLoanAccNum...Starting");
			    StaffVLPortalCustomCode checkLoanAccNum =new StaffVLPortalCustomCode();
			    response = checkLoanAccNum.checkLoanAccNumVL(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "Object of checkLoanAccNum.." + obj);
			    Log.consoleLog(ifr, "#End of checkLoanAccNum...");
			    return obj;
			    
			case "checkLoanAccNumHL":
			    Log.consoleLog(ifr, "#checkLoanAccNumHL...Starting");
			    StaffHLPortalCustomCode checkLoanAccNumHL =new StaffHLPortalCustomCode();
			    response = checkLoanAccNumHL.checkLoanAccNumHomeLoan(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "Object of checkLoanAccNumHL.." + obj);
			    Log.consoleLog(ifr, "#End of checkLoanAccNumHL...");
			    return obj;
			    
			case "checkLoanAccNumKavach":
			    Log.consoleLog(ifr, "#checkLoanAccNumKavach...Starting");
			    StaffHLPortalCustomCode checkLoanAccNumKavachObj =new StaffHLPortalCustomCode();
			    response = checkLoanAccNumKavachObj.checkLoanAccNumKavach(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "Object of checkLoanAccNumKavach.." + obj);
			    Log.consoleLog(ifr, "#End of checkLoanAccNumKavach...");
			    return obj;
			  
			case "onClickKYCValidateBtnSHL":
			    Log.consoleLog(ifr, "Inside onClickKYCValidateBtnSHL::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode onClickKYCValidateBtnSHLObj =new StaffHLPortalCustomCode();
			    response = onClickKYCValidateBtnSHLObj.mImplClickValidateBtnSHL(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			case "OnLoadPopulateCibilLiabilitiesSHL":
			    Log.consoleLog(ifr, "Inside OnLoadPopulateCibilLiabilitiesSHL::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode PopulateCibilLiabilitiesSHLObj =new StaffHLPortalCustomCode();
			    response = PopulateCibilLiabilitiesSHLObj.PopulateCibilLiabilitiesSHL(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			case "clickBTNPanelValuationInitiateSHL":
			    Log.consoleLog(ifr, "Inside clickBTNPanelValuationInitiateSHL::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode clickBTNPanelValuation =new StaffHLPortalCustomCode();
			    response = clickBTNPanelValuation.panelValuationInitiateSHL(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			case "fetchCICResponse":
			    Log.consoleLog(ifr, "Inside fetchCICResponse::AcceleratorBaseCode:::");
			    StaffHLCommanCustomeCode staffHLfetchCICResponse =new StaffHLCommanCustomeCode();
			    response = staffHLfetchCICResponse.fetchCICResponse(ifr,value);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			    
			case "uploadMandatoryDocs":
			    Log.consoleLog(ifr, "Inside uploadMandatoryDocs::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode uploadMandatoryD =new StaffHLPortalCustomCode();
			    response = uploadMandatoryD.uploadMandatoryDocs(ifr,value);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			    
			case "uploadMandatoryDocsKavach":
			    Log.consoleLog(ifr, "Inside uploadMandatoryDocsKavach::AcceleratorBaseCode:::");
			    StaffKavachPortalCustomCode uploadMandatoryKavach =new StaffKavachPortalCustomCode();
			    response = uploadMandatoryKavach.uploadMandatoryDocs(ifr,value);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			    
			    
			    
			case "uploadMandatoryDocsVL":
			    Log.consoleLog(ifr, "Inside uploadMandatoryDocsVL::AcceleratorBaseCode:::");
			    StaffVLPortalCustomCode uploadMandatoryVL =new StaffVLPortalCustomCode();
			    response = uploadMandatoryVL.uploadMandatoryDocsVL(ifr,value);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			    
			case "onLoadExternalDeductions":
				Log.consoleLog(ifr, "#OnLoadExternalDeductions...Starting");
				StaffHLPortalCustomCode onLoadExternalDed = new StaffHLPortalCustomCode();
				onLoadExternalDed.onLoadExternalDeductions(ifr);
				break;
				
			case "onLoadShowLoanDetails":
				Log.consoleLog(ifr, "#onLoadShowLoanDetails...Starting");
				StaffHLPortalCustomCode onLoadShowLoanDetails = new StaffHLPortalCustomCode();
				response = onLoadShowLoanDetails.onLoadShowLoanDetailsHL(ifr);
			    obj.put("Response", response);
				Log.consoleLog(ifr, "obj :: " + obj);
				return obj;
			    
			case "setCbsProductCode":
			    Log.consoleLog(ifr, "Inside setCbsProductCode::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode setCbsProductCode =new StaffHLPortalCustomCode();
			    response = setCbsProductCode.setCbsProductCodeHL(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			case "autoPopulateCollateralDropdownSHL":
			    Log.consoleLog(ifr, "inside autoPopulateCollateralDropdownSHL case");
			    StaffHLPortalCustomCode  populateCollateralObj =new StaffHLPortalCustomCode();
			    response = populateCollateralObj.autoPopulateCollateralDropdownSHL(ifr, control, event, value);
			    obj.put("Response", response);
			    return obj;

			    
			case "checkDebitCharges":
			    Log.consoleLog(ifr, "Inside checkDebitCharges::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode staffHLcheckDebitCharges =new StaffHLPortalCustomCode();
			    response = staffHLcheckDebitCharges.checkDebitCharges(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			    
			case "onLoadFetchBureauSHL":
			    Log.consoleLog(ifr, "Inside OnLoadFetchBureauSHL::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode onLoadFetchBureau =new StaffHLPortalCustomCode();
			    response = onLoadFetchBureau.OnLoadConsentSectionAddCoBorrowerConsent(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			
			case "onChangeNoBorrower":
			    Log.consoleLog(ifr, "Inside onChangeNoBorrower::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode onChangeNoBorrower=new StaffHLPortalCustomCode();
			    response = onChangeNoBorrower.onChangeNoBorrowerDetailsYes(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			  //  break;
			    
			case "OnClickDebitChargesSHL":
			    Log.consoleLog(ifr, "Inside OnClickDebitChargesSHL::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode shlFundTransfer =new StaffHLPortalCustomCode();
			    response =shlFundTransfer.feesAndCharges(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			    
			case "onClickCalculateNetworth":
			    Log.consoleLog(ifr, "Inside onClickCalculateNetworth::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode CalculateNetworth =new StaffHLPortalCustomCode();
			    response = CalculateNetworth.onClickAddModifyDeleteNetworth(ifr);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
				
			case "saveDataCoApplicant":
			    Log.consoleLog(ifr, "Data saving for PartyDetails Grid CoObligant HL::");
			    StaffHLPortalCustomCode shlpcc=new StaffHLPortalCustomCode();
			    String result21 = shlpcc.validateCoBorrower(ifr, control, value, "CB");
			    Log.consoleLog(ifr, "result :: " + result21);
			    obj.put("Response", result21);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			case "OnClickFetchFeesChargesSHL":
			    Log.consoleLog(ifr, "Inside OnClickFetchFeesChargesSHL::AcceleratorBaseCode:::");
			    StaffHLPortalCustomCode fetchFeesObj =new StaffHLPortalCustomCode();
			    response = fetchFeesObj.mAccClickFetchFeeCharges(ifr, "", "", "");
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			    
			case "populateMarginByProjectCost":
			    Log.consoleLog(ifr, "inside populateMarginByProjectCost case");
			    StaffHLPortalCustomCode populateMarginByProje =new StaffHLPortalCustomCode();
			    response = populateMarginByProje.calculateMarginByProjectCost(ifr, control, event, value);
			    obj.put("Response", response);
			    Log.consoleLog(ifr, "obj :: " + obj);
			    return obj;
			    
			case "EXServiceManCheck":
				Log.consoleLog(ifr, "#EXServiceManCheck...Starting");
				StaffHLPortalCustomCode stEXServiceManCheck =new StaffHLPortalCustomCode();
				response = stEXServiceManCheck.EXServiceManCheck(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of EXServiceManCheck.." + obj);
				Log.consoleLog(ifr, "#End of EXServiceManCheck...");
				return obj;
			
			case "NetSalSet":
				Log.consoleLog(ifr, "#NetSalSet...Starting");
				StaffHLPortalCustomCode NetSalarySet =new StaffHLPortalCustomCode();
				response = NetSalarySet.NetSalSet(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of NetSalSet.." + obj);
				Log.consoleLog(ifr, "#End of NetSalSet...");
				return obj;
				
				
				
			case "checkExStaff":
				Log.consoleLog(ifr, "#checkExStaff...Starting");
				StaffHLPortalCustomCode checkExStaff =new StaffHLPortalCustomCode();
				response = checkExStaff.checkExserStaff(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of EXServiceManCheck.." + obj);
				Log.consoleLog(ifr, "#End of EXServiceManCheck...");
				return obj;
				
			case "tenureCheck":
				Log.consoleLog(ifr, "#tenureCheck...Starting");
				StaffHLPortalCustomCode stEXServicetenureCheck =new StaffHLPortalCustomCode();
				response = stEXServicetenureCheck.tenureCheck(ifr,value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of tenureCheck.." + obj);
				Log.consoleLog(ifr, "#End of tenureCheck...");
				return obj;
			case "tenureCheckR":
				Log.consoleLog(ifr, "#tenureCheck...Starting");
				StaffHLPortalCustomCode stEXServicetenureCheckR =new StaffHLPortalCustomCode();
				response = stEXServicetenureCheckR.tenureCheckR(ifr,value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of tenureCheckR.." + obj);
				Log.consoleLog(ifr, "#End of tenureCheckR...");
				return obj;
			    
			 case "EligibilityCalcHL":
                 // chain of responsibility
                 EligibilityContext context=new EligibilityContext();
                 EligibilityHandler elgHandler=new SchemeEligibilityCheck();
                
                        elgHandler. setNext(new EligibilityAsPerRequestedLoanAmt(), ifr).
                         setNext(new EligibilityAsPerNth(), ifr);
                 obj.put("Response", elgHandler.handle(context, ifr));
                 return obj;
			    
			case "EligibilityAsPerLTVHL":
				EligibilityContext cntxt = new EligibilityContext();
				try {
					EligibilityHandler eh = new EligibilityAsPerLTVHL();
					obj.put("Response", eh.handle(cntxt, ifr));
				} catch (Exception e) {
					obj.put("Response", "Error, Techincal glitch");
				}
				return obj;

				
			case "DocumentViewUpload":
				try {
					Log.consoleLog(ifr, "Inter into DocumentViewUpload");
					String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
					String docindex_qry = "Select MAX(d.DOCUMENTINDEX) from PDBDOCUMENTCONTENT d inner join pdbfolder e on d.parentfolderindex = e.folderindex inner join PDBDOCUMENT f on d.documentindex = f.DOCUMENTINDEX WHERE f.name='"
							+ value + "' AND e.name='" + PID + "'";
					Log.consoleLog(ifr, "docindex_qry===>" + docindex_qry);
					List<List<String>> respon = ifr.getDataFromDB(docindex_qry);
					Log.consoleLog(ifr, "respons docindex_qry====>" + docindex_qry);
					if (!respon.isEmpty()) {
						String res = respon.get(0).get(0);
						Log.consoleLog(ifr, "result====>" + res);
						obj.put("Response", res);
						Log.consoleLog(ifr, "Object of DocumentView.." + obj);
						return obj;
					}
				} catch (Exception e) {
					Log.consoleLog(ifr, "Exception " + e);
				}
				
//			case "isExStaff":
//                String cstid1="select  flgcustype from los_trn_customersummary where winame='"+ifr.getObjGeneralData().getM_strProcessInstanceId()+"'";
//                Log.consoleLog(ifr, "cstid1 query" + cstid1);
//                List<List<String>> resCstid1=ifr.getDataFromDB(cstid1);
//                if(resCstid1.isEmpty()){
//                     obj.put("Response", "Error,Technical glitch");
//                     return obj;
//                }
//                if(resCstid1.get(0).get(0).equals("R1")){
//                    obj.put("Response", "Not a exstaff");
//                }else{
//                    obj.put("Response", "exstaff");
//                }
//                return obj;
                
                
			case "isEXStaff":
				Log.consoleLog(ifr, "#isExStaff...Starting");
				StaffHLPortalCustomCode shlisExStaff = new StaffHLPortalCustomCode();
				response = shlisExStaff.checkForExStaff(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of isExStaff.." + obj);
				Log.consoleLog(ifr, "#End of isExStaff...");
				return obj;
				
            case "ExStaffDetails":
                StaffHLPortalCustomCode shpc=new StaffHLPortalCustomCode();
                String cstid="select  customerid from los_trn_customersummary where winame='"+ifr.getObjGeneralData().getM_strProcessInstanceId()+"'";
                List<List<String>> resCstid=ifr.getDataFromDB(cstid);
                if(resCstid.isEmpty()){
                     obj.put("Response", "Error,Technical glitch");
                     return obj;
                }
                String customerIdRes=resCstid.get(0).get(0);
            try {
                String shpcRes= shpc.exStaffDetailsHL(ifr, customerIdRes);
                obj.put("Response", shpcRes);
            } catch (ParseException ex) {
                //Logger.getLogger(AcceleratorBaseCode.class.getName()).log(Level.SEVERE, null, ex);
            }
            return obj;
			case "countDocument":
				try {
					Log.consoleLog(ifr, "Inter into countDocument");
					String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
					String docindex_qry = "SELECT COUNT(*)\r\n" + "FROM (\r\n" + "    SELECT NAME\r\n"
							+ "    FROM PDBDOCUMENT\r\n" + "    WHERE DOCUMENTINDEX IN (\r\n"
							+ "        SELECT DOCUMENTINDEX\r\n" + "        FROM PDBDOCUMENTCONTENT\r\n"
							+ "        WHERE PARENTFOLDERINDEX IN (\r\n" + "            SELECT FOLDERINDEX\r\n"
							+ "            FROM PDBFOLDER\r\n" + "            WHERE NAME = '\" + pid + \"'\r\n"
							+ "        )\r\n" + "    )\r\n" + "    GROUP BY NAME\r\n" + "    HAVING COUNT(*) >= 2\r\n"
							+ ") AS SubQuery";
					Log.consoleLog(ifr, "docindex_qry===>" + docindex_qry);
					List<List<String>> respon = ifr.getDataFromDB(docindex_qry);
					Log.consoleLog(ifr, "respons docindex_qry====>" + docindex_qry);
					if (!respon.isEmpty()) {
						String res = respon.get(0).get(0);
						Log.consoleLog(ifr, "result====>" + res);
						obj.put("Response", res);
						Log.consoleLog(ifr, "Object of DocumentView.." + obj);
						return obj;
					}
				} catch (Exception e) {
					Log.consoleLog(ifr, "Exception " + e);
				}
			case "NeSLValidate":
				Log.consoleLog(ifr, "#OnLoadGetLoanDetails...Starting");
				response = hpcc.mNESLClick(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of OnClickGenNESLButton.." + obj);
				Log.consoleLog(ifr, "#End of OnClickGenNESLButton...");
				return obj;
				
			case "autoPopulateCoBorrowerDetailsDataHL":
			    Log.consoleLog(ifr, "inside autoPopulateCoBorrowerDetailsDataHL case");
			    StaffHLPortalCustomCode  autoPopCoBorDtlsDataHL =new StaffHLPortalCustomCode();
			    response = autoPopCoBorDtlsDataHL.autoPopulateCoBorowerDetailsDataStaffHL(ifr, control, event, value);
			    obj.put("Response", response);
			    return obj;
			case "autoPopulateSectionCollateral":
			    Log.consoleLog(ifr, "inside autoPopulateSectionCollateral case");
			    StaffHLPortalCustomCode  autoPoplSecCollateral =new StaffHLPortalCustomCode();
			    response = autoPoplSecCollateral.autoPopulateSectionCollateral(ifr, control, event, value);
			    obj.put("Response", response);
			    return obj;
				
			case "CalculateEligibleLoanAmountHL":
				StaffHLPortalCustomCode hlStaffHlPortalCode = new StaffHLPortalCustomCode();
	             String hrmsHLResult = "";
	         try {
	             hrmsHLResult = hlStaffHlPortalCode.HlStaffEligibility(ifr,value);
	             } catch (Exception ex) {
	             hrmsHLResult = "error, Techincal error";
	             }
	           obj.put("Response", hrmsHLResult);
	           return obj;
	           
			case "CalculateEligibleLoanAmountPHL":
				StaffHLPortalCustomCode hlStaffHlPortalCodeP = new StaffHLPortalCustomCode();
	             String hrmsHLResultP = "";
	         try {
	             hrmsHLResultP = hlStaffHlPortalCodeP.HlStaffEligibilityOnselectPurpose(ifr,value);
	             } catch (Exception ex) {
	             hrmsHLResultP = "error, Techincal error";
	             }
	           obj.put("Response", hrmsHLResultP);
	           return obj;
	           
			case "NeSLValidateVL":
				Log.consoleLog(ifr, "#NeSLValidateVL...Starting");
				response = stfPcumtomcode.mNESLClick(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of NeSLValidateVL.." + obj);
				Log.consoleLog(ifr, "#End of NeSLValidateVL...");
				return obj;

			case "ScheduleCode":
				Log.consoleLog(ifr, "#ScheduleCode...Starting");
				StaffVLPortalCustomCode.calCulateROI(ifr, value);
				break;
				
			case "setStatusSuccess":
				Log.consoleLog(ifr, "#setStatusSuccess...Starting");
				HRMSPortalCustomCode hrmssetStatusSuccess =new HRMSPortalCustomCode();
				hrmssetStatusSuccess.setStatusSuccess(ifr,value);
				break;
				
			case "checkStatusCheck":
				Log.consoleLog(ifr, "#checkStatusCheck...Starting");
				HRMSPortalCustomCode hrmssetcheckStatusCheck =new HRMSPortalCustomCode();
				response = hrmssetcheckStatusCheck.checkStatusCheck(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of checkStatusCheck.." + obj);
				Log.consoleLog(ifr, "#End of checkStatusCheck...");
				return obj;
				
			case "saveESignStatus":	
				Log.consoleLog(ifr, "#saveESignStatus...Starting");
				StaffVLPortalCustomCode.saveESignStatus(ifr, value);
				break;

//			case "UploadInwardDocument":
//				Log.consoleLog(ifr, "#UploadInwardDocument...Starting");
//				StaffVLPortalCustomCode.uploadInwardDocument(ifr);
//				break;

			case "onLoadCRNumber":
				Log.consoleLog(ifr, "#onLoadCRNumber...Starting");
				response = stfPcumtomcode.onLoadCRNumber(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of onLoadCRNumber.." + obj);
				Log.consoleLog(ifr, "#End of onLoadCRNumber...");
				return obj;

			case "DocumentsCheck":
				Log.consoleLog(ifr, "#DocumentsCheck...Starting");
				StaffVLPortalCustomCode docCheck = new StaffVLPortalCustomCode();
				response = docCheck.documentsCheck(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of DocumentsCheck.." + obj);
				Log.consoleLog(ifr, "#End of DocumentsCheck...");
				return obj;
				
			case "DocumentsCheckHL":
				Log.consoleLog(ifr, "#DocumentsCheck...Starting");
				StaffHLPortalCustomCode docCheckHL = new StaffHLPortalCustomCode();
				response = docCheckHL.documentsCheck(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of DocumentsCheckHL.." + obj);
				Log.consoleLog(ifr, "#End of DocumentsCheckHL...");
				return obj;
				
			case "DocumentsCheckKavach":
				Log.consoleLog(ifr, "#DocumentsCheck...Starting");
				StaffKavachPortalCustomCode staffKavachPortalCustomCode = new StaffKavachPortalCustomCode();
				response = staffKavachPortalCustomCode.documentsCheck(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of DocumentsCheck.." + obj);
				Log.consoleLog(ifr, "#End of DocumentsCheck...");
				return obj;

			case "DdeCharges":
				Log.consoleLog(ifr, "#ScheduleCode...Starting");
				StaffVLPortalCustomCode.ddeCharges(ifr, value);
				break;

			case "checkDocumentOnLoad":
				Log.consoleLog(ifr, "#checkDocumentOnLoad...Starting");
				StaffVLPortalCustomCode staffVLcheckDocumentOnLoad = new StaffVLPortalCustomCode();
				response = staffVLcheckDocumentOnLoad.checkDocumentOnLoad(ifr,value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of checkDocumentOnLoad.." + obj);
				Log.consoleLog(ifr, "#End of checkDocumentOnLoad...");
				return obj;
				
			case "checkDocumentOnLoadHL":
				Log.consoleLog(ifr, "#checkDocumentOnLoadHL...Starting");
				StaffHLPortalCustomCode staffcheckDocumentOnLoadHL = new StaffHLPortalCustomCode();
				response = staffcheckDocumentOnLoadHL.checkDocumentOnLoadHL(ifr,value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of checkDocumentOnLoad.." + obj);
				Log.consoleLog(ifr, "#End of checkDocumentOnLoad...");
				return obj;
				
			case "checkDocumentOnLoadKavach":
				Log.consoleLog(ifr, "#checkDocumentOnLoadKavach...Starting");
				StaffKavachPortalCustomCode checkDocumentOnLoadKavach = new StaffKavachPortalCustomCode();
				response = checkDocumentOnLoadKavach.checkDocumentOnLoadKavach(ifr,value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of checkDocumentOnLoadKavach.." + obj);
				Log.consoleLog(ifr, "#End of checkDocumentOnLoadKavach...");
				return obj;

			case "checkNeslOnLoad":
				Log.consoleLog(ifr, "#checkNeslOnLoad...Starting");
				StaffVLPortalCustomCode staffVLcheckNESLOnLoad = new StaffVLPortalCustomCode();
				response = staffVLcheckNESLOnLoad.checkNeslOnLoad(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of checkNeslOnLoad.." + obj);
				Log.consoleLog(ifr, "#End of checkNeslOnLoad...");
				return obj;

			case "checkDocumentOnLoadHRMS":
				Log.consoleLog(ifr, "#checkDocumentOnLoadHRMS...Starting");
				HRMSPortalCustomCode staffcheckDocumentOnLoadHRMS = new HRMSPortalCustomCode();
				response = staffcheckDocumentOnLoadHRMS.checkDocumentOnLoad(ifr,value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of checkDocumentOnLoadHRMS.." + obj);
				Log.consoleLog(ifr, "#End of checkDocumentOnLoadHRMS...");
				return obj;

			case "checkNeslOnLoadHRMS":
				Log.consoleLog(ifr, "#checkNeslOnLoadHRMS...Starting");
				HRMSPortalCustomCode staffcheckNESLOnLoadHRMS = new HRMSPortalCustomCode();
				response = staffcheckNESLOnLoadHRMS.checkNeslOnLoad(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of checkNeslOnLoad.." + obj);
				Log.consoleLog(ifr, "#End of checkNeslOnLoad...");
				return obj;

			case "UpdateApprovalMatrix":
				Log.consoleLog(ifr, "#UpdateApprovalMatrix...Starting");
				String responseObj = stfPcumtomcode.UpdateApprovalMatrix(ifr, value);
				obj.put("Response", responseObj);
				return obj;

				
			case "UpdateApprovalMatrixSL":
				Log.consoleLog(ifr, "#UpdateApprovalMatrixSL...Starting");
				HRMSPortalCustomCode UpdateApprovalMatrixSL = new HRMSPortalCustomCode();
				responseObj = UpdateApprovalMatrixSL.UpdateApprovalMatrix(ifr, value);
				obj.put("Response", responseObj);
				return obj;
				
			case "UpdateApprovalMatrixSHL":
				Log.consoleLog(ifr, "#UpdateApprovalMatrixSHL...Starting");
				StaffHLPortalCustomCode updateApprovalMatrixSHL = new StaffHLPortalCustomCode();
				responseObj = updateApprovalMatrixSHL.UpdateApprovalMatrix(ifr, value);
				obj.put("Response", responseObj);
				return obj;
//			case "ROICalculation":
//				Log.consoleLog(ifr, "#Inside ROICalculation...");
//				//StaffVLPortalCustomCode staffVLPortalCustomCode =new StaffVLPortalCustomCode();
//				StaffVLPortalCustomCode.calCulateROI(ifr,value);
//				Log.consoleLog(ifr, "#End of ROICalculation...");
//				break;

			case "loanPurpose":
				String eligibilityRes = EligibilityCalculation.purposeLoan(ifr);
				obj.put("Response", eligibilityRes);
				return obj;
			case "dateValidation":
				obj.put("Response", "success");
				String mm = ifr.getValue("Manufacturing_Month").toString();
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				LocalDate inputDate = LocalDate.parse(mm, formatter);
				LocalDate today = LocalDate.now();
				LocalDate threeYearsAgo = today.minusYears(3);

				boolean d = ((inputDate.isEqual(threeYearsAgo) || inputDate.isAfter(threeYearsAgo))
						&& (inputDate.isBefore(today) || inputDate.isEqual(today)));
				if (!d) {
					obj.put("Response", "error, Manufacturing date cannot be prior to 3 years");
				}
				String expectedDateStr = ifr.getValue("Exp_Delivery_Date").toString().trim();

				Log.consoleLog(ifr, "expectedDateStr == " + expectedDateStr);

				DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
				LocalDate expectedDate = LocalDate.parse(expectedDateStr, formatter1);

				LocalDate currentDate = LocalDate.now();
				LocalDate maxAllowedDate = currentDate.plusMonths(6);

				Log.consoleLog(ifr, "Current Date == " + currentDate);
				Log.consoleLog(ifr, "Max Allowed Date == " + maxAllowedDate);
				Log.consoleLog(ifr, "Expected Date == " + expectedDate);
				boolean valid = !expectedDate.isBefore(currentDate) && !expectedDate.isAfter(maxAllowedDate);
				if (!valid) {
					obj.put("Response", "error, Expected delivery date to be less than 6 months");
				}
				return obj;

			case "dateValidationCollateralValuation":
				obj.put("Response", "success");
				String mm3 = ifr.getValue("Original_Purchase_Date").toString();
				DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				LocalDate inputDate3 = LocalDate.parse(mm3, formatter3);
				LocalDate today3 = LocalDate.now();
				LocalDate fiveYearsAgo = today3.minusYears(5);

				boolean d3 = (inputDate3.isEqual(fiveYearsAgo) || inputDate3.isAfter(fiveYearsAgo))
						&& (inputDate3.isBefore(today3) || inputDate3.isEqual(today3));
				if (!d3) {
					obj.put("Response", "error, Date of registration cannot be less than 5 years");
				}
				return obj;
			case "collateralExpenses":
				EligibilityCalculationInterface eci = new EligibilityCalculation();
				JSONObject job = eci.EligibilityCalculationAsLTVNewVehicle(ifr);
				obj.put("Response", job.get("Error").toString().isEmpty() ? "Success" : job.get("Error").toString());
				return obj;

			case "onLoadLoanDeductionGrid":
				StaffVLPortalCustomCode staffVLPortalCustomCode = new StaffVLPortalCustomCode();
				response = staffVLPortalCustomCode.loadGrid(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of DocumentView.." + obj);
				return obj;

			case "collateralValuation":
				EligibilityCalculationInterface eci1 = new EligibilityCalculation();
				JSONObject job1 = eci1.EligibilityCalculationAsLTVUsedVehicle(ifr);
				obj.put("Response", job1.get("Error").toString().isEmpty() ? "Success" : job1.get("Error").toString());
				return obj;
			// json.put("Success","");

			case "staffDetailsLoanAvailbilityCalc":
				StaffVLPortalCustomCode hrmsObj = new StaffVLPortalCustomCode();
				String hrmsResult = "";
				try {
					hrmsResult = hrmsObj.staffDetailsLoanAvailbilityCalc(ifr);
				} catch (ParseException ex) {
					hrmsResult = "error, Techincal error";
				}
				obj.put("Response", hrmsResult);
				return obj;
			case "OnclickFetchHRMSDataVL":
				StaffVLPortalCustomCode hrmsPortalCustomCode = new StaffVLPortalCustomCode();
				String empId = ifr.getValue("StaffNumber").toString();
				String hrmsPortalResponse = "";
				try {
					hrmsPortalResponse = hrmsPortalCustomCode.staffDetailsVL(ifr, empId);
					Log.consoleLog(ifr, "Object of hrmsPortalResponse" + hrmsPortalResponse);
				} catch (ParseException ex) {
					hrmsPortalResponse = "error,Techincal glitch";
				}

				obj.put("Response", hrmsPortalResponse);
				return obj;
				
			case "OnclickFetchHRMSDataGold":
				StaffGoldPortalCustomCode goldPortalCustomCode = new StaffGoldPortalCustomCode();
				empId = ifr.getValue("StaffNumber").toString();
				hrmsPortalResponse = "";
				try {
					hrmsPortalResponse = goldPortalCustomCode.staffDetailsVL(ifr, empId);
					Log.consoleLog(ifr, "Object of hrmsPortalResponse" + hrmsPortalResponse);
				} catch (ParseException ex) {
					hrmsPortalResponse = "error,Techincal glitch";
				}

				obj.put("Response", hrmsPortalResponse);
				return obj;
				
			case "OnclickFetchHRMSDataKavach":
				Log.consoleLog(ifr, "Inside OnclickFetchHRMSDataKavach");
				StaffKavachPortalCustomCode staffKavach = new StaffKavachPortalCustomCode();
				empId = ifr.getValue("StaffNumber").toString();
				Log.consoleLog(ifr, "Inside empId"+empId);
				String hrmsKavachResponse = "";
				try {
					hrmsKavachResponse = staffKavach.staffDetailsKavach(ifr, empId);
					Log.consoleLog(ifr, "Object of hrmsKavachResponse" + hrmsKavachResponse);
				} catch (ParseException ex) {
					hrmsKavachResponse = "error,Techincal glitch";
				}

				obj.put("Response", hrmsKavachResponse);
				return obj;
			case "onLoadDocUploadVL":
				StaffVLPortalCustomCode hrmsObject = new StaffVLPortalCustomCode();
				Log.consoleLog(ifr, "#onLoadDocUploadVL...Starting");
				response = hrmsObject.onLoadDocUploadVL(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of onLoadDocUploadVL.." + obj);
				Log.consoleLog(ifr, "#End of onLoadDocUploadVL...");
				return obj;
				
			case "onLoadDocUploadKavach":
				StaffKavachPortalCustomCode stfKavach = new StaffKavachPortalCustomCode();
				Log.consoleLog(ifr, "#onLoadDocUploadKavach...Starting");
				response = stfKavach.onLoadDocUploadKavach(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of onLoadDocUploadKavach.." + obj);
				Log.consoleLog(ifr, "#End of onLoadDocUploadKavach...");
				return obj;
				
			case "onLoadDocUploadHL":
				StaffHLPortalCustomCode onLoadDocUpdHL = new StaffHLPortalCustomCode();
				Log.consoleLog(ifr, "#onLoadDocUploadHL...Starting");
				response = onLoadDocUpdHL.onLoadDocUploadHL(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of onLoadDocUploadHL.." + obj);
				Log.consoleLog(ifr, "#End of onLoadDocUploadHL...");
				return obj;

			case "onLoadFinalScreenVL":
				StaffVLPortalCustomCode onLoadFeenVL = new StaffVLPortalCustomCode();
				Log.consoleLog(ifr, "#onLoadFinalScreenVL...Starting");
				onLoadFeenVL.onLoadFinalScreenVL(ifr);
				break;
				
			case "selectedLoans":
				StaffKavachPortalCustomCode selectedLoans = new StaffKavachPortalCustomCode();
				Log.consoleLog(ifr, "#selectedLoans...Starting");
				selectedLoans.selectedLoansKavach(ifr,value);
				break;
				
			case "onLoadFinalScreenHL":
				StaffHLPortalCustomCode onLoadFeenHL = new StaffHLPortalCustomCode();
				Log.consoleLog(ifr, "#onLoadFinalScreenHL...Starting");
				onLoadFeenHL.onLoadFinalScreenHL(ifr);
				break;
				
//			case "populateCollateralCostingOnLoadSHL":
//			    Log.consoleLog(ifr, "inside populateCollateralCostingOnLoadSHL case");
//			    StaffHLPortalCustomCode populateCollCostingLoadSHL =new StaffHLPortalCustomCode();
//			    response = populateCollCostingLoadSHL.populateCollateralCostingOnLoadSHL(ifr, control, event, value);
//			    obj.put("Response", response);
//			    return obj;
			    
			case "EligibilityAsPerRequestedAmtPortal":
				JSONObject result1 = new JSONObject();
				EligibilityCalculation eiPortal=new EligibilityCalculation();
				JSONObject eiResponsePortal = eiPortal.EligibilityAsPerRequestedAmtPortal(ifr, value);
				if (eiResponsePortal.containsKey("Error")) {
					result1= eiResponsePortal;
				} else {
					eiResponsePortal.put("Error", "");
					result1 = eiResponsePortal;
				}
				Log.consoleLog(ifr, "eiR===" + eiResponsePortal);
				obj.put("Response",
						result1.get("Error").toString().isEmpty() ? "success" : result1.get("Error").toString());
				return obj;
				
			case "EligibilityAsPerRequestedAmtGoldPortal":
				result1 = new JSONObject();
				EligibilityCalculation eiPortalGold=new EligibilityCalculation();
				JSONObject eiResponsePortalGold = eiPortalGold.EligibilityAsPerRequestedAmtPortalGold(ifr, value);
				if (eiResponsePortalGold.containsKey("Error")) {
					result1= eiResponsePortalGold;
				} else {
					eiResponsePortalGold.put("Error", "");
					result1 = eiResponsePortalGold;
				}
				Log.consoleLog(ifr, "eiR===" + eiResponsePortalGold);
				obj.put("Response",
						result1.get("Error").toString().isEmpty() ? "success" : result1.get("Error").toString());
				return obj;
				
			case "EligibilityAsPerRequestedAmtPortalHL":
				JSONObject result2 = new JSONObject();
				EligibilityCalculation eiPortalHL=new EligibilityCalculation();
				JSONObject eiResponsePortalHL = eiPortalHL.EligibilityAsPerRequestedAmtPortalHL(ifr, value);
				if (eiResponsePortalHL.containsKey("Error")) {
					result2= eiResponsePortalHL;
				} else {
					eiResponsePortalHL.put("Error", "");
					result2 = eiResponsePortalHL;
				}
				Log.consoleLog(ifr, "eiR===" + eiResponsePortalHL);
				obj.put("Response",
						result2.get("Error").toString().isEmpty() ? "success" : result2.get("Error").toString());
				return obj;
			case "EligibilityAsPerRequestedAmt":
				EligibilityCalculationInterface ei = new EligibilityCalculation();
				JSONObject eiResponse = ei.EligibilityAsPerRequestedAmt(ifr, value);
				Log.consoleLog(ifr, "eiR===" + eiResponse);
				obj.put("Response",
						eiResponse.get("Error").toString().isEmpty() ? "success" : eiResponse.get("Error").toString());
				return obj;
				
			case "EligibilityAsPerRequestedAmtGold":
				EligibilityCalculationInterface eiG = new EligibilityCalculation();
				JSONObject eiResponseG = eiG.EligibilityAsPerRequestedAmtGold(ifr, value);
				Log.consoleLog(ifr, "eiR===" + eiResponseG);
				obj.put("Response",
						eiResponseG.get("Error").toString().isEmpty() ? "success" : eiResponseG.get("Error").toString());
				return obj;
//			case "EligibilityAsPerRequestedAmtHL":
//				EligibilityCalculationInterface eiHL = new EligibilityCalculation();
//				JSONObject eiResponseHL = eiHL.EligibilityAsPerRequestedAmtHL(ifr, value);
//				Log.consoleLog(ifr, "eiR===" + eiResponseHL);
//				obj.put("Response",
//						eiResponseHL.get("Error").toString().isEmpty() ? "Success" : eiResponseHL.get("Error").toString());
//				return obj;
			
			case "EligibilityAsPerRequestedAmtRO":
				EligibilityCalculationInterface ei1 = new EligibilityCalculation();
				JSONObject eiResponse1 = ei1.EligibilityAsPerRequestedAmtRO(ifr, value);
				Log.consoleLog(ifr, "eiR===" + eiResponse1);
				obj.put("Response", eiResponse1.get("Error").toString().isEmpty() ? "success"
						: eiResponse1.get("Error").toString());
				return obj;
//			case "EligibilityAsPerRequestedAmtHLRO":
//				EligibilityCalculationInterface ei12 = new EligibilityCalculation();
//				JSONObject eiResponse12 = ei12.EligibilityAsPerRequestedAmtHLRO(ifr, value);
//				Log.consoleLog(ifr, "eiR===" + eiResponse12);
//				obj.put("Response", eiResponse12.get("Error").toString().isEmpty() ? "Success"
//						: eiResponse12.get("Error").toString());
//				return obj;
			case "mGenerateDocVL":
				StaffVLPortalCustomCode staffVLPortalCu = new StaffVLPortalCustomCode();
				response = staffVLPortalCu.mGenDoc(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of mGenerateDocVL.." + obj);
				Log.consoleLog(ifr, "#End of mGenerateDocVL...");
				return obj;
			case "mGenerateDocHL":
				StaffHLPortalCustomCode staffHLGenDoc = new StaffHLPortalCustomCode();
				response = staffHLGenDoc.mGenDoc(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of mGenerateDocHL.." + obj);
				Log.consoleLog(ifr, "#End of mGenerateDocHL...");
				return obj;
			case "actionGridSave":
				Log.consoleLog(ifr, "#actionGridSave...Starting");
				response = hpcc.actionGridSave(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of OnClickGenNESLButton.." + obj);
				Log.consoleLog(ifr, "#End of OnClickGenNESLButton...");
				return obj;
			case "placeHolRecomMonInstal":
				EligibilityCalculation elgblitycal = new EligibilityCalculation();
				Log.consoleLog(ifr, "#placeHolRecomMonInstal...Starting");
				response = elgblitycal.placeHolderRecommendedMonthlyInstallement(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of placeHolRecomMonInstal.." + obj);
				Log.consoleLog(ifr, "#End of placeHolRecomMonInstal...");
				return obj;
			case "sliderCheck":
				Log.consoleLog(ifr, "#sliderCheck...Starting");
				response = hpcc.sliderCheck(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of sliderCheck.." + obj);
				Log.consoleLog(ifr, "#End of sliderCheck...");
				return obj;
			case "checkFinalElgiblityComp":
				Log.consoleLog(ifr, "#checkFinalElgiblityComp...Starting");
				response = hpcc.checkFinalElgiblityComp(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of checkFinalElgiblityComp.." + obj);
				Log.consoleLog(ifr, "#End of checkFinalElgiblityComp...");
				return obj;
			case "mAccSendOTPValidate":
				Log.consoleLog(ifr, "#mAccSendOTPValidate...Starting");
				response = pcc.mAccSendOTPValidate(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of mAccSendOTPValidate.." + obj);
				Log.consoleLog(ifr, "#End of mAccSendOTPValidate...");
				return obj;
			case "mAccClickSendOTPRecieveMoney":
				Log.consoleLog(ifr, "#mAccClickSendOTPRecieveMoney...Starting");
				response = pcc.mAccClickSendOTPRecieveMoney(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of mAccClickSendOTPRecieveMoney.." + obj);
				Log.consoleLog(ifr, "#End of mAccClickSendOTPRecieveMoney...");
				return obj;
				
			case "onLoadGetRecLoanSelTyp":
				Log.consoleLog(ifr, "#onLoadGetRecLoanSelTyp...Starting");
				response = hpcc.onLoadGetRecLoanSelTyp(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of onLoadGetRecLoanSelTyp.." + obj);
				Log.consoleLog(ifr, "#End of onLoadGetRecLoanSelTyp...");
				return obj;

			case "onLoadGetRecLoanSelType":
				Log.consoleLog(ifr, "#onLoadGetRecLoanSelType...Starting");
				hpcc.onLoadGetRecLoanSelType(ifr);
				break;
			case "onLoadActionGrid":
				Log.consoleLog(ifr, "#actionGrid...Starting");
				hpcc.actionGrid(ifr, value);
				break;
			case "onLoadGetRecAppLoanSelType":
				Log.consoleLog(ifr, "#onLoadGetRecAppLoanSelType...Starting");
				hpcc.onLoadGetRecAppLoanSelType(ifr);
				break;
			case "onLoadGetBranchCodeandName":
				Log.consoleLog(ifr, "#onLoadGetBranchCodeandName...Starting");
				response = hpcc.onLoadGetBranchCodeandName(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of onLoadGetBranchCodeandName.." + obj);
				Log.consoleLog(ifr, "#End of onLoadGetBranchCodeandName...");
				return obj;

			case "onLoadGetBranchCodeandNameVL":
				Log.consoleLog(ifr, "#onLoadGetBranchCodeandNameVL...Starting");
				response = StaffVLPortalCustomCode.onLoadGetBranchCodeandNameVL(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of onLoadGetBranchCodeandNameVL.." + obj);
				Log.consoleLog(ifr, "#End of onLoadGetBranchCodeandNameVL...");
				return obj;

			case "uploadDocument":
				Log.consoleLog(ifr, "#uploadDocument...Starting");
				uploadDocToOD uploadDocToOD = new uploadDocToOD();
				response = uploadDocToOD.uploadDocToOD(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of uploadDocument.." + obj);
				Log.consoleLog(ifr, "#End of uploadDocument...");
				return obj;

			case "mGenerateDoc":
				HRMSPortalCustomCode hrm = new HRMSPortalCustomCode();
				try {
					response = hrm.mGenDoc(ifr, value);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of mGenerateDoc.." + obj);
				Log.consoleLog(ifr, "#End of mGenerateDoc...");
				return obj;
				
			case "mGenerateDocKavach":
				StaffKavachPortalCustomCode kavaChCode = new StaffKavachPortalCustomCode();
				try {
					response = kavaChCode.mGenDoc(ifr, value);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of mGenerateDoc.." + obj);
				Log.consoleLog(ifr, "#End of mGenerateDoc...");
				return obj;

			case "createVLLoanDisburse":
				Log.consoleLog(ifr, "#createVLLoanDisburse...Starting");
				StaffVLPortalCustomCode staffCusCode = new StaffVLPortalCustomCode();
				response = staffCusCode.createVLLoanDisburse(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of createVLLoanDisburse.." + obj);
				Log.consoleLog(ifr, "#End of createVLLoanDisburse...");
				return obj;
				
			case "createHLLoanDisburse":
				Log.consoleLog(ifr, "#createVLLoanDisburse...Starting");
				StaffHLPortalCustomCode staffHl = new StaffHLPortalCustomCode();
				response = staffHl.createHLLoanDisburse(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of createHLLoanDisburse.." + obj);
				Log.consoleLog(ifr, "#End of createHLLoanDisburse...");
				return obj;

			case "OnLoadGetLoanDetails":
				Log.consoleLog(ifr, "#OnLoadGetLoanDetails...Starting");
				hpcc.OnLoadGetLoanDetails(ifr, value);
				break;

			case "OnExternalLoanDetailsDetails":
				Log.consoleLog(ifr, "#OnExternalLoanDetailsDetails...Starting");
				StaffVLPortalCustomCode staffCustCode = new StaffVLPortalCustomCode();
				staffCustCode.OnExternalLoanDetailsDetails(ifr);
				break;

			case "backOfficeLoanDisburse":
				Log.consoleLog(ifr, "#OnLoadGetLoanDetails...Starting");
				response = hpcc.backOfficeLoanDisburse(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of uploadDocument.." + obj);
				Log.consoleLog(ifr, "#End of uploadDocument...");
				return obj;
				
			case "backOfficeLoanDisburseKavach":
				Log.consoleLog(ifr, "#OnLoadGetLoanDetails...Starting");
				StaffKavachPortalCustomCode kavachbackOfficeLoanDisburse = new StaffKavachPortalCustomCode();
				response = kavachbackOfficeLoanDisburse.backOfficeLoanDisburse(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of uploadDocument.." + obj);
				Log.consoleLog(ifr, "#End of uploadDocument...");
				return obj;

			case "OnClickCaculateEMI":
				Log.consoleLog(ifr, "#OnClickCaculateEMI...Starting");
				response = hpcc.calculateEMI(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of hrmsdata.." + obj);
				Log.consoleLog(ifr, "#End of getLoanAmtOnPageLoad...");
				return obj;
				
			case "OnClickCalEMI":
				Log.consoleLog(ifr, "#OnClickCaculateEMI...Starting");
				StaffKavachPortalCustomCode sKavach = new StaffKavachPortalCustomCode();
				response = sKavach.onClickCalEMIKavach(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of hrmsdata.." + obj);
				Log.consoleLog(ifr, "#End of getLoanAmtOnPageLoad...");
				return obj;
				
			case "onLoadGetLoanSelType":
				Log.consoleLog(ifr, "#onLoadGetLoanSelType...Starting");
				response = hpcc.onLoadGetLoanSelType(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of hrmsdata.." + obj);
				Log.consoleLog(ifr, "#End of getLoanAmtOnPageLoad...");
				return obj;
			case "OnClickSaveApproveAmt":
				Log.consoleLog(ifr, "#OnClickSaveApproveAmt...Starting");
				response = hpcc.OnClickSaveApproveAmt(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of OnClickSaveApproveAmt.." + obj);
				Log.consoleLog(ifr, "#End of OnClickSaveApproveAmt...");
				return obj;
			case "isAllDocumentsUploaded":
				Log.consoleLog(ifr, "#isAllDocumentsUploaded...Starting");
				response = hpcc.isAllDocumentsUploaded(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of isAllDocumentsUploaded.." + obj);
				Log.consoleLog(ifr, "#End of isAllDocumentsUploaded...");
				return obj;

			case "BackOffice":
				Log.consoleLog(ifr, "#BackOffice...Starting");
				StaffVLPortalCustomCode backOfficeStage = new StaffVLPortalCustomCode();
				backOfficeStage.backOffice(ifr,value);
				break;
				
			case "Summary":
				Log.consoleLog(ifr, "#BackOffice...Starting");
				StaffVLPortalCustomCode summary = new StaffVLPortalCustomCode();
				summary.summaryB(ifr,value);
				break;
				
			case "BackOfficeHL":
				Log.consoleLog(ifr, "#BackOfficeHL...Starting");
				StaffHLPortalCustomCode backOfficeHL = new StaffHLPortalCustomCode();
				backOfficeHL.backOffice(ifr);
				break;

			case "backOfficeKnockOffRule":
				Log.consoleLog(ifr, "backOfficeKnockOffRule");
				String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
				String empIdQuery = "select STAFF_NUMBER from slos_staff_trn where winame='" + processInstanceId + "'";
				List<List<String>> empRes = ifr.getDataFromDB(empIdQuery);
				String emp = "";
				for (List<String> res : empRes) {
					emp = res.get(0);
				}
				HRMSPortalCustomCode hrmsknockOff = new HRMSPortalCustomCode();
				Advanced360EnquiryHRMSData adv360 = new Advanced360EnquiryHRMSData();
				try {
					String result = adv360.getDemographic(ifr);
					if (result.equals(AccelatorStaffConstant.WRITE_OFF_ERROR_MESSAGE)
							|| result.equalsIgnoreCase("ERROR")) {
						Log.consoleLog(ifr, "backOfficeKnockOffRule==>getDemographic=> response " + result);
						getCurrentStage(ifr, processInstanceId);
						obj.put("Response", result);
					}
					if (result.contains(RLOS_Constants.ERROR)) {
						obj.put("Response", result);
						getCurrentStage(ifr, processInstanceId);
						return obj;
					}
					result = adv360.advance360KnockOff(ifr);
					if (result.equals(AccelatorStaffConstant.NPA_ERROR_MESSAGE)
							|| result.equals(AccelatorStaffConstant.SMA_ERROR_MESSAGE)
							|| result.contains(AccelatorStaffConstant.TOTALOVERDUE_ERROR_MESSAGE)) {
//							|| result.equalsIgnoreCase("ERROR")) {
//							|| result.equalsIgnoreCase("ERROR")) {
						Log.consoleLog(ifr, "backOfficeKnockOffRule==>advance360KnockOff=> response " + result);
						obj.put("Response", result);
						getCurrentStage(ifr, processInstanceId);
						return obj;
					}
					if (result.contains(RLOS_Constants.ERROR)) {
						obj.put("Response", result);
						getCurrentStage(ifr, processInstanceId);
						return obj;
					}
					result = hrmsknockOff.getHRMSDetailsKnockOff(ifr);
					if (result.equals(AccelatorStaffConstant.IR_STATUS_MESSAGE) ||

							result.equals(AccelatorStaffConstant.RETIRING_STAFF_MESSAGE)
							|| result.equals(AccelatorStaffConstant.EX_SERVICESMEN_ERROR_MESSAGE)
						   || result.equalsIgnoreCase("ERROR")) {
						Log.consoleLog(ifr, "backOfficeKnockOffRule==>getHRMSDetailsKnockOff=> response " + result);
						obj.put("Response", result);
						getCurrentStage(ifr, processInstanceId);
						return obj;
					}

					Log.consoleLog(ifr, "backOfficeKnockOffRule==> response " + result);
					obj.put("Response", result);
					return obj;
				} catch (Exception e) {
					obj.put("Response", "ERROR");
					return obj;
				}

			case "onchageRequiredLoanAmt":
				Log.consoleLog(ifr, "#Inside getLoanAmtOnPageLoad...");
				response = hrmspcc.getLoanAmtOnPageLoad(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of hrmsdata.." + obj);
				Log.consoleLog(ifr, "#End of getLoanAmtOnPageLoad...");
				return obj;
			case "SLValidateOTP":
				Log.consoleLog(ifr, "avail offer entered");
				String resp = pcc.mAccValidateOTPHRMSDisbursement(ifr, control, event, value);
				obj.put("Response", resp);
				Log.consoleLog(ifr, "avail offer response " + obj);
				return obj;
			case "generateRefNumber":
				Log.consoleLog(ifr, "generateRefNumber==>generateRefNumber");
				String responRefNum = hrmspcc.generateRefNumber(ifr);
				obj.put("Response", responRefNum);
				Log.consoleLog(ifr, "generateRefNumber" + obj);
				return obj;
			case "EligiblityValidation":
				Log.consoleLog(ifr, "EligiblityValidation==>entered");
				String res = hpcc.mClickEligiblityValidation(ifr);
				obj.put("response", res);
				Log.consoleLog(ifr, "Eligibliblity calc " + obj);
				return obj;

			case "OnChangeROIType":// Added by Ahmed on 29-03-2024 for ROI Discussion happened with Pandiyan
				Log.consoleLog(ifr, "#Inside OnChangeROIType...");
				pcm.selectROIValue(ifr);
				Log.consoleLog(ifr, "#End of OnChangeROIType...");
				break;
			case "OnClickfetchHRMSData":
				String hrmsData = "";
				Log.consoleLog(ifr, "#Inside OnClickfetchHRMSData...");
				try {
					hrmsData = hrmspcc.getHRMSData(ifr, value);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				obj.put("Response", hrmsData);
				Log.consoleLog(ifr, "Object of hrmsdata.." + obj);
				Log.consoleLog(ifr, "#End of OnClickfetchHRMSData...");
				return obj;
			case "downloadGeneratedDocument":
				Log.consoleLog(ifr, "#Inside downloadGeneratedDocument...");
				hrmspcc.downLoadSignedGeneratedDocument(ifr);
				Log.consoleLog(ifr, "#End of downloadGeneratedDocument...");
				break;
				
			case "OnSaveRecLoanAmtTenure":
				Log.consoleLog(ifr, "#Inside OnSaveRecLoanAmtTenure...");
				hrmspcc.OnSaveRecLoanAmtTenure(ifr);
				Log.consoleLog(ifr, "#End of OnSaveRecLoanAmtTenure...");
				break;
			
			case "UpdateBorrowerMobileNumber":
				Log.consoleLog(ifr, "#Inside UpdateBorrowerMobileNumber...");
				StaffHLPortalCustomCode updateBorrower = new StaffHLPortalCustomCode();
				updateBorrower.UpdateBorrowerMobileNumber(ifr);
				Log.consoleLog(ifr, "#End of UpdateBorrowerMobileNumber...");
				break;
				
			case "checkStampCharges":
				Log.consoleLog(ifr, "#Inside checkStampCharges...");
				response=hrmspcc.checkStampCharges(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of checkStampCharges.." + obj);
				Log.consoleLog(ifr, "#End of checkStampCharges...");
				return obj;
				
			case "checkODRenEnhance":
				Log.consoleLog(ifr, "#Inside checkODRenEnhance...");
				response=hrmspcc.checkODRenEnhance(ifr);
				obj.put("Response", response);
				Log.consoleLog(ifr, "Object of checkODRenEnhance.." + obj);
				Log.consoleLog(ifr, "#End of checkODRenEnhance...");
				return obj;
				
			case "downloadGeneratedDocumentBO":
				Log.consoleLog(ifr, "#Inside downloadGeneratedDocument...");
				response = hrmspcc.downLoadGeneratedDocument(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "downloadGeneratedDocument response " + obj);
				return obj;
				
			case "downloadGeneratedDocumentHLBO":
				Log.consoleLog(ifr, "#Inside downloadGeneratedDocument...");
				StaffHLPortalCustomCode downloadGenerated = new StaffHLPortalCustomCode();
				response = downloadGenerated.downLoadGeneratedDocument(ifr, value);
				obj.put("Response", response);
				Log.consoleLog(ifr, "downloadGeneratedDocument response " + obj);
				return obj;
			case "availOffer":
				Log.consoleLog(ifr, "availOffer==>entered");
				HRMSPortalCustomCode hrms = new HRMSPortalCustomCode();
				String finalResponse = hrms.mClickAvailHRMSButton(ifr, value);
				obj.put("Response", finalResponse);
				Log.consoleLog(ifr, "avail offer response " + obj);
				return obj;
				
			case "availOfferKavach":
				Log.consoleLog(ifr, "availOffer==>entered");
				StaffKavachPortalCustomCode staffKavachP = new StaffKavachPortalCustomCode();
				String finalResponse1 = staffKavachP.mClickAvailHRMSButton(ifr, value);
				obj.put("Response", finalResponse1);
				Log.consoleLog(ifr, "avail offer response " + obj);
				return obj;
				
			case "NonNESLStateCheck":
				String nonNeslCheck = "";
				Log.consoleLog(ifr, "NonNESLStateCheck : ");
				nonNeslCheck = hpcc.nonNeslStateValidation(ifr, value);
				Log.consoleLog(ifr, "nonNeslCheck : " + nonNeslCheck);
				obj.put("Response", nonNeslCheck);
				Log.consoleLog(ifr, "obj========= : " + obj);
				return obj;
			
			case "NonNESLStateCheckForStaff":
				String nonNESLStateCheckForStaff = "";
				Log.consoleLog(ifr, "NonNESLStateCheck : ");
				nonNESLStateCheckForStaff = hpcc.nonNeslStateValidationForStaff(ifr, value);
				Log.consoleLog(ifr, "nonNeslCheck : " + nonNESLStateCheckForStaff);
				obj.put("Response", nonNESLStateCheckForStaff);
				Log.consoleLog(ifr, "obj========= : " + obj);
				return obj;
			case "NESLEntry":
				String nESLEntryCheck = "";
				Log.consoleLog(ifr, "NESLEntry : ");
				nonNeslCheck = hpcc.nESLEntry(ifr, value);
				Log.consoleLog(ifr, "nESLEntryCheck : " + nonNeslCheck);
				obj.put("Response", nonNeslCheck);
				Log.consoleLog(ifr, "obj========= : " + obj);
				return obj;
			case "AvaIlMoreLoanOnProdCode":
				String avaIlMoreLoanOnProdCodeCheck = "";
				Log.consoleLog(ifr, "AvaIlMoreLoanOnProdCode : ");
				avaIlMoreLoanOnProdCodeCheck = hpcc.avaIlMoreLoanOnProdCode(ifr, value);
				Log.consoleLog(ifr, "avaIlMoreLoanOnProdCodeCheck : " + avaIlMoreLoanOnProdCodeCheck);
				obj.put("Response", avaIlMoreLoanOnProdCodeCheck);
				Log.consoleLog(ifr, "obj========= : " + obj);
				return obj;
			case "NotEligibleForLoan":
				String notEligibleForLoanCheck = "";
				Log.consoleLog(ifr, "NotEligibleForLoan : ");
				notEligibleForLoanCheck = hpcc.notEligibleForLoan(ifr, value);
				Log.consoleLog(ifr, "notEligibleForLoanCheck : " + notEligibleForLoanCheck);
				obj.put("Response", notEligibleForLoanCheck);
				Log.consoleLog(ifr, "obj========= : " + obj);
				return obj;

			case "AvailedLoanOnSameDay":
				String availedLoanOnSameDay = "";
				Log.consoleLog(ifr, "availedLoanOnSameDay : ");
				String availedLoanOnToday = hpcc.availedLoanOnSameDay(ifr);
				Log.consoleLog(ifr, "availedLoanOnToday : " + availedLoanOnToday);
				obj.put("Response", availedLoanOnToday);
				Log.consoleLog(ifr, "obj========= : " + obj);
				return obj;

			case "OnClickExistingCust":
				Log.consoleLog(ifr, "#Inside OnClickCoApplicant...");
				String respon = vpcc.OnClickExistingCust(ifr);
				Log.consoleLog(ifr, "Inside respon" + respon);
				obj.put("Response", respon);
				return obj;
			case "GetDocumentIndex":
				Log.consoleLog(ifr, "Inside GetDocumentIndex");
				// String Response = pcm.downloadTestDocumentFromOD(ifr,
				// ifr.getObjGeneralData().getM_strProcessInstanceId());
				String Response = hpcc.downLoadSignedGeneratedDocument(ifr);
				obj.put("Response", Response);
				return obj;
			case "GetESigningURL":
				Log.consoleLog(ifr, "Inside GetNESLURL");

				String retStatus = "";
				retStatus = NESLCM.getEsignURL(ifr);
				Log.consoleLog(ifr, "retStatus==>" + retStatus);

				obj.put("Response", retStatus);
				return obj;

			case "ReviewDocument":
				Log.consoleLog(ifr, "Inside ReviewDocument");
				String Response2 = ppcc.generateAndReviewDocument(ifr, "PAPL");
				Log.consoleLog(ifr, "Response2==>" + Response2);
				obj.put("Response", Response2);
				return obj;
			case "saveDataInPartyDetailGridFetch":
				Log.consoleLog(ifr, "Inside saveDataInPartyDetailGridFetch::AcceleratorBaseCode:::");
				bbcc.msaveDataInPartyDetailGridFetch(ifr);
			case "BackButtonDataSavingAllStep":
				Log.consoleLog(ifr, "#Inside BackButtonDataSavingAllStep..");
				Log.consoleLog(ifr, "WorkStep Name " + value);
				String loanSelectedType = pcm.getLoanType(ifr, "", "", "");
				Log.consoleLog(ifr, "loanSelectedType : " + loanSelectedType);

				if (loanSelectedType.equalsIgnoreCase("Staff Loan")) {
					if (value.equalsIgnoreCase("Avail Offer")) {
						pcm.stepNameUpdate(ifr, "Staff Details");
					} else if (value.equalsIgnoreCase("Final Eligibility and Doc")) {
						pcm.stepNameUpdate(ifr, "Final Eligibility and Doc");
					} else if (value.equalsIgnoreCase("Receive the Money")) {
						pcm.stepNameUpdate(ifr, "Receive the Money");
					} else if (value.equalsIgnoreCase("Summary")) {
						pcm.stepNameUpdate(ifr, "Summary");
					}

				} else if (loanSelectedType.equalsIgnoreCase("Staff Vehicle")) {
					if (value.equalsIgnoreCase("Collateral Details")) {
						pcm.stepNameUpdate(ifr, "Staff");
					} else if (value.equalsIgnoreCase("Avail Offer")) {
						pcm.stepNameUpdate(ifr, "Collateral Details");
					} else if (value.equalsIgnoreCase("Document Upload")) {
						pcm.stepNameUpdate(ifr, "Avail Offer");
					} else if (value.equalsIgnoreCase("Summary")) {
						pcm.stepNameUpdate(ifr, "Document Upload");
					}

				} 
				else if (loanSelectedType.equalsIgnoreCase("Staff Home Loan")) {
					if (value.equalsIgnoreCase("Co-Applicant")) {
						pcm.stepNameUpdate(ifr, "Staff Details");
					} else if (value.equalsIgnoreCase("Collateral Details")) {
						pcm.stepNameUpdate(ifr, "Co-Applicant");
					} else if (value.equalsIgnoreCase("In-Principle Approval")) {
						pcm.stepNameUpdate(ifr, "Collateral Details");
					} else if (value.equalsIgnoreCase("Document Upload")) {
						pcm.stepNameUpdate(ifr, "In-Principle Approval");
					} else if (value.equalsIgnoreCase("Summary")) {
						pcm.stepNameUpdate(ifr, "Document Upload");
					}

				}
				else if (loanSelectedType.toLowerCase().trim().contains("kavach")) {
					if (value.equalsIgnoreCase("Avail Offer")) {
						pcm.stepNameUpdate(ifr, "Staff Details");
					} else if (value.equalsIgnoreCase("Insurance Details")) {
						pcm.stepNameUpdate(ifr, "Avail Offer");
					} else if (value.equalsIgnoreCase("Summary")) {
						pcm.stepNameUpdate(ifr, "Insurance Details");
					} 

				}
				else if (loanSelectedType.toLowerCase().trim().contains("gold")) {
					if (value.equalsIgnoreCase("Jewellery Details")) {
						pcm.stepNameUpdate(ifr, "Staff Details");
					} else if (value.equalsIgnoreCase("Avail Offer")) {
						pcm.stepNameUpdate(ifr, "Jewellery Details");
					} else if (value.equalsIgnoreCase("Summary")) {
						pcm.stepNameUpdate(ifr, "Avail Offer");
					}

				}
				else if (loanSelectedType.equalsIgnoreCase("Pre-Approved Personal Loan")) {
					// pcm.stepNameUpdate(ifr, "Avail Offer");
					if (value.equalsIgnoreCase("Avail Offer")) {
						pcm.stepNameUpdate(ifr, "Avail Offer");
					} else if (value.equalsIgnoreCase("Final Eligibility and Doc")) {
						pcm.stepNameUpdate(ifr, "Final Eligibility and Doc");
					} else if (value.equalsIgnoreCase("Receive the Money")) {
						pcm.stepNameUpdate(ifr, "Receive the Money");
					} else if (value.equalsIgnoreCase("Summary")) {
						pcm.stepNameUpdate(ifr, "Summary");
					}

				} else if (loanSelectedType.equalsIgnoreCase("Loan Against Deposit")) {
					// pcm.stepNameUpdate(ifr, "Initial Data");

					if (value.equalsIgnoreCase("Initial Data")) {
						pcm.stepNameUpdate(ifr, "Initial Data");
					} else if (value.equalsIgnoreCase("Final Eligibility and Doc")) {
						pcm.stepNameUpdate(ifr, "Final Eligibility and Doc");
					} else if (value.equalsIgnoreCase("Receive Money")) {
						pcm.stepNameUpdate(ifr, "Receive Money");
					} else if (value.equalsIgnoreCase("Disbursement")) {
						pcm.stepNameUpdate(ifr, "Disbursement");
					}

				} else if (value.equalsIgnoreCase("Occupation Details")) {
					Log.consoleLog(ifr, "Value : " + value);
					pcm.stepNameUpdate(ifr, "Loan Details");
				} else if (value.equalsIgnoreCase("InPrinciple Approval")) {
					pcm.stepNameUpdate(ifr, "Occuapation Detail");
				} else if (value.equalsIgnoreCase("Occupation Details ")) {
					Log.consoleLog(ifr, "Value Pension");
					pcm.stepNameUpdate(ifr, "Loan Details ");
					Log.consoleLog(ifr, "Before B Data Saving");
					plpc.savePansionOccuapationBorrower(ifr, control, event, value);
					Log.consoleLog(ifr, "Before CB Data Saving");
					plpc.savePansionOccuapationCoBorrower(ifr, control, event, value);
					Log.consoleLog(ifr, "After CB Data Saving");
				} else if (value.equalsIgnoreCase("In Principle Approval")) {
					pcm.stepNameUpdate(ifr, "Occuapation Detail");
				}
				break;
			case "DataSavingAllStepHRMS":
				Log.consoleLog(ifr, "WorkStep Name " + value);

				String loanType = pcm.getLoanType(ifr, "", "", "");
				Log.consoleLog(ifr, "loanType : " + loanType);

				if (loanType.equalsIgnoreCase("Pre-Approved Personal Loan")) {
					if (value.equalsIgnoreCase("Final Eligibility and Doc")) {
						Log.consoleLog(ifr, "inside Final Eligibility and Doc");
						response = ppcc.populateFinalEligibilityDetail(ifr, control, event, value);
						obj.put("Response", response);
						return obj;
					}
				} else if (loanType.equalsIgnoreCase("Loan Against Deposit")) {
					if (value.equalsIgnoreCase("Final Eligibility and Doc")) {
						Log.consoleLog(ifr, "Final Eligibility and Doc");
						String returnObj = LADOBJ.onClickNextFinalEligibleityOD(ifr, value);
						if (!(returnObj.equalsIgnoreCase(""))) {
							obj.put("Response", returnObj);
							return obj;
						}
						String loanAmount = ifr.getValue("Portal_ODAD_EE_LoanAmount").toString();
						pcm.mAccSetSliderValue(ifr, control, event,
								loanAmount + "," + ifr.getValue("Portal_ODAD_EE_Tenure"));
						break;
					}
				}

				if (value.equalsIgnoreCase("Summary")) {
					Log.consoleLog(ifr, "inside Summary");
					pcm.populateDisbursementDetail(ifr, control, event, value);
					break;
				} else if (value.equalsIgnoreCase("Documents Upload")) {
					Log.consoleLog(ifr, "Documents Upload");
					String countDone = this.pcm.countWFDone(ifr, control, event, value);
					Log.consoleLog(ifr, "countDone" + countDone);
					response = countDone;
					obj.put("Response", response);
					return obj;
				} else if (value.equalsIgnoreCase("DocumentsUploadPension")) {
					Log.consoleLog(ifr, "Documents Upload");
					String countDone = this.plpc.pensionDocumentValidation(ifr, control, event, value);
					Log.consoleLog(ifr, "countDone" + countDone);
					response = countDone;
					obj.put("Response", response);
					return obj;
				} else if (value.equalsIgnoreCase("ValidateDocumentVL")) {
					Log.consoleLog(ifr, "Document Upload");
					String countDone = vpcc.validateDocumentUpload(ifr, control, event, value);
					Log.consoleLog(ifr, "countDone" + countDone);
					response = countDone;
					obj.put("Response", response);

					return obj;
				} else if (value.toUpperCase().equalsIgnoreCase("Documents upload ")) {
					Log.consoleLog(ifr, "Document Uploads");
					String Status = plpc.mImpPensionOnClickDocumentUpload(ifr);
					if (Status.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {

						// cbpcc.mImpOnClickDocumentUpload(ifr, control, event, value);
						Log.consoleLog(ifr, "AcceleratorBaseCode:DOCUMENTS UPLOAD::");
						String countDone = this.pcm.countWFDonePension(ifr, control, event, value);
						Log.consoleLog(ifr, "countDone" + countDone);
						response = countDone;
						obj.put("Response", response);
					} else {
						obj.put("Response", Status);
					}
					return obj;
				} else if (value.equalsIgnoreCase("Occupation Details")) {
					Log.consoleLog(ifr, "Occupation Details");
					String objmana = cbpcc.mCheckmanatPortalFieldsCB(ifr);
					Log.consoleLog(ifr, "objmana" + objmana);
					String objmanaCoBo = cbpcc.mCheckmanatPortalFieldsCBCoBo(ifr);
					Log.consoleLog(ifr, "objmanaCoBo" + objmanaCoBo);
					if (objmana.equalsIgnoreCase("success")) {
						if (objmanaCoBo.equalsIgnoreCase("success")) {
							response = cbpcc.mImpOnClickOccupationDetails(ifr, control, event, value);
							Log.consoleLog(ifr, "Response Message comming Occupation Details ::" + response);
							if (response.contains("showMessage")) {
								obj.put("Response", response);
								return obj;
							}
						} else {
							Log.consoleLog(ifr, "No data found  Message comming Occupation Details ::" + objmanaCoBo);
							obj.put("Response", pcm.returnErrorThroughExecute(ifr,
									"Kindly fill the Manadatory field " + objmanaCoBo + ""));
							return obj;
						}
					} else {
						Log.consoleLog(ifr, "No data found  Message comming Occupation Details ::" + objmana);
						obj.put("Response",
								pcm.returnErrorThroughExecute(ifr, "Kindly fill the Manadatory field " + objmana + ""));
						return obj;
					}
					return obj;
				} else if (value.equalsIgnoreCase("Loan Details")) {
					Log.consoleLog(ifr, "Loan Details");
					response = cbpcc.mImpOnClickLoanDetailsDataCB(ifr, control, event, value);
					Log.consoleLog(ifr, "Response Message comming Loan Details::::" + response);
					obj.put("Response", response);
					return obj;
				} // added by KeerthanaR for pension phase-2 development
				else if (value.equalsIgnoreCase("Loan Details ")) {
					Log.consoleLog(ifr, "Loan Details");
					response = plpc.mImpOnClickLoanDetailsDataCP(ifr, control, event, value);
					Log.consoleLog(ifr,
							"Response Message comming Loan Details::mImpOnClickLoanDetailsDataCP::" + response);
					obj.put("Response", response);
					return obj;
				} else if (value.equalsIgnoreCase("Occupation Details ")) {
					Log.consoleLog(ifr, "Occupation Details Pension");
					response = plpc.mPensionOnClickOccupationDetails(ifr, control, event, value);
					if (response.equalsIgnoreCase("No_ext")) {
						Log.consoleLog(ifr, "return error condition");
						obj.put("Response", pcm.returnErrorThroughExecute(ifr, "Kindly Visit Branch"));
						return obj;
					}
					Log.consoleLog(ifr, " before branchcode set");
					pcm.setbranchcode(ifr);// Added by Ahmed on 07-08-2024
					Log.consoleLog(ifr, "after branchcode set");

				} else if (value.equalsIgnoreCase("Occupation Detail")) {
					Log.consoleLog(ifr, "Occupation Detail");
					String objmandatory = vpcc.mCheckmanatPortalFieldsVL(ifr);
					Log.consoleLog(ifr, "objmandatoryVL" + objmandatory);
					if (objmandatory.equalsIgnoreCase("success")) {
						response = vpcc.mImpOnClickOccupationDetails(ifr, control, event, value);
						Log.consoleLog(ifr, "Response Message comming Occupation Details VL::" + response);
						if (response.contains("showMessage")) {
							obj.put("Response", response);
							return obj;
						} else {
							String validateButton = vpcc.validateCheck(ifr);
							Log.consoleLog(ifr, "validateButton" + validateButton);
							String strfkey = cbpcc.Fkey(ifr, "B");
							String CBRequired = ifr.getValue("P_VL_OD_Co_Applicant").toString();
							Log.consoleLog(ifr, "CBRequired::" + CBRequired);
							if (CBRequired.equalsIgnoreCase("Yes")) {
								if (validateButton.equalsIgnoreCase("success")) {
									Log.consoleLog(ifr, "No data found  Message validate button ::" + validateButton);
									obj.put("Response", pcm.returnErrorThroughExecute(ifr,
											"Kindly validate the Co-Borrower details"));
									return obj;
								}
							}
							if (CBRequired.equalsIgnoreCase("Yes")) {
								String resultVL = vpcc.mprofileValidationCB(ifr, control);
								Log.consoleLog(ifr, "mprofileValidationCB resultVL==>" + resultVL);
								if (!resultVL.equalsIgnoreCase("")) {
									obj.put("Response", pcm.returnErrorThroughExecute(ifr, resultVL));
									return obj;
								} else {
									obj.put("Response", resultVL);
									return obj;
								}
							} else {
								String resultVL = vpcc.validateGrossMonthlyIncomeVL(ifr, control);
								Log.consoleLog(ifr, "validateGrossMonthlyIncomeVL resultVL==>" + resultVL);
								if (!resultVL.equalsIgnoreCase("")) {
									obj.put("Response", pcm.returnErrorThroughExecute(ifr, resultVL));
									return obj;
								} else {
									obj.put("Response", resultVL);
									return obj;
								}
							}
						}
					} else {
						Log.consoleLog(ifr, "No data found  Message comming Occupation Details ::" + objmandatory);
						obj.put("Response", pcm.returnErrorThroughExecute(ifr,
								"Kindly fill the Manadatory field " + objmandatory + ""));
						return obj;
					}
				} else if (value.equalsIgnoreCase("Final Eligibility ")) {
					Log.consoleLog(ifr, "Final Eligibility pension");
					plpc.mImpOnClickFianlEligibility(ifr, control, event, value);
				} // } else if (value.equalsIgnoreCase("Final Eligibility and Doc")) {
					// Log.consoleLog(ifr, "Final Eligibility Budget");
					// response = cbpcc.mImpOnClickFianlEligibility(ifr, control, event, value);
					// obj.put("Response", response);
					// return obj;
					// }
				else if (value.equalsIgnoreCase("Final Eligibility and Doc")) {
					Log.consoleLog(ifr, "Final Eligibility Staff");
					response = hpcc.mImpOnClickFianlEligibility(ifr, control, event, value);
					obj.put("Response", response);
					Log.consoleLog(ifr, "response from NESL" + response);
					return obj;
				} else if (value.equalsIgnoreCase("InPrinciple Approval")) {
					Log.consoleLog(ifr, "InPrinciple Approval");
					response = pcm.populateInPrincipalApprovalCB(ifr, control, event, value);
					obj.put("Response", response);
					return obj;
					// break;
				} else if (value.equalsIgnoreCase("In Principle Approval")) {
					Log.consoleLog(ifr, "In Principle Approval");
					vpcc.populateInPrincipalApprovalVL(ifr, control, event, value);
					break;
				} else if (value.equalsIgnoreCase("Initial Data")) {
					Log.consoleLog(ifr, "Initial Data");
					if (ifr.getValue("P_LAD_IDC_SELECTFACILITY").toString().equalsIgnoreCase("")) {
						obj.put("Response", pcm.returnErrorThroughExecute(ifr, "Please Select Facility Type!"));
						return obj;
					}
					if (ifr.getValue("P_LAD_IDC_SELECTPURPOSE").toString().equalsIgnoreCase("")) {
						obj.put("Response", pcm.returnErrorThroughExecute(ifr, "Please Select Purpose!"));
						return obj;
					}
					int size = ifr.getDataFromGrid("P_LAD_IDC_FD2").size();
					Log.consoleLog(ifr, "size : " + size);
					if (size == 0) {
						obj.put("Response", pcm.returnErrorThroughExecute(ifr, "Please Select FD Account!"));
						return obj;
					}
					String knockof = LADOBJ.mImpOnClickCheckBRMSRules(ifr);
					Log.consoleLog(ifr, "knockof : " + knockof);
					if (!(knockof.equalsIgnoreCase(""))) {
						Log.consoleLog(ifr, "else condition of knock off  : " + knockof);
						obj.put("Response", knockof);
						return obj;
					}
					String SchemeId = "";
					String query = "select SCHEMEID from LOS_M_PRODUCT_RLOS where ProductCode='OD' and"
							+ " PurposeCode='" + ifr.getValue("P_LAD_IDC_SELECTPURPOSE") + "' and SUBPRODUCTCODE='"
							+ ifr.getValue("P_LAD_IDC_SELECTFACILITY") + "'";
					List<List<String>> results = cf.mExecuteQuery(ifr, query, "Data for Scheme:");
					if (results.size() > 0) {
						SchemeId = results.get(0).get(0);
					}
					LADOBJ.onNextClickInitialDataODAD(ifr, SchemeId);
					String loanAmount = ifr.getTableCellValue("P_LAD_IDC_FD2", 0, 2);
					String tenure = ifr.getTableCellValue("P_LAD_IDC_FD2", 0, 8);
					Log.consoleLog(ifr, "loanAmount : " + loanAmount);
					pcm.mAccSetSliderValue(ifr, control, event, loanAmount + "," + tenure);
				}
				break;

			case "mLoanDisbursementChecker":
				Log.consoleLog(ifr, "WorkStep Name " + value);
				if (value.equalsIgnoreCase("mDisbursementCheckerAPICallCB")) {
					BudgetBkoffCustomCode bkpc = new BudgetBkoffCustomCode();
					Log.consoleLog(ifr, "mDisbursementCheckerAPICallCB::::");
					String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

					String FundTransferquery = "SELECT * FROM LOS_INTEGRATION_CBS_STATUS WHERE TRANSACTION_ID = '"
							+ ProcessInstanceId + "' AND API_NAME ='CBS_FundTransfer'  AND API_STATUS ='SUCCESS'";
					Log.consoleLog(ifr, " CBS_FundTransfer Status query: " + FundTransferquery);
					List<List<String>> FundTransferResult = ifr.getDataFromDB(FundTransferquery);
					if (FundTransferResult.size() > 0) {
						Log.consoleLog(ifr, " CBS_FundTransfer is Done already " + FundTransferResult);
						ifr.setColumnDisable("ALV_BENEFICIARY_DETAILS", "7", true);
						response = "DONE";
						obj.put("Response", response);
						return obj;
					}
					String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
					List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query,
							"Execute query for fetching loan selected ");
					String loan_selected = loanSelected.get(0).get(0);
					Log.consoleLog(ifr, "loan type==>" + loan_selected);
					if (loan_selected.equalsIgnoreCase("Canara Budget")
							|| loan_selected.equalsIgnoreCase("Canara Pension")) {
						response = bkpc.mDisbursementCheckerAPICallCB(ifr, loan_selected);
						Log.consoleLog(ifr, "mDisbursementCheckerAPICallCB response::::" + response);
						obj.put("Response", response);
					}
					return obj;
				}
				break;
			case "setLoanSlider":
				Log.consoleLog(ifr, "inside setLoanSlider case ");
				pcm.mAccSetSliderValue(ifr, control, event, value);
				break;
			case "TenoreElongationCheckbox":
				Log.consoleLog(ifr, "inside TenoreElongationCheckbox case ");
				String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
				String query = ConfProperty.getQueryScript("LoanTypeQuery").replaceAll("#PID#", ProcessInstanceId);
				List<List<String>> loanSelected = cf.mExecuteQuery(ifr, query,
						"Execute query for fetching loan selected ");
				String loan_selected = loanSelected.get(0).get(0);
				Log.consoleLog(ifr, "loan type==>" + loan_selected);
				if (loan_selected.equalsIgnoreCase("Canara Budget")) {
					cbpcc.TenureElongationUpdate(ifr, value);
				} else if (loan_selected.equalsIgnoreCase("Vehicle Loan")) {
					vpcc.TenureElongationUpdate(ifr, value);
				}
				break;
			case "mOnChangeCheckEligibilityCB":
				Log.consoleLog(ifr, "inside mOnChangeCheckEligibilityCB case ");
				pcc.mOnChangeCheckEligibilityCB(ifr, control, event, value);
				break;
			case "setLoanSliderLad":
				Log.consoleLog(ifr, " inside setLoanSliderLad : ");
				pcm.mAccSetSliderValue(ifr, control, event, value + ",0");
				break;
			case "EMI Calculator":
				Log.consoleLog(ifr, "inside EMICALCULATOR case ");
				por.emiCalculatorInstallmentAPI(ifr, control, event, value);
				break;
			case "checkExistingCustumer":
				Log.consoleLog(ifr, "inside the checkExistingCustumer ");
				String respons = pcm.checkExistingCustomerCB(ifr, control, event, value);
				obj.put("Response", respons);
				return obj;
			/*
			 * case "setLoanSliderFinalEligibiltyCB": Log.consoleLog(ifr,
			 * "inside setLoanSlider case "); pcm.mAccSetSliderValueFinalEligibilityCB(ifr,
			 * control, event, value); pcm.setPortalDataFinalEligibiltyCB(ifr, control,
			 * event, value); Log.consoleLog(ifr, "setPortalDataFinalEligibiltyCB called");
			 * cbpcc.mImpOnClickFianlEligibility(ifr, control, event, value); break;
			 */
			case "setLoanSliderCP":
				Log.consoleLog(ifr, "inside setLoanSlider case ");
				plpc.mAccSetSliderValueCP(ifr, control, event, value);
				break;
			/*
			 * case "setLoanSliderFECP": Log.consoleLog(ifr, "inside setLoanSlider case ");
			 * plpc.mAccSetSliderValueFinalEligibilityCP(ifr, control, event, value); break;
			 */
			case "Bureau_Tab_Check":// Added by Sandya for co borrower Eligibility in Bureau click
				Log.consoleLog(ifr, "Inside Bureau_Tab_Check");
				vbcc.OnloadBureaucheck(ifr, control, event, value);
				Log.consoleLog(ifr, "ResponseBureau==>");
				break;
			case "docGenerationClick":
				Log.consoleLog(ifr, "docgeneration Click::");
				String docGeneration = new CommonMethods().mGenerateDoc(ifr, control, event, value);
				break;
			case "saveDataInPartyDetailsGridCoObligant":
				Log.consoleLog(ifr, "Data saving for PartyDetails Grid CoObligant::");
				String ApplicantTypeCOB = "CB";
				String resultCOB = new BudgetPortalCustomCode().CoObligantCBSCheck(ifr, control, event,
						ApplicantTypeCOB);
				Log.consoleLog(ifr, "resultCOB :: " + resultCOB);
				obj.put("Response", resultCOB);
				Log.consoleLog(ifr, "obj :: " + obj);
				return obj;
			case "saveDataInPartyDetailsGridCoObligantCP":
				Log.consoleLog(ifr, "Data saving for PartyDetails Grid CoObligant::");
				String ApplicantTypeCOBCP = "CB";
				String resultCOBCP = plpc.CoObligantCBSCheckCP(ifr, control, event, ApplicantTypeCOBCP);
				Log.consoleLog(ifr, "resultCOBCP :: " + resultCOBCP);
				obj.put("Response", resultCOBCP);
				Log.consoleLog(ifr, "obj :: " + obj);
				return obj;
			case "saveDataInPartyDetailsGridCoObligantVL":
				Log.consoleLog(ifr, "Data saving for PartyDetails Grid CoObligant VL::");
				String resultCB = vpcc.coObligantCBSCheckVL(ifr, control, event, value);
				Log.consoleLog(ifr, "resultCB :: " + resultCB);
				obj.put("Response", resultCB);
				Log.consoleLog(ifr, "obj VL:: " + obj);
				return obj;

			case "viewParentDocument":
				response = ConfProperty.getCommonPropertyValue("ParentDocumentWebAPI")
						.replaceAll("#cabinet#", ifr.getObjGeneralData().getM_strEngineName())
						.replaceAll("#Userdbid#", ifr.getObjGeneralData().getM_strDMSSessionId())
						.replaceAll("#DocumentId#", value);
				break;
			case "genDownloadDocument":
				DocumentDownload dd = new DocumentDownload();
				String[] valueArray = value.split("~");
				String filePath = System.getProperty(RLOS_Constants.USERDIRECTORY) + File.separatorChar + valueArray[0]
						+ File.separatorChar + ifr.getObjGeneralData().getM_strProcessInstanceId();
				Log.consoleLog(ifr, "filePath filePath:: " + filePath);
				dd.docDownloadDocument(ifr, filePath, valueArray[1]);
				break;
			}
			break;
		case "change":
			switch (control) {
//                    case "onchageRequiredLoanAmt":
//                    Log.consoleLog(ifr, "#Inside getLoanAmtOnPageLoad...");
//                    String respo = hrmspcc.getLoanAmtOnPageLoad(ifr);
//                    obj.put("Response", respo);
//				    Log.consoleLog(ifr, "Object of hrmsdata.."+obj);
//                    Log.consoleLog(ifr, "#End of getLoanAmtOnPageLoad...");
//                    return obj;
			case "saveDataInPartDetailGridCoborrower":
				Log.consoleLog(ifr, "Data saving for PartyDetails Grid Coborrower::");
				String ApplicantTypeCOB = "CB";
				String resultCOB = new PortalCustomCode().validateCustoemrAccountDigit(ifr, control, event, value);
				obj.put("Response", resultCOB);
				return obj;
			case "saveDataInPartyDetailGridCoborrowerVL":
				Log.consoleLog(ifr, "Inside saveDataInPartDetailGridCoborrowerVL");
				String resultVL = pcc.validateCustomerAccountDigitVL(ifr, control, event, value);
				Log.consoleLog(ifr, "saveDataInPartDetailGridCoborrowerVL resultVL==>" + resultVL);
				obj.put("Response", resultVL);
				return obj;

			}
			break;
		case "onload":
			Log.consoleLog(ifr, "inside Load ");

			// Added by Ahmed for SanctionDate on 02-01-2024
			try {
				if (ConfProperty.getCommonPropertyValue("DATOFSANCTIONLIVEFLG").equalsIgnoreCase("Y")) {
					Date currentDate = new Date();
					Log.consoleLog(ifr, "593 =====>" + control);
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
					SanctionDate = dateFormat.format(currentDate);
				} else {
					SanctionDate = ConfProperty.getCommonPropertyValue("DATOFSANCTIONDEFAULT");
				}

				Log.consoleLog(ifr, "SanctionDate==>" + SanctionDate);
			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception/SanctionDate");
			}
			// Ended by Ahmed for SanctionDate on 02-01-2024
			Log.consoleLog(ifr, "switch control=====>" + control);
			switch (control) {

			case "autoPopulateAvailOfferData":
				Log.consoleLog(ifr, "#autoPopulateAvailOfferData...Starting");
				String sliderValue = ppcc.autoPopulateAvailOfferData(ifr, value);
				obj.put("Response", sliderValue);
				return obj;
			case "autoPopulateHRMSAvailOfferData":
				String slideValue = "";
				Log.consoleLog(ifr, "#autoPopulateAvailOfferData...Starting");
				try {
					slideValue = hpcc.autoPopulateAvailOfferData(ifr, value);
					Log.consoleLog(ifr, "Inside autoPopulateHRMSAvailOfferData" + slideValue);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				obj.put("Response", slideValue);
				Log.consoleLog(ifr, "autoPopulateHRMSAvailOfferData response " + obj);
				return obj;
				
			case "autoPopulateHRMSAvailOfferDataKavach":
				slideValue = "";
				StaffKavachPortalCustomCode staffKavachPortalCustomCode =new StaffKavachPortalCustomCode();
				Log.consoleLog(ifr, "#autoPopulateAvailOfferData...Starting");
				try {
					slideValue = staffKavachPortalCustomCode.autoPopulateAvailOfferData(ifr, value);
					Log.consoleLog(ifr, "Inside autoPopulateHRMSAvailOfferData" + slideValue);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				obj.put("Response", slideValue);
				Log.consoleLog(ifr, "autoPopulateHRMSAvailOfferData response " + obj);
				return obj;
			case "autoOnLoadAccountDetailsScreen":
				Log.consoleLog(ifr, "autoOnLoadAccountDetailsScreen...Starting");
				hpcc.autoOnLoadAccountDetailsScreen(ifr, value);
				Log.consoleLog(ifr, "autoOnLoadAccountDetailsScreen ....end");
				break;
			case "autopopulateDataHFinalEligibility":
				String neslValue = "";
				Log.consoleLog(ifr, "autopopulateDataHFinalEligibility : ");
				neslValue = hpcc.autopopulateDataHFinalEligibility(ifr, value);
				Log.consoleLog(ifr, "neslValue : " + neslValue);
				obj.put("Response", neslValue);
				Log.consoleLog(ifr, "obj========= : " + obj);
				return obj;
			case "autoOnLoadFinalScreen":
				Log.consoleLog(ifr, "autoOnLoadFinalScreen : ");
				hpcc.autoOnLoadFinalScreen(ifr, value);
				break;

			case "autoPopulateFinalEligibilityData":
				Log.consoleLog(ifr, "autoPopulateFinalEligibilityData : ");
				ppcc.autoPopulateFinalEligibilityData(ifr, value);
				break;
			case "autoPopulateRecieveMoneyData":
				Log.consoleLog(ifr, "autoPopulateRecieveMoneyData");
				ppcc.autoPopulateRecieveMoneyData(ifr, value);
				break;
			case "autoPopulateFinalScreenData":
				Log.consoleLog(ifr, "autoPopulateFinalScreenData");
				ppcc.autoPopulateFinalScreenData(ifr, value);
				break;

			case "saveDataInPartDetailGrid":
				Log.consoleLog(ifr, "Data saving for PartyDetails Grid::");
				String ApplicantType = "B";
				String result = new PortalCustomCode().saveDataInPartyDetailGrid(ifr, ApplicantType,
						pcm.getMobileNumber(ifr));
				obj.put("Response", result);
				return obj;
			case "autoPopulateOccupationDetailsData":
				Log.consoleLog(ifr, "inside OCCUPATION case");
				pcm.mImplApplicationNameRefrenceNum(ifr, "Budget");
				BudgetPortalCustomCode bpc = new BudgetPortalCustomCode();
				String fullName = bpc.mFullNameByApplicantName(ifr, "CB");
				if (!fullName.equalsIgnoreCase("")) {
					ifr.setValue("CoApplicantName_CB", fullName);
				}
				response = new PortalCustomCode().autoPopulateOccupationDetailsData(ifr, control, event, value);
				pcm.getPortalDataLoadCB(ifr, control, event, value);
				cbpcc.populateCoborrowerData(ifr);
				obj.put("Response", response);
				return obj;
			case "autoPopulateLoanDetailsDataCB":
				Log.consoleLog(ifr, "inside LOANDETAILS case");
				pcm.mImplApplicationNameRefrenceNum(ifr, "Budget");
				response = cbpcc.autoPopulateLoanDetailsDataCB(ifr, control, event, value);
				cbpcc.mSetLoanDetailsData(ifr);
				obj.put("Response", response);
				return obj;
			case "autoPopulateLoanDetailsDataHL":
				Log.consoleLog(ifr, "inside LOANDETAILS HL case");
				pcm.mImplApplicationNameRefrenceNum(ifr, "HL");
				response = hlpcc.autoPopulateLoanDetailsDataHL(ifr, control, event, value);
				obj.put("Response", response);
				return obj;
			case "autoPopulateLoanDetailsDataCP":
				Log.consoleLog(ifr, "inside LOANDETAILS Pension case");
				pcm.mImplApplicationNameRefrenceNum(ifr, "Pension");
				String coObFullName = cbpcc.mFullNameByApplicantName(ifr, "CB");
				if (!coObFullName.equalsIgnoreCase("")) {
					ifr.setValue("CoApplicantName_CP", coObFullName);
				}
				response = plpc.autoPopulateLoanDetailsDataCP(ifr, control, event, value);
				ifr.setValue("PIDValue", ifr.getObjGeneralData().getM_strProcessInstanceId());
				plpc.mSavingLoanDetailsDataCP(ifr);
				obj.put("Response", response);
				return obj;
			case "autoPopulateOccupationDetailsDataVL":
				Log.consoleLog(ifr, "inside OCCUPATION case");
				pcm.mImplApplicationNameRefrenceNum(ifr, "VL");
				response = vpcc.autoPopulateOccupationDetailsDataVL(ifr, value);
				vpcc.getPortalDataLoadVL(ifr, control, event, value);
				vpcc.populateCoborrowerDataVL(ifr, control, event, value);
				obj.put("Response", response);
				return obj;

			case "autoPopulateInPrincipleDataCB":
				Log.consoleLog(ifr, "inside InPrincipleDataCB case");
				cbpcc.setValueInHiddenSuperannuationField(ifr);
				cbpcc.autoPupulateBueroConsentFromPortal(ifr);
				pcm.mImplApplicationNameRefrenceNum(ifr, "Budget");
				response = new PortalCustomCode().autoPopulateInPrincipleDataCB(ifr, control, event, value);
				// pcm.getPortalInPricipleApprovalCB(ifr, control, event, value);
				pcm.getPortalInPricipleApprovalCB(ifr, control, event, value);
				obj.put("Response", response);
				return obj;
			case "autoPopulateInPrincipleDataVL":
				Log.consoleLog(ifr, "inside autoPopulateInPrincipleDataVL case");
				vpcc.autoPupulateBueroConsentFromPortal(ifr);
				pcm.mImplApplicationNameRefrenceNum(ifr, "VL");
				response = vpcc.autoPopulateInPrincipleDataVL(ifr, value);
				vpcc.getPortalInPricipleApprovalVL(ifr, control, event, value);
				obj.put("Response", response);
				return obj;
			case "autoPopulateDocUploadDataCB":

				Log.consoleLog(ifr, "inside autoPopulateDocUploadDataCB case");
				new PortalCustomCode().autoPopulateDocUploadDataCB(ifr, control, event, value);
				cbpcc.popluateDocumentsUploadCb(ifr);
				break;
			case "autoPopulateDocUploadDataVL":

				Log.consoleLog(ifr, "inside autoPopulateDocUploadDataVL case");
				new PortalCustomCode().autoPopulateDocUploadDataVL(ifr, control, event, value);
				vpcc.popluateDocumentsUploadVL(ifr, value);
				break;
			case "autoPopulateFinalEligibilityDataCB":
				Log.consoleLog(ifr, "inside FinalEligibilityDataCB case");
				pcm.mImplApplicationNameRefrenceNum(ifr, "Budget");
				response = new PortalCustomCode().autoPopulateFinalEligibilityDataCB(ifr, control, event, value);
				obj.put("Response", response);
				return obj;
			case "autoPopulateRecieveMoneyDataCBudget":
				Log.consoleLog(ifr, "inside RecieveMoneyDataCBudget case");
				pcm.mImplApplicationNameRefrenceNum(ifr, "Budget");
				new PortalCustomCode().autoPopulateRecieveMoneyDataCBudget(ifr, control, event, value);

				break;
			case "autoPopulateFinalScreenDataCBudget":
				Log.consoleLog(ifr, "inside RecieveMoneyDataCBudget case");
				pcm.mImplApplicationNameRefrenceNum(ifr, "Budget");
				new PortalCustomCode().autoPopulateFinalScreenDataCBudget(ifr, control, event, value);
				break;

			case "autoPopulatePortalInfo":
				Log.consoleLog(ifr, "inside autopopulateFirst screen LAD case");
				pcm.mImplApplicationNameRefrenceNum(ifr, "LAD");
				response = LADOBJ.autoPopulateIninitalDataCaptureLAD(ifr, value);
				obj.put("Response", response);
				return obj;
			case "populateDataFinalEligibilityLAD":
				Log.consoleLog(ifr, "inside populateDataFinalEligibilityLAD case");
				String sliderValueLAD = LADOBJ.populateDataFinalEligibilityLAD(ifr, control, event, value);
				obj.put("Response", sliderValueLAD);
				return obj;

			case "resumeForm":
				Log.consoleLog(ifr, "resumeForm case");
				response = pcm.resumeForm(ifr);
				obj.put("Response", response);
				return obj;

			case "autoPopulateRecieveMoneyDataODAD":
				Log.consoleLog(ifr, "inside autoPopulateRecieveMoneyData case");
				pcm.mImplApplicationNameRefrenceNum(ifr, "Pension");
				LADOBJ.autoPopulateRecieveMoneyDataLad(ifr, control, event, value);
				break;
			// pension portal first screen
			case "occupationLoad":
				Log.consoleLog(ifr, "inside occupationLoad case");
				pcm.mImplApplicationNameRefrenceNum(ifr, "Pension");
				// pcc.penOccupation(ifr);
				String coObFullName1 = cbpcc.mFullNameByApplicantName(ifr, "CB");
				if (!coObFullName1.equalsIgnoreCase("")) {
					ifr.setValue("CoApplicantName_CP", coObFullName1);
				}
				plpc.autoPopulatePensionOccupationDetails(ifr, control, event, value);
				plpc.getOccupationBorrowerDataPension(ifr, control, event, value);
				plpc.getOccupationCoBorrowerDataPension(ifr, control, event, value);
				plpc.controlSetForCoBorrowerOccupationDetailsPage(ifr);

				break;
			case "principleLoad":
				Log.consoleLog(ifr, "inside principleLoad case");
				// pcc.penOccupation(ifr);
				Log.consoleLog(ifr, "inside principleLoad case for bureau consent");
				// bpc.autoPupulateBueroConsentFromPortal(ifr);
				plpc.autoPupulateBueroConsentFromPortalPension(ifr);
				response = plpc.autoPopulatePrincipleApprovalDetailsPension(ifr, control, event, value);
				obj.put("Response", response);
				return obj;
			case "autoPopulateFinalScreenDataCPension":
				Log.consoleLog(ifr, "inside autoPopulateFinalScreenDataCPension case");
				plpc.autoPopulateFinalScreenDataCPension(ifr, control, event, value);
				break;
			case "autoPopulateFinalEligibilityDataCP":
				Log.consoleLog(ifr, "inside autoPopulateFinalEligibilityDataCP case");
				// pcc.penOccupation(ifr);
				response = plpc.autoPopulateFinalEligibilityDataCP(ifr, control, event, value);
				obj.put("Response", response);
				return obj;
			// break;
			case "autoPopulateRecieveMoneyDataCPension":
				Log.consoleLog(ifr, "inside autoPopulateRecieveMoneyDataCPension case");
				plpc.autoPopulateRecieveMoneyDataCPension(ifr, control, event, value);
				// pcm.mImplApplicationNameRefrenceNum(ifr, "ODAD");
				break;
			case "autoPopulateDataFinalODAD":
				Log.consoleLog(ifr, "autoPopulateDataFinalODAD case : ");
				LADOBJ.autoPopulateDataFinalODAD(ifr, control, event, value);
				break;
			case "documentUploadPension":
				Log.consoleLog(ifr, "documentUploadPension case : ");
				plpc.setGetPortalStepNamePension(ifr, control, event, value);
				plpc.popluateDocumentsUploadCP(ifr);
				break;
			}
			break;
		case "customValidation":
			switch (control) {
			case "I":

			case "D":
				break;
			}
			break;
		}
		obj.put("Response", response);
		return obj;
	}

	public String returnRespWrapper(IFormReference ifr, String control, String event, String value, String resp,
			String postFlag) {
		Log.consoleLog(ifr, "returnRespWrapper::-> control : " + control + " event : " + event + " value : " + value
				+ " resp : " + resp + " postFlag : " + postFlag);
		JSONObject jobj = new JSONObject();
		jobj.put("Response", resp);
		if (postFlag == null || postFlag.equalsIgnoreCase("false")) {
			jobj.put("Status", (resp == null || resp.isEmpty()) ? "true" : "false");
		} else {
			jobj.put("Status", postFlag);
		}
		Log.consoleLog(ifr, "returnRespWrapper::-> jobj : " + jobj.toString());
		return jobj.toString();
	}

	private void getCurrentStage(IFormReference ifr, String processInstanceId) {
		String queryUpdate = "UPDATE LOS_WIREFERENCE_TABLE SET CURR_STAGE='BackOffice' WHERE WINAME='"
				+ processInstanceId + "'";

		Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
		ifr.saveDataInDB(queryUpdate);
	}

	@Override
	public JSONObject validateSubmittedForm(FormDef fd, IFormReference ifr, String string) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject executeCustomService(FormDef fd, IFormReference ifr, String string, String string1,
			String string2) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject getCustomFilterXML(FormDef fd, IFormReference ifr, String string) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject postHookExportToPDF(IFormReference ifr, File file) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject introduceWorkItemInWorkFlow(IFormReference ifr, HttpServletRequest hsr,
			HttpServletResponse hsr1) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject introduceWorkItemInWorkFlow(IFormReference ifr, HttpServletRequest hsr, HttpServletResponse hsr1,
			WorkdeskModel wm) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject updateDataInWidget(IFormReference ifr, String string) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject validateDocumentConfiguration(IFormReference ifr, String string, String string1, File file,
			Locale locale) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject postHookOnDocumentUpload(IFormReference ifr, String string, String string1, File file, int i) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject postHookOnDocumentOperations(IFormReference ifr, String string, String string1, int i,
			String string2) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject introduceWorkItemInSpecificProcess(IFormReference ifr, String string) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject executeEvent(FormDef fd, IFormReference ifr, String string, String string1) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject onChangeEventServerSide(IFormReference ifr, String string) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject generateHTML(IFormReference ifr, EControl ec) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}

	@Override
	public JSONObject getWidgetNameToBeShown(IFormReference ifr) {
		throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
		// Tools | Templates.
	}
}
