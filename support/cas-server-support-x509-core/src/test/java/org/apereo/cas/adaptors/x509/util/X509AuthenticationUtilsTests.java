package org.apereo.cas.adaptors.x509.util;

import org.apereo.cas.configuration.model.support.x509.SubjectDnPrincipalResolverProperties;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509AuthenticationUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("X509")
public class X509AuthenticationUtilsTests {
    @Test
    public void verifyOperation() {
        Arrays.stream(SubjectDnPrincipalResolverProperties.SubjectDnFormat.values()).forEach(opt -> {
            val results = X509AuthenticationUtils.getSubjectDnFormat(opt);
            if (opt != SubjectDnPrincipalResolverProperties.SubjectDnFormat.DEFAULT) {
                assertNotNull(results);
            } else {
                assertNull(results);
            }
        });
    }
}
