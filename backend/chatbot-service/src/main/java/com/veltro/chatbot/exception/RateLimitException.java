package com.veltro.chatbot.exception;

public class RateLimitException extends RuntimeException {
    public RateLimitException(String message) { super(message); }
}
