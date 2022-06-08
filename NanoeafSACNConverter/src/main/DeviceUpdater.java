package main;

import java.util.concurrent.LinkedBlockingDeque;

import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.Panel;
import threading.Job;
import threading.Job.JobRunnable;

public class DeviceUpdater implements JobRunnable {
	
	public boolean updt = false;
	private NanoleafDevice device;
	private Panel[] panels;
	private int[] levels;
	private int[] setLevels;
	private LinkedBlockingDeque<Exception> errors;
	public boolean over;
	private long lastStart;
	
	public DeviceUpdater(NanoleafDevice device, Panel[] panels) {
		this.device = device;
		this.panels = panels;
		levels = new int[panels.length * 3];
		setLevels = new int[panels.length * 3];
		errors = new LinkedBlockingDeque<Exception>();
	}
	
	public void update(int[] levels) {
//		System.out.println("Trying " + device.getName());
		if(lastStart == 0) {
			lastStart = System.currentTimeMillis();
		}
		if(System.currentTimeMillis()-lastStart > 1000*60) {
			System.out.println("Long time scince update: " + device.getName());
		}
		this.levels = levels;
		if(setLevels.length != levels.length) {
			int[] t = new int[levels.length];
			for(int i = 0; i< t.length && i < setLevels.length; i++) {
				t[i] = setLevels[i];
			}
			setLevels = t;
		}
//		try {
//			Exception e = errors.poll(1, TimeUnit.MILLISECONDS);
//			if(e != null) {
//				e.printStackTrace();
//			}
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
		if(!updt) {
//			Thread thread = new Thread(this);
//			thread.start();
			NSACNMain.jobPool.newJob(this);
		}
	}
	
	@Override
    public void run(Job job) {
		lastStart = System.currentTimeMillis();
//		System.out.println("Thing 1 " + device.getName());
    	updt = true;
    	try {
//    		device.enableExternalStreaming();
    		for(int i = 0; i < panels.length; i++) {
    			if(!over && setLevels[(i*3)+0] == levels[(i*3)+0] && setLevels[(i*3)+1] == levels[(i*3)+1] && setLevels[(i*3)+2] == levels[(i*3)+2]) {
    				continue;
    			}
    			setLevels[(i*3)+0] = levels[(i*3)+0];
    			setLevels[(i*3)+1] = levels[(i*3)+1];
    			setLevels[(i*3)+2] = levels[(i*3)+2];
    			device.setPanelColor(panels[i], levels[(i*3)+0], levels[(i*3)+1], levels[(i*3)+2], 1);
//    			device.setPanelExternalStreaming(panels[i], levels[(i*3)+0], levels[(i*3)+1], levels[(i*3)+2], 0);
//				device.setPanelColorAsync(panels[i], levels[(i*3)+0], levels[(i*3)+1], levels[(i*3)+2], 0, (int s, String msg, NanoleafDevice d) -> {
//					if(s!=1) System.out.println(s + " " + msg);
//				});
			}
    		over = false;
    	} catch (Exception e) {
//    		System.out.println("error");
//			System.out.println(e);
    		e.printStackTrace();
			errors.add(e);
		}
//    	try {
//			StaticEffect.Builder eff = new StaticEffect.Builder(device);
//			for(int i = 0; i < panels.length; i++) {
//				eff.setPanel(panels[i], new Frame(levels[(i*3)+1], levels[(i*3)+2], levels[(i*3)+3], 0));
//			}
//			device.displayEffect(eff.build("sACN"));
//			device.displayEffectFor(eff.build("sACN"), 10);/*, (int code, String dt, NanoleafDevice d) -> {
//				if(code != NanoleafCallback.SUCCESS) System.out.println("code:"+code+"; data:\""+dt+"\"");
//			});*/
//		} catch (Exception e) {
//			if(e instanceof NanoleafException) {
//				NanoleafException nE = (NanoleafException)e;
//				if(nE.getCode() == 400) {
//					updt = false;
//					return;
//				}
//			}
//			e.printStackTrace();
//		}
    	updt = false;
//		System.out.println("Thing 2 " + device.getName());
    }
}
