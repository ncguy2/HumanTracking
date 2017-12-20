package net.ncguy.api.log;

import static net.ncguy.api.log.ILogger.LogLevel.*;

public interface ILogger {

    void Log(LogLevel level, String text);

    default void Info(String text)  { Log(INFO,  text); }
    default void Warn(String text)  { Log(WARN,  text); }
    default void Error(String text) { Log(ERROR, text); }
    default void Fatal(String text) { Log(FATAL, text); }
    default void Debug(String text) { Log(DEBUG, text); }

    public static enum LogLevel {
        INFO,
        WARN,
        ERROR,
        FATAL,
        DEBUG,
        ;
    }

}
