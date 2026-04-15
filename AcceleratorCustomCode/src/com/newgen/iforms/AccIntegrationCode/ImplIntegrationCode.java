/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.AccIntegrationCode;

import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.dynamicPicklistQuery.DynamicPicklist;
import java.util.List;
import com.newgen.iforms.properties.Log;
import org.json.simple.JSONObject;

/**
 *
 * @author m_gupta
 */
public class ImplIntegrationCode extends DynamicPicklist {

    CommonFunctionality cf = new CommonFunctionality();
    CommonImplIntegrationCode cic = new CommonImplIntegrationCode();

    public String mImplCIFEnquiryDeatil(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "In mImplCIFEnquiryDeatil ");
        //call cutom method .
        return "";
    }

    public void mImplPartyType(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mImplPartyType");
        try {
            String partyType = ifr.getValue("CTRID_PD_PARTYTYPE").toString();
            if (partyType.equalsIgnoreCase("No")) {

            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mImplPartyType" + e.getMessage());
        }
    }

    public String mImplClickCollateralCreation(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mCreateCollateral");
        String query = "select LOANACCOUNTNO from LOS_NL_LOAN_ACC_CREATION where pid='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
        List<List<String>> resultLink = cf.mExecuteQuery(ifr, query, "Loan Account:");
        String AccountNumber = "";
        if (resultLink.size() > 0) {
            AccountNumber = resultLink.get(0).get(0);
        } else {
            JSONObject returnJSON = new JSONObject();
            returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", "Please create Loan Account!"));
            return returnJSON.toString();
        }
        query = "select B.CIF_NUMBER,A.FULLNAME from LOS_NL_BASIC_INFO A inner join LOS_NL_CIF_CREATION B on "
                + "A.INSERTIONORDERID=B.APPLICANT_TYPE where A.APPLICANTTYPE='B' and A.pid='"
                + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
        resultLink = cf.mExecuteQuery(ifr, query, "CIF Borrower:");
        String cifNumber = "";
        String customerName = "";
        if (resultLink.size() > 0) {
            cifNumber = resultLink.get(0).get(0);
            customerName = resultLink.get(0).get(1);
        } else {
            JSONObject returnJSON = new JSONObject();
            returnJSON.put("showMessage", cf.showMessage(ifr, "ControlId", "error", "CIF Not Present!"));
            return returnJSON.toString();
        }
        query = "select COLLATERALSTAT,INSURANCEREQDIND,COLLATERALSHORTDESCR,COMMENCEMENTDATE,EXPIRYDATE,"
                + "GRACE,REVIEWDATE,PRIORITY,SECURITYVALUATION,OTHERBANKSCLAIMS,DESCRIPTION,ROCFLAG,ROCCREATEDDATE"
                + ",ROCFILLEDDATE,CHARGETYPE,SAFECUSTODYACNTNO,F_KEY from LOS_NL_COLLATERAL_DETAILS where "
                + "PID = '" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
        List<List<String>> cifresult = cf.mExecuteQuery(ifr, query, "Fetch Collateral Creation Details:");
        if (cifresult.size() == 0) {
            JSONObject returnJSON = new JSONObject();
            returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", "Please Enter Collateral Details in Above Grid!"));
            return returnJSON.toString();
        }
        try {
            String getCollateralQuery = "select FirstAPIFlag,SecondAPIFlag,insertionorderid,TYPEOFSECURITY,"
                    + "SUBTYPEOFSECURITY,ThirdAPIFlag,collateralId,TYPEOFSECURITY,TYPEOFPROPERTY,NAMEOFPROJECTSOCIETYPROPERTY,"
                    + "PLOTSURVEYCTSNO,PLOTSURVEYCTSNO,FLATHOUSENO,BUILDINGWING,LOCATIONOFPROPERTY,PROPERTYADDLINE1,"
                    + "DISTRICT,STATE,PINCODE,PINCODE,STATUSOFPROPERTY,TYPEOFPROPERTY,TOTALPROJECTCOST,"
                    + "DATEOFAGREEMENTSALEDEED,REALIZABLEVALUEOFPROP,DATEOFVALUATION,MARGIN"
                    + " from LOS_NL_PRIMARYSECURITY WHERE PID = '" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
            Log.consoleLog(ifr, "getCollateralQuery::" + getCollateralQuery);
            List<List<String>> collateralList = ifr.getDataFromDB(getCollateralQuery);
            Log.consoleLog(ifr, "collateralList::" + collateralList);
            String collateralno = "";
            if (collateralList.size() > 0) {
                for (int i = 0; i < collateralList.size(); i++) {
                    collateralno = collateralList.get(i).get(6);
                    if (!collateralList.get(i).get(0).equalsIgnoreCase("Y")) {
                        /*CreateCollateralAPI cca = new CreateCollateralAPI();
                        String returnVal = "";
                        //cca.setCreateCollateralAPI(ifr, collateralList, i, AccountNumber, cifNumber, "C", collateralno);
                        if (!(returnVal.equalsIgnoreCase(""))) {
                            return returnVal;
                        }*/
                    }
                    query = "select collateralId from LOS_NL_PRIMARYSECURITY where INSERTIONORDERID='" + collateralList.get(i).get(2)
                            + "' and pid='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
                    resultLink = cf.mExecuteQuery(ifr, query, "CIF Borrower:");
                    if (resultLink.size() > 0) {
                        collateralno = resultLink.get(0).get(0);
                    }
                    if (!collateralList.get(i).get(1).equalsIgnoreCase("Y")) {
                        /*CreateCollateralImmovableMovableAPI cca = new CreateCollateralImmovableMovableAPI();
                        String returnVal = cca.setCreateCollateralImmovableMovableAPI(ifr, collateralList, i, AccountNumber, cifNumber, "C", collateralno, customerName);
                        if (!(returnVal.equalsIgnoreCase(""))) {
                            return returnVal;
                        }*/
                    }
                    if (!collateralList.get(i).get(5).equalsIgnoreCase("Y")) {
                        /*SecurityAuthrizeAPI cca = new SecurityAuthrizeAPI();
                        String returnVal = cca.setSecurityAuthrizeAPI(ifr, collateralList, i, collateralno);
                        if (!(returnVal.equalsIgnoreCase(""))) {
                            return returnVal;
                        }*/
                    }
                }
            } else {
                JSONObject returnJSON = new JSONObject();
                returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", "No Collateral details Found!"));
                returnJSON.put("saveWorkitem", "true");
                return returnJSON.toString();
            }
            JSONObject returnJSON = new JSONObject();
            returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", "Collateral Created and Updated Successfully!"));
            returnJSON.put("refreshFrame", cf.refreshFrame("F_CollateralCreation_Details"));
            returnJSON.put("saveWorkitem", "true");
            return returnJSON.toString();
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mCreateCollateral :: " + e);
            Log.errorLog(ifr, "Exception in mCreateCollateral :: " + e);
        }
        JSONObject returnJSON = new JSONObject();
        returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", AcceleratorConstants.USERMESSAGE));
        returnJSON.put("saveWorkitem", "true");
        return returnJSON.toString();
    }

    public String mImplClickLoanDisbursement(IFormReference ifr, String Control, String Event, String value) {
        Log.consoleLog(ifr, "Inside mLoanDisbursement Method DisbursementCheck:");
        String query = "select * from LOS_NL_PRIMARYSECURITY WHERE ThirdAPIFlag='Y' and "
                + "PID = '" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
        List<List<String>> collateralList = cf.mExecuteQuery(ifr, query, "collateralList:");
        if (collateralList.isEmpty()) {
            JSONObject returnJSON = new JSONObject();
            returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", "Please create and Update Collateral!"));
            return returnJSON.toString();
        }
        String Result = "";
        try {
            Log.consoleLog(ifr, "Inside mLoanDisbursement");
            int RowIndex = Integer.parseInt(value);
            Log.consoleLog(ifr, "RowIndex " + RowIndex);

            String paymentMode = ifr.getTableCellValue("ALV_BENEFICIARY_DETAILS", RowIndex, "QNL_BENEFICIARY_DETAILS_PaymentMode").toString();
            Log.consoleLog(ifr, "paymentMode:" + paymentMode);
            String DisbFlag = (String) ifr.getTableCellValue("ALV_BENEFICIARY_DETAILS", RowIndex, "QNL_BENEFICIARY_DETAILS_Status");
            Log.consoleLog(ifr, "DisbFlag::" + DisbFlag);
            if (DisbFlag.equalsIgnoreCase("Success")) {
                JSONObject returnJSON = new JSONObject();
                returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", "Already Disbursed!"));
                return returnJSON.toJSONString();
            }
            String Disbamount = (String) ifr.getTableCellValue("ALV_BENEFICIARY_DETAILS", RowIndex, "QNL_BENEFICIARY_DETAILS_Amount");
            Log.consoleLog(ifr, "Disbamount:" + Disbamount);
            query = "select LOANACCOUNTNO from LOS_NL_LOAN_ACC_CREATION where pid='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
            List<List<String>> resultLink = cf.mExecuteQuery(ifr, query, "Loan Account:");
            String accountId = "";
            if (resultLink.size() > 0) {
                accountId = resultLink.get(0).get(0);
            } else {
                JSONObject returnJSON = new JSONObject();
                returnJSON.put("showMessage", cf.showMessage(ifr, "", "error", "Please create Loan Account!"));
                return returnJSON.toString();
            }
            String beneficiaryName = ifr.getTableCellValue("ALV_BENEFICIARY_DETAILS", RowIndex, "QNL_BENEFICIARY_DETAILS_BeneficiaryName").toString();
            query = "select FullName from LOS_NL_BASIC_INFO where insertionorderid='" + beneficiaryName + "' and pid='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "'";
            resultLink = cf.mExecuteQuery(ifr, query, "Loan Account:");
            String benename = "";
            if (resultLink.size() > 0) {
                benename = resultLink.get(0).get(0);
            }
            if (!(DisbFlag.equalsIgnoreCase("Success"))) {
                if (paymentMode.equalsIgnoreCase("NEFT/RTGS")) {
                    /*disbursmentByDDChequeAPI IDONA = new disbursmentByDDChequeAPI();
                    String fundTransfer = "";
                    //IDONA.mCallDisbursmentByDDChequeAPI(ifr, accountId, Disbamount, benename);
                    if (!(fundTransfer.equalsIgnoreCase(""))) {
                        return fundTransfer;
                    }*/
                } else if (paymentMode.equalsIgnoreCase("Account Transfer")) {
                    /*disbursmentByTransaferAPI IDONA = new disbursmentByTransaferAPI();
                    String accountId1 = ifr.getTableCellValue("ALV_BENEFICIARY_DETAILS", RowIndex, "QNL_BENEFICIARY_DETAILS_FromAccount").toString();
                    String fundTransfer = IDONA.mCallDisbursmentByTransaferAPI(ifr, accountId1, Disbamount, accountId);
                    if (!(fundTransfer.equalsIgnoreCase(""))) {
                        return fundTransfer;
                    }*/
                }
            }
            ifr.setTableCellValue("ALV_BENEFICIARY_DETAILS", RowIndex, 8, "Success");
            ifr.setTableCellValue("ALV_BENEFICIARY_DETAILS", RowIndex, 9, cf.getCurrentDate(ifr));
            Result = "Disbursement Success";
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception in mLoanDisbursement :: " + e);
            Log.errorLog(ifr, "Exception in mLoanDisbursement::" + e);
        }
        return Result;
    }
}
