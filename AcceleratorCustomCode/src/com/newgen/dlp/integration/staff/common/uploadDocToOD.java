package com.newgen.dlp.integration.staff.common;

import java.io.File;
import java.io.IOException;


import com.newgen.iforms.*;
import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.user.*;

import com.newgen.iforms.xmlapi.IFormCallBroker;
import com.newgen.omni.jts.cmgr.XMLParser;
import com.newgen.iforms.properties.Log;


import ISPack.CPISDocumentTxn;
import ISPack.ISUtil.JPDBRecoverDocData;
import ISPack.ISUtil.JPISException;
import ISPack.ISUtil.JPISIsIndex;

public class uploadDocToOD {

	
	Log log = new Log();

	public String uploadDocToOD (IFormReference ifr, String Name){
		String processInstanceId = ifr.getObjGeneralData().getM_strProcessInstanceId();
		String docIndex = "";
		try {

			String sSessionId="",
					destinationPath = System.getProperty("user.dir")+"/MBCDocUpload/"+Name+".pdf",
					sIpAddress = null,comments="",documentName = null;
			int sessionId=0;

			File fileObj = null;

			long lFileLength;

			int volumeId = 1,jtsPort = 0;

//			boolean adddocument = false;

			documentName = Name;

			//Properties propObj = new Properties();

			String cabinetName =ifr.getObjGeneralData().getM_strEngineName();

			log.consoleLog(ifr, "cabinetName : " + cabinetName);

			sSessionId=ifr.getObjGeneralData().getM_strDMSSessionId();

			sessionId = Integer.parseInt(sSessionId);

			log.consoleLog(ifr, "sessionId : " + sessionId);

			//String groupIndex = "0";

			fileObj = new File(destinationPath);

			log.consoleLog(ifr, "fileObj : " + fileObj + " \n destinationPath : " + destinationPath);

			lFileLength = fileObj.length();

			log.consoleLog(ifr, "lFileLength : " + lFileLength);

			//iNoOfPages = 1;

			JPISIsIndex ISINDEX = new JPISIsIndex();

			log.consoleLog(ifr, "ISINDEX : " + ISINDEX);

			JPDBRecoverDocData JPISDEC = new JPDBRecoverDocData();

			log.consoleLog(ifr, "JPISDEC : " + JPISDEC);

			JPISDEC.m_cDocumentType = 'N';

			log.consoleLog(ifr, "JPISDEC.m_cDocumentType : " + JPISDEC.m_cDocumentType);

			JPISDEC.m_nDocumentSize = (int) lFileLength;

			log.consoleLog(ifr, "lFileLength : " + lFileLength);

			log.consoleLog(ifr, "JPISDEC.m_nDocumentSize : " + JPISDEC.m_nDocumentSize);

			volumeId=ifr.getObjGeneralData().getM_iVolId();

			JPISDEC.m_sVolumeId = (short)volumeId;

			log.consoleLog(ifr, "volumeId : " + volumeId);

			log.consoleLog(ifr, "JPISDEC.m_sVolumeId : " + JPISDEC.m_sVolumeId);

			sIpAddress=ifr.getObjGeneralData().getM_strJTSIP();

			jtsPort=Integer.parseInt(ifr.getObjGeneralData().getM_strJTSPORT());

			log.consoleLog(ifr, " jtsPort : " + jtsPort + " sIpAddress : " + sIpAddress+ " cabinetName : " + cabinetName+ " sessionId : " + sessionId+ " lFileLength : " + lFileLength+ " fileObj : " + fileObj+ " ISINDEX : " + ISINDEX+ " JPISDEC : " + JPISDEC+ " JPISDEC.m_nDocumentSize : " + JPISDEC.m_nDocumentSize+ " JPISDEC.m_sVolumeId : " + JPISDEC.m_sVolumeId+ " JPISDEC.m_sVolumeId : " + JPISDEC.m_sVolumeId);

			CPISDocumentTxn.AddDocument_MT(null, sIpAddress, (short)jtsPort, cabinetName, (short)volumeId, destinationPath, JPISDEC, "", ISINDEX);

			log.consoleLog(ifr, "Document added to IS successfully.");

			String commentsChOut=comments;

			if(commentsChOut.indexOf("_")>-1) {

			//	commentsChOut=commentsChOut.substring(0,commentsChOut.lastIndexOf("_"));

			}
	 
			String query ="select folderindex from pdbfolder where name='"+processInstanceId+"'";

			String inputXmlSelect = "<?xml version='1.0'?><APSelect_Input><Option>APSelect</Option><Query>" + query + "</Query><SessionId>" + sSessionId + "</SessionId><EngineName>" + cabinetName + "</EngineName></APSelect_Input>";

			log.consoleLog(ifr, "Mohit-count inputXmlSelect :::" + inputXmlSelect);

			String outputXmlSelect = IFormCallBroker.execute(inputXmlSelect, ifr.getObjGeneralData().getM_strJTSIP(),Integer.parseInt(ifr.getObjGeneralData().getM_strJTSPORT()));

			log.consoleLog(ifr, " in count outputXmlSelect :::"+outputXmlSelect);

			XMLParser xmlParser = new XMLParser();

			xmlParser.setInputXML((outputXmlSelect));

			String mainCode = xmlParser.getValueOf("MainCode");//todo

			String folderindex="";

			if(mainCode.equals("0")) {

				//adddocument = true;

				folderindex = xmlParser.getNextValueOf("td");

				log.consoleLog(ifr, "Adding document");

			}
	 
			String adddocinputxml="<?xml version='1.0'?>\r\n" + 

					"<NGOAddDocument_Input>\r\n" + 

					"	<Option>NGOAddDocument</Option>\r\n" + 

					"	<CabinetName>"+cabinetName+"</CabinetName>\r\n" + 

					"	<UserDBId>"+sessionId+"</UserDBId>\r\n" + 

					"	<GroupIndex>0</GroupIndex>\r\n" + 

					"	<ParentFolderIndex>"+folderindex+"</ParentFolderIndex>\r\n" + 

					"	<DocumentName>"+documentName+"</DocumentName>\r\n" + 

					"	<CreatedByAppName>pdf</CreatedByAppName>\r\n" + 

					"	<Comment>Others</Comment>\r\n" + 

					"	<VolumeIndex>"+volumeId+"</VolumeIndex>\r\n" + 

					"	<FilePath>"+destinationPath+"</FilePath>\r\n" + 

					"	<ProcessDefId>"+ifr.getObjGeneralData().getM_strProcessDefId()+"</ProcessDefId>\r\n" + 

					"	<ISIndex>"+ISINDEX.m_nDocIndex+"#"+volumeId+"</ISIndex>\r\n" + 

					"	<NoOfPages>1</NoOfPages>\r\n" + 

					"	<AccessType>I</AccessType>\r\n" + 

					"	<VersionFlag>Y</VersionFlag>\r\n" + 

					"	<DocumentType>N</DocumentType>\r\n" + 

					"	<DocumentSize>"+JPISDEC.m_nDocumentSize+"</DocumentSize>\r\n" + 

					"</NGOAddDocument_Input>";
	 
			log.consoleLog(ifr, "adddocinputxml is ::  " + adddocinputxml);

			String adddocoutputxml = IFormCallBroker.execute(adddocinputxml, ifr.getObjGeneralData().getM_strJTSIP(),Integer.parseInt(ifr.getObjGeneralData().getM_strJTSPORT()));
			docIndex = adddocoutputxml.substring(adddocoutputxml.indexOf("<DocumentIndex>")+15, adddocoutputxml.indexOf("</DocumentIndex>"));
			//String adddocoutputxml = WFCallBroker.execute(adddocinputxml, sIpAddress, jtsPort,1);

			log.consoleLog(ifr, "adddocoutputxml is ::  " + adddocoutputxml);

			//					}
	 
		}catch (ArithmeticException | ArrayIndexOutOfBoundsException  | NumberFormatException | IOException e) {
			Log.consoleLog(ifr, "Exception :: " + e);
		} catch(Exception e) {

			log.consoleLog(ifr, " Inside Exception for upload to OD :: " +e.getStackTrace());

		} catch (JPISException e) {

			// TODO Auto-generated catch block

			log.consoleLog(ifr, " Inside JPISException for upload to OD :: " +e.getStackTrace());

		}
		
		return docIndex ;

	}


}
