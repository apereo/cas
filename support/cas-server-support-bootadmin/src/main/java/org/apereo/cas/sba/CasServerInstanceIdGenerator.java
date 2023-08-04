package org.apereo.cas.sba;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.DigestUtils;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.domain.values.Registration;
import de.codecentric.boot.admin.server.services.InstanceIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import java.util.Objects;

/**
 * This is {@link CasServerInstanceIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class CasServerInstanceIdGenerator implements InstanceIdGenerator {
    private static final int INSTANCE_ID_LENGTH = 12;

    private final CasConfigurationProperties casProperties;

    @Override
    public InstanceId generateId(final Registration registration) {
        val serverName = StringUtils.defaultIfEmpty(registration.getMetadata().get("name"), casProperties.getHost().getName());
        val registrationHashKey = registration.getHealthUrl() + '-' + Objects.requireNonNull(serverName,
            "Server name cannot be undefined. Instance registration metadata must include a name in CAS configuration or [cas.host.name=...] must be defined.");
        val instanceId = StringUtils.substring(DigestUtils.sha256(registrationHashKey), 0, INSTANCE_ID_LENGTH);
        return InstanceId.of(instanceId);
    }
}
