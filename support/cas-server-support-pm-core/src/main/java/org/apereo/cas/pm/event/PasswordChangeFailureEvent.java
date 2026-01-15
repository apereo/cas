package org.apereo.cas.pm.event;

import module java.base;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;


/**
 * This is {@link PasswordChangeFailureEvent}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@ToString(callSuper = true)
@Getter
public class PasswordChangeFailureEvent extends AbstractCasEvent {
    @Serial
    private static final long serialVersionUID = -1862937393590213821L;

    private final PasswordChangeRequest request;
    private final Throwable error;
    
    public PasswordChangeFailureEvent(final Object source, final ClientInfo clientInfo,
                                      final PasswordChangeRequest request, final Throwable error) {
        super(source, clientInfo);
        this.request = request;
        this.error = error;
    }
}
