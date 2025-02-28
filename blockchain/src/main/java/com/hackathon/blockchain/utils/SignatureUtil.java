
package com.hackathon.blockchain.utils;

import java.security.*;

public class SignatureUtil {
    public static boolean verifySignature(String data, String signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(publicKey);
            sig.update(data.getBytes());
            return sig.verify(java.util.Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}