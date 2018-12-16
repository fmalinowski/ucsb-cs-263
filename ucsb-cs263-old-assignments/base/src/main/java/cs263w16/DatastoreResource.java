package cs263w16;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.logging.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;

//Map this class to /ds route
@Path("/ds")
public class DatastoreResource {
  // Allows to insert contextual objects into the class,
  // e.g. ServletContext, Request, Response, UriInfo
  @Context
  UriInfo uriInfo;
  @Context
  Request request;
  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  MemcacheService  syncCache = MemcacheServiceFactory.getMemcacheService();
  // Return the list of entities to the user in the browser
  @GET
  @Produces(MediaType.TEXT_XML)
  public List<TaskData> getEntitiesBrowser() {
    //datastore dump -- only do this if there are a small # of entities
    // assuming 'small' = 50
    try{
      Query fetchAllTaskData = new Query("TaskData");
      List<TaskData> list = new ArrayList<TaskData>();
      for(Entity t : datastore.prepare(fetchAllTaskData).asList(FetchOptions.Builder.withLimit(50))){
        list.add(new TaskData((String)t.getProperty("keyname"), (String)t.getProperty("value"), (Date)t.getProperty("date")));
      }      
      return list;
    }
    catch(Exception e){
      System.out.println("Get: could not get datastore dump.");
      return null;
    }
  }

  // Return the list of entities to applications
  @GET
  @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
  public List<TaskData> getEntities() {
    //datastore dump -- only do this if there are a small # of entities
    //same code as above method
    try{
      Query fetchAllTaskData = new Query("TaskData");
      List<TaskData> list = new ArrayList<TaskData>();
      for(Entity t : datastore.prepare(fetchAllTaskData).asList(FetchOptions.Builder.withLimit(50))){
        list.add(new TaskData((String)t.getProperty("keyname"), (String)t.getProperty("value"), (Date)t.getProperty("date")));
      }   
      return list;
    }
    catch(Exception e){
      System.out.println("Get: could not get datastore dump.(Limited to 50 items)");
      return null;
    }
  }    

  //Add a new entity to the datastore
  @POST
  @Produces(MediaType.TEXT_HTML)
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public void newTaskData(@FormParam("keyname") String keyname,
      @FormParam("value") String value,
      @Context HttpServletResponse servletResponse) throws IOException {
    Date date = new Date();
    System.out.println("Posting new TaskData: " + keyname + " val: " + value + " ts: " + date);

    //add your code here
    
    try{
          syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO)); 
          Entity ntd = new Entity("TaskData", keyname);
          ntd.setProperty("value", value);
          ntd.setProperty("date", new Date());
          datastore.put(ntd);
          syncCache.put(ntd.getKey(), ntd.getProperty("value")); // Populate cache.
          servletResponse.sendRedirect("/Done.html"); 
    }catch(Exception e){
          servletResponse.sendRedirect("/Error.html"); 
    }
  }

  //The @PathParam annotation says that keyname can be inserted as parameter after this class's route /ds
  @Path("{keyname}")
  public TaskDataResource getEntity(@PathParam("keyname") String keyname) {
    System.out.println("GETting TaskData for " +keyname);
    return new TaskDataResource(uriInfo, request, keyname);
  }
}
