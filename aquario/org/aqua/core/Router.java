package org.aqua.core;

import org.aqua.network.SocketInterface;
import net.lpf.msgs.MessageQueue;
import net.lpf.msgs.Msg;
import org.aqua.serial.BluetoothInterface;
import org.aqua.serial.SerialInterface;
//import org.aqua.serial.Stub;

public class Router extends Thread {
	private static final int MESSAGE_QUEUE = 0;
	private SocketInterface sc;
	private SerialInterface br;
	private boolean on = true;
	private final MessageQueue msg;
	private String hostAddress;

	public Router(MessageQueue msg, String hostAddress) {
		this.msg = msg;
		this.hostAddress = hostAddress;
	}

	@Override
	public void run() {
		while (on) {
			try {
				msg.waitForMessages(MESSAGE_QUEUE);
				if (msg.hasMessage(MESSAGE_QUEUE, "CONN")) {
					Msg[] m = msg.getAllMessages(MESSAGE_QUEUE, "CONN");
					if (m[0].arg().equals("mc")) {
						System.out.println("Router: installing McInterface");
						if (br == null || ( ! br.isAlive() && br != null)) {
							 br = new BluetoothInterface(msg);
							//br = new Stub(msg);
							br.start();
						}
					}
					else if (m[0].arg().equals("cloud")) {
						System.out.println("Router: installing SocketInterface");
						if (sc == null || ( ! sc.isAlive() && sc != null)) {
							sc = new SocketInterface(msg, hostAddress);
							sc.start();
						}
					}
				}
				else if (msg.hasMessage(MESSAGE_QUEUE, "DISC")) {
					Msg[] m = msg.getAllMessages(MESSAGE_QUEUE, "DISC");
					if (m[0].arg().equals("mc")) {
						System.out.println("Router: shutting down McInterface");
						shutDownMc();
					}
					else if (m[0].arg().equals("cloud")) {
						System.out.println("Router: shutting down SocketInterface");
						shutDownCloud();
					}
				}
				else if (msg.hasMessage(MESSAGE_QUEUE, "QUIT")) {
					quit();
				}
				else if (msg.hasMessage(MESSAGE_QUEUE, "NEWHOST")) {
					Msg[] m = msg.getAllMessages(MESSAGE_QUEUE, "NEWHOST");
					System.out.println("Router: applying new hostname");
					this.hostAddress = (String) m[0].arg();
					msg.sendMessage(MESSAGE_QUEUE, "RECONN", null);
				}
				else if (msg.hasMessage(MESSAGE_QUEUE, "RECONN")) {
					msg.getAllMessages(MESSAGE_QUEUE, "RECONN");
					shutDownCloud();
					if (sc == null || ( ! sc.isAlive() && sc != null)) {
						sc = new SocketInterface(msg, hostAddress);
					}
				}
			}
			catch (InterruptedException ex) {
				System.out.println("Router: interrupted signal arrived, exiting");
				on = false;
			}
		}
	}

	private void quit() {
		on = false;
		shutDownMc();
		shutDownCloud();
		System.out.println("Router: quitting");
		interrupt();
	}

	private void shutDownMc() {
		if (br == null) {
			System.out.println("Router: McInterface already down");
		}
		else if (br.isAlive()) {
			br.quit();
		}
	}

	private void shutDownCloud() {
		if (sc == null) {
			System.out.println("Router: SocketInterface already down");
		}
		else if (sc.isAlive()) {
			sc.quit();
		}
	}
}
