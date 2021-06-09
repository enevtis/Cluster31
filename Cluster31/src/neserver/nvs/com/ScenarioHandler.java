package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;


import cluster.nvs.com.globalData;
import obj.cluster.nvs.com.CluScenarioStep;



public class ScenarioHandler extends HandlerTemplate{
	
	String test = "";
	public ScenarioHandler(globalData gData) {
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
		String curStyle= "";
		out += getBeginPage();
		out += "<p class='caption1'>" + gData.tr("scenario") + "</p>";

		
		for (Map.Entry<String, List<CluScenarioStep>> set : gData.scenarioList.entrySet()) {
			   
			List<CluScenarioStep> scenario = set.getValue();
			
			out += gData.tr("scenario") + " : <b>" + set.getKey() + "</b>";
			
			out += "<table class='table1'>";
			
			for(CluScenarioStep s: scenario) {
				
				out += "<tr>";		
				out += "<td>" + s.id + "</td>";
				
				for (Map.Entry<String, String> set2 : s.params.entrySet()) {
					
						out += "<td>";		
						out += set2.getKey() + " = " + set2.getValue();
						out += "</td>";
						
				
				}
				out += "</tr>";			
			
			}

			out += "</table>";
		}
		
		
		
		out += getEndPage();
		
		return out;
		
	}

}
