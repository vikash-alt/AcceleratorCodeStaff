package com.newgen.dlp.integration.common;

import com.newgen.iforms.custom.IFormReference;
import com.newgen.iforms.properties.Log;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.json.simple.JSONObject;

/**
 *
 * @author skalidindi
 */
public class KnockOffValidator implements Validator{
     private final ValidationResult noError = new NoError();
    private final ValidationResult error;

    private final Map<Boolean, ValidationResult> resultMap;

    public KnockOffValidator(String errorMessage) {
        this.error = new ErrorMessage(errorMessage);
        this.resultMap = Map.of(
            Boolean.TRUE, noError,
            Boolean.FALSE, error
        );
    }

    @Override
    public ValidationResult validate(IFormReference ifr,JSONObject account, String jsonKey, String jsonValue) {
        Log.consoleLog(ifr,"jsonKey==>"+ jsonKey+" jsonValue==>"+jsonValue+" "+account.get(jsonKey));
    boolean result=account.get(jsonKey).toString().equalsIgnoreCase(jsonValue) && account.get(jsonKey)!=null;
    return resultMap.get(result);
    }
     @Override
    public ValidationResult validate(JSONObject account, String jsonKey, int jsonValue) {
        try{
            
             boolean result=Integer.parseInt(account.get(jsonKey).toString())>jsonValue;
             return resultMap.get(!result);
        }catch(Exception e){
            return resultMap.get(true); 
        }
   
    
    }
    @Override
	public ValidationResult isEmptyOrNullCheck(JSONObject account,String jsonKey) {
		boolean val=account.get(jsonKey)!=null || !account.get(jsonKey).toString().trim().isEmpty();
		Log.consoleLog(null,"test"+ account.get(jsonKey).toString()+ " valueResult"+val);
                return resultMap.get(val);
		
	}

    @Override
    public String getValue(IFormReference ifr, JSONObject json, String key,String defaultValue) {
        return Optional.ofNullable(json.get(key).toString()).map(Object::toString)
                .filter(s->!s.trim().isEmpty()).orElse(defaultValue);
        
        //throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public List<String> getFlatList(IFormReference ifr, List<List<String>> nestedList, String filterCondition) {
    return  nestedList.stream().filter(a1 -> a1.get(1).equals(filterCondition))
					.map(a2 -> a2.get(0)).collect(Collectors.toList());   
    }

 
  
}


