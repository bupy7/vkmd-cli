package ru.mihaly4.vkmdcli;

import javax.annotation.Nonnull;

public interface ILogger {
    void println(@Nonnull String message);

    void error(@Nonnull String message);
}
