package com.dokdok.gathering.util;

import java.security.SecureRandom;

public final class InvitationCodeGenerator {

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int DEFAULT_LENGTH = 20;
    private static final SecureRandom RANDOM = new SecureRandom();

    private InvitationCodeGenerator() {
        throw new IllegalStateException("Utility class");
    }

    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    public static String generate(int length) {
        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARSET.length());
            builder.append(CHARSET.charAt(index));
        }

        return builder.toString();
    }
}
