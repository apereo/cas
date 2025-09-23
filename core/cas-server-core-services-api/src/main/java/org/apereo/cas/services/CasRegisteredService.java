package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.ObjectUtils;
import java.io.Serial;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link CasRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Getter
@Setter
@Accessors(chain = true)
public class CasRegisteredService extends BaseWebBasedRegisteredService implements CasModelRegisteredService {

    /**
     * The friendly name for this client.
     */
    public static final String FRIENDLY_NAME = "CAS Client";

    @Serial
    private static final long serialVersionUID = -2416680749378661897L;

    private RegisteredServiceProxyPolicy proxyPolicy = new RefuseRegisteredServiceProxyPolicy();

    private RegisteredServiceProxyTicketExpirationPolicy proxyTicketExpirationPolicy;

    private RegisteredServiceProxyGrantingTicketExpirationPolicy proxyGrantingTicketExpirationPolicy;

    private RegisteredServiceServiceTicketExpirationPolicy serviceTicketExpirationPolicy;

    private String redirectUrl;

    private Set<CasProtocolVersions> supportedProtocols = new LinkedHashSet<>();

    private String responseType;

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return FRIENDLY_NAME;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.proxyPolicy = ObjectUtils.getIfNull(this.proxyPolicy, new RefuseRegisteredServiceProxyPolicy());
    }
}
