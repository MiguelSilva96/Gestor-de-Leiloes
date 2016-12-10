public class LeilaoException extends Exception {
	String message;
	
	public LeilaoException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}