package IoT_Project;

import org.json.simple.JSONObject;

public class ActuatorInfo {
	public boolean cooling;
	public double temperature;
	public double max_temp;
	public int actuator_active_seconds;

	@Override
	public String toString() {
		return "ActuatorInfo [cooling=" + cooling + ", temperature=" + temperature + ", max_temp=" + max_temp
				+ ", actuator_active_seconds=" + actuator_active_seconds + "]";
	}
	
	public ActuatorInfo(JSONObject msg) {
		this.cooling = new Integer(msg.get("c").toString()) > 0; 
		this.temperature = new Double(msg.get("t").toString());
		this.max_temp = new Double(msg.get("mt").toString());
		this.actuator_active_seconds = Integer.parseInt(msg.get("as").toString());
	}
}

