package org.apereo.cas.multihost;

import org.apereo.cas.configuration.model.core.multihost.SimpleHostProperties;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The {@link ClientInfo} supplemented with the current host.
 *
 * @author Jerome LELEU
 * @since 7.2.0
 */
@Slf4j
@Getter
public class MultiHostClientInfo extends ClientInfo {

    private SimpleHostProperties currentHost;

    public MultiHostClientInfo(final ClientInfo clientInfo, final SimpleHostProperties currentHost) {
        super(clientInfo.getClientIpAddress(), clientInfo.getServerIpAddress(), clientInfo.getUserAgent(), clientInfo.getGeoLocation());

        setDeviceFingerprint(clientInfo.getDeviceFingerprint());
        val extraInfo = new HashMap<String, Serializable>();
        for (val entry : clientInfo.getExtraInfo().entrySet()) {
            extraInfo.put(entry.getKey(), entry.getValue());
        }
        setExtraInfo(extraInfo);
        val headers = new HashMap<String, String>();
        for (val entry : clientInfo.getHeaders().entrySet()) {
            headers.put(entry.getKey(), (String) entry.getValue());
        }
        setHeaders(headers);
        setLocale(clientInfo.getLocale());

        this.currentHost = currentHost;
    }
}
