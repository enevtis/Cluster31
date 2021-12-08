package neserver.nvs.com;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.net.ssl.*;

import cluster.nvs.com.globalData;

public class NeServer extends Thread {

	public SSLSocket socket;
	public globalData gData;
	

	public NeServer(SSLSocket socket, globalData gData) {
		this.socket = socket;
		this.gData = gData;
	}
	public NeServer(globalData gData) {
		this.gData = gData;
		
		
	}
	
	public void activate() {
	

			int port = Integer.valueOf(gData.commonParams.get("webServicePort"));
	
			try {

				SSLServerSocketFactory serverSocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

				SSLServerSocket sslServerSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(port);
				System.out.println("Https server is listening at port " + port + "");

				while (true) {

					SSLSocket sslSocket = (SSLSocket) (sslServerSocket.accept());
					(new NeServer(sslSocket, gData)).start();
					
				}
			
			
			} catch (IOException e) {
//				System.out.println(
//					"Exception caught when trying to listen on port " + port + " or listening for a connection");
//				System.out.println(e.getMessage());
			} 
		

	
	}
	


	@Override
	public void run() {


		try {


			List<String> tokens = new ArrayList<>();
			String t = "";
			boolean wasAuthorized = false;
			String[] parts;
			
			BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			
			String s;

			while ((s = in.readLine()) != null) {
				if (s.isEmpty()) break;
				tokens.add(s);
				
			}
			
			
			
			
			for(int i=0; i < tokens.size(); i++) {
				t = tokens.get(i);
				
				
				
				if (t.contains("Authorization")) {
					
					if ( checkIsAuthorized(t) ) {
						
						wasAuthorized = true;
						
						
							for(int y=0; y < tokens.size(); y++) {
						
								if(tokens.get(y).contains("GET")) {
								
									parts=tokens.get(y).split("\\s+");

									routeResponse(this.socket, parts[1], this.gData);

								}
						
						
							}
						
						
						}
					
					
					}	
				}	
					
				if (!wasAuthorized) {
					
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					out.writeBytes("HTTP/1.1 401 Unauthorized\r\n");
					out.writeBytes("WWW-Authenticate: Basic realm=\"NVS\"\r\n");
					out.writeBytes("Content-Type: text/html; charset=UTF-8\r\n");
					out.writeBytes("\r\n");	
					out.close();
				}	

	
			in.close();
			this.socket.close();
            

		
		} catch (Exception e) {
			System.err.println(e);
		}
	} 
			@SuppressWarnings("deprecation")
	public void routeResponse(SSLSocket socket, String requestString, globalData gData){
		
		
		if (requestString.contains("favicon.ico")) return ;
		if (requestString.isEmpty()) return ;
		
		try {
			

			String handlerClassName = gData.router.get(parseHandlerName(requestString));

			Class c = Class.forName(handlerClassName);

			
			Constructor cnstr = Class.forName(handlerClassName).getConstructor(cluster.nvs.com.globalData.class);

			Object obj = cnstr.newInstance(gData);			

			Class[] paramTypes = new Class[] { SSLSocket.class, String.class };	

			Method method = c.getMethod("getResponse",paramTypes);
			
			Object[] args = new Object[] {socket,requestString};
			
			method.invoke(obj,args);
	
		
		} catch (InvocationTargetException e) {

			StringWriter errors = new StringWriter();
			e.getCause().printStackTrace(new PrintWriter(errors));
			gData.logger.severe(requestString + " *** InvocationTargetException *** " + errors.toString());		    


		}catch (Exception e) {
			
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(requestString + " *** " + errors.toString());
		}
	
	}

	private String parseHandlerName(String requestString) {
		String out = "";
		String[] parts;
		String tempStr = requestString;
		
		if (tempStr.contains("?")) {
			parts = tempStr.split("\\?");
			out = parts[0];
			
		} else {
			if (tempStr.startsWith("/img")) {
				out = "/img";
			} else if (tempStr.startsWith("/src")){
				out = "/src";
			} else {
				out = tempStr;
			}
			
		}
		
		return out;
	} 
	
	
	
	public boolean checkIsAuthorized(String AuthString){
		boolean out = false;

		String loginPass = "";
		String parts[] = AuthString.split("\\s+");
		
		if (parts.length !=3) return false;
		
		byte[] decodedBytes = Base64.getDecoder().decode(parts[2]);
		loginPass = new String(decodedBytes);

        String user = gData.commonParams.get("webAdminUser");
        String password = gData.commonParams.get("webAdminPassword");
		if (loginPass.equals(user + ":" + password)) return true;
	
		return out;
	}


}
