package org.apereo.cas.support.saml.idp.metadata.locator;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.idp.metadata.generator.SamlIdPMetadataGenerator;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.ReflectionUtils;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.ArrayList;
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
    protected void initMetadataResolver() throws ComponentInitializationException {
        if (getMetadataRootElement() != null) {
            super.initMetadataResolver();
        }
    }

    @Nonnull
    @Override
    @Retryable(value = ResolverException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000, maxDelay = 5000))
    public Iterable<EntityDescriptor> resolve(final CriteriaSet criteria) throws ResolverException {
        try {
            if (!locator.exists(Optional.empty())) {
                generator.generate(Optional.empty());
            }
            val resource = locator.resolveMetadata(Optional.empty());
            if (resource.contentLength() > 0) {
                val element = SamlUtils.getRootElementFrom(resource.getInputStream(), openSamlConfigBean);

                LOGGER.trace("Located metadata root element [{}]", element.getNodeName());
                setMetadataRootElement(element);

                LOGGER.trace("Initializing metadata resolver [{}]", getClass().getSimpleName());
                initialize();
                LOGGER.trace("Resolving metadata for criteria [{}]", criteria);
                return super.resolve(criteria);
            }
        } catch (final Exception e) {
            throw new ResolverException(e);
        }
        return new ArrayList<>(0);
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

}
