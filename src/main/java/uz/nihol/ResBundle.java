package uz.nihol;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ResBundle {

	private static final String STRINGS_FILE = "messages";
	private static final String STRINGS_FILE_RU = "messages_ru";
	private static final String STRINGS_FILE_UZ = "messages_uz";

	private static ResourceBundle resBundle = ResourceBundle.getBundle(STRINGS_FILE);
	private static ResourceBundle resBundleRu = ResourceBundle.getBundle(STRINGS_FILE_RU);
	private static ResourceBundle resBundleUz = ResourceBundle.getBundle(STRINGS_FILE_UZ);

	public static String get(String key) {
		return resBundle.getString(key);
	}

	public static String get(String key, String language) {
		String result;
		try {
			switch (language.toLowerCase()) {
			case "uz":
				result = resBundleUz.getString(key);
				break;
			case "ru":
				result = resBundleRu.getString(key);
				break;
			default:
				result = resBundle.getString(key);
				break;
			}
		} catch (MissingResourceException e) {
			result = resBundle.getString(key);
		}

		return result;
	}
}
