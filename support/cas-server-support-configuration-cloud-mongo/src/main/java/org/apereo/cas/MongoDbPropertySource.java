package org.apereo.cas;

import lombok.EqualsAndHashCode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.data.mongodb.core.MongoOperations;
import java.util.List;

/**
 * This is {@link MongoDbPropertySource}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EqualsAndHashCode(callSuper = true)
public class MongoDbPropertySource extends EnumerablePropertySource<@NonNull MongoOperations> {

    private final List<MongoDbProperty> list;

    public MongoDbPropertySource(final String context, final MongoOperations mongo) {
        super(context, mongo);
        list = getSource().findAll(MongoDbProperty.class, MongoDbProperty.class.getSimpleName());
    }

    @NonNull
    @Override
    public String @NonNull [] getPropertyNames() {
        return list.stream().map(MongoDbProperty::getName).toArray(String[]::new);
    }

    @Override
    public @Nullable Object getProperty(@NonNull final String name) {
        return list
            .stream()
            .filter(prop -> prop.getName().equals(name))
            .findFirst()
            .map(MongoDbProperty::getValue)
            .orElse(null);
    }
}
