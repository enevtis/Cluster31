package neserver.nvs.com;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import javax.net.ssl.SSLSocket;

import cluster.nvs.com.HttpsRequestor;
import cluster.nvs.com.globalData;

import obj.cluster.nvs.com.CluNode;
import scenario.cluster.nvs.com.RunCommandThread;
import scenario.cluster.nvs.com.AdminScenarioThread;



public class SendCommandHandler extends HandlerTemplate{
	
	String test = "";
	public SendCommandHandler(globalData gData) {
		super(gData);

	}

	public void getResponse(SSLSocket socket, String paramsString) {
		
		test = paramsString;
		
		parseParams(paramsString);
		String resp = getPage();

		try {
			
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			
			
		out.write(header200());					
		out.write("Content-Length: " + resp.getBytes("UTF-8").length + "\r\n");
		out.write("\r\n");
		out.write(resp);
		
		out.close();
		
		
		} catch (IOException e) {
	
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}
		
		
	}

	
	public String getPage() {
		String out = "";
		
		out += getBeginPage();

		String act=params.get("act");		

		switch(act) {
		
		case "set_pause":
			out += setPause();
			break;
		case "reset_pause":
			out += resetPause();
			break;		
		case "start_scenario":
			out += startScenario();
			break;			
		case "run_script":
			out += runScript();
			break;			
		case "set_global_master":
			out += setGlobalMaster();
			break;

		case "set_local_master":
			out += setLocalMaster();
			break;			
			
		default:
			
			out += runStartStop();
		break;
		}
		

		out += getEndPage();
		return out;
	}

	private String setGlobalMaster() {
		String out = "";	
		
		for (CluNode n: gData.nodes) {
			if (n.params.get("canBeMaster").equals("yes")) {
				
				String url;
				url = "https://" + n.params.get("ip") + ":";
				url += gData.commonParams.get("webServicePort") + "/";
				url += "send?act=set_local_master";
				url += "&master=" + params.get("master");

				HttpsRequestor r = new HttpsRequestor(gData);
				String resp = r.doGetRequest(url);
				
			}
			
		}
		
		return out;
	}
	private String setLocalMaster() {
		String out = "";	
		CluNode n = gData.getOwnNode();
			
			try {
				
				File masterFlag = new File(gData.mainPath + File.separator + "tmp" + File.separator + "master");				
				
				if(n.id.contains(params.get("master"))) {
					

					
					masterFlag.createNewFile();
					gData.journalText = "I am master now";
					gData.logger.info(gData.journalText);

				} else {
					
					Files.deleteIfExists(masterFlag.toPath());
					gData.journalText = "I am slave now";
					gData.logger.info(gData.journalText);
				}
				
				
				//			FileOutputStream oFile = new FileOutputStream(yourFile, false); 

			} catch (IOException e) {
				StringWriter errors = new StringWriter();
				e.printStackTrace(new PrintWriter(errors));

				gData.logger.severe(errors.toString());
			} 
		
		return out;
	}	
	
	private String runScript() {
	String out = "";	
	String osUser = "root";
	String fullScriptName = gData.scriptPath + File.separator + params.get("script");
	gData.journalText = "executing " + fullScriptName;
	
	try {
		
//		Thread scn= new Thread(new ScenarioThread(gData, gData.scenarioList.get(autoScenarioName), autoScenarioName ));
//		scn.start();
		
		Thread thr1= new Thread(new RunCommandThread(gData, osUser, fullScriptName));	
		thr1.start();

	} catch(Exception e) {
		
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		gData.logger.severe(errors.toString());			
		
	}
	
	return out;
}
	
private String runStartStop() {
	String out = "";	

	String act=params.get("act");
	String pkg = params.get("pkg");
	String resource = params.get("res");
	String osUser = "root";
	String fullScriptName = "";	
	
	fullScriptName = gData.scriptPath + File.separator + resource + "_" + act + ".sh";

	try {		
//		gData.service.execute(new RunCommandThread(gData, osUser, fullScriptName));		

		Thread thr1= new Thread(new RunCommandThread(gData, osUser, fullScriptName));	
		thr1.start();
	
	} catch(Exception e) {
		
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		gData.logger.severe(errors.toString());			
		
	}
	
	
	
	out += "<p class='response'>";		
	out += "command " + fullScriptName + " with user " + osUser + " is executed";
	out += "</p>";			
	
	return out;	
}
private String setPause() {
	String out = "";	
	String osUser = "root";
	String command = "touch " + gData.mainPath + File.separator + "tmp";
	command += File.separator + "pause";

	try {		
//		gData.service.execute(new RunCommandThread(gData, osUser, command));	

		Thread thr1= new Thread(new RunCommandThread(gData, osUser, command));	
		thr1.start();
		
	} catch(Exception e) {
		
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		gData.logger.severe(errors.toString());			
		
	}
	
	gData.journalText = "pause mode is on";
	out = gData.journalText;
	return out;	
}
private String resetPause() {
	String out = "";	
	String osUser = "root";
	String command = "find " + gData.mainPath + File.separator + "tmp";
	command += File.separator + "pause -delete";

	try {	

		Thread thr1= new Thread(new RunCommandThread(gData, osUser, command));	
		thr1.start();
//		gData.service.execute(new RunCommandThread(gData, osUser, command));	

	
	} catch(Exception e) {
		
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));
		gData.logger.severe(errors.toString());			
		
	}
	
	
	gData.journalText = "pause mode is off";	
	out = gData.journalText;
	return out;		
}
private String startScenario() {
	String out = "";
	CluNode n = gData.getOwnNode();

	String autoScenarioName = params.get("scenario");
	gData.journalText = "Start scenario " + autoScenarioName;
	gData.logger.info("****** start ************ " + autoScenarioName + " ***********************");

	
	
	try {

		if (gData.autoScenario !=null) {
			gData.autoScenario.interrupt();
		}
		
		Thread scn= new Thread(new AdminScenarioThread(gData, gData.scenarioList.get(autoScenarioName), autoScenarioName ));
		scn.start();
	
	}catch(Exception e) {
		
		StringWriter errors = new StringWriter();
		e.printStackTrace(new PrintWriter(errors));

		gData.logger.severe(errors.toString());
		
	}

	
	out += gData.journalText;
	return out;
}

}
