package org.apereo.cas.util.services;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.util.DefaultRegisteredServiceJsonSerializer;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link DefaultRegisteredServiceJsonSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultRegisteredServiceJsonSerializerTests {

    @Test
    public void checkNullability() {
        final DefaultRegisteredServiceJsonSerializer zer = new DefaultRegisteredServiceJsonSerializer();
        final String json = "    {\n"
                + "        \"@class\" : \"org.apereo.cas.services.RegexRegisteredService\",\n"
                + "            \"serviceId\" : \"^https://xyz.*\",\n"
                + "            \"name\" : \"XYZ\",\n"
                + "            \"id\" : \"20161214\"\n"
                + "    }";

        final RegisteredService s = zer.from(json);
        assertNotNull(s);
        assertNotNull(s.getAccessStrategy());
        assertNotNull(s.getAttributeReleasePolicy());
        assertNotNull(s.getProxyPolicy());
        assertNotNull(s.getUsernameAttributeProvider());
    }
}
