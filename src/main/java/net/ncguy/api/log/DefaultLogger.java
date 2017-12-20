package net.ncguy.api.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DefaultLogger implements ILogger {

    @Override
    public void Log(LogLevel level, String text) {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat();
        System.out.printf("[%s][%s] %s\n", df.format(date), level.name(), text);
    }
}
