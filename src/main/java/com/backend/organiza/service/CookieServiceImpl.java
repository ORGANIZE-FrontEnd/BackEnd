package com.backend.organiza.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import static org.springframework.web.util.WebUtils.getCookie;

@Service
public class CookieServiceImpl{

    private static final Logger LOGGER = LoggerFactory.getLogger(CookieServiceImpl.class);


    @Getter
    @Value("${security.cookie.cookieName}")
    private String cookieName;

    @Value("${security.cookie.secretKey}")
    private String secretKey;


    protected boolean cookieExists(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        Optional<Cookie> cookie = Arrays.stream(cookies).filter(c -> c.getName().equals(cookieName)).findAny();

        return cookie.filter(value -> value.getName() != null).isPresent();
    }
    public void saveEncryptedToken(HttpServletResponse response , String token) {
        if(token == null || token.isEmpty()) {
            LOGGER.error("ERROR AT SAVE COOKIE: The provided TOKEN is null");
            return;
        }

        if(cookieName == null || cookieName.isEmpty()) {
            LOGGER.error("ERROR AT SAVE COOKIE: Cookie name was not provided");
            return;
        }

        if(secretKey == null || secretKey.isEmpty()) {
            LOGGER.error("ERROR AT SAVE COOKIE: Secret KEY was not provided");
            return;
        }

        try {
            byte[] decodedKey = Base64.getDecoder().decode(secretKey);
            SecretKey originalKey = new SecretKeySpec(Arrays.copyOf(decodedKey, 16), "AES");

            byte[] ivBytes = new byte[12];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(ivBytes);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, ivBytes);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, originalKey, gcmSpec);
            byte[] cipherText = cipher.doFinal(token.getBytes(StandardCharsets.UTF_8));

            byte[] combinedData = new byte[ivBytes.length + cipherText.length];
            System.arraycopy(ivBytes, 0, combinedData, 0, ivBytes.length);
            System.arraycopy(cipherText, 0, combinedData, ivBytes.length, cipherText.length);

            String encodedToken = Base64.getEncoder().encodeToString(combinedData);

            final int expiryTime = 60 * 30;
            final String cookiePath = "/";

            Cookie cookie = new Cookie(cookieName, encodedToken);
            cookie.setSecure(true);
            cookie.setHttpOnly(true);
            cookie.setMaxAge(expiryTime);
            cookie.setPath(cookiePath);
            cookie.setAttribute("SameSite", "None");
            response.addCookie(cookie);
        } catch (Exception e) {
            LOGGER.error("ERROR AT CREATING COOKIE: ", e);
        }
    }

    public String getDecryptedToken(HttpServletRequest request) {
        if(cookieName == null || cookieName.isEmpty()) {
            LOGGER.error("ERROR AT DECRYPT COOKIE: Cookie name was not provided");
            return null;
        }

        if(secretKey == null || secretKey.isEmpty()) {
            LOGGER.error("ERROR AT DECRYPT COOKIE: Secret KEY was not provided");
            return null;
        }

        byte[] decodedKey = Base64.getDecoder().decode(secretKey);

        try {
            if (cookieExists(request)) {
                String encryptedToken = getCookie(request, cookieName).getValue();
                byte[] combinedData = Base64.getDecoder().decode(encryptedToken);

                byte[] ivBytes = Arrays.copyOfRange(combinedData, 0, 12);
                byte[] cipherText = Arrays.copyOfRange(combinedData, 12, combinedData.length);

                GCMParameterSpec gcmSpec = new GCMParameterSpec(128, ivBytes);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                SecretKey originalKey = new SecretKeySpec(Arrays.copyOf(decodedKey, 16), "AES");
                cipher.init(Cipher.DECRYPT_MODE, originalKey, gcmSpec);

                byte[] plainText = cipher.doFinal(cipherText);
                LOGGER.info("DECRYPT COOKIE SERVICE - Token obtained successfully");
                return new String(plainText, StandardCharsets.UTF_8);
            } else {
                LOGGER.info("ERROR AT DECRYPT COOKIE - No valid cookies found");
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("ERROR AT DECRYPT COOKIE: ", e);
            return null;
        }
    }



    public static Boolean eraseCookie(String cookieName, HttpServletResponse response) {
        if (cookieName == null || cookieName.isEmpty()) {
            LOGGER.error("ERROR AT ERASE COOKIE: Cookie name was not provided");
            return false;
        }

        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);

        response.addCookie(cookie);
        LOGGER.info("Cookie erased successfully");
        return true;
    }

}
