package com.fedexsite.core.services.impl;

import com.fedexsite.core.services.MigrationConnectivityService;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.osgi.services.HttpClientBuilderFactory;
import org.apache.http.util.EntityUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * OSGI Service class which contains end point URL, Connection Timeout and Socket Timeout.
 */
@Component(service = MigrationConnectivityService.class)
@Designate(ocd = MigrationConnectivityServiceConfig.class)
public class MigrationConnectivityServiceImpl implements MigrationConnectivityService {

  private int connectionTimeOut;

  private int socketTimeOut;

  private String migrationEndpoint;

  private String domainName;

  @Reference
  HttpClientBuilderFactory httpClientBuilderFactory;

  @Override
  public int getConnectionTimeOut() { return connectionTimeOut; }

  @Override
  public int getSocketTimeOut() { return socketTimeOut; }

  @Override
  public String getMigrationEndpoint() { return migrationEndpoint; }

  @Override
  public String getDomainName() { return domainName; }


  @Activate
  protected void activate(final MigrationConnectivityServiceConfig config) {
    connectionTimeOut = config.getConnectionTimeOut();
    socketTimeOut = config.getSocketTimeOut();
    migrationEndpoint = config.getMigrationEndpoint();
    domainName = config.getDomainName();
  }

  /**
   * method to fetch Json response from MediaStack end point which will used for creating
   *
   * @return Response Body as String
   */
  @Override
  public String executeGetRequest(String date) throws IOException {

    HttpGet httpGet = createGetRequestForMigration(date);
    HttpClientBuilder builder = httpClientBuilderFactory.newBuilder();
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(getConnectionTimeOut())
        .setSocketTimeout(getSocketTimeOut())
        .build();
    builder.setDefaultRequestConfig(requestConfig);
    String responseString = StringUtils.EMPTY;
    try(CloseableHttpClient httpClient = builder.build()){
      HttpResponse response =  httpClient.execute(httpGet);
      if(response.getStatusLine().getStatusCode() == HttpServletResponse.SC_OK) {
        responseString = EntityUtils.toString(response.getEntity());
      }else {
        throw new IOException("Call to url failed" + EntityUtils.toString(response.getEntity()));
      }
    }
    return responseString;
  }


}

@ObjectClassDefinition(name = "Fedex Migration Service",
    description = "Fedex Migration Service")
@interface MigrationConnectivityServiceConfig {

  @AttributeDefinition(name = "Connection Timeout in Milli Seconds",
      description = "Connection Timeout in Milli Seconds for Migration endpoint")
  int getConnectionTimeOut() default 5000;

  @AttributeDefinition(name = "Socket Timeout in Milli Seconds",
      description = "Socket Timeout in Milli Seconds for Migration endpoint")
  int getSocketTimeOut() default 5000;

  @AttributeDefinition(name = "Migration End Point",
      description = "Migration End Point to fetch the content to be migrated")
  String getMigrationEndpoint() default "/rss/cricket";

  @AttributeDefinition(name = "Domain Name",
      description = "Domain Name")
  String getDomainName() default "https://sports.ndtv.com";

}
