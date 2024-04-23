package org.apereo.cas;

import lombok.EqualsAndHashCode;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.data.mongodb.core.MongoOperations;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * This is {@link MongoDbPropertySource}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EqualsAndHashCode(callSuper = true)
public class MongoDbPropertySource extends EnumerablePropertySource<MongoOperations> {

    private final List<MongoDbProperty> list;

    public MongoDbPropertySource(final String context, final MongoOperations mongo) {
        super(context, mongo);
        list = getSource().findAll(MongoDbProperty.class, MongoDbProperty.class.getSimpleName());
    }

    @Nonnull
    @Override
    public String[] getPropertyNames() {
        return list.stream().map(MongoDbProperty::getName).toArray(String[]::new);
    }

    @Override
    public Object getProperty(
        @Nonnull
        final String s) {
        return list.stream()
            .filter(prop -> prop.getName().equals(s))
            .findFirst()
            .map(MongoDbProperty::getValue)
            .orElse(null);
    }
}
