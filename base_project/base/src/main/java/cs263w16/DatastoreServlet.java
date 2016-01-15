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
      
      if(!req.getParameterNames().hasMoreElements()){  // if no parameters passed in
      	try{
      		Query fetchAllTaskData = new Query("TaskData");
	      	PreparedQuery results = datastore.prepare(fetchAllTaskData); 
	      	resp.getWriter().println("There are items of kind \'TaskData\' in the datastore. <br/>");
	  		for(Entity taskDataElement : results.asIterable()){
	        	
	        	resp.getWriter().println("<b> " + taskDataElement.getProperty("value") + "</b><br/>");
	        	resp.getWriter().println("<b> " + taskDataElement.getProperty("date") + "</b><br/>");
	    	}
	    }catch(Exception e){
	    	resp.getWriter().println("Sorry, nothing in the datastore! (or something else went wrong)");
	    }
      }
      else if(req.getParameter("keyname") != null && req.getParameter("value") == null){
      	try{
      		Entity result = datastore.get(KeyFactory.createKey("TaskData", req.getParameter("keyname")));
      		resp.getWriter().println("Found Entity in datastore with key: " + req.getParameter("keyname") + "<br/>");
      		resp.getWriter().println("<b> " + result.getProperty("value") + "</b><br/>");
        	resp.getWriter().println("<b> " + result.getProperty("date") + "</b><br/>");	
      	}catch(EntityNotFoundException e){
      		resp.getWriter().println("Sorry, nothing in the datastore by that key!");
      	}
      }
      else if(req.getParameter("keyname") != null && req.getParameter("value") != null){
      	try{
      		Entity ntd = new Entity("TaskData", req.getParameter("keyname"));
      		ntd.setProperty("value", req.getParameter("value"));
      		ntd.setProperty("date", new Date());
      		datastore.put(ntd);
      		resp.getWriter().println("Stored KEY: " + req.getParameter("keyname") + " and VALUE: " + req.getParameter("value") + " in Datastore.");
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
