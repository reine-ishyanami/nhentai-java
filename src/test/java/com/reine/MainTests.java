package com.reine;

import com.reine.properties.Profile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author reine
 * 2024/7/17 23:38
 */
@SpringBootTest
public class MainTests {

    @Autowired
    private Profile profile;

    @Test
    void print_profile() {
        System.out.println(profile);
    }
}
