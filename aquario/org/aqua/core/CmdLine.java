package org.aqua.core;

import java.util.Scanner;
import net.lpf.msgs.MessageQueue;

public class CmdLine extends Thread {
	private String[] command;
	private final Scanner in;
	private boolean on = true;
	private static final int MESSAGE_QUEUE = 0;
	private final MessageQueue msg;

	public CmdLine(MessageQueue msg) {
		in = new Scanner(System.in);
		this.msg = msg;
	}

	@Override
	public void run() {
		while (on) {
			command = in.nextLine().split(" ");
			switch (command[0]) {
				case "sethost": {
					if (command.length < 2) {
						System.out.println("no new host address specified\nusage: 'sethost <hostname>'");
					}
					else {
						msg.sendMessage(MESSAGE_QUEUE, "NEWHOST", command[1]);
					}
					break;
				}
				case "connect": {
					if (command.length < 2) {
						System.out.println("no target specified\nusage: 'connect <cloud | mc>'");
						break;
					}
					if (command[1].equals("mc")) {
						msg.sendMessage(MESSAGE_QUEUE, "CONN", "mc");
					}
					else if (command[1].equals("cloud")) {
						msg.sendMessage(MESSAGE_QUEUE, "CONN", "cloud");
					}
					break;
				}
				case "disconnect": {
					if (command.length < 2) {
						System.out.println("no target specified\nusage: 'disconnect <cloud | mc>'");
						break;
					}
					if (command[1].equals("mc")) {
						msg.sendMessage(MESSAGE_QUEUE, "DISC", "mc");
					}
					else if (command[1].equals("cloud")) {
						msg.sendMessage(MESSAGE_QUEUE, "DISC", "cloud");
					}
					break;
				}		
				case "quit": {
					System.out.println("CmdLine: shutting down modules...");
					msg.sendMessage(MESSAGE_QUEUE, "QUIT", null);
					on = false;
					System.out.println("CmdLine: quitting");
					break;
				}
				case "help": {
					System.out.println("sethost connect disconnect quit");
					break;
				}
			}
		}
	}	
}
