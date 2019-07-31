package es.rcs.tfm.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

import es.rcs.tfm.api.ApiNames;
import es.rcs.tfm.db.DbNames;
import es.rcs.tfm.nlp.NlpNames;
import es.rcs.tfm.solr.IndexNames;
import es.rcs.tfm.srv.SrvNames;
import es.rcs.tfm.web.WebNames;
import es.rcs.tfm.xml.XmlNames;

@ComponentScan(basePackages = {
		AppNames.APP_CONFIG_PKG,
		XmlNames.XML_CONFIG_PKG,
		DbNames.DB_CONFIG_PKG,
		IndexNames.IDX_CONFIG_PKG,
		NlpNames.NLP_CONFIG_PKG,
		SrvNames.SRV_CONFIG_PKG,
		ApiNames.API_CONFIG_PKG,
		WebNames.WEB_CONFIG_PKG
		})
public class AppBoot implements WrapperListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(AppBoot.class);

	static { //runs when the main class is loaded.
	    System.setProperty("logback-access.debug", "false");
	    System.setProperty("org.jboss.logging.provider", "slf4j");
	}
	
	private static Class<AppBoot> applicationClass = AppBoot.class;
	private static ConfigurableApplicationContext context;

	/*---------------------------------------------------------------
    * Main Method
    *-------------------------------------------------------------*/
	public static void main(String[] args) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("main ... begin");
		}

		WrapperManager.start( new AppBoot(), args );
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("main ... end");
		}
	}
	

   /*---------------------------------------------------------------
    * WrapperListener Methods
    *-------------------------------------------------------------*/
   /**
    * The start method is called when the WrapperManager is signaled by the 
    * native Wrapper code that it can start its application.  This
    * method call is expected to return, so a new thread should be launched
    * if necessary.
    *
    * @param args List of arguments used to initialize the application.
    *
    * @return Any error code if the application should exit on completion
    *         of the start method.  If there were no problems then this
    *         method should return null.
    */
   public Integer start( String[] args ) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("start ... begin");
		}

		Thread t = Thread.currentThread();
		t.setName(AppNames.APP_NAME + " Start");

		try {
			context = SpringApplication.run(applicationClass, args);
		} catch (Exception ex) {
			LOG.warn("start ex " + ex.toString());
		}
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("start ... end");
		}

		return null;
   }

   /**
    * Called when the application is shutting down.  The Wrapper assumes that
    * this method will return fairly quickly.  If the shutdown code code
    * could potentially take a long time, then WrapperManager.signalStopping()
    * should be called to extend the timeout period.  If for some reason,
    * the stop method can not return, then it must call
    * WrapperManager.stopped() to avoid warning messages from the Wrapper.
    *
    * @param exitCode The suggested exit code that will be returned to the OS
    *                 when the JVM exits.
    *
    * @return The exit code to actually return to the OS.  In most cases, this
    *         should just be the value of exitCode, however the user code has
    *         the option of changing the exit code if there are any problems
    *         during shutdown.
    */
   public int stop( int exitCode ) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("stop ... begin");
		}

		Thread t = Thread.currentThread();
		t.setName(AppNames.APP_NAME + " Stop");

		context.close();
		
		if (LOG.isDebugEnabled()) {
			LOG.debug("stop ... end");
		}

		exitCode = 0;
       
       return exitCode;
   }
   
   /**
    * Called whenever the native Wrapper code traps a system control signal
    * against the Java process.  It is up to the callback to take any actions
    * necessary.  Possible values are: WrapperManager.WRAPPER_CTRL_C_EVENT, 
    * WRAPPER_CTRL_CLOSE_EVENT, WRAPPER_CTRL_LOGOFF_EVENT, or 
    * WRAPPER_CTRL_SHUTDOWN_EVENT
    *
    * @param event The system control signal.
    */
   public void controlEvent( int event )
   {
       if (	(event == WrapperManager.WRAPPER_CTRL_LOGOFF_EVENT) && 
				(WrapperManager.isLaunchedAsService())) {
			// Ignore
       } else {
			WrapperManager.stop( 0 );
			// Will not get here.
       }
   }	
	
}
