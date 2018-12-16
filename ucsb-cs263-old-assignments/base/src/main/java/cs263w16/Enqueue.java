package cs263w16;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.logging.*;
import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;
import com.google.appengine.api.taskqueue.*;


// The Enqueue servlet should be mapped to the "/enqueue" URL.

public class Enqueue extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try{
        	String key = request.getParameter("keyname");
        	String value = request.getParameter("value");
        	// Add the task to the default queue.
        	if(key == "" || value == "")
        		throw new java.lang.IllegalArgumentException();
        	
        	Queue queue = QueueFactory.getDefaultQueue();
        	queue.add(TaskOptions.Builder.withUrl("/worker").param("keyname", key).param("value", value));

        	response.sendRedirect("/Done.html");
        }
        catch(Exception e){
        	response.getWriter().println("Sorry. Please fix the error: " + e.getMessage());
        }
    }
}