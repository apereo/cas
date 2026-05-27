package org.apereo.cas.consent;

import module java.base;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.scripting.ExecutableCompiledScript;
import org.apereo.cas.util.scripting.ExecutableCompiledScriptFactory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

/**
 * This is {@link GroovyConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GroovyConsentRepository extends BaseSimpleConsentRepository implements DisposableBean {
    @Serial
    private static final long serialVersionUID = 3482998768083902246L;

    private final ExecutableCompiledScript watchableScript;

    public GroovyConsentRepository(final Resource groovyResource) {
        val scriptFactory = ExecutableCompiledScriptFactory.getExecutableCompiledScriptFactory();
        this.watchableScript = scriptFactory.fromResource(groovyResource);
        setConsentDecisions(readDecisionsFromGroovyResource());
    }

    @Override
    public ConsentDecision storeConsentDecision(final ConsentDecision decision) throws Throwable {
        val result = super.storeConsentDecision(decision);
        writeAccountToGroovyResource(decision);
        return result;
    }

    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) throws Throwable {
        super.deleteConsentDecision(decisionId, principal);
        return Boolean.TRUE.equals(watchableScript.execute("delete", Boolean.class, decisionId, principal, LOGGER));
    }

    @Override
    public boolean deleteConsentDecisions(final String principal) throws Throwable {
        super.deleteConsentDecisions(principal);
        return Boolean.TRUE.equals(watchableScript.execute("deletePrincipal", Boolean.class, principal, LOGGER));
    }

    @Override
    public void deleteAll() throws Throwable {
        super.deleteAll();
        watchableScript.execute("deleteAll", Void.class, LOGGER);
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }

    private void writeAccountToGroovyResource(final ConsentDecision decision) throws Throwable {
        watchableScript.execute("write", Boolean.class, decision, LOGGER);
    }

    private Set<ConsentDecision> readDecisionsFromGroovyResource() {
        return FunctionUtils.doUnchecked(
            () -> watchableScript.execute("read", Set.class, getConsentDecisions(), LOGGER));
    }
}
