/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.staffHL;

import com.newgen.iforms.custom.IFormReference;
import java.util.List;

/**
 *
 * @author skalidindi
 */
public class SchemeEligibilityCheck extends EligibilityHandler{

    @Override
    public String handle(EligibilityContext context,IFormReference ifr) {
      String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
      context.setProcessInstanceId(processInstanceId);
      String query="SELECT  NVL(total_hl_elig, 0) AS total_hl_elig, NVL(total_hl_util, 0) AS total_hl_util, NVL(total_hl_avail, 0) AS total_hl_avail,nvl(REQ_AMT_TOT_PLD,0) as REQ_AMT_TOT_PLD FROM slos_staff_home_trn WHERE winame = '"+processInstanceId+"'";
      List<List<String>> res=ifr.getDataFromDB(query);
      if(res.isEmpty()){
          return "Error, Not eligible";
      }
      double totalHlElg=Double.parseDouble(res.get(0).get(0));
      ifr.setValue("Elg_Per_Scale_Scheme", String.valueOf(totalHlElg));
      double totalHlUtil=Double.parseDouble(res.get(0).get(1));
      double totalHlAvail=Double.parseDouble(res.get(0).get(2));
     // double requestedAmt=Double.parseDouble(res.get(0).get(3));
      context.setHlAvailable(totalHlAvail);
      context.setHlUtilized(totalHlUtil);
      context.setHlEligibleAmt(totalHlElg);
     // context.setTotalRequestAmt(requestedAmt);
      if(totalHlAvail<=0){
          return "Error, Available loan amount is less than or equal to zero";
      }
      
      context.setIsEligible(true);
      return next.handle(context, ifr);
    }
    
}
