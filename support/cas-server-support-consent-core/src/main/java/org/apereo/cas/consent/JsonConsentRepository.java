package org.apereo.cas.consent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.ResourceUtils;
import org.hjson.JsonValue;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link JsonConsentRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JsonConsentRepository implements ConsentRepository {
    private static final long serialVersionUID = -402728451864783825L;

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    private final Resource jsonResource;
    private Set<ConsentDecision> consentDecisions;

    public JsonConsentRepository(final Resource jsonResource) {
        this.jsonResource = jsonResource;
        readDecisionsFromJsonResource();
    }

    @Override
    public ConsentDecision findConsentDecision(final Service service, final RegisteredService registeredService,
                                               final Authentication authentication) {
        return this.consentDecisions
                .stream()
                .filter(d -> d.getPrincipal().equals(authentication.getPrincipal().getId())
                        && d.getService().equals(service.getId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean storeConsentDecision(final ConsentDecision decision) {
        final ConsentDecision consent = this.consentDecisions
                .stream()
                .filter(d -> d.getId() == decision.getId())
                .findFirst()
                .orElse(null);
        if (consent != null) {
            this.consentDecisions.remove(decision);
        } else {
            decision.setId(Math.abs(RandomUtils.getInstanceGood().nextInt()));
        }
        this.consentDecisions.add(decision);
        writeAccountToJsonResource();
        return true;
    }

    private void readDecisionsFromJsonResource() {
        this.consentDecisions = new LinkedHashSet<>();
        if (ResourceUtils.doesResourceExist(jsonResource)) {
            try (Reader reader = new InputStreamReader(jsonResource.getInputStream())) {
                final TypeReference<Set<ConsentDecision>> personList = new TypeReference<Set<ConsentDecision>>() {
                };
                this.consentDecisions = MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    private boolean writeAccountToJsonResource() {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(this.jsonResource.getFile(), this.consentDecisions);
            readDecisionsFromJsonResource();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return true;
    }
}
