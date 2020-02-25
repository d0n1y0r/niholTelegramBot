package uz.nihol.bot;

import java.io.InvalidObjectException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import uz.nihol.BotConfig;
import uz.nihol.Commands;
import uz.nihol.IntegrationLogger;
import uz.nihol.ResBundle;
import uz.nihol.db.DBManager;
import uz.nihol.dto.News;
import uz.nihol.dto.Vacancy;

public class Bot extends TelegramLongPollingBot {

	private static final String ERROR_MESSAGE_TEXT = "There was an error sending the message to channel *%s*, the error was: ```%s```";
	private static final Logger logger = IntegrationLogger.getLogger(Bot.class);

	ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
	InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
	DBManager dbManager = null;

	private long chat_id;
	private String langCode;
	private boolean userNotification;

	public void onUpdateReceived(Update update) {
		update.getUpdateId();
		try {
			Message message = update.getMessage();
			if (message != null && message.hasText()) {
				chat_id = message.getChatId();
				try {
					dbManager = DBManager.getInstance();
					langCode = dbManager.getUserLanguage(message.getFrom().getId());
					userNotification = dbManager.getUserAlert(message.getFrom().getId());
					handleIncomingMessage(message);
				} catch (InvalidObjectException | SQLException e) {
					logger.error(e.getMessage());
				}
			} else if (update.hasCallbackQuery()) {
				try {
					handleCallbackQuery(update);
				} catch (InvalidObjectException e) {
					logger.error(e.getMessage());
				}
			}
		} catch (TelegramApiException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public String getBotUsername() {
		return BotConfig.NIHOL_BOT_USER;
	}

	@Override
	public String getBotToken() {
		return BotConfig.NIHOL_BOT_TOKEN;
	}

	private void handleIncomingMessage(Message message) throws InvalidObjectException, TelegramApiException {
		SendMessage sendMessage = new SendMessage();
		sendMessage.setReplyMarkup(replyKeyboardMarkup);
		sendMessage.enableHtml(true);
		sendMessage.setChatId(message.getChatId());
		if (userNotification)
			sendMessage.enableNotification();
		else
			sendMessage.disableNotification();
		mainKeyboard();

		if (message.getText().equals(Commands.MENU_START)) {
			sendMessage.setText(ResBundle.get("welcome", langCode));
			sendMessageToBot(sendMessage);
		} else if (message.getText().equals(Commands.MENU_STOP)) {
			sendMessage.setText(ResBundle.get("goodbye", langCode));
			sendMessageToBot(sendMessage);
		} else if (message.getText().equals(ResBundle.get(Commands.MENU_O_NAS, langCode))) {
			sendMessage.setText(getONas());
			sendMessageToBot(sendMessage);
		} else if (message.getText().equals(ResBundle.get(Commands.MENU_CONTACT, langCode))) {
			sendMessage.setText(getContact());
			sendMessageToBot(sendMessage);
		} else if (message.getText().equals(ResBundle.get(Commands.MENU_VACANCYS, langCode))) {
			getVacancyList();
		} else if (message.getText().contains(Commands.MENU_VACANCY_INFO)) {
			getVacancyInfo(message.getText());
		} else if (message.getText().equals(ResBundle.get(Commands.MENU_NEWS, langCode))) {
			getNewsList();
		} else if (message.getText().contains(Commands.MENU_NEWS_INFO)) {
			getNewsInfo(message.getText());
		} else if (message.getText().equals(ResBundle.get(Commands.MENU_SETTINGS, langCode))) {
			setupSettings(message);
		} else if (message.getText().equals(ResBundle.get(Commands.MENU_SETTINGS_LOCALE, langCode))) {
			setupLanguage();
		} else if (message.getText().equals(ResBundle.get(Commands.MENU_SETTINGS_ALERT, langCode))) {
			setupAlert();
		} else if (message.getText().equals(ResBundle.get(Commands.MENU_BACK, langCode))) {
			sendMessage.setText(ResBundle.get("selectMenu", langCode));
			sendMessageToBot(sendMessage);
		} else {
			sendMessage.setText(ResBundle.get("noUn", langCode));
			sendMessageToBot(sendMessage);
		}
	}

	private void handleCallbackQuery(Update update) throws InvalidObjectException, TelegramApiException {
		String queryData = update.getCallbackQuery().getData();
		int userId = update.getCallbackQuery().getFrom().getId();
		int message_id = update.getCallbackQuery().getMessage().getMessageId();
		long chat_id = update.getCallbackQuery().getMessage().getChatId();

		switch (queryData) {
		case "uz":
		case "ru":
			try {
				dbManager = DBManager.getInstance();
				dbManager.putUserLanguage(userId, queryData);
				langCode = queryData;
				String message = String.format(ResBundle.get("selectedLang", langCode), getLanguageName(langCode));
				DeleteMessage deleteMessage = new DeleteMessage();
				deleteMessage.setChatId(chat_id);
				deleteMessage.setMessageId(message_id);
				execute(deleteMessage);
				SendMessage sendMessage = new SendMessage();
				if (userNotification)
					sendMessage.enableNotification();
				else
					sendMessage.disableNotification();
				sendMessage.setText(message);
				sendMessage.enableHtml(true);
				sendMessage.setReplyMarkup(mainKeyboard());
				sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
				sendMessageToBot(sendMessage);
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
			break;
		case "on":
		case "off":
			try {
				dbManager = DBManager.getInstance();
				dbManager.putUserAlert(userId, queryData.equals("on") ? true : false);
				userNotification = queryData.equals("on") ? true : false;
				String message = ResBundle.get(queryData.equals("on") ? "alertOn" : "alertOff", langCode);
				DeleteMessage deleteMessage = new DeleteMessage();
				deleteMessage.setChatId(chat_id);
				deleteMessage.setMessageId(message_id);
				execute(deleteMessage);
				SendMessage sendMessage = new SendMessage();
				if (userNotification)
					sendMessage.enableNotification();
				else
					sendMessage.disableNotification();
				sendMessage.setText(message);
				sendMessage.enableHtml(true);
				sendMessage.setReplyMarkup(mainKeyboard());
				sendMessage.setChatId(update.getCallbackQuery().getMessage().getChatId());
				sendMessageToBot(sendMessage);
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}
			break;
		default:
			break;
		}
	}

	private void sendMessageToBot(SendMessage sendMessage) throws TelegramApiException {
		try {
			if (userNotification)
				sendMessage.enableNotification();
			else
				sendMessage.disableNotification();
			execute(sendMessage);
		} catch (Exception e) {
			sendErrorMessage(sendMessage, e.getMessage());
			logger.error(e.getMessage());
		}
	}

	private void sendErrorMessage(SendMessage message, String errorText) {
		SendMessage sendMessage = new SendMessage();
		sendMessage.enableMarkdown(true);
		sendMessage.setChatId(message.getChatId());
		sendMessage
				.setText(String.format(ERROR_MESSAGE_TEXT, message.getText().trim(), errorText.replace("\"", "\\\"")));
		sendMessage.enableMarkdown(true);
		if (userNotification)
			sendMessage.enableNotification();
		else
			sendMessage.disableNotification();
		try {
			execute(sendMessage);
		} catch (TelegramApiException e) {
			logger.error(e.getMessage());
		}
	}

	private ReplyKeyboardMarkup mainKeyboard() {
		ArrayList<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
		KeyboardRow keyboardFirstRow = new KeyboardRow();
		KeyboardRow keyboardSecondRow = new KeyboardRow();
		KeyboardRow keyboardThreeRow = new KeyboardRow();

		replyKeyboardMarkup.setSelective(true);
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboard(false);

		keyboard.clear();
		keyboardFirstRow.clear();
		keyboardSecondRow.clear();
		keyboardThreeRow.clear();

		keyboardFirstRow.add(ResBundle.get(Commands.MENU_O_NAS, langCode));
		keyboardFirstRow.add(ResBundle.get(Commands.MENU_NEWS, langCode));

		keyboardSecondRow.add(ResBundle.get(Commands.MENU_CONTACT, langCode));
		keyboardSecondRow.add(ResBundle.get(Commands.MENU_VACANCYS, langCode));

		keyboardThreeRow.add(ResBundle.get(Commands.MENU_SETTINGS, langCode));

		keyboard.add(keyboardFirstRow);
		keyboard.add(keyboardSecondRow);
		keyboard.add(keyboardThreeRow);
		replyKeyboardMarkup.setKeyboard(keyboard);
		return replyKeyboardMarkup;
	}

	public String getONas() {
		return ResBundle.get("nihol_text", langCode);
	}

	public String getContact() {
		String message = "\ud83d\udcde " + "Телефон: +998 71 208 58 44 \n" + "\ud83d\udce0 "
				+ "Факс: +998 71 208 58 44 \n" + "\ud83d\udce9 E-mail : info@nihol.com \n" + "\ud83d\udce2 "
				+ "Адрес : г.Ташкент ул. Интизор 26";
		return message;
	}

	public void setupSettings(Message message) {
		ArrayList<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();
		KeyboardRow keyboardFirstRow = new KeyboardRow();
		KeyboardRow keyboardSecondRow = new KeyboardRow();

		replyKeyboardMarkup.setSelective(true);
		replyKeyboardMarkup.setResizeKeyboard(true);
		replyKeyboardMarkup.setOneTimeKeyboard(false);

		keyboard.clear();
		keyboardFirstRow.clear();
		keyboardSecondRow.clear();

		keyboardFirstRow.add(ResBundle.get(Commands.MENU_SETTINGS_LOCALE, langCode));
		keyboardFirstRow.add(ResBundle.get(Commands.MENU_SETTINGS_ALERT, langCode));

		keyboardSecondRow.add(ResBundle.get(Commands.MENU_BACK, langCode));

		keyboard.add(keyboardFirstRow);
		keyboard.add(keyboardSecondRow);
		replyKeyboardMarkup.setKeyboard(keyboard);

		String text = ResBundle.get("selectDiv", langCode);
		SendMessage sendMessage = new SendMessage().setChatId(chat_id);
		if (userNotification)
			sendMessage.enableNotification();
		else
			sendMessage.disableNotification();
		sendMessage.enableHtml(true);
		try {
			sendMessage.setText(text);
			sendMessage.setReplyMarkup(replyKeyboardMarkup);
			execute(sendMessage);
		} catch (TelegramApiException e) {
			logger.error(e.getMessage());
		}

	}

	public void setupLanguage() {
		List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
		List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<InlineKeyboardButton>();

		rowList.clear();
		keyboardButtonsRow1.clear();

		keyboardButtonsRow1
				.add(new InlineKeyboardButton().setText(ResBundle.get("uz", langCode)).setCallbackData("uz"));
		keyboardButtonsRow1
				.add(new InlineKeyboardButton().setText(ResBundle.get("ru", langCode)).setCallbackData("ru"));
		rowList.add(keyboardButtonsRow1);

		inlineKeyboardMarkup.setKeyboard(rowList);

		SendMessage sendMessage = new SendMessage().setChatId(chat_id);
		sendMessage.enableHtml(true);
		try {
			if (userNotification)
				sendMessage.enableNotification();
			else
				sendMessage.disableNotification();
			sendMessage.setText(ResBundle.get("selectLang", langCode));
			sendMessage.setReplyMarkup(inlineKeyboardMarkup);
			execute(sendMessage);
		} catch (TelegramApiException e) {
			logger.error(e.getMessage());
		}

	}

	public void setupAlert() {
		List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
		List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<InlineKeyboardButton>();

		rowList.clear();
		keyboardButtonsRow1.clear();

		keyboardButtonsRow1
				.add(new InlineKeyboardButton().setText(ResBundle.get("on", langCode)).setCallbackData("on"));
		keyboardButtonsRow1
				.add(new InlineKeyboardButton().setText(ResBundle.get("off", langCode)).setCallbackData("off"));
		rowList.add(keyboardButtonsRow1);

		inlineKeyboardMarkup.setKeyboard(rowList);

		SendMessage sendMessage = new SendMessage().setChatId(chat_id);
		sendMessage.enableHtml(true);
		if (userNotification)
			sendMessage.enableNotification();
		else
			sendMessage.disableNotification();
		try {
			sendMessage.setText(ResBundle.get("menu.MENU_SETTINGS_ALERT", langCode));
			sendMessage.setReplyMarkup(inlineKeyboardMarkup);
			execute(sendMessage);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

	}

	public void getNewsList() {
		SendMessage sendMessage = new SendMessage().setChatId(chat_id);
		sendMessage.enableHtml(true);
		if (userNotification)
			sendMessage.enableNotification();
		else
			sendMessage.disableNotification();
		String news = new News().getListNews(langCode);
		try {
			sendMessage.setText(news);
			execute(sendMessage);
		} catch (TelegramApiException e) {
			logger.error(e.getMessage());
		}
	}

	public void getNewsInfo(String newsId) {
		SendMessage sendMessage = new SendMessage().setChatId(chat_id);
		sendMessage.enableHtml(true);
		if (userNotification)
			sendMessage.enableNotification();
		else
			sendMessage.disableNotification();
		String newsInfo = new News().getNewsInfo(newsId, langCode);
		try {
			sendMessage.setText(newsInfo);
			execute(sendMessage);
		} catch (TelegramApiException e) {
			logger.error(e.getMessage());
		}
	}

	public void getVacancyList() {
		SendMessage sendMessage = new SendMessage().setChatId(chat_id);
		sendMessage.enableHtml(true);
		if (userNotification)
			sendMessage.enableNotification();
		else
			sendMessage.disableNotification();
		String vacancys = new Vacancy().getListVacancys(langCode);
		try {
			sendMessage.setText(vacancys);
			execute(sendMessage);
		} catch (TelegramApiException e) {
			logger.error(e.getMessage());
		}
	}

	public void getVacancyInfo(String newsId) {
		SendMessage sendMessage = new SendMessage().setChatId(chat_id);
		sendMessage.enableHtml(true);
		if (userNotification)
			sendMessage.enableNotification();
		else
			sendMessage.disableNotification();
		String newsInfo = new Vacancy().getVacancyInfo(newsId, langCode);
		try {
			sendMessage.setText(newsInfo);
			execute(sendMessage);
		} catch (TelegramApiException e) {
			logger.error(e.getMessage());
		}
	}

	public String getLanguageName(String code) {
		String language = "Русский";
		switch (code) {
		case "ru":
			language = ResBundle.get("ru", langCode);
			break;
		case "uz":
			language = ResBundle.get("uz", langCode);
			break;
		default:
			break;
		}
		return language;
	}

}
