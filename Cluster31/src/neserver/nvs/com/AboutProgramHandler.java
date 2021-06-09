package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.net.ssl.SSLSocket;

import cluster.nvs.com.globalData;



public class AboutProgramHandler extends HandlerTemplate{
	
	String test = "";
	public AboutProgramHandler(globalData gData) {
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
		String curStyle = "";
		out += getBeginPage();


		out += "<p class='caption1'><img src='img/nvs.png'>Simple cluster manager</p>";

		out += "<br>Version: <b>" + globalData.getVersionInfo() + "</b><br><br>";

		out += "2018-2020 years. All rights reserved <a href='http://www.nvs-itech.com'>www.nvs-itech.com</a> . <br><br>";

		String[] parts = gData.getSystemProperties().split("\\s+");
		out += "<hr>";
		for (int i = 0; i < parts.length; i++) {

			out += parts[i] + "<br>";

		}

		out += getEndPage();

		return out;
	}

}
