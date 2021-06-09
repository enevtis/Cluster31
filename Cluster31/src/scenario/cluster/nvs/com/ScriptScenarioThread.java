package scenario.cluster.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;

import obj.cluster.nvs.com.CluNode;
import obj.cluster.nvs.com.CluScenarioStep;
import cluster.nvs.com.HttpsRequestor;
import cluster.nvs.com.globalData;

public class ScriptScenarioThread extends Thread {
	public globalData gData = new globalData();
	private CluScenarioStep s;
	private String autoScenarioName = "";
	private CluNode n;
	private String message = "";
	
	public ScriptScenarioThread(globalData gData, CluScenarioStep s, CluNode n, String autoScenarioName) {
		this.gData = gData;
		this.s = s;
		this.autoScenarioName = autoScenarioName;
		this.n = n;
	}

	@Override
	public void run() {	

		try {

			action();

		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}	
	
	}
	

	public void action() {


		String response = "";
		int autoServiceAskStatusIntervalSec = Integer
				.valueOf(gData.commonParams.get("autoServiceAskStatusIntervalSec"));
	

		executeRemoteScript(n.params.get("ip"), s.params.get("run_script"));

		message = "*** " + s.id + " node=" + n.id + " executing " + s.params.get("run_script");
		gData.logger.info(message);
		gData.saveToLog(message, autoScenarioName);
		
		
		
		try {

			while (true) {
				Thread.sleep(autoServiceAskStatusIntervalSec * 1000);
				response = getRemoteStatusScript(n.params.get("ip"),s.params.get("status_script"));
				message = "*** " + n.id + " " + s.params.get("status_script") + " returns " + response + ". Expecting: " + s.params.get("status_ok");
				gData.logger.info(message);
				gData.saveToLog(message, autoScenarioName);
				
				
				if (s.params.get("status_ok").isEmpty()) break;
				if (response.equals(s.params.get("status_ok"))) break;
				

			}

		} catch (InterruptedException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			gData.logger.severe(errors.toString());
		}

	}

	private String executeRemoteScript(String ip, String runScript) {
		String out = "";

		String url;
		url = "https://" + ip + ":";
		url += gData.commonParams.get("webServicePort") + "/";
		url += "send?act=run_script";
		url += "&script=" + runScript;

		HttpsRequestor r = new HttpsRequestor(gData);

		String resp = r.doGetRequest(url);

		out = resp;
		return out;
	}

	private String getRemoteStatusScript(String ip, String statusScript) {
		String out = "";
		String url;
		url = "https://" + ip + ":";
		url += gData.commonParams.get("webServicePort") + "/";
		url += "status?act=show_script_status";
		url += "&script=" + statusScript;

		HttpsRequestor r = new HttpsRequestor(gData);

		String resp = r.doGetRequest(url);

		out = resp;
		return out;
	}
}
