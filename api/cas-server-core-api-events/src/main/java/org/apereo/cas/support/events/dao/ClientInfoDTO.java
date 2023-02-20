package org.apereo.cas.support.events.dao;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;

import java.io.Serial;

@Getter
@AllArgsConstructor
public class ClientInfoDTO implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = -6032827784114751797L;

    private final String clientIpAddress;
    private final String serverIpAddress;

    private final String userAgent;

    private final String geoLocation;

    private final boolean initialized;

    public ClientInfoDTO(ClientInfo clientInfo){
        if(clientInfo != null && StringUtils.isNotEmpty(clientInfo.getClientIpAddress())) {
            this.userAgent = clientInfo.getUserAgent();
            this.clientIpAddress = clientInfo.getClientIpAddress();
            this.serverIpAddress = clientInfo.getServerIpAddress();
            this.geoLocation = clientInfo.getGeoLocation();
            this.initialized = true;
        }else {
            this.userAgent = null;
            this.clientIpAddress = null;
            this.serverIpAddress = null;
            this.geoLocation = null;
            this.initialized = false;
        }
    }
}
