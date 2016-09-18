package org.apereo.cas;

import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link MongoDbPropertySource}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class MongoDbPropertySource extends EnumerablePropertySource<MongoOperations> {
    private List<MongoDbProperty> list = new ArrayList<>();
    
    public MongoDbPropertySource(final String context, final MongoOperations mongo) {
        super(context, mongo);
    }

    /**
     * Init the collection of properties.
     */
    public void init() {
       list = getSource().findAll(MongoDbProperty.class, MongoDbProperty.class.getSimpleName());
    }
    
    @Override
    public String[] getPropertyNames() {
        return list.stream().map(MongoDbProperty::getName).toArray(String[]::new);
    }
    
    @Override
    public Object getProperty(final String s) {
        final Optional<MongoDbProperty> p = list.stream().filter(prop -> prop.getName().equals(s)).findFirst();
        if (p.isPresent()) {
            return p.get().getValue();
        }
        return null;
    }
}
