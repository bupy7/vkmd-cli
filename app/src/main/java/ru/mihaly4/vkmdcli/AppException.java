package ru.mihaly4.vkmdcli;

import javax.annotation.Nonnull;

public class AppException extends java.lang.Exception {
    public AppException(@Nonnull String message) {
        super(message);
    }
}
