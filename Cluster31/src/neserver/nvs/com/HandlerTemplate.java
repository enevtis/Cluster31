package neserver.nvs.com;

import java.util.HashMap;
import java.util.Map;

import cluster.nvs.com.globalData;
import obj.cluster.nvs.com.CluNode;



public class HandlerTemplate {

	public globalData gData;
	public Map<String, String> params;
	public CluNode curNode = null;
	
	public HandlerTemplate(globalData gData) {
		this.gData = gData;
	}

	
	public Map<String, String> queryToMap(String query) {

		if (query == null || query.isEmpty())
			return null;

		Map<String, String> result = new HashMap<>();
		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1) {
				result.put(entry[0], entry[1]);
			} else {
				result.put(entry[0], "");
			}
		}
		return result;
	}

	public void parseParams(String requestString) {
		
		
		String strForParse = "";
		String[] parts = requestString.split("\\?+");
		
		if (parts.length == 2) {
			params = queryToMap(parts[1]);
		} else {
			params = queryToMap(requestString);
			
		}
		
	}
public String header200() {
	String out = "";
	out += "HTTP/1.0 200 OK\r\n";
	out += "Content-Type: text/html; charset=utf-8\r\n";
	return out;
}
public String header302(String location) {
	String out = "";
	out += "HTTP/1.0 302 Redirect\r\n";
	out += "Location:" + location +"\r\n";
	out += "Content-Type: text/html; charset=utf-8\r\n";
	return out;
}

protected String getCSS() {

	String out = "";
	
	out += " body { \n";
	out += "font-family: 'Tahoma';\n";
//	out += "background-color: '" + gData.commonParams.get("backgroundColor") + "';\n";	

	out += "}";		


	out += ".table1 { \n";
	out += "border-collapse: collapse; \n";
	out += "font-family: Verdana, Arial, Helvetica, sans-serif;\n";	
	out += "font-size: 12px; \n";		
	out += "background-color:#191970\n";
	out += "border-spacing: 7px 5px;  \n";	
	out += "border: 1px solid #399;  \n";	
	out += "} \n";

	out += ".table1 th {";
	out += "background-color:#008080\n";	
	out += "} \n";

	out += ".table1 tr {";
	out += "background-color:#AFEEEE  \n";	
	out += "} \n";
	
	out += ".table1 td { \n";
	
	out += "padding: 1px; \n";
	out += "text-align: left; \n";
	out += "border-bottom: 1px solid #ddd; \n";
	out += "border: 1px solid #333; \n";
	out += "background-color:##E0FFFF\n";
	out += "} \n";
	out += ".table1 tr:hover {background-color:#f5f5f5;} \n";



	
	out += ".caption1 {  \n";
	out += "font-family: courier;\n";
	out += "font-size: 150%; \n";
	
	out += "text-align: left; \n";
	out += "color: #001099;\n";
	out += "} \n";

	
	
	out += ".group {  \n";
	out += "text-align: left; \n";
	out += "color: #001099;\n";
	out += "border: 1px solid #399;  \n";	
	out += "} \n";		

	out += ".server1 {  \n";
	out += "width:90px; \n";
	out += "height:35px; \n";
	out += "} \n";		

	out += ".server2 {  \n";
	out += "width:71px; \n";
	out += "height:170px; \n";
	out += "} \n";	
	
	return out;

}


protected String getBeginPage(){
String out = "";
	out += "<!DOCTYPE html> \n";
	out += "<html> \n";
	out += "<head> \n";
	out += "<meta charset=\"utf-8\"> \n";
	out += "<link rel=\"icon\" href=\"/img/nvs.png\"> \n";
	out += "<script src=\"/src/cluster.js\"></script> \n";
	out += "<link rel=\"stylesheet\" href=\"/src/style.css\"> \n";
	out += "</head>";
	out += "<style>" + getCSS() + "</style>";	
	out += "<body style='background-color:" + gData.commonParams.get("backgroundColor") + ";'>";	

	
	return out;
}

protected String getEndPage(){
String out = "";
	out += " </body> \n";
	out += " </html>  \n";
return out;
}
}
