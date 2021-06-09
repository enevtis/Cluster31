package cluster.nvs.com;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import conf.cluster.nvs.com.ConfigurationReader;
import obj.cluster.nvs.com.CluNode;
import obj.cluster.nvs.com.CluRemoteSystem;
import obj.cluster.nvs.com.CluScenarioStep;
import scenario.cluster.nvs.com.AutoScenarioThread;

public class globalData {

	public Logger logger = Logger.getLogger(this.getClass().getPackage().getName());
	public Hashtable<String, String> commonParams = new Hashtable<String, String>();
	
	public List<CluNode> nodes = new ArrayList<>();
	public List<CluRemoteSystem> rSystems = new ArrayList<>();		//только в quorum режиме
	
	public String mainPath = Paths.get(".").toAbsolutePath().normalize().toString();
	public String scriptPath = "";
	public ExecutorService executorService = Executors.newFixedThreadPool(20);
	public ScheduledExecutorService service = Executors.newScheduledThreadPool(50);	
	public String lang = "EN";
	public Hashtable<String, String> msgText = new Hashtable<String, String>(); // для перевода на EN,DE

	public Map<String, List<CluScenarioStep>> scenarioList = new HashMap<>(); // сценарии для кластера
	public String SecretKey = "A9F6D5FG43DGD210GF";

	
	public boolean debugInfo = false;
	public boolean thereIsReplication = false;
	
	
	public boolean IamMaster = false;
	public int pingTimeout = 5000; // 5 sec

	public int maxTop = 0; // max coordinate nodes on main page
	public String journalText = ""; // for common journal

	public String scenarioRunningName = "";		
	public String replicationStatus = "";
	
	public volatile AutoScenarioThread autoScenario = null;
	public volatile int sockets = 0;
	
	
	static final int WORK_MODE_AGENT = 1;
	static final int WORK_MODE_QUORUM = 2;
	
	public int workMode = 0;  // 
	
	public Map<String, String> router = new HashMap();
	
	public void init() {
	
		scriptPath = mainPath + File.separator + "conf" + File.separator;
		scriptPath += "scripts";

		ConfigurationReader cr = new ConfigurationReader(this);

		cr.readCommonParameters(commonParams);

		/// determine default agent or quorum mode		
		
		this.workMode = WORK_MODE_AGENT;
		
		if(this.commonParams.containsKey("workMode")) {

			if(this.commonParams.get("workMode").equals("quorum")) {

				this.workMode = WORK_MODE_QUORUM;

			}
		}		
		


		if (this.workMode == WORK_MODE_AGENT) 			
			init_agent();
		else 			
			init_quorum();
		
		


		if (this.commonParams.containsKey("debugInfo"))
			debugInfo = this.commonParams.get("debugInfo").equals("true") ? true : false;
		
		if (this.commonParams.containsKey("interfaceLang")) 
			this.lang = this.commonParams.get("interfaceLang");
		
		
		
	}
	
	public void init_agent() {
		
		ConfigurationReader cr = new ConfigurationReader(this);

		cr.readNodesConfig(nodes);
		cr.addDefaultScriptsToConfig(nodes);
		cr.readAllScenarios(scenarioList);
		cr.setMaxTopCoordinateOfNodeOnMainPage();

		IamMaster = cr.determineMasterRole();
		thereIsReplication = this.nodes.size() > 1 ? true : false;	
		
		translate_init();
		init_router_agent("neserver.nvs.com.");

		
	}
	public void init_quorum() {
		
		ConfigurationReader cr = new ConfigurationReader(this);
		cr.readQuorumConfig(this.rSystems);
	
		init_router_quorum("pages.quorum.nvs.com.");
		
	}
	
	
	
	public void init_router_agent(String pack) {
		
		router.put("/", pack + "RootPageHandler");
		router.put("/send", pack + "SendPageHandler");
		router.put("/status", pack + "StatusPageHandler");
		router.put("/redirect", pack + "RedirectHandler");
		router.put("/img", pack + "ImgHandler");
		router.put("/src", pack + "SrcHandler");
		router.put("/help", pack + "HelpHandler");	

		router.put("/ctrl", pack + "ControlHandler");								
		router.put("/status", pack + "StatusHandler");
		router.put("/configcheck", pack + "ConfigurationCheckHandler");
		router.put("/pkgctrl", pack + "PackageControlHandler");			
		router.put("/scenario", pack + "ScenarioHandler");
		router.put("/about", pack + "AboutProgramHandler");
		router.put("/send", pack + "SendCommandHandler");
		router.put("/journal", pack + "JournalCollectorHandler");
		router.put("/pause", pack + "PauseHandler");		
		
	}

	public void init_router_quorum(String pack) {

		router.put("/", pack + "RootPageHandler");		
		router.put("/img", "neserver.nvs.com.ImgHandler");
		router.put("/src", "neserver.nvs.com.SrcHandler");
		router.put("/help", "neserver.nvs.com.HelpHandler");	
		router.put("/about", "neserver.nvs.com.AboutProgramHandler");		
		
		
	}
	
	
	public static String getVersionInfo() {
		String out = "";

		try {

			Enumeration<URL> resources = Thread.currentThread().getContextClassLoader()
					.getResources("META-INF/MANIFEST.MF");
			while (resources.hasMoreElements()) {
				URL manifestUrl = resources.nextElement();
				Manifest manifest = new Manifest(manifestUrl.openStream());
				Attributes mainAttributes = manifest.getMainAttributes();
				String softVendor = mainAttributes.getValue("Soft-Vendor");

				if (softVendor != null && !softVendor.isEmpty()) {

					String softTitle = mainAttributes.getValue("Soft-Title");
					String softVersion = mainAttributes.getValue("Soft-Version");

					// out += softVendor + " " + softTitle + " ver. " + softVersion;
					out += softVersion;

				}
			}

		} catch (IOException E) {
			out = "ERROR getting version";
		}

		return out;
	}

	public String getSystemProperties() {
		String out = "";
		out += "\njava.version=" + System.getProperty("java.version");
		out += "\njava.runtime.version=" + System.getProperty("java.runtime.version");
		out += "\nos.name=" + System.getProperty("os.name");
		out += "\nos.version=" + System.getProperty("os.version");
		out += "\nos.arch=" + System.getProperty("os.arch");

		return out;
	}
	public void saveToLog(String outText, String fileName) {

		String fullFileName = this.mainPath + File.separator + "log" + File.separator + fileName + ".log";

		try {
			File fileOut = new File(fullFileName);

			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			Date date = new Date();

			FileWriter fr = new FileWriter(fileOut, true);
			BufferedWriter br = new BufferedWriter(fr);
			br.write(dateFormat.format(date) + ": " + outText + "\r\n");

			br.close();
			fr.close();

		} catch (Exception e) {
			this.logger.info(e.getMessage());
		}

	}

	public String getOwnHostname() {
		String out = "";

		InetAddress ip;
		String hostname;

		try {
			ip = InetAddress.getLocalHost();
			hostname = ip.getHostName();
			out = hostname;
		} catch (UnknownHostException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			this.logger.severe(errors.toString());
		}
		return out;
	}

	public String getOwnHostParameter(String paramName) {
		String out = "";
		String myHostName = getOwnHostname();

		for (CluNode n : this.nodes) {

			if (n.id.equals(myHostName)) {

				return n.params.get(paramName);
			}

		}

		return out;
	}



	public String getCurrentTime() {

		return new SimpleDateFormat("HH:mm:ss  dd.MM.yyyy").format(new Date());

	}

	public String getShortCurrentTime() {

		return new SimpleDateFormat("HH:mm:ss").format(new Date());

	}

	public boolean checkPause() {
		boolean out = false;
		String pauseFileName = this.mainPath + File.separator + "tmp" + File.separator + "pause";

		File f = new File(pauseFileName);
		out = f.exists() ? true : false;
		return out;
	}
	public boolean checkScenarioRun() {
		boolean result = false;
		
		if (!this.scenarioRunningName.isEmpty()) {
			result = true;
		}
		return result;
	}
	public boolean setScenarioRun(String scenarioName) {

		boolean result = false;
		if (this.scenarioRunningName.isEmpty()) {			
			this.scenarioRunningName = scenarioName;
			result = true;
		}
		return result;
	}
	public void resetScenarioRun(String scenarioName) {

		if(this.scenarioRunningName.equals(scenarioName)) {		
			this.scenarioRunningName = "";
			this.logger.info("Scenario running flag has been reset");
		} else {
			this.logger.info("Scenario " + scenarioName + " cannot be reset because " + this.scenarioRunningName + " is running yet");
		}

	}
	public CluNode getOwnNode() {

		CluNode out = null;
		String localNode = getOwnHostname();

		for (CluNode n : this.nodes) {

			if (localNode.contains(n.id)) {
				return n;

			}

		}

		return out;
	}
	public void translate_init() {

		addToTrans("Configuration check", "Konfigurationsprüfung", "Проверка конфигурации");
		addToTrans("Help", "Hilfe", "Справка");
		addToTrans("Your commands can interfere production work. Be carefully",
				"Ihre Befehle können die Produktionsarbeit beeinträchtigen. Sei vorsichtig",
				"Ваши команды могут повлиять на продуктивную работу. Будьте внимательны");
		
		addToTrans("Control panel for","Bedienfeld für","Панель управления для");
		
		addToTrans("Set global pause","die globale Pause einstellen","Установка глобальной паузы");		
		addToTrans("Scenario","Szenario","Сценарии");		
		addToTrans("scenario","szenario","сценарии");
		addToTrans("Configuration check","Konfigurationsprüfung","Проверка конфигурации");				
		addToTrans("Pause","Pause","Пауза");				
		addToTrans("About","Über das Programm","О программе");		
		addToTrans("run scenario","Szenario ausführen","выполнить сценарий");

		
		
		/*		addToTrans("","","");	*/		


	
	}
	protected void addToTrans(String enWord, String deWord, String ruWord) {

		msgText.put(enWord + "_DE", deWord);
		msgText.put(enWord + "_RU", ruWord);
	}

	public String tr(String word) {
		String out = "";

		if (this.lang.equals("EN")) {
			return word;
		}
		;

		String key = word + "_" + this.lang;

		if (msgText.containsKey(key)) {

			out = msgText.get(key);

		} else {

			out = word;

		}

		return out;
	}
	public CluNode getNodeByIp(String ip) {

		CluNode out = null;

		for (CluNode n : this.nodes) {
			if (n.params.get("ip").equals(ip)) {
				return n;
			}

		}

		return out;

	}
	public CluNode getMasterNode() throws Exception {

		CluNode out = null;
		String message = "master not found";
		String image = "E.png";
		String left = "0px", top = "0px";
		String hostname = "";
		int countMasters = 0;


		for (CluNode n : this.nodes) {

			if (n.params.get("canBeMaster").equals("yes")) {

				String url = "https://" + n.params.get("ip") + ":";
				url += this.commonParams.get("webServicePort") + "/";
				url += "status?act=show_are_you_master";

				this.logger.info(" ***** " + url + " **** ");
				
				
				HttpsRequestor r = new HttpsRequestor(this);

				String resp = r.doGetRequest(url);
				
				this.logger.info(" ***** " + resp + " **** ");

				if (resp.contains("master")) {
					return n;
				}

			}

		}

		return out;
	}
	public String determineRole() {

		String out = "slave";
		String masterFlagFileName = this.mainPath + File.separator + "tmp" + File.separator + "master";

		File f = new File(masterFlagFileName);

		if (f.exists()) {
			out = "master";
		}
		return out;
	}

	public boolean checkMasterFlag() {
		boolean result = false;
		String pauseFileName = this.mainPath + File.separator + "tmp" + File.separator + "master";

		File f = new File(pauseFileName);
		try {

			result  = f.exists() ? true : false;

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			this.logger.severe(errors.toString());
		}		
		
		
		return result;
	}

	public CluNode getNodeById(String id) {

		CluNode out = null;

		for (CluNode n : this.nodes) {
			if (n.id.equals(id)) {
				return n;
			}

		}

		return out;

	}
	public boolean checkAbort() {
		boolean out = false;
		String pauseFileName = this.mainPath + File.separator + "tmp" + File.separator + "abort";

		File f = new File(pauseFileName);
		out = f.exists() ? true : false;
		return out;
	}
}
