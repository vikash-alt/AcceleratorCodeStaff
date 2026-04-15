/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.hrms;

 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
 
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 
import com.newgen.dlp.integration.common.APICommonMethods;
 
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.portalAcceleratorCode.PortalCommonMethods;
import com.newgen.iforms.properties.Log;
 
public class OmnidocDownload {
	PortalCommonMethods pcm = new PortalCommonMethods();
	public String downloadDocumentFromOD(IFormReference ifr, String documentIndex) {
 
        Log.consoleLog(ifr, "#downloadTestDocumentFromOD===>");
        try {
            APICommonMethods cm = new APICommonMethods();
            JSONParser jsonparser = new JSONParser();
            String response;
            //String url = "http://192.168.12.44:8088/OmniDocsRestWS/rest/services/getDocumentJSON";
           String url = pcm.getConstantValue(ifr, "ODDOWNLOAD", "URL");
            Log.consoleLog(ifr, "#url===>" + url);
 
            String jsonin = "{\n"
                    + "  \"NGOGetDocumentBDO\": {\n"
                    + "    \"cabinetName\": \"" + ifr.getObjGeneralData().getM_strEngineName() + "\",\n"
                    + "    \"siteId\": \"" + ifr.getObjGeneralData().getM_iSiteId() + "\",\n"
                    + "    \"volumeId\": \"" + ifr.getObjGeneralData().getM_iVolId() + "\",\n"
                    + "    \"userName\": \"\",\n"
                    + "    \"userPassword\": \"\",\n"
                    + "    \"userDBId\": \"" + ifr.getObjGeneralData().getM_strDMSSessionId() + "\",\n"
                    + "    \"locale\": \"" + "EN" + "\",\n"
                    + "    \"passAlgoType\": \"\",\n"
                    + "    \"docIndex\": \"" + documentIndex + "\",\n"
                    + "    \"encrFlag\": \"\"\n"
                    + "  }\n"
                    + "}";
 
            JSONObject jsonObject = (JSONObject) jsonparser.parse(jsonin);
 
            Log.consoleLog(ifr, "jsonObject :- " + jsonObject.toString());
 
            response = getDocumentInBase64Formate(ifr, jsonObject, url);
            Log.consoleLog(ifr, "OD get Document response " + response);
 
            JSONObject returnrespone = new JSONObject();
            returnrespone = cm.getDownloadParams(ifr, response);
            Log.consoleLog(ifr, "returnrespone==>" + returnrespone.toString());
 
            return returnrespone.toString();
 
        } catch (Exception e) {
            Log.consoleLog(ifr, "download doc exception:- " + e.getMessage());
            Log.consoleLog(ifr, "download doc exception:- " + e.getLocalizedMessage());
 
        }
        return "{\"statusCode\":\"500\"}";
    }
 
    public static String getDocumentInBase64Formate(IFormReference ifr, JSONObject jsonObject, String downloadURL) {
        HttpURLConnection httpConn = null;
        URLConnection connection = null;
        OutputStream out = null;
        BufferedReader in = null;
        String responseString;
        String responseJSon = "";
        try {
            URL url = new URL(downloadURL);
            connection = url.openConnection();
            httpConn = (HttpURLConnection) connection;
 
            httpConn.setRequestProperty("Content-Type", "application/json");
            httpConn.setRequestMethod("POST");
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);
            out = httpConn.getOutputStream();
            out.write(jsonObject.toJSONString().getBytes());
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
 
                in = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
                while ((responseString = in.readLine()) != null) {
                    responseJSon = responseJSon + responseString;
                }
            } else {
                responseJSon = "{\"statusCode\":\"500\"}";
            }
 
        } catch (IOException e) {
 
        }
 
        return responseJSon;
 
    }
 
}
 
 
