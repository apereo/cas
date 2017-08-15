package org.apereo.cas.persondir.support;

import net.shibboleth.idp.attribute.IdPAttributeValue;
import net.shibboleth.idp.attribute.resolver.AttributeResolver;
import net.shibboleth.idp.attribute.resolver.ResolutionException;
import net.shibboleth.idp.attribute.resolver.context.AttributeResolutionContext;
import org.apereo.services.persondir.IPersonAttributeDao;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.BasePersonAttributeDao;
import org.apereo.services.persondir.support.NamedPersonImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link IPersonAttributeDao} implementation that can use a Shibboleth
 * {@link net.shibboleth.idp.attribute.resolver.AttributeResolver} to resolve attributes.
 *
 * @author Jonathan Johnson
 * @since 5.0.0
 */
public class ShibbolethPersonAttributeDao extends BasePersonAttributeDao {

    private final AttributeResolver attributeResolver;

    public ShibbolethPersonAttributeDao(final AttributeResolver attributeResolver) {
        this.attributeResolver = attributeResolver;
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
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Set<IPersonAttributes> getPeople(final Map<String, Object> query) {
        return new HashSet<>(0);
    }

    @Override
    public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(final Map<String, List<Object>> query) {
        return new HashSet<>(0);
    }

    @Override
    public Set<String> getPossibleUserAttributeNames() {
        return new HashSet<>(0);
    }

    @Override
    public Set<String> getAvailableQueryAttributes() {
        return new HashSet<>(0);
    }
}
