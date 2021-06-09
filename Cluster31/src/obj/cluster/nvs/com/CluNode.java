package obj.cluster.nvs.com;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class CluNode {
	public String id;
	public Hashtable<String, String> params = new Hashtable<String, String>();
	public List<CluPackage> packages = new ArrayList<>();
}
