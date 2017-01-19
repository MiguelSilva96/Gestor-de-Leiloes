import java.util.concurrent.locks.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeSet;

public class SistemaLeiloes {

	class Utilizador implements Comparable {

		final String username;
		final String password;
		List<String> mensagens;

		public Utilizador(String username, String password) {
			this.username = username;
			this.password = password;
			mensagens = new ArrayList<String>();
		}

		synchronized void adiciona(String msg) {
			mensagens.add(msg);
			notifyAll();
		}

		synchronized String getMsg() {
			while(mensagens.size()==0)
				try{wait();} catch(Exception e) {}
			String msg = mensagens.get(0);
			mensagens.remove(0);
			return msg;
		}

		public int compareTo(Object o) {
			Utilizador u = (Utilizador) o;
			return username.compareTo(u.username);
		}
	}

	class Leilao {

		private int valor_atual;
		private String descricao_item;
		private String ultimoLicitador;
		private String vendedor;
		private Lock leilaoLocker;
		private boolean aDecorrer;
		private TreeSet<Utilizador> licitadores;

		public Leilao(int valor_inicial, String descricao_item, String vendedor) {
			this.descricao_item = descricao_item;
			this.vendedor = vendedor;
			ultimoLicitador = "";
			valor_atual = valor_inicial;
			licitadores = new TreeSet<>();
			leilaoLocker = new ReentrantLock();
			aDecorrer = true;
		}

		void addMessageLicitadores(int idLeilao) {
			StringBuilder message = new StringBuilder();
			String mensagem;
			message.append("O utilizador ");
			message.append(ultimoLicitador);
			message.append(" licitou no leilão ");
			message.append(idLeilao);
			message.append("(" + descricao_item + ") ");
			message.append(". Valor: ");
			message.append(valor_atual);
			mensagem = message.toString();
			for(Utilizador u : licitadores)
				synchronized(u) {
					u.adiciona(mensagem);
				}
		}

		String finalizaLeilao (int idLeilao, Utilizador user) {
			StringBuilder message = new StringBuilder();
			message.append("O utilizador vencedor do leilao ");
			message.append(idLeilao);
			message.append("(" + descricao_item + ") " + "foi ");
			message.append(ultimoLicitador);
			message.append(" com o valor final de ");
			message.append(valor_atual);
			for(Utilizador u : licitadores)
				u.adiciona(message.toString());
			aDecorrer = false;
			return message.toString();
		}
	}

	private HashMap<String, Utilizador> utilizadores;
	private HashMap<Integer, Leilao> leiloes;
	private int currentID;

	public SistemaLeiloes() {
		utilizadores = new HashMap<>();
		leiloes = new HashMap<>();
		currentID = 0;
	}

	/**
	 * Método que regista um utilizador no sistema
	 * @param username Identificador único do utilizador
	 * @param password Palavra passe para autenticação do utilizador
	 */
	public synchronized void registarUtilizador(String username, String password) throws UtilizadorException {
		Utilizador user;
		if(utilizadores.containsKey(username)) {
			String str = "Nome de utilizador indisponível";
			throw new UtilizadorException(str);
		}
		user = new Utilizador(username, password);
		utilizadores.put(username, user);
	}

	/**
	 * Método que autentica um utilizador
	 * @param username Identificador único do utilizador
	 * @param password Palavra passe para autenticação do utilizador
	 * @return 		   Booleano que indica se o utilizador foi autenticado ou não
	 */
	public synchronized boolean autenticar(String username, String password) {
		Utilizador user;
		if(utilizadores.containsKey(username)) {
			user = utilizadores.get(username);
			return user.password.equals(password);
		}
		return false;
	}

	/**
	 * Método que inicia um novo leilão, associado a um utilizador como vendedor
	 * @param descricaoItem Descrição breve do item a leiloar
	 * @param vendedor 		Username do utilizador a iniciar o leilão
	 * @param valor_inicial Valor inicial a que o item será leiloado
	 * @return 				Id atribuido ao leilao inserido pelo utilizador
	 */
	public int iniciarLeilao(String descricaoItem, String vendedor, int valor_inicial) {
		Leilao leilao; 
		leilao = new Leilao(valor_inicial, descricaoItem, vendedor);
		synchronized(this) {
			currentID++;
			leiloes.put(new Integer(currentID), leilao);
			return currentID;
		}
	}

	//Listar leiloes
	public List<String> listarLeiloes(String username) {
		List<String> result = new ArrayList<>();
		synchronized(this) {
			leiloes.forEach((k,v) -> v.leilaoLocker.lock());
			try {
				leiloes.forEach((k,v) -> {
					StringBuilder sb = new StringBuilder();
					sb.append("Id leilao: ").append(k);
					sb.append(". ").append(v.descricao_item);
					sb.append(". Valor atual: ").append(v.valor_atual);
					sb.append(".");
					if(v.ultimoLicitador.equals(username))
						sb.append(" +");
					if(v.vendedor.equals(username))
						sb.append(" *");
					result.add(sb.toString());
				});
			} finally {
				leiloes.forEach((k,v) -> v.leilaoLocker.unlock());
			}
		}
		return result;
	}

	/**
	 * Método que efetua licitação num item selecionado pelo utilizador
	 * @param idLeilao Numero identificativo do leilao em que será efetuada a licitação
	 * @param valor	   Valor da licitação
	 * @param username Username do utilizador a licitar
	 */
	public void licitarItem(int idLeilao, int valor, String username) throws LeilaoException {
		Leilao leilao;
		String str;
		Utilizador user;
		synchronized(this) {
			leilao = getLeilao(idLeilao);
			user = utilizadores.get(username);
			leilao.leilaoLocker.lock();
		}
		try {
			if(!leilao.aDecorrer) {
				str = "O leilao já terminou!";
				throw new LeilaoException(str);
			}
			if(valor > leilao.valor_atual) {
				leilao.valor_atual = valor;
				leilao.licitadores.add(user);
				leilao.ultimoLicitador = username;
				leilao.addMessageLicitadores(idLeilao);
			}
			else {
				str = "O valor atual no leilao é igual ou superior!";
				throw new LeilaoException(str);
			}
		} finally {
			leilao.leilaoLocker.unlock();
		}
	}

	//Finaliza um leilão
	public String finalizarLeilao(int idLeilao, String username) throws LeilaoException, UtilizadorException {
		Leilao leilao;
		Utilizador user;
		String message;
		synchronized(this) {
			leilao = getLeilao(idLeilao);
			user = utilizadores.get(username);
			if(!user.username.equals(leilao.vendedor)) {
				String str = "Sem permissão para finalizar leilão.";
				throw new UtilizadorException(str);
			}
			leilao.leilaoLocker.lock();
			leiloes.remove(idLeilao);
		}
		try {	
			message = leilao.finalizaLeilao(idLeilao, user);
		} finally {
			leilao.leilaoLocker.unlock();
		}
		return message;
	}

	//Método auxiliar para obter um dos leilões no Map
	private synchronized Leilao getLeilao(int idLeilao) throws LeilaoException {
		Leilao l;
		if(leiloes.containsKey(idLeilao))
			l = leiloes.get(idLeilao);
		else
			throw new LeilaoException("Leilao não existe");
		return l;
	}


	/**
	 * Método para obter a próxima mensagem para utilizador
	 * @param username Username do utilizador ao qual a mensagem é destinada
	 * @return 		   Mensagem destinada ao utilizador em questão
	 */
	public String lerMensagem(String username) {
		Utilizador user;
		synchronized(this) {
			user = utilizadores.get(username);
		}
		return user.getMsg();
	}
}