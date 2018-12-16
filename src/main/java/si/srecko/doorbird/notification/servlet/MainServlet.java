package si.srecko.doorbird.notification.servlet;

import si.srecko.doorbird.notification.controls.MainController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: Srecko Mandelj
 * Date: 28.4.2016
 * Time: 11:07
 * To change this template use File | Settings | File Templates.
 */
public class MainServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        String event=httpServletRequest.getParameter("event");
        if(event==null) return;
        MainController mainController=new MainController();
        mainController.doWork(event);
    }
}
