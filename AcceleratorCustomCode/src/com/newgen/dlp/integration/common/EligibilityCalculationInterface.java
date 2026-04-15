package com.newgen.dlp.integration.common;

import com.newgen.iforms.custom.IFormReference;
import org.json.simple.JSONObject;

public interface EligibilityCalculationInterface {
    JSONObject EligibilityCalculationAsLTVNewVehicle(IFormReference ifr);
    JSONObject EligibilityCalculationAsLTVUsedVehicle(IFormReference ifr);
	JSONObject EligibilityAsPerRequestedAmt(IFormReference ifr, String value);
	JSONObject EligibilityAsPerRequestedAmtRO(IFormReference ifr, String value);
	//JSONObject EligibilityAsPerRequestedAmtHL(IFormReference ifr, String value);
	//JSONObject EligibilityAsPerRequestedAmtHLRO(IFormReference ifr, String value);
	JSONObject EligibilityAsPerRequestedAmtGold(IFormReference ifr, String value);
	JSONObject EligibilityAsPerRequestedAmtPortalGold(IFormReference ifr, String value);
    
}
