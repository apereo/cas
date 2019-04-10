package org.apereo.cas.couchdb.audit;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apereo.inspektr.audit.AuditActionContext;

import java.util.Date;

/**
 * This is {@link CouchDbAuditActionContext}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@Setter
public class CouchDbAuditActionContext extends AuditActionContext {
    private static final long serialVersionUID = 5316526142085559254L;

    @JsonProperty("_id")
    private String cid;

    @JsonProperty("_rev")
    private String rev;

    @JsonCreator
    public CouchDbAuditActionContext(@JsonProperty("_id") final String cid, 
                                     @JsonProperty("_rev") final String rev,
                                     @JsonProperty("principal") final String principal,
                                     @JsonProperty("resourceOperatedUpon") final String resourceOperatedUpon,
                                     @JsonProperty("actionPerformed") final String actionPerformed,
                                     @JsonProperty("applicationCode") final String applicationCode,
                                     @JsonProperty("whenActionWasPerformed") final Date whenActionWasPerformed,
                                     @JsonProperty("clientIpAddress") final String clientIpAddress,
                                     @JsonProperty("serverIpAddress") final String serverIpAddress) {
        super(principal, resourceOperatedUpon, actionPerformed, applicationCode, whenActionWasPerformed, clientIpAddress, serverIpAddress);
        this.cid = cid;
        this.rev = rev;
    }

    public CouchDbAuditActionContext(final AuditActionContext context) {
        super(context.getPrincipal(), context.getResourceOperatedUpon(), context.getActionPerformed(), context.getApplicationCode(),
            context.getWhenActionWasPerformed(), context.getClientIpAddress(), context.getServerIpAddress());
    }
}
