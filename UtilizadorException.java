public class UtilizadorException extends Exception {
	String message;
	
	public UtilizadorException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}