package com.example.peachmusic.common.retry;

import com.example.peachmusic.common.enums.ErrorCode;
import com.example.peachmusic.common.exception.CustomException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class LockRetryExecutor {

    @Retryable(
            retryFor = {CannotAcquireLockException.class},
            maxAttempts = LockRetryConstants.MAX_ATTEMPTS,
            backoff = @Backoff(delay = LockRetryConstants.BACKOFF_DELAY_MS)
    )
    public <T> T execute(Supplier<T> action) {
        return action.get();
    }

    @Recover
    public <T> T recover(CannotAcquireLockException e) {
        throw new CustomException(ErrorCode.LIKE_CONFLICT);
    }
}
