import java.util.concurrent.locks.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class SistemaLeiloes {

	class Utilizador {

		final String username;
		final String password;
		List<String> mensagens;

		public Utilizador(String username, String password) {
			this.username = username;
			this.password = password;
			mensagens = new ArrayList<>();
		}
	}

	class Leilao {

		private int valor_atual;
		private String descricao_item;
		private String ultimoLicitador;
		private String vendedor;
		private Lock leilaoLocker;
		private Set<Utilizador> licitadores;

		public Leilao(int valor_inicial, String descricao_item, String vendedor) {
			this.descricao_item = descricao_item;
			this.vendedor = vendedor;
			ultimoLicitador = null;
			valor_atual = valor_inicial;
			licitadores = new TreeSet<>();
			leilaoLocker = new ReentrantLock();
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
					u.mensagens.add(mensagem);
				}
		}
	}

	private HashMap<String, Utilizador> utilizadores;
	private HashMap<Integer, Leilao> leiloes;
	private int currentID;

	public SistemaLeiloes() {
		utilizadores = new HashMap<>();
		leiloes = new HashMap<>();
	}

	/**
	 * Método que regista um utilizador no sistema
	 * @param username Identificador único do utilizador
	 * @param password Palavra passe para autenticação do utilizador
	 */
	public void registarUtilizador(String username, String password) throws UtilizadorException {
		synchronized(this) {
			if(utilizadores.containsKey(username)) {
				throw new UtilizadorException("Nome de utilizador indisponível");
			}
			Utilizador user = new Utilizador(username, password);
			utilizadores.put(username, user);
		}
	}

	/**
	 * Método que autentica um utilizador
	 * @param username Identificador único do utilizador
	 * @param password Palavra passe para autenticação do utilizador
	 * @return 		   Booleano que indica se o utilizador foi autenticado ou não
	 */
	public synchronized boolean autenticar(String username, String password) {
		if(utilizadores.containsKey(username)) {
			Utilizador u = utilizadores.get(username);
			return u.password.equals(password);
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
		Leilao l = new Leilao(valor_inicial, descricaoItem, vendedor);
		synchronized(this) {
			leiloes.put(++currentID, l);
			return currentID;
		}
	}

	//Listar leiloes
	public List<String> listarLeiloes(String username) {
		//implementação
		return null;
	}

	/**
	 * Método que efetua licitação num item selecionado pelo utilizador
	 * @param idLeilao Numero identificativo do leilao em que será efetuada a licitação
	 * @param valor	   Valor da licitação
	 * @param username Username do utilizador a licitar
	 */
	public void licitarItem(int idLeilao, int valor, String username) throws LeilaoException, UtilizadorException {
		Leilao leilao;
		Utilizador user;
		synchronized(this) {
			leilao = getLeilao(idLeilao);
			user = utilizadores.get(username);
			leilao.leilaoLocker.lock();
		}
		try {
			if(valor > leilao.valor_atual) {
				leilao.valor_atual = valor;
				leilao.licitadores.add(user);
				leilao.ultimoLicitador = username;
				leilao.addMessageLicitadores(idLeilao);
			}
			else throw new LeilaoException("O valor atual no leilao é igual ou superior!");
		} finally {
			leilao.leilaoLocker.unlock();
		}
	}

	//Finaliza um leilão
	public String finalizarLeilao(int idLeilao, String username) throws UtilizadorException {
		return null;
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

	/* 
		Vai existir uma thread para cada user reservada para vir buscar mensagens
		Isto vai servir para sempre que alguem licita num leilao todos saibam(todos que participaram)
		Tambem serve para o finalizar leilão pois também vai adicionar mensagem nos moradores interessados 
	*/

	/**
	 * Método para obter a próxima mensagem para utilizador
	 * @param username Username do utilizador ao qual a mensagem é destinada
	 * @return 		   Mensagem destinada ao utilizador em questão
	 */
	public String lerMensagem(String username) throws InterruptedException {
		Utilizador user;
		synchronized(this) {
			user = utilizadores.get(username);
		}
		synchronized(user) {
			while(user.mensagens.size() == 0)
				wait();
			String msg = user.mensagens.get(0);
			user.mensagens.remove(0);
			return msg;
		}
	}
}