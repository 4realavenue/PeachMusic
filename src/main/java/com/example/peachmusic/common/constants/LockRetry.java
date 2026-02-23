package com.example.peachmusic.common.constants;

public final class LockRetry {

    private LockRetry() {}

    public static final int MAX_ATTEMPTS = 3;
    public static final long BACKOFF_DELAY_MS = 50L;
}
