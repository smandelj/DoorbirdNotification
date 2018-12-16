package si.srecko.doorbird.notification.servlet;

import si.srecko.doorbird.notification.controls.MainTimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Timer;

/**
 * Created by Srecko Mandelj on 14. 08. 2017.
 */
public class TimerContextListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        MainTimerTask timerTask=new MainTimerTask();
        Timer timer=new Timer();
        timer.schedule(timerTask, 1000, 120000);
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
