/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.staffHL;

import com.newgen.iforms.custom.IFormReference;

/**
 *
 * @author skalidindi
 */
public abstract class EligibilityHandler {

     EligibilityHandler next=null;
     public EligibilityHandler  setNext(EligibilityHandler next,IFormReference ifr){
         this.next=next;
         return next;
     }
    public abstract String handle(EligibilityContext context,IFormReference ifr);
    
}
