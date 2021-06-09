package utils.cluster.nvs.com;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	public static String runSuCommand(String user, String command, Logger logger) {

		String result = "";

		try {

			ProcessBuilder pb = new ProcessBuilder("su", "-", user, "-c", command);
			pb.redirectErrorStream(true);

			Process p = pb.start();

			
			
//			p.waitFor(5, TimeUnit.SECONDS);
//			p.destroy();
			p.waitFor();

			Reader reader = new InputStreamReader(p.getInputStream());
			int ch;
			while ((ch = reader.read()) != -1) {
				result += ((char) ch);

			}
			reader.close();

		} catch (Exception e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			logger.severe(errors.toString());
			e.printStackTrace();
			result = "E";
		}

		return result;

	}

	public static String readTextFileToHtml(String fullName, Logger logger) {

		String out = "";

		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fullName), "UTF8"))) {

			String cLine;
			while ((cLine = br.readLine()) != null) {
				sb.append(cLine).append("<br>");
			}
		} catch (IOException e) {

			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));

			logger.severe(errors.toString());
			out = errors.toString();
		}

		out = sb.toString();

		return out;
	}

	public static List<String> getTagValues(String inputString,String tagName) {
	    List<String> tagValues = new ArrayList<String>();
	    Pattern TAG_REGEX = Pattern.compile("<"+tagName+">(.+?)</"+tagName+">", Pattern.DOTALL);
	    Matcher matcher = TAG_REGEX.matcher(inputString);
	    while (matcher.find()) {
	        tagValues.add(matcher.group(1));
	    }
	    return tagValues;
	}

	public static String getValue(String inputString,String tagName) {
		String out = "";
		Pattern TAG_REGEX = Pattern.compile("<"+tagName+">(.+?)</"+tagName+">", Pattern.DOTALL);
		Matcher matcher = TAG_REGEX.matcher(inputString);
	    while (matcher.find()) {
	        out = matcher.group(1);
	    }
		return out;
	}
	
}
