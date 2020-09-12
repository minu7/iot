package IoT_Project;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Interface {
	static Server regServer = new Server(5683);
	
	public static void main(String[] args) {
		new Thread() {
			public void run() {
				regServer.start(); 
			}
		}.start();
		
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader commandLine = new BufferedReader(input);
		showCommand();
		while (true) {
			String str = "";
			
			try {
				str = commandLine.readLine();
			} catch(Exception e) {
				System.out.println(e.toString());
			}
			
			String[] command = str.split(" ");
			if (!validCommand(command)) {
				System.out.println("The command provided is not valid");
				showCommand();
				continue;
			}
			switch (command[0]) {
				case "list": //show the list of all the resources registered
					showResources();
					break;
		
				case "get": //get the info from a sensor or an actuator
					if(command[1].contentEquals("all") && command[2].equals("sensors")) {
						getAll("sensors");
					}else if(command[1].contentEquals("all") && command[2].equals("actuators")) {
						getAll("actuators");
					}else if (command[3].equals("sensor")) {
						getStatus(command[1] + " " + command[2], "sensors");
					} else {
						getStatus(command[1] + " " + command[2], "actuators");
					}
					
					break;

				case "set": //set the status of a sensor/all sensors
					if (!command[1].contentEquals("all")) {
						setMaxTemp(command[1] + " " + command[2], new Double(command[3])); //set a specific actuator
					} else {
						setAll(new Double(command[2])); //set all actuators
					}
					break;
			
				case "historic":
					viewHistoric(command[1] + " " + command[2]);
					break;
		
				case "close":
					System.out.println("Closing the application");
					System.exit(0);
					break;
			}
		}
	}
	
	public static void showCommand() {
		System.out.println("The available commands are the following:");
		System.out.println("list: to see the resources available");
		System.out.println("get node # sensor: to see the info registered by the sensor choosen");
		System.out.println("get all sensors: to see the info registered by all sensors");
		System.out.println("get node # actuator: to see the status of the actuator choosen");
		System.out.println("get all actuators: to see the status of all actuators");
		System.out.println("set node # *max_temp*: to set the max_temp of the actuator identified by index");
		System.out.println("set all *max_temp*: to set the max_temp of all actuators");
		System.out.println("historic node #: to see the historic of the latest sensing of the node");
		System.out.println("close: to close the application");
	}
	
	public static void showResources() {
		for (RegisteredResource resource : Server.regResources) {
			System.out.println(resource.getName() + " " + resource.getPath());
		}
	}
	
	public static void getStatus(String node, String type) {
		for (RegisteredResource resource : Server.regResources) {
			// in path the type is explicited
			if(node.equals(resource.getName()) && (resource.getPath().contains(type))){
				String response = resource.get(MediaTypeRegistry.APPLICATION_JSON).getResponseText();
				try {
					JSONObject msg = (JSONObject) JSONValue.parseWithException(response);
					if (type == "actuators") {
						ActuatorInfo a = new ActuatorInfo(msg);
						System.out.println(a);
					} else if (type == "sensors") {
						SensorInfo s = new SensorInfo(msg);
						System.out.println(s);
					}
					return;
				}catch (org.json.simple.parser.ParseException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Resource not found");
	}
	
	public static void getAll(String type) {
		for (RegisteredResource resource : Server.regResources) {
			if (resource.getPath().contains(type)) {
				String response = resource.get(MediaTypeRegistry.APPLICATION_JSON).getResponseText();
				try {
					JSONObject msg = (JSONObject) JSONValue.parseWithException(response);
					if (type == "actuators") {
						ActuatorInfo a = new ActuatorInfo(msg);
						System.out.println(a);
					} else if (type == "sensors") {
						SensorInfo s = new SensorInfo(msg);
						System.out.println(s);
					}
				} catch(org.json.simple.parser.ParseException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void setMaxTemp(String node, Double maxTemp) {
		for (RegisteredResource resource : Server.regResources) {
			if (node.equals(resource.getName()) && resource.getPath().contains("actuators")) {
				CoapResponse response = resource.post("max_temp=" + maxTemp, MediaTypeRegistry.TEXT_PLAIN);
				String resCode = response.getCode().toString();
				
				if (resCode.startsWith("2")) {
					System.out.println("Acturator " + node + " new max_temp = " + maxTemp);
				} else {
					System.out.println("Error " + resCode);
				}
				return;
			}
		}
		System.out.println("Error: resource not found");
	}
	
	public static void setAll(Double maxTemp) {
		for (RegisteredResource resource : Server.regResources) {
			if (resource.getPath().contains("actuators")) {
				CoapResponse response = resource.post("max_temp=" + maxTemp, MediaTypeRegistry.TEXT_PLAIN);
				String resCode = response.getCode().toString();
				
				if (resCode.startsWith("2")) {
					System.out.println("Acturator " + resource.getName() + " new max_temp = " + maxTemp);
				} else {
					System.out.println("Error " + resCode);
				}
			}
		}
	}
	
	public static void viewHistoric(String node) {
		for(RegisteredResource r  : Server.regResources) {
			// System.out.println(r.getName());
			// System.out.println(r.getPath());
			// System.out.println(node.equals(r.getName()));
			// System.out.println(r.getPath().contains("sensors"));
			if (node.equals(r.getName()) && (r.getPath().contains("sensors"))){
				System.out.println(r.obsRes);
				break;
			}
		}
	}
	
	public static boolean validCommand(String[] command ) {
		switch (command[0]) {
			case "get":
				if (command[1].equals("node") && !(command.length == 4)) {
					return false;
				}
				if (command[1].equals("all") && !(command.length == 3)) {
					return false;
				}
				if (command[1].equals("all") && (command[2].equals("sensors") || command[2].equals("actuators"))) {
					return true;
				} else if(command[1].equals("node") && (command[3].equals("sensor") || command[3].equals("actuator"))) {
					return true;
				} else {
					return false;
				}
	
			case "set":
				if (command[1].equals("node")) {
					if (!(command.length == 4)) {
						return false;
					}
					if (Utils.isNumeric(command[3])) {
						return true;
					}
				} else if(command[1].equals("all")){
					return true;
				} else {
					return false;
				}
	
			case "historic":
				if (command[1].equals("node")) {
					return true;
				} else {
					return false;
				}

			case "close":
				return true;

			case "list":
				return true;

		}
		return false;
	}

}
