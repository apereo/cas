package org.apereo.cas.web.flow.account;

import module java.base;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apereo.inspektr.audit.AuditActionContext;

/**
 * This is {@link AccountAuditActionContext}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@Accessors(chain = true)
class AccountAuditActionContext extends AuditActionContext {
    @Serial
    private static final long serialVersionUID = 8935451143814878214L;

    private final String json;

    AccountAuditActionContext(final AuditActionContext context, final String json) {
        super(context);
        this.json = json;
    }
}
