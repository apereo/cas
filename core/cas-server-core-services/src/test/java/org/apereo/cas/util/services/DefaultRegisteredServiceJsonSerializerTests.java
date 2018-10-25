package org.apereo.cas.util.services;

import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultRegisteredServiceJsonSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultRegisteredServiceJsonSerializerTests {

    @Test
    public void checkNullability() {
        val zer = new DefaultRegisteredServiceJsonSerializer();
        val json = "    {\n"
            + "        \"@class\" : \"org.apereo.cas.services.RegexRegisteredService\",\n"
            + "            \"serviceId\" : \"^https://xyz.*\",\n"
            + "            \"name\" : \"XYZ\",\n"
            + "            \"id\" : \"20161214\"\n"
            + "    }";

        val s = zer.from(json);
        assertNotNull(s);
        assertNotNull(s.getAccessStrategy());
        assertNotNull(s.getAttributeReleasePolicy());
        assertNotNull(s.getProxyPolicy());
        assertNotNull(s.getUsernameAttributeProvider());
    }
}
