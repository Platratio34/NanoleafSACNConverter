package main;

import java.util.HashMap;
import java.util.List;

import dataManagment.JsonObj;
import dataManagment.JsonSerializable;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.Shapes;

public class DMXDevice implements JsonSerializable {

	private int uni;
	private int adr;
	private int absAdr;
	
	private int[] levels;
	private int nCh = 3;
	
	private NanoleafDevice device;
	private Panel[] panels;
	
	private DeviceUpdater updater;
	private boolean heldChange = true;
	private long maxBetween = 10*1000;
	private long lastUpdate = 0;
	
	
	public DMXDevice(int uni, int adr, Shapes device) {
		this.uni = uni;
		this.adr = adr;
		this.device = device;
		try {
			List<Panel> p = device.getPanels();
			for(Panel P : p) {
				if(P.getId() == 0) {
					p.remove(P);
				}
			}
			panels = new Panel[0];
			panels = device.getPanels().toArray(panels);
		} catch (Exception e) {
			e.printStackTrace();
		}
		levels = new int[nCh*panels.length+1];
		updater = new DeviceUpdater(device, panels);
	}
	public DMXDevice(int absAdr, NanoleafDevice d) {
		setAbsAdr(absAdr);
		this.device = d;
		try {
			panels = new Panel[0];
			panels = d.getPanels().toArray(panels);
		} catch (Exception e) {
			e.printStackTrace();
		}
		levels = new int[nCh*panels.length+1];
		updater = new DeviceUpdater(device, panels);
	}
	public DMXDevice(JsonObj obj, NanoleafDevice d) {
		device = d;
		try {
			panels = new Panel[0];
			panels = d.getPanels().toArray(panels);
		} catch (Exception e) {
			e.printStackTrace();
		}
		levels = new int[nCh*panels.length+1];
		deserialize(obj);
		updater = new DeviceUpdater(device, panels);
	}
	
	public int getUni() {
		return uni;
	}
	public int getAdr() {
		return adr;
	}
	public int getAbsAdr() {
		absAdr = (uni-1) * 512;
		absAdr += adr;
		return absAdr;
	}
	public NanoleafDevice getDevice() {
		return device;
	}
	
	public void setUni(int uni) {
		this.uni = uni;
		getAbsAdr();
	}
	public void setAdr(int adr) {
		this.adr = adr;
		getAbsAdr();
	}
	public void setAbsAdr(int abs) {
		absAdr = abs;
		adr = abs % 512;
		uni = (abs-adr)/(512);
		uni += 1;
	}
	
	public void update(int[] data) {
		boolean c = false;
		for(int i = 0; i < levels.length; i++) {
			if(data.length > adr+i-1) {
				int t = levels[i];
				levels[i] = data[adr+i-1];
//				if(uni == 1) System.out.println(uni + "." + (adr+i-1) + " : " + t + " " + levels[i]);
				if(levels[i] != t) {
//					System.out.println(t + " " + levels[i]);
					c = true;
				} else {
//					System.out.println(t + " " + levels[i]);
				}
			}
		}
		if(lastUpdate + maxBetween < System.currentTimeMillis()) {
			c = true;
			updater.over = true;
		}
		if(c || heldChange) {
//			System.out.println("change " + device.getName());
//			NSACNMain.logMsg("change " + device.getName());
			lastUpdate = System.currentTimeMillis();
			if(updater.updt) {
				heldChange = true;
				return;
			}
			heldChange = false;
			try {
				updater.update(levels);
			} catch(Exception e) {
				e.printStackTrace();
			}
//			try {
//				for(int i = 0; i < panels.length; i++) {
//					device.setPanelColor(panels[i], levels[(i*3)+0], levels[(i*3)+1], levels[(i*3)+2], 0);
//				}
//			} catch(Exception e) {
//				System.out.println("thinged");
//				e.printStackTrace();
//			}
		}
	}
	@Override
	public JsonObj serialize() {
		JsonObj obj = new JsonObj();
		obj.setKey("device", device.getName());
		obj.setKey("absAdr", getAbsAdr());
		JsonObj arr = new JsonObj();
		obj.setKey("panels", arr);
		for(int i = 0; i < panels.length; i++) {
			arr.addArray(panels[i].getId());
		}
		return obj;
	}
	@Override
	public void deserialize(JsonObj obj) {
		if(obj.hasKey("absAdr")) {
			setAbsAdr(obj.getKey("absAdr").integer());
		}
		if(obj.hasKey("adr")) {
			setAdr(obj.getKey("adr").integer());
		}
		if(obj.hasKey("uni")) {
			setUni(obj.getKey("uni").integer());
		}
		if(obj.hasKey("panels")) {
			JsonObj[] arr = obj.getKey("panels").getArr();
			Panel[] arr2 = new Panel[panels.length];
			HashMap<Integer, Panel> m = new HashMap<Integer, Panel>();
			for(int i = 0; i < panels.length; i++) {
				m.put(panels[i].getId(), panels[i]);
			}
			for(int i = 0; i < arr.length; i++) {
				int id = arr[i].integer();
				if(m.containsKey(id) ) {
					arr2[i] = m.get(id);
					if(id != 0) m.remove(id);
				} else {
					System.out.println("Failed to find panel ID: " + id + ", using panel 0 insted");
					arr2[i] = m.get(0);
				}
			}
			int j = arr.length - 1;
			for(HashMap.Entry<Integer, Panel> ent : m.entrySet()) {
				arr2[j] = ent.getValue();
				j++;
			}
			panels = arr2;
		}
	}
	
	@Override
	public String toString() {
		String str = "";
		str += device.getName() + " | " + uni+"."+adr+ " | " + panels.length + " panels , "+ (levels.length) + " addresses used";
		return str;
	}
}
