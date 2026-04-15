/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.dlp.integration.nesl;

import com.newgen.dlp.integration.cbs.CustomerAccountSummary;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.ConfProperty;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import com.newgen.iforms.properties.Log;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author ahmed.zindha
 */
public class EsignCommonMethods {

    CustomerAccountSummary cas = new CustomerAccountSummary();
    CommonFunctionality cf = new CommonFunctionality();
    PortalCommonMethods pcm = new PortalCommonMethods();

//    String getSignCoordinate(IFormReference ifr, String FileList, String DocPath) {
//        try {
//            Log.consoleLog(ifr, "Inside #GeteSignCoordinate..");
//            Log.consoleLog(ifr, "FileList==>" + FileList);
//
//            String eSignSnippet = "";
//            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
//            if (FileList.contains(",")) {
//                String FList[] = FileList.split(",");
//                int i = 1;
//                for (String Files : FList) {
//                    if (!eSignSnippet.equalsIgnoreCase("")) {
//                        eSignSnippet = eSignSnippet + ",";
//                    }
//                    String Query1 = "SELECT noofpages FROM PDBDOCUMENT WHERE DOCUMENTINDEX IN\n"
//                            + "(SELECT DOCUMENTINDEX FROM PDBDOCUMENTCONTENT WHERE PARENTFOLDERINDEX IN\n"
//                            + "(SELECT FOLDERINDEX FROM PDBFOLDER "
//                            + "WHERE NAME='" + ProcessInstanceId + "')) "
//                            + "AND NAME='" + Files.substring(0, Files.indexOf(".")) + "' AND ROWNUM=1 ORDER BY CREATEDDATETIME DESC";
//                    Log.consoleLog(ifr, "Query1==>" + Query1);
//                    List Result = ifr.getDataFromDB(Query1);
//                    String NoofPages = Result.toString().replace("[", "").replace("]", "");
//                    if (NoofPages.equalsIgnoreCase("")) {
//                        NoofPages = "2";
//                    }
//                    Log.consoleLog(ifr, "Br3==>");
//                    String CoOrdSnippet = getNoofpagesCoordinates(ifr, Integer.parseInt(NoofPages));
//                    Log.consoleLog(ifr, "CoOrdSnippet==>" + CoOrdSnippet);
//                    String Snippet = "{\n"
//                            + "\"prtcptenttyId\": 1,\n"
//                            + "\"documentID\": " + i + ",\n"
//                            + "\"coordinates\": [\n"
//                            + CoOrdSnippet
//                            + "]\n"
//                            + "}\n";
//                    eSignSnippet = eSignSnippet + Snippet;
//                    i++;
//                    Log.consoleLog(ifr, "Br4==>");
//                }
//            } else {
//                String Query2 = "SELECT noofpages FROM PDBDOCUMENT WHERE DOCUMENTINDEX IN\n"
//                        + "(SELECT DOCUMENTINDEX FROM PDBDOCUMENTCONTENT WHERE PARENTFOLDERINDEX IN\n"
//                        + "(SELECT FOLDERINDEX FROM PDBFOLDER "
//                        + "WHERE NAME='" + ProcessInstanceId + "')) "
//                        + "AND NAME='" + FileList.substring(0, FileList.indexOf(".")) + "' AND ROWNUM=1 ORDER BY CREATEDDATETIME DESC";
//                Log.consoleLog(ifr, "Query2==>" + Query2);
//                List Result2 = ifr.getDataFromDB(Query2);
//                String NoofPages1 = Result2.toString().replace("[", "").replace("]", "");
//                if (NoofPages1.equalsIgnoreCase("")) {
//                    NoofPages1 = "2";
//                }
//                String CoOrdSnippet = getNoofpagesCoordinates(ifr, Integer.parseInt(NoofPages1));
//                Log.consoleLog(ifr, "CoOrdSnippet==>" + CoOrdSnippet);
//
//                String Snippet = "{\n"
//                        + "\"prtcptenttyId\": 1,\n"
//                        + "\"documentID\": 1,\n"
//                        + "\"coordinates\": [\n"
//                        + CoOrdSnippet
//                        + "]\n"
//                        + "}\n";
//                eSignSnippet = eSignSnippet + Snippet;
//            }
//            return eSignSnippet;
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "Exception/eSignSnippet==>" + e);
//            Log.errorLog(ifr, "Exception/eSignSnippet==>" + e);
//        }
//        return RLOS_Constants.ERROR;
//    }
    String getDocDetailsCount(IFormReference ifr, String FileList, String DocPath) {
        try {
            Log.consoleLog(ifr, "Inside #GetDocDetailsCount..");
            Log.consoleLog(ifr, "FileList==>" + FileList);
            String DocCount = "";
            if (FileList.contains(",")) {
                String FList[] = FileList.split(",");
                int i = 1;
                for (String Files : FList) {
                    if (!DocCount.equalsIgnoreCase("")) {
                        DocCount = DocCount + ",";
                    }
                    DocCount = DocCount + String.valueOf(i);
                    i++;
                }
            } else {
                DocCount = DocCount + "1";
            }
            return DocCount;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/eSignSnippet==>" + e);
            Log.errorLog(ifr, "Exception/eSignSnippet==>" + e);
        }
        return RLOS_Constants.ERROR;
    }

//    String getDocBase64Content(IFormReference ifr, String FileList, String DocPath) {
//        try {
//            Log.consoleLog(ifr, "Inside #GetDocBase64Content..");
//            Log.consoleLog(ifr, "FileList==>" + FileList);
//            String DocumentSnippet = "";
//            if (FileList.contains(",")) {
//                String FList[] = FileList.split(",");
//                int i = 1;
//                for (String Files : FList) {
//                    if (!DocumentSnippet.equalsIgnoreCase("")) {
//                        DocumentSnippet = DocumentSnippet + ",";
//                    }
//                    String DocSnippet = DocPath + File.separator + Files;
//                    String Data = getB64Content(ifr, DocSnippet, i);
//                    DocumentSnippet = DocumentSnippet + Data;
//                    i++;
//                }
//            } else {
//                String DocumentPath = DocPath + File.separator + FileList;
//                String Data = getB64Content(ifr, DocumentPath, 1);
//                DocumentSnippet = DocumentSnippet + Data;
//            }
//            return DocumentSnippet;
//        } catch (Exception e) {
//            Log.consoleLog(ifr, "Exception/GetDocBase64Content==>" + e);
//            Log.errorLog(ifr, "Exception/GetDocBase64Content==>" + e);
//        }
//        return "";
//    }
    public String getB64Content(IFormReference ifr, String DocumentPath, int DocId) {
        try {
            File file = new File(DocumentPath);
            if (file.exists()) {
                Log.consoleLog(ifr, "File Exists");
                byte[] byteData = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                String base64String = Base64.getEncoder().encodeToString(byteData);
                String DocSnippet = "{\n"
                        + "\"docData\": \"" + base64String + "\",\n"
                        + "\"documentID\": " + DocId + ",\n"
                        + "\"prtcptenttyId\": \"1\"\n"
                        + "}\n";
                return DocSnippet;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/GetDocBase64Content==>" + e);
        }
        return "";
    }

    public static String removeZero(String str) {
        // Count leading zeros
        // Initially setting loop counter to 0
        int i = 0;
        while (i < str.length() && str.charAt(i) == '0') {
            i++;
        }
        // Converting string into StringBuffer object
        // as strings are immutable
        StringBuffer sb = new StringBuffer(str);
        // The StringBuffer replace function removes
        // i characters from given index (0 here)
        sb.replace(0, i, "");
        // Returning string after removing zeros
        return sb.toString();
    }

    public String checkNESLWorkflowStatus(IFormReference ifr) {

        try {
            Log.consoleLog(ifr, "CheckNESLWorkflowStatus Started..");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

            String Query1 = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS WHERE PROCESSINSTANCEID='" + ProcessInstanceId + "'";
            Log.consoleLog(ifr, "Query1==>" + Query1);
            List Result1 = ifr.getDataFromDB(Query1);
            String Count1 = Result1.toString().replace("[", "").replace("]", "");
            Log.consoleLog(ifr, "Count1==>" + Count1);

            if (Integer.parseInt(Count1) == 0) {
                Log.consoleLog(ifr, "NESL Not triggered...");
                return "NeSL is not successful due to technical issue. Please check with Branch for further details.";
            }

            String Query2 = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS WHERE "
                    + "PROCESSINSTANCEID='" + ProcessInstanceId + "' AND REQ_STATUS='N'";
            Log.consoleLog(ifr, "Query2==>" + Query2);
            List Result2 = ifr.getDataFromDB(Query2);
            String Count2 = Result2.toString().replace("[", "").replace("]", "");
            Log.consoleLog(ifr, "Count2==>" + Count2);

            if (Integer.parseInt(Count2) > 0) {
                Log.consoleLog(ifr, "NESL eSign Request is in progress...");
                return "NESL Triggered. Please check your SMS/Email for eStamping & eSigning.";
            }

            String Query3 = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS WHERE "
                    + "PROCESSINSTANCEID='" + ProcessInstanceId + "' AND REQ_STATUS='Y' AND E_SIGN_STATUS!='Success'";
            Log.consoleLog(ifr, "Query3==>" + Query3);
            List Result3 = ifr.getDataFromDB(Query3);
            String Count3 = Result3.toString().replace("[", "").replace("]", "");
            Log.consoleLog(ifr, "Count3==>" + Count3);

            if (Integer.parseInt(Count3) > 0) {
                Log.consoleLog(ifr, "NESL eSign Failed...");
                return "NESL failed. Please check with Branch for further details.";
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception==>" + e);
        }
        return "";
    }

//    public String getNoofpagesCoordinates(IFormReference ifr, int noOfPages) {
//        Log.consoleLog(ifr, "getNoofpagesCoordinates==>");
//        String CoOrdSnippet = "";
//        for (int i = 1; i <= noOfPages; i++) {
//            if (!CoOrdSnippet.equalsIgnoreCase("")) {
//                CoOrdSnippet = CoOrdSnippet + ",";
//            }
//            String Cordinates = "\"" + i + ", 447, 770\"\n";
//            CoOrdSnippet = CoOrdSnippet + Cordinates;
//        }
//        Log.consoleLog(ifr, "CoOrdSnippet/getNoofpagesCoordinates==>" + CoOrdSnippet);
//        return CoOrdSnippet;
//    }
    ///Started on 28-02-24 for Multiple CoApplicants Handling
    public String getNoofApplicants(IFormReference ifr, String ProductType, String ProcessInstanceId) {

        Log.consoleLog(ifr, "#getNoofApplicants started..");

        String query0 = "";
        String resultCount = "0";
        // String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        
            query0 = "SELECT COUNT(1) FROM LOS_TRN_CUSTOMERSUMMARY WHERE WINAME='" + ProcessInstanceId + "'";
      
        Log.consoleLog(ifr, "query0=====>" + query0);
        List< List< String>> Result0 = ifr.getDataFromDB(query0);
        Log.consoleLog(ifr, "#Result0===>" + Result0.toString());
        if (Result0.size() > 0) {
            resultCount = Result0.get(0).get(0);
        }

        return resultCount;
    }

    public String getApplicantUniqueKeyParams(IFormReference ifr, String ProcessInstanceId) {

        Log.consoleLog(ifr, "#getApplicantUniqueKeyParams started..");

        ArrayList fKeySet = new ArrayList();
        //String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        String query = "SELECT F_KEY FROM LOS_NL_BASIC_INFO "
                + "WHERE PID='" + ProcessInstanceId + "' ORDER BY F_KEY ASC ";

        Log.consoleLog(ifr, "query=====>" + query);
        List< List< String>> Result = ifr.getDataFromDB(query);
        Log.consoleLog(ifr, "#Result===>" + Result.toString());
        if (Result.size() > 0) {

            for (List<String> FKey : Result) {
                fKeySet.add(FKey);
            }

        }
        String Output = fKeySet.toString().replace("[", "").replace("]", "").replace(", ", ",");
        Log.consoleLog(ifr, "#Output==>" + Output);
        return Output;
    }

    public String getStampDetailsSnippet(IFormReference ifr, List<List<String>> Result3,
            String articleCode, String stampDutyAmnt, JSONObject applicantObj, JSONObject loanObj) {
    	String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
    	String loanAmt=loanObj.get("loanAmount").toString();
    	double loanAmtInDou=Double.parseDouble(loanAmt);
    	String loanAmount=String.format("%.2f",loanAmtInDou);
        String eStampSnippet = "{\n"
                + "                \"firstparty\": \"" + applicantObj.get("customerFullName") + "\",\n"
                + "                \"considerationPrice\": \"" + loanAmount + "\",\n"
                + "                \"stampDutyAmount\": \"" + stampDutyAmnt + "\",\n"
                + "                \"stampdutyPaidby\": \"Canara Bank\",\n"
                + "                \"secondparty\": \"Canara Bank\",\n"
                + "                \"documentID\": \"1\",\n"
                + "                \"articleCode\": \"" + articleCode + "\",\n"
                + "                \"descriptionofDocument\": \"Personal loan\"\n"
                + "      }\n";

        return eStampSnippet;

    }
    
    public String getStampDetailsSnippetForWBAndMP(IFormReference ifr, List<List<String>> Result3,
            String articleCode, String stampDutyAmnt, JSONObject applicantObj, JSONObject loanObj, String permZIP, String panno) {
    	String loanAmt=loanObj.get("loanAmount").toString();
    	double loanAmtInDou=Double.parseDouble(loanAmt);
    	String loanAmount=String.format("%.2f",loanAmtInDou);
        String eStampSnippet = "{\n"
                + "                \"firstparty\": \"" + applicantObj.get("customerFullName") + "\",\n"
                + "                \"secondparty\": \"Canara Bank\",\n"
                + "                \"considerationPrice\": \"" + loanAmount + "\",\n"
                + "                \"stampDutyAmount\": \"" + stampDutyAmnt + "\",\n"
                + "                \"stampdutyPaidby\": \"Canara Bank\",\n"
                + "                \"documentID\": \"1\",\n"
                + "                \"articleCode\": \"" + articleCode + "\",\n"
                + "                \"descriptionofDocument\": \"Personal loan\",\n"
                + "                \"firstPartyPin\": \"" + permZIP + "\",\n"
                + "                \"secondPartyPin\": \"" + permZIP + "\",\n"
                + "                \"firstPartyOVDType\": \"panno\",\n"
                + "                \"firstPartyOVDValue\": \"" + panno + "\",\n"
                + "                \"secondPartyOVDType\": \"panno\",\n"
                + "                \"secondPartyOVDValue\": \"AAACC6106G\"\n"
                + "      }\n";

        return eStampSnippet;

    }

    public String getParticipantSnippet(IFormReference ifr, List<List<String>> Result3,
            String DocumentCountSnippet, JSONObject applicantObj, int partyId) {

        String reltocntrct = "";
        if (partyId == 1) {
            reltocntrct = "debtor";
        } else {
            reltocntrct = "co-obligant";
        }

        String participantSnippet = "{\n"
                + "                \"prtcptenttyId\": \"" + partyId + "\",\n"
                + "                \"altemlid\": \"\",\n"
                + "                \"altmobno\": \"" + applicantObj.get("mobileNumber") + "\",\n"
                + "                \"cntrprtyaddr\": \"\",\n"
                + "                \"cntrprtycntmobno\": \"" + applicantObj.get("mobileNumber") + "\",\n"
                + "                \"cntrprtycntnm\": \"FIRE\",\n"
                + "                \"comaddr\": \"\",\n"
                + "                \"doi\": \"" + applicantObj.get("DOB") + "\",\n"
                + "                \"lglcnstn\": \"Resident Individual\",\n"
                + "                \"emlid\": \"" + applicantObj.get("emailId").toString().replace("{}", "") + "\",\n"
                + "                \"fulnm\": \"" + applicantObj.get("customerFullName") + "\",\n"
                + "                \"panno\": \"" + applicantObj.get("panNumber") + "\",\n"
                + "                \"partytyp\": \"Indian Entity\",\n"
                + "                \"pin\": \"\",\n"
                + "                \"regoffpin\": \"\",\n"
                + "                \"reltocntrct\": \"" + reltocntrct + "\",\n"
                + "                \"ovdtype\": \"\",\n"
                + "                \"ovdid\": \"\",\n"
                + "                \"signatoryAadhar\": \"" + applicantObj.get("signatoryAadhar") + "\",\n"
                + "                \"signatoryGender\": \"" + applicantObj.get("signatoryGender") + "\",\n"
                + "                \"documentID\": [\n"
                + DocumentCountSnippet
                + "                ],\n"
                + "                \"seqno\": " + partyId + "\n"
                + "            }\n";

        return participantSnippet;
    }

    public JSONObject getApplicantInformation(IFormReference ifr, List<List<String>> Result3) {

        String signatoryAadhar = "";
        String signatoryGender = "M";//Deafult
        String customerFullName = "";
        String customerFirstName = "";
        String customerMiddleName = "";
        String customerLastName = "";
        String panNumber = "";
        String mobileNumber = "";
        String emailId = "";

        //String aadharNum = "";
        String dateOfBirth = "";
        String gender = "";
        String customerId = "";

        if (!Result3.isEmpty()) {
            //Code modified by Ahmed on 06-05-2024 for FName+MName+LName change
            customerFirstName = Result3.get(0).get(0).replace("{}", "").replace("null", "").trim();;
            customerMiddleName = Result3.get(0).get(1).replace("{}", "").replace("null", "").trim();;
            customerLastName = Result3.get(0).get(2).replace("{}", "").replace("null", "").trim();;

            if (customerMiddleName.equalsIgnoreCase("")) {
                customerFullName = customerFirstName + " " + customerLastName;
            } else {
                customerFullName = customerFirstName + " " + customerMiddleName + " " + customerLastName;
            }
            Log.consoleLog(ifr, "customerFullName==>" + customerFullName);

//                         customerFullName = customerFirstName.replace("{}", "") + customerMiddleName.replace("{}", "") + customerLastName.replace("{}", "");
//                         code mofified by prakash k 22-05-2024  for given space between first name and last name 
//                        customerFullName = customerFirstName.replace("{}", "") + " "
//                                + customerMiddleName.replace("{}", "") + " "
//                                + customerLastName.replace("{}", "");
//            customerFullName = customerFullName.trim();
            panNumber = Result3.get(0).get(3);
            mobileNumber = Result3.get(0).get(4);

            if (mobileNumber.length() > 10) {
                mobileNumber = mobileNumber.substring(2, 12);
                Log.consoleLog(ifr, "MobileNumber==>" + mobileNumber);
            }
            emailId = Result3.get(0).get(5);
            dateOfBirth = Result3.get(0).get(6);
            gender = Result3.get(0).get(7);

            if (gender.equalsIgnoreCase("MALE")) {
                signatoryGender = "M";
            } else if (gender.equalsIgnoreCase("FEMALE")) {
                signatoryGender = "F";
            } else {
                signatoryGender = "M";//Transgender is not supporting by NESL
            }
            customerId = Result3.get(0).get(8);
        }

        Log.consoleLog(ifr, "customerFullName==>" + customerFullName);
        Log.consoleLog(ifr, "panNumber=========>" + panNumber);
        Log.consoleLog(ifr, "mobileNumber======>" + mobileNumber);
        Log.consoleLog(ifr, "emailId===========>" + emailId);
        Log.consoleLog(ifr, "dateOfBirth=======>" + dateOfBirth);
        Log.consoleLog(ifr, "gender============>" + gender);

        HashMap<String, String> customerdetails = new HashMap<>();
        customerdetails.put("MobileNumber", mobileNumber);
        customerdetails.put("customerId", customerId);//Added by Ahmed on 31-07-2024
        //Commeneted by Ahmed on 12-06-2024 for fetching Aadhar No via Vault
        String aadharNum = cas.getAadharCustomerAccountSummary(ifr, customerdetails);

        Log.consoleLog(ifr, "aadharNum==>" + aadharNum);
        signatoryAadhar = aadharNum;

        Log.consoleLog(ifr, "signatoryAadhar/getCustomerAccountSummary==>" + signatoryAadhar);
        signatoryAadhar = signatoryAadhar.substring(8, 12);
        Log.consoleLog(ifr, "signatoryAadhar==>" + signatoryAadhar);

        String DOB = "";
        try {
            DateFormat originalFormat = new SimpleDateFormat("dd-MM-yyyy");
            DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date date = originalFormat.parse(dateOfBirth);
            DOB = targetFormat.format(date);
            Log.consoleLog(ifr, "DOB==>" + DOB);
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception DOB==>" + e);
        }

        JSONObject obj = new JSONObject();
        obj.put("customerFullName", customerFullName);
        obj.put("panNumber", panNumber);
        obj.put("mobileNumber", mobileNumber);
        obj.put("emailId", emailId);
        obj.put("dateOfBirth", dateOfBirth);
        obj.put("gender", gender);
        obj.put("signatoryGender", signatoryGender);
        obj.put("aadharNum", aadharNum);
        obj.put("signatoryAadhar", signatoryAadhar);
        obj.put("DOB", DOB);
        return obj;

    }

    public JSONObject getLoanInformation(IFormReference ifr, String ProductType, String ProcessInstanceId) throws ParseException {

        String loanAmount = "";
        String Tenure = "";
        String roi = "";
        String emi = "";
        // String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        String query2 = "";
//        if (ProductType.equalsIgnoreCase("PAPL")) {
//            query2 = "select loanamount,tenure,rateofinterest,emi from "
//                    + "los_tran_papl_finaleligibility WHERE WINAME='" + ProcessInstanceId + "'";
//        } else if (ProductType.equalsIgnoreCase("Pension")) {
//            query2 = "SELECT LOAN_AMT,TENURE,ROI,EMI from "
//                    + "LOS_PEN_FINAL_ELIGIBILITY WHERE PID='" + ProcessInstanceId + "'";
//        } else {
//            query2 = "SELECT LOANAMOUNT,Tenure,RATEOFINTEREST,EMI from "
//                    + "LOS_TRN_FINALELIGIBILITY WHERE WINAME='" + ProcessInstanceId + "'";
//        }

        String activityName = ifr.getActivityName();
        Log.consoleLog(ifr, "#ProductType====>" + ProductType);
        Log.consoleLog(ifr, "#activityName===>" + activityName);

        /*if (activityName.equalsIgnoreCase("Portal")) {
            if (ProductType.equalsIgnoreCase("PAPL")) {
                query2 = ConfProperty.getQueryScript("Portal_PAPLGETLOANDTLSQRY").replaceAll("#WINAME#", ProcessInstanceId);
            } else if (ProductType.equalsIgnoreCase("Canara Pension")) {
                query2 = ConfProperty.getQueryScript("Portal_PENSGETLOANDTLSQRY").replaceAll("#WINAME#", ProcessInstanceId);
            } else {
                query2 = ConfProperty.getQueryScript("Portal_OTHERGETLOANDTLSQRY").replaceAll("#WINAME#", ProcessInstanceId);
            }

        } else {
            query2 = ConfProperty.getQueryScript("BackOff_GETLOANDTLSQRY").replaceAll("#WINAME#", ProcessInstanceId);
        }
        
*/
        String query1 ="";
        String query5 = "";
        String DateofSanction = "";
        if(ProductType.contains("VL"))
        {
        	
        	query1="select app_loan_amt_vl,app_loan_tenure_vl,MI_ROC_VL,EFFECTIVE_ROI from SLOS_STAFF_TRN where winame='"+ProcessInstanceId+"'";
        	 Log.consoleLog(ifr, "#query1===>" + query1);
             List< List< String>> Result4 = ifr.getDataFromDB(query1);
             Log.consoleLog(ifr, "#Result4===>" + Result4.toString());
             if (!Result4.isEmpty()) {
                 loanAmount = Result4.get(0).get(0);
                 Tenure = Result4.get(0).get(1);
                 emi = Result4.get(0).get(2);
                 roi = Result4.get(0).get(3);
             }
             
         	String formaDate="";
			String querySanctionDate = "SELECT SANCTION_DATE FROM slos_trn_loandetails " + "WHERE PID='"
					+ ProcessInstanceId + "' AND ROWNUM=1";
			Log.consoleLog(ifr, "SANCTION_AMOUNT_Query==>NOT PAPL::::" + querySanctionDate);

			List<List<String>> ResultSanctionDate = cf.mExecuteQuery(ifr, querySanctionDate, "querySanctionDate:");
			if (!ResultSanctionDate.isEmpty()) {
				formaDate = ResultSanctionDate.get(0).get(0);
				Log.consoleLog(ifr, "sanctionDate==>" + formaDate);
			}
			SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	        DateofSanction = dateFormat.format(formaDate);
			 Date parsedDate = dbFormat.parse(formaDate);

		      SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
		     String formattedDate = targetFormat.format(parsedDate);
		     Log.consoleLog(ifr, "Formatted Date of Sanction: " + formattedDate);
		       DateofSanction = formattedDate;
        	
        	
        }
        else {
        query2="select loan_amount,tenure_months,roi,emi from SLOS_STAFF_TRN where winame='"+ProcessInstanceId+"'";
        Log.consoleLog(ifr, "#query2===>" + query2);
        List< List< String>> Result4 = ifr.getDataFromDB(query2);
        Log.consoleLog(ifr, "#Result4===>" + Result4.toString());
        if (!Result4.isEmpty()) {
            loanAmount = Result4.get(0).get(0);
            Tenure = Result4.get(0).get(1);
            roi = Result4.get(0).get(2);
            emi = Result4.get(0).get(3);
        }

        Log.consoleLog(ifr, "LoanAmount==>" + loanAmount);
        Log.consoleLog(ifr, "Tenure======>" + Tenure);
        Log.consoleLog(ifr, "ROI=========>" + roi);
        Log.consoleLog(ifr, "EMI=========>" + emi);
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateofSanction = dateFormat.format(currentDate);
        }
        
       
       
        JSONObject obj = new JSONObject();
        obj.put("loanAmount", loanAmount);
        obj.put("Tenure", Tenure);
        obj.put("roi", roi);
        obj.put("emi", emi);
        obj.put("DateofSanction", DateofSanction);
        return obj;
       
    }

    String getDocBase64Content(IFormReference ifr, String FileList, String DocPath, int partyId) {
        try {
            Log.consoleLog(ifr, "Inside #getMDocBase64Content..");
            Log.consoleLog(ifr, "FileList==>" + FileList);
            String DocumentSnippet = "";
            if (FileList.contains(",")) {
                String FList[] = FileList.split(",");
                int i = 1;
                for (String Files : FList) {
                    if (!DocumentSnippet.equalsIgnoreCase("")) {
                        DocumentSnippet = DocumentSnippet + ",";
                    }
                    String DocSnippet = DocPath + File.separator + Files;
                    String Data = getB64Content(ifr, DocSnippet, i, partyId);
                    DocumentSnippet = DocumentSnippet + Data;
                    i++;
                }
            } else {
                String DocumentPath = DocPath + File.separator + FileList;
                String Data = getB64Content(ifr, DocumentPath, 1, partyId);
                DocumentSnippet = DocumentSnippet + Data;
            }
            return DocumentSnippet;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getMDocBase64Content==>" + e);
            Log.errorLog(ifr, "Exception/getMDocBase64Content==>" + e);
        }
        return "";
    }

    public String getB64Content(IFormReference ifr, String DocumentPath, int DocId, int partyId) {
        try {
            File file = new File(DocumentPath);
            if (file.exists()) {
                Log.consoleLog(ifr, "File Exists");
                byte[] byteData = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
                String base64String = Base64.getEncoder().encodeToString(byteData);
                String DocSnippet = "{\n"
                        + "\"docData\": \"" + base64String + "\",\n"
                        + "\"documentID\": " + DocId + ",\n"
                        + "\"prtcptenttyId\": \"" + partyId + "\"\n"
                        + "}\n";
                return DocSnippet;
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/GetDocBase64Content==>" + e);
        }
        return "";
    }

    public String getSignCoordinate(IFormReference ifr, String FileList, String DocPath, int partyId, String ProcessInstanceId) {
        try {
            Log.consoleLog(ifr, "Inside #getMSignCoordinate..");
            Log.consoleLog(ifr, "FileList==>" + FileList);

            String eSignSnippet = "";
            // String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            if (FileList.contains(",")) {
                String FList[] = FileList.split(",");
                int i = 1;
                for (String Files : FList) {
                    if (!eSignSnippet.equalsIgnoreCase("")) {
                        eSignSnippet = eSignSnippet + ",";
                    }
                    String Query1 = "SELECT noofpages FROM PDBDOCUMENT WHERE DOCUMENTINDEX IN\n"
                            + "(SELECT DOCUMENTINDEX FROM PDBDOCUMENTCONTENT WHERE PARENTFOLDERINDEX IN\n"
                            + "(SELECT FOLDERINDEX FROM PDBFOLDER "
                            + "WHERE NAME='" + ProcessInstanceId + "')) "
                            + "AND NAME='" + Files.substring(0, Files.indexOf(".")) + "' AND ROWNUM=1 ORDER BY CREATEDDATETIME DESC";
                    Log.consoleLog(ifr, "Query1==>" + Query1);
                    List Result = ifr.getDataFromDB(Query1);
                    String NoofPages = Result.toString().replace("[", "").replace("]", "");
                    if (NoofPages.equalsIgnoreCase("")) {
                        NoofPages = "2";
                    }
                    Log.consoleLog(ifr, "Br3==>");
                    String CoOrdSnippet = getNoofpagesCoordinates(ifr, Integer.parseInt(NoofPages), partyId);
                    Log.consoleLog(ifr, "CoOrdSnippet==>" + CoOrdSnippet);
                    String Snippet = "{\n"
                            + "\"prtcptenttyId\": " + partyId + ",\n"
                            + "\"documentID\": " + i + ",\n"
                            + "\"coordinates\": [\n"
                            + CoOrdSnippet
                            + "]\n"
                            + "}\n";
                    eSignSnippet = eSignSnippet + Snippet;
                    i++;
                    Log.consoleLog(ifr, "Br4==>");
                }
            } else {
                String Query2 = "SELECT noofpages FROM PDBDOCUMENT WHERE DOCUMENTINDEX IN\n"
                        + "(SELECT DOCUMENTINDEX FROM PDBDOCUMENTCONTENT WHERE PARENTFOLDERINDEX IN\n"
                        + "(SELECT FOLDERINDEX FROM PDBFOLDER "
                        + "WHERE NAME='" + ProcessInstanceId + "')) "
                        + "AND NAME='" + FileList.substring(0, FileList.indexOf(".")) + "' AND ROWNUM=1 ORDER BY CREATEDDATETIME DESC";
                Log.consoleLog(ifr, "Query2==>" + Query2);
                List Result2 = ifr.getDataFromDB(Query2);
                String NoofPages1 = Result2.toString().replace("[", "").replace("]", "");
                if (NoofPages1.equalsIgnoreCase("")) {
                    NoofPages1 = "2";
                }
                String CoOrdSnippet = getNoofpagesCoordinates(ifr, Integer.parseInt(NoofPages1), partyId);
                Log.consoleLog(ifr, "CoOrdSnippet==>" + CoOrdSnippet);

                String Snippet = "{\n"
                        + "\"prtcptenttyId\": " + partyId + ",\n"
                        + "\"documentID\": 1,\n"
                        + "\"coordinates\": [\n"
                        + CoOrdSnippet
                        + "]\n"
                        + "}\n";
                eSignSnippet = eSignSnippet + Snippet;
            }
            return eSignSnippet;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getMSignCoordinate==>" + e);
            Log.errorLog(ifr, "Exception/getMSignCoordinate==>" + e);
        }
        return RLOS_Constants.ERROR;
    }

    public String getNoofpagesCoordinates(IFormReference ifr, int noOfPages, int partyId) {
        Log.consoleLog(ifr, "getMNoofpagesCoordinates==>");
        String CoOrdSnippet = "";

        PortalCommonMethods pc = new PortalCommonMethods();
        String pageCoordinates = pc.getConstantValue(ifr, "NESL", "PAGECOORDINATES");//227, 770
        Log.consoleLog(ifr, "pageCoordinates==>" + pageCoordinates);
        
        String staffXcordinates = pcm.getParamValue(ifr, "STAFFCORDCHECK", "XCORDINATES");
        int xcordinates = Integer.parseInt(staffXcordinates);
        
        String staffYcordinates = pcm.getParamValue(ifr, "STAFFCORDCHECK", "YCORDINATES");
        int ycordinates = Integer.parseInt(staffYcordinates);

        if (partyId == 1) {
            for (int i = 1; i <= noOfPages; i++) {
                if (!CoOrdSnippet.equalsIgnoreCase("")) {
                    CoOrdSnippet = CoOrdSnippet + ",";
                }
               /* String Cordinates = "\"" + i + ", " + pageCoordinates + "\"\n";
                CoOrdSnippet = CoOrdSnippet + Cordinates;*/
                // String Cordinates = "\"" + i + ", 447, 770\"\n";
                 String Cordinates = "\"" + i + ", "+xcordinates+", "+ycordinates+"\"\n";
               // String Cordinates = "\"" + i + ", " + pageCoordinates + "\"\n";
                CoOrdSnippet = CoOrdSnippet + Cordinates;
            }

        } else if (partyId == 2) {
            for (int i = 1; i <= noOfPages; i++) {
                if (!CoOrdSnippet.equalsIgnoreCase("")) {
                    CoOrdSnippet = CoOrdSnippet + ",";
                }
                /*String Cordinates = "\"" + i + ", " + pageCoordinates + "\"\n";
                CoOrdSnippet = CoOrdSnippet + Cordinates;*/
                // String Cordinates = "\"" + i + ", 447, 770\"\n";
                String Cordinates = "\"" + i + ", "+xcordinates+", "+ycordinates+"\"\n";
               // String Cordinates = "\"" + i + ", " + pageCoordinates + "\"\n";
                CoOrdSnippet = CoOrdSnippet + Cordinates;
            }
        }

        Log.consoleLog(ifr, "CoOrdSnippet/getMNoofpagesCoordinates==>" + CoOrdSnippet);
        return CoOrdSnippet;
    }

    public String getEsignURL(IFormReference ifr) {

        try {
            Log.consoleLog(ifr, "getEsignURL Started..");

            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);
            /*
            String Query1 = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS WHERE "
                    + "PROCESSINSTANCEID='" + ProcessInstanceId + "' AND REQ_STATUS='Y' "
                    + "AND E_SIGN_STATUS!='Success'";
            Log.consoleLog(ifr, "Query1==>" + Query1);
            List< List< String>> Result1 = ifr.getDataFromDB(Query1);
            Log.consoleLog(ifr, "#Result1===>" + Result1.toString());
            String Count1 = "0";
            if (Result1.size() > 0) {
                Count1 = Result1.get(0).get(0);
            }
            Log.consoleLog(ifr, "Count1==>" + Count1);

            if (Integer.parseInt(Count1) > 0) {
                Log.consoleLog(ifr, "NESL eSign Failed...");
                return "NESL failed. Kindly contact Branch.";
            }*/

            String Query2 = "SELECT ESIGNLINK FROM LOS_INTEGRATION_NESL_STATUS WHERE "
                    + "PROCESSINSTANCEID='" + ProcessInstanceId + "'";
            Log.consoleLog(ifr, "Query2==>" + Query2);
            List< List< String>> Result2 = ifr.getDataFromDB(Query2);
            Log.consoleLog(ifr, "#Result2===>" + Result2.toString());
            String eSignLink = "";
            if (Result2.size() > 0) {
                eSignLink = Result2.get(0).get(0);
            }
            Log.consoleLog(ifr, "eSignLink==>" + eSignLink);
            return eSignLink;

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getEsignURL==>" + e);
        }
        return RLOS_Constants.ERROR;
    }

    public String getNESLWorkflowStatus(IFormReference ifr) {

        try {
            Log.consoleLog(ifr, "getNESLWorkflowStatus Started..");

            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

            String Query1 = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS WHERE "
                    + "PROCESSINSTANCEID='" + ProcessInstanceId + "'";
            Log.consoleLog(ifr, "Query1==>" + Query1);
            List< List< String>> Result1 = ifr.getDataFromDB(Query1);
            Log.consoleLog(ifr, "#Result1===>" + Result1.toString());
            String Count1 = "0";
            if (Result1.size() > 0) {
                Count1 = Result1.get(0).get(0);
            }
            Log.consoleLog(ifr, "Count1==>" + Count1);

            if (Integer.parseInt(Count1) == 0) {
                Log.consoleLog(ifr, "You haven`t initiated eSigning. Kindly review the Document and initiate the NESL request.");
                return "Kindly click on [Review eSigning Document] button to review the document and initiate the NESL request.";
            }

            String Query2 = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS WHERE "
                    + "PROCESSINSTANCEID='" + ProcessInstanceId + "' AND REQ_STATUS='N'";
            Log.consoleLog(ifr, "Query2==>" + Query2);
            List< List< String>> Result2 = ifr.getDataFromDB(Query2);
            Log.consoleLog(ifr, "#Result2===>" + Result2.toString());
            String Count2 = "0";
            if (Result2.size() > 0) {
                Count2 = Result2.get(0).get(0);
            }
            Log.consoleLog(ifr, "Count2==>" + Count2);

            if (Integer.parseInt(Count2) > 0) {
                Log.consoleLog(ifr, "NESL Initiated. Click on [eSign Document] button to eSign/eStamp the document.");
                return "NESL Initiated. Click on eSigning button to eSign/eStamp the document.";
            }

            return RLOS_Constants.SUCCESS;
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception/getNESLWorkflowStatus==>" + e);
        }
        return RLOS_Constants.ERROR;
    }

    //Added by Ahmed on 05-07-2024 for disabling the section based on NESL Triggered State
    public String checkNESLTriggeredStatus(IFormReference ifr) {
        String count = "0";
        try {
            Log.consoleLog(ifr, "checkNESLTriggeredStatus Started..");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

            String Query1 = "SELECT COUNT(*) FROM LOS_INTEGRATION_NESL_STATUS WHERE "
                    + "PROCESSINSTANCEID='" + ProcessInstanceId + "'";
            Log.consoleLog(ifr, "Query1==>" + Query1);
            List<List<String>> Result1 = ifr.getDataFromDB(Query1);
            Log.consoleLog(ifr, "#Result1===>" + Result1.toString());
            if (Result1 != null && !Result1.isEmpty() && !Result1.get(0).isEmpty()) {
                count = Result1.get(0).get(0);
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception==>" + e);
            return "error, database is down, please check after sometimes";
        }
        return count;
    }
    
    public String checkFundTransferStatus(IFormReference ifr) {
        String count = "0";
        try {
            Log.consoleLog(ifr, "checkFundTransferStatus Started..");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            Log.consoleLog(ifr, "ProcessInstanceId==>" + ProcessInstanceId);

            String Query1 = "SELECT COUNT(*) FROM LOS_INTEGRATION_CBS_STATUS WHERE "
                    + "TRANSACTION_ID='" + ProcessInstanceId + "' and API_NAME = 'CBS_FundTransfer' and API_STATUS = 'SUCCESS'";
            Log.consoleLog(ifr, "Query1==>" + Query1);
            List<List<String>> Result1 = ifr.getDataFromDB(Query1);
            Log.consoleLog(ifr, "#Result1===>" + Result1.toString());
            if (Result1 != null && !Result1.isEmpty() && !Result1.get(0).isEmpty()) {
                count = Result1.get(0).get(0);
            }

        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception==>" + e);
            return "error, database is down, please check after sometimes";
        }
        return count;
    }
}
