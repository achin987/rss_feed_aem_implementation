package com.fedexsite.core.jobs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import com.fedexsite.core.services.MigrationConnectivityService;
import com.fedexsite.core.utils.FedexConstants;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.jcr.Session;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class MigrateRssfeedTest {

  private final AemContext context = new AemContext(ResourceResolverType.RESOURCERESOLVER_MOCK);

  @InjectMocks
  MigrateRssFeed fixture = new MigrateRssFeed();

  @Mock
  MigrationConnectivityService migrationConnectivityService;

  @Mock
  ResourceResolverFactory resolverFactory;

  @Mock
  Replicator replicator;

  @Mock
  Job job;

  @Mock
  ResourceResolver resourceResolver;

  @Mock
  ModifiableValueMap modifiableValueMap;

  @Mock
  FragmentTemplate fragmentTemplate;

  @Mock
  Resource resource;

  @Mock
  ContentFragment contentFragment;

  @BeforeEach
  void setUp() {
    context.build().resource(FedexConstants.FEDEXSITE_DAM_PATH, "sling:resourceType", "sling:Folder").commit();
    context.build().resource(FedexConstants.FEDEXSITE_DAM_PATH + "/" + FedexConstants.CONTENT_FRAGMENTS, "sling:resourceType", "sling:Folder").commit();
    String currentDate = new SimpleDateFormat(FedexConstants.YYYY_MM_DD).format(
        Calendar.getInstance().getTime());
    context.build().resource(FedexConstants.FEDEXSITE_DAM_PATH + "/" + FedexConstants.CONTENT_FRAGMENTS + "/" + currentDate, "sling:resourceType", "sling:Folder").commit();
    context.build().resource(FedexConstants.FEDEXSITE_DAM_PATH + "/" + FedexConstants.CONTENT_FRAGMENTS + "/" + currentDate + "/" + JcrConstants.JCR_CONTENT, "sling:resourceType", "nt:unstructured").commit();
    context.currentResource(FedexConstants.FEDEXSITE_DAM_PATH + "/" + FedexConstants.CONTENT_FRAGMENTS + "/" + currentDate + "/" + JcrConstants.JCR_CONTENT);
    context.registerAdapter(Resource.class, ModifiableValueMap.class, modifiableValueMap);
    context.registerAdapter(Resource.class, FragmentTemplate.class, fragmentTemplate);
    context.registerAdapter(ContentFragment.class, Resource.class, resource);
    context.registerService(MigrationConnectivityService.class, migrationConnectivityService);
    context.registerService(ResourceResolverFactory.class, resolverFactory);
    context.registerService(Replicator.class, replicator);
  }

  @Test
  void run() throws LoginException, IOException, ContentFragmentException {
    when(resolverFactory.getServiceResourceResolver(FedexConstants.AUTH_INFO)).thenReturn(resourceResolver);
    when(resourceResolver.getResource(FedexConstants.FEDEXSITE_DAM_PATH)).thenReturn(resource);
    when(resource.getChild(anyString())).thenReturn(resource);
    when(resource.adaptTo(ModifiableValueMap.class)).thenReturn(modifiableValueMap);
    when(resource.adaptTo(FragmentTemplate.class)).thenReturn(fragmentTemplate);
    when(contentFragment.adaptTo(Resource.class)).thenReturn(resource);
    String currentDate = new SimpleDateFormat(FedexConstants.YYYY_MM_DD).format(
        Calendar.getInstance().getTime());
    when(job.getProperty(FedexConstants.DATE)).thenReturn(currentDate);
    when(migrationConnectivityService.executeGetRequest(currentDate)).thenReturn(fetchTestXML());
    when(resourceResolver.resolve("/conf/fedexsite/settings/dam/cfm/models/rss-feed-fragment-model")).thenReturn(resource);
    when(fragmentTemplate.createFragment(any(), anyString(), anyString())).thenReturn(contentFragment);
    fixture.process(job);
  }

  @Test
  void runWithoutResource()
      throws LoginException, IOException, ContentFragmentException, ReplicationException {
    when(resolverFactory.getServiceResourceResolver(FedexConstants.AUTH_INFO)).thenReturn(resourceResolver);
    when(resourceResolver.getResource(FedexConstants.FEDEXSITE_DAM_PATH)).thenReturn(resource);
    when(resourceResolver.create(any(), anyString(), any())).thenReturn(resource);
    doNothing().when(resourceResolver).commit();
    when(resource.getChild(JcrConstants.JCR_CONTENT)).thenReturn(resource);
    when(resource.getChild(FedexConstants.DATA)).thenReturn(resource);
    when(resource.getChild(FedexConstants.MASTER)).thenReturn(resource);
    when(resource.getChild("content-fragments")).thenReturn(null);
    when(resource.getChild("3292952")).thenReturn(null);
    when(resource.adaptTo(ModifiableValueMap.class)).thenReturn(modifiableValueMap);
    when(resource.adaptTo(FragmentTemplate.class)).thenReturn(fragmentTemplate);
    when(contentFragment.adaptTo(Resource.class)).thenReturn(resource);
    String currentDate = new SimpleDateFormat(FedexConstants.YYYY_MM_DD).format(
        Calendar.getInstance().getTime());
    when(resource.getChild(currentDate)).thenReturn(null);
    when(job.getProperty(FedexConstants.DATE)).thenReturn(currentDate);
    when(migrationConnectivityService.executeGetRequest(currentDate)).thenReturn(fetchTestXML());
    when(resourceResolver.resolve("/conf/fedexsite/settings/dam/cfm/models/rss-feed-fragment-model")).thenReturn(resource);
    when(fragmentTemplate.createFragment(any(), anyString(), anyString())).thenReturn(contentFragment);
    //doNothing().when(replicator).replicate(resourceResolver.adaptTo(Session.class), ReplicationActionType.ACTIVATE, new String[0], any());
    fixture.process(job);
  }

  public static String fetchTestXML(){
    return "<rss>"
        + "\t<channel>\n"
        + "\t\t<item>\n"
        + "\t\t\t<guid isPermaLink=\"false\">3292952</guid>\n"
        + "\t\t\t<link>https://sports.ndtv.com/cricket/rassie-van-der-dussen-ruled-out-of-england-vs-south-africa-test-series-3292952#rss-sports-cricket</link>\n"
        + "\t\t\t<title>Rassie van der Dussen Ruled Out Of England vs South Africa Test Series</title>\n"
        + "\t\t\t<description>ENG vs SA: South Africa batter Rassie van der Dussen is out of the third Test against England with a finger injury. Wiaan Mulder has replaced him.</description>\n"
        + "\t\t\t<updated>2022-08-27T23:50:59+05:30</updated>\n"
        + "\t\t</item>\n"
        + "\t\t<item>\n"
        + "\t\t\t<guid isPermaLink=\"false\">3292952</guid>\n"
        + "\t\t\t<link>https://sports.ndtv.com/cricket/rassie-van-der-dussen-ruled-out-of-england-vs-south-africa-test-series-3292952#rss-sports-cricket</link>\n"
        + "\t\t\t<title>Rassie van der Dussen Ruled Out Of England vs South Africa Test Series</title>\n"
        + "\t\t\t<description>ENG vs SA: South Africa batter Rassie van der Dussen is out of the third Test against England with a finger injury. Wiaan Mulder has replaced him.</description>\n"
        + "\t\t\t<updated>2022-08-27T23:50:59+05:30</updated>\n"
        + "\t\t</item>\n"
        + "\t</channel>\n"
        + "</rss>\n";
  }

}
