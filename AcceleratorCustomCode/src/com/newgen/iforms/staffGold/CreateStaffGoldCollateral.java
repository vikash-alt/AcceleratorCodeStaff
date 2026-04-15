package com.newgen.iforms.staffGold;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.newgen.dlp.integration.common.APICommonMethods;
import com.newgen.iforms.constants.RLOS_Constants;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;

public class CreateStaffGoldCollateral {
	
	APICommonMethods cm = new APICommonMethods();

    public String getCollateral(IFormReference ifr, HashMap<String, String> mapCL, String productCode)
            throws ParseException {

        Log.consoleLog(ifr, "CreateCollateral:getCollateral->Start:::");
        String serviceName = "";
        String processInstanceId = "";
        String apiErrorCode = "";
        String apiErrorMessage = "";
        String APIStatus = "";
//        String Response = "";
        String request = "";
        String collaterID = "";
        String response = "";
        try {
            String apiName = "Collateral";
            serviceName = "CBS_" + apiName;
            processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

            String APICriterion = "'CBS_CollateralCreation'";

            Log.consoleLog(ifr, "CreateCollateral:getCollateral->Entered to Create Collateral...");

            Date currentDate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyHHmmssSSS");
            String formattedDate = dateFormat.format(currentDate);

            String BankCode = cm.getConstantValue(ifr, "CBDLP", "BANKCODE");
            String Channel = cm.getConstantValue(ifr, "CBSDGRAPH", "CHANNEL");
            String UserId = cm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");

            String TBranch = cm.GetHomeBranchCode(ifr, processInstanceId, productCode);
            String selectedBranchCode = "";

            selectedBranchCode=ifr.getValue("BRANCHCODEVL").toString();
//            String querySelectedBranchCode = "select DISB_BRANCH from SLOS_TRN_LOANDETAILS where PID='"
//                    + processInstanceId + "' ";
//            Log.consoleLog(ifr, "querySelectedBranchCode==>" + querySelectedBranchCode);
//            List<List<String>> branchCodeResult = ifr.getDataFromDB(querySelectedBranchCode);
//            if (branchCodeResult.size() > 0) {
//                selectedBranchCode = branchCodeResult.get(0).get(0);
//
//            }
//            if (!Optional.ofNullable(selectedBranchCode).isPresent()) {
//                selectedBranchCode = TBranch;
//            }

            String codChargeType = "1";
            String codColl = "";
            String codSec = "";
            String codCollQuery = "select codcoll from alos_mst_collateral_gold where codprod='" + productCode
                    + "'";
            List<List<String>> queryRes = ifr.getDataFromDB(codCollQuery);

            codColl = queryRes.get(0).get(0);
            Log.consoleLog(ifr, "codColl====>" + codColl);
            codSec = queryRes.get(0).get(0);
            Log.consoleLog(ifr, "codSec====>" + codSec);

            String datPresentVal = cm.getCurrentAPIDate(ifr);
            String datNextDue = cm.datNextDueforCollater(ifr, datPresentVal);
            String codLastMntMakerid = cm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");
            String codLastMntChkrid = cm.getConstantValue(ifr, "CBSLADFDENQ", "USERID");

            String customerId = mapCL.get("CustomerID");
            String grossWt = mapCL.get("GrossWt");
            String netWt = mapCL.get("NetWt");
            String descOfJewellery = mapCL.get("DescOfJewellery");
            String amtPresentVal = mapCL.get("AmtPresentVal");
            String landableAmountVal = mapCL.get("LandableAmountVal");

            String assetMortCrt = "Y";
            
            String ornamentsJson = dynamicornamentdata(ifr, amtPresentVal, codColl);
            
            Log.consoleLog(ifr, "ornamentsJson"+ornamentsJson);
            request = "{\n" + "            \"input\":{\n" + "               \"SessionContext\":{ \n"
                    + "                  \"SupervisorContext\":{\n"
                    + "                     \"PrimaryPassword\":\"\",\n" + "                     \"UserId\": \""
                    + UserId + "\"\n" + "                  },\n" + "                  \"BankCode\":\""
                    + BankCode + "\",\n" + "                  \"Channel\":\"" + Channel + "\", \n"
                    + "                  \"ExternalBatchNumber\":\"\", \n"
                    + "                  \"ExternalReferenceNo\":\"\", \n"
                    + "                  \"ExternalSystemAuditTrailNumber\":\"\", \n"
                    + "                  \"LocalDateTimeText\":\"\",\n"
                    + "                  \"OriginalReferenceNo\":\"\", \n"
                    + "                  \"OverridenWarnings\":\"\", \n"
                    + "                  \"PostingDateText\":\"\",\n"
                    + "                  \"ServiceCode\":\"\", \n"
                    + "                  \"SessionTicket\":\"\", \n"
                    + "                  \"TransactionBranch\":\"" + selectedBranchCode + "\",\n"
                    + "                  \"UserId\":\"" + UserId + "\", \n"
                    + "                  \"ValueDateText\":\"\" \n" + "               },\n"
                    + "               \"ExtUniqueRefId\":\"" + formattedDate + "\", \n"
                    + "               \"codColl\":\"" + codColl + "\", \n"                  
                    + "               \"codCustId\":\""
                    + customerId + "\", \n" + "               \"codChargeType\":\"" + codChargeType + "\", \n"
                    + "               \"amtPresentVal\":\"" + amtPresentVal + "\", \n"
                    + "               \"datPresentVal\":\"" + datPresentVal + "\",\n"
                    + "               \"datNextDue\":\"" + datNextDue + "\", \n"
                    + "               \"namLender\":\"\",\n" + "               \"codCustodyStatus\":\"1\",\n"
                    + "               \"namCustodian\":\"\", \n" + "               \"datDeedSent\":\"\", \n"
                    + "               \"datDeedReturn\":\"\", \n" + "               \"txtDeedDetl1\":\"\", \n"
                    + "               \"namRegnAuth\":\"\", \n" + "               \"codLastMntMakerid\":\""
                    + codLastMntMakerid + "\",\n" + "               \"codLastMntChkrid\":\"" + codLastMntChkrid
                    + "\", \n" + "               \"baCollProp\":{ \n"
                    + "                  \"amtQuitRent\":\"\", \n"
                    + "                  \"txtCollatId\":\"\", \n" + "                  \"codAreaUnit\":\"\",\n"
                    + "                  \"fltArea\":\"\",\n" + "                  \"codFreeLease\":\"\", \n"
                    + "                  \"datLeaseExpiry\":\"\",\n"
                    + "                  \"amtForcedSale\":\"\", \n" + "                  \"txtDesc1\":\"\"\n"
                    + "               },\n" + "               \"baCollCultivation\":{ \n"
                    + "                  \"codAreaWtUnit\":\"\", \n"
                    + "                  \"codAreaNo\":\"\", \n" + "                  \"txtNote1\":\"\", \n"
                    + "                  \"txtNote2\":\"\", \n" + "                  \"codSurveyNo\":\"\", \n"
                    + "                  \"textDesc1\":\"\", \n" + "                  \"textDesc2\":\"\" \n"
                    + "               },\n" + "               \"baCollAutomobile\":{ \n"
                    + "                  \"codChassisNo\":\"\", \n"
                    + "                  \"codEngineNo\":\"\",\n" + "                  \"codRegnNo\":\"\", \n"
                    + "                  \"codModel\":\"\", \n" + "                  \"codMfgYear\":\"\", \n"
                    + "                  \"txtNotes1\":\"\",\n" + "                  \"txtNotes2\":\"\",\n"
                    + "                  \"hSRPNo\":\"\",\n" + "                  \"preOwned\":\"\"\n"
                    + "               },\n" + "               \"baCollFinsec\":{ \n"
                    + "                  \"ctrUnits\":\"\", \n"
                    + "                  \"codSeriesNum1\":\"\", \n"
                    + "                  \"codFinsec\":\"" + codColl + "\",\n"
                    + "                  \"codSeriesNum2\":\"\"\n" + "               },\n"
                    + "               \"baCollNs\":{\n" + "                  \"codNscollatId\":\"\", \n"
                    + "                  \"txtDesc1\":\"\",\n" + "                  \"txtDesc2\":\"\" \n"
                    + "               },\n" + "               \"baCollCommodity\":"+ornamentsJson+",\n"
                    + "               \"baCollCattle\":{\n" + "                  \"txtBreed\":\"\", \n"
                    + "                  \"ctrAge\":\"\",\n" + "                  \"ctrNo\":\"\", \n"
                    + "                  \"ctrQty\":\"\",\n" + "                  \"txtIdMarks\":\"\", \n"
                    + "                  \"txtDsc1\":\"\" \n" + "               },\n"
                    + "               \"baCollMachinery\":{\n" + "                  \"codMake\":\"\", \n"
                    + "                  \"codSlNo\":\"\", \n" + "                  \"codRegNo\":\"\",\n"
                    + "                  \"codEngNo\":\"\", \n" + "                  \"codCapacity\":\"\", \n"
                    + "                  \"txtDes\":\"\" \n" + "               },\n"
                    + "               \"totStockVal\":\"\", \n" + "               \"totCredStock\":\"\",\n"
                    + "               \"totBookDebts\":\"\",\n" + "               \"totBookDebtsOut\":\"\", \n"
                    + "               \"codStockStmtFreq\":\"\",\n" + "               \"txtValuerName\":\"\",\n"
                    + "               \"txtValuerMobNo\":\"\",\n" + "               \"txtLongitude\":\"\",\n"
                    + "               \"txtLatitude\":\"\",\n" + "           \"assetMortCrt\":\"" + assetMortCrt
                    + "\"\n" + "               }\n" + "         }";
            Log.consoleLog(ifr, "Request====>" + request);
            response = cm.getWebServiceResponse(ifr, apiName, request);
            Log.consoleLog(ifr, "Response====>" + response);
            if (!response.equalsIgnoreCase("{}")) {
                JSONParser parser = new JSONParser();
                JSONObject responseObj = (JSONObject) parser.parse(response);
                Log.consoleLog(ifr, "CreateCollateral:getCollateral->responseObj: " + responseObj);

                String body = responseObj.get("body").toString();


                Log.consoleLog(ifr, "CreateCollateral:getCollateral->body: " + body);
                JSONObject bodyObj = (JSONObject) parser.parse(body);
                Log.consoleLog(ifr, "CreateCollateral:getCollateral->bodyObj: " + bodyObj);

                String CheckError = cm.GetAPIErrorResponse(ifr, processInstanceId, bodyObj);
                Log.consoleLog(ifr, "CreateCollateral:getCollateral->CheckError: " + CheckError);
                if (!CheckError.equalsIgnoreCase("true")) {
                    String[] ErrorData = CheckError.split("#");
                    apiErrorCode = ErrorData[0];
                    apiErrorMessage = ErrorData[1];
                } else {
                    String collateralCreationResponse = bodyObj.get("CollateralCreationResponse").toString();
                    Log.consoleLog(ifr, "CreateCollateral:getCollateral->collateralCreationResponse: " + collateralCreationResponse);
                    JSONObject collateralCreationResponseObj = (JSONObject) parser.parse(collateralCreationResponse);
                    Log.consoleLog(ifr, "CreateCollateral:getCollateral->collateralCreationResponseObj: " + collateralCreationResponseObj);
                    collaterID = collateralCreationResponseObj.get("collaterID").toString();
                    Log.consoleLog(ifr, "CreateCollateral:getCollateral->collaterID: " + collaterID);

                    if (!collaterID.equalsIgnoreCase("")) {
                        String updateCollaterIdQuery = "update SLOS_STAFF_JEWELLERY_DETAILS set collateral_id = '" + collaterID
                                + "', TOTAL_ELIGIBLE= '" + landableAmountVal + "' where winame = '" + processInstanceId + "'";
                        Log.consoleLog(ifr,
                                "CreateCollateral:getCollateral->updateCollateralIdQuery: " + updateCollaterIdQuery);

                        ifr.saveDataInDB(updateCollaterIdQuery);
                        Log.consoleLog(ifr, "CreateCollateral:getCollateral->CaptureRequestResponse Calling ");

                    }else{
                        apiErrorMessage = "Collateral Id not created";
                    }
                }

            } else {
                response = "No response from the server.";
                apiErrorMessage = "No response from the CBS server.";
            }
            String finalResponse = "";
            if (apiErrorMessage.equalsIgnoreCase("")) {
                APIStatus = "SUCCESS";
            } else {
                APIStatus = "FAIL";
            }
            if (APIStatus.equalsIgnoreCase(RLOS_Constants.SUCCESS)) {
				return collaterID;
			}
            

        } catch (Exception e) {
            Log.consoleLog(ifr, "CreateCollateral:getCollateral->Exception Block: " + e);
            Log.errorLog(ifr, "CreateCollateral:getCollateral->Exception Block: " + e);
            return "\"\"#" + e.getMessage();
        }
        finally {
        	 cm.CaptureRequestResponse(ifr, processInstanceId, serviceName, request, response, apiErrorCode,
                     apiErrorMessage, APIStatus);
		}
        return RLOS_Constants.ERROR+"#"+apiErrorMessage;
    }

    public String dynamicornamentdata(IFormReference ifr, String amtPresentVal, String codColl) {
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();

        String qualityFineNess=cm.getConstantValue(ifr, "GOLDCOLL", "QUALITYFINESS");
        String GoldPurity=cm.getConstantValue(ifr, "GOLDCOLL", "PURITY");
        Log.consoleLog(ifr, "qualityFineNess"+qualityFineNess);
        String marketValueQuery = "SELECT MARKET_VALUE FROM ALOS_M_COLLATERAL_GOLD_RATE  WHERE CARATAGE = '" + qualityFineNess + "'";
        List<List<String>> marketValueQuerylist = ifr.getDataFromDB(marketValueQuery);
        double marketValue = Double.parseDouble(marketValueQuerylist.get(0).get(0));
        
        
       
        String jewelleryQuery="SELECT DESC_OF_JEWELLERY,SUM(NO_OF_ITEMS) AS NO_OF_ITEMS,SUM(GROSS_WEIGHT) AS TOTAL_GROSS_WEIGHT,SUM(NET_WEIGHT) AS TOTAL_NET_WEIGHT FROM\r\n"
        		+ "slos_staff_jewellery_details_c where winame ='" + processInstanceId + "'"+ " GROUP BY DESC_OF_JEWELLERY";

        Log.consoleLog(ifr, "jewelleryQuery - " + jewelleryQuery);
        List<List<String>> result = ifr.getDataFromDB(jewelleryQuery);

        JSONArray jsonArray = new JSONArray();

        for (List<String> row : result) {
                       
            JSONObject obj = new JSONObject();
            obj.put("codSec", codColl);
            obj.put("grossWtVal", row.get(2));
            obj.put("netWtVal", row.get(3));

            double netWeight = Double.parseDouble(row.get(3));
            double apprValue = marketValue * netWeight;
            obj.put("amtApprVal", String.format("%.2f", apprValue));

            obj.put("txtDesc", "GOLD JEWELLARY FOR SWARNA LOAN SCHEME");
            obj.put("ornamentType", row.get(0));
            obj.put("noOfUnits", row.get(1));
            obj.put("gptmPurity", GoldPurity);

            jsonArray.put(obj);
        }

        String jsonString = jsonArray.toString();
        Log.consoleLog(ifr, "ornament dynamic script: " + jsonString);

        return jsonString;
    }

   

}
