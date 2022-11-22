package net.lpf.msgs;
import java.util.ArrayList;
import java.util.Objects;
import static java.util.Objects.isNull;

	public class MessageQueue {
	private final ArrayList<ArrayList<Msg>> streams;
	private final Object[] s;
	public MessageQueue(int streamCount) {
		streams = new ArrayList<>();
		s = new Object[streamCount];
		initStreams(streamCount);
	}
	public MessageQueue() {
		this(1);
	}
	private void initStreams(int streamCount) {
		for (int i = 0; i < streamCount; i ++) {
			s[i] = new Object();
		}
		for (int i = 0; i < streamCount; i ++) {
			streams.add(new ArrayList<>());
		}
	}
	public Msg getMessage(int stream, String cmd) {
		Msg c;
		ArrayList<Msg> str = streams.get(stream);
		synchronized (s[stream]) {
			Msg[] cs = (Msg[]) str.stream().filter(msg -> msg.cmd().equals(cmd)).toArray(Msg[]::new);
			str.remove((c = cs[cs.length - 1]));
			str.removeIf(Objects::isNull);
		}
		return c;
	}
	public boolean hasMessage(int stream, String cmd) {
		ArrayList<Msg> str = streams.get(stream);
		synchronized (s[stream]) {
			if (str.stream().anyMatch(c -> c.cmd().equals(cmd))) {
				return true;
			}
		}
		return false;
	}
	public Msg[] getAllMessages(int stream, String cmd) {
		ArrayList<Msg> str = streams.get(stream);
		Msg[] c;
		synchronized (s[stream]) {
			c = str.stream().filter(msg -> msg.cmd().equals(cmd)).toArray(Msg[]::new);
			str.removeIf(msg -> msg.cmd().equals(cmd) || isNull(msg));
			str.removeIf(Objects::isNull);
		}
		return c;
	}
	public void sendMessage(int stream, String msg, String args) {
		ArrayList<Msg> c = streams.get(stream);
		synchronized (s[stream]) {
			c.add(0, new Msg(msg, args));
			c.removeIf(Objects::isNull);
			s[stream].notifyAll();
		}
	}
	public void waitForMessages(int stream) throws InterruptedException {
		ArrayList<Msg> c = streams.get(stream);
		synchronized (s[stream]) {
			while (c.isEmpty()) {
				s[stream].wait();
			}
		}
	}
	public void waitForMessages(int stream, long millis) throws InterruptedException {
		ArrayList<Msg> c = streams.get(stream);
		synchronized (s[stream]) {
			if (c.isEmpty()) {
				s[stream].wait(millis);
			}
		}
	}
	public void waitForMessage(int stream, String command) throws InterruptedException {
		boolean found = false;
		ArrayList<Msg> c = streams.get(stream);
		synchronized (s[stream]) {
			while ( ! found) {
				if (c.stream().anyMatch(msg -> msg.cmd().equals(command))) {
					found = true;
				}
				else {
					s[stream].wait();
				}
			}
		}
	}
}
