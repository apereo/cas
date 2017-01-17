package org.apereo.cas.support;

import org.apache.cxf.sts.RealmParser;
import org.apache.cxf.ws.security.sts.provider.STSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.StringTokenizer;

/**
 * This is {@link UriRealmParser}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class UriRealmParser implements RealmParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(UriRealmParser.class);
    private Map<String, Object> realmMap;

    @Override
    public String parseRealm(final Map<String, Object> messageContext) throws STSException {
        final String url = (String) messageContext.get("org.apache.cxf.request.url");

        final StringTokenizer st = new StringTokenizer(url, "/");
        String realm = null;
        int count = st.countTokens();
        if (count <= 1) {
            return null;
        }
        count--;
        for (int i = 0; i < count; i++) {
            realm = st.nextToken();
        }
        realm = realm.toUpperCase();
        if (realmMap == null || !realmMap.containsKey(realm)) {
            LOGGER.warn("Unknown realm: {}", realm);
            throw new STSException("Unknown realm: " + realm);
        }

        LOGGER.debug("URI realm parsed: {}", realm);
        return realm;
    }

    public Map<String, Object> getRealmMap() {
        return realmMap;
    }

    public void setRealmMap(final Map<String, Object> realms) {
        this.realmMap = realms;
    }

}

