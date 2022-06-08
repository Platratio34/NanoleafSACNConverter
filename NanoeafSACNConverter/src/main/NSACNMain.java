package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import dataManagment.JsonObj;
import io.github.rowak.nanoleafapi.Aurora;
import io.github.rowak.nanoleafapi.Canvas;
import io.github.rowak.nanoleafapi.NanoleafDevice;
import io.github.rowak.nanoleafapi.Panel;
import io.github.rowak.nanoleafapi.Shapes;
import io.github.rowak.nanoleafapi.util.NanoleafDeviceMeta;
import io.github.rowak.nanoleafapi.util.NanoleafSetup;
import peterGames.GameController;
import peterGames.objects.GameButton;
import peterGames.objects.GameText;
import peterGames.util.Config;
import sacnReciver.Reciver;
import sacnReciver.ReciverRunner;
import threading.JobPool;

@SuppressWarnings("unused")
public class NSACNMain {
	
	public static int timeout = 2000;
	public static ArrayList<DMXDevice> dmxDevices;
	public static HashMap<String, NanoleafDevice> nDevices;
	
	public static boolean guiEnb;
	public static GameController gC;
	public static Config cfg;
	
	private static GameText cText;
	private static ArrayList<String> log;
	private static ArrayList<String> log2;
	
	private static Reciver reciver;
	
	public static Scanner scan = new Scanner(System.in);
	
	private static boolean testPattern = false;
	
	public static JobPool jobPool;
	private volatile static boolean running = true;
	
	private static final String LOG_FILE = "nanoleaf_sACN.log";

	public static void main(String[] args) {
		log2 = new ArrayList<String>();
		jobPool = new JobPool(8);
		dmxDevices = new ArrayList<DMXDevice>();
		nDevices = new HashMap<String, NanoleafDevice>();
		log = new ArrayList<String>();
		reciver = new Reciver();
		if(args.length > 0) {
			if(args[0].equals("gui")) {
				guiEnb = true;
				gC = new GameController();
				cfg = gC.getconfig();
				GameButton addButton = new GameButton(gC, "New Device", 80, 20) {
					protected void onPressed(boolean pressed) {
						NanoleafDevice d = getNewDevice();
						if(d == null) return;
						if(nDevices.containsValue(d)) return;
						nDevices.put(d.getName(), d);
						saveDevices();
					}
				};
				gC.addObject(addButton);
				GameButton onButton = new GameButton(gC, "All On", 80, 20) {
					protected void onPressed(boolean pressed) {
						for(HashMap.Entry<String, NanoleafDevice> ent : nDevices.entrySet()) {
							NanoleafDevice d = ent.getValue();
							try {
								d.setBrightness(100);
								
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
				onButton.move(0,40);
				gC.addObject(onButton);
				GameButton offButton = new GameButton(gC, "All Off", 80, 20) {
					protected void onPressed(boolean pressed) {
						for(HashMap.Entry<String, NanoleafDevice> ent : nDevices.entrySet()) {
							NanoleafDevice d = ent.getValue();
							try {
								d.setBrightness(0);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
				offButton.move(0,60);
				gC.addObject(offButton);

				GameButton brigButton = new GameButton(gC, "+ 10%", 80, 20) {
					protected void onPressed(boolean pressed) {
						for(HashMap.Entry<String, NanoleafDevice> ent : nDevices.entrySet()) {
							NanoleafDevice d = ent.getValue();
							try {
								d.increaseBrightness(10);
								
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
				brigButton.move(0,80);
				gC.addObject(brigButton);
				
				GameButton redButton = new GameButton(gC, "All Red", 80, 20) {
					protected void onPressed(boolean pressed) {
						for(HashMap.Entry<String, NanoleafDevice> ent : nDevices.entrySet()) {
							NanoleafDevice d = ent.getValue();
							Thread t1 = new Thread(new Runnable() {
							    @Override
							    public void run() {
							        // code goes here.
							    	try {
//							    		logMsg("thing");
//							    		d.enableExternalStreaming();
										List<Panel> ps = d.getPanels();
										for(Panel p : ps) {
											if(p.getId() != 0) {
												d.setPanelColor(p, 255, 0, 0, 0);
//												d.setPanelExternalStreaming(p, 255, 0, 0, 0);
											}
										}
							    	} catch (Exception e) {
										e.printStackTrace();
									}
							    }
							});  
							t1.start();
//							try {
////								d.enableExternalStreaming();
//								List<Panel> ps = d.getPanels();
////								StaticEffect.Builder eff = new StaticEffect.Builder(ps);
//								for(Panel p : ps) {
//									if(p.getId() != 0) {
//										d.setPanelColor(p, 255, 0, 0, 0);
////										d.setPanelColorAsync(p, 255, 0, 0, 0, (int code, String data, NanoleafDevice d2) -> {
////											if(code != NanoleafCallback.SUCCESS) logMsg("code="+code+"; data=\""+data+"\"");
////											logMsg("Thingy " + d.getName() + " : " + p.getId() + " ; " + data);
////										});
//									
////										eff.setPanel(p, new Frame(255, 0, 0));
////										d.setPanelExternalStreaming(p, "FF0000", 1);
//									}
//								
//								}
////								StaticEffect ef = eff.build("sACN");
//								
////								System.out.println(ef.toJSON("display"));
////								d.displayEffect(eff.build("sACN"));
//								
//								//{"loop":false,"animData":"3 65520 1 255 0 0 0 -1 54178 1 255 0 0 0 -1 49973 1 255 0 0 0 -1","palette":[],"version":"2.0","animName":"sACN","animType":"static","colorType":"HSB"}
////								d.sendStaticEffectExternalStreaming(ef);
////								d.sendAnimData("3 65520 1 255 0 0 0 -1 54178 1 255 0 0 0 -1 49973 1 255 0 0 0 -1");
////								d.writeEffectAsync(ef.toJSON().toString(), (int code, String data, NanoleafDevice d2) -> {
////									if(code != NanoleafCallback.SUCCESS) logMsg("code="+code+"; data=\""+data+"\"");
////								});
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
						}
					}
				};
				redButton.move(120,0);
				gC.addObject(redButton);
				GameButton greenButton = new GameButton(gC, "All Green", 80, 20) {
					protected void onPressed(boolean pressed) {
						for(HashMap.Entry<String, NanoleafDevice> ent : nDevices.entrySet()) {
							NanoleafDevice d = ent.getValue();
							Thread t1 = new Thread(new Runnable() {
							    @Override
							    public void run() {
							        // code goes here.
							    	try {
										List<Panel> ps = d.getPanels();
										for(Panel p : ps) {
											if(p.getId() != 0) {
												d.setPanelColor(p, 0, 255, 0, 0);
											}
										}
							    	} catch (Exception e) {
										e.printStackTrace();
									}
							    }
							});  
							t1.start();
						}
					}
				};
				greenButton.move(120,20);
				gC.addObject(greenButton);
				GameButton blueButton = new GameButton(gC, "All Blue", 80, 20) {
					protected void onPressed(boolean pressed) {
						for(HashMap.Entry<String, NanoleafDevice> ent : nDevices.entrySet()) {
							NanoleafDevice d = ent.getValue();
							Thread t1 = new Thread(new Runnable() {
							    @Override
							    public void run() {
							        // code goes here.
							    	try {
										List<Panel> ps = d.getPanels();
										for(Panel p : ps) {
											if(p.getId() != 0) {
												d.setPanelColor(p, 0, 0, 255, 0);
											}
										}
							    	} catch (Exception e) {
										e.printStackTrace();
									}
							    }
							});  
							t1.start();
						}
					}
				};
				blueButton.move(120,40);
				gC.addObject(blueButton);
				
				GameButton paternButton = new GameButton(gC, "Start", 80, 20) {
					protected void onPressed(boolean pressed) {
				    	testPattern = true;
						for(HashMap.Entry<String, NanoleafDevice> ent : nDevices.entrySet()) {
							NanoleafDevice d = ent.getValue();
							Thread t1 = new Thread(new Runnable() {
							    @Override
							    public void run() {
							    	int a = 0;
							    	while(testPattern) {
								    	try {
											List<Panel> ps = d.getPanels();
											for(Panel p : ps) {
												if(p.getId() != 0) {
													if(a==0) d.setPanelColor(p, 255, 0, 0, 0);
													if(a==1) d.setPanelColor(p, 255, 255, 0, 0);
													if(a==2) d.setPanelColor(p, 0, 255, 0, 0);
													if(a==3) d.setPanelColor(p, 0, 255, 255, 0);
													if(a==4) d.setPanelColor(p, 0, 0, 255, 0);
													if(a==5) d.setPanelColor(p, 255, 0, 255, 0);
												}
											}
								    	} catch (Exception e) {
											e.printStackTrace();
								    	}
								    	a++;
								    	a%=6;
							    	}
							    }
							});  
							t1.start();
						}
					}
				};
				paternButton.move(120,80);
				gC.addObject(paternButton);
				GameButton paternButtonStop = new GameButton(gC, "Stop", 80, 20) {
					protected void onPressed(boolean pressed) {
						testPattern = false;
					}
				};
				paternButtonStop.move(120,100);
				gC.addObject(paternButtonStop);
				
				GameButton saveButton = new GameButton(gC, "Save", 80, 20) {
					protected void onPressed(boolean pressed) {
						saveDevices();
					}
				};
				saveButton.move(320,0);
				gC.addObject(saveButton);
				GameButton loadButton = new GameButton(gC, "Load", 80, 20) {
					protected void onPressed(boolean pressed) {
						loadDevices();
					}
				};
				loadButton.move(320,20);
				gC.addObject(loadButton);
				
				GameButton listButton = new GameButton(gC, "List", 80, 20) {
					protected void onPressed(boolean pressed) {
						for(HashMap.Entry<String, NanoleafDevice> ent : nDevices.entrySet()) {
							try {
								List<Panel> ps = ent.getValue().getPanels();
								for(int j = 0; j < ps.size(); j++) {
									System.out.println(ps.get(j));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
				listButton.move(420,0);
				gC.addObject(listButton);
				
				GameButton streamButton = new GameButton(gC, "Streaming P", 80, 20) {
					protected void onPressed(boolean pressed) {
						for(NanoleafDevice dv : nDevices.values()) {
							try {
								dv.enableExternalStreaming();
								List<Panel> ps = dv.getPanels();
								for(int j = 0; j < ps.size(); j++) {
									dv.setPanelExternalStreaming(ps.get(j), 128, 0, 128, 0);
//									System.out.println(ps.get(j));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
//							System.out.println("thing");
						}
					}
				};
				streamButton.move(420,30);
				gC.addObject(streamButton);
				GameButton streamButtonY = new GameButton(gC, "Streaming Y", 80, 20) {
					protected void onPressed(boolean pressed) {
						for(NanoleafDevice dv : nDevices.values()) {
							try {
								dv.enableExternalStreaming();
								List<Panel> ps = dv.getPanels();
								for(int j = 0; j < ps.size(); j++) {
									dv.setPanelExternalStreaming(ps.get(j), 128, 128, 0, 0);
									System.out.println(ps.get(j));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							System.out.println("thing");
						}
					}
				};
				streamButtonY.move(420,50);
				gC.addObject(streamButtonY);
				
				
				cText = new GameText(gC, "");
				logMsg("Started");
				cText.move(10,150);
				gC.addObject(cText);
				
				gC.getFrame().addWindowListener(new java.awt.event.WindowAdapter() {
				    @Override
				    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				    	logMsg("Stoping . . ");
						running = false;
						jobPool.stop();
						reciver.stop();
						try {
							FileWriter fWrite = new FileWriter(LOG_FILE);
							for(int i = 0; i < log2.size(); i++) {
								fWrite.write(log2.get(i) + "\n");
							}
							fWrite.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
				    }
				});
				
				gC.run();
			}
		}
		loadDevices();
		ReciverRunner.TIMEOUT = 1000 * 1;

		Thread runThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(running) {
					updateDevices();
				}
				logMsg("Run thread stopped");
			}
		});
		runThread.start();
		if(!guiEnb) {
			while(true) {
				System.out.print("> ");
				String in = scan.nextLine();
				String[] cmd = in.split(" ");
				if(cmd.length == 0) {
					System.out.println("Must provide a command");
					continue;
				}
				if(cmd[0].equals("stop")) {
					logMsg("Stoping . . ");
					running = false;
					jobPool.stop();
					reciver.stop();
					try {
						FileWriter fWrite = new FileWriter(LOG_FILE);
						for(int i = 0; i < log2.size(); i++) {
							fWrite.write(log2.get(i) + "\n");
						}
						fWrite.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
				if(cmd[0].equals("device")) {
					if(cmd.length == 2) {
						if(cmd[1].equals("new")) {
							NanoleafDevice d = getNewDevice();
							if(d == null) {
								System.out.println("New device was null");
								continue;
							}
							if(nDevices.containsValue(d)) continue;
							nDevices.put(d.getName(), d);
							saveDevices();
							continue;
						}
						if(cmd[1].equals("save")) {
							saveDevices();
							continue;
						}
						if(cmd[1].equals("load")) {
							loadDevices();
							continue;
						}
						if(cmd[1].equals("list")) {
							for(HashMap.Entry<String, NanoleafDevice> ent : nDevices.entrySet()) {
								try {
									NanoleafDevice dv =  ent.getValue();
									System.out.println(dv.getName() + " | " + dv.getHostname()+":"+dv.getPort() + " | " + dv.getModel());
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							continue;
						}
					}
				}
				if(cmd[0].equals("dmx")) {
					if(cmd.length == 4) {
						if(cmd[1].equals("add")) {
							String dv = cmd[2];
							int adr = Integer.parseInt(cmd[3]);
							if(nDevices.containsKey(dv)) {
								dmxDevices.add(new DMXDevice(adr , nDevices.get(dv)));
								System.out.println("Added DMX patch for " + dv);
								saveDevices();
							} else {
								System.out.println("Failed to add DMX patch for " + dv + ". Device not found");
							}
							continue;
						}
					}
					if(cmd.length == 2) {
						if(cmd[1].equals("list")) {
							for(DMXDevice dv : dmxDevices) {
								System.out.println(dv.toString());
							}
						}
					}
				}
				if(cmd[0].equals("help")) {
					System.out.println("-- Displaying help for NanoleafSACNConverter --");
					System.out.println("= Commands =");
					System.out.println("device new : Runs the new device wizard");
					System.out.println("device save : Saves devices to JSON");
					System.out.println("device load : Loads devices from JSON");
					System.out.println("device list : Lists all Nanoleaf deivces");
					System.out.println("dmx add [device name] [absolute dmx address] : Adds a new DMX device and saves");
					System.out.println("dmx list : Lists all DMX devices");
					System.out.println("help : displays this list");
					continue;
				}
				System.out.println("Unknown command, use command help for a list of commands");
			}
			
		}
	}
	
	public static NanoleafDevice getNewDevice() {
		try {
			logMsg("Starting find");
			List<NanoleafDeviceMeta> devices = NanoleafSetup.findNanoleafDevices(timeout);
			if(devices.size() == 0) {
				logMsg("No Devices Found");
//				scan.close();
				return null;
			}
			logMsg("Devices Found");
			for(NanoleafDeviceMeta d : devices) {
				logMsg(d.getDeviceId() + " " + d.getHostName() + " " + d.getDeviceName());
			}
//			logMsg(devices+"\n");
			NanoleafDeviceMeta d = null;
			while(d == null) {
				System.out.print("Device Name\n>");
				String name = scan.nextLine();
				if(name.equals("exit")) {
					return null;
				}
				for(int i = 0; i < devices.size(); i++) {
					logMsg(devices.get(i).getDeviceName());
					if(devices.get(i).getDeviceName().equals(name)) {
						d = devices.get(i);
					}
				}
				if(d == null) {
					logMsg("Select a diffrent device");
				}
			}
			logMsg("Selected Device: " + d);
			logMsg("Push power button for 5-7 seconds");
			logMsg("Enter somthing to get token");
			scan.nextLine();
			String accessToken = NanoleafSetup.createAccessToken(d.getHostName(), d.getPort());
			logMsg("Access Token: \"" + accessToken + "\"");
			NanoleafDevice shape = new Shapes(d.getHostName(), d.getPort(), accessToken);
			return shape;
		} catch (Exception e) {
			e.printStackTrace();
		}
//		scan.close();
		return null;
	}
	
	public static void updateDevices() {
		if(reciver.update()) {
//			logMsg("aa");
//			System.out.println(reciver.getDmx(1,400));
			for(int i = 0; i < dmxDevices.size(); i++) {
//				System.out.println("device");
				DMXDevice d = dmxDevices.get(i);
				d.update(reciver.getDmx(d.getUni()));
//				System.out.println(""+reciver.getDmx(d.getUni(), 400));
//				System.out.print("");
//				System.out.println(reciver.printDmx(d.getUni()));
			}
		}
	}
	
	public static void saveDevices() {
		JsonObj obj = new JsonObj();
		JsonObj nDO = new JsonObj();
		obj.setKey("nDevices", nDO);
		for(HashMap.Entry<String, NanoleafDevice> ent : nDevices.entrySet()) {
			JsonObj jd = new JsonObj();
			nDO.addArray(jd);
			NanoleafDevice d = ent.getValue();
			jd.setKey("name", d.getName());
			jd.setKey("hostname", d.getHostname());
			jd.setKey("port", d.getPort());
			jd.setKey("accessToken", d.getAccessToken());
			jd.setKey("type", d.getShapeType().getValue());
		}

		JsonObj dDO = new JsonObj();
		obj.setKey("dmxDevices", dDO);
		for(int i = 0; i < dmxDevices.size(); i++) {
			DMXDevice d = dmxDevices.get(i);
			dDO.addArray(d);
		}
		File f = new File("save.json");

		try {
			PrintStream stream = new PrintStream(f);
			stream.print(obj);
			stream.close();
			logMsg("---Devices Saved---");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void loadDevices() {
		JsonObj obj = JsonObj.parseP("save.json");
		if(obj == null) {
			logMsg("Faild to load JSON");
			return;
		}
		logMsg("Starting Load");
		if(obj.hasKey("nDevices")) {
			nDevices = new HashMap<String, NanoleafDevice>();
			JsonObj[] nDO = obj.getKey("nDevices").getArr();
			for(int i = 0; i < nDO.length; i++) {
				int type = nDO[i].getKey("type").integer();
				String hostname = nDO[i].getKey("hostname").string();
				int port = nDO[i].getKey("port").integer();
				String accessToken = nDO[i].getKey("accessToken").string();
				NanoleafDevice d = null;
				try {
					type = 7;
					if(type >= 7) {
						d = new Shapes(hostname, port, accessToken);
					} else if(type >= 2) {
						d = new Canvas(hostname, port, accessToken);
					} else if(type >= 0) {
						d = new Aurora(hostname, port, accessToken);
					} else {
						throw new Exception("Invalid Shape Type: " + type);
					}
					d.enableExternalStreaming();
					nDevices.put(d.getName(), d);
					logMsg("Loaded device " + d.getName());
				} catch(Exception e) {
					logMsg("Faild to load " + nDO[i].getKey("name").string() + ". Trying search");
					e.printStackTrace();
//					System.out.println(nDO[i]);
					try {
						List<NanoleafDeviceMeta> devices = NanoleafSetup.findNanoleafDevices(timeout);
						if(devices.size() > 0) {
							for(NanoleafDeviceMeta dv : devices) {
								System.out.println(dv);
								if(dv.getDeviceName().equals(nDO[i].getKey("name").string())) {
									type = 7;
									if(type >= 7) {
										d = new Shapes(dv.getHostName(), dv.getPort(), accessToken);
									} else if(type >= 2) {
										d = new Canvas(dv.getHostName(), dv.getPort(), accessToken);
									} else if(type >= 0) {
										d = new Aurora(dv.getHostName(), dv.getPort(), accessToken);
									} else {
										throw new Exception("Invalid Shape Type: " + type);
									}
									d.enableExternalStreaming();
									nDevices.put(d.getName(), d);
									logMsg("Loaded device " + d.getName());
									break;
								}
							}
						}
						if(d == null) logMsg("Faild to load " + nDO[i].getKey("name").string() + ". Attempt 2. Device not found");
					} catch(Exception e1) {
						logMsg("Faild to load " + nDO[i].getKey("name").string() + ". Attempt 2. Error");
						e1.printStackTrace();
					}
				}
			}
			logMsg("Loaded devices");
		}
		if(obj.hasKey("dmxDevices")) {
			dmxDevices = new ArrayList<DMXDevice>();
			JsonObj[] dDO = obj.getKey("dmxDevices").getArr();
//			logMsg("loading " + dDO.length + " DMX device(s)");
			for(int i = 0; i < dDO.length; i++) {
				String device = dDO[i].getKey("device").string();
				if(nDevices.containsKey(device)) {
					dmxDevices.add(new DMXDevice(dDO[i], nDevices.get(device)));
					logMsg("Loaded DMX patch for " + device);
				} else {
					logMsg("Failed to load DMX patch for " + device + ". Device not found");
				}
			}
			logMsg("Loaded DMX patch\n");
		}
	}
	
	public static void logMsg(String msg) {
		log2.add(msg);
		System.out.println(msg);
		if(guiEnb) {
			log.add(msg);
			String text = "";
			int lS = log.size() - 1;
			for(int i = 0; i < 10 && i <= lS; i++) {
				if(text != "") text = "\n" + text;
				text = log.get(lS - i) + text;
			}
			cText.setText(text);
			gC.repaint();
		}
	}

}
