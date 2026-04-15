package com.newgen.iforms.staffHL;

import com.newgen.dlp.integration.fintec.ConsumerAPI;
import com.newgen.dlp.integration.fintec.ExperianAPI;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.ConfProperty;
import com.newgen.iforms.properties.Log;
import java.util.List;
import org.json.simple.parser.ParseException;

/**
 *
 * @author aharagon
 */
public class HLExternalAPIs {

    CommonFunctionality cf = new CommonFunctionality();

//    public String mCallBureauHL(IFormReference ifr, String BureauType, String aadharNo, String applicantType, String loanamount, String fkey, String DateofBirth) throws ParseException {
//
//        try {
//            Log.consoleLog(ifr, "Inside mCallBureauHL:");
//            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
//            String CountQuery;
//            if (BureauType.equalsIgnoreCase("CB")) {
//                CountQuery = ConfProperty.getQueryScript("getCibilCountQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#APPTYPE#", applicantType+fkey);
//            } else {
//                CountQuery = ConfProperty.getQueryScript("getExperianCountQuery").replaceAll("#ProcessInstanceId#", ProcessInstanceId).replaceAll("#APPTYPE#", applicantType+fkey);
//            }
//
//            List< List< String>> Result = ifr.getDataFromDB(CountQuery);
//            Log.consoleLog(ifr, "#Result===>" + Result.toString());
//            String Count = "";
//            if (Result.size() > 0) {
//                Count = Result.get(0).get(0);
//            }
//
//            if (Integer.parseInt(Count) > 0) {
//                return Count;
//            }
//            //added by ishwarya on 14022024
//            String minLoanmaount = loanamount;
//            //pcm.mGetMinLoanAmount(ifr);
//            Log.consoleLog(ifr, "minLoanamount:" + minLoanmaount);
//            //Code Logic Modified for Experian
//            if (BureauType.equalsIgnoreCase("EX")) {
//                Log.consoleLog(ifr, "BureauType->" + BureauType);
//                ExperianAPI EXP = new ExperianAPI();
//                String BureauScore = EXP.getExperianCIBILScoreHL(ifr, ProcessInstanceId, aadharNo, "CB", minLoanmaount, applicantType, fkey, DateofBirth);
//                Log.consoleLog(ifr, "BureauScore From Experian===>" + BureauScore);
//                if (BureauScore.contains(RLOS_Constants.ERROR)) {
//                    return RLOS_Constants.ERROR + ":" + BureauScore.replaceAll("ERROR:*", "").trim() + " in Experian API";
//                }
//            } else {
//                Log.consoleLog(ifr, "BureauType->" + BureauType);
//                ConsumerAPI CB1 = new ConsumerAPI();
//                String BureauScore = CB1.getConsumerCIBILScoreHL(ifr, "CB", minLoanmaount, aadharNo, applicantType, fkey, DateofBirth);
//                Log.consoleLog(ifr, "BureauScore From Transunion==>" + BureauScore);
//                //String BureauResult = BureauScore.split("ERROR")[1].trim();
//                //Log.consoleLog(ifr, "BureauResult=>" + BureauResult);
//                //String BureauResult = BureauScore;
//                if (BureauScore.contains(RLOS_Constants.ERROR)) {
//                    Log.consoleLog(ifr, "inside ERROR:::::::::mCallBureauHL::::==>" + RLOS_Constants.ERROR + ":" + BureauScore.replaceAll("ERROR:*", "").trim() + " in Transunion API");
//                    return RLOS_Constants.ERROR + ":" + BureauScore.replaceAll("ERROR:*", "").trim() + " in Transunion API";
//                }
//            }
//        } catch (NumberFormatException | ParseException ex) {
//            Log.errorLog(ifr, "Exception mCallBureauHL : " + ex);
//        }
//        return "";
//    }
    public String mCallBureauHL(IFormReference ifr, String BureauType, String aadharNo, String applicantType, String loanamount) throws ParseException {

        try {
            Log.consoleLog(ifr, "HLExternalAPIs:mCallBureauHL->Inside mCallBureauHL");
            String ProcessInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String CountQuery;
            Log.consoleLog(ifr, "HLExternalAPIs:mCallBureauHL->Inside applicantType$$$$" + applicantType);
            String insertionOrderId = applicantType.split("~")[1];
//            CountQuery = "SELECT COUNT(*) FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='"+ProcessInstanceId+"' and BUREAUTYPE='"+BureauType+"'";
            CountQuery = "SELECT COUNT(*) FROM LOS_CAN_IBPS_BUREAUCHECK WHERE PROCESSINSTANCEID='"+ProcessInstanceId+"' and BUREAUTYPE='"+BureauType+"' and applicant_type='"+applicantType+"'";

            List<List<String>> Result = cf.mExecuteQuery(ifr, CountQuery, "getCIC count: ");
            // List< List< String>> Result = ifr.getDataFromDB(CountQuery);
            Log.consoleLog(ifr, "HLExternalAPIs:mCallBureauHL->Result: " + Result.toString());
            String Count = "";
            if (Result.size() > 0) {
                Count = Result.get(0).get(0);
            }

            if (Integer.parseInt(Count) > 0) {
                return Count;
            }
            String minLoanmaount = loanamount;
            Log.consoleLog(ifr, "HLExternalAPIs:mCallBureauHL->minLoanamount:" + minLoanmaount);
            if (BureauType.equalsIgnoreCase("EX")) {
                Log.consoleLog(ifr, "HLExternalAPIs:mCallBureauHL->BureauType->" + BureauType);
                ExperianAPI EXP = new ExperianAPI();
                String BureauScore = EXP.getExperianCIBILScore2(ifr, ProcessInstanceId, aadharNo, "CB", minLoanmaount,
                        applicantType);
                Log.consoleLog(ifr, "HLExternalAPIs:mCallBureauHL->BureauScore From Experian: " + BureauScore);
                if (BureauScore.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR + ":" + BureauScore.replaceAll("ERROR:*", "").trim()
                            + " in Experian API";
                }
            } else if (BureauType.equalsIgnoreCase("EF")) {
                Log.consoleLog(ifr, "HLExternalAPIs:mCallBureauHL->BureauType->" + BureauType);
                EquifaxAPI EQ = new EquifaxAPI();
                String BureauScore = EQ.getEquifaxCIBILScore2(ifr, ProcessInstanceId, aadharNo, "CB", minLoanmaount,
                        applicantType);
                Log.consoleLog(ifr,
                        "HLExternalAPIs:mCallBureauHL->BureauScore From EquifaxAPI: " + BureauScore);
                if (BureauScore.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR + ":" + BureauScore.replaceAll("ERROR:*", "").trim()
                            + " in Equifax API";
                }
            } else if (BureauType.equalsIgnoreCase("HM")) {
                Log.consoleLog(ifr, "HLExternalAPIs:mCallBureauHL->BureauType->" + BureauType);
                HighmarkAPI HM = new HighmarkAPI();
                String BureauScore = HM.getHighMarkCIBILScore2(ifr, ProcessInstanceId, aadharNo, "CB", minLoanmaount,
                        applicantType);
                Log.consoleLog(ifr, "HLExternalAPIs:mCallBureauHL->BureauScore From Highmark: " + BureauScore);
                if (BureauScore.contains(RLOS_Constants.ERROR)) {
                    return RLOS_Constants.ERROR + ":" + BureauScore.replaceAll("ERROR:*", "").trim()
                            + " in Highmark API";
                }
            } else if (BureauType.equalsIgnoreCase("CB")) {
                Log.consoleLog(ifr, "HLExternalAPIs:mCallBureauHL->BureauTypecalling cibil: " + BureauType);
                ConsumerAPI CB1 = new ConsumerAPI();

                String BureauScore = "";
                Log.consoleLog(ifr, "Before Calling CIBIL inside staff HL");
                BureauScore = CB1.getConsumerCIBILScore2(ifr, "CB", minLoanmaount, aadharNo, applicantType);
                Log.consoleLog(ifr,
                        "HLExternalAPIs:mCallBureauHL->BureauScore From Transunion: " + BureauScore);
                if (BureauScore.contains(RLOS_Constants.ERROR)) {
                    Log.consoleLog(ifr, "HLExternalAPIs:mCallBureauHL-> " + RLOS_Constants.ERROR + ":"
                            + BureauScore.replaceAll("ERROR:*", "").trim() + " in Transunion API");
                    return RLOS_Constants.ERROR + ":" + BureauScore.replaceAll("ERROR:*", "").trim()
                            + " in Transunion API";
                }
            } 
            Log.consoleLog(ifr, "HLExternalAPIs:mCallBureauHL->after calling BureauType: " + BureauType);
        } catch (Exception ex) {
            Log.errorLog(ifr, "HLExternalAPIs:mCallBureauHL->Exception mCallBureauHL: " + ex);
        }
        return "";
    }
}

