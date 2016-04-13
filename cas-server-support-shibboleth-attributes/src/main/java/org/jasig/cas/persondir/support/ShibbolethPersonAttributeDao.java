package org.jasig.cas.persondir.support;

import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.component.InitializableComponent;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.NamedPersonImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link IPersonAttributeDao} implementation that can use a Shibboleth
 * {@link net.shibboleth.idp.attribute.resolver.AttributeResolver} to resolve attributes.
 *
 * @author Jj
 * @since 5.0.0
 */
@RefreshScope
@Component("shibbolethPersonAttributeDao")
public class ShibbolethPersonAttributeDao implements IPersonAttributeDao {
    @Autowired
    private AttributeResolver attributeResolver;

    /**
     * Initializes the component. Right now, all it does is makes sure that the attribute resolver is initialized
     */
    @PostConstruct
    public void init() {
        if (this.attributeResolver instanceof InitializableComponent && !((InitializableComponent) this.attributeResolver).isInitialized()) {
            try {
                ((InitializableComponent) this.attributeResolver).initialize();
            } catch (final ComponentInitializationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public IPersonAttributes getPerson(final String uid) {
        final AttributeResolutionContext attributeResolutionContext = new AttributeResolutionContext();
        attributeResolutionContext.setPrincipal(uid);

        try {
            this.attributeResolver.resolveAttributes(attributeResolutionContext);

            final Map<String, List<Object>> attributes = attributeResolutionContext.getResolvedIdPAttributes()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            p -> p.getValue().getValues().stream().map(IdPAttributeValue::getValue).collect(Collectors.toList()))
                    );

            return new NamedPersonImpl(uid, attributes);
        } catch (final ResolutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Does nothing. Look elsewhere.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> query) {
        throw new UnsupportedOperationException();
    }

    /**
     * Does nothing. Look elsewhere.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query) {
        throw new UnsupportedOperationException();
    }

    /**
     * Does nothing. Look elsewhere.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Set<String> getPossibleUserAttributeNames() {
        throw new UnsupportedOperationException();
    }

    /**
     * Does nothing. Look elsewhere.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Set<String> getAvailableQueryAttributes() {
        throw new UnsupportedOperationException();
    }

    /**
     * Does nothing. Look elsewhere.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<Object>> getMultivaluedUserAttributes(final Map<String, List<Object>> seed) {
        throw new UnsupportedOperationException();
    }

    /**
     * Does nothing. Look elsewhere.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Map<String, List<Object>> getMultivaluedUserAttributes(final String uid) {
        throw new UnsupportedOperationException();
    }

    /**
     * Does nothing. Look elsewhere.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getUserAttributes(final Map<String, Object> seed) {
        throw new UnsupportedOperationException();
    }

    /**
     * Does nothing. Look elsewhere.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> getUserAttributes(final String uid) {
        throw new UnsupportedOperationException();
    }
}
