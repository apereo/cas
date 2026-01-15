package org.apereo.cas.pm.event;

import module java.base;
import org.apereo.cas.pm.PasswordChangeRequest;
import org.apereo.cas.support.events.AbstractCasEvent;
import lombok.Getter;
import lombok.ToString;
import org.apereo.inspektr.common.web.ClientInfo;


/**
 * This is {@link PasswordChangeSuccessEvent}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@ToString(callSuper = true)
@Getter
public class PasswordChangeSuccessEvent extends AbstractCasEvent {
    @Serial
    private static final long serialVersionUID = -1862937393590213811L;

    private final PasswordChangeRequest request;

    public PasswordChangeSuccessEvent(final Object source, final ClientInfo clientInfo,
                                      final PasswordChangeRequest request) {
        super(source, clientInfo);
        this.request = request;
    }
}
