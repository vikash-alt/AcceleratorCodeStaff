/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.staffHL;

import com.newgen.iforms.commons.CommonFunctionality;

/**
 *
 * @author skalidindi
 */
public class  CommonFunctionalityCreator {
   private static volatile CommonFunctionality cf=null;
    private CommonFunctionalityCreator(){
        
    }
    public static CommonFunctionality  getInstance(){
        if(cf==null){
            synchronized (CommonFunctionalityCreator.class) {
                if(cf==null){
                    cf=new CommonFunctionality();
                }
            }
        }
        return cf;
    }
    
}
