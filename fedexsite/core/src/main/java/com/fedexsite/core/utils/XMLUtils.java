package com.fedexsite.core.utils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XMLUtils {

  private static final Logger logger = LoggerFactory.getLogger(XMLUtils.class);

  // Private constructor to avoid initialization
  private XMLUtils(){

  }

  /**
   * Convert XML to JSON
   *
   * @param responseString
   * @return
   */
  public static String convertXMLtoJson(String responseString){
    String jsonString = StringUtils.EMPTY;
    try {
      jsonString =  XML.toJSONObject(responseString).toString();
    } catch (JSONException e) {
      logger.error("Error while converting XML to Json", e);
    }
    return jsonString;
  }
}
