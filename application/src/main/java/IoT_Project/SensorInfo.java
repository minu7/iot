package IoT_Project;

import java.sql.Timestamp;

import org.json.simple.JSONObject;

public class SensorInfo {
	public Timestamp timestamp;
	public double temperature;
	public double max_temp;
	// public int actuator_active_seconds;
	
	@Override
	public String toString() {
		return "SensorInfo [timestamp=" + timestamp + ", temperature=" + temperature + ", max_temp=" + max_temp
				+ "]"; //, actuator_active_seconds=" + actuator_active_seconds + "]";
	}
	
	public SensorInfo(JSONObject msg) {
		this.timestamp = new Timestamp(((Long)msg.get("dt")) * 1000); 
		this.temperature = new Double(msg.get("t").toString());
		this.max_temp = new Double(msg.get("mt").toString());
		// this.actuator_active_seconds = Integer.parseInt(msg.get("as").toString());
	}
}
