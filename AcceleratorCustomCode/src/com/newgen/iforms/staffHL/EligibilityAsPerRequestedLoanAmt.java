/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.staffHL;

import com.newgen.iforms.custom.IFormReference;

/**
 *
 * @author skalidindi
 */
public class EligibilityAsPerRequestedLoanAmt extends  EligibilityHandler{

    @Override
    public String handle(EligibilityContext context, IFormReference ifr) {
    double maxLimitOnSlider=Math.min(context.getHlAvailable(),context.getMaxLoanAsPerLTV());
    String amtReq=ifr.getValue("").toString();
    double amtReqD=0.0;
    if(amtReq.isEmpty() || amtReq.trim().length()==0){
        amtReqD=maxLimitOnSlider;
    }
    else {
      amtReqD=  Double.parseDouble(amtReq);
      if(amtReqD>maxLimitOnSlider){
          amtReqD=maxLimitOnSlider;
      }
    }
   
    return next.handle(context, ifr);
    }
    
}
