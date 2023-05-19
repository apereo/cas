package org.apereo.cas.services.query;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.simple.SimpleQuery;
import lombok.val;

import java.util.stream.StreamSupport;

/**
 * This is {@link IsAssignableFrom}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class IsAssignableFrom<O, A> extends SimpleQuery<O, A> {
    private final Class expectedClass;

    public IsAssignableFrom(final Attribute<O, A> attribute, final Class clazz) {
        super(attribute);
        this.expectedClass = clazz;
    }
    
    @Override
    public String toString() {
        return "assignableFrom(" + asLiteral(expectedClass.getName()) + ')';
    }

    @Override
    protected boolean matchesSimpleAttribute(final SimpleAttribute<O, A> attribute, final O object, final QueryOptions queryOptions) {
        val value = (Class) attribute.getValue(object, queryOptions);
        return value != null && expectedClass.isAssignableFrom(value);
    }

    @Override
    protected boolean matchesNonSimpleAttribute(final Attribute<O, A> attribute, final O object, final QueryOptions queryOptions) {
        return StreamSupport.stream(attribute.getValues(object, queryOptions).spliterator(), false)
            .anyMatch(value -> value != null && expectedClass.isAssignableFrom((Class) value));
    }

    @Override
    protected int calcHashCode() {
        return this.attribute.hashCode();
    }
}
