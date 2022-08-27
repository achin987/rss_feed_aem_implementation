package com.fedexsite.core.utils;

import java.util.Collections;
import java.util.Map;
import org.apache.sling.api.resource.ResourceResolverFactory;

public class FedexConstants {

  //Content Fragment Constants
  public static final String DATA = "data";
  public static final String MASTER = "master";
  public static final String TITLE = "title";
  public static final String DESCRIPTION = "description";
  public static final String LINK = "link";
  public static final String CONTENT = "content";
  public static final String UPDATED = "updated";

  // RSS feed Constants
  public static final String DATE = "date";
  public static final String YYYY_MM_DD = "yyyy-MM-dd";

  // Request Constants
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String CONTENT_TYPE_VALUE = "application/xml";
  public static final String USER_AGENT = "User-Agent";
  public static final String USER_AGENT_VALUE = "PostmanRuntime/7.26.8";
  public static final String ACCEPT = "Accept";
  public static final String ACCEPT_VALUE = "*/*";
  public static final String HOST = "Host";
  public static final String HOST_VALUE = "sports.ndtv.com";

  // DAM FOLDER CONSTANTS
  public static final String CONTENT_FRAGMENTS = "content-fragments";
  public static final String SLING_FOLDER = "sling:Folder";
  public static final String FEDEXSITE_DAM_PATH = "/content/dam/fedexsite";

  /*The CONSTANT WriteService*/
  public static final String WRITE_SERVICE = "writeService";
  public static final Map<String, Object> AUTH_INFO = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, WRITE_SERVICE);

}
