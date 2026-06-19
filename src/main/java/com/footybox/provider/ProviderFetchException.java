package com.footybox.provider;

public class ProviderFetchException extends RuntimeException {

    private final boolean unavailable;

    public ProviderFetchException(String message, boolean unavailable) {
        super(message);
        this.unavailable = unavailable;
    }

    public ProviderFetchException(String message, boolean unavailable, Throwable cause) {
        super(message, cause);
        this.unavailable = unavailable;
    }

    public boolean isUnavailable() {
        return unavailable;
    }
}
