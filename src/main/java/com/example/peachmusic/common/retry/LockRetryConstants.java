package com.example.peachmusic.common.retry;

public final class LockRetryConstants {

    private LockRetryConstants() {}

    public static final int MAX_ATTEMPTS = 3;
    public static final long BACKOFF_DELAY_MS = 50L;
}
