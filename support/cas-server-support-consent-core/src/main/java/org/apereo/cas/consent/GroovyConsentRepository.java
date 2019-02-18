package org.apereo.cas.consent;

import org.apereo.cas.util.scripting.WatchableGroovyScriptResource;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

import java.util.Set;

/**
 * This is {@link GroovyConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class GroovyConsentRepository extends BaseConsentRepository implements DisposableBean {
    private static final long serialVersionUID = 3482998768083902246L;

    private final transient WatchableGroovyScriptResource watchableScript;

    public GroovyConsentRepository(final Resource groovyResource) {
        this.watchableScript = new WatchableGroovyScriptResource(groovyResource);
        setConsentDecisions(readDecisionsFromGroovyResource());
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        val result = super.storeConsentDecision(decision);
        writeAccountToGroovyResource(decision);
        return result;
    }

    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        super.deleteConsentDecision(decisionId, principal);
        return watchableScript.execute("delete", Boolean.class, decisionId, principal, LOGGER);
    }

    private void writeAccountToGroovyResource(final ConsentDecision decision) {
        watchableScript.execute("write", Boolean.class, decision, LOGGER);
    }

    private Set<ConsentDecision> readDecisionsFromGroovyResource() {
        return watchableScript.execute("read", Set.class, getConsentDecisions(), LOGGER);
    }

    @Override
    public void destroy() {
        this.watchableScript.close();
    }
}
