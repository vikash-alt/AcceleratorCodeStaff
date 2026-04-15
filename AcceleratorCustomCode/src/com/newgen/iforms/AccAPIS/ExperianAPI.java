/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.AccAPIS;

import com.newgen.iforms.custom.IFormReference;

/**
 *
 * @author Administrator
 */
public class ExperianAPI {

    public String mCallExperianAPI(IFormReference ifr, String applicantType) {
        APICommonMethod acm = new APICommonMethod();
        acm.mBureauGridData(ifr, applicantType);
        return "SUCCESS";
    }
}
