package org.apereo.cas.adaptors.x509.authentication.principal;

import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509UPNExtractorUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("X509")
public class X509UPNExtractorUtilsTests {
    @Test
    public void verifyEmptyValue() {
        val names = new ArrayList();
        names.add(100);
        names.add(ArrayUtils.EMPTY_BYTE_ARRAY);

        val altNames = new HashSet();
        altNames.add(names);

        val results = X509UPNExtractorUtils.extractUPNString(altNames);
        assertTrue(results.isEmpty());
    }

    @Test
    public void verifyBadValue() {
        val names = new ArrayList();
        names.add(100);
        names.add(new byte[]{1, 2, 3, 4});

        val altNames = new HashSet();
        altNames.add(names);

        val results = X509UPNExtractorUtils.extractUPNString(altNames);
        assertTrue(results.isEmpty());
    }
}
