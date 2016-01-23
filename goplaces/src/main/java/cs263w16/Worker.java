package cs263w16;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;

// The Worker servlet should be mapped to the "/worker" URL.
public class Worker extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try{
        	String key = request.getParameter("keyname");
        	String value = request.getParameter("value");
        	// Do something with key.
        	Date date = new Date();
        	Entity entity = new Entity("TaskData", key);
        	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
            syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
        	syncCache.put(entity.getKey(), value); // Populate cache.
            entity.setProperty("value", value);
      		entity.setProperty("date", new Date());
      		datastore.put(entity);
        }
        catch(Exception e){
        	response.sendRedirect("/Error.html");
        }

    }
}