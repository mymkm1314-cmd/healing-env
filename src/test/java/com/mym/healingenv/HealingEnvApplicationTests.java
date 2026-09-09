package com.mym.healingenv;

import com.mym.healingenv.utils.PasswordUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HealingEnvApplicationTests {

    @Test
    public void genHash() {
        String hash = PasswordUtils.encode("admin123");
        System.out.println("HASH=" + hash);
        System.out.println("验证=" + PasswordUtils.matches("admin123", hash));
    }

}
