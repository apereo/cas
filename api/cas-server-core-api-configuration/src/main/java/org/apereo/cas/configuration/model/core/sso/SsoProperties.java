package org.apereo.cas.configuration.model.core.sso;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties class for {@code create.sso}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@RequiresModule(name = "cas-server-core-authentication", automated = true)
@Slf4j
@Getter
@Setter
public class SsoProperties implements Serializable {

    private static final long serialVersionUID = -8777647966370741733L;

    /**
     * Flag that indicates whether to create SSO session on re-newed authentication event.
     */
    private boolean renewedAuthn = true;

    /**
     * Flag that indicates whether to allow SSO session with a missing target service.
     */
    private boolean missingService = true;
}
