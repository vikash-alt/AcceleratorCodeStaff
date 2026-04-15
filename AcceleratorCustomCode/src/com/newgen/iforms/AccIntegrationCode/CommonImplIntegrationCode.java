/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.AccIntegrationCode;

import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author m_gupta
 */
public class CommonImplIntegrationCode {

    CommonFunctionality cf = new CommonFunctionality();

    public void setValidateKYCDetails(IFormReference ifr) {

        ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidationStatus", "Yes");
        ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidatedOn", cf.getCurrentDate(ifr));
        ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidatedBy", ifr.getUserName());
        ifr.setStyle("QNL_BASIC_INFO_CNL_KYC2_KYC_No", "disable", "true");
        ifr.setStyle("QNL_BASIC_INFO_CNL_KYC2_KYC_ID", "disable", "true");
        ifr.setStyle("AadharVaultRefId", "disable", "true");
        ifr.setStyle("BTN_KYC_Validate", "visible", "false");
    }

    public void clearValidateKYCDetails(IFormReference ifr) {
        ifr.setValue("QNL_BASIC_INFO_CNL_KYC2_ValidationStatus", "");
        ifr.setStyle("QNL_BASIC_INFO_CNL_KYC2_KYC_No", "disable", "false");
        ifr.setStyle("QNL_BASIC_INFO_CNL_KYC2_KYC_ID", "disable", "false");
        ifr.setStyle("BTN_KYC_Validate", "visible", "true");
    }

    public String kycVerified(IFormReference ifr) {
        String idType = "";
        JSONObject kycDetailsGridData = null;
        JSONArray kycDetailsGrid = ifr.getDataFromGrid("LV_KYC"); //LV_KYC
        Log.consoleLog(ifr, "kycDetailsGrid " + kycDetailsGrid);
        int count = ifr.getDataFromGrid("LV_KYC").size();
        Log.consoleLog(ifr, "count= " + count);
        for (int i = 0; i < count; i++) {
            kycDetailsGridData = (JSONObject) kycDetailsGrid.get(i);
            Log.consoleLog(ifr, "kycDetailsGridData:: " + kycDetailsGridData.toString());
            idType = kycDetailsGridData.get("ID Type").toString();
            Log.consoleLog(ifr, "idType= " + idType);
        }
        return idType;
    }

    public String getCurrentTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy hh.mm.ss aa");
        String formattedDate = dateFormat.format(new Date()).toString();
        System.out.println(formattedDate);
        return formattedDate;
    }

    // convert 24hrs timestamp to 12hrs 
    public String get12hrsDateTimeStamp(String inputDate) {
        String outputDate = "";
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy h:mm:ss.SSSSSSSSS a");
        //SimpleDateFormat outputFormat = new SimpleDateFormat("dd-mm-yyyy h:mi:ss.SSSSSSSSS a");
        try {
            Date date = inputFormat.parse(inputDate);
            outputDate = outputFormat.format(date);
            System.out.println(outputDate);
        } catch (java.text.ParseException e) {

        }
        return outputDate;
    }

    //validation for nationalID's
    public boolean isValidateID(String nationalID) {//Checked by Mayank
        String strPattern = "^[2-9][0-9]{11}$";
        boolean val = nationalID.matches(strPattern);
        return val;
    }

    //date format conversion .
    public String dateFormatConversion(String inputDateStr) {
        String outputDateStr = "";
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date date = inputFormat.parse(inputDateStr);
            outputDateStr = outputFormat.format(date);
            System.out.println("Converted Date: " + outputDateStr);
        } catch (ParseException e) {

        }
        return outputDateStr;
    }

    public String getAge(IFormReference ifr, String dob) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dob, formatter);
        LocalDate currentDate = LocalDate.now();
        Period period = Period.between(date, currentDate);
        int cal = period.getYears();
        String age = Integer.toString(cal);
        Log.consoleLog(ifr, "age " + age);
        return age;
    }
}
