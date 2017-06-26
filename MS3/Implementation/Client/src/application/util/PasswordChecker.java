package application.util;

/**
 * Pr�ft, ob das Passwort eine ausreichende St�rke besitzt.
 * @author L�on
 *
 */
public class PasswordChecker {
	
	public static String checkPasswordStrength(String password)
	{
		if (password.matches("([^!?#�$%&@�A-Z].?)"))  {
			return "Das Passwort sollte aus Klein- und Gro�buchstaben, sowie Zahlen und Sonderzeichen bestehen";
		}
		
		if (password.length() < 8) {
			return "Das Passwort sollte mindestens aus 8 Zeichen bestehen";
		}
		
		return "";
	}
}
