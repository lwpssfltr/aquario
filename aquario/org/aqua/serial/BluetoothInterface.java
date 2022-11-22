package org.aqua.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import net.lpf.msgs.MessageQueue;

public class BluetoothInterface extends SerialInterface {
	private boolean scanFinished = false;
	private RemoteDevice hc05device;
	private String hc05Url;
	private StreamConnection streamConnection;
	private OutputStream os;
	private InputStream is;
	
		public BluetoothInterface(MessageQueue msg) {
			super(msg);
			init();
		}

		@Override
		protected String waterTemp() {
			String temp = "----";
			try {
				os.write(20);
				temp = "" + is.read();
			}
			catch (IOException ex) {
				Logger.getLogger(BluetoothInterface.class.getName()).log(Level.SEVERE, null, ex);
			}
			return temp;
		}

		@Override
		protected void lightOn() {
			try {
				os.write(30);
			}
			catch (IOException ex) {
				Logger.getLogger(BluetoothInterface.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		@Override
		protected void lightOff() {
			try {
				os.write(40);
			}
			catch (IOException ex) {
				Logger.getLogger(BluetoothInterface.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	
		@Override
		protected void feed() {
			try {
				os.write(50);
			}
			catch (IOException ex) {
				Logger.getLogger(BluetoothInterface.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	
		@Override
		protected String airTemp() {
			String temp = "----";
			try {	
				os.write(60);
				temp = "" + is.read();	
			}
			catch (IOException ex) {
				Logger.getLogger(BluetoothInterface.class.getName()).log(Level.SEVERE, null, ex);
			}
			return temp;
		}

		@Override
		protected String humidity() {
			String temp = "----";
			try {
				os.write(70);
				temp = "" + is.read();
			}
			catch (IOException ex) {
				Logger.getLogger(BluetoothInterface.class.getName()).log(Level.SEVERE, null, ex);
			}
			return temp;
		}

		@Override
		protected void pompOn() {
			try {
				os.write(70);
			}
			catch (IOException ex) {
				Logger.getLogger(BluetoothInterface.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		@Override
		protected void pompOff() {
		try {
			os.write(80);
		}
		catch (IOException ex) {
			Logger.getLogger(BluetoothInterface.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void init() {
		try {
			scanFinished = false;
			LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, new DiscoveryListener() {
				@Override
				public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
					try {
						String name = btDevice.getFriendlyName(false);
						System.out.format("%s (%s)\n", name, btDevice.getBluetoothAddress());
						if (name.matches("HC-05")) {
							hc05device = btDevice;
							System.out.println("got it!");
						}
					}
					catch (IOException ex) {
						Logger.getLogger(BluetoothInterface.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				@Override
				public void inquiryCompleted(int discType) {
					scanFinished = true;
				}
				@Override
				public void serviceSearchCompleted(int transID, int respCode) {
				}
				@Override
				public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
				}
			});
			while ( ! scanFinished) {
				Thread.sleep(500);
			}
			//search for services:
			UUID uuid = new UUID(0x1101); //scan for btspp://... services (as HC-05 offers it)
			UUID[] searchUuidSet = new UUID[] {uuid};
			int[] attrIDs = new int[] {
				0x0100 // service name
			};
			scanFinished = false;
			LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(attrIDs, searchUuidSet, hc05device, new DiscoveryListener() {
				@Override
				public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
				}
				@Override
				public void inquiryCompleted(int discType) {
				}
				@Override
				public void serviceSearchCompleted(int transID, int respCode) {
					scanFinished = true;
				}
				@Override
				public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
					for (ServiceRecord sr: servRecord) {
						hc05Url = sr.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
						if (hc05Url != null) {
							break; //take the first one
						}
					}
				}
			});
			while ( ! scanFinished) {
				Thread.sleep(500);
			}
			streamConnection = (StreamConnection) Connector.open(hc05Url);	
			os = streamConnection.openOutputStream();
			is = streamConnection.openInputStream();
		}
		catch (BluetoothStateException | InterruptedException ex) {
			Logger.getLogger(BluetoothInterface.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (IOException ex) {
			Logger.getLogger(BluetoothInterface.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	@Override
	public void quit() {
		try {	
			os.close();
		}
		catch (IOException | NullPointerException ex) {
		}
		try {
			is.close();
		}
		catch (IOException | NullPointerException ex) {
		}
		try {
			streamConnection.close();
		}
		catch (IOException | NullPointerException ex) {
		}
		super.quit();
	}
}
