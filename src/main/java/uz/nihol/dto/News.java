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

public class News {
	
	private Document document;
	private static final Logger logger = IntegrationLogger.getLogger(Bot.class);
	
	public static final String LINK_NEWS_LIST = "http://nihol.uz/web/news/";
	public static final String LINK_NEWS_LIST_UZ = "http://nihol.uz/uz/news/";
	public static final String LINK_NEWS_INFO = "http://nihol.uz/web/news/";
	public static final String LINK_NEWS_INFO_UZ = "http://nihol.uz/uz/news/";

	public String getListNews(String langCode) {
		String listNest = "<b >" + ResBundle.get(Commands.MENU_NEWS, langCode) + "</b>\n";

		try {
			document = Jsoup.connect(langCode.equals("uz") ? LINK_NEWS_LIST_UZ : LINK_NEWS_LIST).get();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		Elements elements = document.getElementsByClass("blog-list-simple-text");

		if (elements.size() == 0)
			return listNest + ResBundle.get("notFound", langCode);

		for (int i = 0; i < elements.size(); i++) {
			if (i > 10)
				break;
			String[] url = elements.get(i).getElementsByClass("mt-20 left-holder").first().getElementsByTag("a")
					.attr("href").split("/");
			String newsItem = "\n" + "\ud83d\udc49 \""
					+ elements.get(i).getElementsByClass("news-list-header").first().text();
			newsItem += "  ( /news" + url[url.length - 1] + " )";
			listNest += newsItem + "\n";
		}
		return listNest;
	}

	public String getNewsInfo(String newsId, String langCode) {
		newsId = newsId.substring(Commands.MENU_NEWS_INFO.length());
		String newsInfo = "";
		try {
			String url = langCode.equals("uz") ? LINK_NEWS_INFO_UZ : LINK_NEWS_INFO;
			url += newsId + "/";
			document = Jsoup.connect(url).get();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		Element newsTitle = document.getElementById("blog-title-custom-id");
		Element newsDate = document.getElementById("DISPLAY_ACTIVE_FROM-ID");
		Element newsContent = document.getElementsByClass("blog-post-content news-text").first();

		if (newsTitle == null || newsTitle.text() == null || newsTitle.text().isEmpty())
			return ResBundle.get("notFoundNews", langCode);

		newsInfo = "\ud83c\udf0d " + newsTitle.text();
		newsInfo += "\n\n\ud83d\udcc5 " + newsDate.text();
		if (newsContent.text().length() > 4096 - newsInfo.length()) {
			newsInfo += "\n\n\ud83d\udccb " + newsContent.text().substring(0, 4096 - newsInfo.length() - 50) + " ...";
		} else {
			newsInfo += "\n\n\ud83d\udccb " + newsContent.text();
		}
		return newsInfo;
	}
}
