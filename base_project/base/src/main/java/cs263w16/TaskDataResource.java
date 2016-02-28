package cs263w16;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.logging.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;


public class TaskDataResource {
  @Context
  UriInfo uriInfo;
  @Context
  Request request;
  String keyname;
  DatastoreService datastore;
  MemcacheService syncCache;

  public TaskDataResource(UriInfo uriInfo, Request request, String kname) {
    this.uriInfo = uriInfo;
    this.request = request;
    this.keyname = kname;
    datastore = DatastoreServiceFactory.getDatastoreService();
    syncCache = MemcacheServiceFactory.getMemcacheService();
    syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
  }
  // for the browser
  @GET
  @Produces(MediaType.TEXT_XML)
  public TaskData getTaskDataHTML() {
    //add your code here (get Entity from datastore using this.keyname)
    // throw new RuntimeException("Get: TaskData with " + keyname +  " not found");
    //if not found
  
    try{
        String result = (String)syncCache.get(KeyFactory.createKey("TaskData", keyname));
        if(result != null){
          TaskData taskData = new TaskData(keyname, result, new Date());  
          return taskData;
        }
        Entity result2 = datastore.get(KeyFactory.createKey("TaskData", keyname));
        TaskData taskData = new TaskData((String)result2.getProperty("keyname"), (String)result2.getProperty("value"), (Date)result2.getProperty("date"));
        return taskData;
    }
    catch(Exception e){
         throw new RuntimeException("Get: TaskData with keyname " + keyname +  " not found");
    }

  }
  // for the application
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public TaskData getTaskData() {
    //same code as above method
    
    try{
        String result = (String)syncCache.get(KeyFactory.createKey("TaskData", keyname));
        if(result != null){
          TaskData taskData = new TaskData(keyname, result, new Date());  
          return taskData;
        }
        Entity result2 = datastore.get(KeyFactory.createKey("TaskData", keyname));
        TaskData taskData = new TaskData((String)result2.getProperty("keyname"), (String)result2.getProperty("value"), (Date)result2.getProperty("date"));
        return taskData;
    }
    catch(Exception e){
         throw new RuntimeException("Get: TaskData with keyname " + keyname +  " not found");
    }

  }

  @PUT
  @Consumes(MediaType.APPLICATION_XML)
  public Response putTaskData(String val) {
    Response res = null;
    //add your code here
    //first check if the Entity exists in the datastore
    //if it is not, create it and 
    //signal that we created the entity in the datastore
    try{
      //signal that we updated the entity
      Entity result = datastore.get(KeyFactory.createKey("TaskData", keyname));
      result.setProperty("value", val);
      datastore.put(result);
      syncCache.put(result.getKey(), result.getProperty("value")); // Populate cache.
      res = Response.noContent().build();
    }
    catch(Exception e){
      Entity ntd = new Entity("TaskData", keyname);
      ntd.setProperty("value", val);
      ntd.setProperty("date", new Date());
      datastore.put(ntd);
      syncCache.put(ntd.getKey(), ntd.getProperty("value")); // Populate cache.
      res = Response.created(uriInfo.getAbsolutePath()).build();
    }
    return res;
  }

  @DELETE
  public void deleteIt() {

    //delete an entity from the datastore
    //just print a message upon exception (don't throw)
    try{
      syncCache.delete(KeyFactory.createKey("TaskData", keyname));
      datastore.delete(KeyFactory.createKey("TaskData", keyname));
    }
    catch(Exception e){
      System.out.println("Delete unsuccessful: keyname " + keyname); 
    }
  }

} 
