import java.net.*;
import java.io.*;
import java.util.List;

class UserBool {
	String userOn;
	boolean running;

	UserBool() {
		userOn = null;
		running = true;
	}
}

class ClientWriter extends Thread {
	
	Socket client;
	SistemaLeiloes sistemaLeiloes;
	UserBool ub;
	PrintWriter out;

	public ClientWriter(Socket client, SistemaLeiloes sistemaLeiloes, UserBool ub, PrintWriter out) {
		this.client =client;
		this.sistemaLeiloes = sistemaLeiloes;
		this.ub = ub;
		this.out = out;
	}

	public void run() {
		try {
			while(ub.running) {
				String str = sistemaLeiloes.lerMensagem(ub.userOn);
				synchronized(out) {
					out.println(str);
					out.flush();
				}
			}
		} catch(Exception e) {}
	}
}

class ClientHandler extends Thread {

	Socket client;
	SistemaLeiloes sistemaLeiloes;
	UserBool ub;
	ClientWriter cw;
	PrintWriter out;

	public ClientHandler(Socket client, SistemaLeiloes sistemaLeiloes, UserBool ub, ClientWriter cw, PrintWriter out) {
		this.client = client;
		this.sistemaLeiloes = sistemaLeiloes;
		this.ub = ub;
		this.cw = cw;
		this.out = out;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			while(ub.running) {
				String str = in.readLine();
				if(str == null) {
					ub.running = false;
					break;
				}
				String[] strs = str.split(" ");
				if(ub.userOn == null) {
					switch(strs[0]) {
						case "l": 
							boolean b = sistemaLeiloes.autenticar(strs[1], strs[2]);
							if(b) {
								synchronized(out) {
									out.println("Login efetuado com sucesso!");
									ub.userOn = strs[1];
								}
								cw.start();
							}
							else {
								synchronized(out) {
									out.println("Username ou password errados");
								}
							}
							out.flush();
							break;
						case "r": 
							try {
								sistemaLeiloes.registarUtilizador(strs[1], strs[2]);
							} catch (UtilizadorException e) {
								synchronized(out) {
									out.println(e.getMessage());
									out.flush();
								}
								continue;
							}
							synchronized(out) {
								out.println("Registo efetuado com sucesso!");
							}
							out.flush();
							break;
						default:
							synchronized(out) {
								out.println("Faça login ou registe-se");
								out.flush();
							}
							break;
					}
				}
				else {
					switch(strs[0]) {
						case "i":
							int inicial = Integer.parseInt(strs[2]);
							int result;
							result = sistemaLeiloes.iniciarLeilao(strs[1], ub.userOn, inicial);
							StringBuilder sb = new StringBuilder();
							sb.append("Id do seu leilão: ").append(result);
							sb.append(".");
							synchronized(out) {
								out.println(sb.toString());
								out.flush();
							}
							break;
						case "lc":
							int id = Integer.parseInt(strs[1]);
							int valor = Integer.parseInt(strs[2]);
							try {
								sistemaLeiloes.licitarItem(id, valor, ub.userOn);
								synchronized(out) {
									out.println("Licitação efetuada com sucesso!");
									out.flush();
								}
							} catch (LeilaoException le) {
								synchronized(out) {
									out.println(le.getMessage());
									out.flush();
								}
								continue;
							}
							break;
						case "ls":
							List<String> l = sistemaLeiloes.listarLeiloes(ub.userOn);
							synchronized(out) {
								for(String s : l) {
									out.println(s);
								}
								out.flush();
							}
							if(l.size() == 0)
								synchronized(out) {
									out.println("Não existem leilões a decorrer");
								}
							break;
						case "f":
							int idl = Integer.parseInt(strs[1]);
							String res;
							try {
								res = sistemaLeiloes.finalizarLeilao(idl, ub.userOn);
								synchronized(out) {
									out.println(res);
									out.flush();
								}
							} catch (LeilaoException|UtilizadorException e) {
								synchronized(out) {
									out.println(e.getMessage());
									out.flush();
								}
								continue;
							}
							break;
						case "t": 
							ub.running = false;
							synchronized(out) {
								out.println("Sessão terminada");
								out.flush();
							}
							break;
						default:
							synchronized(out) {
								out.println("Já se encontra com sessão iniciada!");
								out.flush();
							}
							break;
					}
				}
			}
		} catch(Exception e) {}
	}

}

public class Server {

	public static void main(String[] args) throws Exception {
		SistemaLeiloes sistemaLeiloes = new SistemaLeiloes();
		int portNumber = Integer.parseInt(args[0]);
		ServerSocket srv = new ServerSocket(portNumber);
		while(true) {
			Socket client = srv.accept();
			UserBool ub = new UserBool();
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			ClientWriter cw = new ClientWriter(client, sistemaLeiloes, ub, out);
			ClientHandler ch = new ClientHandler(client, sistemaLeiloes, ub, cw, out);
			ch.start();
		}
	}
}
