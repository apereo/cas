package org.apereo.cas.configuration;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CommaSeparatedStringToThrowablesConverterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = AopAutoConfiguration.class)
public class CommaSeparatedStringToThrowablesConverterTests {
    @Test
    public void verifyConverters() {
        val c = new CommaSeparatedStringToThrowablesConverter();
        val list = c.convert(Exception.class.getName() + ',' + RuntimeException.class.getName());
        assertEquals(2, list.size());
    }

    @Test
    public void verifyConverter() {
        val c = new CommaSeparatedStringToThrowablesConverter();
        val list = c.convert(Exception.class.getName());
        assertEquals(1, list.size());
    }
}
