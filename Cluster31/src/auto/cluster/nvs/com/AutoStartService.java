package auto.cluster.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;


import cluster.nvs.com.globalData;
import obj.cluster.nvs.com.CluNode;
import scenario.cluster.nvs.com.AutoScenarioThread;

public class AutoStartService implements Runnable {
	globalData gData = null;

	public AutoStartService(globalData gData) {
		this.gData = gData;
	}

	@Override
	public void run() {

		
		if(!gData.checkMasterFlag()) {
			
			gData.logger.info("I am not master now: nothing to do.");

			return;
		}

		
		
		if(gData.checkPause()) {
			
			gData.logger.info("Pause flag is found: nothing to do.");

			return;
		}
		
		
		if (gData.checkScenarioRun()) {
			gData.logger.info("scenario " + gData.scenarioRunningName + " running flag is found: nothing to do.");
			return;
		} else {
			
			gData.setScenarioRun("Auto");
		}
			


		try {


			runAutoScenario();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());

		} finally {

			
			gData.resetScenarioRun("Auto");
	
		}

	}

	private void runAutoScenario() {

		CluNode n = gData.getOwnNode();

		String autoScenarioName = n.params.get("autoStartScenario");

		gData.logger.info("****** start ************ " + autoScenarioName + " ***********************");
		
		gData.autoScenario = new AutoScenarioThread(gData, gData.scenarioList.get(autoScenarioName), autoScenarioName );
		
		gData.autoScenario.start();
		
		
		//		gData.service.execute(new ScenarioThread(gData, gData.scenarioList.get(autoScenarioName), autoScenarioName ));

	}

}
