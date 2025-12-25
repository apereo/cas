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
public class MongoDbPropertySource extends EnumerablePropertySource<MongoOperations>
    implements MutablePropertySource<MongoOperations> {

    public MongoDbPropertySource(final String context, final MongoOperations mongo) {
        super(context, mongo);
    }

    @Override
    public String[] getPropertyNames() {
        val list = getSource().findAll(MongoDbProperty.class, MongoDbProperty.class.getSimpleName());
        return list.stream().map(MongoDbProperty::getName).toArray(String[]::new);
    }

    @Override
    public @Nullable Object getProperty(final String name) {
        val query = Query.query(Criteria.where("name").is(name));
        val prop = getSource().findOne(query, MongoDbProperty.class, MongoDbProperty.class.getSimpleName());
        return prop != null ? prop.getValue() : null;
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
        return this;
    }
}
