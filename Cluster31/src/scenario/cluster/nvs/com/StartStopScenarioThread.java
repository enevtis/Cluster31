package scenario.cluster.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import obj.cluster.nvs.com.CluNode;
import obj.cluster.nvs.com.CluPackage;
import obj.cluster.nvs.com.CluScenarioStep;
import cluster.nvs.com.HttpsRequestor;
import cluster.nvs.com.globalData;

public class StartStopScenarioThread extends Thread {
	public globalData gData = new globalData();
	private CluScenarioStep s;
	private String autoScenarioName = "";
	private CluNode n;

	public StartStopScenarioThread(globalData gData, CluScenarioStep s, CluNode n, String autoScenarioName) {
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

		String ip = n.params.get("ip");
		String action = s.params.get("action");
		String pack = s.params.get("pack");

		String response = "";
		String suitableResourceStatus = getSuitableResourceStatus(action);
		String neededResourceStatus = getNeededResourceStatus(action);
		int autoServiceAskStatusIntervalSec = Integer
				.valueOf(gData.commonParams.get("autoServiceAskStatusIntervalSec"));

		List<String> resources = getResourcesFormPackage(n, pack, action);
		
		String message = "";

		for (String resName : resources) {

			response = getResStatus(ip, resName);

			String phrase = suitableResourceStatus.contains(response) ? "Action begins" : " Nothing to do";

			message = n.id + ":" + resName + ":" + action + " said :" + response + ". We are waiting for: "
					+ neededResourceStatus + " " + phrase;
			gData.logger.info(message);
			gData.saveToLog(message, autoScenarioName);

			if (suitableResourceStatus.contains(response)) {

				
				message = n.id + ":" + resName + ":" + action + " : status:" + response
						+ ". We have to send command " + action;
				gData.logger.info(message);
				gData.saveToLog(message, autoScenarioName);				
				

				sendCommand(ip, resName, action);

				try {

					response = getResStatus(ip, resName);

					while (!response.contains(neededResourceStatus)) {

						Thread.sleep(autoServiceAskStatusIntervalSec * 1000);

						response = getResStatus(ip, resName);

						message = "waiting " + n.id + ":" + resName + ":" + " returns " + response;
						gData.logger.info(message);
						gData.saveToLog(message, autoScenarioName);		
	
						
						if (gData.checkAbort()) {
							gData.logger.info("found abort flag. Giving up...");
						}

					}

					message = n.id + ":" + resName + ":" + action + " is OK. Go ahead...";
					gData.logger.info(message);
					gData.saveToLog(message, autoScenarioName);						
					

				} catch (InterruptedException e) {

					StringWriter errors = new StringWriter();
					e.printStackTrace(new PrintWriter(errors));
					gData.logger.severe(errors.toString());

				}

			}

		}

	}

	private String getSuitableResourceStatus(String action) {
		String out = "";

		switch (action) {
		case "start":
			out = "D";
			break;
		case "stop":
			out = "U";
			break;
		default:
			out = "X";
			break;
		}

		return out;
	}

	private String getNeededResourceStatus(String action) {
		String out = "";

		switch (action) {
		case "start":
			out = "U";
			break;
		case "stop":
			out = "D";
			break;
		default:
			out = "X";
			break;
		}

		return out;
	}

	private List<String> getResourcesFormPackage(CluNode n, String pkgName, String action) {
		List<String> out = new ArrayList<>();

		for (CluPackage p : n.packages) {
			if (p.id.equals(pkgName)) {

				if (action.toLowerCase().equals("start")) {

					for (int i = p.resources.size() - 1; i > -1; i--) {

						out.add(p.resources.get(i).id);
					}

				} else {

					for (int i = 0; i < p.resources.size(); i++) {
						out.add(p.resources.get(i).id);
					}

				}

			}

		}
		return out;
	}

	private String getResStatus(String ip, String resName) {
		String out = "";

		String url;
		url = "https://" + ip + ":";
		url += gData.commonParams.get("webServicePort") + "/";
		url += "status?act=show_one_res_status";
		url += "&res=" + resName;

		HttpsRequestor r = new HttpsRequestor(gData);

		String resp = r.doGetRequest(url);

		out = resp;

		return out;
	}

	private String sendCommand(String ip, String resName, String cmd) {
		String out = "";

		String url = "https://" + ip + ":";
		url += gData.commonParams.get("webServicePort") + "/";
		url += "send";
		url += "?act=" + cmd;
		url += "&res=" + resName;

		HttpsRequestor r = new HttpsRequestor(gData);

		out = r.doGetRequest(url);
		gData.logger.info("Send command " + url);

		return out;
	}

}
