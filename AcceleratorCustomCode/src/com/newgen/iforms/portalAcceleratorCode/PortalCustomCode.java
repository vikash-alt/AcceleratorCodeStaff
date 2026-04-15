package com.newgen.iforms.portalAcceleratorCode;

import com.newgen.dlp.integration.cbs.Advanced360EnquiryData;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.dlp.integration.cbs.Demographic;
import com.newgen.dlp.integration.cbs.EMICalculator;
import com.newgen.dlp.integration.nesl.EsignCommonMethods;
import com.newgen.dlp.integration.nesl.EsignIntegrationChannel;
import com.newgen.dlp.integration.staff.constants.AccelatorStaffConstant;
import com.newgen.iforms.budget.BudgetPortalCustomCode;
import com.newgen.iforms.AccIntegrationCode.ImplIntegrationCode;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;

import com.newgen.omni.wf.util.excp.NGException;
import org.json.simple.JSONObject;
import java.io.IOException;
import java.util.List;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.hrms.EmailAndSms;
import com.newgen.iforms.hrms.HRMSPortalCustomCode;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.budget.BudgetBkoffCustomCode;
import com.newgen.iforms.custom.IFormAPIHandler;
import com.newgen.iforms.budget.BudgetDisbursementScreen;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.lad.LADPortalCustomCode;
import com.newgen.iforms.papl.PAPLPortalCustomCode;
import com.newgen.iforms.pension.PensionLoanPortalCustomCode;
import com.newgen.iforms.vl.VLBkoffcCustomCode;
import com.newgen.iforms.vl.VLPortalCustomCode;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.HashMap;
import org.json.simple.parser.JSONParser;
import java.math.BigDecimal;

public class PortalCustomCode extends ImplIntegrationCode {

	PortalCommonMethods pcm = new PortalCommonMethods();
	CommonFunctionality cf = new CommonFunctionality();
	CommonMethods cm = new CommonMethods();
	BRMSRules pbc = new BRMSRules();
	LADPortalCustomCode lpcc = new LADPortalCustomCode();
	BudgetPortalCustomCode bpcc = new BudgetPortalCustomCode();
	PensionLoanPortalCustomCode plpc = new PensionLoanPortalCustomCode();
	VLPortalCustomCode vlpc = new VLPortalCustomCode();
	VLBkoffcCustomCode vlbcc = new VLBkoffcCustomCode();
	BudgetBkoffCustomCode bbc = new BudgetBkoffCustomCode();
	EmailAndSms objSmsAndEmail = new EmailAndSms();

	// SendEmail CBS_EMAIL = new SendEmail();
	// SendSMS CBS_SMS = new SendSMS();
	public String mAccClickSendOTPRecieveMoneyCB(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccClickSendOTPRecieveMoneyCB");
		String result = bpcc.mAccClickSendOTPRecieveMoneyCB(ifr, control, event, value);
		return result;
	}

	public String mAccValidateOTPRecieveMoneyCB(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccValidateOTPReciveMonCB");
		String response = bpcc.mAccValidateOTPRecieveMoneyCB(ifr, control, event, value);
		return response;
	}

	// mACCCAPICalling
	public String populatePanDetail(final IFormReference ifr, final String Control, final String Event,
			final String value) throws ParseException {
		Log.consoleLog(ifr, "Inside populatePanDetail");

		// return pvs.mPanVerify(ifr);
		return "";
		// return this.pcm.POSTRequest(ifr, Control, Event, value);
	}

	public String getPortalPersonalDetails(final IFormReference ifr, final String Control, final String Event,
			final String value) {
		Log.consoleLog(ifr, "Inside getPortalPersonalDetails");
		return this.pcm.getPortalDataLoad(ifr, Control, Event, value);
	}

	public String getPortalResidenceDetails(final IFormReference ifr, final String Control, final String Event,
			final String value) {
		Log.consoleLog(ifr, "Inside getPortalResidenceDetails");
		return this.pcm.getResidencePortalData(ifr, Control, Event, value);
	}

	public String mACCCAPICalling(final IFormReference ifr, final String Control, final String Event,
			final String value) {
		Log.consoleLog(ifr, "Inside mACCCAPICalling");
		// return this.ads.mAadharVerifier(ifr, value);
		return "";
	}

	public void mACCClickReSendOTP(final IFormReference ifr, final String Control, final String Event,
			final String value) {
		Log.consoleLog(ifr, "Inside mACCClickReSendOTP");
		try {
			this.cf.showMessage(ifr, "PortalLOS_B_KYCResendOTP", "", "OTP Resend Successfully");
		} catch (Exception e) {
			Log.consoleLog(ifr, " Exception in mACCClickReSendOTP" + e);
		}
	}

	public void mACCClickenterIDNumber(final IFormReference ifr, final String control, final String event,
			final String value) {
		Log.consoleLog(ifr, "Inside mACCClickenterIDNumber");
		final String adhar = ifr.getValue("PortalLOS_R_AadharIDN").toString();
		Log.consoleLog(ifr, "Inside mACCClickenterIDNumber adhar" + adhar);
		if (adhar.equalsIgnoreCase("AN")) {
			ifr.setStyle("PortalLOS_T_AKYC_AadharNo", "visible", "true");
			ifr.setStyle("PortalLOS_T_AKYC_VirtualIdNo", "visible", "false");
			ifr.setStyle("PortalLOS_B_KYCSendOTP", "visible", "true");
		} else if (adhar.equalsIgnoreCase("VN")) {
			ifr.setStyle("PortalLOS_T_AKYC_AadharNo", "visible", "false");
			ifr.setStyle("PortalLOS_T_AKYC_VirtualIdNo", "visible", "true");
			ifr.setStyle("PortalLOS_B_KYCSendOTP", "visible", "true");
		}
	}

	public void mACCClicksameCurrentAddFill(final IFormReference ifr, final String control, final String event,
			final String value) {
		Log.consoleLog(ifr, "Inside mACCClicksameCurrentAddFill");
		final String fillCurrentAddCheckBox = ifr.getValue("PortalLOS_CH_ResDet_PA_CheckBox").toString();
		if (fillCurrentAddCheckBox.equalsIgnoreCase("true")) {
			final String Residence_Detail = ConfProperty.getCommonPropertyValue("Residence_Details");
			Log.consoleLog(ifr, Residence_Detail);
			final String[] Portal_value_pa = Residence_Detail.split(",");
			final String Residence_Detail_CA = ConfProperty.getCommonPropertyValue("Residence_Details_CA");
			Log.consoleLog(ifr, Residence_Detail_CA);
			final String[] Portal_value_ca = Residence_Detail_CA.split(",");
			final String[] srt = new String[Portal_value_pa.length];
			for (int i = 0; i < Portal_value_pa.length; ++i) {
				srt[i] = ifr.getValue(Portal_value_pa[i]).toString();
			}
			for (int i = 0; i < Portal_value_ca.length; ++i) {
				Log.consoleLog(ifr, "srt[i]" + srt[i]);
				Log.consoleLog(ifr, "Portal_value_ca[i]" + Portal_value_ca[i]);
				ifr.setValue(Portal_value_ca[i], srt[i]);
			}
		} else if (fillCurrentAddCheckBox.equalsIgnoreCase("false")) {
			final String Residence_Detail_CA2 = ConfProperty.getCommonPropertyValue("Residence_Details_CA");
			Log.consoleLog(ifr, Residence_Detail_CA2);
			final String[] Portal_value_ca2 = Residence_Detail_CA2.split(",");
			for (int j = 0; j < Portal_value_ca2.length; ++j) {
				Log.consoleLog(ifr, "Portal_value_ca[i]" + Portal_value_ca2[j]);
				ifr.setValue(Portal_value_ca2[j], " ");
			}
		}
	}

	public void mACCClickConstructAddress(final IFormReference ifr, final String control, final String event,
			final String value) {
		Log.consoleLog(ifr, "mACCClickConstructAddress");
		final String Residence_Detail = ConfProperty.getCommonPropertyValue("Residence_Details");
		Log.consoleLog(ifr, Residence_Detail);
		final String[] Portal_value_pa = Residence_Detail.split(",");
		final String Residence_Detail_CA = ConfProperty.getCommonPropertyValue("Residence_Details_CA");
		Log.consoleLog(ifr, Residence_Detail_CA);
		final String[] Portal_value_ca = Residence_Detail_CA.split(",");
		final String[] Addper = new String[Portal_value_pa.length];
		final String[] AddCurr = new String[Portal_value_ca.length];
		for (int i = 0; i < Portal_value_pa.length; ++i) {
			Addper[i] = ifr.getValue(Portal_value_pa[i]).toString();
		}
		for (int i = 0; i < Portal_value_ca.length; ++i) {
			Log.consoleLog(ifr, "Portal_value_ca[i]" + Portal_value_ca[i]);
			AddCurr[i] = (String) ifr.getValue(Portal_value_ca[i].toString());
		}
		for (int i = 0; i < Addper.length; ++i) {
			if (Addper[i].equalsIgnoreCase(AddCurr[i])) {
				final String CurrentAddCheckBox = ifr.getValue("PortalLOS_CH_ResDet_PA_CheckBox").toString();
				if (CurrentAddCheckBox.equalsIgnoreCase("true")) {
					ifr.setValue("PortalLOS_CH_ResDet_PA_CheckBox", "true");
				}
			} else {
				ifr.setValue("PortalLOS_CH_ResDet_PA_CheckBox", "false");
			}
		}
	}

	public String mAccValidateOTPRecieveMoneyODAD(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccValidateOTPRecieveMoneyODAD : ");
		return lpcc.mValidateOTPRecieveMoneyODAD(ifr);
	}

	public String mAccportalEMIRepaymentEngine(final IFormReference ifr, final String Control, final String Event,
			final String value) {
		Log.consoleLog(ifr, "Inside portalEMIRepaymentEngine:");
		String loanAmount = "";
		String loanTenure = "";
		String roi = "";
		String loanType = pcm.getLoanType(ifr, Control, Event, value);
		String fieldId = "";
		if (loanType.equalsIgnoreCase("OD AGAINST DEPOSIT")) {
			Log.consoleLog(ifr, "inside if of loantype OD  ...");
			fieldId = ConfProperty.getCommonPropertyValue("PORTALLOANTYPEOD");
			Log.consoleLog(ifr, "fieldId ... " + fieldId);
			String[] str = fieldId.split(",");
			loanAmount = str[0];
			loanTenure = str[1];
			roi = ifr.getValue(str[2]).toString();
			Log.consoleLog(ifr, "ROI : " + roi);

		} else if (loanType.equalsIgnoreCase("PERSONAL LOAN")) {
			Log.consoleLog(ifr, "inside if of loantype PL  ...");
			String fielId = ConfProperty.getCommonPropertyValue("PORTALLOANTYPEPL");
			Log.consoleLog(ifr, "fieldID ... " + fielId);
			String[] str = fielId.split(",");
			loanAmount = str[0];
			loanTenure = str[1];
			roi = "10.8";

		}
		try {
			String amt = ifr.getValue(loanAmount).toString().replaceAll(",", "");
			amt = (amt.equalsIgnoreCase("") ? "0" : amt);
			Log.consoleLog(ifr, "Inside amt : " + amt);
			String tenure = ifr.getValue(loanTenure).toString();
			tenure = (tenure.equalsIgnoreCase("") ? "0" : tenure);
			Log.consoleLog(ifr, "Inside tenure : " + tenure);
			String rate = roi;
			rate = (rate.equalsIgnoreCase("") ? "0" : rate);
			final String params = "LoanAmount=" + amt + "&Tenure=" + tenure + "&RateEMI=" + rate;
			Log.consoleLog(ifr, "Inside params : " + params);
			final String Ip = ConfProperty.getCommonPropertyValue("AppServerIpPort");
			Log.consoleLog(ifr, "Inside Ip : " + Ip);
			final String url = Ip + "//Repayment_Schedules/EMIScheduler.jsp?" + params;
			Log.consoleLog(ifr, "Inside url : " + url);
			final JSONObject message = new JSONObject();
			message.put((Object) "openWindow", (Object) this.cf.openWindow(url));
			return message.toString();
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in portalEMIRepaymentEngine:" + e);
			Log.errorLog(ifr, "Exception in portalEMIRepaymentEngine:" + e);
			return "Some Error Occurred on Code";
		}
	}

	public void mLoadSubProduct(final IFormReference ifr, final String Control, final String Event,
			final String value) {
		cf.mSetClearComboValue(ifr, "PortalLOS_C_LD_SubProduct,PortalLOS_C_LD_Purpose,PortalLOS_C_LD_Variant");
		String product = ifr.getValue("PortalLOS_C_LD_Product").toString();
		Log.consoleLog(ifr, " product : " + product);
		String ProductCode = ConfProperty.getQueryScript("PortalProduct").replaceAll("#product#", product);
		final List<List<String>> getDataList = (List<List<String>>) ifr.getDataFromDB(ProductCode);
		Log.consoleLog(ifr, " getDataList : " + getDataList);
		String ProdCode = "";
		if (getDataList.size() > 0) {
			ProdCode = getDataList.get(0).get(0);
			Log.consoleLog(ifr, "IngetDataListside mACCLoadLoanDetails ProdCode : " + ProdCode);
		}

		String SubProduct = ConfProperty.getQueryScript("PortalSubProduct");
		SubProduct = SubProduct.replaceAll("#productCode#", ProdCode);
		cf.loadComboValues(ifr, "PortalLOS_C_LD_SubProduct", SubProduct, "Sub Product Combo Population:", "L");

	}

	public void mLoadVariant(final IFormReference ifr, final String Control, final String Event, final String value) {
		cf.mSetClearComboValue(ifr, "PortalLOS_C_LD_Variant");
		String product = ifr.getValue("PortalLOS_C_LD_Product").toString();
		Log.consoleLog(ifr, " product : " + product);
		String ProductCode = ConfProperty.getQueryScript("PortalProduct").replaceAll("#product#", product);
		final List<List<String>> getDataList = (List<List<String>>) ifr.getDataFromDB(ProductCode);
		Log.consoleLog(ifr, " getDataList : " + getDataList);
		String ProdCode = "";
		if (getDataList.size() > 0) {
			ProdCode = getDataList.get(0).get(0);
			Log.consoleLog(ifr, "IngetDataListside mACCLoadLoanDetails ProdCode : " + ProdCode);
		}

		String Variant = ConfProperty.getQueryScript("PortalVariant");
		Variant = Variant.replaceAll("#productCode#", ProdCode)
				.replaceAll("#subProductCode#", ifr.getValue("PortalLOS_C_LD_SubProduct").toString())
				.replaceAll("#purposeCode#", ifr.getValue("PortalLOS_C_LD_Purpose").toString());
		;
		cf.loadComboValues(ifr, "PortalLOS_C_LD_Variant", Variant, "Sub Product Combo Population:", "L");
	}

	public void mLoadPurpose(final IFormReference ifr, final String Control, final String Event, final String value) {
		cf.mSetClearComboValue(ifr, "PortalLOS_C_LD_Purpose,PortalLOS_C_LD_Variant");
		String product = ifr.getValue("PortalLOS_C_LD_Product").toString();
		Log.consoleLog(ifr, " product : " + product);
		String ProductCode = ConfProperty.getQueryScript("PortalProduct").replaceAll("#product#", product);
		final List<List<String>> getDataList = (List<List<String>>) ifr.getDataFromDB(ProductCode);
		Log.consoleLog(ifr, " getDataList : " + getDataList);
		String ProdCode = "";
		if (getDataList.size() > 0) {
			ProdCode = getDataList.get(0).get(0);
			Log.consoleLog(ifr, "IngetDataListside mACCLoadLoanDetails ProdCode : " + ProdCode);
		}
		String Purpose = ConfProperty.getQueryScript("PortalPurpose");
		Purpose = Purpose.replaceAll("#productCode#", ProdCode).replaceAll("#subProductCode#",
				ifr.getValue("PortalLOS_C_LD_SubProduct").toString());
		cf.loadComboValues(ifr, "PortalLOS_C_LD_Purpose", Purpose, "Purpose Combo Population:", "L");
	}

	public String portalEmailVarification(final IFormReference ifr, final String control, final String event,
			final String value) {
		Log.consoleLog(ifr, "Inside portalEmailVarification");
		final String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
		Log.consoleLog(ifr, "pid::" + pid);
		try {
			Date date = new Date();
			Log.consoleLog(ifr, "Date : " + date);
			// this.cf.showMessage(ifr, "PortalLOS_B_EmpDet_EmpAdd_SendOTP", "", "code
			// starts here");
			final String emailId = ifr.getValue("PortalLOS_T_EmpDet_EmpAdd_EmailID").toString();
			Log.consoleLog(ifr, "emailId::" + emailId);
			final String mobileNo = "";
			final String partyType = "";
			if (!emailId.equalsIgnoreCase("")) {
				Log.consoleLog(ifr, "Welcome Email");
				String query = "insert into ng_rlos_email_sms (EMAILTO,EMAILSUBJECT,EMAILBODY,EMAILREQUIRED,SMSREQUIRED,SMSCONTENT,PROCESSNAME,WINO,RAISEDDATE) values('"
						+ emailId + "','PortalOtpVarification','PleaseEnterOpt','Y','Y','OTPVerification','"
						+ ifr.getProcessName() + "','" + pid + "','" + date + "') ";
				// final String query = "insert into ng_rlos_email_sms
				// (EMAILTO,EMAILSUBJECT,EMAILBODY,MOBILENUMBER,EMAILREQUIRED,SMSREQUIRED,SMSCONTENT,EMAILCC,DOCINDEX,
				// DOCUMENTNAME,PROCESSNAME,PARKINGBRANCH,WINO,APPLICANTTYPE) values('" +
				// emailId + "','PortalOtpVarification','PleaseEnterOpt','" + mobileNo +
				// "','Y','Y','OTPVerification','',null,null, '" + ifr.getProcessName() +
				// "','','" + pid + "','" + partyType + "')";
				Log.consoleLog(ifr, "query::" + query);
				final int retval1 = ifr.saveDataInDB(query);
				Log.consoleLog(ifr, "retval1::" + retval1);
				// this.cf.showMessage(ifr, "PortalLOS_B_EmpDet_EmpAdd_SendOTP", "", "EmailId
				// Sent Successfully");
				final JSONObject re = new JSONObject();
				re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "PortalLOS_B_EmpDet_EmpAdd_SendOTP",
						"error", "OTP SEnd Successfully"));
				return re.toString();
			} else {
				// this.cf.showMessage(ifr, "PortalLOS_B_EmpDet_EmpAdd_SendOTP", "", "Please
				// Enter The EmailId");
				final JSONObject re = new JSONObject();
				re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "PortalLOS_B_EmpDet_EmpAdd_SendOTP",
						"error", "Please Enter The EmailId"));
				return re.toString();
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in portalEmailVarification :: " + e);
			Log.errorLog(ifr, "Exception in portalEmailVarification :: " + e);
		}
		return "";
	}

	public String varifyEmailOTP(final IFormReference ifr, final String control, final String event, final String value)
			throws NGException, MalformedURLException, IOException {
		Log.consoleLog(ifr, "Inside varifyEmailOTP:");
		final String emailId = ifr.getValue("PortalLOS_T_EmpDet_EmpAdd_EmailID").toString();
		final String OTP = ifr.getValue("PortalLOS_T_EmpDet_EmpAdd_OTPEmailVer").toString();
		Log.consoleLog(ifr, "OTP :" + OTP);
		JSONObject re = new JSONObject();
		if (emailId.equalsIgnoreCase("")) {
			re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "PortalLOS_T_EmpDet_EmpAdd_EmailID",
					"error", "Please Enter emailId"));
			return re.toString();
		}
		if (OTP.equalsIgnoreCase("")) {
			re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "PortalLOS_B_EmpDet_EmpAdd_VerEmail",
					"error", "Please Enter OTP"));
			return re.toString();
		}
		final String query = ConfProperty.getQueryScript("PEmailOTPQuery").replaceAll("#emailId#", emailId);
		Log.consoleLog(ifr, "query:" + query);
		final List<List<String>> OTPD = (List<List<String>>) this.cf.mExecuteQuery(ifr, query, "Query for OTP Check:");
		if (OTPD.size() > 0) {
			final String OTPC = OTPD.get(0).get(0);
			Log.consoleLog(ifr, "OTPDB :" + OTPC);
			if (OTP.equalsIgnoreCase("123456")) {
				Log.consoleLog(ifr, "inside otp verification correct OTP:");
				re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "PortalLOS_T_EmpDet_EmpAdd_EmailID",
						"error", "OTP Verification Correct "));
				return re.toString();
			} else {
				re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "PortalLOS_T_EmpDet_EmpAdd_EmailID",
						"error", "Please Enter OTP Correct OTP"));
				return re.toString();
			}
		}
		return re.toString();
	}

	public void portalFetchCibilScore(final IFormReference ifr, final String control, final String event,
			final String value) {
		ifr.setValue("PortalLOS_T_EmpDet_EmpAdd_CibilScore", "730");
	}

	public String mAccmaskAadhar(final IFormReference ifr, final String control, final String event,
			final String value) {
		String aadharNumber = ifr.getValue("PortalLOS_T_AKYC_AadharNo").toString();
		IFormAPIHandler iFormAPIHandler = (IFormAPIHandler) ifr;
		HttpServletRequest req = iFormAPIHandler.getRequest();
		HttpSession session = req.getSession();
		session.setAttribute("PortalLOS_T_AKYC_AadharNo", aadharNumber);
		JSONObject jsonObject = new JSONObject();
		if (aadharNumber.length() != 12) {
			// Aadhar number should be 12 digits long (including hyphens)
			jsonObject.put((Object) "showMessage",
					(Object) this.cf.showMessage(ifr, "PortalLOS_B_KYCSendOTP", "error", "Invalid Aadhar Number"));
			return jsonObject.toString();
		}
		int length = aadharNumber.length();

		int startIndex = length - 4;
		String maskedPart = aadharNumber.substring(startIndex);
		String maskedAadhar = "xxxxxxxx" + maskedPart;
		ifr.setValue("PortalLOS_T_AKYC_AadharNo", maskedAadhar);
		return " ";
	}
	// fetch address from pincode

	public void mFetchAddressPinCode(IFormReference ifr, String Control, String Event, String value) {
		Log.consoleLog(ifr, "Inside mFetchAddressPinCode: ");

		try {
			String pinCode = "";
			String districtName = "";
			String stateName = "";
			String countryName = "";
			String query = "";

			if (Control.equalsIgnoreCase("PortalLOS_T_ResDet_PA_Pincode")
					|| Control.equalsIgnoreCase("PortalLOS_T_ResDet_CA_Pincode")
					|| Control.equalsIgnoreCase("PortalLOS_T_EmpDet_EmpAdd_Pincode")) {

				pinCode = ifr.getValue(Control).toString();
				query = ConfProperty.getQueryScript("PORTALFETCHADDRESS").replaceAll("#Pincode#", pinCode);
				Log.consoleLog(ifr, "Query: " + query);

				List<List<String>> getDataList = ifr.getDataFromDB(query);
				Log.consoleLog(ifr, "getDataList: " + getDataList);

				if (!getDataList.isEmpty()) {
					districtName = getDataList.get(0).get(0);
					stateName = getDataList.get(0).get(1);
					countryName = getDataList.get(0).get(2);
				} else {
					cf.showMessage(ifr, "PortalLOS_T_ResDet_PA_Pincode", "error", "Please Enter pin Code");
				}

				if (Control.equalsIgnoreCase("PortalLOS_T_ResDet_PA_Pincode")) {
					ifr.setValue("PortalLOS_T_ResDet_PA_City", districtName);
					ifr.setValue("PortalLOS_T_ResDet_PA_State", stateName);
					ifr.setValue("PortalLOS_T_ResDet_PA_Country", countryName);
				} else if (Control.equalsIgnoreCase("PortalLOS_T_ResDet_CA_Pincode")) {
					ifr.setValue("PortalLOS_C_ResDet_CA_City", districtName);
					ifr.setValue("PortalLOS_C_ResDet_CA_State", stateName);
					ifr.setValue("PortalLOS_C_ResDet_CA_Country", countryName);
				} else if (Control.equalsIgnoreCase("PortalLOS_T_EmpDet_EmpAdd_Pincode")) {
					ifr.setValue("PortalLOS_T_EmpDet_EmpAdd_City", districtName);
					ifr.setValue("PortalLOS_T_EmpDet_EmpAdd_State", stateName);
					ifr.setValue("PortalLOS_T_EmpDet_EmpAdd_Country", countryName);
				}
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in mFetchAddressPinCode: " + e);
		}
	}

	public String navigationNxtClick(IFormReference ifr, String control, String event, String value) {

		IFormAPIHandler iFormAPIHandler = (IFormAPIHandler) ifr;
		HttpServletRequest req = iFormAPIHandler.getRequest();
		HttpSession session = req.getSession();
		session.removeAttribute("LOS_PORTAL_T_HIGHER_AMT");

		JSONObject jsonObject = new JSONObject();
		String loanType = pcm.getLoanType(ifr, control, event, value);

		if (loanType.equalsIgnoreCase("PERSONAL LOAN")) {
			String otp = ifr.getValue("PortalLOS_T_AKYC_EnterOTP").toString();
			if (otp.equalsIgnoreCase("")) {
				jsonObject.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "PortalLOS_B_KYCProceed",
						"error", "Please Enter The Valid OTP"));
				return jsonObject.toString();
			}
		}
		jsonObject.put("NavigationNextClick", "true");
		// jsonObject.put("saveWorkitem", "true");
		return jsonObject.toJSONString();
	}

	public void maritalDetaiils(final IFormReference ifr, final String Control, final String Event,
			final String value) {
		Log.consoleLog(ifr, "Inside maritalDetaiils");
		final String str = ifr.getValue("PortalLOS_C_PerDet_MaritalStatus").toString();
		Log.consoleLog(ifr, " str" + str);
		try {
			if (str.equalsIgnoreCase("M")) {
				Log.consoleLog(ifr, " str" + str);
				ifr.setStyle("PortalLOS_T_PerDet_SpouseName", "visible", "true");
			} else {
				ifr.setStyle("PortalLOS_T_PerDet_SpouseName", "visible", "false");
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, " Exception in maritalDetaiils" + e);
		}
	}

	public void editPreviewPersonalDetaiils(final IFormReference ifr, final String Control, final String Event,
			final String value) {
		Log.consoleLog(ifr, "inside editPreviewPersonalDetaiils..");
		try {
			ifr.setStyle("PortalLOS_OD_T_PerDet_Title", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_PerDet_Fname", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_PerDet_Mname", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_PerDet_Lname", "disable", "false");
			ifr.setStyle("PortalLOS_OD_C_PerDet_Gender", "disable", "false");
			ifr.setStyle("PortalLOS_OD_DatePick_PerDet_Dob", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_PerDet_NationalID", "disable", "false");
			ifr.setStyle("PortalLOS_OD_C_PerDet_MaritalStatus", "disable", "false");
			ifr.setStyle("PortalLOS_OD_C_PerDet_Dependents", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_PerDet_FatherName", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_PerDet_MotherName", "disable", "false");
			ifr.setStyle("PortalLOS_OD_C_PerDet_EducationQualification", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_PerDet_Nationality", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_PerDet_EmailAddress", "disable", "false");

		} catch (Exception e) {
			Log.consoleLog(ifr, " Exception in editPreviewPersonalDetaiils " + e);
		}
	}

	public void editPreviewResidenceDetaiils(final IFormReference ifr, final String Control, final String Event,
			final String value) {
		Log.consoleLog(ifr, "inside editPreviewPersonalDetaiils..");
		try {
			ifr.setStyle("PortalLOS_OD_T_PerDet_Title", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_ResDet_PA_AD1", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_ResDet_PA_AD2", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_ResDet_PA_City", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_ResDet_PA_State", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_ResDet_PA_Country", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_ResDet_PA_Pincode", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_ResDet_CA_AD1", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_ResDet_CA_AD2", "disable", "false");
			ifr.setStyle("PortalLOS_OD_C_ResDet_CA_City", "disable", "false");
			ifr.setStyle("PortalLOS_OD_C_ResDet_CA_State", "disable", "false");
			ifr.setStyle("PortalLOS_OD_C_ResDet_CA_Country", "disable", "false");
			ifr.setStyle("PortalLOS_OD_T_ResDet_CA_Pincode", "disable", "false");

		} catch (Exception e) {
			Log.consoleLog(ifr, " Exception in editPreviewPersonalDetaiils " + e);
		}
	}

	public String compareEmploymentMonths(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside compareEmploymentMonths");
		String totalmonths = ifr.getValue("PortalLOS_T_EmpDet_TotalExp").toString();
		Log.consoleLog(ifr, "Total month string : " + totalmonths);
		String currmonths = ifr.getValue("PortalLOS_T_EmpDet_ExpCurrEmployer").toString();
		Log.consoleLog(ifr, "Current month string : " + currmonths);
		int totmon = Integer.parseInt(totalmonths);
		int currmon = Integer.parseInt(currmonths);
		Log.consoleLog(ifr, "Current month integer : " + currmon);
		Log.consoleLog(ifr, "Total month integer : " + totmon);
		JSONObject message = new JSONObject();
		if (totmon < currmon) {
			Log.consoleLog(ifr, "Inside if for message : ");
			message.put("showMessage", cf.showMessage(ifr, "PortalLOS_T_EmpDet_ExpCurrEmployer", "error",
					"Current Months Cannot Be More Than Total Months"));
			Log.consoleLog(ifr, "Message Object  : " + message);
			return message.toString();
		}

		return "";
	}

	public void needHigherAmtChange(IFormReference ifr, String control, String event, String value) {

		String higheramount = ifr.getValue("LOS_PORTAL_T_HIGHER_AMT").toString();
		IFormAPIHandler iFormAPIHandler = (IFormAPIHandler) ifr;
		HttpServletRequest req = iFormAPIHandler.getRequest();
		HttpSession session = req.getSession();
		session.setAttribute("LOS_PORTAL_T_HIGHER_AMT", higheramount);
		Log.consoleLog(ifr, "LOS_PORTAL_T_HIGHER_AMT  : " + higheramount);

	}

	/* Start Implementation Code */
	public void autoPopulateInitialData(IFormReference ifr, String control, String event, String value) {
		try {
			JSONObject Obj = new JSONObject();
			Log.consoleLog(ifr, "inside autoPopulateInitialData  : ");
			String mobileData = "";
			String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String query = "SELECT MOBILENUMBER from LOS_WIREFERENCE_TABLE where WINAME = '" + PID + "' ";
			Log.consoleLog(ifr, "mobileData query : " + query);
			List<List<String>> list = ifr.getDataFromDB(query);
			mobileData = list.get(0).get(0);
			Log.consoleLog(ifr, "mobileData : " + mobileData);

			ifr.setValue("P_LAD_IDC_PERMANENTADDRESS",
					"NO.771, Chabi Ganj, Kashmere Gate, Delhi ,Zip code:  110006 , Country:  India");
			ifr.setValue("P_LAD_IDC_COMMUNICATIONADDRESS",
					"NO. Sheetal Enclave, Lt A K Marg, Gurgaon, Delhi, Haryana, Zip code:  122001, Country:  India");
			ifr.setValue("P_LAD_IDC_EMAIL", "RahulKumar123@gmail.com");
			String Purpose = (String) ifr.getValue("P_LAD_IDC_SELECTPURPOSE");
			if (Purpose.equalsIgnoreCase("Business’")) {
				ifr.setStyle("P_LAD_IDC_UCRNNO", "visible", "true");
				String URCNumber = (String) ifr.getValue("P_LAD_IDC_UCRNNO");
				if (!isValidURCNumber(URCNumber, ifr)) {
					Log.consoleLog(ifr, "inside !!isValidURCNumber(urcNumber):");
					Obj.put("showMessage", cf.showMessage(ifr, "P_LAD_IDC_UCRNNO", "error",
							"Please enter following format ABCDE-FG-22-1234567"));
					ifr.setValue("P_LAD_IDC_UCRNNO", URCNumber);
				}
			}

			String fdNo = "23";

		} catch (Exception e) {
			Log.consoleLog(ifr, "Error occured in autoPopulateInitialData " + e);
		}
	}

	public boolean isValidURCNumber(String urcNumber, IFormReference ifr) {
		String pattern = "^[A-Za-z]{5}-[A-Za-z]{2}-\\d{2}-\\d{7}$";
		Log.consoleLog(ifr, "pattern");
		return urcNumber.matches(pattern);
	}

	public String mAccClickSendOTPRecieveMoney(IFormReference ifr) {
		Log.consoleLog(ifr, "Inside mAccClickSendOTPRecieveMoney");
		JSONObject messagereturn = new JSONObject();
		try {
			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			EsignCommonMethods NESLCM = new EsignCommonMethods();
			String Status = NESLCM.checkNESLWorkflowStatus(ifr);
			Log.consoleLog(ifr, "Status==>" + Status);
			if (!(Status.equalsIgnoreCase(""))) {
				// messagereturn.put("showMessage", cf.showMessage(ifr, "", "error", Status));
				return "error" + "," + Status;
			}
			String reqStatus = "";
			String eSignStatus = "";
			// HRMSPortalCustomCode hpcc=new HRMSPortalCustomCode();
			String Query2 = "SELECT REQ_STATUS,E_SIGN_STATUS FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "Query2===>" + Query2);
			List<List<String>> resForQuery2 = ifr.getDataFromDB(Query2);
			Log.consoleLog(ifr, "resForQuery2===>" + resForQuery2);
			if (!resForQuery2.isEmpty()) {
				reqStatus = resForQuery2.get(0).get(0);
				eSignStatus = resForQuery2.get(0).get(1);

			}
			if ((!reqStatus.isEmpty() && reqStatus.trim().equalsIgnoreCase("Y"))
					&& (!eSignStatus.isEmpty() && eSignStatus.trim().equalsIgnoreCase("Success"))) {
				String mobileno = pcm.getCurrentWiMobileNumber(ifr);
				String currentDate = cf.getCurrentDateTime(ifr);
				String randomnum = pcm.generateRandomNumber(ifr);
				Log.consoleLog(ifr, randomnum);
				String query = ConfProperty.getQueryScript("PCOUNTOTPQuery").replaceAll("#mobileno#", mobileno);
				List<List<String>> mobilecount = cf.mExecuteQuery(ifr, query, "Count For Mobile No:");

				String otpCheck = ConfProperty.getQueryScript("OTPCHECKENABLE");
				if (otpCheck.equalsIgnoreCase("NO")) {
					randomnum = ConfProperty.getQueryScript("OTPDEFAULT");
					Log.consoleLog(ifr, "otpCheck No : " + otpCheck);
				}
				if ((mobilecount.get(0).get(0).equalsIgnoreCase("0"))) {
					query = ConfProperty.getQueryScript("PINSERTOTPQuery").replaceAll("#mobileno#", mobileno)
							.replaceAll("#randomnum#", randomnum).replaceAll("#currentDate#", currentDate);
				} else {
					query = ConfProperty.getQueryScript("PUPDATEOTPQuery").replaceAll("#mobileno#", mobileno)
							.replaceAll("#randomnum#", randomnum).replaceAll("#currentDate#", currentDate);
				}
				ifr.setStyle("H_HRMS_RM_VALIDATE", "disable", "false");
				Log.consoleLog(ifr, "query:" + query);
				int result = ifr.saveDataInDB(query);
				Log.consoleLog(ifr, "result:" + result);
				ifr.setValue("P_PAPL_ENTEROTP", "");
				ifr.setStyle("P_PAPL_RM_Resend", "visible", "false");
				ifr.setStyle("P_PAPL_ENTEROTP", "visible", "true");
				ifr.setStyle("P_PAPL_RM_VALIDATE", "visible", "true");
				ifr.setStyle("Portal_L_Timer_Level", "visible", "true");
				ifr.setStyle("Portal_L_Timer", "visible", "true");
				messagereturn.put("retValue", "optRM");
				return messagereturn.toString();
			} else {
				return "error" + ","
						+ "NeSL is not completed due to technical issue. Please check with Branch for further details.";
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception mAccClickSendOTPRecieveMoney : " + e);
			Log.errorLog(ifr, "Exception mAccClickSendOTPRecieveMoney : " + e);
		}
		return "";
	}

	public String mAccValidateOTPRecieveMoney(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccValidateOTPRecieveMoney : ");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		JSONObject re = new JSONObject();
		String enterOTP = ifr.getValue("P_PAPL_ENTEROTP").toString();
		Log.consoleLog(ifr, "enterOTP ; " + enterOTP);
		if (enterOTP.equalsIgnoreCase("")) {
			re.put("showMessage", cf.showMessage(ifr, "P_PAPL_RM_VALIDATE", "error", "Kindly Enter OTP"));
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
			try {

				String loanDisbMode = pcm.getConstantValue(ifr, "PAPLLOANDISBURSE", "ENABLE");// Y,N
				Log.consoleLog(ifr, "loanDisbMode====>" + loanDisbMode);

				if (loanDisbMode.equalsIgnoreCase("Y")) {
					Log.consoleLog(ifr, "##Loan Disbursement Mode Enabled##");

					String loanDisbCondMode = pcm.getConstantValue(ifr, "PAPLLOANDISBURSE", "CONDITIONALENABLE");// Y,N
					Log.consoleLog(ifr, "loanDisbCondMode====>" + loanDisbCondMode);

					boolean loanDisbFlag = false;
					if (loanDisbCondMode.equalsIgnoreCase("Y")) {
						Log.consoleLog(ifr, "##Loan Disbursement Conditional Mode Enabled##");

						String chkQuery = "SELECT COUNT(1) FROM LOS_STG_LOANDISBURSEMENT " + "WHERE PROCESSINSTANCEID='"
								+ PID + "' AND STATUS='Y'";
						List<List<String>> Result = ifr.getDataFromDB(chkQuery);
						Log.consoleLog(ifr, "#Result===>" + Result.toString());
						String loanDisbCount = "0";
						if (Result.size() > 0) {
							loanDisbCount = Result.get(0).get(0);
						}
						Log.consoleLog(ifr, "#loanDisbCount===>" + loanDisbCount);

						if (Integer.parseInt(loanDisbCount) > 0) {
							loanDisbFlag = true;
						}

					} else {
						Log.consoleLog(ifr, "##Loan Disbursement Conditional Mode Disabled##");
						loanDisbFlag = true;
					}

					Log.consoleLog(ifr, "##loanDisbFlag##" + loanDisbFlag);

					if (loanDisbFlag) {
						PAPLPortalCustomCode ppcc = new PAPLPortalCustomCode();
						Log.consoleLog(ifr, "CBSFinalScreenValidation is calling.....");

						String LoanAccNumber = "";
						String LoanAccountAvl_Query = "SELECT LOAN_ACCOUNT_NO FROM LOS_T_IBPS_LOAN_DETAILS "
								+ "WHERE WINAME='" + PID + "' AND ROWNUM=1";
						List<List<String>> Result = cf.mExecuteQuery(ifr, LoanAccountAvl_Query,
								"LoanAccountAvl_Query:");
						if (Result.size() > 0) {
							LoanAccNumber = Result.get(0).get(0);

						}
						Log.consoleLog(ifr, "LoanAccNumber==>" + LoanAccNumber);

						if (LoanAccNumber.equalsIgnoreCase("")) {

							Log.consoleLog(ifr, "LoanAccNumber is empty..");
							String APIResponse = ppcc.mGetAPIData(ifr);
							if (APIResponse.contains(RLOS_Constants.ERROR)) {
								return pcm.returnError(ifr);
							}

							JSONParser jp = new JSONParser();
							JSONObject obj = (JSONObject) jp.parse(APIResponse);
							String DOB = cf.getJsonValue(obj, "DOB");
							String Age = cf.getJsonValue(obj, "Age");
							String writeOffPresent = cf.getJsonValue(obj, "writeOffPresent");
							String count = cf.getJsonValue(obj, "count");
							String PAPLExist = cf.getJsonValue(obj, "PAPLExist");
							String Classification = cf.getJsonValue(obj, "Classification");
							String totalExp = cf.getJsonValue(obj, "totalExp");
							String grossSalary = cf.getJsonValue(obj, "grossSalary");

							String ProductCode = pcm.mGetProductCode(ifr);
							String decision = ppcc.mCallBRMSKnockOff(ifr, "KNOCKOFF_PAPLTEST",
									Age + "," + PAPLExist + "," + count + "," + grossSalary + "," + Classification + ","
											+ ProductCode + "," + totalExp + "," + DOB + "," + writeOffPresent,
									"totalknockoff_output");
							if (decision.contains(RLOS_Constants.ERROR)) {
								return pcm.returnError(ifr);
							} else if (decision.equalsIgnoreCase("Approve")) {

								Log.consoleLog(ifr, "Knock off passed...");
								// Modified by Ahmed
								String CBSFinalScreenValidation = ppcc.CBSFinalScreenValidation(ifr, PID);
								if (!(CBSFinalScreenValidation.equalsIgnoreCase(RLOS_Constants.SUCCESS))) {
									return pcm.returnError(ifr);
								}

								/*
								 * Email em = new Email(); String emailId = pcm.getCurrentEmailId(ifr, "PAPL",
								 * ""); em.sendEmail(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId(),
								 * emailId, "", "RETAIL", "PAPL", "5");
								 */
								// Added by Ahmed for Doc sending in Base64 content
//                                String bodyParams = "";
//                                String subjectParams = "";
//                                String fileName = "SignedDocument.pdf";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
//                                String fileContent = "";//Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
//                                pcm.triggerCCMAPIs(ifr, PID, "PAPL", "5", bodyParams, subjectParams, fileName, fileContent);
//                            
//                                
								re.put("NavigationNextClick", "true");

							} else {
								JSONObject message = new JSONObject();
								message.put("showMessage", cf.showMessage(ifr, "", "error", "You are Not Eligible!"));
								return message.toString();
							}
						} else {

							Log.consoleLog(ifr, "LoanAccNumber is not empty..");
							String CBSFinalScreenValidation = ppcc.CBSFinalScreenValidation(ifr, PID);
							if (!(CBSFinalScreenValidation.equalsIgnoreCase(RLOS_Constants.SUCCESS))) {
								return pcm.returnError(ifr);
							}
							re.put("NavigationNextClick", "true");
						}
					} else {
						Log.consoleLog(ifr, "#Conditional Emable Flag Set to Y but Workitem Not found on the table.");
						re.put("showMessage",
								cf.showMessage(ifr, "P_PAPL_RM_VALIDATE", "error", "Please try after sometime.."));
						return re.toString();
					}

				} else if (loanDisbMode.equalsIgnoreCase("N")) {
					Log.consoleLog(ifr, "##Loan Disbursement Mode Disabled##");
					re.put("showMessage",
							cf.showMessage(ifr, "P_PAPL_RM_VALIDATE", "error", "Please try after sometime."));
					return re.toString();
				}

			} catch (Exception e) {
				Log.consoleLog(ifr, "Excpetion:" + e);
				Log.errorLog(ifr, "Excpetion:" + e);
			}
		} else {
			re.put("showMessage", cf.showMessage(ifr, "P_PAPL_RM_VALIDATE", "error", "Kindly Enter Correct OTP"));
			return re.toString();
		}
		return re.toString();
	}

	public String mAccSendOTPValidate(IFormReference ifr) {
		JSONObject messagereturn = new JSONObject();
		String status = "";
		try {

			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			EsignCommonMethods NESLCM = new EsignCommonMethods();
			String Status = NESLCM.checkNESLWorkflowStatus(ifr);
			Log.consoleLog(ifr, "Status==>" + Status);
			if (!(Status.equalsIgnoreCase(""))) {
				// messagereturn.put("showMessage", cf.showMessage(ifr, "", "error", Status));
				return "error" + "," + Status;
			}

			String reqStatus = "";
			String eSignStatus = "";
			// HRMSPortalCustomCode hpcc=new HRMSPortalCustomCode();
			String Query2 = "SELECT REQ_STATUS,E_SIGN_STATUS FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='"
					+ processInstanceId + "'";
			Log.consoleLog(ifr, "Query2===>" + Query2);
			List<List<String>> resForQuery2 = ifr.getDataFromDB(Query2);
			Log.consoleLog(ifr, "resForQuery2===>" + resForQuery2);
			if (!resForQuery2.isEmpty()) {
				reqStatus = resForQuery2.get(0).get(0);
				eSignStatus = resForQuery2.get(0).get(1);

			}
			if ((!reqStatus.isEmpty() && reqStatus.trim().equalsIgnoreCase("Y"))
					&& (!eSignStatus.isEmpty() && eSignStatus.trim().equalsIgnoreCase("Success"))) {
				String query2 = "Select STATUS from SLOS_STG_LOANDISBURSEMENT WHERE WINAME = '" + processInstanceId
						+ "'";

				List<List<String>> list = ifr.getDataFromDB(query2);
				Log.consoleLog(ifr, "query " + query2);

				if (!list.isEmpty()) {
					status = list.get(0).get(0);
					if (status.equalsIgnoreCase("N")) {
						return "error, technical glitch";
					}
				}

				String mobileno = pcm.getCurrentWiMobileNumber(ifr);
				String currentDate = cf.getCurrentDateTime(ifr);
				String randomnum = pcm.generateRandomNumber(ifr);
				Log.consoleLog(ifr, randomnum);
				String query = ConfProperty.getQueryScript("PCOUNTOTPQuery").replaceAll("#mobileno#", mobileno);
				List<List<String>> mobilecount = cf.mExecuteQuery(ifr, query, "Count For Mobile No:");

				String otpCheck = ConfProperty.getQueryScript("OTPCHECKENABLE");
				if (otpCheck.equalsIgnoreCase("NO")) {
					randomnum = ConfProperty.getQueryScript("OTPDEFAULT");
					Log.consoleLog(ifr, "otpCheck No : " + otpCheck);
				}
				if ((mobilecount.get(0).get(0).equalsIgnoreCase("0"))) {
					query = ConfProperty.getQueryScript("PINSERTOTPQuery").replaceAll("#mobileno#", mobileno)
							.replaceAll("#randomnum#", randomnum).replaceAll("#currentDate#", currentDate);
				} else {
					query = ConfProperty.getQueryScript("PUPDATEOTPQuery").replaceAll("#mobileno#", mobileno)
							.replaceAll("#randomnum#", randomnum).replaceAll("#currentDate#", currentDate);
				}
				Log.consoleLog(ifr, "query:" + query);
				Log.consoleLog(ifr, query);
				int result = ifr.saveDataInDB(query);
				Log.consoleLog(ifr, "result:" + result);
				String emailStage = "40";
				String emailContent = "";
				String emailSubject = "";
				if (result > 0) {
					if (otpCheck.equalsIgnoreCase("YES")) {

						Log.consoleLog(ifr, "#Calling DLPCommonObjects..");
						String bodyParams = "STAFF" + "#" + randomnum;
						String subjectParams = "";
						String fileName = "";// Added by Ahmed on 03-06-2024 for performing FileContent EMAIL
												// Validations
						String fileContent = "";// Added by Ahmed on 03-06-2024 for performing FileContent EMAIL
												// Validations
						pcm.triggerCCMAPIs(ifr, "", "STAFF", "29", bodyParams, subjectParams, fileName, fileContent);
						// Ended by Ahmed on 10-05-2024 triggering MailContent from
						// DLPCommonObjects=========
						String emailBody = "select body from CAN_MST_EMAIL_HEADERS where stage='" + "29" + "'";
						List<List<String>> emailResp = ifr.getDataFromDB(emailBody);
						if (emailResp.size() > 0) {
							emailContent = emailResp.get(0).get(0);
							emailSubject = "OTP";
							emailContent = "";

						}
						objSmsAndEmail.triggerEmail(ifr, emailStage, emailSubject, emailContent);

					}
				}
				ifr.setStyle("H_HRMS_RM_SENDOTP", "visible", "false");
				ifr.setStyle("H_HRMS_ENTEROTP", "visible", "true");
				ifr.setStyle("H_HRMS_RM_VALIDATE", "visible", "true");
				ifr.setStyle("Portal_L_Timer_Level", "visible", "true");
				ifr.setStyle("Portal_L_Timer", "visible", "true");
				messagereturn.put("retValue", "optRM");
				return messagereturn.toString();

			} else {
				return "error" + ","
						+ "NeSL is not completed due to technical issue. Please check with Branch for further details.";
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception mAccClickSendOTPRecieveMoney : " + e);
			Log.errorLog(ifr, "Exception mAccClickSendOTPRecieveMoney : " + e);
		}
//    	Log.consoleLog(ifr, "Inside mAccSendOTPValidate : ");
//    	ifr.setStyle("H_HRMS_ENTEROTP", "visible", "true");
//		ifr.setStyle("H_HRMS_RM_VALIDATE", "visible", "true");
//		ifr.setStyle("H_HRMS_RM_SENDOTP", "visible", "false");
//		Log.consoleLog(ifr, "Inside mAccSendOTPValidate end : ");
		return "";
	}

//    public void mAccClickResume(IFormReference ifr, String control, String event, String value) {
//    	Log.consoleLog(ifr, "Inside mAccClickResume starts : ");
//    	pcm.resumeForm(ifr);
//    	Log.consoleLog(ifr, "Inside mAccClickResume end : ");
//    	
//    }

	public String mAccValidateOTPHRMSDisbursement(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccValidateOTPHRMSDisb : ");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		JSONObject re = new JSONObject();

		String enterOTP = ifr.getValue("H_HRMS_ENTEROTP").toString();

		Log.consoleLog(ifr, "enterOTP ; " + enterOTP);

		if (enterOTP.equalsIgnoreCase("")) {
			// re.put("showMessage", cf.showMessage(ifr, "H_HRMS_ENTEROTP", "error", "Kindly
			// Enter OTP"));
			return "error" + "," + "Kindly Enter OTP";
		}
		String sendOTP = "";
		String loanType = "";
		String tenure = "";
		String loanamt = "";
		String mobileNumber = pcm.getCurrentWiMobileNumber(ifr);
		String query = ConfProperty.getQueryScript("POTPQuery").replaceAll("#mobileNumber#", mobileNumber);
		Log.consoleLog(ifr, "query:" + query);
		List<List<String>> OTPD = cf.mExecuteQuery(ifr, query, "Query for OTP Check:");
		if (!OTPD.isEmpty()) {
			sendOTP = OTPD.get(0).get(0);
			Log.consoleLog(ifr, "sendOTP==>" + sendOTP);
		}
		String checkForProductType = "SELECT LOAN_TYPE, TENURE_MONTHS,LOAN_AMOUNT FROM SLOS_STAFF_TRN WHERE WINAME='"
				+ PID + "'";
		Log.consoleLog(ifr, "query:" + checkForProductType);
		List<List<String>> cPT = cf.mExecuteQuery(ifr, checkForProductType, "Query for OTP Check:");
		if (!cPT.isEmpty()) {
			loanType = cPT.get(0).get(0);
			tenure = cPT.get(0).get(1);
			loanamt = cPT.get(0).get(2);
			Log.consoleLog(ifr, "loanType==>" + loanType);
		}
		if (enterOTP.equalsIgnoreCase(sendOTP)) {
			ifr.setStyle("navigationNextBtn", "disable", "false");
//			re.put("showMessage", cf.showMessage(ifr, "", "success", "OTP verification successful"));
			Log.consoleLog(ifr, "Inside otp verification method:");
			String status = "";
			ifr.setStyle("H_HRMS_RM_VALIDATE", "disable", "true");
			try {
				// double amountreq = 0.0;
				HRMSPortalCustomCode hpcc = new HRMSPortalCustomCode();
//				hpcc.getLoanAmt(ifr, PID, loanamt, tenure);
//				String queryForCheck = "SELECT AMOUNT_REQUESTED FROM SLOS_STAFF_ELIGIBILITY WHERE WINAME='" + PID + "'";
//				List<List<String>> res = ifr.getDataFromDB(queryForCheck);
//				Log.consoleLog(ifr, "notEligibleForLoan query===>" + queryForCheck);
//				if (!res.isEmpty()) {
//					amountreq = Double.parseDouble(res.get(0).get(0));
//					if (amountreq <= 1000) {
//						return "error" + "," + AccelatorStaffConstant.NOTELIGIBALEFORLOAN;
//					}
//
//				}
				String queryForStatus = "SELECT STATUS FROM SLOS_STG_LOANDISBURSEMENT WHERE WINAME='" + PID + "'";
				List<List<String>> resStatus = ifr.getDataFromDB(queryForStatus);
				Log.consoleLog(ifr, "status check query===>" + queryForStatus);
				if (!resStatus.isEmpty()) {

					status = resStatus.get(0).get(0);
				}
				Log.consoleLog(ifr, "CBSFinalScreenValidation is calling.....");

//				String loanDisbMode = pcm.getConstantValue(ifr, "PAPLLOANDISBURSE", "ENABLE");// Y,N
//				Log.consoleLog(ifr, "loanDisbMode====>" + loanDisbMode);

//				if (loanDisbMode.equalsIgnoreCase("Y")) {
//					Log.consoleLog(ifr, "##Loan Disbursement Mode Enabled##");

//				String loanDisbCondMode = pcm.getConstantValue(ifr, "HRMSLOANDISBURSE", "CONDITIONALENABLE");// Y,N
//				Log.consoleLog(ifr, "loanDisbCondMode====>" + loanDisbCondMode);

//				boolean loanDisbFlag = false;
//				if (loanDisbCondMode.equalsIgnoreCase("Y")) {
//					Log.consoleLog(ifr, "##Loan Disbursement Conditional Mode Enabled##");
//					Date d = new Date();
//					SimpleDateFormat sd1 = new SimpleDateFormat("dd/MM/yyyy");
//					String curDate = sd1.format(d);
//
//					String Query1 = "INSERT INTO SLOS_STG_LOANDISBURSEMENT(WINAME,STATUS,CREATEDDATE,UPDATEDDATE) "
//							+ "VALUES ('" + PID + "','','" + curDate + "','')";
//					Log.consoleLog(ifr, "Query1==>" + Query1);
//					ifr.saveDataInDB(Query1);
//
//					String loanDisbMode = pcm.getConstantValue(ifr, "HRMSLOANDISBURSE", "ENABLE");
//					if (loanDisbMode.equalsIgnoreCase("Y")) {
//						String queryUpdate = "UPDATE SLOS_STG_LOANDISBURSEMENT SET STATUS='Y' WHERE WINAME='" + PID + "'";
//
//						Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
//						ifr.saveDataInDB(queryUpdate);
//						loanDisbFlag = true;
//					}
//					if (loanDisbMode.equalsIgnoreCase("N")) {
//						String queryUpdate = "UPDATE SLOS_STG_LOANDISBURSEMENT SET STATUS='N' WHERE WINAME='" + PID + "'";
//
//						Log.consoleLog(ifr, "queryUpdate : " + queryUpdate);
//						ifr.saveDataInDB(queryUpdate);
//						loanDisbFlag = false;
//					}
////							
//
////						String chkQuery = "SELECT COUNT(1) FROM LOS_STG_LOANDISBURSEMENT " + "WHERE PROCESSINSTANCEID='"
////								+ PID + "' AND STATUS='Y'";
////						List<List<String>> Result = ifr.getDataFromDB(chkQuery);
////						Log.consoleLog(ifr, "#Result===>" + Result.toString());
////						String loanDisbCount = "0";
////						if (Result.size() > 0) {
////							loanDisbCount = Result.get(0).get(0);
////						}
////						Log.consoleLog(ifr, "#loanDisbCount===>" + loanDisbCount);
////
////						if (Integer.parseInt(loanDisbCount) > 0) {
////							loanDisbFlag = true;
////						}
//
//				} else {
//					Log.consoleLog(ifr, "##Loan Disbursement Conditional Mode Disabled##");
//					loanDisbFlag = false;
//				}
//
//				Log.consoleLog(ifr, "##loanDisbFlag##" + loanDisbFlag);

				if (status.trim().equalsIgnoreCase("Y")) {
//					String reqStatus = "";
//					String eSignStatus = "";
					// HRMSPortalCustomCode hpcc=new HRMSPortalCustomCode();
//					String Query2 = "SELECT REQ_STATUS,E_SIGN_STATUS FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='"
//							+ PID + "'";
//					Log.consoleLog(ifr, "Query2===>" + Query2);
//					List<List<String>> resForQuery2 = ifr.getDataFromDB(Query2);
//					Log.consoleLog(ifr, "resForQuery2===>" + resForQuery2);
//					if (!resForQuery2.isEmpty()) {
//						reqStatus = resForQuery2.get(0).get(0);
//						eSignStatus = resForQuery2.get(0).get(1);
//
//					}
//					if ((!reqStatus.isEmpty() && reqStatus.trim().equalsIgnoreCase("Y"))
//							&& (!eSignStatus.isEmpty() && eSignStatus.trim().equalsIgnoreCase("Success"))) {
//					   HRMSPortalCustomCode hrmsPortalCustomCode =new HRMSPortalCustomCode();
//					    String response =hrmsPortalCustomCode.getLoanAmt(ifr, PID, loanamt, tenure,"disbursement");
//						if(response!=null && !response.isEmpty())
//						{
//							String queryforEligiblity = "SELECT AMOUNT_REQUESTED FROM SLOS_STAFF_ELIGIBILITY WHERE PID='"
//									+ PID + "'";
//							List<List<String>> resultforEligiblity = ifr.getDataFromDB(queryforEligiblity);
//							Log.consoleLog(ifr, "resultforEligiblity===>" + resultforEligiblity);
//							if (!resultforEligiblity.isEmpty()) {
//								String result = resultforEligiblity.get(0).get(0);
//								try {
//									double resultElg = Double.parseDouble(result);
//									double responseElg = Double.parseDouble(response);
//									if (responseElg > resultElg) {
//										return "error, eligiblity changed due to new eligiblity";
//									}
//								} catch (NumberFormatException e) {
//									return "error, invalid format for loanamount please check";
//								}
//							}
//						}
						String CBSFinalScreenValidation = hpcc.CBSFinalScreenValidation(ifr, PID, loanType,"Portal");
						String[] cbsFinalScreenValidation = CBSFinalScreenValidation.split(",");
						if (CBSFinalScreenValidation.contains(RLOS_Constants.ERROR.toLowerCase())
								&& cbsFinalScreenValidation.length > 1) {
							return "error" + "," + cbsFinalScreenValidation[1];
						}
						if (CBSFinalScreenValidation.contains(RLOS_Constants.ERROR)
								&& cbsFinalScreenValidation.length == 0) {
							return "error" + "," + "technical glitch try after sometime";
						}
						if (CBSFinalScreenValidation.contains(RLOS_Constants.SUCCESS)) {
							pcm.mAccCompleteWorkItemStatus(ifr);
						}
						String loanAppNo = "";
						String queryForLoanAppNo = "SELECT LOAN_ACCOUNTNO FROM SLOS_TRN_LOANDETAILS WHERE PID='" + PID
								+ "'";
						Log.consoleLog(ifr, "application number uery===>" + queryForLoanAppNo);
						List<List<String>> result = ifr.getDataFromDB(queryForLoanAppNo);
						Log.consoleLog(ifr, "result===>" + result);
						if (!result.isEmpty()) {
							loanAppNo = result.get(0).get(0);

						}

						String bodyParams = "STAFF" + "#" + loanAppNo;
						String subjectParams = "";
						String fileName = "";// Added by Ahmed on 03-06-2024 for performing FileContent EMAIL
												// Validations
						String fileContent = "";// Added by Ahmed on 03-06-2024 for performing FileContent EMAIL
												// Validations
						try {
							pcm.triggerCCMAPIs(ifr, PID, "STAFF", "28", bodyParams, subjectParams, fileName,
									fileContent);
						} catch (Exception e) {
							Log.consoleLog(ifr, "Exception disbursement==>" + e);
						}

						// re.put("NavigationNextClick", "true");
						return "success" + "," + "OTP verification successful";
//					} else {
//						return "error" + ","
//								+ "NeSL is not completed due to technical issue. Please check with Branch for further details.";
//					}
				}
			}

			catch (Exception e) {
				ifr.setStyle("H_HRMS_RM_VALIDATE", "disable", "false");
				Log.consoleLog(ifr, "Excpetion:" + e);
				Log.errorLog(ifr, "Excpetion:" + e);
			}
		} else {
			ifr.setStyle("H_HRMS_RM_VALIDATE", "disable", "false");
			// re.put("showMessage", cf.showMessage(ifr, "H_HRMS_RM_VALIDATE", "error",
			// "Kindly Enter Correct OTP"));
			return "error" + "," + "Kindly Enter Correct OTP";
		}
		return re.toString();
	}

	public String mImpOnClickAvailButton(IFormReference ifr, String control, String event, String value)
			throws ParseException {
		PAPLPortalCustomCode ppcc = new PAPLPortalCustomCode();
		return ppcc.mClickAvailButton(ifr);
	}

//	public String mImpOnClickAvailHRMSButton(IFormReference ifr, String control, String event, String value)
//			throws ParseException {
//		HRMSPortalCustomCode hrmspcc = new HRMSPortalCustomCode();
//		return hrmspcc.mClickAvailHRMSButton(ifr);
//	}

	public void mImplPopulateTandCInPortal(IFormReference ifr, String Control, String Event, String JSdata) {// Working
																												// For
																												// Advanced
																												// List
																												// View
		try {
			Log.consoleLog(ifr, "Inside mImplPopulateTandCInPortal method::");
			int size = ifr.getDataFromGrid("ALV_TERMS_AND_CONDITIONS").size();
			if (size == 0) {
				Log.consoleLog(ifr, "Inside Terms and Condition: ");
				String Coll_ProductType1 = ifr.getValue("Coll_ProductType1").toString();
				Log.consoleLog(ifr, "Inside mImplPopulateTandCInPortal Coll_ProductType1::" + Coll_ProductType1);
				String schemeid = ConfProperty.getQueryScript("mgetschemeIDPortal").replaceAll("#Coll_ProductType1#",
						Coll_ProductType1);
				List<List<String>> SchemeID = cf.mExecuteQuery(ifr, schemeid, "Scheme");
				if (!SchemeID.isEmpty()) {
					String Scheme = SchemeID.get(0).get(0);
					String query = ConfProperty.getQueryScript("mAccpopulateTandC").replaceAll("#Scheme#", Scheme);
					cf.addToTable(ifr, "ALV_TERMS_AND_CONDITIONS1", query, "Terms and Conditions");
					ifr.setColumnDisable("ALV_TERMS_AND_CONDITIONS1", "0", true);
				}

			} else {
				Log.consoleLog(ifr, "Rows are already there in the grid ");

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  Terms and Condition: " + e);
		}
	}

	public String saveDataInPartyDetailGrid(IFormReference ifr, String ApplicantType, String mobileNumber) {
		String result = "";
		try {
			Log.consoleLog(ifr, "Inside saveDataInPartyDetailGrid::");
			CustomerAccountSummary cas = new CustomerAccountSummary();
			String activityName = ifr.getActivityName();
			Log.consoleLog(ifr, "activityName::" + activityName);
			HashMap<String, String> customerdetails = new HashMap<>();
			customerdetails.put("ApplicantType", ApplicantType);
			if (ApplicantType.equalsIgnoreCase("CB")) {
				Log.consoleLog(ifr, "saveDataInPartyDetailGrid::Param Value::" + mobileNumber);
				String[] value = mobileNumber.split("~");
				customerdetails.put("mobileNumber", value[0]);
				customerdetails.put("CustomerId", value[1]);
			} else {
				String strCusterid = pcm.getWICustomerID(ifr);
				customerdetails.put("CustomerId", strCusterid);
				customerdetails.put("mobileNumber", mobileNumber);
			}
			result = cas.updateCustomerAccountSummary(ifr, customerdetails);
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception saveDataInPartyDetailGrid::" + e);
			Log.errorLog(ifr, "Exception saveDataInPartyDetailGrid::" + e);
		}
		return result;
	}

	public String autoPopulateOccupationDetailsData(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside autoPopulateOccupationDetailsData  : ");
		String currentStep = pcm.setGetPortalStepName(ifr, value);
		String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		try {
//            String ProfileCB = "Salaried";
//            Log.consoleLog(ifr, "ProfileCB==>" + ProfileCB);
//            ifr.setValue("P_CB_OD_Profile", ProfileCB);
			// String queryResadd = "select
			// a.Line1,a.Line2,a.Line3,c.EMAILID,a.state,a.country,a.pincode,c.MobileNo from
			// LOS_NL_Address a inner join LOS_NL_BASIC_INFO b on a.f_key=b.f_key inner join
			// LOS_L_BASIC_INFO_I c on b.f_key=c.f_key where b.PID='" + ProcessInsanceId +
			// "' and a.AddressType='CA' and b.ApplicantType='B'";
			// String queryPeradd = "select a.Line1,a.Line2,a.Line3,a.pincode from
			// LOS_NL_Address a inner join LOS_NL_BASIC_INFO b on a.f_key=b.f_key where
			// b.PID='" + ProcessInsanceId + "' and a.AddressType='P' and
			// b.ApplicantType='B'";
			String queryResadd = ConfProperty.getQueryScript("getResAddrQuery").replaceAll("#ProcessInsanceId#",
					ProcessInsanceId);
			String queryPeradd = ConfProperty.getQueryScript("getPerAddrQuery").replaceAll("#ProcessInsanceId#",
					ProcessInsanceId);
			Log.consoleLog(ifr, "PortalCustomCode:autoPopulateOccupationDetailsData-> CA:" + queryResadd);
			Log.consoleLog(ifr, "PortalCustomCode:autoPopulateOccupationDetailsData-> PA:" + queryPeradd);

			List<List<String>> resultResadd = ifr.getDataFromDB(queryResadd);
			List<List<String>> resultPeradd = ifr.getDataFromDB(queryPeradd);

			if (resultResadd.size() > 0) {
				String comAddressLine1 = resultResadd.get(0).get(0);
				String comAddressLine2 = resultResadd.get(0).get(1);
				String comAddressLine3 = resultResadd.get(0).get(2);
				String EMAILID = resultResadd.get(0).get(3);
				String state = resultResadd.get(0).get(4);
				String country = resultResadd.get(0).get(5);
				String pincode = resultResadd.get(0).get(6);
				String strmobile = resultResadd.get(0).get(7);
				ifr.setValue("P_CB_OD_EMAILID", EMAILID);
				ifr.setValue("P_CB_OCCINFO_MOBILENO", strmobile);
				ifr.setValue("P_CB_OD_COMMUNICATION_ADDRESS", comAddressLine1 + " , " + comAddressLine2 + " , "
						+ comAddressLine3 + "," + state + "," + country + "," + pincode);
			}
			if (resultPeradd.size() > 0) {
				String addressLine1 = resultPeradd.get(0).get(0);
				String addressLine2 = resultPeradd.get(0).get(1);
				String addressLine3 = resultPeradd.get(0).get(2);
				String pincode = resultPeradd.get(0).get(3);
				ifr.setValue("P_CB_OD_PERMANENT_ADDRESS",
						addressLine1 + " , " + addressLine2 + " , " + addressLine3 + "," + pincode);
			}
			ifr.setValue("P_CB_OD_TypeOfOccupation", "");
			String FirstNameCB = "", MiddleNameCB = "", LastNameCB = "";
			// String FullNameCBQuery = "SELECT TITLE||'.
			// '||regexp_replace(a.FIRSTNAME,'{}'),regexp_replace(a.MIDDLENAME,'{}'),regexp_replace(a.LASTNAME,'{}')
			// FROM LOS_L_BASIC_INFO_I a INNER JOIN LOS_NL_BASIC_INFO b ON a.F_KEY = b.F_KEY
			// WHERE b.CUSTOMERFLAG = 'Y' AND b.PID ='" + ProcessInsanceId + "' and
			// b.ApplicantType='B'";
			String FullNameCBQuery = ConfProperty.getQueryScript("getFullNameCBQuery").replaceAll("#ProcessInsanceId#",
					ProcessInsanceId);
			Log.consoleLog(ifr, "mobileData query : " + FullNameCBQuery);
			List<List<String>> FullNameCBList = ifr.getDataFromDB(FullNameCBQuery);
			if (!FullNameCBList.isEmpty()) {
				FirstNameCB = FullNameCBList.get(0).get(0);
				MiddleNameCB = FullNameCBList.get(0).get(1);
				LastNameCB = FullNameCBList.get(0).get(2);
				if (!MiddleNameCB.isEmpty()) {
					MiddleNameCB = MiddleNameCB.replace("null", "");
				}
				if (!LastNameCB.isEmpty()) {
					LastNameCB = LastNameCB.replace("null", "");
				}
				String Fullname = FirstNameCB + " " + MiddleNameCB + " " + LastNameCB;
				ifr.setValue("P_CB_H_CUSTOMERNAME1", Fullname.replace("  ", " "));
			}

			// ifr.setStyle("P_CB_OD_Profile", "disable","true");
			// Code for Disbursal Account
			String pid = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String CustomerId = pcm.getCustomerIDCB(ifr, "B");
			Log.consoleLog(ifr, "Disbursal Account ::CustomerId==>" + CustomerId);
			Advanced360EnquiryData objCbs360 = new Advanced360EnquiryData();
			String response360 = objCbs360.executeAdvanced360Inquiry(ifr, pid, CustomerId, "Canara Budget");
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
						Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:AccountId::" + AccountId);
						String BranchCode = InputStringResponseJSON.get("BranchCode").toString();
						String strAcctOpen = InputStringResponseJSON.get("DatAcctOpen").toString();
						String strAcctbal = InputStringResponseJSON.get("AcyAmount").toString();
						String stractprdcode = InputStringResponseJSON.get("ProductCode").toString();// ProductCode;
						Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:BranchCode::" + BranchCode);
						ifr.addItemInCombo("P_CB_OD_SalaryAccount", AccountId, AccountId + "-" + BranchCode + "-"
								+ strAcctOpen + "-" + strAcctbal + "-" + stractprdcode);
						// ifr.addItemInCombo("Occupation_Details_CB_combo12", AccountId);
					}
				}
			}
			ifr.setValue("P_CB_OD_TypeOfOccupation", "");
			Log.consoleLog(ifr, "autoPopulateOccupationDetailsData:setValue for occupation::");
			// Commented by Aravindh On 27/05/24
//            String responseMisStatus = bpcc.CheckMisStatusBudget(ifr);// MIS Status Check
//            if (responseMisStatus.contains(RLOS_Constants.ERROR)) {
//                Log.consoleLog(ifr, "inside error condition responseMisStatus Budget");
//                return pcm.returnError(ifr);
//            }

			Demographic objDemographic = new Demographic();
			String GetDemoGraphicData = objDemographic.getDemographic(ifr, pid, CustomerId);
			Log.consoleLog(ifr, "GetDemoGraphicData==>" + GetDemoGraphicData);
			if (GetDemoGraphicData.contains(RLOS_Constants.ERROR)) {
				Log.consoleLog(ifr, "inside error condition Demographic Budget");
				return pcm.returnErrorAPIThroughExecute(ifr);
			} else {
				Log.consoleLog(ifr, "inside non-error condition Demographic Budget");
				JSONParser jsonparser = new JSONParser();
				JSONObject obj = (JSONObject) jsonparser.parse(GetDemoGraphicData);
				Log.consoleLog(ifr, obj.toString());
				String DateOfCustOpen = obj.get("DateOfCustOpen").toString();
				Log.consoleLog(ifr, "DateOfCustOpen : " + DateOfCustOpen);
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception:" + e);
			Log.errorLog(ifr, "Exception:" + e);
			return pcm.returnError(ifr);
		}
		return currentStep;
	}

	public String mOnChangeCheckEligibilityCB(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside the mOnChangeCheckEligibilityCB : ");
		JSONObject message = new JSONObject();
		try {
			String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			Log.consoleLog(ifr, "inside eligibility brms:::");
			String loanROI = pcm.mGetROICB(ifr);
			Log.consoleLog(ifr, "roi : " + loanROI);
			String obligation = null;
			String obligation_Query = ConfProperty.getQueryScript("getTotalAmountQuery")
					.replaceAll("#ProcessInstanceId#", ProcessInsanceId);
			;
			Log.consoleLog(ifr, "obligation_Query===>" + obligation_Query);
			List<List<String>> list = cf.mExecuteQuery(ifr, obligation_Query, "obligation_Query:");
			if (list.size() > 0) {
				obligation = list.get(0).get(0);
			}
			String obligationInput = obligation.equalsIgnoreCase("") ? "0" : obligation;

			String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
			Log.consoleLog(ifr, "schemeID:" + schemeID);

			String Prodcapping = null;
			String ProdCapping_Query = ConfProperty.getQueryScript("GetMaxLoanAmount").replaceAll("#schemeID#",
					schemeID);
			List<List<String>> ProdcappingList = cf.mExecuteQuery(ifr, ProdCapping_Query, "ProdCapping_Query:");
			if (ProdcappingList.size() > 0) {
				Prodcapping = ProdcappingList.get(0).get(0);
			}
			String gross = null;
			String deduction = null;
			String gross_Query = ConfProperty.getQueryScript("GetIncomeDataOccupationInfoCB").replaceAll("#PID#",
					ProcessInsanceId);
			List<List<String>> grossList = cf.mExecuteQuery(ifr, gross_Query, "gross_Query:");
			if (grossList.size() > 0) {
				gross = grossList.get(0).get(0);
				deduction = grossList.get(0).get(2);
			}
			Log.consoleLog(ifr, "gross : " + gross);
			Log.consoleLog(ifr, "deductigrosson : " + deduction);
			String reqAmount = "";
			String loanTenure = "";
			String proposedFacilityQuery = ConfProperty.getQueryScript("PortalInprincipleSliderData")
					.replaceAll("#PID#", ProcessInsanceId);
			List<List<String>> list4 = cf.mExecuteQuery(ifr, proposedFacilityQuery, "proposedFacilityQuery:");
			if (list4.size() > 0) {
				reqAmount = list4.get(0).get(1);
				loanTenure = list4.get(0).get(2);
			}
			Log.consoleLog(ifr, "propoInfo reqAmount: " + reqAmount);
			HashMap hm = new HashMap();
			hm.put("cibiloblig", obligationInput);
			Log.consoleLog(ifr, "cibiloblig===>" + obligationInput);
			hm.put("grosssalary", String.valueOf(gross));
			hm.put("deductionmonth", String.valueOf(deduction));
			hm.put("tenure", String.valueOf(loanTenure));
			hm.put("roi", String.valueOf(loanROI));
			hm.put("loancap", Prodcapping);
			hm.put("reqAmount", reqAmount);
			String finaleligibilityBG = bbc.getAmountForInprincipleDataSaveBO(ifr, hm);
			Log.consoleLog(ifr, "finaleligibilityBG===>" + finaleligibilityBG);
			if (finaleligibilityBG.contains(RLOS_Constants.ERROR)) {
				return RLOS_Constants.ERROR;
			}
			String Query = "SELECT COUNT(*) FROM LOS_L_FINAL_ELIGIBILITY " + "WHERE PID='" + ProcessInsanceId + "'";
			Log.consoleLog(ifr, "Query===>" + Query);
			List Result = ifr.getDataFromDB(Query);
			String Count = Result.toString().replace("[", "").replace("]", "");
			if (Count.equalsIgnoreCase("")) {
				Count = "0";
			}
			if (Integer.parseInt(Count) == 0) {
				// String Query2 = "INSERT INTO LOS_L_FINAL_ELIGIBILITY (PID) VALUES ('" +
				// ProcessInstanceId + "')";
				String Query2 = ConfProperty.getQueryScript("insertQueryforPIDinFinalEligibility")
						.replaceAll("#ProcessInstanceId#", ProcessInsanceId);
				Log.consoleLog(ifr, "Query1===>" + Query2);
				ifr.saveDataInDB(Query2);
			}
			// String Query2 = "UPDATE LOS_L_FINAL_ELIGIBILITY "
			// + "SET LOAN_AMOUNT='" + finalelig + "' WHERE PID='" + ProcessInstanceId +
			// "'";
			String Query2 = ConfProperty.getQueryScript("updateQueryforLoanamtinFinalEligibility")
					.replaceAll("#finalelig#", finaleligibilityBG).replaceAll("#ProcessInstanceId#", ProcessInsanceId);
			Log.consoleLog(ifr, "Query2===>" + Query2);
			ifr.saveDataInDB(Query2);
			// String Query3 = "UPDATE LOS_L_FINAL_ELIGIBILITY "
			// + "SET IN_PRINCIPLE_AMOUNT='" + finalelig + "' WHERE PID='" +
			// ProcessInstanceId + "'";
			String Query3 = ConfProperty.getQueryScript("updateQueryforPrincipleamtinFinalEligibility")
					.replaceAll("#finalelig#", finaleligibilityBG).replaceAll("#ProcessInstanceId#", ProcessInsanceId);
			Log.consoleLog(ifr, "Query3===>" + Query3);
			ifr.saveDataInDB(Query3);
			String finalEligibleAmount = "";
			String productCode = pcm.mGetProductCode(ifr);
			String finalAmountInParams = productCode + "," + finaleligibilityBG;
			finalEligibleAmount = bpcc.checkFinalEligibility(ifr, "ELIGIBILITY_CB", finalAmountInParams,
					"validcheck1op");

			if (finalEligibleAmount.equalsIgnoreCase("Eligible")) {
				Log.consoleLog(ifr, "eligibility Passed Successfully:::");
			}

			String loanAmount = "-" + finaleligibilityBG;
			BigDecimal rate = new BigDecimal(loanROI);
			int tenure = Integer.parseInt(loanTenure);
			BigDecimal emicalc = pcm.calculateEMIPMT(ifr, loanAmount, rate, tenure);
			String emi = emicalc.toString();
			Log.consoleLog(ifr, "emi : " + emi);
			ifr.setValue("P_CB_PA_EMI_INPRNCPL", emi);
		} catch (Exception e) {
			Log.consoleLog(ifr, "error inside the  mOnChangeCheckEligibilityCB: " + e);
		}
		return "";
	}

	public void autoPopulateDocUploadDataVL(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside  autoPopulateDocUploadDataVL");
		pcm.setGetPortalStepName(ifr, value);
	}

	public String autoPopulateFinalEligibilityDataCB(IFormReference ifr, String control, String event, String value) {
		String sliderValue = "";
		try {
			Log.consoleLog(ifr, "inside  autoPopulateFinalEligibilityDataCB");

			pcm.setGetPortalStepName(ifr, value);

			String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

			Log.consoleLog(ifr, "PID .." + ProcessInsanceId);

			String Query1 = ConfProperty.getQueryScript("PortalFindFinalEligibiltyCB").replaceAll("#PID#",
					ProcessInsanceId);

			Log.consoleLog(ifr, "Query1.." + Query1);

			List<List<String>> result = ifr.getDataFromDB(Query1);

			String tenure1 = "";

			String loanAmount1 = "";

			if (result.size() > 0) {

				loanAmount1 = result.get(0).get(0);

				Log.consoleLog(ifr, "Loanamount .." + loanAmount1);

				tenure1 = result.get(0).get(1);

				Log.consoleLog(ifr, "tenure1 .." + tenure1);

				String Checkbox1 = result.get(0).get(2);

				String Checkbox2 = result.get(0).get(3);

				sliderValue = loanAmount1 + "," + tenure1;

				ifr.setValue("FinalEligibility_CB_checkbox2", Checkbox1);

				ifr.setValue("FinalEligibility_CB_checkbox3", Checkbox2);

				sliderValue = loanAmount1 + "," + tenure1;

			}

			// String query = "SELECT MOBILENUMBER from LOS_WIREFERENCE_TABLE where WINAME =
			// '" + ProcessInsanceId + "' ";
			String query = ConfProperty.getQueryScript("getMobNumFromWIREFTable").replaceAll("#ProcessInsanceId#",
					ProcessInsanceId);
			Log.consoleLog(ifr, "MOBILENUMBER Query : " + query);
			List list = ifr.getDataFromDB(query);
			String MOBILENUMBER = list.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "MOBILENUMBER : " + MOBILENUMBER);

			Log.consoleLog(ifr, "inside autoPopulateFinalEligibilityDataCB  : ");
			// String rateOfIntrest = (String) ifr.getValue("P_CB_PA_RATE_OF_INTEREST");
			// ifr.setValue("P_CB_FE_RATE_OF_INTEREST", rateOfIntrest);
			// Log.consoleLog(ifr, "rateOfIntrest : " + rateOfIntrest);
			List<List<String>> loanAmount = null;
			// String data = "select RECOMMEND_LOAN_AMOUNT from LOS_Lin_FINAL_ELIGIBILITY
			// where PID ='" + ProcessInsanceId + "'";
			String data = ConfProperty.getQueryScript("getRecLoanAmtQuery").replaceAll("#ProcessInsanceId#",
					ProcessInsanceId);
			Log.consoleLog(ifr, "loanAmount query : " + data);
			loanAmount = ifr.getDataFromDB(data);
			Log.consoleLog(ifr, "loanAmount : " + loanAmount);
			String amount = loanAmount.get(0).get(0);
			ifr.setValue("P_CB_FE_LOAN_AMOUNT", amount);
			Log.consoleLog(ifr, "amount : " + amount);
			List<List<String>> loanTenure = null;
			String schemeID = pcm.mGetSchemeID(ifr, ifr.getObjGeneralData().getM_strProcessInstanceId());
			Log.consoleLog(ifr, "schemeID:" + schemeID);
			// String tenureData = "select maxtenure from LOS_M_LoanInfo where scheme_id='"
			// + schemeID + "'";
			/*
			 * String tenureData =
			 * ConfProperty.getQueryScript("getmaxTenureData").replaceAll("#schemeID#",
			 * schemeID); Log.consoleLog(ifr, "tenureData query : " + tenureData);
			 * loanTenure = ifr.getDataFromDB(tenureData); Log.consoleLog(ifr,
			 * "loanTenure : " + loanTenure); String tenure = loanTenure.get(0).get(0);
			 */
			ifr.setValue("P_CB_FE_TENURE", tenure1);
			Log.consoleLog(ifr, "tenure : " + tenure1);
			/*
			 * List<List<String>> loanROI = null; String roiID = pcm.mGetRoiID(ifr);
			 * Log.consoleLog(ifr, "roiID:" + roiID); //String roiData =
			 * "select totalroi from los_m_roi where roiid='" + roiID + "'"; String roiData
			 * = ConfProperty.getQueryScript("getTotalROIData").replaceAll("#roiID#",
			 * roiID); Log.consoleLog(ifr, "roiData query : " + roiData); loanROI =
			 * ifr.getDataFromDB(roiData); Log.consoleLog(ifr, "loanROI : " + loanROI);
			 */
			String roi = pcm.mGetROICB(ifr);
			Log.consoleLog(ifr, "roi : " + roi);
			ifr.setValue("P_CB_FE_RATE_OF_INTEREST", roi + "%");
			String processingFee = pcm.getProcessingFee(ifr, schemeID, amount, " and A.FeeCode='CHR30'");
			String CICCharges = pcm.getProcessingFee(ifr, schemeID, amount, " and A.FeeCode in ('CHR31','CHR32')");
			String esignAndStamp = pcm.getProcessingFee(ifr, schemeID, amount, " and A.FeeCode in ('CHR34','CHR35')");
			Log.consoleLog(ifr,
					"schemeID::::" + schemeID + "CICCharges::::" + CICCharges + "esignAndStamp::::" + esignAndStamp);
			// String processingFee = pcm.getCalulatedProcessingFeeCB(ifr, amount);
			Log.consoleLog(ifr, "processingFee : " + processingFee);
			long processingFeeRound = Math.round(Double.parseDouble(processingFee));
			// long CICChargesRound= Math.round(Double.parseDouble(CICCharges));
			// long esignAndStampRound= Math.round(Double.parseDouble(esignAndStamp));

			ifr.setValue("P_CB_FE_PROCEESING_FEES", ("₹ " + String.valueOf(processingFeeRound)));
			ifr.setStyle("P_CB_FE_PROCEESING_FEES", "readonly", "true");
			ifr.setValue("P_CB_FE_CIC_FEES", ("₹ " + String.valueOf(CICCharges)));
			ifr.setStyle("P_CB_FE_CIC_FEES", "readonly", "true");
			ifr.setValue("P_CB_FE_E_SIGN_STAMP", ("₹ " + String.valueOf(esignAndStamp)));
			ifr.setStyle("P_CB_FE_E_SIGN_STAMP", "readonly", "true");
			// ifr.setValue("P_CB_FE_FINTECT_CHARGES", ("₹ " +
			// String.valueOf(esignAndStamp)));
			ifr.setStyle("P_CB_FE_FINTECT_CHARGES", "readonly", "true");

			// String processingFee = pcm.getCalulatedProcessingFeeCB(ifr, amount);
			// Log.consoleLog(ifr, "processingFee : " + processingFee);
			// long processingFeeRound = Math.round(Double.parseDouble(processingFee));
			ifr.setValue("P_CB_FE_PROCEESING_FEES", ("₹ " + String.valueOf(processingFeeRound)));
			ifr.setStyle("P_CB_FE_PROCEESING_FEES", "readonly", "true");
//            String[] emiDetails = pcm.getCalulatedLoanDetails(ifr, amount, tenure, roi);
//            String emi = emiDetails[1];
//            long emiRupeeRounded = Math.round(Double.parseDouble(emi));
//            ifr.setValue("P_CB_FE_EMI", ("₹ " + String.valueOf(emiRupeeRounded)));
			// Redukesh
			Log.consoleLog(ifr, " before calling emicalculator api the values are  amount" + amount + " tenure "
					+ tenure1 + " roi " + roi);
			// EMICalculator API = new EMICalculator();
			// String EMIAmoumt = API.getEmiCalculatorInstallment(ifr, ProcessInsanceId,
			// amount, tenure, roi);

			String FrameSection = "FinalOffer";
			EMICalculator e = new EMICalculator();
			String EMIAmount = e.getEmiCalculatorInstallment(ifr, ProcessInsanceId, amount, tenure1, roi, FrameSection);

			Log.consoleLog(ifr, "after calling emicalculator api EMIAmoumt : " + EMIAmount);
			ifr.setValue("P_CB_FE_EMI", ("₹ " + EMIAmount));
			ifr.setStyle("P_CB_FE_EMI", "readonly", "true");
			Double OtherFee = (Double.parseDouble(processingFee) * 0.3);
			long OtherFeesRounded = Math.round(OtherFee);
			ifr.setValue("P_CB_FE_OTHER_FEES", ("₹ " + String.valueOf(OtherFeesRounded)));
		} catch (Exception e) {
			Log.consoleLog(ifr, "Error occured in autoPopulateFinalEligibilityData CanaraBudget " + e);
		}
		return sliderValue;
	}

	public String autoPopulateRecieveMoneyDataCBudget(IFormReference ifr, String control, String event, String value) {

		try {
			Log.consoleLog(ifr, "inside autoPopulateRecieveMoneyDataCBudget  : ");
//            if (value.equalsIgnoreCase("Receive the Money")) {
//                pcm.stepNameUpdate(ifr, value);
//            }
			pcm.setGetPortalStepName(ifr, value);
			String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			// String query = "SELECT MOBILENUMBER from LOS_WIREFERENCE_TABLE where WINAME =
			// '" + ProcessInsanceId + "' ";
			String query = ConfProperty.getQueryScript("getMobNumFromWIREFTable").replaceAll("#ProcessInsanceId#",
					ProcessInsanceId);
			Log.consoleLog(ifr, "MOBILENUMBER Query : " + query);
			List list = ifr.getDataFromDB(query);
			String MOBILENUMBER = list.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "MOBILENUMBER : " + MOBILENUMBER);
			List<List<String>> loanAmount = null;
			// String data = "SELECT LOAN_AMOUNT FROM LOS_L_FINAL_ELIGIBILITY where PID ='"
			// + ProcessInsanceId + "'";
			/*
			 * String data =
			 * ConfProperty.getQueryScript("PORTALFINDSLIDERVALUE").replaceAll("#WINAME#",
			 * ProcessInsanceId); Log.consoleLog(ifr, "loanAmount query : " + data);
			 * loanAmount = ifr.getDataFromDB(data); Log.consoleLog(ifr, "loanAmount : " +
			 * loanAmount); String amount = loanAmount.get(0).get(0); String tenure =
			 * loanAmount.get(0).get(1); ifr.setValue("P_CB_RM_LOANAMOUNT", amount);
			 * ifr.setValue("P_CB_NETDISBURSEMENTNO", amount); Log.consoleLog(ifr,
			 * "amount : " + amount);
			 */
			// String QueryData = "select
			// LOANAMOUNT,TENURE,RATEOFINTEREST,EMI,PROCESSINGFEE,SB_ACCOUNTNO,RP_ACCOUNTNO
			// from los_trn_finaleligibility where WINAME='" + ProcessInsanceId + "'";
			String QueryData = ConfProperty.getQueryScript("getDataFromFinalEligQuery").replaceAll("#ProcessInsanceId#",
					ProcessInsanceId);
			List<List<String>> dataResult = ifr.getDataFromDB(QueryData);
			Log.consoleLog(ifr, "dataResult" + dataResult);
			if (!dataResult.isEmpty()) {
				String amount = dataResult.get(0).get(0);
				String tenure = dataResult.get(0).get(1);
				String roi = dataResult.get(0).get(2);
				String emi = dataResult.get(0).get(3);
				String PROCESSINGFEE = dataResult.get(0).get(4);
				String SB_ACCOUNTNO = dataResult.get(0).get(5);
				String RP_ACCOUNTNO = dataResult.get(0).get(6);
				ifr.setValue("P_CB_RM_LOANAMOUNT", amount);
				ifr.setValue("P_CB_NETDISBURSEMENTNO", amount);
				Log.consoleLog(ifr, "amount : " + amount);
				ifr.setValue("P_CB_RM_ROI", (String.valueOf(roi)) + " %");
				ifr.setValue("P_CB_RM_TENURE", (String.valueOf(tenure)) + " Months");
				ifr.setStyle("P_CB_RM_TENURE", "readonly", "true");
				ifr.setValue("P_CB_SBACCOUNTNO", SB_ACCOUNTNO);
				ifr.setValue("P_CB_REPAYMENTNO", RP_ACCOUNTNO);
				ifr.setValue("P_CB_RM_PROCESSINGFEE", "₹ " + PROCESSINGFEE);
				ifr.setValue("P_CB_RM_EMI", "₹ " + emi);
			}
			/*
			 * List<List<String>> loanROI = null; String roiData =
			 * "select totalroi from los_m_roi where roiid='R21'"; Log.consoleLog(ifr,
			 * "roiData query : " + roiData); loanROI = ifr.getDataFromDB(roiData); //
			 * Log.consoleLog(ifr, "loanROI : " + loanROI); String roi =
			 * loanROI.get(0).get(0); ifr.setValue("P_CB_RM_ROI", (String.valueOf(roi)) +
			 * " %"); Log.consoleLog(ifr, "roi : " + roi); ifr.setStyle("P_CB_RM_ROI",
			 * "readonly", "true");
			 */
			// List<List<String>> loanTenure = null;
			// String tenureData = "select maxtenure from LOS_M_LoanInfo where
			// scheme_id='S22'";
			// Log.consoleLog(ifr, "tenureData query : " + tenureData);
			// loanTenure = ifr.getDataFromDB(tenureData);
			// Log.consoleLog(ifr, "loanTenure : " + loanTenure);
			// String tenure = loanTenure.get(0).get(0);
			// ifr.setValue("P_CB_PA_TENURE", tenure);
			// Log.consoleLog(ifr, "tenure : " + tenure);
			// ifr.setValue("P_CB_RM_TENURE", (String.valueOf(tenure)) + " Months");
			ifr.setStyle("P_CB_RM_TENURE", "readonly", "true");
			/*
			 * List<List<String>> loanAccNo = null; String accNoData =
			 * "SELECT ACCOUNTID FROM LOS_NL_BASIC_INFO WHERE CUSTOMERFLAG = 'Y' AND PID ='"
			 * + ProcessInsanceId + "'"; Log.consoleLog(ifr, "accNoData query : " +
			 * accNoData); loanAccNo = ifr.getDataFromDB(accNoData); Log.consoleLog(ifr,
			 * "loanAccNo : " + loanAccNo); String accNo = loanAccNo.get(0).get(0);
			 * Log.consoleLog(ifr, "accNo : " + accNo); ifr.setValue("P_CB_SBACCOUNTNO",
			 * accNo); ifr.setValue("P_CB_REPAYMENTNO", accNo); Log.consoleLog(ifr,
			 * "accNo Populated : " + accNo); String processingFee =
			 * pcm.getCalulatedProcessingFeeCB(ifr, amount); Log.consoleLog(ifr,
			 * "processingFee : " + processingFee); Double processingFEES =
			 * (Double.parseDouble(processingFee)); Double OtherFee =
			 * (Double.parseDouble(processingFee) * 0.1); Double FeesAndCharges =
			 * processingFEES + OtherFee; Log.consoleLog(ifr, "processingFee : " +
			 * FeesAndCharges); long FeesAndChargesRound = Math.round(FeesAndCharges);
			 * ifr.setValue("P_CB_RM_PROCESSINGFEE", "₹ " +
			 * String.valueOf(FeesAndChargesRound));
			 */
			ifr.setStyle("P_CB_RM_FINTECH_CHARGES", "readonly", "true");
			// String[] emiDetails = pcm.getCalulatedLoanDetails(ifr, amount, tenure, roi);
			// String emi = emiDetails[1];
			// long emiRupeeRounded = Math.round(Double.parseDouble(emi));
			// ifr.setValue("P_CB_RM_EMI", ("₹ " + String.valueOf(emiRupeeRounded)));
			ifr.setStyle("P_CB_RM_EMI", "readonly", "true");
			// Log.consoleLog(ifr, "emiRupeeRounded : " + emiRupeeRounded);
			// String OTP = ifr.getValue("P_CB_ENTEROTP").toString();
			// Log.consoleLog(ifr, " OTP : " + OTP);
			// Added By Shivam on 20-01-2024
			// String Query = "select CICFEES,ESIGN,FINTECHCHARGES from
			// los_nl_proposed_facility where PID='" + ProcessInsanceId + "'";
			String Query = ConfProperty.getQueryScript("getCICESIGNFintechChrgQuery").replaceAll("#ProcessInsanceId#",
					ProcessInsanceId);
			List<List<String>> resultset = ifr.getDataFromDB(Query);
			Log.consoleLog(ifr, "Query is " + Query);
			if (!resultset.isEmpty()) {
				String cicFee = resultset.get(0).get(0);
				String Esign = resultset.get(0).get(1);
				String FintecCharges = resultset.get(0).get(2);
				ifr.setValue("P_CB_RM_CICFEES", cicFee);
				ifr.setValue("P_CB_RM_SIGN_STAMP", Esign);
				ifr.setValue("P_CB_RM_FINTECH_CHARGES", FintecCharges);
			}

			// Added by Ahmed for Inserting the values to LOS_TRN_FINALELIGIBILITY on
			// 22-01-2024 after disc with Acc.team
			/*
			 * String Query1 = "DELETE FROM LOS_TRN_FINALELIGIBILITY WHERE WINAME='" +
			 * ProcessInsanceId + "'"; Log.consoleLog(ifr, "Query1==>" + Query1);
			 * ifr.saveDataInDB(Query1);
			 * 
			 * String Query2 =
			 * "INSERT INTO LOS_TRN_FINALELIGIBILITY(WINAME,LOANAMOUNT,Tenure,RATEOFINTEREST,EMI) "
			 * + "VALUES('" + ProcessInsanceId + "','" + amount + "','" + tenure + "','" +
			 * roi + "','" + emiRupeeRounded + "')"; Log.consoleLog(ifr, "Query2==>" +
			 * Query2); ifr.saveDataInDB(Query2);
			 */
			// For NESL Triggering
			Log.consoleLog(ifr, "#Documents generated..NESL is going to trigger.");
			EsignIntegrationChannel NESL = new EsignIntegrationChannel();
//			String returnMessageFromNESL = NESL.redirectNESLRequest(ifr, "Budget", "eStamping");
//			JSONParser jp = new JSONParser();
//			if (returnMessageFromNESL.contains(RLOS_Constants.ERROR)) {
//				return pcm.returnErrorHold(ifr);
//			} else if (returnMessageFromNESL.contains("showMessage")) {
//				JSONObject obj = (JSONObject) jp.parse(returnMessageFromNESL);
//				obj.put("eflag", "false");
//				return obj.toString();
//			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Error occured in autoPopulateRecieveMoneyData " + e);
		}
		return "";
	}

	public void autoPopulateFinalScreenDataCBudget(IFormReference ifr, String control, String event, String value) {
		try {
			Log.consoleLog(ifr, "inside autoPopulateFinalScreenData  : ");
//            if (value.equalsIgnoreCase("Final Screen")) {
//                pcm.stepNameUpdate(ifr, value);
//            }
			pcm.setGetPortalStepName(ifr, value);
			String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			// String query = "SELECT MOBILENUMBER from LOS_WIREFERENCE_TABLE where WINAME =
			// '" + ProcessInsanceId + "' ";
			String query = ConfProperty.getQueryScript("getMobNumFromWIREFTable").replaceAll("#ProcessInsanceId#",
					ProcessInsanceId);
			Log.consoleLog(ifr, "MOBILENUMBER Query : " + query);
			List list = ifr.getDataFromDB(query);
			String MOBILENUMBER = list.toString().replace("[", "").replace("]", "");
			Log.consoleLog(ifr, "MOBILENUMBER : " + MOBILENUMBER);
			String APPLICATION_REFNO = "", FinalLoanAccNo = "", FinalSanctionAmt = "", EMIDATE = "";
			String FinalScreenDataQuery = ConfProperty.getQueryScript("getFinalScreenData").replaceAll("#PID#",
					ProcessInsanceId);
			Log.consoleLog(ifr, "FinalScreenDataQuery===>" + FinalScreenDataQuery);
			List<List<String>> Result = ifr.getDataFromDB(FinalScreenDataQuery);
			Log.consoleLog(ifr, "FinalLoanDetailsList : " + Result);
			if (!Result.isEmpty()) {
				APPLICATION_REFNO = Result.get(0).get(0);
				FinalLoanAccNo = Result.get(0).get(1);
				FinalSanctionAmt = Result.get(0).get(2);
				EMIDATE = Result.get(0).get(3);
				Log.consoleLog(ifr, "APPLICATION_REFNO : " + APPLICATION_REFNO);
				Log.consoleLog(ifr, "FinalLoanAccNo : " + FinalLoanAccNo);
				Log.consoleLog(ifr, "FinalSanctionAmt : " + FinalSanctionAmt);
				Log.consoleLog(ifr, "EMIDATE : " + EMIDATE);
			}
			if (APPLICATION_REFNO.equalsIgnoreCase("") || APPLICATION_REFNO.equalsIgnoreCase("null")) {
				APPLICATION_REFNO = ifr.getValue("P_PAPL_REFERENCENO1").toString();
				Log.consoleLog(ifr, "ApplicationRefNo Updated : " + APPLICATION_REFNO);
			}
			ifr.setValue("P_CB_FINAL_REF_NO", APPLICATION_REFNO);
			ifr.setValue("P_CB_FS_LOAN_ACC_NO", FinalLoanAccNo);
			ifr.setValue("P_CB_FS_SANCTIONED_LOAN", FinalSanctionAmt);
			ifr.setValue("P_CB_FINAL_EMIDATE", EMIDATE);

			// current Time
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();

			ifr.setValue("P_CB_FINAL_DATEOFDISBURSEMENT", dtf.format(now));
			// ifr.setValue("P_CB_FINAL_EMIDATE", dtf.format(now));

			ifr.setStyle("navigationBackBtn", "visible", "false");
		} catch (Exception e) {
			Log.consoleLog(ifr, "Error occured in autoPopulateFinalScreenData " + e);
		}
	}

	/*
	 * public void autoPopulateFinalScreenDataCBudget(IFormReference ifr, String
	 * control, String event, String value) { try { Log.consoleLog(ifr,
	 * "inside autoPopulateFinalScreenData  : "); // if
	 * (value.equalsIgnoreCase("Final Screen")) { // pcm.stepNameUpdate(ifr, value);
	 * // } pcm.setGetPortalStepName(ifr, value); String ProcessInsanceId =
	 * ifr.getObjGeneralData().getM_strProcessInstanceId(); String query =
	 * "SELECT MOBILENUMBER from LOS_WIREFERENCE_TABLE where WINAME = '" +
	 * ProcessInsanceId + "' "; Log.consoleLog(ifr, "MOBILENUMBER Query : " +
	 * query); List list = ifr.getDataFromDB(query); String MOBILENUMBER =
	 * list.toString().replace("[", "").replace("]", ""); Log.consoleLog(ifr,
	 * "MOBILENUMBER : " + MOBILENUMBER); String ApplicationRefNo =
	 * ifr.getValue("P_PAPL_REFERENCENO1").toString(); Log.consoleLog(ifr,
	 * "ApplicationRefNo : " + ApplicationRefNo); ifr.setValue("P_CB_FINAL_REF_NO",
	 * ApplicationRefNo); List<List<String>> loanAccNo = null; String accNoData =
	 * "select salaryacc_no from LOS_MST_CB where mobile_no ='" + MOBILENUMBER +
	 * "'"; Log.consoleLog(ifr, "accNoData query : " + accNoData); loanAccNo =
	 * ifr.getDataFromDB(accNoData); Log.consoleLog(ifr, "loanAccNo : " +
	 * loanAccNo); String accNo = loanAccNo.get(0).get(0); Log.consoleLog(ifr,
	 * "accNo : " + accNo); ifr.setValue("P_CB_FINALLOANAMOUNT", accNo);
	 * ifr.setValue("P_CB_SBLOANNO", accNo);
	 * 
	 * //current Time DateTimeFormatter dtf =
	 * DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"); LocalDateTime now =
	 * LocalDateTime.now();
	 * 
	 * ifr.setValue("P_CB_FINAL_DATEOFDISBURSEMENT", dtf.format(now));
	 * ifr.setValue("P_CB_FINAL_EMIDATE", dtf.format(now));
	 * 
	 * ifr.setStyle("navigationBackBtn", "visible", "false"); } catch (Exception e)
	 * { Log.consoleLog(ifr, "Error occured in autoPopulateFinalScreenData " + e); }
	 * }
	 */
	/*
	 * public void docGen(IFormReference ifr, String docid, String journeyName) {
	 * 
	 * try { Log.consoleLog(ifr, "inside docGen  : "); ObjectMapper objectMapper =
	 * new ObjectMapper(); ObjectNode userNode = objectMapper.createObjectNode();
	 * Log.consoleLog(ifr, "userNode: " + userNode.toString()); userNode.put("Mode",
	 * "SINGLE"); userNode.put("DocID", docid); userNode.put("callFrom",
	 * "Backoffice"); userNode.put("journey", "Pre-Approved Personal Loan");
	 * userNode.put("journey", journeyName); userNode.put("referenceKey",
	 * ifr.getObjGeneralData().getM_strProcessInstanceId());
	 * userNode.put("Activity", ifr.getActivityName()); userNode.put("Identifier",
	 * "N"); userNode.put("RPSchedule", "N"); userNode.put("TypeOfFecility",
	 * "Term Loan"); userNode.put("InterestRate", 8.5); userNode.put("LoanTerm",
	 * "Y"); userNode.put("LoanAmount", 15000); GenerateDocument dc = new
	 * GenerateDocument(); JsonNode userDoc = dc.executeDocGenerator(ifr, userNode);
	 * Log.consoleLog(ifr, "userDoc: " + userDoc.toString()); } catch (Exception e)
	 * { Log.consoleLog(ifr, "Error occured in docGen " + e); Log.errorLog(ifr,
	 * "Error occured in docGen " + e); }
	 * 
	 * }
	 */
	// to getDefault API input from DB
	public String getLoanAccountCreate(IFormReference ifr, String param) {
		Log.consoleLog(ifr, "Entered into getLoanAccountCreate  :");
		String query = "SELECT CONSTNAME,CONSTVALUE FROM LOS_MST_CONSTANTS ";
		String value = null;
		try {
			List<List<String>> LoanAccCreationData = null;
			JSONArray gridResultSet = new JSONArray();
			LoanAccCreationData = ifr.getDataFromDB(query);
			for (int i = 0; i < LoanAccCreationData.size(); i++) {
				JSONObject formDetailsJson = new JSONObject();
				if (LoanAccCreationData.get(i).get(0).equalsIgnoreCase(param)) {
					value = LoanAccCreationData.get(i).get(1);
				}

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Error occured in getLoanAccountCreate " + e);
		}
		return value;
	}

	public String loanDisbursementCompleteAPI(IFormReference ifr, String param) {
		Log.consoleLog(ifr, "Inside loanDisbursementCompleteAPI : ");
		String loanScheduleRes = "";
		Log.consoleLog(ifr, "inside  LoanScheduleAPI CALL : ");
		try {
			JSONObject jsonInput = new JSONObject();
			jsonInput.put("CustomerId", "1234");
			String request = jsonInput.toString();
			HashMap<String, String> requestHeader = new HashMap<>();
			loanScheduleRes = cf.CallWebService(ifr, "LoanSchedule", request, "", requestHeader);
			Log.consoleLog(ifr, "loanScheduleRes " + loanScheduleRes);
			JSONParser parser = new JSONParser();
			JSONObject obj = (JSONObject) parser.parse(loanScheduleRes);
			String responseMessage = cf.getJsonValue(obj, "responseMessage");
			Log.consoleLog(ifr, "responseMessage loanScheduleRes" + responseMessage);
			String responseCode = cf.getJsonValue(obj, "responseCode");
			Log.consoleLog(ifr, "responseCode " + responseCode);
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in  loanScheduleRes" + e.getMessage());

		}

		return null;
	}

	public String mAccClickSendOTPRecieveMoneyCBudget(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccClickSendOTPRecieveMoneyCBudget");
		JSONObject messagereturn = new JSONObject();
		messagereturn.put("showMessage", cf.showMessage(ifr, "P_CB_RM_SENDOTP", "error", "OTP SENT SUCCESSFULLY!! "));
		ifr.setStyle("P_CB_ENTEROTP", "visible", "true");
		ifr.setStyle("P_CB_RM_VALIDATE", "visible", "true");

		messagereturn.put("retValue", "optRM");
		return messagereturn.toString();
	}

	public String mAccValidateOTPRecieveMoneyCBudget(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccValidateOTPRecieveMoneyCBudget : ");
		String PID = ifr.getObjGeneralData().getM_strProcessInstanceId();
		JSONObject re = new JSONObject();
		Log.consoleLog(ifr, "inside EnteredOTP_CB_ENTEROTP  : ");
		String OTP = ifr.getValue("P_CB_ENTEROTP").toString();
		Log.consoleLog(ifr, " OTP : " + OTP);
		if (OTP.equalsIgnoreCase("")) {
			Log.consoleLog(ifr, " blank ");
			re.put("showMessage", cf.showMessage(ifr, "P_CB_RM_VALIDATE", "error", "Please Enter OTP"));

		}
		if (OTP.equalsIgnoreCase("306106")) {
			Log.consoleLog(ifr, "inside otp verification Correct OTP");

			try {
				Log.consoleLog(ifr, "CBSFinalScreenValidation is calling.....");
				BudgetDisbursementScreen bfs = new BudgetDisbursementScreen();
				bfs.CBSFinalScreenValidation(ifr, PID);
				re.put("NavigationNextClick", "true");

			} catch (Exception e) {
				Log.consoleLog(ifr, "Exception:" + e);
				Log.errorLog(ifr, "Exception:" + e);
			}

			Log.consoleLog(ifr, "Correct OTP");
			re.put("showMessage", cf.showMessage(ifr, "P_CB_RM_VALIDATE", "error", "Disbursement Successful"));
		} else {
			Log.consoleLog(ifr, "inside otp verification wrong OTP");
			re.put("showMessage", cf.showMessage(ifr, "P_CB_RM_VALIDATE", "error", "Please enter Correct OTP"));
			ifr.setStyle("navigationBackBtn", "visible", "true");
			return re.toString();
		}
		return re.toString();
	}

	public String mImpOnClickCustFeedBackButton(IFormReference ifr, String control, String event, String value) {
		JSONObject message = new JSONObject();
		try {
			ifr.setStyle("P_PAPL_HAPPY_FEEDBACK", "visible", "true");
			ifr.setStyle("PL_PAPL_DROPOFF", "visible", "false");
			message.put("NavigationNextClick", "true");
		} catch (Exception e) {
		}

		return "";
	}

	// Start LAD
	public String userSeletedFD(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside userSelectedFD");
		return lpcc.mUserSeletedFD(ifr, control, event, value);
	}

	public String mAccClickSendOTPRecieveMoneyODAD(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccClickSendOTPRecieveMoneyODAD");
		JSONObject messagereturn = new JSONObject();
		try {
			EsignCommonMethods NESLCM = new EsignCommonMethods();
			String Status = NESLCM.checkNESLWorkflowStatus(ifr);
			Log.consoleLog(ifr, "Status==>" + Status);
			if (!(Status.equalsIgnoreCase(""))) {
				messagereturn.put("showMessage", cf.showMessage(ifr, "", "error", Status));
				return messagereturn.toString();
			}
			if (ifr.getValue("P_ODAD_SBACCOUNTNO").toString().equalsIgnoreCase("")) {
				messagereturn.put("showMessage", cf.showMessage(ifr, "", "error", "Please Select SB Account Number!"));
				return messagereturn.toString();
			}

			String mobileno = pcm.getCurrentWiMobileNumber(ifr);
			String currentDate = cf.getCurrentDateTime(ifr);
			String randomnum = pcm.generateRandomNumber(ifr);
			String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			Log.consoleLog(ifr, randomnum);
			String query = "";
			query = ConfProperty.getQueryScript("PCOUNTOTPQuery").replaceAll("#mobileno#", mobileno);
			Log.consoleLog(ifr, "query:" + query);
			List<List<String>> mobilecount = cf.mExecuteQuery(ifr, query, "Count For Mobile No:");
			String otpCheck = ConfProperty.getQueryScript("OTPCHECKENABLE");
			if (otpCheck.equalsIgnoreCase("NO")) {
				randomnum = ConfProperty.getQueryScript("OTPDEFAULT");
				Log.consoleLog(ifr, "otpCheck No : " + otpCheck);
				// ema.emailNotificationAPI(ifr, email, randomnum, mobileno);
			}
			if ((mobilecount.get(0).get(0).equalsIgnoreCase("0"))) {
				query = ConfProperty.getQueryScript("PINSERTOTPQuery").replaceAll("#mobileno#", mobileno)
						.replaceAll("#randomnum#", randomnum).replaceAll("#currentDate#", currentDate);
				Log.consoleLog(ifr, "query:" + query);
			} else {
				query = ConfProperty.getQueryScript("PUPDATEOTPQuery").replaceAll("#mobileno#", mobileno)
						.replaceAll("#randomnum#", randomnum).replaceAll("#currentDate#", currentDate);
				Log.consoleLog(ifr, "query:" + query);
			}
			Log.consoleLog(ifr, query);
			int result = ifr.saveDataInDB(query);
			Log.consoleLog(ifr, "result:" + result);
			if (result > 0) {
				// String msgToPrompt = "OTP SENT SUCCESSFULLY!! ";
				if (otpCheck.equalsIgnoreCase("YES")) {

					// Added by Ahmed on 27-05-2024 using DLPCommonObjects instead of directly
					// calling
					String LoanAmount = ifr.getValue("P_ODAD_RM_LOANAMOUNT").toString();
					String bodyParams = randomnum + "#" + "RETAIL" + "#" + LoanAmount;
					String subjectParams = "";
					String fileName = "";// Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
					String fileContent = "";// Added by Ahmed on 03-06-2024 for performing FileContent EMAIL Validations
					pcm.triggerCCMAPIs(ifr, processInstanceId, "PAPL", "3", bodyParams, subjectParams, fileName,
							fileContent);

					// ema.emailNotificationAPI(ifr, email, randomnum, mobileno);
//                    Log.consoleLog(ifr, "otpCheck Yes : " + otpCheck);
//                    //  --call OPT Service
//                    // Create the inner JSON object
//                    JSONObject encryptData = new JSONObject();
//                    encryptData.put("dest", "91" + mobileno);
//                    encryptData.put("msg", "OTP is " + randomnum + " for login into Corporate Account. This OTP is valid for the duration of 10 minutes. Do not share this OTP with anyone for security reasons. \nCanara Bank");
//                    encryptData.put("uname", ConfProperty.getQueryScript("SMSUName"));
//                    encryptData.put("pwd", ConfProperty.getQueryScript("SMSPwd"));
//                    encryptData.put("intl", "0");
//                    encryptData.put("prty", "1");
//                    String serviceName = "SendOTP";
//                    String reqest = encryptData.toString();
//                    HashMap<String, String> requestHeader = new HashMap<>();
//                    String response = cf.CallWebService(ifr, serviceName, reqest, "", requestHeader);
//                    Log.consoleLog(ifr, "SMS OTP Response : " + response);
				}
			}
			ifr.setStyle("P_ODAD_RM_Resend", "visible", "false");
			ifr.setStyle("P_ODAD_ENTEROTP", "visible", "true");
			ifr.setStyle("P_ODAD_RM_VALIDATE", "visible", "true");
			ifr.setStyle("Portal_L_Timer_Level_ODAD", "visible", "true");
			ifr.setStyle("Portal_L_Timer_ODAD", "visible", "true");
			messagereturn.put("clearOTPTypeField", "P_ODAD_ENTEROTP");
			messagereturn.put("retValue", "optRMODAD");
			return messagereturn.toString();
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception mAccClickSendOTPRecieveMoney : " + e);
		}
		return "";
	}

	public String getFDAccountEnquiry(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside PortalCustomCode getFDAccountEnquiry");
		ifr.clearTable("P_LAD_IDC_FD2");
		return lpcc.getFDAccountEnquiry(ifr);
	}

	// End LAD
	public void pensionOccupation(IFormReference ifr, String control, String event, String value) {

	}

	public String mAccCheckWorkingExperience(IFormReference ifr, String control, String event, String value) {
		JSONObject message = new JSONObject();
		try {
			String loanType = pcm.getLoanType(ifr, control, event, value);
			if (loanType.equalsIgnoreCase("Canara Budget")) {
				Log.consoleLog(ifr, "inside the mAccCheckWorkingExperience : ");
				double inputValue = Double.parseDouble(value);

				Log.consoleLog(ifr, "inside the mAccCheckWorkingExperience inputValue::" + inputValue);
				if (inputValue >= 1) {
					if (inputValue >= 1 && inputValue <= 100 && Math.floor(inputValue * 10) == inputValue * 10) {
						Log.consoleLog(ifr, "Inside regexCondtionTrue::");
					} else {
						Log.consoleLog(ifr, "Inside regexCondtion:Not Matched::");
						ifr.setValue(control, "");
						message.put("showMessage",
								cf.showMessage(ifr, control, "error", "Please enter correct value!"));
						return message.toString();
					}
					BigDecimal currentExperience = new BigDecimal(
							ifr.getValue("P_CB_OD_ExperienceYear").toString().equalsIgnoreCase("") ? "0.0"
									: ifr.getValue("P_CB_OD_ExperienceYear").toString());
					Log.consoleLog(ifr, "currentExperience value::" + currentExperience);
					BigDecimal totalExperience = new BigDecimal(
							ifr.getValue("P_CB_OD_OverAllExperience").toString().equalsIgnoreCase("") ? "0.0"
									: ifr.getValue("P_CB_OD_OverAllExperience").toString());
					Log.consoleLog(ifr, "totalExperience value::" + totalExperience);
					if (currentExperience.compareTo(totalExperience) > 0
							&& !currentExperience.equals(new BigDecimal("0.0"))
							&& !totalExperience.equals(new BigDecimal("0.0"))) {
						Log.consoleLog(ifr, "inside the if condition of mAccCheckWorkingExperience : ");
						// ifr.setValue("P_CB_OD_ExperienceYear", "");
						ifr.setValue("P_CB_OD_OverAllExperience", "");
						message.put("showMessage", cf.showMessage(ifr, control, "error",
								"Total experience should be equal to or greater than  Current Experiece"));
						return message.toString();
					}
				} else {
					Log.consoleLog(ifr, "Inside else Experience Should not be less than 1");
					ifr.setValue(control, "");
					message.put("showMessage", cf.showMessage(ifr, control, "error",
							"Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank as your work experience is less than 1 year."));
					return message.toString();
				}
			} else if (loanType.equalsIgnoreCase("Canara Pension")) {
				Log.consoleLog(ifr, "inside the mAccCheckWorkingExperience Pension : ");
				double inputValue = Double.parseDouble(value);

				Log.consoleLog(ifr, "inside the mAccCheckWorkingExperience inputValue::" + inputValue);
				if (inputValue >= 1) {
					if (inputValue >= 1 && inputValue <= 100 && Math.floor(inputValue * 10) == inputValue * 10) {
						Log.consoleLog(ifr, "Inside regexCondtionTrue::");
					} else {
						Log.consoleLog(ifr, "Inside regexCondtion:Not Matched::");
						ifr.setValue(control, "");
						message.put("showMessage",
								cf.showMessage(ifr, control, "error", "Please enter correct value!"));
						return message.toString();
					}
					BigDecimal currentExperience = new BigDecimal(
							ifr.getValue("P_CP_OD_ExperienceYear_COB").toString().equalsIgnoreCase("") ? "0.0"
									: ifr.getValue("P_CP_OD_ExperienceYear_COB").toString());
					Log.consoleLog(ifr, "currentExperience value::" + currentExperience);
					BigDecimal totalExperience = new BigDecimal(
							ifr.getValue("P_CP_OD_OverAllExperience_COB").toString().equalsIgnoreCase("") ? "0.0"
									: ifr.getValue("P_CP_OD_OverAllExperience_COB").toString());
					Log.consoleLog(ifr, "totalExperience value::" + totalExperience);
					if (currentExperience.compareTo(totalExperience) > 0
							&& !currentExperience.equals(new BigDecimal("0.0"))
							&& !totalExperience.equals(new BigDecimal("0.0"))) {
						Log.consoleLog(ifr, "inside the if condition of mAccCheckWorkingExperience : ");
						// ifr.setValue("P_CB_OD_ExperienceYear", "");
						ifr.setValue("P_CP_OD_OverAllExperience_COB", "");
						message.put("showMessage", cf.showMessage(ifr, control, "error",
								"Total experience should be equal to or greater than  Current Experiece"));
						return message.toString();
					}
				} else {
					Log.consoleLog(ifr, "Inside else Experience Should not be less than 1");
					ifr.setValue(control, "");
					message.put("showMessage", cf.showMessage(ifr, control, "error",
							"Thank you for choosing Canara Bank. You are not eligible for the selected digital loan journey, as per scheme guidelines of the Bank as your work experience is less than 1 year."));
					return message.toString();
				}

			} else {
				Log.consoleLog(ifr, "Inside else for other loan type");

			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in mAccCheckWorkingExperience::" + e);
			Log.errorLog(ifr, "Exception in mAccCheckWorkingExperience::" + e);
			ifr.setValue(control, "");
			message.put("showMessage", cf.showMessage(ifr, control, "error", "Please enter correct value!"));
			return message.toString();
		}
		return "";
	}

	public String mAccCalculateNetIncome(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside the mAccCalculateNetIncome : ");
		JSONObject message = new JSONObject();
		try {
			BigDecimal grossAmt = new BigDecimal(
					ifr.getValue("P_CB_OD_GrossSalary").toString().equalsIgnoreCase("") ? "0.0"
							: ifr.getValue("P_CB_OD_GrossSalary").toString());
			BigDecimal deductamt = new BigDecimal(
					ifr.getValue("P_CB_OD_DeductionFromSalary").toString().equalsIgnoreCase("") ? "0.0"
							: ifr.getValue("P_CB_OD_DeductionFromSalary").toString());
			String grossAmount = ifr.getValue("P_CB_OD_GrossSalary").toString();
			String deduction = ifr.getValue("P_CB_OD_DeductionFromSalary").toString();
			BigDecimal netSalary;
			BigDecimal comp = new BigDecimal(0);
			if ((!grossAmount.equalsIgnoreCase("")) && (!deduction.equalsIgnoreCase(""))) {
				if (grossAmt.compareTo(deductamt) > 0) {
					netSalary = grossAmt.subtract(deductamt);
					String NetSalary = netSalary.toString();
					Log.consoleLog(ifr, "NetSalary ..:" + NetSalary);
					ifr.setValue("P_CB_OD_NetIncome", NetSalary);
				} else {
					ifr.setValue("P_CB_OD_NetIncome", "");
					ifr.setValue("P_CB_OD_DeductionFromSalary", "");
					message.put("showMessage",
							cf.showMessage(ifr, control, "error", "Net Income cannot be less than deductions!"));
					return message.toString();
				}
			} else {
				Log.consoleLog(ifr, "inside the else condition of mAccCalculateNetIncome :");
				if (grossAmt.compareTo(comp) == 0) {
					Log.consoleLog(ifr, "inside the else condition if 1 :");
					ifr.setValue("P_CB_OD_NetIncome", "");
				} else if (deductamt.compareTo(comp) == 0) {
					Log.consoleLog(ifr, "inside the else condition if 1 :");
					ifr.setValue("P_CB_OD_NetIncome", "");
				} else {
					Log.consoleLog(ifr, "inside the else condition:");
				}

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "error inside the  mAccCalculateNetIncome: " + e);
		}
		return "";
	}

	public String mAccOpenWebAPIDocument(IFormReference ifr, String control, String event, String value) {
		return pcm.openWebAPIDocument(ifr, "HEALTHDOCUMENTWEBAPIURL");
		// return pcm.getDocID(ifr, "Communication of Repayment Schedule to Borrowers",
		// "");
	}

	public String mAccOpenNeslDocument(IFormReference ifr, String control, String event, String value) {
		return pcm.getDocID(ifr, "NESL_1_eSigning",
				" and C.Name='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'");
	}

	public void autoPopulateDocUploadDataCB(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside  autoPopulateDocUploadDataCB");
		pcm.setGetPortalStepName(ifr, value);
	}

	// Function modified by Ahmed on 17-07-2024
//    public String validateCustoemrAccountDigit(IFormReference ifr, String control, String event, String value) {
//        Log.consoleLog(ifr, "inside  validateCustoemrAccountDigit");
//        JSONObject re = new JSONObject();
//
//        String strMobiule = ifr.getValue("P_CB_OD_MOBILE_NUMBER").toString();
//        String customerId = ifr.getValue("P_CB_OD_CUSTOMER_ID").toString();
//        Log.consoleLog(ifr, "coapplicant Mobile ::" + strMobiule);
//        Log.consoleLog(ifr, "coapplicant customerId ::" + customerId);
//        if (customerId.length() < 5 || customerId.length() > 13) {
//            ifr.setValue("P_CB_OD_CUSTOMER_ID", "");
//
//            re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_CB_OD_CUSTOMER_ID", "error", "Please enter valid Customer ID"));
//            return re.toString();
//        } else {
//
//            Log.consoleLog(ifr, "before coapplicantcheck ");
//            String StrMessage = bpcc.coapplicantcheck(ifr, customerId, strMobiule);
//            if (StrMessage.equalsIgnoreCase("Error")) {
//                Log.consoleLog(ifr, " coapplicantcheck Error ");
//                ifr.setValue("P_CB_OD_CUSTOMER_ID", "");
//                re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_CB_OD_CUSTOMER_ID", "error", "Kindly enter different co-obligant customer number"));
//                return re.toString();
//            }
////            else {
////                Log.consoleLog(ifr, "before CBS CoObligantCBSCheck ");
////                String CObligantRes = bpcc.CoObligantCBSCheck(ifr, control, event, value);
////                return CObligantRes;
////            }
//
//        }
//        return "";
//    }
	public String validateCustoemrAccountDigit(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside  validateCustoemrAccountDigit");
		JSONObject re = new JSONObject();

		String strMobiule = ifr.getValue("P_CB_OD_MOBILE_NUMBER").toString();
		String customerId = ifr.getValue("P_CB_OD_CUSTOMER_ID").toString();
		Log.consoleLog(ifr, "coapplicant Mobile ::" + strMobiule);
		Log.consoleLog(ifr, "coapplicant customerId ::" + customerId);
		if (customerId.length() < 5 || customerId.length() > 13) {
			ifr.setValue("P_CB_OD_CUSTOMER_ID", "");

			re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_CB_OD_CUSTOMER_ID", "error",
					"Please enter valid Customer ID"));
			return re.toString();
		} else {

			Log.consoleLog(ifr, "before coapplicantcheck ");
			String StrMessage = bpcc.coapplicantcheck(ifr, customerId, strMobiule);
			if (StrMessage.equalsIgnoreCase("Error")) {
				Log.consoleLog(ifr, " coapplicantcheck Error ");
				ifr.setValue("P_CB_OD_CUSTOMER_ID", "");
				re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_CB_OD_CUSTOMER_ID", "error",
						"Kindly enter different co-obligant customer number"));
				return re.toString();
			}
//            else {
//                Log.consoleLog(ifr, "before CBS CoObligantCBSCheck ");
//                String CObligantRes = bpcc.CoObligantCBSCheck(ifr, control, event, value);
//                return CObligantRes;
//            }

		}
		return "";
	}

	public String validateSalariedCustomer(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside  validateCustoemrAccountDigit");
		JSONObject re = new JSONObject();
		String customerProfile = ifr.getValue("P_CB_OD_Profile").toString();
		if (!customerProfile.equalsIgnoreCase("Salaried")) {
			ifr.setValue("P_CB_OD_Profile", "Salaried");

			re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_CB_OD_Profile", "error",
					"Only salaried customers are eligible for this Loan"));
			return re.toString();
		}
//        else {
//            Log.consoleLog(ifr, "before CBS CoObligantCBSCheck ");
//            String CObligantRes = bpcc.CoObligantCBSCheck(ifr, control, event, value);
//            return CObligantRes;
//        }
		return "";
	}

	public String mAccClickSendOTPRecieveMoneyCP(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccClickSendOTPRecieveMoneyCP");
		String result = plpc.mAccClickSendOTPRecieveMoneyCP(ifr, control, event, value);
		return result;
	}

	public String mAccValidateOTPRecieveMoneyCP(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccValidateOTPRecieveMoneyCP");
		String response = plpc.mAccValidateOTPRecieveMoneyCP(ifr, control, event, value);
		return response;
	}

	public void mAccChangeSalaryAccountLAD(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccChangeSalaryAccountLAD");
		lpcc.mChangeSalaryAccountLAD(ifr);
	}

	public void mAccChangeSalaryAccountCB(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccChangeSalaryAccountLAD");
		bpcc.addacctdetails(ifr);
	}

	// modified by ishwarya for DM on 25/6/2024
	public void mAccChangeSalaryAccountVL(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "Inside mAccChangeSalaryAccountVL");
		vlpc.addacctdetailsVL(ifr);
	}
	// addedd by keerthana for account.no update on 01/07/2024
//    public void mAccChangePensionAccountCP(IFormReference ifr, String control, String event, String value) {
//        Log.consoleLog(ifr, "Inside mAccChangePensionAccountCP");
//        plpc.addacctdetailsCP(ifr);
//    }

	public String mAccPortalCheckWorkingExperienceVL(IFormReference ifr, String control, String event, String value) {
		return vlpc.mCheckWorkingExperienceB(ifr, "P_VL_OD_ExperienceYear", "P_VL_OD_OverAllExperience");
	}

	public String mAccPortalCalculateNetIncomeVL(IFormReference ifr, String control, String event, String value) {
		return pcm.mCalculateNetIncome(ifr, "P_VL_OD_GrossSalary", "P_VL_OD_DeductionFromSalary", "P_VL_OD_NetIncome",
				"P_VL_OD_GrossPension", "P_VL_OD_NetPension");
	}

	public String validateSalariedCustomerVL(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside  validateCustoemrAccountDigit");
		JSONObject re = new JSONObject();
		String customerProfile = ifr.getValue("P_VL_OD_Profile").toString();
		if (!customerProfile.equalsIgnoreCase("Salaried")) {
			ifr.setValue("P_VL_OD_Profile", "");
			re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_VL_OD_Profile", "error",
					"Only salaried customers are eligible for this Loan"));
			return re.toString();
		} else {

			return "";
		}
	}

	// modified by sharon on 24/07/2024
	public String validateCustomerAccountDigitVL(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside  validateCustoemrAccountDigitVL");
		JSONObject message = new JSONObject();

		// String ApplicantType="CB";
		String custMobile = ifr.getValue("P_VL_OD_MOBILE_NUMBER").toString();
		String customerId = ifr.getValue("P_VL_OD_CUSTOMER_ID").toString();
		String pattern = "^(?!.*(\\d)\\1{5,})(?=.*\\d{5,13}$).*$";

		if (!customerId.matches(pattern)) {
			ifr.setValue("P_VL_OD_CUSTOMER_ID", "");
			message.put("eflag", "false"); // Hard Stop
			message.put("SHOWMSG", "Please enter a valid Customer ID");
			return message.toString();
		} else {
			Log.consoleLog(ifr, "before coapplicantcheckVL");
			String StrMessage = bpcc.coapplicantcheck(ifr, customerId, custMobile);
			if (StrMessage.equalsIgnoreCase("Error")) {
				ifr.setValue("P_VL_OD_CUSTOMER_ID", "");
				ifr.setValue("P_VL_OD_MOBILE_NUMBER", "");

				message.put("eflag", "false");// Hard Stop
				message.put("SHOWMSG", "Please Enter different Mobile number and customer ID.");
				// re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr,
				// "P_VL_OD_CUSTOMER_ID", "error", "Kindly enter different Co-Borrower customer
				// ID"));
				return message.toString();
			}
		}
		return "";
	}

	public String validateCustoemrAccountDigitPension(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside  validateCustoemrAccountDigit");
		JSONObject re = new JSONObject();
		String strMobiule = ifr.getValue("P_CP_OD_MOBILE_NUMBER").toString();
		String customerId = ifr.getValue("P_CP_OD_CUSTOMER_ID").toString();
		Log.consoleLog(ifr, "coapplicant Mobile ::" + strMobiule);
		Log.consoleLog(ifr, "coapplicant customerId ::" + customerId);
		if (strMobiule != null || customerId != null) {
			if (customerId.length() < 5 || customerId.length() > 13) {
				ifr.setValue("P_CP_OD_CUSTOMER_ID", "");
				re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_CP_OD_CUSTOMER_ID", "error",
						"Please enter valid Customer ID"));
				return re.toString();
			} else {
				Log.consoleLog(ifr, "before coapplicantcheckpension ");
				String StrMessage = bpcc.coapplicantcheck(ifr, customerId, strMobiule);
				if (StrMessage.equalsIgnoreCase("Error")) {
					re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_CP_OD_CUSTOMER_ID", "error",
							"Kindly enter different co-obligant customer number"));
					return re.toString();
				} else {
					return "";
				}
			}
		} else {
			ifr.setStyle("P_CP_OD_MOBILE_NUMBER", "mandatory", "true");
			ifr.setStyle("P_CP_OD_CUSTOMER_ID", "mandatory", "true");
			re.put((Object) "showMessage", (Object) this.cf.showMessage(ifr, "P_CP_OD_CUSTOMER_ID", "error",
					"Kindly enter co-obligant customer id and mobile number"));
			return re.toString();
		}

	}

	public String mAccChangeStatus(IFormReference ifr, String control, String event, String value) {
		JSONObject message = new JSONObject();
		try {
			Log.consoleLog(ifr, "inside the mAccChangeStatus : ");
			String Status = ifr.getValue("P_CB_OD_Status").toString();
			Log.consoleLog(ifr, "Status: " + Status);
			if (Status.equalsIgnoreCase("Temporary")) {
				Log.consoleLog(ifr, "inside the if condition of mAccChangeStatus : ");
				ifr.setValue("P_CB_OD_Status", "Permanent");
				message.put("showMessage", cf.showMessage(ifr, control, "error", "Temporary Employment not allowed"));
				return message.toString();
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in mAccChangeStatus::" + e);
			Log.errorLog(ifr, "Exception in mAccChangeStatus::" + e);
			ifr.setValue(control, "");
			message.put("showMessage", cf.showMessage(ifr, control, "error", "Please enter correct value!"));
			return message.toString();
		}
		return "";
	}

	public String mAccChangeDateSuperAnnuation(IFormReference ifr, String control, String event, String value) {
		JSONObject message = new JSONObject();
		try {
			LADPortalCustomCode obj = new LADPortalCustomCode();
			Log.consoleLog(ifr, "inside the SuperAnnuation : ");
			String DATEOFSUPERANNUATION = ifr.getValue("P_CB_OD_DATEOFSUPERANNUATION").toString();
			Log.consoleLog(ifr, "before fortmat DATEOFSUPERANNUATION: " + DATEOFSUPERANNUATION);
			DATEOFSUPERANNUATION = DATEOFSUPERANNUATION.replaceAll("/", "-");
			Log.consoleLog(ifr, "after fortmat DATEOFSUPERANNUATION: " + DATEOFSUPERANNUATION);
			int diff = Integer.parseInt(obj.getMonthDifference(ifr, DATEOFSUPERANNUATION));

			if (diff < 12) {
				Log.consoleLog(ifr, "inside the if condition of mAccChangeStatus : ");
				ifr.setValue("P_CB_OD_DATEOFSUPERANNUATION", "");
				message.put("showMessage", cf.showMessage(ifr, control, "error",
						"Date Of SuperAnnuation Should be More than  12 Months of Current Date "));
				return message.toString();
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in mAccChangeStatus::" + e);
			Log.errorLog(ifr, "Exception in mAccChangeStatus::" + e);
			ifr.setValue(control, "");
			message.put("showMessage", cf.showMessage(ifr, control, "error", "Please enter correct value!"));
			return message.toString();
		}
		return "";
	}

	public String onChangeOccTypeIsSalariedVL(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside mAccPortalCalculateNetPensionVL portalcustomcode ");
		return vlpc.mOnchangeOccTypeIsSalariedVL(ifr);
	}

	public String OnChangeRelationshipBorrowerCB(IFormReference ifr, String control, String event, String value) {
		JSONObject message = new JSONObject();
		try {
			String RELATIONSHIP_BORROWER = ifr.getValue("P_CB_OD_RELATIONSHIP_BORROWER").toString();
			Log.consoleLog(ifr, "RELATIONSHIP_BORROWER: " + RELATIONSHIP_BORROWER);
			if (RELATIONSHIP_BORROWER.equalsIgnoreCase("Others")) {
				Log.consoleLog(ifr, "inside the if condition of OnChangeRelationshipBorrowerCB : ");
				ifr.setStyle("P_CB_OD_RELATIONSHIP_BORROWER_OTHERS", "visible", "true");

			} else {
				Log.consoleLog(ifr,
						"inside the else condition of OnChangeRelationshipBorrowerCB Value  :" + RELATIONSHIP_BORROWER);
				ifr.setStyle("P_CB_OD_RELATIONSHIP_BORROWER_OTHERS", "visible", "false");
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in OnChangeRelationshipBorrowerCB::" + e);
			Log.errorLog(ifr, "Exception in OnChangeRelationshipBorrowerCB::" + e);
		}
		return "";
	}

	public String mAccCalculateNetIncomeCoborrower(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside the mAccCalculateNetIncomeCoborrower : ");
		JSONObject message = new JSONObject();
		try {
			BigDecimal grossAmt = new BigDecimal(
					ifr.getValue("P_CB_OD_GrossSalary_COB").toString().equalsIgnoreCase("") ? "0.0"
							: ifr.getValue("P_CB_OD_GrossSalary_COB").toString());
			BigDecimal deductamt = new BigDecimal(
					ifr.getValue("P_CB_OD_DeductionFromSalary_COB").toString().equalsIgnoreCase("") ? "0.0"
							: ifr.getValue("P_CB_OD_DeductionFromSalary_COB").toString());
			String grossAmount = ifr.getValue("P_CB_OD_GrossSalary_COB").toString();
			String deduction = ifr.getValue("P_CB_OD_DeductionFromSalary_COB").toString();
			BigDecimal netSalary;
			BigDecimal comp = new BigDecimal(0);
			if ((!grossAmount.equalsIgnoreCase("")) && (!deduction.equalsIgnoreCase(""))) {
				if (grossAmt.compareTo(deductamt) > 0) {
					netSalary = grossAmt.subtract(deductamt);
					String NetSalary = netSalary.toString();
					Log.consoleLog(ifr, "NetSalary Coborrower..:" + NetSalary);
					ifr.setValue("P_CB_OD_NetIncome_COB", NetSalary);
				} else {
					ifr.setValue("P_CB_OD_NetIncome_COB", "");
					ifr.setValue("P_CB_OD_DeductionFromSalary_COB", "");
					message.put("showMessage",
							cf.showMessage(ifr, control, "error", "Net Salary cannot be less than deductions !"));
					return message.toString();
				}
			} else {
				Log.consoleLog(ifr, "inside the else condition of mAccCalculateNetIncomeCoborrower :");
				if (grossAmt.compareTo(comp) == 0) {
					Log.consoleLog(ifr, "inside the else condition if 1 :");
					ifr.setValue("P_CB_OD_NetIncome_COB", "");
				} else if (deductamt.compareTo(comp) == 0) {
					Log.consoleLog(ifr, "inside the else condition if 1 :");
					ifr.setValue("P_CB_OD_NetIncome_COB", "");
				} else {
					Log.consoleLog(ifr, "inside the else condition:");
				}

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "error inside the  mAccCalculateNetIncomeCoborrower: " + e);
		}
		return "";
	}

	/*
	 * public String mAccCheckWorkingExperienceInmonthsBorrower(IFormReference ifr,
	 * String control, String event, String value) { JSONObject message = new
	 * JSONObject(); try { Log.consoleLog(ifr,
	 * "inside the mAccCheckWorkingExperienceInmonths : "); double inputValue =
	 * Double.parseDouble(value); Log.consoleLog(ifr,
	 * "inside the mAccCheckWorkingExperienceInmonths inputValue::" + inputValue);
	 * if (inputValue >= 0 && inputValue <= 11 && Math.floor(inputValue * 10) ==
	 * inputValue * 10) { Log.consoleLog(ifr, "Inside regexCondtionTrue::"); } else
	 * { Log.consoleLog(ifr, "Inside regexCondtion:Not Matched::");
	 * ifr.setValue(control, ""); message.put("showMessage", cf.showMessage(ifr,
	 * control, "error", "Please enter correct value!")); return message.toString();
	 * } BigDecimal currentExperience = new
	 * BigDecimal(ifr.getValue("P_CB_OD_ExperienceYear").toString().equalsIgnoreCase
	 * ("") ? "0.0" : ifr.getValue("P_CB_OD_ExperienceYear").toString());
	 * Log.consoleLog(ifr, "currentExperience value::" + currentExperience);
	 * BigDecimal totalExperience = new
	 * BigDecimal(ifr.getValue("P_CB_OD_OverAllExperience").toString().
	 * equalsIgnoreCase("") ? "0.0" :
	 * ifr.getValue("P_CB_OD_OverAllExperience").toString()); Log.consoleLog(ifr,
	 * "totalExperience value::" + totalExperience);
	 * 
	 * BigDecimal currentExperienceInMonths = new
	 * BigDecimal(ifr.getValue("P_CB_OD_Experience_InMonths").toString().
	 * equalsIgnoreCase("") ? "0.0" :
	 * ifr.getValue("P_CB_OD_Experience_InMonths").toString()); Log.consoleLog(ifr,
	 * "currentExperienceInMonths value::" + currentExperienceInMonths); BigDecimal
	 * totalExperienceInMonths = new
	 * BigDecimal(ifr.getValue("P_CB_OD_OverAllExperience_InMonths").toString().
	 * equalsIgnoreCase("") ? "0.0" :
	 * ifr.getValue("P_CB_OD_OverAllExperience_InMonths").toString());
	 * Log.consoleLog(ifr, "totalExperienceInMonths value::" +
	 * totalExperienceInMonths); if (currentExperience.equals(totalExperience)) { if
	 * (currentExperienceInMonths.compareTo(totalExperienceInMonths) > 0 &&
	 * !currentExperienceInMonths.equals(new BigDecimal("0.0")) &&
	 * !totalExperienceInMonths.equals(new BigDecimal("0.0"))) { Log.consoleLog(ifr,
	 * "inside the if condition of mAccCheckWorkingExperienceInmonths : ");
	 * ifr.setValue("P_CB_OD_Experience_InMonths", "");
	 * ifr.setValue("P_CB_OD_OverAllExperience_InMonths", "");
	 * message.put("showMessage", cf.showMessage(ifr, control, "error",
	 * "Current Experience cannot be greater than Total Experience !")); return
	 * message.toString(); } } } catch (Exception e) { Log.consoleLog(ifr,
	 * "Exception in mAccCheckWorkingExperienceInmonths::" + e); Log.errorLog(ifr,
	 * "Exception in mAccCheckWorkingExperienceInmonths::" + e);
	 * ifr.setValue(control, ""); message.put("showMessage", cf.showMessage(ifr,
	 * control, "error", "Please enter correct value!")); return message.toString();
	 * } return ""; }
	 */
	public String mAccCheckWorkingExperienceInmonthsBorrower(IFormReference ifr, String control, String event,
			String value) {
		JSONObject message = new JSONObject();
		try {
			String loanType = pcm.getLoanType(ifr, control, event, value);
			Log.consoleLog(ifr, "Inside loan type" + loanType);

			if (loanType.equalsIgnoreCase("Canara Budget")) {

				Log.consoleLog(ifr, "inside the mAccCheckWorkingExperienceInmonths : ");

				double inputValue = Double.parseDouble(value);

				Log.consoleLog(ifr, "inside the mAccCheckWorkingExperienceInmonths inputValue::" + inputValue);

				if (inputValue >= 0 && inputValue <= 11 && Math.floor(inputValue * 10) == inputValue * 10) {

					Log.consoleLog(ifr, "Inside regexCondtionTrue::");

				} else {

					Log.consoleLog(ifr, "Inside regexCondtion:Not Matched::");

					ifr.setValue(control, "");

					message.put("showMessage", cf.showMessage(ifr, control, "error", "Please enter correct value!"));

					return message.toString();

				}

				BigDecimal currentExperience = new BigDecimal(
						ifr.getValue("P_CB_OD_ExperienceYear").toString().equalsIgnoreCase("") ? "0.0"
								: ifr.getValue("P_CB_OD_ExperienceYear").toString());

				Log.consoleLog(ifr, "currentExperience value::" + currentExperience);

				BigDecimal totalExperience = new BigDecimal(
						ifr.getValue("P_CB_OD_OverAllExperience").toString().equalsIgnoreCase("") ? "0.0"
								: ifr.getValue("P_CB_OD_OverAllExperience").toString());

				Log.consoleLog(ifr, "totalExperience value::" + totalExperience);

				BigDecimal currentExperienceInMonths = new BigDecimal(
						ifr.getValue("P_CB_OD_Experience_InMonths").toString().equalsIgnoreCase("") ? "0.0"
								: ifr.getValue("P_CB_OD_Experience_InMonths").toString());

				Log.consoleLog(ifr, "currentExperienceInMonths value::" + currentExperienceInMonths);

				BigDecimal totalExperienceInMonths = new BigDecimal(
						ifr.getValue("P_CB_OD_OverAllExperience_InMonths").toString().equalsIgnoreCase("") ? "0.0"
								: ifr.getValue("P_CB_OD_OverAllExperience_InMonths").toString());

				Log.consoleLog(ifr, "totalExperienceInMonths value::" + totalExperienceInMonths);

				if (currentExperience.equals(totalExperience)) {

					if (currentExperienceInMonths.compareTo(totalExperienceInMonths) > 0
							&& !currentExperienceInMonths.equals(new BigDecimal("0.0"))
							&& !totalExperienceInMonths.equals(new BigDecimal("0.0"))) {

						Log.consoleLog(ifr, "inside the if condition of mAccCheckWorkingExperienceInmonths : ");

						ifr.setValue("P_CB_OD_Experience_InMonths", "");

						ifr.setValue("P_CB_OD_OverAllExperience_InMonths", "");

						message.put("showMessage", cf.showMessage(ifr, control, "error",
								"Total experience should be equal to or greater than  Current Experiece"));

						return message.toString();

					}

				}
			} else if (loanType.equalsIgnoreCase("Canara Pension")) {
				Log.consoleLog(ifr, "inside the mAccCheckWorkingExperienceInmonths : ");

				double inputValue = Double.parseDouble(value);

				Log.consoleLog(ifr, "inside the mAccCheckWorkingExperienceInmonths inputValue::" + inputValue);

				if (inputValue >= 0 && inputValue <= 11 && Math.floor(inputValue * 10) == inputValue * 10) {

					Log.consoleLog(ifr, "Inside regexCondtionTrue::");

				} else {

					Log.consoleLog(ifr, "Inside regexCondtion:Not Matched::");

					ifr.setValue(control, "");

					message.put("showMessage", cf.showMessage(ifr, control, "error", "Please enter correct value!"));

					return message.toString();

				}

				BigDecimal currentExperience = new BigDecimal(
						ifr.getValue("P_CP_OD_ExperienceYear_COB").toString().equalsIgnoreCase("") ? "0.0"
								: ifr.getValue("P_CP_OD_ExperienceYear_COB").toString());

				Log.consoleLog(ifr, "currentExperience value::" + currentExperience);

				BigDecimal totalExperience = new BigDecimal(
						ifr.getValue("P_CP_OD_OverAllExperience_COB").toString().equalsIgnoreCase("") ? "0.0"
								: ifr.getValue("P_CP_OD_OverAllExperience_COB").toString());

				Log.consoleLog(ifr, "totalExperience value::" + totalExperience);

				BigDecimal currentExperienceInMonths = new BigDecimal(
						ifr.getValue("P_CP_OD_ExperienceYear_Months_COB").toString().equalsIgnoreCase("") ? "0.0"
								: ifr.getValue("P_CP_OD_ExperienceYear_Months_COB").toString());

				Log.consoleLog(ifr, "currentExperienceInMonths value::" + currentExperienceInMonths);

				BigDecimal totalExperienceInMonths = new BigDecimal(
						ifr.getValue("P_CP_OD_OverAllExperience_Months_COB").toString().equalsIgnoreCase("") ? "0.0"
								: ifr.getValue("P_CP_OD_OverAllExperience_Months_COB").toString());

				Log.consoleLog(ifr, "totalExperienceInMonths value::" + totalExperienceInMonths);

				if (currentExperience.equals(totalExperience)) {

					if (currentExperienceInMonths.compareTo(totalExperienceInMonths) > 0
							&& !currentExperienceInMonths.equals(new BigDecimal("0.0"))
							&& !totalExperienceInMonths.equals(new BigDecimal("0.0"))) {

						Log.consoleLog(ifr, "inside the if condition of mAccCheckWorkingExperienceInmonths : ");

						ifr.setValue("P_CP_OD_ExperienceYear_Months_COB", "");

						ifr.setValue("P_CP_OD_OverAllExperience_Months_COB", "");

						message.put("showMessage", cf.showMessage(ifr, control, "error",
								"Total experience should be equal to or greater than  Current Experiece"));

						return message.toString();

					}

				}

			} else {
				Log.consoleLog(ifr, "Inside else for other loan type");

			}

		} catch (Exception e) {

			Log.consoleLog(ifr, "Exception in mAccCheckWorkingExperienceInmonths::" + e);

			Log.errorLog(ifr, "Exception in mAccCheckWorkingExperienceInmonths::" + e);

			ifr.setValue(control, "");

			message.put("showMessage", cf.showMessage(ifr, control, "error", "Please enter correct value!"));

			return message.toString();

		}

		return "";

	}

	// Added by Aravindh K K on 28/05/24 for Budget
	public String OnChangeOccupationTypeCOB(IFormReference ifr, String control, String event, String value) {
		JSONObject message = new JSONObject();
		try {
			Log.consoleLog(ifr, "inside the OnChangeOccupationTypeCOB : ");
			String OccupationTypeCOB = ifr.getValue("P_CB_OD_Profile_COB").toString();
			Log.consoleLog(ifr, "OccupationTypeCOB: " + OccupationTypeCOB);
			String RELATIONSHIP_BORROWER = ifr.getValue("P_CB_OD_RELATIONSHIP_BORROWER").toString();
			Log.consoleLog(ifr, "RELATIONSHIP_BORROWER : " + RELATIONSHIP_BORROWER);
			if (OccupationTypeCOB.equalsIgnoreCase("NIE")) {
				Log.consoleLog(ifr,
						"inside the if condition OccupationTypeCOB == Non Income Earner==> " + OccupationTypeCOB);
				if (RELATIONSHIP_BORROWER.equalsIgnoreCase("SON") || RELATIONSHIP_BORROWER.equalsIgnoreCase("D")
						|| RELATIONSHIP_BORROWER.equalsIgnoreCase("S")) {
					ifr.setStyle("P_CB_OD_OrganizationName_COB", "visible", "false");
					ifr.setStyle("P_CB_OD_Category_COB", "visible", "false");
					ifr.setStyle("P_CB_OD_Status_COB", "visible", "false");
					ifr.setStyle("P_CB_OD_Designation_DOB", "visible", "false");
					ifr.setStyle("P_CB_OD_ExperienceYear_COB", "visible", "false");
					ifr.setStyle("P_CB_OD_ExperienceYear_Months_COB", "visible", "false");
					ifr.setStyle("P_CB_OD_OverAllExperience_COB", "visible", "false");
					ifr.setStyle("P_CB_OD_OverAllExperience_Months_COB", "visible", "false");
					ifr.setStyle("P_CB_OD_DATEOFSUPERANNUATION_COB", "visible", "false");
					ifr.setStyle("P_CB_OD_GrossSalary_COB", "visible", "false");
					ifr.setStyle("P_CB_OD_DeductionFromSalary_COB", "visible", "false");
					ifr.setStyle("P_CB_OD_NetIncome_COB", "visible", "false");
					ifr.setStyle("P_CB_OD_TypeOfOccupation_COB", "visible", "false");
				} else {
					ifr.setValue("P_CB_OD_Profile_COB", "");
					Log.consoleLog(ifr, "Inside Non Income Earner Relationship other than Spouse / Legal Heirs ");
					message.put("showMessage", cf.showMessage(ifr, control, "error",
							"Only Spouse/Legal Heirs are allowed to be Non Income Earner as Co-obligant."));
					return message.toString();
				}
			} // Need to be Uncommented once scorecard Coborrower Fixed in BO
				// else if (OccupationTypeCOB.equalsIgnoreCase("PEN")){
				// Log.consoleLog(ifr, "inside if condition Retired OccupationTypeCOB ==> " +
				// OccupationTypeCOB);
				// ifr.setStyle("P_CB_OD_TypeOfOccupation_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_OrganizationName_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_Category_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_Status_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_Designation_DOB", "visible", "false");
				// ifr.setStyle("P_CB_OD_ExperienceYear_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_ExperienceYear_Months_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_OverAllExperience_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_OverAllExperience_Months_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_DATEOFSUPERANNUATION_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_RelationshipWithCanara_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_RelationshipWithCanara_InMonths_COB", "visible",
				// "true");
				// ifr.setStyle("P_CB_OD_GrossSalary_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_DeductionFromSalary_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_NetIncome_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_Residence_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_RecoveryMechanism_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_NatureOfSecurity_COB", "visible", "true");
				//
				// } else if (OccupationTypeCOB.equalsIgnoreCase("PROF")){
				// Log.consoleLog(ifr, "inside if condition Self-Employed/Professional
				// OccupationTypeCOB ==> " + OccupationTypeCOB);
				// ifr.setStyle("P_CB_OD_TypeOfOccupation_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_OrganizationName_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_Category_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_Status_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_Designation_DOB", "visible", "false");
				// ifr.setStyle("P_CB_OD_ExperienceYear_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_ExperienceYear_Months_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_OverAllExperience_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_OverAllExperience_Months_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_DATEOFSUPERANNUATION_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_RelationshipWithCanara_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_RelationshipWithCanara_InMonths_COB", "visible",
				// "true");
				// ifr.setStyle("P_CB_OD_GrossSalary_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_DeductionFromSalary_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_NetIncome_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_Residence_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_RecoveryMechanism_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_NatureOfSecurity_COB", "visible", "true");
				//
				// } else if (OccupationTypeCOB.equalsIgnoreCase("SELF")){
				// Log.consoleLog(ifr, "inside if condition Self-Employed/Professional
				// OccupationTypeCOB ==> " + OccupationTypeCOB);
				// ifr.setStyle("P_CB_OD_TypeOfOccupation_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_OrganizationName_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_Category_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_Status_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_Designation_DOB", "visible", "false");
				// ifr.setStyle("P_CB_OD_ExperienceYear_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_ExperienceYear_Months_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_OverAllExperience_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_OverAllExperience_Months_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_DATEOFSUPERANNUATION_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_RelationshipWithCanara_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_RelationshipWithCanara_InMonths_COB", "visible",
				// "true");
				// ifr.setStyle("P_CB_OD_GrossSalary_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_DeductionFromSalary_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_NetIncome_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_Residence_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_RecoveryMechanism_COB", "visible", "true");
				// ifr.setStyle("P_CB_OD_NatureOfSecurity_COB", "visible", "true");
				//
				// }
				// else if (OccupationTypeCOB.equalsIgnoreCase("OTH")) {
				// Log.consoleLog(ifr, "inside the if condition OccupationTypeCOB == Others: ==>
				// "+ OccupationTypeCOB );
				// ifr.setStyle("P_CB_OD_TypeOfOccupation_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_OrganizationName_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_Category_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_Status_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_Designation_DOB", "visible", "false");
				// ifr.setStyle("P_CB_OD_ExperienceYear_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_ExperienceYear_Months_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_OverAllExperience_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_OverAllExperience_Months_COB", "visible", "false");
				// ifr.setStyle("P_CB_OD_DATEOFSUPERANNUATION_COB", "visible", "false");
				// }
			else {
				Log.consoleLog(ifr, "inside the else condition OccupationTypeCOB ==> " + OccupationTypeCOB);
				ifr.setStyle("P_CB_OD_TypeOfOccupation_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_OrganizationName_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_Category_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_Status_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_Designation_DOB", "visible", "true");
				ifr.setStyle("P_CB_OD_ExperienceYear_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_ExperienceYear_Months_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_OverAllExperience_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_OverAllExperience_Months_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_DATEOFSUPERANNUATION_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_GrossSalary_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_DeductionFromSalary_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_NetIncome_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_Residence_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_RecoveryMechanism_COB", "visible", "true");
				ifr.setStyle("P_CB_OD_NatureOfSecurity_COB", "visible", "true");

			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in OnChangeOccupationTypeCOB::" + e);
			Log.errorLog(ifr, "Exception in OnChangeOccupationTypeCOB::" + e);
			ifr.setValue(control, "");
		}
		return "";
	}

	public String mAccCheckWorkingExperienceCoObligant(IFormReference ifr, String control, String event, String value) {
		JSONObject message = new JSONObject();
		try {
			Log.consoleLog(ifr, "inside the mAccCheckWorkingExperienceCoObligant : ");
			double inputValue = Double.parseDouble(value);
			Log.consoleLog(ifr, "inside the mAccCheckWorkingExperienceCoObligant inputValue::" + inputValue);
			if (inputValue >= 0 && inputValue <= 100 && Math.floor(inputValue * 10) == inputValue * 10) {
				Log.consoleLog(ifr, "Inside regexCondtionTrue::");
			} else {
				Log.consoleLog(ifr, "Inside regexCondtion:Not Matched::");
				ifr.setValue(control, "");
				message.put("showMessage", cf.showMessage(ifr, control, "error", "Please enter correct value!"));
				return message.toString();
			}
			BigDecimal currentExperience = new BigDecimal(
					ifr.getValue("P_CB_OD_ExperienceYear_COB").toString().equalsIgnoreCase("") ? "0.0"
							: ifr.getValue("P_CB_OD_ExperienceYear_COB").toString());
			Log.consoleLog(ifr, "currentExperience value::" + currentExperience);
			BigDecimal totalExperience = new BigDecimal(
					ifr.getValue("P_CB_OD_OverAllExperience_COB").toString().equalsIgnoreCase("") ? "0.0"
							: ifr.getValue("P_CB_OD_OverAllExperience_COB").toString());
			Log.consoleLog(ifr, "totalExperience value::" + totalExperience);
			if (currentExperience.compareTo(totalExperience) > 0 && !currentExperience.equals(new BigDecimal("0.0"))
					&& !totalExperience.equals(new BigDecimal("0.0"))) {
				Log.consoleLog(ifr, "inside the if condition of mAccCheckWorkingExperienceCoObligant : ");
				ifr.setValue("P_CB_OD_ExperienceYear_COB", "");
				ifr.setValue("P_CB_OD_OverAllExperience_COB", "");
				message.put("showMessage", cf.showMessage(ifr, control, "error",
						"Total experience should be equal to or greater than  Current Experiece"));
				return message.toString();
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in mAccCheckWorkingExperienceCoObligant::" + e);
			Log.errorLog(ifr, "Exception in mAccCheckWorkingExperienceCoObligant::" + e);
			ifr.setValue(control, "");
			message.put("showMessage", cf.showMessage(ifr, control, "error", "Please enter correct value!"));
			return message.toString();
		}
		return "";
	}

	public String mAccCheckWorkingExperienceInmonthsCoObligant(IFormReference ifr, String control, String event,
			String value) {

		JSONObject message = new JSONObject();
		try {
			Log.consoleLog(ifr, "inside the mAccCheckWorkingExperienceInmonthsCoObligant : ");
			double inputValue = Double.parseDouble(value);
			Log.consoleLog(ifr, "inside the mAccCheckWorkingExperienceInmonthsCoObligant inputValue::" + inputValue);
			if (inputValue >= 0 && inputValue <= 11 && Math.floor(inputValue * 10) == inputValue * 10) {
				Log.consoleLog(ifr, "Inside regexCondtionTrue::");
			} else {
				Log.consoleLog(ifr, "Inside regexCondtion:Not Matched::");
				ifr.setValue(control, "");
				message.put("showMessage", cf.showMessage(ifr, control, "error", "Please enter correct value!"));
				return message.toString();
			}
			BigDecimal currentExperience = new BigDecimal(
					ifr.getValue("P_CB_OD_ExperienceYear_COB").toString().equalsIgnoreCase("") ? "0.0"
							: ifr.getValue("P_CB_OD_ExperienceYear_COB").toString());
			Log.consoleLog(ifr, "currentExperience value::" + currentExperience);
			BigDecimal totalExperience = new BigDecimal(
					ifr.getValue("P_CB_OD_OverAllExperience_COB").toString().equalsIgnoreCase("") ? "0.0"
							: ifr.getValue("P_CB_OD_OverAllExperience_COB").toString());
			Log.consoleLog(ifr, "totalExperience value::" + totalExperience);
			BigDecimal currentExperienceInMonths = new BigDecimal(
					ifr.getValue("P_CB_OD_ExperienceYear_Months_COB").toString().equalsIgnoreCase("") ? "0.0"
							: ifr.getValue("P_CB_OD_ExperienceYear_Months_COB").toString());
			Log.consoleLog(ifr, "currentExperienceInMonths value::" + currentExperienceInMonths);
			BigDecimal totalExperienceInMonths = new BigDecimal(
					ifr.getValue("P_CB_OD_OverAllExperience_Months_COB").toString().equalsIgnoreCase("") ? "0.0"
							: ifr.getValue("P_CB_OD_OverAllExperience_Months_COB").toString());
			Log.consoleLog(ifr, "totalExperienceInMonths value::" + totalExperienceInMonths);
			if (currentExperience.equals(totalExperience)) {
				if (currentExperienceInMonths.compareTo(totalExperienceInMonths) > 0
						&& !currentExperienceInMonths.equals(new BigDecimal("0.0"))
						&& !totalExperienceInMonths.equals(new BigDecimal("0.0"))) {
					Log.consoleLog(ifr, "inside the if condition of mAccCheckWorkingExperienceInmonthsCoObligant : ");
					ifr.setValue("P_CB_OD_ExperienceYear_Months_COB", "");
					ifr.setValue("P_CB_OD_OverAllExperience_Months_COB", "");
					message.put("showMessage", cf.showMessage(ifr, control, "error",
							"Total experience should be equal to or greater than  Current Experiece"));
					return message.toString();
				}
			}
		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in mAccCheckWorkingExperienceInmonthsCoObligant::" + e);
			Log.errorLog(ifr, "Exception in mAccCheckWorkingExperienceInmonthsCoObligant::" + e);
			ifr.setValue(control, "");
			message.put("showMessage", cf.showMessage(ifr, control, "error", "Please enter correct value!"));
			return message.toString();
		}
		return "";

	}

	public String autoPopulateInPrincipleDataCB(IFormReference ifr, String control, String event, String value) {
		String sliderValue = "";
		try {
			Log.consoleLog(ifr, "inside autoPopulateInPrincipleDataCB  : ");
			pcm.setGetPortalStepName(ifr, value);
			String ProcessInsanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
			String customername = null;
			// String customerdata_Query = "SELECT a.FIRSTNAME FROM LOS_L_BASIC_INFO_I a
			// INNER JOIN LOS_NL_BASIC_INFO b ON a.F_KEY = b.F_KEY WHERE b.CUSTOMERFLAG =
			// 'Y' AND b.PID ='" + ProcessInsanceId + "' and b.ApplicantType='B'";
			String customerdata_Query = ConfProperty.getQueryScript("getCustomerDataQuery")
					.replaceAll("#ProcessInsanceId#", ProcessInsanceId);
			List<List<String>> list3 = cf.mExecuteQuery(ifr, customerdata_Query, "customerdata_Query:");
			if (list3.size() > 0) {
				customername = list3.get(0).get(0);
			}
			Log.consoleLog(ifr, "Customer Name : " + customername);
			ifr.setValue("P_CB_L_CUSNAME", "Congratulations! " + customername);
			String inprinciple = null;
			String RequestedLoanAMT = "";
			// String RequestedLoanAMTQuery = "select REQLOANAMOUNT from
			// LOS_TRN_LOAN_DETAILS WHERE PID ='" + ProcessInsanceId + "'";
			String RequestedLoanAMTQuery = ConfProperty.getQueryScript("QueryInprincipleReqAmt").replaceAll("#PID#",
					ProcessInsanceId);
			// String tenureData_Query =
			// ConfProperty.getQueryScript("getTenureDataQuery").replaceAll("#PID#", PID);
			List<List<String>> RequestedLoanList = cf.mExecuteQuery(ifr, RequestedLoanAMTQuery,
					"RequestedLoanAMTQuery:");
			if (RequestedLoanList.size() > 0) {
				RequestedLoanAMT = RequestedLoanList.get(0).get(0);
			}
			double RequestedLoanDouble = Double.parseDouble(RequestedLoanAMT);

			// String amount_Query = "SELECT IN_PRINCIPLE_AMOUNT FROM
			// LOS_L_FINAL_ELIGIBILITY where PID ='" + ProcessInsanceId + "'";
			String amount_Query = ConfProperty.getQueryScript("getAmountQuery").replaceAll("#ProcessInsanceId#",
					ProcessInsanceId);
			List<List<String>> list4 = cf.mExecuteQuery(ifr, amount_Query, "amount_Query:");
			if (list4.size() > 0) {
				inprinciple = list4.get(0).get(0);
			}
			Log.consoleLog(ifr, "inprinciple : " + inprinciple);
//            
//            if (RequestedLoanDouble < doubleValueInp) {
//                String Query3 = ConfProperty.getQueryScript("updateQueryforPrincipleamtinFinalEligibility").replaceAll("#finalelig#", String.valueOf(RequestedLoanDouble)).replaceAll("#ProcessInstanceId#", ProcessInsanceId);
//                Log.consoleLog(ifr, "Query3===>" + Query3);
//                cf.mExecuteQuery(ifr, Query3, "updateQueryforPrincipleamtinFinalEligibility");
//                doubleValueInp = RequestedLoanDouble;
//                Log.consoleLog(ifr, "inprinciple Final : " + inprinciple);
//            }
			double doubleValueInp = Double.parseDouble(inprinciple);
			double roundedValue = Math.floor(doubleValueInp / 1000) * 1000;
			String roundedString = String.valueOf(roundedValue);
			Log.consoleLog(ifr, "roundedString : " + roundedString);
			ifr.setValue("P_CB_PA_LOAN_AMOUNT", roundedString);
			String Fkey = bpcc.Fkey(ifr, "B");
			String schemeID = pcm.getBaseSchemeID(ifr, ProcessInsanceId);
			Log.consoleLog(ifr, "schemeID:" + schemeID);
			String loanTenure = null;
			// String tenureData_Query = "select maxtenure from LOS_M_LoanInfo where
			// scheme_id='" + schemeID + "'";
			String tenureData_Query = ConfProperty.getQueryScript("getTenureDataQuery").replaceAll("#schemeID#",
					schemeID);
			List<List<String>> list1 = cf.mExecuteQuery(ifr, tenureData_Query, "tenureData_Query:");
			if (list1.size() > 0) {
				loanTenure = list1.get(0).get(0);
			}
			String superAnnuationTenure = "";
			String tenureData_Query1 = ConfProperty.getQueryScript("CBTENUREQUERY").replaceAll("#Fkey#", Fkey);
			// "SELECT FLOOR(MONTHS_BETWEEN(DATEOFSUPERANNUATION, sysdate))AS
			// month_difference from LOS_NL_Occupation_INFO Where F_KEY='" + Fkey + "'";
			List<List<String>> superannotationL = cf.mExecuteQuery(ifr, tenureData_Query1,
					"Based on super annotation:");
			if (superannotationL.size() > 0) {
				superAnnuationTenure = superannotationL.get(0).get(0);
			}
			Log.consoleLog(ifr, "superAnnuationTenure : " + superAnnuationTenure);
			if (Integer.parseInt(superAnnuationTenure) < Integer.parseInt(loanTenure)) {
				loanTenure = superAnnuationTenure;
			} else if (Integer.parseInt(superAnnuationTenure) >= Integer.parseInt(loanTenure)) {
				loanTenure = loanTenure;
			}
			Log.consoleLog(ifr,
					"superAnnuationTenure  " + superAnnuationTenure + "loanTenure : after vaidation " + loanTenure);
			// Code modified by Vandana 25-03-2024 Months Removed from loanTenure

			Log.consoleLog(ifr,
					superAnnuationTenure + "loanTenure : after slider difference vaidation check" + loanTenure);
			ifr.setValue("P_CB_PA_TENURE", loanTenure);
			ifr.setStyle("P_CB_PA_TENURE", "readonly", "true");
			String loanROI = pcm.mGetROICB(ifr);
			Log.consoleLog(ifr, "roi : " + loanROI);
			String ROI = loanROI;
			ifr.setValue("P_CB_PA_RATE_OF_INTEREST", ROI);
			ifr.setStyle("P_CB_PA_RATE_OF_INTEREST", "readonly", "true");
			String loanAmount = "-" + inprinciple;
			BigDecimal rate = new BigDecimal(loanROI);
			int tenure = Integer.parseInt(loanTenure);
			BigDecimal emicalc = pcm.calculateEMIPMT(ifr, loanAmount, rate, tenure);
			String emi = emicalc.toString();
			Log.consoleLog(ifr, "emi : " + emi);
			ifr.setValue("P_CB_PA_EMI_INPRNCPL", emi);
			String Query1 = ConfProperty.getQueryScript("PORTALFINDSLIDERVALUE").replaceAll("#WINAME#",
					ProcessInsanceId);
			List<List<String>> result = cf.mExecuteQuery(ifr, Query1, "Query ");
			String tenure1 = "";
			String loanAmount1 = "";
			if (result.size() > 0) {
				loanAmount1 = result.get(0).get(0);
				tenure1 = result.get(0).get(1);
				sliderValue = loanAmount1 + "," + tenure1;
			} else {
				loanAmount1 = ifr.getValue("P_CB_PA_LOAN_AMOUNT").toString();
				tenure1 = ifr.getValue("P_CB_PA_TENURE").toString();
				sliderValue = loanAmount1 + "," + tenure1;
			}
			Log.consoleLog(ifr, "sliderValue  : " + sliderValue);
		} catch (Exception e) {
			Log.consoleLog(ifr, "Error occured in autoPopulateInPrincipleDataCB " + e);
		}
		return sliderValue;
	}

	public void mOnChangeROITypeCP(IFormReference ifr, String Control, String Event, String value) {
		Log.consoleLog(ifr, "inside on Pension change ROI Type");
		plpc.onChangeROITypeCP(ifr);
	}

	public String mAccPortalCalculateNetIncomeCBVL(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside on Pension change ROI Type");
		return pcm.mCalculateNetIncome(ifr, "P_VL_OD_GrossSalary_CB", "P_VL_OD_DeductionFromSalary_CB",
				"P_VL_OD_NetIncome_CB", "P_VL_OD_GrossPension", "P_VL_OD_NetPension");
	}

	public String mAccPortalCheckWorkingExperienceCBVL(IFormReference ifr, String control, String event, String value) {
		return vlpc.mCheckWorkingExperienceCB(ifr, "P_VL_OD_ExperienceYear_CB", "P_VL_OD_OverAllExperience_CB");
	}

	/*
	 * public String profileValidationCB(IFormReference ifr, String control, String
	 * event, String value) { Log.consoleLog(ifr, "inside profileValidationCB");
	 * return vlpc.mprofileValidationCB(ifr, control);
	 * 
	 * }
	 */
	// added by Janani on 06-07-2024
	public String mOnChangeVehicleModel(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside mOnChangeVehicleModel portalcustomcode ");
		return vlpc.OnChangeVehicleModel(ifr);
	}

	// added by Janani on 06-07-2024
	public String mOnChangeProfileCB(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside mOnChangeProfileCB portalcustomcode ");
		return vlpc.OnChangeProfileCB(ifr);
	}

	public void OnChangeOccupationTypeCOBPension(IFormReference ifr, String Control, String Event, String value) {// Checked
		try {
			Log.consoleLog(ifr, "inside mOnChangeProfileCB portalcustomcode ");
			String subtype = "Pensioner";
			String occSubtype = ifr.getValue("P_CP_OD_Profile_COB").toString();
			if (occSubtype.equalsIgnoreCase("PEN")) {
				ifr.setStyle("P_CP_OD_TypeOfOccupation_COB", "disable", "true");
				Log.consoleLog(ifr, "Inside caluclateEMIFinalEligibility");
				ifr.addItemInCombo("P_CP_OD_TypeOfOccupation_COB", subtype);
				ifr.setValue("P_CP_OD_TypeOfOccupation_COB", subtype);

			} else {
				ifr.setValue("P_CP_OD_TypeOfOccupation_COB", "");
			}

			if (occSubtype.equalsIgnoreCase("PEN") || occSubtype.equalsIgnoreCase("Salaried")) {
				Log.consoleLog(ifr, "Inside retirement change");
				ifr.setValue("P_CP_OD_DATEOFSUPERANNUATION_COB", "");
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in mImplOnChangeSectionStateOutwardDocument" + e);
			Log.errorLog(ifr, "Exception in mImplOnChangeSectionStateOutwardDocument" + e);

		}
	}

//added by Logaraj on 08-07-2024   
	// added by ishwarya on 07-09-2024
	public String mAccPortalCalculateAnnualNetIncomeVL(IFormReference ifr, String control, String event, String value) {
		return pcm.mCalculateNetIncome(ifr, "P_VL_OD_GrossAnnualSalary", "P_VL_OD_AnnualDeductionFromSalary",
				"P_VL_OD_AnnualNetIncome", "P_VL_OD_GrossPension", "P_VL_OD_NetPension");
	}

	// added by Logaraj on 11-07-2024 for relationship type
	public void onChangeRelationTypeBorrower(IFormReference ifr, String control, String event, String value) {

		try {
			Log.consoleLog(ifr, "inside onChangeRelationTypeBorrower portalcustomcode ");
			String relationShiptype = ifr.getValue("P_CP_OD_Relationship_with_Borrower").toString();
			if (relationShiptype.equalsIgnoreCase("OTH")) {
				Log.consoleLog(ifr, "Inside Others");
				ifr.setStyle("P_CP_OD_RELATIONSHIP_BORROWER_OTHERS", "visible", "true");
			} else {
				ifr.setStyle("P_CP_OD_RELATIONSHIP_BORROWER_OTHERS", "visible", "false");
			}

		} catch (Exception e) {
			Log.consoleLog(ifr, "Exception in relationshiptype" + e);
			Log.errorLog(ifr, "Exception in relationshiptype" + e);
		}
	}

	// added by Sharon on 19-07-2024
	public String mOnchangeCostofVehicle(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "inside moOnchangeCostofVehicle portalcustomcode ");
		String CostofVehicle = vlpc.OnchangeCostofVehicle(ifr);
		return CostofVehicle;
	}

	// added by janani on 22-07-2024
	public String mAccPortalCalculateAnnualNetIncomeCBVL(IFormReference ifr, String control, String event,
			String value) {
		Log.consoleLog(ifr, "PortalCustomCude-->mAccPortalCalculateAnnualNetIncomeCBVL");
		return pcm.mCalculateNetIncome(ifr, "P_VL_OD_GrossAnnualSalary_CB", "P_VL_OD_AnnualDeductionFromSalary_CB",
				"P_VL_OD_AnnualNetIncome_CB", "P_VL_OD_GrossPension", "P_VL_OD_NetPension");
	}

	// added by janani on 22-07-2024
	public String onChangeOccTypeCBVL(IFormReference ifr, String control, String event, String value) {
		Log.consoleLog(ifr, "VLPortalCustomCode->onChangeOccTypeCBVL: ");
		return vlpc.onChangeOccTypeCBVL(ifr);
	}

}
