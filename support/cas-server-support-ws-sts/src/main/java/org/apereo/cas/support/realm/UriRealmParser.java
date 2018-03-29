package org.apereo.cas.support.realm;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.sts.RealmParser;
import org.apache.cxf.sts.token.realm.RealmProperties;
import org.apache.cxf.ws.security.sts.provider.STSException;

import java.util.Map;
import java.util.StringTokenizer;

/**
 * This is {@link UriRealmParser}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@AllArgsConstructor
public class UriRealmParser implements RealmParser {

    private final Map<String, RealmProperties> realmMap;

    @Override
    public String parseRealm(final Map<String, Object> messageContext) throws STSException {
        final var url = (String) messageContext.get("org.apache.cxf.request.url");

        final var st = new StringTokenizer(url, "/");
        String realm = null;
        var count = st.countTokens();
        if (count <= 1) {
            return null;
        }
        count--;
        for (var i = 0; i < count; i++) {
            realm = st.nextToken();
        }
        realm = realm.toUpperCase();
        if (StringUtils.isBlank(realm) || !realmMap.containsKey(realm)) {
            LOGGER.warn("Unknown realm: [{}]", realm);
            throw new STSException("Unknown realm: " + realm);
        }

        LOGGER.debug("URI realm parsed: [{}]", realm);
        return realm.trim();
    }
}

