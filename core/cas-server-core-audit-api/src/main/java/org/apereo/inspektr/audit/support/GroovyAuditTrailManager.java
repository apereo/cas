package org.apereo.inspektr.audit.support;

import org.apereo.inspektr.audit.AuditActionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import org.springframework.context.ApplicationContext;

/**
 * {@link org.apereo.inspektr.audit.AuditTrailManager} that dumps auditable information to a configured logger
 * prepped by a Groovy script and template.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class GroovyAuditTrailManager extends Slf4jLoggingAuditTrailManager {
    private final File groovyTemplate;
    private final ApplicationContext applicationContext;
    
    @Override
    protected String toString(final AuditActionContext auditActionContext) {
        val map = new HashMap<String, Object>(auditActionContext.getClientInfo().getHeaders());
        map.putAll(auditActionContext.getClientInfo().getExtraInfo());
        map.putAll(getMappedAuditActionContext(auditActionContext));
        map.put("applicationContext", applicationContext);
        map.put("logger", LOGGER);

        return FunctionUtils.doUnchecked(() -> {
            val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
            return scriptFactory.createTemplate(groovyTemplate, map);
        });

    }
}
