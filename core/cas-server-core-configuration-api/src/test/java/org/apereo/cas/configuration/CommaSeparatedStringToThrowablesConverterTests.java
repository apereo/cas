package org.apereo.cas.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * This is {@link CommaSeparatedStringToThrowablesConverterTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
public class CommaSeparatedStringToThrowablesConverterTests {

    @Test
    public void verifyConverters() {
        final CommaSeparatedStringToThrowablesConverter c = new CommaSeparatedStringToThrowablesConverter();
        final List list = c.convert(Exception.class.getName() + "," + RuntimeException.class.getName());
        assertEquals(2, list.size());
    }

    @Test
    public void verifyConverter() {
        final CommaSeparatedStringToThrowablesConverter c = new CommaSeparatedStringToThrowablesConverter();
        final List list = c.convert(Exception.class.getName());
        assertEquals(1, list.size());
    }
}
