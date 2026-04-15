/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.AccAPIS;

import com.newgen.iforms.acceleratorCode.CommonMethods;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import java.io.File;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Administrator
 */
public class APICommonMethod {

    CommonFunctionality cf = new CommonFunctionality();
    CommonMethods cm = new CommonMethods();

    public void mBureauGridData(IFormReference ifr, String applicantType) {
        //String localPath = "C:\\Newgen\\jboss-eap-7.2\\bin\\CB_Report.pdf";
        String localPath = "C:\\Newgen\\EAP 7.3\\bin\\CB_Report.pdf";

        Log.consoleLog(ifr, "Inside try of add file");
        File file1 = null;
        String docIndex = "";
        file1 = new File(localPath);
        Log.consoleLog(ifr, "After File 1");
        Log.consoleLog(ifr, "FolderID::" + ifr.getObjGeneralData().getM_strFolderId());
        docIndex = cm.addDocument(ifr, ifr.getObjGeneralData().getM_strFolderId(), file1, "127.0.0.1", "8080", "I", "CB Report", "CB_DOCXML");
        Log.consoleLog(ifr, "docIndex-" + docIndex);
        // ResJsonData.put("QNL_CB_Details_Document_Name", docIndex);
        //   QNL_CB_Details.Document_Name
        ifr.setValue("CB_DOCID", docIndex);
        JSONArray ResJson = new JSONArray();
        JSONObject ResJsonData = new JSONObject();
        ResJsonData.put("QNL_CB_Details_Applicant_Type", applicantType);
        ResJsonData.put("QNL_CB_Details_Report_Enquiry_Date", cf.getCurrentDate(ifr));
        ResJsonData.put("QNL_CB_Details_CB_Type", ifr.getValue("DD_CB_BUREAU_ID").toString());
        ResJsonData.put("QNL_CB_Details_CB_Score", "747");
        ResJsonData.put("QNL_CB_Details_Remarks", "Ok");
        ResJsonData.put("QNL_CB_Details_Document_Name", docIndex);
        ResJson.add(ResJsonData);
        Log.consoleLog(ifr, "Data to Add in Bureau:" + ResJson);
        ifr.addDataToGrid("ALV_CB_Details", ResJson);
    }
}
