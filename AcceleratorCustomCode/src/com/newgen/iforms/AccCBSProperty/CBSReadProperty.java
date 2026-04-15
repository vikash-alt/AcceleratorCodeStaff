/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.newgen.iforms.AccCBSProperty;

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
public class CBSReadProperty {

    public static final Properties cbsprop = new Properties();

    static {
        try {
            String filePath1 = System.getProperty(RLOS_Constants.USERDIRECTORY) + File.separatorChar + AcceleratorConstants.CBSCONFIGFOLDER + File.separator + AcceleratorConstants.CBSPROPFILENAME;
            FileInputStream fis1 = new FileInputStream(filePath1);
            cbsprop.load(fis1);
            Enumeration e = cbsprop.propertyNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement(), val = null != cbsprop.get(key) ? cbsprop.get(key).toString() : null;
                if (null == val || "".trim().equals(val)) {
                    cbsprop.setProperty(key.trim().toLowerCase(), "");
                } else {
                    cbsprop.setProperty(key.trim().toLowerCase(), val.trim());
                }
            }
        } catch (IOException ioe) {
            System.out.println("=======>Exception at ReadProperty => static block => " + ioe);
        }
    }

    public static String getCBSProperty(String key) {
        String val = cbsprop.getProperty(key.toLowerCase().trim());
        return val;
    }
}
