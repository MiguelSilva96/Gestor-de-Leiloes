import java.util.concurrent.locks.*;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SistemaLeiloes {

	class Leilao {

		private int valor_atual;
		private String descricao_item;
		private String ultimoLicitador;
		private String vendedor;
		private Lock leilaoLocker;
		private Set<String> licitadores;

		public Leilao(int valor_inicial, String descricao_item, String vendedor) {
			this.descricao_item = descricao_item;
			this.vendedor = vendedor;
			ultimoLicitador = null;
			valor_atual = valor_inicial;
			licitadores = new TreeSet<>();
			leilaoLocker = new ReentrantLock();
		}
	}

	class Utilizador {

		private String username;
		private String password;

		public Utilizador(String username, String password) {
			this.username = username;
			this.password = password;
		}
	}

	private HashMap<String, Utilizador> utilizadores;
	private HashMap<Integer, Leilao> leiloes;
	private Lock systemLocker;

	public SistemaLeiloes() {
		utilizadores = new HashMap<>();
		leiloes = new HashMap<>();
		systemLocker = new ReentrantLock();
	}

    //Registar utilizador no sistema
    public void registarUtilizador(String username, String password) throws UtilizadorException {
    	systemLocker.lock();
    	try {
			if(utilizadores.containsKey(username)) {
				throw new UtilizadorException("Nome de utilizador indisponível");
        	}
        	Utilizador user = new Utilizador(username, password);
        	utilizadores.put(username, user);
        } finally {
        	systemLocker.unlock();
        }
	}

	//Autenticar utilizador
	public boolean autenticar(String username, String password) {
		systemLocker.lock();
		try {
			if(utilizadores.containsKey(username))
				return true;
			return false;
		} finally {
			systemLocker.unlock();
		}
	}

	//Ainda deve receber o valor inicial como argumento
	public void iniciarLeilao(String descricaoItem, String vendedor) throws UtilizadorException {
		//implementação (username pode nao existir)
	}

	//Listar leiloes
	public List<String> listarLeiloes(String username) {
		//implementação
		return null;
	}

	//Este método pode mudar ou nao dependendo do finalizar e do uso ou nao de variaveis de condiçao
	public void licitarItem(int idLeilao, int valor, String username) throws LeilaoException, UtilizadorException {
		systemLocker.lock();
		Leilao l;
		try {
			l = getLeilao(idLeilao);
			l.leilaoLocker.lock();
		} finally {
			systemLocker.unlock();
		}
		try {
			if(valor > l.valor_atual) {
				l.valor_atual = valor;
				l.licitadores.add(username);
				l.ultimoLicitador = username;
			}
			else throw new LeilaoException("O valor atual no leilao é igual ou superior!");
		} finally {
			l.leilaoLocker.unlock();
		}
	}


	public String finalizarLeilao(int idLeilao, String username) {
		return null;
	}

	private Leilao getLeilao(int idLeilao) throws LeilaoException {
		Leilao l;
		if(leiloes.containsKey(idLeilao))
			l = leiloes.get(idLeilao);
		else
			throw new LeilaoException("Leilao não existente");
		return l;
	}

	private Utilizador getUtilizador(String username) throws UtilizadorException {
		Utilizador u;
		if(utilizadores.containsKey(username))
			u = utilizadores.get(username);
		else
			throw new UtilizadorException("Utilizador não existente");
		return u;
	}
}