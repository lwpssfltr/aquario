package org.aqua.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import net.lpf.msgs.MessageQueue;
import net.lpf.msgs.Msg;

public class SocketInterface extends Thread {
	private volatile boolean on = true;
	private Socket s;
	private final MessageQueue msg;
	private final String hostString;
	private static final int CLOUD_SOCKET = 50009;
	private static final int SOURCE_QUEUE = 1;
	private static final int SINK_QUEUE = 2;
	
	public SocketInterface(MessageQueue msg, String hostAddress) {
		this.msg = msg;
		this.hostString = hostAddress;
	}
	
	public void quit() {
		if (isAlive()) {
			on = false;
			try {
				s.close();
			}
			catch (IOException | NullPointerException ex) {
				System.out.println("SocketInterface: quitting");
			}
		}
	}

	@Override
	public void run() {
		if (on) {
			try {
				s = new Socket(InetAddress.getByName(hostString), CLOUD_SOCKET);
				DataInputStream sktin = new DataInputStream(s.getInputStream());
				DataOutputStream sktout = new DataOutputStream(s.getOutputStream());
				sktout.writeUTF("connect");
				sktout.flush();
				Thread.sleep(1000);
			if (sktin.available() == 0) {
				System.out.println("SocketInterface: auth error - no response for 'connect'\nSocketInterface: exiting");
				s.close();
				return;
			}	
			String ack = sktin.readUTF();
			if ( ! ack.equals("ack")) {
				System.out.println("SocketInterface:auth error - expected 'ack' but " + ack + " arrived\nSocketInterface: exiting");
				s.close();
				return;
			}
			while (on) {
				try {
					String query = sktin.readUTF();
					switch (query) {
						case "airtemp":
							msg.getAllMessages(SOURCE_QUEUE, "RTEMP");
							msg.sendMessage(SINK_QUEUE, "ATEMP", null);
							msg.waitForMessages(SOURCE_QUEUE, 15000);
							if (msg.hasMessage(SOURCE_QUEUE, "RATEMP")) {
								Msg[] m = msg.getAllMessages(SOURCE_QUEUE, "RATEMP");
								sktout.writeUTF((String) m[0].arg());
							}
							else {
								sktout.writeUTF("----");
							}
							break;
						case "watertemp":
							msg.getAllMessages(SOURCE_QUEUE, "RWTEMP");
							msg.sendMessage(SINK_QUEUE, "WTEMP", null);
							msg.waitForMessages(SOURCE_QUEUE, 15000);
							if (msg.hasMessage(SOURCE_QUEUE, "RWTEMP")) {
								Msg[] m = msg.getAllMessages(SOURCE_QUEUE, "RWTEMP");
								sktout.writeUTF((String) m[0].arg());
							}
							else {
								sktout.writeUTF("----");
							}
							break;
						case "humidity":
							msg.getAllMessages(SOURCE_QUEUE, "RHUM");
							msg.sendMessage(SINK_QUEUE, "HUM", null);
							msg.waitForMessages(SOURCE_QUEUE, 15000);
							if (msg.hasMessage(SOURCE_QUEUE, "RHUM")) {
								Msg[] m = msg.getAllMessages(SOURCE_QUEUE, "RHUM");
								sktout.writeUTF((String) m[0].arg());
							}
							else {
								sktout.writeUTF("----");
							}
							break;
						case "feed":
							msg.getAllMessages(SOURCE_QUEUE, "RFEED");
							msg.sendMessage(SINK_QUEUE, "FEED", null);
							msg.waitForMessages(SOURCE_QUEUE, 15000);
							if (msg.hasMessage(SOURCE_QUEUE, "RFEED")) {
								msg.getAllMessages(SOURCE_QUEUE, "RFEED");
							}
							break;
						case "lighton":
							msg.getAllMessages(SOURCE_QUEUE, "RLIGHT");
							msg.sendMessage(SINK_QUEUE, "LIGHT", "on");
							msg.waitForMessages(SOURCE_QUEUE, 15000);
							if (msg.hasMessage(SOURCE_QUEUE, "RLIGHT")) {
								msg.getAllMessages(SOURCE_QUEUE, "RLIGHT");
							}
							break;
						case "lightoff":
							msg.getAllMessages(SOURCE_QUEUE, "RLIGHT");
							msg.sendMessage(SINK_QUEUE, "LIGHT", "off");
							msg.waitForMessages(SOURCE_QUEUE, 15000);
							if (msg.hasMessage(SOURCE_QUEUE, "RLIGHT")) {
								msg.getAllMessages(SOURCE_QUEUE, "RLIGHT");
							}
							break;
						case "pompon":
							msg.getAllMessages(SOURCE_QUEUE, "RPOMP");
							msg.sendMessage(SINK_QUEUE, "POMP", "on");
							msg.waitForMessages(SOURCE_QUEUE, 15000);
							if (msg.hasMessage(SOURCE_QUEUE, "RPOMP")) {
								msg.getAllMessages(SOURCE_QUEUE, "RPOMP");
							}
							break;
						case "pompoff":
							msg.getAllMessages(SOURCE_QUEUE, "RPOMP");
							msg.sendMessage(SINK_QUEUE, "POMP", "off");
							msg.waitForMessages(SOURCE_QUEUE, 15000);
							if (msg.hasMessage(SOURCE_QUEUE, "RPOMP")) {
								msg.getAllMessages(SOURCE_QUEUE, "RPOMP");
							}
							break;
						case "ping":
							sktout.writeUTF("pong");
							break;
					}
					sktout.flush();
				}
				catch (IOException ex) {
					throw ex;
					}
				}
			}
			catch (IOException ex) {
				if (ex.getMessage().equals("Connection refused")) {
					System.out.println("SocketInterface: Cloud unavailable, quitting");
					quit();
				}
			}
			catch (InterruptedException ex) {
				System.out.println("ScoketInterface: interrupted signal arrived, exiting");
				quit();
			}
		}
	}
}
