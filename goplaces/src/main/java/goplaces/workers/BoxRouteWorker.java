package goplaces.workers;

import goplaces.models.Place;
import goplaces.models.Route;


import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.json.simple.JSONArray;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import lib.GoogleMap;
import lib.RouteBoxer;
import goplaces.models.CustomizeRouteQuery;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.taskqueue.*;

public class BoxRouteWorker extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try{ 
        	JSONParser parser = new JSONParser();       	
			String originalRouteJsonString = request.getParameter("originalroutejsontext");
			JSONObject originalRouteJsonObject = (JSONObject)parser.parse(originalRouteJsonString);
			JSONObject routes = (JSONObject)((JSONArray)originalRouteJsonObject.get("routes")).get(0);
			JSONArray legs = (JSONArray)routes.get("legs");
			JSONArray steps = (JSONArray)((JSONObject)legs.get(0)).get("steps");
			// JSONObject steps = (JSONObject)steps_holder.get(0);
			String routeID = request.getParameter("routeid");
			System.out.println("Route steps loaded in a JSONArray...size is " + steps.size());



        }	
        catch(Exception e){
        	System.out.println("ERROR " + e.getMessage());
        }

    }
}

