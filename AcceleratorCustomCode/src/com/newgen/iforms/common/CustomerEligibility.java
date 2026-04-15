/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.common;

import com.newgen.dlp.integration.brm.BRMCommonRules;
import com.newgen.dlp.integration.cbs.Advanced360EnquiryData;
import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.Log;
import com.newgen.iforms.vl.VLPortalCustomCode;
import java.util.HashMap;
import org.json.simple.parser.ParseException;

/**
 *
 * @author ahmed.zindha
 */
public class CustomerEligibility {

    PortalCommonMethods pcm = new PortalCommonMethods();

    public String checkCustomerEligibility(IFormReference ifr) throws ParseException {

        //1.Collect Customer Data
        //2.Collect Customer Default Data from 360 APi 
        //3.Collect Customer Default Score from fintech API`s
        //4.check CIC score & Eligibility
        //5.Check ScoreCard
        //6.Update the Eligibility & CRG
        //7.Update CRG/Risk Rate
        if (!collectCustomerInfo(ifr).equalsIgnoreCase(RLOS_Constants.ERROR)) {
            if (!getCustomerDefaultData(ifr).equalsIgnoreCase(RLOS_Constants.ERROR)) {
                if (!checkCICScore(ifr).equalsIgnoreCase(RLOS_Constants.ERROR)) {
                    if (!checkCICEligibility(ifr).equalsIgnoreCase(RLOS_Constants.ERROR)) {
                        if (!checkScoreCard(ifr).equalsIgnoreCase(RLOS_Constants.ERROR)) {
                            if (!updateEligibility(ifr).equalsIgnoreCase(RLOS_Constants.ERROR)) {
                                if (!updateCRGRiskRate(ifr).equalsIgnoreCase(RLOS_Constants.ERROR)) {
                                }
                            }
                        }
                    }
                }
            }
        }

        return RLOS_Constants.ERROR;

    }

    private String collectCustomerInfo(IFormReference ifr) {
        String mobileNumber = "";
        HashMap<String, String> customerdetails = new HashMap<>();
        customerdetails.put("MobileNumber", mobileNumber);
        CustomerAccountSummary cas = new CustomerAccountSummary();
        String status = cas.getCustomerAccountSummary(ifr, customerdetails);
        return status;
    }

    private String checkCICScore(IFormReference ifr) throws ParseException {
//Commented by ahmed don 12-06-2024 for code notbeing used. Need to finetune Aadhar No logic
        /*
        String mobileNumber = "";
        HashMap<String, String> customerdetails = new HashMap<>();
        customerdetails.put("MobileNumber", mobileNumber);
        CustomerAccountSummary cas = new CustomerAccountSummary();
        String aadharNum = cas.getAadharCustomerAccountSummary(ifr, customerdetails);

        
        
        
        
        VLPortalCustomCode vl = new VLPortalCustomCode();
        String cb1 = vl.mCallBureau(ifr, "CB", aadharNum, "CB");
        if (cb1.contains(RLOS_Constants.ERROR)) {
            return pcm.returnError(ifr);
        }

        String cb2 = vl.mCallBureau(ifr, "EX", aadharNum, "CB");
        if (cb2.contains(RLOS_Constants.ERROR)) {
            return pcm.returnError(ifr);
        }
        return RLOS_Constants.ERROR;
        
        
         */
        return RLOS_Constants.ERROR;
    }

    private String checkCICEligibility(IFormReference ifr) {

        BRMCommonRules objbcr = new BRMCommonRules();
        String productCode = pcm.mGetProductCodeVL(ifr, ifr.getValue("P_VL_OD_LOAN_PURPOSE").toString());
        String subProductCode = "SUPVL";
        String decision = objbcr.checkCICScore(ifr, productCode, subProductCode, "CB", "B");
        //           mCheckCIBILScoreknockOff(ifr, "CB", subProductCode, productCode,"");
        Log.consoleLog(ifr, "decision1/CB::" + decision);
        if (decision.contains(RLOS_Constants.ERROR)) {
            return RLOS_Constants.ERROR;
        } else if (decision.equalsIgnoreCase("Approve")) {

        }
        return RLOS_Constants.ERROR;
    }

    private String getCustomerDefaultData(IFormReference ifr) {

        String customerId = "";
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        Advanced360EnquiryData API360 = new Advanced360EnquiryData();
        String response = API360.executeAdvanced360Inquiry(ifr, processInstanceId, customerId, "VL");
        Log.consoleLog(ifr, "response==>" + response);
        return response;
    }

    private String checkScoreCard(IFormReference ifr) {
        return "";
    }

    private String updateEligibility(IFormReference ifr) {
        return "";
    }

    private String updateCRGRiskRate(IFormReference ifr) {
        return "";
    }
}
