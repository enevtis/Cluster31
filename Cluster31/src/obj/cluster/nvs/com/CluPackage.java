package obj.cluster.nvs.com;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class CluPackage {
	public String id;
	public Hashtable<String, String> params = new Hashtable<String, String>();
	public List<CluResource> resources = new ArrayList<>();
}
