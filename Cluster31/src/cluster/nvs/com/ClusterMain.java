package cluster.nvs.com;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.TimeUnit;

import auto.cluster.nvs.com.AutoSelfStatusWatch;
import auto.cluster.nvs.com.AutoStartService;
import diag.cluster.nvs.com.AutoSelfDiagnostic;
import obj.cluster.nvs.com.CluRemoteSystem;
import quorum.nvs.com.QuorumControlService;
import quorum.nvs.com.WebUIServiceQuorum;



public class ClusterMain {

	public static ClusterMain srv = null;
	public static WebUIService webSrv = null;
	public static WebUIServiceQuorum webSrvQ = null;
	
	
	public globalData gData = new globalData();

	public static void main(String[] args) {
		if (args.length == 0) {
			start();
			while (true) {
				try {
					Thread.sleep(60 * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if (args.length == 1) {

			if (args[0].equals("start")) {

				start();
				while (true) {
					try {
						Thread.sleep(60 * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			} else if (args[0].equals("stop")) {
				System.exit(0);
			}

		}
	}

	public static void start() {
		srv = new ClusterMain();

		LoadLibrary();
		srv.gData.init();

		srv.gData.logger.info("starting cluster manager...Version:" + globalData.getVersionInfo());
		srv.gData.logger.info("runtime details:" + srv.gData.getSystemProperties());

		
		System.setProperty("javax.net.ssl.keyStore","servercert.jks");
		System.setProperty("javax.net.ssl.keyStorePassword","password");
		System.setProperty("file.encoding","UTF-8");
		System.setProperty("sun.jnu.encoding","UTF-8");


		if(srv.gData.commonParams.containsKey("jmxRemotePort")) {
			
			System.setProperty("com.sun.management.jmxremote.port",srv.gData.commonParams.get("jmxRemotePort"));
			System.setProperty("com.sun.management.jmxremote.ssl","false");
			System.setProperty("com.sun.management.jmxremote.authenticate","false");

		}
		
		
		
		if(!srv.gData.commonParams.containsKey("workMode")) {
			
				startAgent();
			
		} else {
			
			if(srv.gData.commonParams.get("workMode").equals("quorum"))

				startQuorum();

			else

				startAgent();
			
		}
			


		while (true) {
			try {
				Thread.sleep(300 * 1000);
			} catch (InterruptedException e) {

				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));
				srv.gData.logger.severe(errors.toString());
			}
		}
	}

	
	public static void startAgent() {
		
	
		srv.gData.workMode = globalData.WORK_MODE_AGENT;
		
		if (srv.gData.commonParams.containsKey("autoDiagnosticIntervalSec")) {			
			int interval= Integer.valueOf(srv.gData.commonParams.get("autoDiagnosticIntervalSec"));
			int delay= 30;
			
			
			srv.gData.service.scheduleAtFixedRate(new AutoSelfDiagnostic(srv.gData), delay, interval, TimeUnit.SECONDS);
			srv.gData.logger.info("AutoSelfDiagnostic monitor srarted...");
			
			
		}
		
		
		
		if (srv.gData.IamMaster) {
			srv.gData.service.scheduleAtFixedRate(new AutoSelfStatusWatch(srv.gData), 0, 5, TimeUnit.SECONDS); // Delay
																												// = 0
			srv.gData.logger.info("AutoSelfStatusWatch monitor srarted...");
			
			int delay= Integer.valueOf(srv.gData.commonParams.get("autoServiceFirstDelaySec"));
			int interval= Integer.valueOf(srv.gData.commonParams.get("autoServiceIntervalSec"));
			
			
			if( srv.gData.commonParams.get("autoServiceStart").equals("true")) {				
								
				srv.gData.service.scheduleAtFixedRate(new AutoStartService(srv.gData), delay, interval, TimeUnit.SECONDS);
				srv.gData.logger.info("AutoStartScenario monitor srarted...");				

			}else {
				srv.gData.logger.info("AutoStartScenario monitor is DISABLED...");
			}
		
		}

		webSrv = new WebUIService(srv.gData);
		srv.gData.service.execute(webSrv);

		
		
		srv.gData.logger.info("All services in mode agent srarted...");
		srv.gData.logger.info("**************++++++++++*****************");
	
	}

	public static void startQuorum() {
		
		srv.gData.workMode = globalData.WORK_MODE_QUORUM;
		
		
		int delay = 5;
		
		
		for(CluRemoteSystem rs: srv.gData.rSystems) {

			int interval = Integer.valueOf(rs.params.get("scanQuorumAskStatusIntervalSec"));
			srv.gData.service.scheduleAtFixedRate(new QuorumControlService(srv.gData, rs), delay , interval, TimeUnit.SECONDS);
			srv.gData.logger.info("Service " + rs.id + " in mode quorum srarted...");

		}
		
		
		webSrvQ = new WebUIServiceQuorum(srv.gData);
		srv.gData.service.execute(webSrvQ);

		
		srv.gData.logger.info("All services in mode quorum srarted...");
		srv.gData.logger.info("**************----------******************");
		
	}
	
	
	public static void stop() {

		System.exit(0);

	}

	private static void LoadLibrary() {
		String currentLibraryPath = System.getProperty("user.dir") + File.separator + "lib";
		String fileName = "";
		File folder = new File(currentLibraryPath);

		String[] files = folder.list();

		for (String file : files) {
			try {
				fileName = currentLibraryPath + File.separator + file;

				if (fileName.endsWith("jar")) {
					addSoftwareLibrary(new File(fileName));
				}
			} catch (Exception e) {
				srv.gData.logger.severe(e.getMessage());
			}

		}

	}

	private static void addSoftwareLibrary(File file) throws Exception {
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
		method.setAccessible(true);
		method.invoke(ClassLoader.getSystemClassLoader(), new Object[] { file.toURI().toURL() });
	}

	
}
