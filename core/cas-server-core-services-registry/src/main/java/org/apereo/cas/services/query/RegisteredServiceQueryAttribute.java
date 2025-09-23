package org.apereo.cas.services.query;

import org.apereo.cas.services.RegisteredService;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapperImpl;
import java.util.Objects;

/**
 * This is {@link RegisteredServiceQueryAttribute}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class RegisteredServiceQueryAttribute extends SimpleAttribute<RegisteredService, Object> {
    private final Class<? extends RegisteredService> serviceClass;

    private Query query;

    public RegisteredServiceQueryAttribute(final RegisteredServiceQuery query) {
        super(query.getType(), (Class) query.getValue().getClass(), query.getName());
        this.serviceClass = query.getType();

        val classAttribute = new RegisteredServiceQueryAttribute(serviceClass, String.class, "@class");
        this.query = QueryFactory.and(
            QueryFactory.has(this),
            query.isIncludeAssignableTypes()
                ? new IsAssignableFrom<>(classAttribute, serviceClass)
                : QueryFactory.equal(classAttribute, serviceClass),
            QueryFactory.equal(this, query.getValue())
        );
    }

    public RegisteredServiceQueryAttribute(final Class serviceClass,
                                           final Class type, final String name) {
        super(serviceClass, type, name);
        this.serviceClass = serviceClass;
    }

    /**
     * Return the real query.
     *
     * @return the query
     */
    public Query toQuery() {
        return Objects.requireNonNullElseGet(query, () -> QueryFactory.none(serviceClass));
    }

    @Override
    public Object getValue(final RegisteredService service, final QueryOptions queryOptions) {
        val registeredServiceWrapper = new BeanWrapperImpl(serviceClass);
        registeredServiceWrapper.setBeanInstance(service);
        if ("@class".equalsIgnoreCase(getAttributeName())) {
            return service.getClass();
        }
        return registeredServiceWrapper.isReadableProperty(getAttributeName())
            ? registeredServiceWrapper.getPropertyValue(getAttributeName())
            : StringUtils.EMPTY;
    }
}
