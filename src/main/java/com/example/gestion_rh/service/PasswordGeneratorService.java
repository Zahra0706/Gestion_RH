package com.example.gestion_rh.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class PasswordGeneratorService {

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String OTHER_CHAR = "!@#$%&*()_+-=[]?";

    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
    private static final String PASSWORD_ALLOW = PASSWORD_ALLOW_BASE;

    private static final SecureRandom random = new SecureRandom();

    public String generateRandomPassword(int length) {
        if (length < 1) throw new IllegalArgumentException("La longueur doit être positive");

        StringBuilder sb = new StringBuilder(length);

        // Au moins un caractère de chaque type
        sb.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        sb.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        sb.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        sb.append(OTHER_CHAR.charAt(random.nextInt(OTHER_CHAR.length())));

        // Remplir le reste
        for (int i = 4; i < length; i++) {
            sb.append(PASSWORD_ALLOW.charAt(random.nextInt(PASSWORD_ALLOW.length())));
        }

        // Mélanger les caractères
        return shuffleString(sb.toString());
    }

    private String shuffleString(String input) {
        char[] characters = input.toCharArray();
        for (int i = characters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }
        return new String(characters);
    }
}
