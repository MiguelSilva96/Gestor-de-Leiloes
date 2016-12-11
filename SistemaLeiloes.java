import java.util.concurrent.locks.*;
import java.util.HashMap;
import java.util.List;

public class SistemaLeiloes {

	class Leilao {

		private int valor_atual;
		private String descricao_item;
		private String ultimoLicitador;
		private Lock leilaoLocker;

		public Leilao(int valor_inicial, String descricao_item) {
			this.descricao_item = descricao_item;
			ultimoLicitador = null;
			valor_atual = valor_inicial;
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

    
    public void registarUtilizador(String username, String password) throws UtilizadorException {
	if (utilizadores.containsKey(username)) {
            throw new UtilizadorException("Nome de utilizador indispon�vel");
        }
        
        Utilizador user = new Utilizador (username, password);
        utilizadores.put(username, user);
	}

	public boolean autenticar(String username, String password) {
		return true;
	}

	//Ainda deve receber o valor inicial como argumento
	public void iniciarLeilao(String descricaoItem, String username) throws UtilizadorException {
		//implementação (username pode nao existir)
	}

	public List<Leilao> listarLeiloes() {
		//implementação
		return null;
	}

	//Este método pode mudar ou nao dependendo do finalizar e do uso ou nao de variaveis de condiçao
	public void licitarItem(int idLeilao, int valor, String username) throws LeilaoException, UtilizadorException {
		systemLocker.lock();
		Leilao l;
		Utilizador u;
		try {
			l = getLeilao(idLeilao);
			u = getUtilizador(username); //Se o utilizador não existir, exceção e nao faz licitação
			l.leilaoLocker.lock();
		} finally {
			systemLocker.unlock();
		}
		try {
			if(valor > l.valor_atual) {
				l.valor_atual = valor;
				l.ultimoLicitador = username;
			}
			else throw new LeilaoException("O valor atual no leilao é igual ou superior!");
		} finally {
			l.leilaoLocker.unlock();
		}
	}

	public String finalizarLeilao(int idLeilao, String username) {
		//implementação (username pode ser de outro user que nao o vendedor)
		return null;
	}

	private Leilao getLeilao(int idLeilao) throws LeilaoException {
		return null;
	}

	private Utilizador getUtilizador(String username) throws UtilizadorException {
		return null;
	}

}