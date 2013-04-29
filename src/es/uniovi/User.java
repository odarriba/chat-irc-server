package es.uniovi;
import java.net.Socket;

public class User {
	private Socket socket;
	private String nick;
	
	public User(String nick, Socket socket) {
		this.nick = nick;
		this.socket = socket;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	public String getNick() {
		return nick;
	}
	
	public void setNick(String nick) {
		this.nick = nick;
	}
}
