package goplaces.apis;

import com.google.appengine.api.taskqueue.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * Mapped to /enqueue_waypointsreview_task.
 * This API supports POST requests to add WaypointsReview background task to the default queue.
 *
 * @author Aviral Takkar
 *
 * TODO Document the curl command to access API using POST request.
 *
 *
 */
public class WaypointsReviewAPI extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String place_ids = request.getParameter("place_ids");
            QueueFactory.getDefaultQueue().add(TaskOptions.Builder.withUrl("/waypointsreview").param("places", place_ids));
            response.sendRedirect("/WaypointsReviewTaskAdded.html");
        }
        catch(Exception e ){
            response.sendRedirect("/TaskAddError.html");
            System.out.println(e.getMessage());
        }
    }

    protected void doGet(HttpServletRequest req,HttpServletResponse resp)
            throws ServletException, java.io.IOException {
        resp.sendRedirect("/UnsupportedRequest.html");
    }
}