package org.aqua.server.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientService extends Thread {
	private static final int CLOUD_SOCKET = 50009;
	private ServerSocket server;
	private final ExecutorService pool;
	private Handler curHandler;
	
	public ClientService() {
		try {
			server = new ServerSocket(CLOUD_SOCKET);
		}
		catch (IOException ex) {
			Logger.getLogger(ClientService.class.getName()).log(Level.SEVERE, null, ex);
		}
		pool = Executors.newFixedThreadPool(1);
		}
	@Override
	public void run() {
		try {
			while (true) {
				pool.execute(curHandler = new Handler(server.accept()));
			}
		}	
		catch (IOException ex) {
			pool.shutdown();
		}
	}
	public void lightOn() {
		if (curHandler != null && curHandler.isAlive()) {
			curHandler.sendUTF("lighton");
		}
	}
	public void lightOff() {
		if (curHandler != null && curHandler.isAlive()) {
			curHandler.sendUTF("lightoff");
		}
	}
	public void pompOn() {
		if (curHandler != null && curHandler.isAlive()) {
			curHandler.sendUTF("pompon");
		}
	}
	public void pompOff() {
		if (curHandler != null && curHandler.isAlive()) {
			curHandler.sendUTF("pompoff");
		}
	}
	public void feed() {
		if (curHandler != null && curHandler.isAlive()) {
			curHandler.sendUTF("feed");
		}
	}
	public void quit() {
		try {
			server.close();
		}
		catch (IOException ex) {
		}
	}
	public boolean isClientConnected() {
		if (curHandler == null) {
			return false;
		}
		return curHandler.isAlive();
	}
	public String atemp() {
		if (curHandler != null && curHandler.isAlive()) {
			curHandler.sendUTF("airtemp");
			return curHandler.getUTF();
		}
		return "----";
	}
	public String wtemp() {
		if (curHandler != null && curHandler.isAlive()) {
			curHandler.sendUTF("watertemp");
			return curHandler.getUTF();
		}
		return "----";
	}
	public String humid() {
		if (curHandler != null && curHandler.isAlive()) {
			curHandler.sendUTF("humidity");
			return curHandler.getUTF();
		}
		return "----";
	}
}
