package com.newgen.dlp.integration.common;

/**
*
* @author skalidindi
*/
public class ErrorMessage implements ValidationResult{
    private final String message;
   public ErrorMessage(String message) {
       this.message = message;
   }
   @Override public boolean hasError() { return true; }
   @Override public String getMessage() { return message; }
}
