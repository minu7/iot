package IoT_Project;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.Queue;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;

public class ObserveResource implements CoapHandler {
	Queue<SensorInfo> sensored_values = new LinkedList<SensorInfo>();
	private String name;
	// int numIns = 0;
	int max_size = 35;
	boolean yellow_alert = false;
	
	public ObserveResource(String name) {
		super();
		this.name = name;
	}

	@Override
	public void onLoad(CoapResponse response) {
		try {
			// System.out.println(response.getResponseText().toString());
			JSONObject msg = (JSONObject)JSONValue.parseWithException(response.getResponseText().toString());

			if (sensored_values.size() >= max_size) {
				sensored_values.poll();
			}
			SensorInfo t = new SensorInfo(msg);
			sensored_values.add(t);
		} catch(org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}	
	}
	
	@Override
	public String toString() {
		String tmp = "";
		for (SensorInfo s : sensored_values) {
			tmp += s + "\n";
		}
		return tmp;
	}

	public void printAlert(String lev, String val, Timestamp t) {
		if(lev.equals("YELLOW")) {
			if(!yellow_alert) {
				System.out.println("Alert level: " + lev + " on " + name + ", carbon dioxide level:" + val + " at " + t);
				yellow_alert =true;
			}else {
				yellow_alert=false;
			}
		}else {
			System.out.println("Alert level: " + lev + " on " + name + ", carbon dioxide level:" + val + " at " + t);
		}	
	}
	
	@Override
	public void onError() {
		System.out.println("An error occurred while observing");
		
	}
	
}
