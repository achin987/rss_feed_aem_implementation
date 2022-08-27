package com.fedexsite.core.utils;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationOptions;
import com.day.cq.replication.Replicator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.jcr.Session;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceUtils {

  private static final Logger logger = LoggerFactory.getLogger(ResourceUtils.class);

  // Private Constructor to avoid initialization
  private ResourceUtils(){

  }

  /**
   * Method of fetch node. If the node does not exist, it will be created
   *
   * @param parentResource - Parent Node below which node will be created
   * @param nodeName - name of the new node
   * @param nodeType - primary type of the code
   * @param resolver - resourceResolver object to create node
   * @return Resouce fetched or created if not exist
   * @throws PersistenceException
   */
  public static Resource createOrGetResource(Resource parentResource, String nodeName, String nodeType, ResourceResolver resolver)
      throws PersistenceException {
    Resource child = parentResource.getChild(nodeName);
    if(null == child){
      child = resolver.create(parentResource, nodeName, Collections
          .singletonMap(JcrConstants.JCR_PRIMARYTYPE, nodeType));
      resolver.commit();
    }
    return child;
  }

  /**
   * Replicate the content fragments using the Replication API
   *
   * @param paths
   * @param resourceResolver
   * @param replicator
   */
  public static void replicateContent(List<String> paths, ResourceResolver resourceResolver, Replicator replicator, ReplicationActionType action) {
    if(paths.isEmpty()){
      return;
    }

    try {
      // Create leanest replication options for activation
      ReplicationOptions options = new ReplicationOptions();
      // Do not create new versions as this adds to overhead
      options.setSuppressVersions(true);
      // Avoid sling job overhead by forcing synchronous. Note this will result in serial activation.
      options.setSynchronous(true);
      // Do NOT suppress status update of resource (set replication properties accordingly)
      options.setSuppressStatusUpdate(false);

      //Rep the content   replicate(Session session, ReplicationActionType type, String path)
      replicator.replicate(resourceResolver.adaptTo(Session.class), action, paths.toArray(new String[0]), null);
    } catch (ReplicationException e) {
      logger.error("**** Error while replicating Node : {} ", e.getMessage());
    }
  }
}
