package org.iesalixar.daw2.lectoraton.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

/**
 * Token firmado (HMAC) para enlaces del correo "aprobar como editor".
 */
@Component
public class EditorPromotionTokenUtil {

    private static final Duration TTL = Duration.ofDays(14);

    private final SecretKeySpec signingKey;

    public EditorPromotionTokenUtil(@Value("${jwt.secret}") String jwtSecret) {
        byte[] keyBytes = sha256(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String crearToken(Long usuarioId) {
        long exp = Instant.now().plus(TTL).getEpochSecond();
        String payload = usuarioId + ":" + exp;
        byte[] sig = sign(payload.getBytes(StandardCharsets.UTF_8));
        String p64 = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        String s64 = Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
        return p64 + "." + s64;
    }

    /**
     * @return id del usuario o null si inválido o caducado
     */
    public Long validarYUsuarioId(String token) {
        if (token == null || token.isBlank() || !token.contains(".")) {
            return null;
        }
        int dot = token.indexOf('.');
        String p64 = token.substring(0, dot);
        String s64 = token.substring(dot + 1);
        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(p64);
            byte[] sigExpected = Base64.getUrlDecoder().decode(s64);
            byte[] sigActual = sign(payloadBytes);
            if (!MessageDigest.isEqual(sigExpected, sigActual)) {
                return null;
            }
            String payload = new String(payloadBytes, StandardCharsets.UTF_8);
            String[] parts = payload.split(":");
            if (parts.length != 2) {
                return null;
            }
            long uid = Long.parseLong(parts[0]);
            long exp = Long.parseLong(parts[1]);
            if (Instant.now().getEpochSecond() > exp) {
                return null;
            }
            return uid;
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] sign(byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }
}
