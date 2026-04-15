/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.nesl;

import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.json.simple.JSONObject;

/**
 *
 * @author ahmed.zindha
 */
public class EsignIntegrationChannel {

    EsignCreateRequest NCSR = new EsignCreateRequest();
    ArrayList Files = new ArrayList();
    CommonFunctionality cf = new CommonFunctionality();

    public String redirectNESLRequest(IFormReference ifr, String ProductType, String eSignMode, String loanType) {
        Log.consoleLog(ifr, "Inside Redirect_NESLRequest.....");

       //String neslFlagEnabled = ConfProperty.getCommonPropertyValue("NESLENABLE");
       String neslFlagEnabled = "Y";
        Log.consoleLog(ifr, "NESL Enabled ?====>" + neslFlagEnabled);
        Log.consoleLog(ifr, "neslFlagEnabled?==>" + neslFlagEnabled.equalsIgnoreCase("Y"));
        if ((neslFlagEnabled.equalsIgnoreCase("Y"))
                || (neslFlagEnabled.equalsIgnoreCase("YES"))) {

            String strIniPath = System.getProperty("user.dir") + File.separator + "NESL_Config" + File.separator + "Configuation.ini";
            File ini = new File(strIniPath);
            try (FileInputStream fInputStream = new FileInputStream(ini);) {

                //  String ProcessInstanceId = "LOS-00000000000003470";
                String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

                Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
                Properties property = new Properties();
                String Filepath = "";
                String SigningFiles = "";
                property.load(fInputStream);
                Filepath = property.getProperty("Filepath");
                SigningFiles = property.getProperty(ProductType + "_FileOrder");
                String Path = Filepath + File.separator + ProcessInstanceId;
                Log.consoleLog(ifr, "Path==>" + Path);
                getFileList(Path);
                Log.consoleLog(ifr, "Files==>" + Files.size());
                if (!Files.isEmpty()) {
                    Log.consoleLog(ifr, "Files were present on the specified directory..");
                    String Status = NCSR.createNESLRequest(ifr, SigningFiles, Path, eSignMode, ProductType,loanType);
                    Log.consoleLog(ifr, "Status==>" + Status);

//                    if (eSignMode.equalsIgnoreCase("eStamping")) {
//                        Status = NCSR.createNESLRequest(ifr, SigningFiles, Path, eSignMode, ProductType);
//                        Log.consoleLog(ifr, "Status==>" + Status);
//                    } else {
//                        Status = NCSR.createNESLRequest_eSigning(ifr, SigningFiles, Path, eSignMode, ProductType);
//                        Log.consoleLog(ifr, "Status==>" + Status);
//                    }
                    return Status;
                } else {
                    Log.consoleLog(ifr, "No files were present on the specified directory..");
//                    JSONObject message = new JSONObject();
//                    message.put("showMessage", cf.showMessage(ifr, ProductType, "error", "No files were present on the specified directory.."));
                    return "error"+","+"No files were present on the specified directory..";
                }
            } catch (Exception e) {
                Log.consoleLog(ifr, "Exception....." + e);
                Log.errorLog(ifr, "Exception....." + e);
            }
        } else {

            Log.errorLog(ifr, "#NESL Flag Disabled..");
            Log.errorLog(ifr, "#NESL bypassed");

            String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String countQuery = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS "
                    + "WHERE PROCESSINSTANCEID='" + processInstanceId + "'";
            Log.consoleLog(ifr, "CountQuery==>" + countQuery);
            List< List< String>> Result = ifr.getDataFromDB(countQuery);
            Log.consoleLog(ifr, "#Result===>" + Result.toString());

            // Added  by prakash 17/02/2023 for if  Resultsize is zero ne need to parse the count  to avoid number format Exception
            String count = "0";
            if (Result.size() > 0) {
                count = Result.get(0).get(0);
            }

            if (Integer.parseInt(count) > 0) {
                return RLOS_Constants.SUCCESS;
            } else {
                String Query1 = "INSERT INTO LOS_INTEGRATION_NESL_STATUS(PROCESSINSTANCEID,NESL_TRANS_ID,REQ_STATUS,NESL_MODE,E_SIGN_STATUS,ESIGNLINK) "
                        + "VALUES ('" + processInstanceId + "','NESL_BYPASSED','Y','" + eSignMode + "','Success','NESL_BYPASSED')";
                Log.consoleLog(ifr, "NESL bypassed Query==>" + Query1);
                int status = ifr.saveDataInDB(Query1);
                if (status > 0) {
                    return RLOS_Constants.SUCCESS;
                }
            }
            return RLOS_Constants.SUCCESS;
        }

        return RLOS_Constants.ERROR;
    }

    public void getFileList(String Path) {

        File fObj = new File(Path);

        if (fObj.exists() && fObj.isDirectory()) {
            File a[] = fObj.listFiles();
            System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
            System.out.println("Displaying Files from the directory : " + fObj);
            System.out.println("= = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =");
            printFileNames(a, 0, 0);
        }
    }

    public void printFileNames(File[] a, int i, int lvl) {
        if (i == a.length) {
            return;
        }
        if (a[i].isFile()) {
            System.out.println(a[i].getName());
            Files.add(a[i].getName());
        }
        printFileNames(a, i + 1, lvl);
    }

}
