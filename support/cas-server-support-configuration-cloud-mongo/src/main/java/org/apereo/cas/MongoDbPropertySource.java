package org.apereo.cas;

import module java.base;
import org.apereo.cas.configuration.api.MutablePropertySource;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * This is {@link MongoDbPropertySource}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("NullAway.Init")
public class MongoDbPropertySource extends EnumerablePropertySource<MongoOperations>
    implements MutablePropertySource<MongoOperations> {

    private Set<String> propertyNames;

    public MongoDbPropertySource(final String context, final MongoOperations mongo) {
        super(context, mongo);
        refresh();
    }

    @Override
    public void refresh() {
        val list = getSource().findAll(MongoDbProperty.class, MongoDbProperty.class.getSimpleName());
        this.propertyNames = new HashSet<>(list.stream().map(MongoDbProperty::getName).toList());
    }

    @Override
    public void removeAll() {
        val query = Query.query(Criteria.where("name").exists(true));
        getSource().remove(query, MongoDbProperty.class, MongoDbProperty.class.getSimpleName());
        propertyNames.clear();
    }

    @Override
    public void removeProperty(final String name) {
        val query = Query.query(Criteria.where("name").is(name));
        getSource().remove(query, MongoDbProperty.class, MongoDbProperty.class.getSimpleName());
        propertyNames.remove(name);
    }

    @Override
    public String[] getPropertyNames() {
        return propertyNames.toArray(String[]::new);
    }

    @Override
    public @Nullable Object getProperty(final String name) {
        if (propertyNames.contains(name)) {
            val query = Query.query(Criteria.where("name").is(name));
            val prop = getSource().findOne(query, MongoDbProperty.class, MongoDbProperty.class.getSimpleName());
            return prop != null ? Objects.requireNonNull(prop).getValue() : null;
        }
        return null;
    }

    @Override
    @CanIgnoreReturnValue
    public MutablePropertySource setProperty(final String name, final Object value) {
        val query = Query.query(Criteria.where("name").is(name));
        val update = new Update().set("value", value).setOnInsert("name", name);
        getSource().upsert(
            query,
            update,
            MongoDbProperty.class,
            MongoDbProperty.class.getSimpleName()
        );
        propertyNames.add(name);
        return this;
    }
}
