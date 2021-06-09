package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.net.ssl.SSLSocket;

import cluster.nvs.com.globalData;
import obj.cluster.nvs.com.CluNode;
import obj.cluster.nvs.com.CluPackage;



public class RootPageHandler extends HandlerTemplate{
	
	
	public RootPageHandler(globalData gData) {
		super(gData);

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

		
		
		} catch (IOException e) {
	
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());
		}
		
		
	}

	
	public String getPage() {
		String out = "";
		out += getBeginPage();
		
		out += "<img id='pic_more' src='/img/menu.png' title='menu'";
		out += " style='position:absolute;left:10px;top:25px;cursor:pointer;' ";
		out += " onclick='showMenu(this.id)';\" ";
		out += ">";
		
		out += "<p class='caption1'>&nbsp;&nbsp;&nbsp;&nbsp;<b>" + gData.commonParams.get("clusterName") + "</b> </p>";	
	
		
		for (CluNode n : gData.nodes) {

			out += "<div ";

			String curStyle = "position:absolute;";

			curStyle += "top:" + n.params.get("top") + "px;";
			curStyle += "left:" + n.params.get("left") + "px;";
			curStyle += "cursor:pointer;";

			out += " style='" + curStyle + "' ";
			out += " id='" + n.id + "' ";
			out += " class='" + n.params.get("image") + "' ";

			String link = "/pkgctrl";
			link += "?ip=" + n.params.get("ip");
			link += "&pkg=all";

			out += " onclick=\"window.location='" + link + "';\" ";

			out += " title='" + n.params.get("descr") + "' ";
			out += ">";

			if (n.params.containsKey("image")) {
				out += "<img ";
				out += " id='" + n.id + "_node_img' ";
				out += " src='/img/" + n.params.get("image") + ".png' ";
				out += ">";
			}
			out += "</div>";

			String cStyle, onClk;
			int topStep = 0;
			int top, left;
			for (CluPackage p : n.packages) {

				top = Integer.valueOf(n.params.get("top")) + topStep + 15;
				left = Integer.valueOf(n.params.get("left")) + 90;

				cStyle = "position:absolute;cursor:pointer;";
				cStyle += "top:" + top + "px;";
				cStyle += "left:" + left + "px;";

				link = "/pkgctrl";
				link += "?ip=" + n.params.get("ip");
				link += "&pkg=" + p.id;

				onClk = " onclick=\"window.location='" + link + "';\" ";
				;

				out += "<br><div class='package' ";
				out += onClk + " style='" + cStyle + "'>";
				out += "<img id='" + n.id + "_" + p.id + "' src='/img/Q.png'>&nbsp;" + p.id + "</div>";

				topStep += 20;

			}

		}

		String curStyle = "position:absolute;";
//		String masterHost = gData.getMasterHostName();
		String masterHost = "mlk-server01";
		String left = "", top = "";


		for (CluNode n : gData.nodes) {
			if (n.id.equals(masterHost)) {
				left = n.params.get("left");
				top = n.params.get("top");
			}

		}

		
		curStyle += "left:" + left + "px;";
		curStyle += "top:" + top + "px;";

		out += "<div id='masterSign' style='" + curStyle + "'";
		out += " title='master' >";
		out += "<img src='/img/empty.png'></div>";

		

		
		
		
		if (gData.thereIsReplication) {
	
			curStyle = "position:absolute;";
			curStyle += "left:" + left  + "px;";
			curStyle += "top:" + (top + Integer.valueOf(gData.commonParams.get("replicationLabelOffset"))) + "px;";
	
			out += "<div id='replicationStatus' class='replication' style='" + curStyle + "'";
			out += " title='replication status' >";
			out += "</div>";
		}
	
		
		
		curStyle += "left:" + gData.commonParams.get("journalAreaLeft")+ "px;";
		curStyle += "top:" + (gData.maxTop + Integer.valueOf(gData.commonParams.get("journalAreaOffsetTop"))) + "px;";
		
		out += "<div id='journal' class='journal' style='" + curStyle + "'";
		out += " title='journal' >";
		out += "</div>";		
		
		
		
		
		
		out += "<script>";

		for (CluNode n : gData.nodes) {
			out += "addLabel('" + n.id + "','" + n.params.get("ip") + " <b>" + n.id + "</b>');\n";
		}
		out += "</script>";

		out += strPopupMenu();

		out += refreshFunctions();

		out += getEndPage();
		return out;
	}

	public String strPopupMenu() {
		String out = "";
		String link = "";

		out += "<div class='menu'>  \n";

		link = "scenario";
		out += "<div id='menu_" + link + "' class='menu-item' onclick=\"window.location='" + link + "';\">"
				+ gData.tr("Scenario") + "</div>  \n";

		link = "configcheck";
		out += "<div id='menu_" + link + "' class='menu-item' onclick=\"window.location='" + link + "';\">"
				+ gData.tr("Configuration check") + "</div>  \n";

		link = "pause";
		out += "<div id='menu_" + link + "' class='menu-item' onclick=\"window.location='" + link + "';\">"
				+ gData.tr("Pause") + "</div>  \n";

		
		link = "help";
		out += "<div id='menu_" + link + "' class='menu-item' onclick=\"window.location='" + link + "';\">"
				+ gData.tr("Help") + "</div>  \n";

		link = "about";
		out += "<div id='menu_" + link + "' class='menu-item' onclick=\"window.location='" + link + "';\">"
				+ gData.tr("About") + "</div>  \n";



		out += "</div>  \n";
		
		return out;
	}
	public String refreshFunctions() {

		String out = "";
		out += "<script>";

		int interval = 3000;
		String link = "";

		if (gData.commonParams.containsKey("refreshGuiIntervalMilliSec")) {
			interval = Integer.valueOf(gData.commonParams.get("refreshGuiIntervalMilliSec"));		
		}
		

		for (CluNode n : gData.nodes) {

			out += " setInterval(async () => { \n";
			out += " var statuses; \n";	
			out += " var ip = '" + n.params.get("ip") + "'; \n";		
			out += " var hostName = '" + n.id + "'; \n";			
			

			link = "redirect?page=status&ip=" + n.params.get("ip");
			link += "&act=show_pkg_status";

			out += "getStatus('" + link + "',function(statuses){ \n";
	
			if(gData.debugInfo) out += "console.log(statuses); \n";
	
			
			out += "var resp = JSON.parse(statuses);\n";			
			
			out += "for(i=0; i< Object.keys(resp.statuses).length; i++) { \n";
			out += "	var curId = hostName + '_' + resp.statuses[i].pkg ; \n";
			out += "	var curImage = 'img/' + resp.statuses[i].status + '.png'; \n";			

			out += "	var obj=document.getElementById(curId); \n";
			out += "	if(obj){ \n";
			out += "		obj.src = curImage ; \n";
			out += "		obj.title = getTitleForStatus(resp.statuses[i].status); \n";		
			out += "	} \n";

			
			out += "} \n";
			
			
			out += "}); \n";
	

			out += " }, " + interval + "); \n";
		}

		
		
	/// Master role refresh function	
		out += " setInterval(async () => { \n";		

		link = "status?act=show_master_icon";

		out += "getStatus('" + link + "',function(statuses){ \n";		
		
		if(gData.debugInfo) out += "console.log(statuses); \n";

		out += "var resp = JSON.parse(statuses);\n";

		out += "	var obj=document.getElementById('masterSign'); \n";
		out += "	if(obj){ \n";
		
		out += "		obj.childNodes[0].src = 'img/' + resp.image ; \n";
		
		out += "		obj.style.top = resp.top + 'px'; \n";		
		out += "		obj.style.left = resp.left + 'px'; \n";
		out += "		obj.title = resp.message; \n";		
		
		out += "	} \n";

		if (gData.thereIsReplication) {
		
			out += "	var obj=document.getElementById('replicationStatus'); \n";
			out += "	if(obj){ \n";
			out += "		obj.style.top = resp.top2 + 'px'; \n";		
			out += "		obj.style.left = resp.left2 + 'px'; \n";
			out += "		obj.innerHTML = resp.replication; \n";				
			out += "	} \n";
		
		}		
		
		out += "}); \n";		
		out += " }, " + interval + "); \n";		
		

		/// JOURNAL refresh function	
		out += " setInterval(async () => { \n";		

		link = "journal?act=show_common_journal";

		out += "getStatus('" + link + "',function(statuses){ \n";		
		
			if(gData.debugInfo) out += "console.log(statuses); \n";


		out += "	var obj=document.getElementById('journal'); \n";
		out += "	if(obj){ \n";
		
		out += "		obj.innerHTML = '<br>' + statuses; \n";
		
		out += "	} \n";
		
		
		out += "}); \n";		
		out += " }, " + interval + "); \n";	
		
		
		out += "</script>";
		return out;

	}



}
