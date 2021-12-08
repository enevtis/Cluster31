package pages.quorum.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.net.ssl.SSLSocket;

import cluster.nvs.com.globalData;
import neserver.nvs.com.HandlerTemplate;

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

		out += "<p class='caption1'>&nbsp;&nbsp;&nbsp;&nbsp;<b>quorum server</b> </p>";
		
		
		
		out += strPopupMenu();
		
		out += getEndPage();
		return out;
	}
	public String strPopupMenu() {
		String out = "";
		String link = "";

		out += "<div class='menu'>  \n";
		
		link = "help";
		out += "<div id='menu_" + link + "' class='menu-item' onclick=\"window.location='" + link + "';\">"
				+ gData.tr("Help") + "</div>  \n";

		link = "about";
		out += "<div id='menu_" + link + "' class='menu-item' onclick=\"window.location='" + link + "';\">"
				+ gData.tr("About") + "</div>  \n";



		out += "</div>  \n";
		
		return out;
	}
}
