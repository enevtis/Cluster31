package neserver.nvs.com;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.net.ssl.SSLSocket;

import cluster.nvs.com.globalData;



public class EmptyHandler extends HandlerTemplate{
	
	
	public EmptyHandler(globalData gData) {
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
		out += "EMPTY";
		
		 for (Map.Entry mapElement : params.entrySet()) { 
	            String key = (String)mapElement.getKey(); 
	            String value = (String)mapElement.getValue(); 
	            out += "<br>" + key + " : " + value + ""; 
	        } 
		
		out += getEndPage();
		return out;
	}

}
