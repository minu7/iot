package IoT_Project;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class RegisteredResource extends CoapClient {
	private String name;
	private String title;
	private String addr;
	private String path;
	private boolean isObservable;
	private String uri;
	private String type;
	CoapObserveRelation obsRelation;
	ObserveResource obsRes;
	
	public RegisteredResource(String path, String title, String addr, boolean isObs) {
		super();
		this.path = path;
		this.title = title;
		this.addr = addr;
		String[] node = addr.split(":");
		this.name = "node " + node[node.length - 1];
		this.uri = "coap://[" + this.addr + "]" + this.path;
		this.setURI(this.uri);
		this.isObservable = isObs;
		
		if (path.contains("sensors")) {
			this.type = "sensor";
		} else {
			this.type = "actuator";
		}
		
		if (this.isObservable) {
			this.obsRes = new ObserveResource(name);
			this.obsRelation = this.observe(obsRes, MediaTypeRegistry.APPLICATION_JSON);
		}
		System.out.println("Node " + name + ", " + path + " registered");
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getPath() {
		return this.path;
	}
	
	public String getTitle() {
		return this.title;
	}
	
	public String getAddr() {
		return this.addr;
	}
	
	public boolean getObs() {
		return this.isObservable;
	}
	
	public String getUri() {
		return this.uri;
	}
	
	public String getType() {
		return this.type;
	}	
}
