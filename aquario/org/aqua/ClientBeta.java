package org.aqua;

import java.util.Scanner;
import net.lpf.msgs.MessageQueue;
import org.aqua.core.CmdLine;
import org.aqua.core.Router;

public class ClientBeta {
	private CmdLine cmd;
	private MessageQueue msg;
	private Router r;
	public ClientBeta(String host) throws InterruptedException {
		init(host);
	}
	public static void main(String[] args) throws InterruptedException {
		String host;
		if (args.length == 0) {
			System.out.println("Type in the hostname");
			host = new Scanner(System.in).nextLine();
		}
		else {
			host = args[0];
		}
			new ClientBeta(host);
		}
	private void init(String host) throws InterruptedException {
		msg = new MessageQueue(3);
		r = new Router(msg, host);
		r.start();
		cmd = new CmdLine(msg);
		cmd.start();
		msg.waitForMessage(0, "QUIT");
	}
}
