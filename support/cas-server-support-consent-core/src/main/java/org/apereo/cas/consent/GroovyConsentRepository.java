package org.apereo.cas.consent;

import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.springframework.core.io.Resource;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link GroovyConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GroovyConsentRepository extends BaseConsentRepository {
    private static final long serialVersionUID = 4882998768083902246L;
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

    private void writeAccountToGroovyResource(final ConsentDecision decision) {
        ScriptingUtils.executeGroovyScript(groovyResource, "write", Boolean.class, decision);
    }

    private Set<ConsentDecision> readDecisionsFromGroovyResource() {
        return ScriptingUtils.executeGroovyScript(groovyResource, "read", Set.class, getConsentDecisions());
    }
}
