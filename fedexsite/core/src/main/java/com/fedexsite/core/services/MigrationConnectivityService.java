package com.fedexsite.core.services;

import com.fedexsite.core.utils.FedexConstants;
import java.io.IOException;
import org.apache.http.client.methods.HttpGet;

public interface MigrationConnectivityService {

  int getConnectionTimeOut();

  int getSocketTimeOut();

  String getMigrationEndpoint();

  String getDomainName();

  /**
   * method to fetch Json response from mediastack end point which will used for updating components inside the page
   *
   * @param @param offset - paramter to fecth the news article from the offset + 100
   * @return Response Body as String
   */
  String executeGetRequest(String date) throws IOException;

  /**
   * Created GetRequest Object for the migration end point
   *
   * @return GetRequest Object
   */
  default HttpGet createGetRequestForMigration(String date){
    StringBuilder endPoint = new StringBuilder(getDomainName() + getMigrationEndpoint());
    HttpGet getRequest = new HttpGet(endPoint.toString());
    getRequest.addHeader(FedexConstants.CONTENT_TYPE, FedexConstants.CONTENT_TYPE_VALUE);
    getRequest.setHeader(FedexConstants.USER_AGENT, FedexConstants.USER_AGENT_VALUE);
    getRequest.setHeader(FedexConstants.ACCEPT, FedexConstants.ACCEPT_VALUE);
    getRequest.setHeader(FedexConstants.HOST, FedexConstants.HOST_VALUE);
    return getRequest;
  }
}
