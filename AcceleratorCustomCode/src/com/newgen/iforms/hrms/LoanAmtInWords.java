/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.newgen.iforms.hrms;

import com.newgen.iforms.properties.Log;

/**
 *
 * @author ranshaw
 */
public class LoanAmtInWords {
	
	 private static final String[] units = {
	            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
	            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
	    };
	    
	    private static final String[] tens = {
	            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
	    };
 
	    public static String convertToWords(double amount) {
	        if (amount == 0) {
	            return "Zero";
	        }
 
	        String words = "";
 
	        // Split into integer and decimal parts
	        long wholePart = (long) amount;
	        long decimalPart = Math.round((amount - wholePart) * 100);
 
	        // Convert whole part
	        if (wholePart > 0) {
	            words += convertWholeNumberToWords(wholePart) + " Rupees ";
                    Log.consoleLog(null, "words in rupees "+ words);
	        }
 
	        // Convert decimal part
	        if (decimalPart > 0) {
	            words += "and " + convertWholeNumberToWords(decimalPart) + " Paise";
                    Log.consoleLog(null, "words in Paise "+ words);
	        }
 
	        return words.trim();
	    }
 
	    private static String convertWholeNumberToWords(long number) {
	        if (number == 0) {
	            return "";
	        }
 
	        String words = "";
 
	        // Handle lakhs, thousands, hundreds, and so on
	        if (number >= 100000) {
	            words += convertWholeNumberToWords(number / 100000) + " Lakh ";
	            number %= 100000;
                    Log.consoleLog(null, "words in lakh "+ words);
	        }
	        if (number >= 1000) {
	            words += convertWholeNumberToWords(number / 1000) + " Thousand ";
	            number %= 1000;
                    Log.consoleLog(null, "words in Thousand "+ words);
	        }
	        if (number >= 100) {
	            words += convertWholeNumberToWords(number / 100) + " Hundred ";
	            number %= 100;
                    Log.consoleLog(null, "words in hundred "+ words);
	        }
	        if (number >= 20) {
	            words += tens[(int) (number / 10)] + " ";
	            number %= 10;
                    Log.consoleLog(null, "words in tens "+ words);
	        }
	        if (number > 0) {
	            words += units[(int) number] + " ";
                    Log.consoleLog(null, "words in zero "+ words);
	        }
 Log.consoleLog(null, "words last statement "+ words);
	        return words.trim();
	    }
	    
	    public static String amtInWords(double loanAmt) {
	    	String amountInWords = convertToWords(loanAmt);
			return amountInWords;
	            
	    }
           
 
}
