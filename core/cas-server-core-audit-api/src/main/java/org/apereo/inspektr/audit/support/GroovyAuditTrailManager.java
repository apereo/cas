package org.apereo.inspektr.audit.support;

import org.apereo.inspektr.audit.AuditActionContext;
import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import java.io.File;
import java.util.HashMap;

/**
 * {@link org.apereo.inspektr.audit.AuditTrailManager} that dumps auditable information to a configured logger
 * prepped by a Groovy script and template.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
public class GroovyAuditTrailManager extends Slf4jLoggingAuditTrailManager {
    private final Template groovyTemplate;
    private final ApplicationContext applicationContext;

    public GroovyAuditTrailManager(final File groovyTemplate, final ApplicationContext applicationContext) throws Exception {
        this.groovyTemplate = new GStringTemplateEngine().createTemplate(groovyTemplate);
        this.applicationContext = applicationContext;
    }

    @Override
    protected String toString(final AuditActionContext auditActionContext) {
        val map = new HashMap<String, Object>(auditActionContext.getClientInfo().getHeaders());
        map.putAll(auditActionContext.getClientInfo().getExtraInfo());
        map.putAll(getMappedAuditActionContext(auditActionContext));
        map.put("applicationContext", applicationContext);
        map.put("logger", LOGGER);
        return groovyTemplate.make(map).toString();
    }
}
