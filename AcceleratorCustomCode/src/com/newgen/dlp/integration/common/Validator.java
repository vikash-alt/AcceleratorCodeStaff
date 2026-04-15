package com.newgen.dlp.integration.common;

import com.newgen.iforms.custom.IFormReference;
import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author skalidindi
 */
public interface Validator {
    ValidationResult validate(IFormReference ifr,JSONObject account,String jsonKey,String jsonValue);
     ValidationResult isEmptyOrNullCheck(JSONObject account ,String jsonValue);
     ValidationResult validate(JSONObject account,String jsonKey,int jsonValue);
     String getValue(IFormReference ifr,JSONObject json,String key,String defaultValue);
    List<String> getFlatList(IFormReference ifr,List<List<String>> nestedList,String filterCondition);
}
