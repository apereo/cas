package org.apereo.cas.support.realm;

import lombok.val;
import org.apache.cxf.sts.token.realm.RealmProperties;
import org.apache.cxf.ws.security.sts.provider.STSException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link UriRealmParserTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WSFederation")
public class UriRealmParserTests {

    @Test
    public void verifyToken() {
        val properties = new RealmProperties();
        val parser = new UriRealmParser(Map.of("APEREO.ORG", properties));
        assertEquals("APEREO.ORG", parser.parseRealm(Map.of("org.apache.cxf.request.url", "https://apereo.org/cas")));
        assertThrows(STSException.class, () ->
            parser.parseRealm(Map.of("org.apache.cxf.request.url", "https://example.org/cas")));
    }
}
