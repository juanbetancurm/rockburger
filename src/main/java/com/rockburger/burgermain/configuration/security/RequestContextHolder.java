package com.rockburger.burgermain.configuration.security;

/**
 * Holds request-specific context information across threads.
 */
public class RequestContextHolder {
    private static final ThreadLocal<String> currentRequestId = new ThreadLocal<>();

    public static void setRequestId(String requestId) {
        currentRequestId.set(requestId);
    }

    public static String getRequestId() {
        return currentRequestId.get();
    }

    public static void clear() {
        currentRequestId.remove();
    }
}