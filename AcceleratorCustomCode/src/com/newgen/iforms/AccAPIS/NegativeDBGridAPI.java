/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.AccAPIS;

import com.newgen.iforms.custom.IFormReference;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author Administrator
 */
public class NegativeDBGridAPI {

    public String mCallNegativeDefaulterAPI(IFormReference ifr, String applicantType) {
        JSONArray ResJson = new JSONArray();
        JSONObject ResJsonData = new JSONObject();
        ResJsonData.put("QNL_NEGATIVE_DB_DEFAULTER_PartyType", applicantType);
        ResJsonData.put("QNL_NEGATIVE_DB_DEFAULTER_NegativeDb", "Not Found");
        ResJsonData.put("QNL_NEGATIVE_DB_DEFAULTER_DefaulterList", "Not Found");
        ResJson.add(ResJsonData);
        ifr.addDataToGrid("ALV_NEGATIVE_DB_DEFAULTER", ResJson);
        return "SUCCESS";
    }
}
