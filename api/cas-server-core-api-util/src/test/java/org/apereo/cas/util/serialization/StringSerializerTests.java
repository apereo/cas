package org.apereo.cas.util.serialization;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link StringSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Utility")
public class StringSerializerTests {
    @Test
    public void verifyOperation() {
        val input = mock(StringSerializer.class);
        when(input.supports(any(File.class))).thenCallRealMethod();
        when(input.supports(anyString())).thenCallRealMethod();
        when(input.load(any(InputStream.class))).thenCallRealMethod();
        when(input.getContentTypes()).thenCallRealMethod();
        assertTrue(input.supports(new File("something")));
        assertTrue(input.supports("something"));
        assertTrue(input.load(new ByteArrayInputStream(ArrayUtils.EMPTY_BYTE_ARRAY)).isEmpty());
        assertEquals(input.getContentTypes(), List.of(MediaType.TEXT_PLAIN));
    }
}
