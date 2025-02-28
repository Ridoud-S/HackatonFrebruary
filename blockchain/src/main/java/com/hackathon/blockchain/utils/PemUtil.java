// src/main/java/com/hackathon/blockchain/utils/PemUtil.java
package com.hackathon.blockchain.utils;

import java.security.Key;
import java.util.Base64;

public class PemUtil {
    public static String toPEMFormat(Key key, String keyType) {
        String encoded = Base64.getEncoder().encodeToString(key.getEncoded());
        return String.format(
                "-----BEGIN %s KEY-----\n%s\n-----END %s KEY-----",
                keyType,
                chunkedBase64(encoded),
                keyType
        );
    }

    private static String chunkedBase64(String encoded) {
        return encoded.replaceAll("(.{64})", "$1\n");
    }
}