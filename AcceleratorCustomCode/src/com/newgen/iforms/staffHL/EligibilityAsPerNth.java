/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.staffHL;

import com.newgen.dlp.integration.common.KnockOffValidator;
import com.newgen.dlp.integration.common.Validator;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.hrms.HRMSPortalCustomCode;
import com.newgen.iforms.properties.Log;
import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author skalidindi
 */
public class EligibilityAsPerNth extends EligibilityHandler{

    @Override
    public String handle(EligibilityContext context, IFormReference ifr) {
        Validator valid=new KnockOffValidator("");
        String maxAmtLTVQuery="select eligible_amount from los_cam_collateral_details where pid='"+context.getProcessInstanceId()+"'";
         List<List<String>> maxLTVRes=ifr.getDataFromDB(maxAmtLTVQuery);
         Log.consoleLog(ifr, "maxAmtLTVQuery==="+maxAmtLTVQuery);
         if(maxLTVRes.isEmpty()){
             return "Error, Technical error";
         }
         context.setMaxLoanAsPerLTV(Double.parseDouble(maxLTVRes.get(0).get(0)));
        String query="select  gross_salary,pension,co_borrower_income,STATUTORY_DEDUCTIONS,LOAN_DEDUCTIONS,otherDed,roi from slos_staff_home_trn where winame='"+context.getProcessInstanceId()+"'";
       List<List<String>> res=ifr.getDataFromDB(query);
       Log.consoleLog(ifr, "query==="+query);
        JSONObject json=new JSONObject();
       if(res.isEmpty()){
           return "Error, Technical error.";
       }
       
       json.put("key", res.get(0).get(0));
       double grossSalary=Double.parseDouble(valid.getValue(ifr, json, "key", "0.0"));
       Log.consoleLog(ifr, "grossSalary==="+grossSalary);
      json.put("key", res.get(0).get(1));
      double pension=Double.parseDouble(valid.getValue(ifr, json, "key", "0.0"));
       Log.consoleLog(ifr, "pension==="+pension);
       
      json.put("key", res.get(0).get(2));
      double coBorrowerAmt=Double.parseDouble(valid.getValue(ifr, json, "key", "0.0"));
      Log.consoleLog(ifr, "coBorrowerAmt==="+coBorrowerAmt);
      
      json.put("key", res.get(0).get(3));
      double statDed=Double.parseDouble(valid.getValue(ifr, json, "key", "0.0"));
      Log.consoleLog(ifr, "statDed==="+statDed);
      
      
      json.put("key", res.get(0).get(4));
      double loanDed=Double.parseDouble(valid.getValue(ifr, json, "key", "0.0"));
     Log.consoleLog(ifr, "loanDed==="+loanDed);
     
      json.put("key", res.get(0).get(5));
      double otherDed=Double.parseDouble(valid.getValue(ifr, json, "key", "0.0"));
      
      Log.consoleLog(ifr, "otherDed==="+otherDed);
     // double roi=Double.parseDouble(res.get(0).get(5).trim());
      
      double minNth=(0.3*(grossSalary+pension))+(0.3*coBorrowerAmt);
      
      Log.consoleLog(ifr, "minNth==="+minNth);
      if(minNth<=0.0){
          return "Error, Minimum nth is cannnot be less tha or equals zero";
      }
      double netSalAftDed=grossSalary+pension+coBorrowerAmt-(statDed+loanDed+otherDed);
      Log.consoleLog(ifr, "netSalAftDed==="+netSalAftDed);
      String productCodeQuery="select sub_product_code, trim(sub_product) from slos_staff_home_trn ssht join slos_home_product_sheet shps on trim(sub_product)=trim(hl_product) where winame='"+context.getProcessInstanceId()+"'";
        List<List<String>> productCodeRes=ifr.getDataFromDB(productCodeQuery);
        Log.consoleLog(ifr, "productCodeRes==="+productCodeRes);
        if(productCodeRes.isEmpty()){
            return "Error, Product code not found";
        }
    String  subProductCode=productCodeRes.get(0).get(0);
    String productCode=productCodeRes.get(0).get(1);
    String minMaxReqTenQuery="select min_tenure__,max_tenure_,principal_tenure_factor,interest_tenure_factor"
             + " from slos_home_product_sheet where trim(sub_product)= '"+productCode+"' and trim(sub_product_code)='"+subProductCode+"' ";
    List<List<String>> minMaxReqTenRes=ifr.getDataFromDB(minMaxReqTenQuery);
     Log.consoleLog(ifr, "minMaxReqTenRes==="+minMaxReqTenRes);
    if(minMaxReqTenRes.isEmpty()){
        return "Error, Technical error.";
    }
    int  minTen=Integer.parseInt(minMaxReqTenRes.get(0).get(0));
    int  maxTen=Integer.parseInt(minMaxReqTenRes.get(0).get(1));
    int principalTenure=Integer.parseInt(minMaxReqTenRes.get(0).get(2));
    int interestTenFact=Integer.parseInt(minMaxReqTenRes.get(0).get(3));
    Log.consoleLog(ifr, "minTen==="+minTen);
    Log.consoleLog(ifr, "maxTen==="+maxTen);
    Log.consoleLog(ifr, "principalTenure==="+principalTenure);
    Log.consoleLog(ifr, "interestTenFact==="+interestTenFact);
    String reqTen=ifr.getValue("").toString();
     json.put("key", reqTen);
      int reqTenInt=Integer.parseInt(valid.getValue(ifr, json, "key", minMaxReqTenRes.get(0).get(1)));
    Log.consoleLog(ifr, "reqTenInt==="+reqTenInt);
      if(reqTenInt<minTen || reqTenInt>maxTen){
        return "Error, Minimum tenure should be = "+minTen+" and maximum tenure you avail upto max = "+maxTen+". ";
    }
       
    
        String productCodeQueryForRoi="select productcode,designation,REQ_AMT_TOT_PLD,ROI_TYPE,probation,trim(HL_PRODUCT) from  slos_staff_home_trn ssht left join  slos_home_purpose shp on trim(hl_purpose) =trim(PURPOSENAME) where winame='"+context.getProcessInstanceId()+"'";
        List<List<String>> productCodeResForRoi=ifr.getDataFromDB(productCodeQueryForRoi);
        if(productCodeRes.isEmpty()){
            return "Error, Product code not found";
        }
        productCode=productCodeResForRoi.get(0).get(0);
      String  designation=productCodeResForRoi.get(0).get(1);
       String totalReqAmt=productCodeResForRoi.get(0).get(2);
      String  roiType=productCodeResForRoi.get(0).get(3);
      String  probation=productCodeResForRoi.get(0).get(4);
      String  subProduct=productCodeResForRoi.get(0).get(5);
        
         String roiQuery="select case when to_number('"+totalReqAmt+"') < to_number(param_amount) "
                    + "then roi_lessthan_40L else roi_greaterthan_40L end as roi,max_loan_amt,ltv"
                    + " from staff_hl_prod_des_matrix where upper(designation)=upper('"+designation.trim()+"')"
                    + " and trim(sub_product_code_cbs)='"+productCode.trim()+"' and upper(trim(sub_product))='"+subProduct.trim().toUpperCase()+"' and "
                    + "upper(probation_tag)=upper('"+probation+"') and upper(roi_type)=upper('"+roiType+"') ";
           
         List<List<String>> ltvRes=ifr.getDataFromDB(roiQuery);
         if(ltvRes.isEmpty()){
             return "Error, Technical error";
         }
         int roi=Integer.parseInt(ltvRes.get(0).get(0));
    context.setRoi(roi);
    
    
    double eligbilAsPerNth=(netSalAftDed-minNth)/ calculatePMT(reqTenInt,context.getRoi())* 100000;
    Log.consoleLog(ifr, "eligbilAsPerNth==="+eligbilAsPerNth);
    double eligibileLoan=Math.min(eligbilAsPerNth, Math.min(context.getHlAvailable(), context.getMaxLoanAsPerLTV()));
    Log.consoleLog(ifr, "eligibileLoan==="+eligibileLoan);
    double amtReqD=0.0;
    double maxLimitOnSlider=Math.min(context.getHlAvailable(),context.getMaxLoanAsPerLTV());
    Log.consoleLog(ifr, "maxLimitOnSlider==="+maxLimitOnSlider);
    String amtReq=ifr.getValue("").toString();
    if(amtReq.isEmpty() || amtReq.trim().length()==0){
        amtReqD=maxLimitOnSlider;
    }
    else {
      amtReqD=  Double.parseDouble(amtReq);
      if(amtReqD>maxLimitOnSlider){
          amtReqD=maxLimitOnSlider;
      }
    }
    Log.consoleLog(ifr, "amtReqD==="+amtReqD);
    if(eligibileLoan>amtReqD){
        return "Error, Cannot avail amount more than = "+eligibileLoan;
    }
    double inPrincipal=Math.min(eligibileLoan, amtReqD);
    Log.consoleLog(ifr, "inPrincipal==="+inPrincipal);
     return "success";
    }
    private  double calculatePMT(int tenure, double roi) {
		double monthlyInterestRate = roi / 100 / 12;
		double numerator = monthlyInterestRate * 100000;
		double denominator = 1 - Math.pow(1 + monthlyInterestRate, -tenure);

		return numerator / denominator;
	}

}
