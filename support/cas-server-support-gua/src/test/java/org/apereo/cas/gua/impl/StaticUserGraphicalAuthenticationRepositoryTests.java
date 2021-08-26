package org.apereo.cas.gua.impl;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link StaticUserGraphicalAuthenticationRepositoryTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class StaticUserGraphicalAuthenticationRepositoryTests {

    @Test
    public void verifyImage() throws Exception {
        val repo = new StaticUserGraphicalAuthenticationRepository(Map.of("casuser", new ClassPathResource("image.jpg")));
        assertFalse(repo.getGraphics("casuser").isEmpty());
    }

    @Test
    public void verifyBadImage() throws Exception {
        val repo = new StaticUserGraphicalAuthenticationRepository(Map.of("casuser", new ClassPathResource("missing.jpg")));
        assertTrue(repo.getGraphics("casuser").isEmpty());
    }
}
