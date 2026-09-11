package com.mym.healingenv;

import com.mym.healingenv.utils.PasswordUtils;
import org.junit.jupiter.api.Test;

class HealingEnvApplicationTests {

    @Test
    void passwordHashCanBeVerified() {
        String hash = PasswordUtils.encode("admin123");
        org.junit.jupiter.api.Assertions.assertTrue(PasswordUtils.matches("admin123", hash));
        org.junit.jupiter.api.Assertions.assertFalse(PasswordUtils.matches("wrong-password", hash));
    }
}
