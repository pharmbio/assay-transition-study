package utils;

import java.io.OutputStream;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;

public class LoggerUtils {
	
	final private static String DEFAULT_PATTERN = "%d{yyyy-MM-dd HH:mm:ss} %-5p %c [%t] - %m%n";
	
	public static Appender<ILoggingEvent> addStreamAppenderToLogger(Logger theLogger, OutputStream stream, String pattern){
		LoggerContext context = theLogger.getLoggerContext();

		if(pattern == null || pattern.isEmpty())
			pattern = DEFAULT_PATTERN;
		PatternLayoutEncoder ple = new PatternLayoutEncoder();
		ple.setPattern(pattern);
		ple.setContext(context);
		ple.start();

		OutputStreamAppender<ILoggingEvent> streamAppender = new OutputStreamAppender<ILoggingEvent>();
		streamAppender.setContext(context);
		streamAppender.setEncoder(ple);
		streamAppender.setOutputStream(stream);
		streamAppender.start();
		String name = "stream-appender-"+(int)Math.round(Math.random()*100);
		streamAppender.setName(name);

		theLogger.addAppender(streamAppender);
		
		return streamAppender;
	}

}
