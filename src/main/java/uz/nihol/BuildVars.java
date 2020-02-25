package uz.nihol;

public class BuildVars {
	public static final String linkDB = "jdbc:sqlite:" + System.getProperty("user.dir")
			+ "/src/main/java/uz/nihol/db/nihol.db";
	public static final String controllerDB = "com.mysql.cj.jdbc.Driver";
	public static final String userDB = "<your-database-user>";
	public static final String password = "<your-databas-user-password>";
}
