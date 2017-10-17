package org.apereo.cas.consent;

import org.apereo.cas.util.ScriptingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.Set;

/**
 * This is {@link GroovyConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GroovyConsentRepository extends BaseConsentRepository {
    private static final long serialVersionUID = 3482998768083902246L;
    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyConsentRepository.class);
    
    private final Resource groovyResource;

    public GroovyConsentRepository(final Resource groovyResource) {
        this.groovyResource = groovyResource;
        setConsentDecisions(readDecisionsFromGroovyResource());
    }
    
    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        final boolean result = super.storeConsentDecision(decision);
        writeAccountToGroovyResource(decision);
        return result;
    }
    
    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        return ScriptingUtils.executeGroovyScript(groovyResource, "delete", Boolean.class, decisionId, principal, LOGGER);
    }
    
    private void writeAccountToGroovyResource(final ConsentDecision decision) {
        ScriptingUtils.executeGroovyScript(groovyResource, "write", Boolean.class, decision, LOGGER);
    }

    private Set<ConsentDecision> readDecisionsFromGroovyResource() {
        return ScriptingUtils.executeGroovyScript(groovyResource, "read", Set.class, getConsentDecisions(), LOGGER);
    }
}
