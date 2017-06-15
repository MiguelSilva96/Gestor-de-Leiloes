import java.io.*;
import java.net.*;

class Client_Thread extends Thread {

    Socket cliente;
    boolean running;

    public Client_Thread (Socket cli) {
        this.cliente = cli;
        running = true;
    }

    public void run () {
        try {
            PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader (new InputStreamReader(System.in));
            String m = null;
            while ((m = stdIn.readLine()) != null && running) {
                String[] parts = m.split(" ");
                switch (parts[0]) {
                    case "r":
                        if (parts.length != 3) {
                            System.out.println("Instrução inválida.");
                            continue;
                        }
                        break;
                    case "l":
                        if (parts.length != 3) {
                            System.out.println("Instrução inválida.");
                            continue;
                        }
                        break;
                    case "i":
                        if (parts.length != 3) {
                            System.out.println("Instrução inválida.");
                            continue;
                        }
                        break;
                    case "lc":
                        if (parts.length != 3) {
                            System.out.println("Instrução inválida.");
                            continue;
                        }
                        break;
                    case "f":
                        if (parts.length != 2) {
                            System.out.println("Instrução inválida.");
                            continue;
                        }
                        break;
                    case "ls":
                        if (parts.length != 1) {
                            System.out.println("Instrução inválida.");
                            continue;
                        }
                        break;
                    case "t":
                        if (parts.length != 1) {
                            System.out.println("Instrução inválida.\n");
                            continue;
                        }
                        break;
                    default:
                        break;
                }
                out.println(m);
                if(m.equals("t")) break;
            }
        } catch(Exception e) {}
    }

    void setRunning(boolean b) {
        this.running = b;
    }

}

public class Cliente {

    public static void main(String[] args) throws Exception {
        
        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        Socket clienteSocket = new Socket(hostName, portNumber);

        Client_Thread ct = new Client_Thread (clienteSocket);
        BufferedReader in = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
        String m = null;
        ct.start();
        while ((m = in.readLine()) != null) {
            System.out.println(m);
            if(m.equals("Sessão terminada")) {
                ct.setRunning(false);
                ct.join();
                break;
            }
        }
        clienteSocket.close();
    }
}
