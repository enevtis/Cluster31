package scenario.cluster.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import obj.cluster.nvs.com.CluNode;
import obj.cluster.nvs.com.CluScenarioStep;
import cluster.nvs.com.HttpsRequestor;
import cluster.nvs.com.globalData;

public class SendScenarioThread extends Thread {
	public globalData gData = new globalData();
	private CluScenarioStep s;
	private String autoScenarioName = "";
	private CluNode n;
	private String message = "";

	public SendScenarioThread(globalData gData, CluScenarioStep s, CluNode n, String autoScenarioName) {
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


		sendRemoteCommand(n.params.get("ip"));
		gData.journalText = "*** " + s.id + " node=" + n.id + " executing " + s.params.get("descr");
		gData.logger.info(gData.journalText);

	}

	private String sendRemoteCommand(String ip) {
		String out = "";

		String url;
		url = "https://" + ip + ":";
		url += gData.commonParams.get("webServicePort") + "/";
		url += "send?";
		url += getParamsForSend();

		message= "send: " + url;
		gData.logger.info(message);
		gData.saveToLog(message, autoScenarioName);
		
		HttpsRequestor r = new HttpsRequestor(gData);
		String resp = r.doGetRequest(url);

		gData.logger.info(resp);
		gData.saveToLog(resp, autoScenarioName);
		

		return out;
	}

	private String getParamsForSend() {
		String out = "";

		for (Map.Entry mapElement : s.params.entrySet()) {
			String key = (String) mapElement.getKey();
			String value = (String) mapElement.getValue();

			if (key.toLowerCase().contains("p.")) {
				String htmlParam = key.replace("p.", "");
				out += htmlParam + "=" + value + "&";
			}

		}

		if (out.length() > 2)
			out = out.substring(0, out.length() - 1);

		return out;
	}

}
