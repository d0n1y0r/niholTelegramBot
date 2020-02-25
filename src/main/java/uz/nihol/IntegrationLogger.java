package uz.nihol;

import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class IntegrationLogger {

	static {
		PatternLayout layout = new PatternLayout();
		String conversionPattern = "%d{yyyy-MM-dd HH:mm:ss:SSS}\n%c %M%n%p %m%n";
		layout.setConversionPattern(conversionPattern);

		DailyRollingFileAppender rollingAppender = new DailyRollingFileAppender();
		rollingAppender.setName("DailyRollingFileAppender");
		rollingAppender.setFile((System.getProperty("user.dir") + "/NiholBotLog.log"));
		rollingAppender.setEncoding("UTF-8");
		rollingAppender.setDatePattern("'.'yyyy-MM-dd");
		rollingAppender.setLayout(layout);
		rollingAppender.activateOptions();

		Logger rootLogger = Logger.getRootLogger();
		rootLogger.addAppender(rollingAppender);
	}

	@SuppressWarnings("rawtypes")
	public static Logger getLogger(Class clazz) {
		Logger logger = Logger.getLogger(clazz);
		return logger;
	}

}
