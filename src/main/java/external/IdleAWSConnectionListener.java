package external;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.amazonaws.http.IdleConnectionReaper;

/**
 * Application Lifecycle Listener implementation class IdleAWSConnectionListener
 *
 */
public class IdleAWSConnectionListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public IdleAWSConnectionListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce)  { 
        try {
            IdleConnectionReaper.shutdown();
        } catch (Throwable t) {
            // log the error
        }
    }

	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent sce)  { 
         
    }
	
}
