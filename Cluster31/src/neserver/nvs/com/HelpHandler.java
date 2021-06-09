package neserver.nvs.com;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.net.ssl.SSLSocket;

import cluster.nvs.com.globalData;

public class HelpHandler extends HandlerTemplate{
	
	
	public HelpHandler(globalData gData) {
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
		out += "<p class='caption1'>" + gData.tr("Help") + "</p>";

		String pageName = "/resources/help/help.html";
		
		if (params!=null) {
			if (params.containsKey("page")) {
				pageName = "/resources/help/" + params.get("page") + ".html";
			} 
		}

		out += getHelp(pageName);

		out += getEndPage();
		return out;
	}

	public String getHelp(String page) {
		String out = "";


		InputStream in = getClass().getResourceAsStream(page);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;

		byte[] data = new byte[1048576];

		try {
			while ((nRead = in.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);

				buffer.flush();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		out = buffer.toString();

		return out;
	}
}
