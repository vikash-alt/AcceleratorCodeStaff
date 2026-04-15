package com.newgen.dlp.integration.common;

/**
*
* @author skalidindi
*/
public class NoError implements ValidationResult {

   @Override public boolean hasError() { return false; }
   @Override public String getMessage() { return "NoError"; }   
}
