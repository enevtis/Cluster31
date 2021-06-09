package scenario.cluster.nvs.com;

import utils.cluster.nvs.com.Utils;
import cluster.nvs.com.globalData;

public class RunCommandThread implements Runnable {
	public globalData gData = new globalData();
	private String user;
	private String fullScriptName;

	public RunCommandThread(globalData gData, String user, String fullScriptName) {
		this.gData = gData;
		this.user = user;
		this.fullScriptName = fullScriptName;
	}

	@Override
	public void run() {
		String out = "";
		gData.journalText = "executing " + fullScriptName;
		out += Utils.runSuCommand(user, fullScriptName,gData.logger);
		gData.journalText = out;
		
	}

}
