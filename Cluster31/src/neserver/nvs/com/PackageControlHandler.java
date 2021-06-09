package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import obj.cluster.nvs.com.CluResource;
import cluster.nvs.com.globalData;
import obj.cluster.nvs.com.CluNode;
import obj.cluster.nvs.com.CluPackage;



public class PackageControlHandler extends HandlerTemplate{
	
	String test = "";
	public PackageControlHandler(globalData gData) {
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
		socket.close();
		
		
		} catch (IOException e) {
	
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}
		
		
	}

	
	public String getPage() {

		
		String out = "";

		String ip = params.get("ip");
		String pkg = params.get("pkg");

		out += getBeginPage();

		out += getControlForWholeNode();

		out += getAvailableScenario();

		out += refreshFunctions();

		out += getEndPage();
		
		return out;
	}


	private String getControlForWholeNode() {
		String out = "";
		String ip = params.get("ip");
		String pkg = params.get("pkg");
		String curStyle = "";
		String onClick = "";

		out += "<p class='warning'><img src='img/warning.png'>&nbsp;"
				+ gData.tr("Your commands can interfere production work. Be carefully") + "</p>";
		out += "<p class='caption1'> " + gData.tr("Control panel for") + ":";

		for (CluNode n : gData.nodes) {

			if (n.params.get("ip").equals(ip)) {

				curNode = n; // сохраняем указатель на ноду для других функций.

				out += "<br><p class='caption2'>" + params.get("ip") + " " + n.params.get("hostname");
				out += " " + n.params.get("descr") + " " + getNodeRole() + "</p>";
			}

		}

		out += "</p>";

		for (CluNode n : gData.nodes) {

			if (n.params.get("ip").equals(ip)) {

				out += "<ul>";
				for (CluPackage p : n.packages) {

					out += "<li class='package'>package:" + p.id + "<br><br>";

					out += "<ul>";

					for (CluResource r : p.resources) {

						out += "<li class='resource'>";
						out += "<img id='" + p.id + "_" + r.id + "' src='/img/Q.png'>&nbsp;resource:" + r.id + "";
						out += "<br>";

						out += "<ol>";

						onClick = "window.location='/redirect?page=send&ip=" + ip + "&act=start&pkg=" + p.id;
						onClick += "&res=" + r.id + "'";
						out += "<li><div class='command' onclick = \"" + onClick + "\" >";
						out += "<b>START</b></div>";

						onClick = "window.location='/redirect?page=send&ip=" + ip + "&act=stop&pkg=" + p.id;
						onClick += "&res=" + r.id + "'";
						out += "<li><div class='command' onclick = \"" + onClick + "\" >";
						out += "<b>STOP</b></div>";

						out += "</ol><br></br>";

					}

					out += "</ul>";

				}
				out += "</ul>";
			}

		}

		return out;
	}

	public String refreshFunctions() {

		String out = "";

		out += "<script>";

		int interval = 3000;

		if (gData.commonParams.containsKey("refreshGuiIntervalSec")) {
			interval = Integer.valueOf(gData.commonParams.get("refreshGuiIntervalSec")) * 1000;
		}

		out += " setInterval(async () => { \n";
		out += " var statuses; \n";
		out += " var ip = '" + curNode.params.get("ip") + "'; \n";
		out += " var hostName = '" + curNode.id + "'; \n";

		String link = "redirect?page=status&ip=" + curNode.params.get("ip");
		link += "&act=show_resources_status";

		out += "getStatus('" + link + "',function(statuses){ \n";

		if(gData.debugInfo) out += "console.log(statuses); \n";
		
		out += "var resp = JSON.parse(statuses);\n";

		out += "for(i=0; i< Object.keys(resp.packages).length; i++) { \n";
		out += "var curId = resp.packages[i].pkg ; \n";

		out += "	for(j=0; j< Object.keys(resp.packages[i].resources).length; j++) { \n";

		out += "	var curId2 = resp.packages[i].resources[j].res ; \n";
		out += "	var curStatus = resp.packages[i].resources[j].status ; \n";

		out += "	var curImgId = curId + '_' + curId2; \n";
		out += "	var curImage = 'img/' + curStatus + '.png'; \n";

		out += "	var obj=document.getElementById(curImgId); \n";
		out += "	if(obj){ \n";
		out += "		obj.src = curImage ; \n";
		out += "		obj.title = getTitleForStatus(curStatus) + ' ' + resp.message ; \n";
		out += "	} \n";

		out += "	} \n";

		out += "} \n";

		out += "}); \n";

		out += " }, " + interval + "); \n";

		out += "</script>";
		return out;

	}

	private String getControlForPackage() {
		String out = "";
		out += "For Node " + params.get("ip");
		out += "<br>for package " + params.get("pkg");
		return out;
	}

	private String getNodeRole() {
		String out = "";


		CluNode n = gData.getNodeByIp(params.get("ip"));

		CluNode masterNode = null;
		
		try {
			
			masterNode = gData.getMasterNode();
		
			if (masterNode != null) {

				if (n == masterNode) {

					out += "<p style='color:red;'><h3>master node</h3><p>";

				} else {

					out += "<p style='color:blue;'><h4>slave node</h4><p>";

				}	
		
			}	
		
		
		}catch (Exception e) {
			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			gData.logger.severe(errors.toString());
			
		}
		
			
			return out;
	}

	private String getAvailableScenario() {
		String out = "";
		CluNode n = gData.getNodeByIp(params.get("ip"));

		out += gData.tr("run scenario");

		out += "<ol>";

		for (Map.Entry mapElement : n.params.entrySet()) {
			String key = (String) mapElement.getKey();
			String value = (String) mapElement.getValue();

			if (key.toLowerCase().contains("scenario")) {

				String onClick = "window.location='/redirect?page=send&ip=" + params.get("ip") + "&act=start_scenario";
				onClick += "&scenario=" + value + "'";

				out += "<li class='command' onclick = \"" + onClick + "\" >" + key + ":" + value;

			}

		}
		out += "</ol>";

		return out;
	}

}
