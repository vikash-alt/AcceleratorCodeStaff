/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.staffHL;

import com.newgen.dlp.integration.common.KnockOffValidator;
import com.newgen.dlp.integration.common.Validator;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author skalidindi
 */
public class EligibilityAsPerLTVHL  extends  EligibilityHandler{

    @Override
    public String handle(EligibilityContext context, IFormReference ifr) {
        Validator valid=new KnockOffValidator("");
        JSONObject json=new JSONObject();
        String costOfPlot=ifr.getValue("P_HL_CD_PLOTCOST").toString();
        String costOfConstruction=ifr.getValue("P_HL_CD_CONSTRUCTIONCOST").toString();
        String reqMinMargin=ifr.getValue("DownpayCollatSHL").toString();
        String productCode="";
        String subProduct="";
        String totalReqAmt="";
        String designation="";
        String roiType="";
        String probation="";
        String ltv="";
        
        json.put("key", costOfPlot);
        double costOfPlotD=Double.parseDouble(valid.getValue(ifr, json, "key", "0"));
        //context.setCostOfPlot(costOfPlotD);
        
        json.put("key", costOfConstruction);
       double costOfConstructionD=Double.parseDouble(valid.getValue(ifr, json, "key", "0"));
      // context.setCostOfConstruction(costOfConstructionD);
       
       double totalCostOfProperty=costOfPlotD+costOfConstructionD;
       //context.setTotalCostProperty(totalCostOfProperty);
        
       json.put("key", reqMinMargin);
        double reqMinMarginD=Double.parseDouble(valid.getValue(ifr, json, "key", "0"));
       
        String productCodeQuery="select productcode,designation,REQ_AMT_TOT_PLD,ROI_TYPE,probation,trim(HL_PRODUCT) from  slos_staff_home_trn ssht left join  slos_home_purpose shp on trim(hl_purpose) =trim(PURPOSENAME) where winame='"+context.getProcessInstanceId()+"'";
        List<List<String>> productCodeRes=ifr.getDataFromDB(productCodeQuery);
        if(productCodeRes.isEmpty()){
            return "Error, Product code not found";
        }
        productCode=productCodeRes.get(0).get(0);
        designation=productCodeRes.get(0).get(1);
        totalReqAmt=productCodeRes.get(0).get(2);
        roiType=productCodeRes.get(0).get(3);
        probation=productCodeRes.get(0).get(4);
        subProduct=productCodeRes.get(0).get(5);
        
         String ltvQuery="select case when to_number('"+totalReqAmt+"') < to_number(param_amount) "
                    + "then roi_lessthan_40L else roi_greaterthan_40L end as roi,max_loan_amt,ltv"
                    + " from staff_hl_prod_des_matrix where upper(designation)=upper('"+designation.trim()+"')"
                    + " and trim(sub_product_code_cbs)='"+productCode.trim()+"' and upper(trim(sub_product))='"+subProduct.trim().toUpperCase()+"' and "
                    + "upper(probation_tag)=upper('"+probation+"') and upper(roi_type)=upper('"+roiType+"') ";
            Log.consoleLog(ifr, "query veh loan===>" + ltvQuery);
         List<List<String>> ltvRes=ifr.getDataFromDB(ltvQuery);
         if(ltvRes.isEmpty()){
             return "Error, Technical error";
         }
         int roi=Integer.parseInt(ltvRes.get(0).get(0));
         //context.setRoi(roi);
         ltv=ltvRes.get(0).get(2);
         
         double value = Double.parseDouble(ltv) / 100.0;
         double finalVal= Math.round(value * 100.0) / 100.0;
         double minMargin=(1.00-finalVal)*totalCostOfProperty;
         if(reqMinMarginD>totalCostOfProperty|| reqMinMarginD<=0){
             return "Error, minimum margin cannot be greater than total cost of property";
         }
         double maxLoanAsPerLTV=totalCostOfProperty-reqMinMarginD;
         
         if(maxLoanAsPerLTV<=0){
             return "Error, Max loan as per ltv cannot be less than or equal to zero.";
         }
         ifr.setValue("P_HL_CD_Eligible_amount",String.valueOf(maxLoanAsPerLTV));
         ifr.setValue("P_HL_CD_LTV",ltv);
        // context.setMaxLoanAsPerLTV(maxLoanAsPerLTV);
        return "success";
    }
    
}
