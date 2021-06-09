package neserver.nvs.com;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Logger;

import javax.net.ssl.SSLSocket;

import obj.cluster.nvs.com.CluNode;
import obj.cluster.nvs.com.CluPackage;
import obj.cluster.nvs.com.CluResource;
import cluster.nvs.com.HttpsRequestor;
import cluster.nvs.com.globalData;
import utils.cluster.nvs.com.*;

public class StatusHandler extends HandlerTemplate{
	
	
	public StatusHandler(globalData gData) {
		super(gData);
		// TODO Auto-generated constructor stub
	}
	public void getResponse(SSLSocket socket, String paramsString) {
		
		parseParams(paramsString);
		String resp = getPage();
		

		try {
			
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			
			
		out.write(header200());					
		out.write("Content-Length: " + resp.getBytes("UTF-8").length + "\r\n");
		out.write("\r\n");
		out.write(resp);
		
		out.close();
		
		socket.close();
		
		} catch (IOException e) {
	
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}
		
		
	}

	public String getPage() {
		String out = "";

		if (!params.containsKey("act")) {

			return "error";

		}

		String act = params.get("act");
		switch (act) {
		case "show_script":
			out = showScript();
			break;

		case "show_own_name":
			out = gData.getOwnHostname();
			break;

		case "show_pkg_status":
			out = showPkgStatus();
			break;

		case "show_resources_status":
			out = showResourcesStatus();
			break;

		case "show_role":
			out = gData.determineRole();
			break;

		case "show_master_icon":						// requested from javascript
			out = showMasterIcon();
			break;

		case "show_are_you_master":						//redirected from java
			out = showAmIMaster();
			break;

		case "show_one_res_status":						
			out = showOneResStatus();
			break;

		case "show_script_status":
			out = showScriptStatus();
			break;			

		case "show_quorum_info":
			out = showQuorumInfo();
			break;
			
		default:
			out = "Error act=" + act;
			break;

		}

		return out;

	}

	private String showMasterIcon() {
		String out = "";
		String message = "master not found";
		String image = "E.png";
		String left = "0", top = "0";
		String left2 = "0", top2 = "0";
		String hostname = "";
		String replicationStatus = "Replication status unknown";
		int countMasters = 0;

 		
		for (CluNode n : gData.nodes) {
			
			if (n.params.get("canBeMaster").equals("yes")) {
				
				String url = "https://" + n.params.get("ip") + ":";
				url += gData.commonParams.get("webServicePort") + "/";
				url += "status?act=show_are_you_master";

				

				HttpsRequestor r = new HttpsRequestor(gData);

				String resp = r.doGetRequest(url);
				

				if (resp.contains("master")) {
					
					String respParts[] = resp.split(":");
					if( respParts.length >= 1) hostname = respParts[0];
					if( respParts.length >= 3) left = respParts[2];
					if( respParts.length >= 4) top = respParts[3];					
					if( respParts.length >= 5) image = respParts[4];
					
					
					

					if (gData.thereIsReplication) {
						if( respParts.length >= 6) left2 = respParts[5];
						if( respParts.length >= 7) top2 = respParts[6];
						if( respParts.length >= 8) replicationStatus = respParts[7];


					
					}
					
					
					countMasters ++;
					
				}


			
			}
		
		}

		
		
		if (countMasters == 1) {
			message = "found master " + hostname;
		}else {
			message = "found " + countMasters + " masters " + hostname;
			image = "E.png";
		}

		
		
		out += "{";
		out += "\"left\":\"" + left + "\",";
		out += "\"top\":\"" + top + "\",";
		out += "\"image\":\"" + image + "\",";
		out += "\"message\":\"" + message + "\"";

		if (gData.thereIsReplication) {

			out += ",";
			out += "\"left2\":\"" + left2 + "\",";
			out += "\"top2\":\"" + top2 + "\",";
			out += "\"replication\":\"" + replicationStatus + "\"";			
		}
			
			out += "}";

			
			
			
			return out;
	}


	
	private String showAmIMaster() {
		String out = "";

		
		if(gData.checkMasterFlag()) {

			CluNode n = gData.getOwnNode();
			
			out += n.id + ":master:" + n.params.get("left") + ":" + n.params.get("top") + "";
			
				if (gData.checkPause())	out += ":master_passive.png";
				else 	out += ":master_active.png";

				
				if (gData.nodes.size() > 1) {
				
					out += ":" + (Integer.valueOf(n.params.get("left"))) ;
					out += ":" + (Integer.valueOf(n.params.get("top")) + Integer.valueOf(gData.commonParams.get("replicationLabelOffset"))) ;

					
					String statusScriptName = gData.mainPath + File.separator + "conf" + File.separator;
					statusScriptName += "scripts" + File.separator + "sr_status.sh";
					
					
					out += ":" + getReplicationStatus("root",statusScriptName, gData.logger );
	
				}

		
		}
		
		return out;
	}

	private String showResourcesStatus() {
		String out = "";
		String osUser = "root";
		String message = "";

		CluNode n = gData.getOwnNode();

		out += "{";
		out += "\"message\":\"MESSAGEREPLACE\",";
		out += "\"packages\" : ";
		out += "[";

		for (CluPackage p : n.packages) {

			out += "{ \"pkg\" : \"" + p.id + "\", \"resources\": [";

			for (CluResource r : p.resources) {

				out += "{";

				out += "\"res\":\"" + r.id + "\",\"status\":";

				out += "\"";

				String statusScriptName = gData.mainPath + File.separator + "conf" + File.separator;
				statusScriptName += "scripts" + File.separator + r.id + "_status.sh";

				File f = new File(statusScriptName);


				if (f.exists()) {
					String response = Utils.runSuCommand(osUser, statusScriptName,gData.logger);
					out += response;

				} else {
					out += "E";
					message += "(" + r.id + "_status.sh" + " not found)";
				}

				out += "\"";

				out += "},";

			}

			out = out.substring(0, out.length() - 1);

			out += "] },";

		}

		out = out.substring(0, out.length() - 1);

		out += "]";
		out += "}";

		out = out.replace("MESSAGEREPLACE", message);

		return out;

	}
	private String showScriptStatus() {
		String out = "";
		String osUser = "root";
		String statusScriptName = gData.scriptPath + File.separator + params.get("script"); 
		
		File f = new File(statusScriptName);
		
		if (f.exists()) {

			String response = Utils.runSuCommand(osUser, statusScriptName,gData.logger);
			out = response.replaceAll("[^A-Za-z0-9]","");;

		} else {
			out = "E";
			gData.journalText = statusScriptName + " not found";
			gData.logger.severe( gData.journalText );
		}

		return out;

	}
	private String showPkgStatus() {
		String out = "";
		String osUser = "root";
		String message = "";

		CluNode n = gData.getOwnNode();

	
		
		out += "{";

		out += "\"statuses\" : ";
		out += "[";

		for (CluPackage p : n.packages) {

			out += "{";

			out += "\"pkg\":\"" + p.id + "\",\"status\":";

			String allResourcesStatusString = "";

			for (CluResource r : p.resources) {

				String statusScriptName = gData.mainPath + File.separator + "conf" + File.separator;
				statusScriptName += "scripts" + File.separator + r.id + "_status.sh";
				File f = new File(statusScriptName);

				if (f.exists()) {

					allResourcesStatusString += Utils.runSuCommand(osUser, statusScriptName,gData.logger);

				} else {
					out += "E";
					message += "(" + r.id + "_status.sh" + " not found)";
				}

			}

			out += "\"" + getPkgStatusSummaryString(allResourcesStatusString) + "\"";

			message = allResourcesStatusString;

			out += " },";

		}

		out = out.substring(0, out.length() - 1);

		out += "],";
		out += "\"message\":\"" + message + "\"";
		out += "}";

		return out;

	}

	private String getPkgStatusSummaryString(String AllResourcesStatusString) {
		String out = "";
		AllResourcesStatusString = AllResourcesStatusString.replaceAll("[^UDEW]", "");

		long countU = AllResourcesStatusString.chars().filter(ch -> ch == 'U').count();
		long countD = AllResourcesStatusString.chars().filter(ch -> ch == 'D').count();
		long countW = AllResourcesStatusString.chars().filter(ch -> ch == 'W').count();
		long countE = AllResourcesStatusString.chars().filter(ch -> ch == 'E').count();

		if (countE > 0)
			return "E";
		if (countW > 0)
			return "W";
		if (countD == AllResourcesStatusString.length())
			return "D";
		if (countU == AllResourcesStatusString.length())
			return "U";

		if ((countU + countD) == AllResourcesStatusString.length())
			return "W";

		return "Q";

	}



	private String showScript() {
		String out = "";
		String fullPathFileName = gData.mainPath + File.separator + "conf" + File.separator + "scripts" + File.separator
				+ params.get("script");
		out += getBeginPage();

		out += "<p class='caption1'><b>" + gData.getOwnHostname() + "</b>:" + gData.getOwnHostParameter("descr")
				+ "</p>";

		out += "<br>Content of " + fullPathFileName + "<br><hr>";

		String curStyle = "background-color:white;color: black;";

		out += "<code style='" + curStyle + "'>" + Utils.readTextFileToHtml(fullPathFileName,gData.logger) + "</code>";

		out += "<br><hr>";
		String command = "ls -l " + fullPathFileName;


		String outLscommand = Utils.runSuCommand(params.get("user"), command,gData.logger);
		out += "<br>" + outLscommand;

		String[] parts = outLscommand.split("\\s+");

		if (parts.length > 1) {
			if (parts[0].toLowerCase().contains("x")) {

				out += "<br><b><font color='green'>script is executable</font></b>";
			} else if (outLscommand.toLowerCase().contains("no such file")) {
				out += "<br><b><font color='red'>script does not exists</font></b>";

			} else {

				out += "<br><b><font color='red'>script is not executable</font></b>";
			}

		}

		out += getEndPage();

		return out;
	}

private String showOneResStatus() {
	String out = "";
	String osUser = "root";
	String res = params.get("res");
	
	String statusScriptName = gData.mainPath + File.separator + "conf" + File.separator +
	 "scripts" + File.separator + res + "_status.sh";
	
	File f = new File(statusScriptName);

	if (f.exists()) {

		out = Utils.runSuCommand(osUser, statusScriptName,gData.logger);

	} else {
		
		out = "E";
		gData.logger.severe(statusScriptName + " not found");
	}
	
	return out;
}
public static String getReplicationStatus(String user, String statusScriptName, Logger logger) {
	String out = "";
	String osUser = "root";
	

	File f = new File(statusScriptName);

	if (f.exists()) {

		out += Utils.runSuCommand(osUser, statusScriptName,logger);
		
		out = out.replaceAll("[^A-Za-z0-9-<> ]","");
	}

	
	return out;
}

private String showQuorumInfo() {
	String out = "";
	
	 for (CluNode n: gData.nodes) {

		 if (n.params.get("canBeMaster").equals("yes")) {
			 
			 out += "<host>";
			 out += "<ip>" + n.params.get("ip") + "</ip>" ;
			 out += "</host>";
		 
		 }
	 
	 }
	
	 out += "<clusterName>" + gData.commonParams.get("clusterName") + "</clusterName>";
	 out += "<time>" + gData.getCurrentTime() + "</time>";
	 out += "<hostname>" + gData.getOwnHostname() + "</hostname>";
	
	return out;
}
}
