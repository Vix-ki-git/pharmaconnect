package com.cts.mfrp.petz.api.swagger;

/**
 * Holds the currently-running test method's display name on a ThreadLocal so
 * {@link RecordingFilter} can tag each captured HTTP call with it. BaseApiTest
 * sets and clears this around every @Test method.
 */
public final class TestContext {

    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private TestContext() {}

    public static void set(String testName) {
        CURRENT.set(testName);
    }

    public static String current() {
        String name = CURRENT.get();
        return name == null ? "unknown" : name;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
