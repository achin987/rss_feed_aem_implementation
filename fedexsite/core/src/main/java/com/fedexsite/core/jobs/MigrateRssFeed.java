package com.fedexsite.core.jobs;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.adobe.cq.dam.cfm.ContentFragmentException;
import com.adobe.cq.dam.cfm.FragmentTemplate;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.Replicator;
import com.fedexsite.core.dtos.Channel;
import com.fedexsite.core.dtos.Feed;
import com.fedexsite.core.dtos.Item;
import com.fedexsite.core.dtos.RSS;
import com.fedexsite.core.services.MigrationConnectivityService;
import com.fedexsite.core.utils.FedexConstants;
import com.fedexsite.core.utils.ResourceUtils;
import com.fedexsite.core.utils.XMLUtils;
import com.google.gson.Gson;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = JobConsumer.class,
    immediate = true,
    property = {
        Constants.SERVICE_DESCRIPTION + "=Migration of RSS Feed",
        JobConsumer.PROPERTY_TOPICS + "=rssfeed/migration/job"
    })
public class MigrateRssFeed implements JobConsumer {

  private final Logger logger = LoggerFactory.getLogger(MigrateRssFeed.class);

  @Reference
  private MigrationConnectivityService migrationConnectivityService;

  @Reference
  private ResourceResolverFactory resolverFactory;

  @Reference
  private Replicator replicator;

  @Override
  public JobResult process(Job job) {
    String date = (String)job.getProperty(FedexConstants.DATE);
    try(ResourceResolver resourceResolver = resolverFactory.getServiceResourceResolver(FedexConstants.AUTH_INFO)){
      Resource cfParentResc = getOrCreateFragmentFolder(resourceResolver);
      String response = migrationConnectivityService.executeGetRequest(date);
      logger.debug("XML Response : {}" , response);
      String jsonResponse = XMLUtils.convertXMLtoJson(response);
      logger.debug("JSON Response : {}" , jsonResponse);

      Gson gson = new Gson();
      Feed feed = gson.fromJson(jsonResponse, Feed.class);
      RSS rss = feed.getRss();

      Resource templateResc = resourceResolver.resolve("/conf/fedexsite/settings/dam/cfm/models/rss-feed-fragment-model");

      Channel channel = rss.getChannel();
      List<Item> itemList = channel.getItem();
      List<String> newContentFragments = new ArrayList<>();
      for(Item item : itemList){
        FragmentTemplate fragmentTemplate = templateResc.adaptTo(FragmentTemplate.class);
        if(StringUtils.isNotBlank(item.getTitle()) && (null == cfParentResc.getChild(String.valueOf(item.getGuid().getContent()))) ){
            createContentFragment(item, cfParentResc, fragmentTemplate, newContentFragments);
        }
      }
      resourceResolver.commit();
      ResourceUtils.replicateContent(newContentFragments, resourceResolver, replicator, ReplicationActionType.ACTIVATE);
    } catch (IOException e) {
      logger.error("Error while fecthing data : {}", e.getMessage());
      return JobResult.FAILED;
    } catch (LoginException e) {
      logger.error("Error while fecthing resolver from system user : {}", e.getMessage());
      return JobResult.FAILED;
    } catch (ContentFragmentException e) {
      logger.error("Error while creating content fragment : {}", e.getMessage());
      return JobResult.FAILED;
    }
    return JobConsumer.JobResult.OK;
  }

  /**
   * @param item - new Article dto with data from json
   * @param parentResource - Language folder below which content fragment will be created
   * @param fragmentTemplate - Template object to create fragment
   * @throws ContentFragmentException - Exception thrown if there is any issue while creating content fragment
   */
  private void createContentFragment(Item item, Resource parentResource,
      FragmentTemplate fragmentTemplate, List<String> newContentFragments) throws ContentFragmentException {
    ContentFragment contentFragment = fragmentTemplate.createFragment(parentResource, JcrUtil.createValidName(String.valueOf(item.getGuid().getContent())), item.getTitle());
    Resource master = contentFragment.adaptTo(Resource.class).getChild(JcrConstants.JCR_CONTENT).getChild(FedexConstants.DATA).getChild(FedexConstants.MASTER);
    ModifiableValueMap data = master.adaptTo(ModifiableValueMap.class);
    if (item.getTitle() != null) data.put(FedexConstants.TITLE,item.getTitle());
    if (item.getDescription() != null) data.put(FedexConstants.DESCRIPTION,item.getDescription());
    if (item.getLink() != null) data.put(FedexConstants.LINK,item.getLink());
    if (item.getUpdated() != null) data.put(FedexConstants.UPDATED,item.getUpdated());
    if(item.getGuid() != null){
      data.put(FedexConstants.CONTENT,item.getGuid().getContent());
    }
    newContentFragments.add(parentResource.getPath() + "/" + JcrUtil.createValidName(String.valueOf(item.getGuid().getContent())));
  }

  /**
   * Method to get or create a dam folder with today's date to place the Content Fragments inside them
   *
   * @param resourceResolver - Resolver object to get or create resource
   * @return
   * @throws PersistenceException
   */
  private Resource getOrCreateFragmentFolder(ResourceResolver resourceResolver)
      throws PersistenceException {
    Resource mediasiteDam = ResourceUtils.createOrGetResource(resourceResolver.getResource(FedexConstants.FEDEXSITE_DAM_PATH), FedexConstants.CONTENT_FRAGMENTS, FedexConstants.SLING_FOLDER, resourceResolver);
    String currentDate = new SimpleDateFormat(FedexConstants.YYYY_MM_DD).format(Calendar.getInstance().getTime());
    Resource contentFragmentFolder = ResourceUtils.createOrGetResource(mediasiteDam, currentDate, FedexConstants.SLING_FOLDER, resourceResolver);
    ResourceUtils.createOrGetResource(contentFragmentFolder, JcrConstants.JCR_CONTENT, JcrConstants.NT_UNSTRUCTURED, resourceResolver);
    return contentFragmentFolder;
  }
}
