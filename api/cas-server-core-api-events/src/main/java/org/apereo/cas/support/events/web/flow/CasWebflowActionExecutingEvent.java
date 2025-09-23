package org.apereo.cas.support.events.web.flow;

import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

/**
 * This is {@link CasWebflowActionExecutingEvent}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
@ToString(callSuper = true)
@Getter
public class CasWebflowActionExecutingEvent extends AbstractCasEvent {

    @Serial
    private static final long serialVersionUID = -2862937393590213844L;

    private final Map scope;

    public CasWebflowActionExecutingEvent(final Object source,
                                          final Map scope,
                                          final ClientInfo clientInfo) {
        super(source, clientInfo);
        this.scope = new HashMap<>(scope);
    }
}
