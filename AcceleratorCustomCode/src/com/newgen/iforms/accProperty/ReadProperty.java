/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.accProperty;

import com.newgen.iforms.AccConstants.AcceleratorConstants;
import com.newgen.iforms.constants.RLOS_Constants;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

/**
 *
 * @author vishal.yadav
 */
public class ReadProperty {

    public static final Properties prop3 = new Properties();

    static {
        try {
            String filePath1 = System.getProperty(RLOS_Constants.USERDIRECTORY) + File.separatorChar + RLOS_Constants.CONFIGFOLDER + File.separator + AcceleratorConstants.PROP3;
            FileInputStream fis1 = new FileInputStream(filePath1);
            prop3.load(fis1);
            Enumeration e = prop3.propertyNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement(), val = null != prop3.get(key) ? prop3.get(key).toString() : null;
                if (null == val || "".trim().equals(val)) {
                    prop3.setProperty(key.trim().toLowerCase(), "");
                } else {
                    prop3.setProperty(key.trim().toLowerCase(), val.trim());
                }
            }
        } catch (IOException ioe) {
            System.out.println("=======>Exception at ReadProperty => static block => " + ioe);
        }
    }

    public static String getProperty3(String key) {
        String val = prop3.getProperty(key.toLowerCase().trim());
        return val;
    }
}
