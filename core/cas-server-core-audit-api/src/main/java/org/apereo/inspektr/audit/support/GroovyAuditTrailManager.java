package org.apereo.inspektr.audit.support;

import org.apereo.inspektr.audit.AuditActionContext;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import lombok.val;
import java.io.File;
import java.util.HashMap;

/**
 * {@link org.apereo.inspektr.audit.AuditTrailManager} that dumps auditable information to a configured logger
 * prepped by a Groovy script and template.
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class GroovyAuditTrailManager extends Slf4jLoggingAuditTrailManager {
    private final Template groovyTemplate;

    public GroovyAuditTrailManager(final String groovyTemplate) throws Exception {
        this.groovyTemplate = new SimpleTemplateEngine().createTemplate(groovyTemplate);
    }

    public GroovyAuditTrailManager(final File groovyTemplate) throws Exception {
        this.groovyTemplate = new SimpleTemplateEngine().createTemplate(groovyTemplate);
    }

    @Override
    protected String toString(final AuditActionContext auditActionContext) {
        val map = new HashMap<String, Object>(auditActionContext.getClientInfo().getHeaders());
        map.putAll(auditActionContext.getClientInfo().getExtraInfo());
        map.putAll(getMappedAuditActionContext(auditActionContext));
        return groovyTemplate.make(map).toString();
    }
}
