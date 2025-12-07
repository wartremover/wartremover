package org.wartremover.test;

public enum TestEnum {
    A1;

    public static TestEnum valueOf(int x) {
        return A1;
    }

    public static TestEnum valueOf(String x1, String x2) {
        return A1;
    }
}
