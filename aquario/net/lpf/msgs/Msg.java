package net.lpf.msgs;
public class Msg {
	private final String cmd;
	private final Object arg;
	public Msg(String cmd, Object arg){
		this.cmd = cmd;
		this.arg = arg;
	}
	public Msg(String cmd){
		this.cmd = cmd;
		this.arg = null;
	}
	public String cmd() {
		return this.cmd;
	}
	public Object arg(){
		return this.arg;
	}
}
