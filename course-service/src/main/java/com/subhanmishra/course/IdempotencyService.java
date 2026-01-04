package com.subhanmishra.course;

public interface IdempotencyService {
    boolean isDone(String key);
    void markDone(String key);
}
