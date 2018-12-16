package cs263w16;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;

@SuppressWarnings("serial")
public class DatastoreServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      resp.setContentType("text/html");
      resp.getWriter().println("<html><body>");
      
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
      syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));

      
      if(!req.getParameterNames().hasMoreElements()){  // if no parameters passed in
      	try{
      		Query fetchAllTaskData = new Query("TaskData");
	      	PreparedQuery results = datastore.prepare(fetchAllTaskData); 
	      	resp.getWriter().println("<b><i>Items of kind \'TaskData\' in the datastore. </b></i><br/><br/>");
	  		for(Entity taskDataElement : results.asIterable()){
	        	resp.getWriter().println("<b>Item in datastore</b><br/>");
	        	if(syncCache.get(taskDataElement.getKey()) != null){
	        		resp.getWriter().println("<b>Item in memcache as well.</b><br/>");
	        	}
	        	resp.getWriter().println("" + taskDataElement.getProperty("value") + "<br/>");
	        	resp.getWriter().println("" + taskDataElement.getProperty("date") + "<br/><br/>");
	    	}



	    }catch(Exception e){
	    	resp.getWriter().println("Sorry. " + e.getMessage());
	    }
      }
      else if(req.getParameter("keyname") != null && req.getParameter("value") == null){
      	try{
      		
      		String result = (String)syncCache.get(KeyFactory.createKey("TaskData", req.getParameter("keyname")));
      		if(result != null){
      			resp.getWriter().println("Found Entity in memcache with key: " + req.getParameter("keyname") + "<br/>");
	      		resp.getWriter().println("<b> " + result + "</b><br/><br/>");
	        	//date not stored in memcache
	        	//resp.getWriter().println("<b> " + result.getProperty("date") + "</b><br/>");	
      		}
      		else{
      			Entity result2 = datastore.get(KeyFactory.createKey("TaskData", req.getParameter("keyname")));
      			syncCache.put(result2.getKey(), result2.getProperty("value")); // Populate cache.
	      		resp.getWriter().println("Found Entity in datastore with key: " + req.getParameter("keyname") + "<br/>");
	      		resp.getWriter().println("" + result2.getProperty("value") + "<br/>");
	        	      resp.getWriter().println("" + result2.getProperty("date") + "<br/><br/>");	
	            }
      	}catch(Exception e){
      		resp.getWriter().println("Sorry, no such data! " + e.getMessage());
      	}
      }
      else if(req.getParameter("keyname") != null && req.getParameter("value") != null){
      	try{
      		Entity ntd = new Entity("TaskData", req.getParameter("keyname"));
      		ntd.setProperty("value", req.getParameter("value"));
      		ntd.setProperty("date", new Date());
      		datastore.put(ntd);
      		syncCache.put(ntd.getKey(), ntd.getProperty("value")); // Populate cache.
      		resp.getWriter().println("Stored KEY: " + req.getParameter("keyname") + " and VALUE: " + req.getParameter("value") + " in Datastore and Memcache.");
      	}catch(Exception e){
      		resp.getWriter().println("Something went wrong. " + e.getMessage());	
      	}
      }
      else{
      	resp.getWriter().println("Error: unexpected request parameters");	
      }

      resp.getWriter().println("</body></html>");
  }
}
