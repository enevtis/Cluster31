package quorum.nvs.com;

import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import cluster.nvs.com.HttpsRequestor;
import cluster.nvs.com.globalData;
import obj.cluster.nvs.com.CluNode;
import obj.cluster.nvs.com.CluRemoteSystem;

public class QuorumControlService implements Runnable {
	
	public globalData gData = null;
	public CluRemoteSystem rs = null;

	public QuorumControlService(globalData gData, CluRemoteSystem rs) {
		this.gData = gData;
		this.rs = rs;
	}

	@Override
	public void run() {

		try {

			checkStatusAllNodes();
			controlRemoteSystem();

		} catch (Exception e) {

			gData.journalText = e.getMessage();
			StringWriter errors = new StringWriter();
			gData.logger.severe(errors.toString());

		}

	}
public void controlRemoteSystem() {
	
	for (CluNode n: rs.nodes) {
		
		
		gData.logger.info("node=" + n.params.get("ip") + " role=" + n.params.get("role"));
		
	}
	
	long masterDownSec = ChronoUnit.SECONDS.between(rs.MasterAvailable, LocalDateTime.now());
	long limitSec = Long.valueOf(rs.params.get("timeoutMasterSec"));
	long periodSec = Long.valueOf(rs.params.get("scanQuorumAskStatusIntervalSec"));
	
	
	if(masterDownSec  > (3 * periodSec)) {
		gData.logger.info("master is down already =" +  masterDownSec + " seconds");	
		}
	

	if(masterDownSec > limitSec) {
		
		if (rs.commandWasSend) {
			gData.logger.info("Command for start already has been send. Nothing to do.");
			return;
		}
		
		
		
		for (CluNode n: rs.nodes) {
			
			if (n.params.get("ip").equals(rs.lastMasterIp)) {
				
				String url = "https://" + n.params.get("nextMasterIp") + ":";
				url += rs.params.get("webServicePort") + "/";
				url += "send?act=start_scenario";
				url += "&scenario=" + n.params.get("nextMasterScenario");
				rs.commandWasSend = true;
				
				HttpsRequestor r = new HttpsRequestor(gData);
				String resp = r.doGetRequest(url);	
				
				
				gData.logger.info("Send=" + url);
				
				
			}
			

			
		}
		
	}
	
	
}


public void checkStatusAllNodes() {

	
	for( CluNode n: rs.nodes) {
		
		String url = "https://" + n.params.get("ip") + ":";
		url += rs.params.get("webServicePort") + "/";
		url += "status?act=show_role";
		HttpsRequestor r = new HttpsRequestor(gData);
		String resp = r.doGetRequest(url);		
	
		if(resp != null && !resp.isEmpty()) {
			
			if (resp.toLowerCase().contains("master")) {
				n.params.put("role", "master");
				rs.lastMasterIp = n.params.get("ip");
				rs.MasterAvailable = LocalDateTime.now();
			}else if (resp.toLowerCase().contains("slave")) {
				n.params.put("role", "slave");
			}else {
				n.params.put("role", "lost");
			}
			
		}
	
	}


}	
}
