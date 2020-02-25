package uz.nihol.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.JDBC;

import uz.nihol.BuildVars;

public class DBManager {

	private static DBManager instance = null;

	public static synchronized DBManager getInstance() throws SQLException {
		if (instance == null)
			instance = new DBManager();
		return instance;
	}

	// Объект, в котором будет храниться соединение с БД
	private Connection connection;

	private DBManager() throws SQLException {
		// Регистрируем драйвер, с которым будем работать
		// в нашем случае Sqlite
		DriverManager.registerDriver(new JDBC());
		// Выполняем подключение к базе данных
		this.connection = DriverManager.getConnection(BuildVars.linkDB);
	}

	public String getUserLanguage(Integer userId) {
		String languageCode = "ru";
		try {
			Statement statement = this.connection.createStatement();

			ResultSet resultSet = statement
					.executeQuery("SELECT languageCode FROM UserLanguage WHERE userId = " + userId);
			if (resultSet.next()) {
				languageCode = resultSet.getString("languageCode");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

		}
		return languageCode;
	}

	public boolean putUserLanguage(Integer userId, String language) {
		int updatedRows = 0;
		try {
			PreparedStatement preparedStatement = this.connection
					.prepareStatement("REPLACE INTO UserLanguage (userId, languageCode) VALUES(?, ?)");
			preparedStatement.setInt(1, userId);
			preparedStatement.setString(2, language);
			updatedRows = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return updatedRows > 0;
	}

	public boolean getUserAlert(Integer userId) {
		boolean alert = true;
		try {
			Statement statement = this.connection.createStatement();

			ResultSet resultSet = statement.executeQuery("SELECT alert FROM userAlert WHERE userId = " + userId);
			if (resultSet.next()) {
				alert = resultSet.getBoolean("alert");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {

		}
		return alert;
	}

	public boolean putUserAlert(Integer userId, boolean alert) {
		int updatedRows = 0;
		try {
			PreparedStatement preparedStatement = this.connection
					.prepareStatement("REPLACE INTO userAlert (userId, alert) VALUES(?, ?)");
			preparedStatement.setInt(1, userId);
			preparedStatement.setBoolean(2, alert);
			updatedRows = preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return updatedRows > 0;
	}
}
