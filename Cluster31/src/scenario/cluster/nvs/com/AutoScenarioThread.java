package scenario.cluster.nvs.com;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import obj.cluster.nvs.com.CluNode;
import cluster.nvs.com.HttpsRequestor;
import cluster.nvs.com.globalData;

import obj.cluster.nvs.com.CluScenarioStep;

public class AutoScenarioThread implements Runnable{

	public globalData gData;
	private Thread worker;
	private final AtomicBoolean running = new AtomicBoolean(false);
	List<CluScenarioStep> scenario = null;
	String autoScenarioName;
	
	
	public AutoScenarioThread(globalData gData,List<CluScenarioStep> scenario, String autoScenarioName) {
		this.gData = gData;
		this.scenario = scenario;
		this.autoScenarioName = autoScenarioName;
		running.set(true);
		
	}
	
	
	@Override
	public void run() {
		
		


		try {

			if (gData.checkScenarioRun()) {

				gData.logger.info("scenario " + gData.scenarioRunningName + " running flag is found: Try again.");
				return;
			} else {

				gData.setScenarioRun(autoScenarioName);
			}


			for (CluScenarioStep s : scenario) {

				
				
				if(!gData.checkMasterFlag()) {
					
					gData.logger.info("I am not master now: exit from scenario " + autoScenarioName);
					
				}
				
				
				
				gData.journalText = "scenario " + autoScenarioName + " step " + s.id + " " + s.params.get("descr");

				CluNode n = gData.getNodeById(s.params.get("node"));

				HttpsRequestor r = new HttpsRequestor(gData);

				if (r.checkNode(n.params.get("ip"))) {

					String action = s.params.get("action");
					gData.logger.info("---- step " + s.id + " ---- " + s.params.get("descr"));

					switch (action) {

					case "start":
					case "stop":
						StartStopScenarioThread oneStartStopStep = new StartStopScenarioThread(gData, s, n,
								autoScenarioName);
						oneStartStopStep.start();

						if (s.params.get("wait").equals("yes")) {
							gData.saveToLog(s.id + " pause", "scenario");
							oneStartStopStep.join();
						}

						break;

					case "send":

						SendScenarioThread oneSendStep = new SendScenarioThread(gData, s, n, autoScenarioName);
						oneSendStep.start();

						if (s.params.get("wait").equals("yes")) {
							gData.saveToLog(s.id + " pause", "scenario");
							oneSendStep.join();
						}

						break;

					default:
						ScriptScenarioThread oneScriptStep = new ScriptScenarioThread(gData, s, n, autoScenarioName);
						oneScriptStep.start();

						if (s.params.get("wait").equals("yes")) {
							gData.saveToLog(s.id + " pause", "scenario");
							oneScriptStep.join();
						}

						break;

					}

					// make pause

					if (s.params.containsKey("pause")) {
						int pause = Integer.valueOf(s.params.get("pause"));
						gData.logger.info("Pause " + pause + " sec." + "after step " + s.id);

						try {
							Thread.sleep(pause * 1000);
						} catch (InterruptedException e) {

						}
					}

				} else {

					gData.logger.info(n.id + " is not reachable. Step " + s.id + " command was ignored");

				}

			}

			gData.resetScenarioRun(autoScenarioName);

			gData.journalText = "end scenario " + autoScenarioName;

			gData.logger.info("****** finish ************ " + autoScenarioName + " ***********************");

		} catch (Exception e) {

			gData.journalText = e.getMessage();
			StringWriter errors = new StringWriter();
			gData.logger.severe(errors.toString());

		}
		
		
		

		
		
	}
	
    public void interrupt() {
        running.set(false);
        worker.interrupt();
    }
    
    boolean isRunning() {
        return running.get();
    } 
	public void start() {
        worker = new Thread(this);
        worker.start();
    }
    public void stop() {
        running.set(false);
    }
}
