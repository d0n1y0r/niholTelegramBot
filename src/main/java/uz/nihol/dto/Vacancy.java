package uz.nihol.dto;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import uz.nihol.Commands;
import uz.nihol.IntegrationLogger;
import uz.nihol.ResBundle;
import uz.nihol.bot.Bot;

public class Vacancy {
	
	private Document document;
	private static final Logger logger = IntegrationLogger.getLogger(Bot.class);

	public static final String LINK_VACANCY_LIST = "http://nihol.uz/web/vacancies/";
	public static final String LINK_VACANCY_INFO = "http://nihol.uz/web/vacancies/";

	public String getListVacancys(String langCode) {
		String listNest = new String();
		listNest = "<b >" + ResBundle.get(Commands.MENU_VACANCYS, langCode) + "</b>\n";
		try {
			document = Jsoup.connect(LINK_VACANCY_LIST).get();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		Elements elements = document.getElementsByClass("vacancy-header");

		for (int i = 0; i < elements.size(); i++) {
			if (i > 10)
				break;
			String[] url = elements.get(i).attr("href").split("/");
			String newsItem = "\n" + "\ud83c\udf00 " + elements.get(i).text();
			newsItem += "  ( /vac" + url[url.length - 1] + " )";
			listNest += newsItem + "\n";
		}
		return listNest;
	}

	public String getVacancyInfo(String newsId, String langCode) {
		String newsInfo = newsId = newsId.substring(Commands.MENU_VACANCY_INFO.length());
		try {
			document = Jsoup.connect(LINK_VACANCY_INFO + newsId + "/").get();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		Element newsTitle = document.getElementsByClass("blog-title-box").first();
		Element newsContent = document.getElementsByClass("blog-post-content").first();

		if (newsTitle == null || newsTitle.text() == null || newsTitle.text().isEmpty())
			return ResBundle.get("notFoundVac", langCode);

		newsInfo = "\ud83c\udf0d " + newsTitle.text();
		if (newsContent.text().length() > 4096 - newsInfo.length()) {
			newsInfo += "\n\n\ud83d\udccb " + newsContent.text().substring(0, 4096 - newsInfo.length() - 50) + " ...";
		} else {
			newsInfo += "\n\n\ud83d\udccb " + newsContent.text();
		}
		return newsInfo;
	}
}
