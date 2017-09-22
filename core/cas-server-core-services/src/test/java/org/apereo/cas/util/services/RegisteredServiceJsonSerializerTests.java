package org.apereo.cas.util.services;

import org.apereo.cas.services.RegisteredService;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This is {@link RegisteredServiceJsonSerializerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class RegisteredServiceJsonSerializerTests {

    @Test
    public void checkNullability() {
        final RegisteredServiceJsonSerializer zer = new RegisteredServiceJsonSerializer();
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
