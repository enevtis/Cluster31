package conf.cluster.nvs.com;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import cluster.nvs.com.HttpsRequestor;
import cluster.nvs.com.globalData;
import obj.cluster.nvs.com.CluNode;
import obj.cluster.nvs.com.CluPackage;
import obj.cluster.nvs.com.CluRemoteSystem;
import obj.cluster.nvs.com.CluResource;
import obj.cluster.nvs.com.CluScenarioStep;
import simplecrypto.nvs.com.SimpleCrypto;

public class ConfigurationReader {

	globalData gData = null;

	public ConfigurationReader(globalData gData) {
		this.gData = gData;
	}

	public void readAllScenarios(Map<String, List<CluScenarioStep>> scenarioList) {

		String folderName = gData.mainPath + File.separator + "conf";

		File folder = new File(folderName);

		List<CluScenarioStep> scenario = null;

		for (String s : folder.list()) {

			if (s.contains("scenario")) {

				String fileNameWithOutExt = s.replaceFirst("[.][^.]+$", "");
				String scenarioName = fileNameWithOutExt.replace("scenario_", "");

				scenario = new ArrayList<CluScenarioStep>();
				scenarioList.put(scenarioName, scenario);
				readScenario(folderName + File.separator + s, scenario);

			}

		}

	}

	public void readScenario(String scenarioFullFileName, List<CluScenarioStep> scenario) {

		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docBuilderFactory.newDocumentBuilder();

			docBuilder.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
					return new InputSource(new StringReader(""));
				}
			});

			Document document = docBuilder.parse(new File(scenarioFullFileName));
			NodeList listSteps = document.getElementsByTagName("scenarioSteps");

			listSteps = document.getElementsByTagName("step");

			for (int i = 0; i < listSteps.getLength(); i++) {

				if (listSteps.item(i).getNodeType() == Node.ELEMENT_NODE) {

					CluScenarioStep step = new CluScenarioStep();

					scenario.add(step);
					step.id = listSteps.item(i).getAttributes().getNamedItem("id").getTextContent();

					for (int y = 0; y < listSteps.item(i).getChildNodes().getLength(); y++) {

						if (listSteps.item(i).getChildNodes().item(y).getNodeType() == Node.ELEMENT_NODE) {

							Node nodeLevel2 = listSteps.item(i).getChildNodes().item(y);

							step.params.put(nodeLevel2.getNodeName(), nodeLevel2.getTextContent());

						}
					}

				}
			}
		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			gData.logger.severe(errors.toString());
		}

	}

	public void readNodesConfig(List<CluNode> nodes) {

		String fileNodes = gData.mainPath + File.separator + "conf" + File.separator + "nodes.xml";

		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docBuilderFactory.newDocumentBuilder();

			docBuilder.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
					return new InputSource(new StringReader(""));
				}
			});

			Document document = docBuilder.parse(new File(fileNodes));
			NodeList listNodes = document.getElementsByTagName("clusterNodes");
			String type, key, value = "";

			listNodes = document.getElementsByTagName("node");

			for (int i = 0; i < listNodes.getLength(); i++) {

				if (listNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

					key = listNodes.item(i).getAttributes().getNamedItem("id").getTextContent();

					CluNode n = new CluNode();
					nodes.add(n);

					n.id = listNodes.item(i).getAttributes().getNamedItem("id").getTextContent();

					for (int y = 0; y < listNodes.item(i).getChildNodes().getLength(); y++) {

						if (listNodes.item(i).getChildNodes().item(y).getNodeType() == Node.ELEMENT_NODE) {

							Node nodeLevel2 = listNodes.item(i).getChildNodes().item(y);

							type = nodeLevel2.getNodeName();

							if (nodeLevel2.getAttributes().getNamedItem("id") != null)
								key = nodeLevel2.getAttributes().getNamedItem("id").getTextContent();
							else
								key = "";

							value = nodeLevel2.getTextContent();

//	        				System.out.println("  2) typ=" + type + " key= " + key + " value=" + value);

							if (!type.equals("pack")) {
								n.params.put(nodeLevel2.getNodeName(), nodeLevel2.getTextContent());
							} else {

								CluPackage p = new CluPackage();
								n.packages.add(p);

								p.id = nodeLevel2.getAttributes().getNamedItem("id").getTextContent();

								for (int z = 0; z < nodeLevel2.getChildNodes().getLength(); z++) {

									if (nodeLevel2.getChildNodes().item(z).getNodeType() == Node.ELEMENT_NODE) {

										Node nodeLevel3 = nodeLevel2.getChildNodes().item(z);

										key = nodeLevel3.getNodeName();
										value = nodeLevel3.getTextContent();

										CluResource res = new CluResource();
										p.resources.add(res);
										res.id = nodeLevel3.getAttributes().getNamedItem("id").getTextContent();
										for (int s = 0; s < nodeLevel3.getChildNodes().getLength(); s++) {
											Node nodeLevel4 = nodeLevel3.getChildNodes().item(s);
											res.params.put(nodeLevel4.getNodeName(), nodeLevel4.getTextContent());

										}

									}

								}

							}

						}

					}

				}
			}
		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			gData.logger.severe(errors.toString());
		}

	}

	public void addDefaultScriptsToConfig(List<CluNode> nodes) {

		for (CluNode n : nodes) {

			for (CluPackage p : n.packages) {

				for (CluResource r : p.resources) {

					if (!r.params.contains("start"))
						r.params.put("start", r.id + "-start.sh");
					if (!r.params.contains("stop"))
						r.params.put("stop", r.id + "-stop.sh");
					if (!r.params.contains("status"))
						r.params.put("status", r.id + "-status.sh");

				}
			}
		}

	}

	public void readCommonParameters(Hashtable<String, String> commonParams) {

		String fullPathFileName = gData.mainPath + File.separator + "conf" + File.separator + "common.properties";

		try {

			Properties params = new Properties();

			FileInputStream input = new FileInputStream(new File(fullPathFileName));
			params.load(new InputStreamReader(input, Charset.forName("UTF-8")));

			Enumeration e = params.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				String value = params.getProperty(key);
				gData.commonParams.put(key, value);

			}

			if (gData.commonParams.containsKey("hash")) {
				SimpleCrypto crypto = new SimpleCrypto();
				gData.commonParams.put("password", SimpleCrypto.decrypt(gData.SecretKey, commonParams.get("hash")));
			}

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			gData.logger.severe(errors.toString());

		}

	}

	public boolean determineMasterRole() {


		boolean out = true;
		String osUser = "root";
		String myHostName = gData.getOwnHostname();

		boolean wasManyMasters = false;

		CluNode myN = gData.getOwnNode();

		gData.logger.info("Determination master node : My host (" + myHostName + ")");

		
		if (myN == null) { 
			gData.logger.info("ERROR! It seems the system is not correct configured yet...") ;
			return false;
		}
	

		 if (!myN.params.get("canBeMaster").equals("yes")) {
			gData.logger.info("Parameter canBeMaster <> yes, I cannot be master :-( I am always slave");
			return false;
		 }
	

		////////// Single node can be ONLY master ////
		if(gData.nodes.size() ==1) {	
			
			String masterFlagFileName = gData.mainPath + File.separator + "tmp";
			masterFlagFileName += File.separator + "master";

			File f = new File(masterFlagFileName);

			gData.logger.info(masterFlagFileName + " was created.");

			if (!f.exists()) {
				
				String command = "touch " + masterFlagFileName;
				String response = runSuCommand(osUser, command);
				gData.logger.info(masterFlagFileName + " was created:" + response);
				
			} 
			
			return true;
		}
		
		
		
		
		String myRole = determineLocalRole();

		gData.logger.info("My role is:" + myRole);

		if (myRole.equals("slave"))
			return false;

		for (CluNode n : gData.nodes) {

			if (n.params.get("canBeMaster").equals("yes")) {

				if (!n.id.equals(myHostName)) {

					String url = "https://" + n.params.get("ip") + ":";
					url += gData.commonParams.get("webServicePort") + "/";
					url += "status?act=show_role";

					gData.logger.info("Another node " + n.id + " said : ");

					HttpsRequestor r = new HttpsRequestor(gData);

					String resp = r.doGetRequest(url);

					gData.logger.info("he is " + resp);

					if (resp.contains("master"))
						out = false;

				}
			}
		}

		gData.logger.info("It is ok, I am ONLY master and will command!");

		return out;
	}

	public void setMaxTopCoordinateOfNodeOnMainPage() {

		for (CluNode n : gData.nodes) {

			int curTop = Integer.valueOf(n.params.get("top"));

			if (curTop > gData.maxTop)
				gData.maxTop = curTop;

		}

	}
	public static String runSuCommand(String user, String command) {

		String result = "";

		try {

			ProcessBuilder pb = new ProcessBuilder("su", "-", user, "-c", command);
			pb.redirectErrorStream(true);

			Process p = pb.start();

			int retCode = p.waitFor();

			Reader reader = new InputStreamReader(p.getInputStream());
			int ch;
			while ((ch = reader.read()) != -1) {
				result += ((char) ch);

			}
			reader.close();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			result =errors.toString();
			e.printStackTrace();
			result = "E";
		}

		return result;

	}
	public String determineLocalRole() {

		String out = "slave";
		String masterFlagFileName = gData.mainPath + File.separator + "tmp" + File.separator + "master";

		File f = new File(masterFlagFileName);

		if (f.exists()) {
			out = "master";
		}
		return out;
	}


	public void readQuorumConfig(List<CluRemoteSystem> remoteSystems) {

		String fileNodes = gData.mainPath + File.separator + "conf" + File.separator + "quorum.xml";

		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docBuilderFactory.newDocumentBuilder();

			docBuilder.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String arg0, String arg1) throws SAXException, IOException {
					return new InputSource(new StringReader(""));
				}
			});

			Document document = docBuilder.parse(new File(fileNodes));
			NodeList listNodes = document.getElementsByTagName("remoteSystems");
			String type, key, value = "";

			listNodes = document.getElementsByTagName("system");

			for (int i = 0; i < listNodes.getLength(); i++) {

				if (listNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {

					key = listNodes.item(i).getAttributes().getNamedItem("id").getTextContent();

					CluRemoteSystem rs = new CluRemoteSystem();
					remoteSystems.add(rs);

					rs.id = listNodes.item(i).getAttributes().getNamedItem("id").getTextContent();

					for (int y = 0; y < listNodes.item(i).getChildNodes().getLength(); y++) {

						if (listNodes.item(i).getChildNodes().item(y).getNodeType() == Node.ELEMENT_NODE) {

							Node nodeLevel2 = listNodes.item(i).getChildNodes().item(y);

							type = nodeLevel2.getNodeName();

							if (nodeLevel2.getAttributes().getNamedItem("id") != null)
								key = nodeLevel2.getAttributes().getNamedItem("id").getTextContent();
							else
								key = "";

							value = nodeLevel2.getTextContent();

//	        				System.out.println("  2) typ=" + type + " key= " + key + " value=" + value);

							if (!type.equals("node")) {
								rs.params.put(nodeLevel2.getNodeName(), nodeLevel2.getTextContent());
							} else {

								CluNode n = new CluNode();
								rs.nodes.add(n);

								n.id = nodeLevel2.getAttributes().getNamedItem("id").getTextContent();

								for (int z = 0; z < nodeLevel2.getChildNodes().getLength(); z++) {

									if (nodeLevel2.getChildNodes().item(z).getNodeType() == Node.ELEMENT_NODE) {

										Node nodeLevel3 = nodeLevel2.getChildNodes().item(z);

										key = nodeLevel3.getNodeName();
										value = nodeLevel3.getTextContent();

										n.params.put(key,value);


									}

								}

							}

						}

					}

				}
			}
		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			gData.logger.severe(errors.toString());
		}

	}
	

}
