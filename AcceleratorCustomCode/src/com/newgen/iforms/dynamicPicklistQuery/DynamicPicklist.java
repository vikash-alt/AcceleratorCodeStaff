/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.dynamicPicklistQuery;

import com.newgen.iforms.EControl;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.custom.IFormCustomHooks;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.util.List;

/**
 *
 * @author vishal.yadav
 */
public class DynamicPicklist extends IFormCustomHooks {

    CommonFunctionality cf = new CommonFunctionality();

    @Override
    public boolean picklistPreHook(String controlId, IFormReference ifr) {
        if (controlId.equalsIgnoreCase("QNL_COMMITTEE_CNL_COMMITTEE_DETAILS_EMPID")) {
            String usertype = (String) ifr.getValue("QNL_COMMITTEE_CNL_COMMITTEE_DETAILS_USERTYPE");
            Log.consoleLog(ifr, "QNL_COMMITTEE_CNL_COMMITTEE_DETAILS_USERTYPE :" + usertype);
            String Query = "";
            String maxlevel = ifr.getValue("DevMaxLevel").toString();
            String query = ConfProperty.getQueryScript("FindDevLevelName").replaceAll("#DEVLEVEL#", maxlevel);
            List<List<String>> result = cf.mExecuteQuery(ifr, query, "Query for FindDevLevelName:");
            String committeeName = "";
            if (result.size() > 0) {
                committeeName = result.get(0).get(0);
                query = ConfProperty.getQueryScript("Committee_Query").
                        replaceAll("#COMMITTEE#", committeeName).replaceAll("#BranchCode#", ifr.getValue("QL_SOURCINGINFO_BranchCode").toString());
                result = cf.mExecuteQuery(ifr, query, "To Check Entry of data");
                if (result.size() > 0) {
                    String PID = result.get(0).get(0);
                    if (usertype.equalsIgnoreCase("Head")) {
                        Query = ConfProperty.getQueryScript("HeadCommitteeQuery").replaceAll("#UPDATEDWINO#", PID);
                    } else if (usertype.equalsIgnoreCase("Zonal")) {
                        Query = ConfProperty.getQueryScript("ZonalCommitteeQuery").replaceAll("#UPDATEDWINO#", PID);
                    } else if (usertype.equalsIgnoreCase("Nodal")) {
                        Query = ConfProperty.getQueryScript("NodalCommitteeQuery").replaceAll("#UPDATEDWINO#", PID);
                    } else {
                        Query = ConfProperty.getQueryScript("queryOfficer").replaceAll("#PID#", PID);
                    }
                }
            }
            Log.consoleLog(ifr, "Query " + Query);
            try {
                ((EControl) ifr.getIFormControl(controlId)).getM_objPicklist().setM_strQuery(Query);
            } catch (Exception e) {
                Log.consoleLog(ifr, "Exception: " + e);
                Log.errorLog(ifr, "Exception: " + e);
            }
            return true;
        }
        return false;
    }

    /**
     *
     * @param controlId
     * @param objReference
     * @param searchString
     * @param colIndex
     */
    @Override
    public void picklistSearchHook(String controlId, IFormReference ifr, String searchString, int colIndex) {

    }
}
