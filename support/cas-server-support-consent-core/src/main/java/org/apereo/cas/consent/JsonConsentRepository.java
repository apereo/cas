package org.apereo.cas.consent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.util.ResourceUtils;
import org.hjson.JsonValue;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link JsonConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JsonConsentRepository extends BaseConsentRepository {
    private static final long serialVersionUID = -402728417464783825L;

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private final Resource jsonResource;

    public JsonConsentRepository(final Resource jsonResource) {
        this.jsonResource = jsonResource;
        setConsentDecisions(readDecisionsFromJsonResource());
    }
    
    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        final boolean result = super.storeConsentDecision(decision);
        writeAccountToJsonResource();
        return result;
    }
    
    @Override
    public boolean deleteConsentDecision(final long decisionId, final String principal) {
        final boolean result = super.deleteConsentDecision(decisionId, principal);
        writeAccountToJsonResource();
        return result;
    }
    
    private Set<ConsentDecision> readDecisionsFromJsonResource() {
        if (ResourceUtils.doesResourceExist(jsonResource)) {
            try (Reader reader = new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8)) {
                final TypeReference<Set<ConsentDecision>> personList = new TypeReference<Set<ConsentDecision>>() {
                };
                return MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        return new LinkedHashSet<>(0);
    }

    private boolean writeAccountToJsonResource() {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(this.jsonResource.getFile(), getConsentDecisions());
            readDecisionsFromJsonResource();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return true;
    }
}
