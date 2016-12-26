import java.net.*;
import java.io.*;

class ClientMessenger extends Thread {
	
}

class ClientHandler extends Thread {

}

public class Server {

	public static void main(String[] args) throws Exception {
		SistemaLeiloes sistemaLeiloes = new SistemaLeiloes();
		int portNumber = Integer.parseInt(args[0]);
		ServerSocket srv = new ServerSocket(portNumber);
		while(true) {
			Socket client = srv.accept();
/*
			ClientMessenger clientMessenger = new ClientMessenger(client, sistemaLeiloes);
			ClientHandler clientHandler = new ClientHandler(client, sistemaLeiloes, clientMessager);
			clientMessager.start();
			clientHandler.start();*/
		}
	}
}
