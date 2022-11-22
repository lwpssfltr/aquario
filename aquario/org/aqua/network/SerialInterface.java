package org.aqua.serial;

import net.lpf.msgs.MessageQueue;
import net.lpf.msgs.Msg;

public abstract class SerialInterface extends Thread {
	private static final int SINK_QUEUE = 1;
	private static final int SOURCE_QUEUE = 2;
	private boolean on = true;
	private final MessageQueue msg;

	public SerialInterface(MessageQueue msg) {
		this.msg = msg;
	}

	protected abstract String waterTemp();
	protected abstract String airTemp();
	protected abstract String humidity();
	protected abstract void lightOn();
	protected abstract void lightOff();
	protected abstract void pompOn();
	protected abstract void pompOff();
	protected abstract void feed();
	
	@Override
	public void run() {
		while (on) {
			try {
				msg.waitForMessages(SOURCE_QUEUE);
				if (msg.hasMessage(SOURCE_QUEUE, "ATEMP")) {
					msg.getAllMessages(SOURCE_QUEUE, "ATEMP");
					msg.sendMessage(SINK_QUEUE, "RATEMP", airTemp());
				}
				else if (msg.hasMessage(SOURCE_QUEUE, "WTEMP")) {
					msg.getAllMessages(SOURCE_QUEUE, "WTEMP");
					msg.sendMessage(SINK_QUEUE, "RWTEMP", waterTemp());
				}
				else if (msg.hasMessage(SOURCE_QUEUE, "HUM")) {
					msg.getAllMessages(SOURCE_QUEUE, "HUM");
					msg.sendMessage(SINK_QUEUE, "RHUM", humidity());
				}
				else if (msg.hasMessage(SOURCE_QUEUE, "LIGHT")) {
					Msg light = msg.getAllMessages(SOURCE_QUEUE, "LIGHT")[0];
					if (light.arg().equals("on")) {
						lightOn();
						msg.sendMessage(SINK_QUEUE, "RLIGHT", null);
					}
					else if (light.arg().equals("off")) {
						lightOff();
						msg.sendMessage(SINK_QUEUE, "RLIGHT", null);
					}
				}
				else if (msg.hasMessage(SOURCE_QUEUE, "POMP")) {
					Msg light = msg.getAllMessages(SOURCE_QUEUE, "POMP")[0];
					if (light.arg().equals("on")) {
						pompOn();
						msg.sendMessage(SINK_QUEUE, "RPOMP", null);
					}
					else if (light.arg().equals("off")) {
						pompOff();
						msg.sendMessage(SINK_QUEUE, "RPOMP", null);
					}
				}
				else if (msg.hasMessage(SOURCE_QUEUE, "FEED")) {
					msg.getAllMessages(SOURCE_QUEUE, "FEED");
					feed();
					msg.sendMessage(SINK_QUEUE, "RFEED", null);
				}
			}
			catch (InterruptedException ex) {
				System.out.println("SerialInterface: interrupted signal arrived, exiting");
				on = false;
			}
		}
	}

	public void quit() {
		on = false;
		interrupt();
	}	
}
