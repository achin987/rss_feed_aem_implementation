package com.fedexsite.core.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fedexsite.core.services.impl.MigrationConnectivityServiceImpl;
import com.fedexsite.core.utils.FedexConstants;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.osgi.services.HttpClientBuilderFactory;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class MigrationConnectivityServiceTest {

  private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

  private String writeService = "writeService";

  private String readService = "readService";

  @InjectMocks
  MigrationConnectivityServiceImpl serviceImpl = new MigrationConnectivityServiceImpl();

  @Mock
  HttpClientBuilderFactory httpClientBuilderFactory;

  @Mock
  HttpClientBuilder builder;

  @Mock
  CloseableHttpClient httpClient;

  @Mock
  CloseableHttpResponse response;

  @Mock
  StatusLine statusLine;

  @Mock
  HttpEntity entity;

  @Test
  void testGetConnection() throws IOException {
    String currentDate = new SimpleDateFormat(FedexConstants.YYYY_MM_DD).format(
        Calendar.getInstance().getTime());
    when(httpClientBuilderFactory.newBuilder()).thenReturn(builder);
    when(builder.build()).thenReturn(httpClient);
    when(httpClient.execute(any())).thenReturn(response);
    when(response.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(400);
    when(response.getEntity()).thenReturn(entity);
    Assertions.assertThrows(IOException.class, () -> {
      serviceImpl.executeGetRequest(currentDate);
    });
  }

}
