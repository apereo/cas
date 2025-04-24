package org.apereo.cas.support.oauth.events;

import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.Getter;
import org.apereo.inspektr.common.web.ClientInfo;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link OAuth20AccessTokenResponseEvent}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class OAuth20AccessTokenResponseEvent extends AbstractCasEvent {
    @Serial
    private static final long serialVersionUID = -1859884708612488910L;

    @Getter
    private final Map context;

    public OAuth20AccessTokenResponseEvent(final Object source, final ClientInfo clientInfo,
                                           final Map context) {
        super(source, clientInfo);
        this.context = new HashMap<>(context);
    }
}
