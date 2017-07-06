package application.util;

/**
 * Benannte Boolean-Werte.
 * @author Leonid Vilents
 */
public enum EBoolean {

	LOGIN (true),
	NOT_LOGIN (false),
	CASEMODDER (true),
	NOT_CASEMODDER (false),
	PROFILE_OWNER (true),
	NOT_PROFILE_OWNER (false);
	
	private final boolean value;
	
	EBoolean(boolean value) {this.value = value;}
	
	public boolean value() {return this.value;}
}