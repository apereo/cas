package org.apereo.cas.support.realm;

import module java.base;
import lombok.val;
import org.apache.cxf.sts.token.realm.RealmProperties;
import org.apache.cxf.ws.security.sts.provider.STSException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UriRealmParserTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
class UriRealmParserTests {

    @Test
    void verifyToken() {
        val properties = new RealmProperties();
        val parser = new UriRealmParser(Map.of("APEREO.ORG", properties));
        assertEquals("APEREO.ORG", parser.parseRealm(Map.of("org.apache.cxf.request.url", "https://apereo.org/cas")));
        assertThrows(STSException.class, () ->
            parser.parseRealm(Map.of("org.apache.cxf.request.url", "https://example.org/cas")));
    }
}
