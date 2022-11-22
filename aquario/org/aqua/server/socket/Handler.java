package org.aqua.server.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Handler implements Runnable {
	private final Socket socket;
	private DataInputStream sktin;
	private DataOutputStream sktout;
	public Handler(Socket socket) {
		this.socket = socket;
		try {
			this.sktin = new DataInputStream(socket.getInputStream());
			this.sktout = new DataOutputStream(socket.getOutputStream());
		}	
		catch (IOException ex) {
			Logger.getLogger(ClientService.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public boolean isAlive() {
		try {
			sktout.writeUTF("ping");
			Thread.sleep(3500);
			if(sktin.available() > 0){
				if (sktin.readUTF().equals("pong")){
					return true;
				}
			}
			return false;
		}
		catch (IOException | InterruptedException ex) {
			return false;
		}
	}

	public void sendUTF(String s) {
		try {
			sktout.writeUTF(s);
			sktout.flush();
		}
		catch (IOException ex) {
			try {
				socket.close();
			}
				catch (IOException ex1) {
			}
		}
	}
	
	@Override
	public void run() {
		if (sktin != null && sktout != null) {
			try {
				sktin.readUTF();
				sktout.writeUTF("ack");
				sktout.flush();
			}
			catch (IOException ex) {
				try {
					socket.close();
				}
				catch (IOException ex1) {
				}
			}
		}
	}

	public String getUTF() {
		String s = "";
		try {
			s = sktin.readUTF();
		}	
		catch (IOException ex) {}
		return s;
	}
}
