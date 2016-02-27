package goplaces.apis;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.taskqueue.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Mapped to /enqueue_boxrouteworker_task.
 * This API supports POST requests to add BoxRouteWorker background task to the default queue.
 *
 * @author Aviral Takkar
 *
 * TODO Document the curl command to access API using POST request.
 */

public class BoxRouteWorkerAPI extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try{
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            String route_id = request.getParameter("route_id");
            String radius = request.getParameter("radius");
            String keywords = request.getParameter("keywords");

            Entity originalRouteEntity = datastore.get(KeyFactory.createKey("Route", Long.parseLong(route_id)));
            Text originalRouteJsonText = (Text)originalRouteEntity.getProperty("routeJSON");
            String originalRouteJSON = originalRouteJsonText.getValue();
            Queue queue = QueueFactory.getDefaultQueue();
            queue.add(TaskOptions.Builder.withUrl("/boxroute").param("originalroutejsontext", originalRouteJSON)
                    .param("routeid", route_id).param("radius", radius)
                    .param("keywords", keywords));
            response.sendRedirect("/BoxRouterTaskAdded.html");
        }
        catch(Exception e){
            response.sendRedirect("/TaskAddError.html");
            System.out.println(e.getMessage());
        }
    }
    protected void doGet(HttpServletRequest req,HttpServletResponse resp)
            throws ServletException, java.io.IOException {
        resp.sendRedirect("/UnsupportedRequest.html");
    }
}