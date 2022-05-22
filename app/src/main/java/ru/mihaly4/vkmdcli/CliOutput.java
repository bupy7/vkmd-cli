package ru.mihaly4.vkmdcli;

import javax.annotation.Nonnull;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CliOutput implements ILogger {
    public void print(@Nonnull String message) {
        System.out.print(message);
    }

    public void println(@Nonnull String message) {
        System.out.println(message);
    }

    public void error(@Nonnull String message) {
        println("[ERROR " + getTimestamp() +"] " + message);
    }

    @Nonnull
    public OutputStream getStream() {
        return System.out;
    }

    private String getTimestamp() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        Date date = new Date();
        return dateFormat.format(date);
    }
}
