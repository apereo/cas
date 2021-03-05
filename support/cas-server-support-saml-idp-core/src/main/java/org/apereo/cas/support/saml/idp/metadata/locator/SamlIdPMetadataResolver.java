package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;
import org.apereo.cas.support.saml.services.SamlRegisteredService;

import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.jooq.lambda.Unchecked;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.ReflectionUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link SamlIdPMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class SamlIdPMetadataResolver extends DOMMetadataResolver {
    private final SamlIdPMetadataLocator locator;

    private final SamlIdPMetadataGenerator generator;

    private final OpenSamlConfigBean openSamlConfigBean;

    public SamlIdPMetadataResolver(final SamlIdPMetadataLocator locator,
        final SamlIdPMetadataGenerator generator,
        final OpenSamlConfigBean openSamlConfigBean) {
        super(null);
        this.locator = locator;
        this.generator = generator;
        this.openSamlConfigBean = openSamlConfigBean;
    }

    @Override
    @Retryable(value = ResolverException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    public Iterable<EntityDescriptor> resolve(final CriteriaSet criteria) {
        return determineFilteringCriteria(criteria)
            .stream()
            .map(Unchecked.function(defn -> resolveMetadata(criteria, defn)))
            .filter(results -> results != null && Iterables.size(results) > 0)
            .findFirst()
            .orElseGet(() -> new ArrayList<>(0));
    }

    @Override
    protected void initMetadataResolver() throws ComponentInitializationException {
        if (getMetadataRootElement() != null) {
            super.initMetadataResolver();
        }
    }

    private Iterable<EntityDescriptor> resolveMetadata(final CriteriaSet criteria,
        final Optional<SamlRegisteredService> registeredService) throws Exception {
        if (!locator.exists(registeredService) && locator.shouldGenerateMetadataFor(registeredService)) {
            generator.generate(registeredService);
        }
        val resource = locator.resolveMetadata(registeredService);
        LOGGER.trace("Resolved metadata resource is [{}]", resource);
        if (resource.contentLength() > 0) {
            val element = SamlUtils.getRootElementFrom(resource.getInputStream(), openSamlConfigBean);

            LOGGER.trace("Located metadata root element [{}]", element.getNodeName());
            setMetadataRootElement(element);

            LOGGER.trace("Initializing metadata resolver [{}]", getClass().getSimpleName());
            initialize();
            LOGGER.trace("Resolving metadata for criteria [{}]", criteria);
            return super.resolve(criteria);
        }
        return null;
    }

    private Element getMetadataRootElement() {
        val field = ReflectionUtils.findField(getClass(), "metadataElement");
        ReflectionUtils.makeAccessible(Objects.requireNonNull(field));
        return (Element) ReflectionUtils.getField(field, this);
    }

    private void setMetadataRootElement(final Element element) {
        var field = ReflectionUtils.findField(getClass(), "metadataElement");
        ReflectionUtils.makeAccessible(Objects.requireNonNull(field));
        ReflectionUtils.setField(field, this, element);

        field = ReflectionUtils.findField(getClass(), "isInitialized");
        ReflectionUtils.makeAccessible(Objects.requireNonNull(field));
        ReflectionUtils.setField(field, this, Boolean.FALSE);
    }

    /**
     * Determine the criteria to resolve idp metadata.
     * If the criteria-set contains a service defn to act as an override,
     * based on the presence of {@link SamlIdPSamlRegisteredServiceCriterion},
     * that service is positioned first in the list. An empty criteria
     * is always added to calculate and resolve metadata globally as the last step,
     * in case an override is not available.
     *
     * @param criteria criteria set
     * @return list of optional service defns
     */
    private static List<Optional<SamlRegisteredService>> determineFilteringCriteria(
        final CriteriaSet criteria) {
        val results = new ArrayList<Optional<SamlRegisteredService>>();
        if (criteria.contains(SamlIdPSamlRegisteredServiceCriterion.class)) {
            val criterion = criteria.get(SamlIdPSamlRegisteredServiceCriterion.class);
            results.add(Optional.of(Objects.requireNonNull(criterion).getRegisteredService()));
        }
        results.add(Optional.empty());
        return results;
    }
}
