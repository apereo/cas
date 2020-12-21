package org.apereo.cas.memcached.kryo;

import com.esotericsoftware.kryo.util.Pool;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link CasKryoPool}. It provides pooling while allowing for try-with-resources to be used.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class CasKryoPool extends Pool<CloseableKryo> implements KryoPool<CloseableKryo> {
    private static final int CAPACITY = 1024;

    private final CloseableKryoFactory factory;

    public CasKryoPool() {
        this(new ArrayList<>(0), true, true, false, false);
    }

    public CasKryoPool(final Collection<Class> classesToRegister, final boolean warnUnregisteredClasses,
        final boolean registrationRequired, final boolean replaceObjectsByReferences,
        final boolean autoReset) {
        super(true, false, CAPACITY);

        factory = new CloseableKryoFactory(this);
        factory.setWarnUnregisteredClasses(warnUnregisteredClasses);
        factory.setReplaceObjectsByReferences(replaceObjectsByReferences);
        factory.setAutoReset(autoReset);
        factory.setRegistrationRequired(registrationRequired);
        factory.setClassesToRegister(classesToRegister);
    }

    @Override
    public CloseableKryo borrow() {
        return obtain();
    }

    @Override
    public void release(final CloseableKryo kryo) {
        free(kryo);
    }

    @Override
    protected CloseableKryo create() {
        return factory.getObject();
    }
}
