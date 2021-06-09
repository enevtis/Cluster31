package obj.cluster.nvs.com;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import obj.cluster.nvs.com.CluNode;

public class CluRemoteSystem {
	public String id;
	public String lastMasterIp;
	public Hashtable<String, String> params = new Hashtable<String, String>();
	public List<CluNode> nodes = new ArrayList<>();
	public LocalDateTime MasterAvailable = LocalDateTime.now();
	public boolean commandWasSend = false;
	
}
