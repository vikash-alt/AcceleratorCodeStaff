/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.CBS;

import com.newgen.iforms.AccCBSProperty.CBSReadProperty;
import com.newgen.iforms.commons.CommonFunctionality;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author bhojagude.suresh
 */
public class CBSCommonIntegrationCode {

    CommonFunctionality cf = new CommonFunctionality();

    public String getCustomStackTrace(IFormReference ifr, Exception e) {
        String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
        String username = ifr.getUserName();
        String activityName = ifr.getActivityName();
        String logData = username + " - " + activityName + " - " + processInstanceId;
        Log.consoleLog(ifr, "Check Error Logs for : " + logData);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return logData + " :\n" + sw.toString();
    }

    public String getCommonInput() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        Date sysdate = new Date();
        String requestId = "req_" + sdf.format(sysdate);
        return requestId;
    }

    public String connectToSocket(IFormReference ifr, String inputMessage, String socketType) {
        String cbsIp;
        int cbsport;
        if ("webserviceSocket".equalsIgnoreCase(socketType)) {
            cbsIp = CBSReadProperty.getCBSProperty("CBSUtilityServerIP");
            cbsport = Integer.parseInt(CBSReadProperty.getCBSProperty("CBSUtilityServerPort"));
        } else {
            cbsIp = CBSReadProperty.getCBSProperty("CBSUtilityServerIP");
            cbsport = Integer.parseInt(CBSReadProperty.getCBSProperty("DocumentGenerationUtilityServerPort"));
        }
        try (Socket s = new Socket(cbsIp, cbsport); DataInputStream din = new DataInputStream(s.getInputStream());
                DataOutputStream dout = new DataOutputStream(s.getOutputStream());) {
            writeDataToSocket(ifr, dout, inputMessage);
            s.setSoTimeout(Integer.parseInt(CBSReadProperty.getCBSProperty("CBSSoTimeout")));
            String messageRtn = readDataFromSocket(ifr, din);
            Log.consoleLog(ifr, "Socket Closed!");
            return messageRtn;
        } catch (IOException | NumberFormatException e) {
            Log.errorLog(ifr, getCustomStackTrace(ifr, e));
            return "Socket time out exception";
        }
    }

    public boolean writeDataToSocket(IFormReference ifr, DataOutputStream dOut, String sData) {
        boolean bFlag = false;
        Log.consoleLog(ifr, "Data to Write from client to server: " + sData);
        try {
            if (sData != null && sData.length() > 0) {
                String len = String.format("%09d", sData.length());
                dOut.write(len.getBytes("UTF-8"));
                dOut.write(sData.getBytes("UTF-8"));
                bFlag = true;
            } else {
                Log.consoleLog(ifr, "sRequest is Blank.");
            }
        } catch (Exception e) {
            Log.errorLog(ifr, getCustomStackTrace(ifr, e));
            bFlag = false;
        }
        return bFlag;
    }

    public String readDataFromSocket(IFormReference ifr, DataInputStream dIn) {
        Log.consoleLog(ifr, "readDataFromSocket readDataFromSocket :: " + new Date().getTime());
        String sData = "";
        Long endMili = 0l;
        Long startmili = 0l;
        try {
            byte[] buffer = new byte[9];
            startmili = System.currentTimeMillis();
            Log.consoleLog(ifr, "After api execution startmili: " + startmili);
            dIn.read(buffer, 0, 9);
            int iFile = Integer.parseInt(new String(buffer, "UTF-8"));
            buffer = new byte[iFile];
            int len = 0;
            int reclen = 0;
            while ((len = dIn.read(buffer)) > 0) {
                Log.consoleLog(ifr, "Inside while loop ::");
                byte[] arrayBytes = new byte[len];
                System.arraycopy(buffer, 0, arrayBytes, 0, len);
                sData += new String(arrayBytes, "UTF-8");
                reclen += len;
                if (reclen >= iFile) {
                    break;
                }
            }
            endMili = System.currentTimeMillis();
            Log.consoleLog(ifr, "After api execution executed: " + endMili);
            Log.consoleLog(ifr, "Difference after api execution executed: " + (endMili - startmili));
        } catch (SocketTimeoutException st) {
            endMili = System.currentTimeMillis();
            Log.errorLog(ifr, "SocketTimeoutException in catch SocketTimeoutException : " + getCustomStackTrace(ifr, st));
            Log.consoleLog(ifr, "After api execution in catch SocketTimeoutException: " + endMili);
            Log.consoleLog(ifr,
                    "Difference after api execution in catch SocketTimeoutException: " + (endMili - startmili));
        } catch (Exception e) {
            endMili = System.currentTimeMillis();
            Log.consoleLog(ifr, "After api execution in Exception: " + endMili);
            Log.consoleLog(ifr, "Difference after api execution in Exception: " + (endMili - startmili));
            Log.errorLog(ifr, getCustomStackTrace(ifr, e));
        }
        Log.consoleLog(ifr, "Data Received from Socket Server: " + sData);
        return sData;
    }

    public Boolean getAPIExecutionStatus(IFormReference ifr, String api) {
        String query = "select LoanNumber,AccOpeningDate,LOANLINKSTATUS from LOS_C_CHECKCUSTOMERLOAN where "
                + "pid='" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "' and LOANLINKSTATUS='" + api + "'";
        Log.consoleLog(ifr, "Query to check LoanNumber:" + query);
        List<List<String>> resultLoan = ifr.getDataFromDB(query);
        Log.consoleLog(ifr, "Query result:" + resultLoan);
        if (resultLoan.size() > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    public void minsertLoanStatus(IFormReference ifr, String accountId, String Product, String APIName) {
        String query = "insert into LOS_C_CHECKCUSTOMERLOAN(pid,AccOpeningDate,LoanNumber,ProductType,LOANLINKSTATUS) "
                + "values ('" + ifr.getObjGeneralData().getM_strProcessInstanceId() + "','"
                + cf.getCurrentDateTime(ifr) + "','" + accountId + "','" + Product + "','" + APIName + "')";
        Log.consoleLog(ifr, "Query to insert enter new customer:" + query);
        int resultupdate = ifr.saveDataInDB(query);
        Log.consoleLog(ifr, "Query result:" + resultupdate);
    }

    public String getCBSID(IFormReference ifr, String request) {
        String requestMaker = "";
        try {
            String wiName = ifr.getObjGeneralData().getM_strProcessInstanceId();
            String query = "";
            if (request.equalsIgnoreCase("maker")) {
                query = "select CBS_ID from los_master_employee where "
                        + "upper(LOSEmployeeID) =  upper((select submituser from los_nl_action_history where "
                        + "insertionorderid=(select max(insertionorderid) from los_nl_action_history where "
                        + "submitstagename='Detailed Data Entry' and pid ='"
                        + wiName + "')))";
            } else {
                query = "select CBS_ID from los_master_employee where upper(LOSEmployeeID) =upper('"
                        + ifr.getObjGeneralData().getM_strUserName() + "')";
            }
            List<List<String>> requestMakerOutput = cf.mExecuteQuery(ifr, query, "getRequestMaker Query:");
            if (requestMakerOutput.size() > 0) {
                if (!requestMakerOutput.get(0).get(0).isEmpty()) {
                    requestMaker = requestMakerOutput.get(0).get(0);
                }
            }
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception inside getRequestMaker" + e);
            Log.errorLog(ifr, "Exception inside getRequestMaker" + e);
            return requestMaker;
        }
        return requestMaker;
    }

    public String checkNullValueForNumber(String value) {
        if ("".equals(value) || value == null) {
            return "0";
        }
        return value;
    }

    public boolean isNullOrEmpty(Object object) {
        if (object != null && !object.toString().trim().equalsIgnoreCase("")) {
            return false;
        }
        return true;
    }

    public String checkNullValueForString(String value) {
        if ("".equals(value) || value == null) {
            return "";
        }
        return value;
    }

    public String mFetchDateForCBS(IFormReference ifr, String date) {
        Log.consoleLog(ifr, "Inside mFetchDateForCBS:");
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            SimpleDateFormat format1 = new SimpleDateFormat("ddMMyyyy");
            return format1.format(format.parse(date));
        } catch (Exception e) {
            Log.consoleLog(ifr, "Exception inside mFetchDateForCBS" + e);
            Log.errorLog(ifr, "Exception inside mFetchDateForCBS" + e);
        }
        return "";
    }

    public String mCheckDetails(IFormReference ifr, String[] arr) {
        for (int i = 0; i < arr.length; i++) {
            String value = ifr.getValue(arr[i]).toString();
            if (value.equalsIgnoreCase("")) {
                JSONObject returnJSON = new JSONObject();
                returnJSON.put("showMessage", cf.showMessage(ifr, arr[i], "error", "Kindly Enter value of " + arr[i].split("_")[3]));
                returnJSON.put("saveWorkitem", "true");
                return returnJSON.toString();
            }
        }
        return "";
    }
}
