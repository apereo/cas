package org.apereo.cas.memcached.kryo;

import com.esotericsoftware.kryo.util.Pool;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link CasKryoPool}. It provides pooling while allowing for try-with-resources to be used.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class CasKryoPool extends Pool<CloseableKryo> {
    private static final int POOL_MAX_CAPACITY = 8;

    private final CloseableKryoFactory factory;

    public CasKryoPool(final Collection<Class> classesToRegister,
                       final boolean warnUnregisteredClasses,
                       final boolean registrationRequired,
                       final boolean replaceObjectsByReferences,
                       final boolean autoReset) {
        super(true, false, POOL_MAX_CAPACITY);

        factory = new CloseableKryoFactory(this);
        factory.setWarnUnregisteredClasses(warnUnregisteredClasses);
        factory.setReplaceObjectsByReferences(replaceObjectsByReferences);
        factory.setAutoReset(autoReset);
        factory.setRegistrationRequired(registrationRequired);
        factory.setClassesToRegister(classesToRegister);
    }

    public CasKryoPool() {
        this(new ArrayList<>(), true, true, false, false);
    }

    public CasKryoPool(final Collection<Class> classesToRegister) {
        this(classesToRegister, true, true, false, false);
    }

    /**
     * Borrow kryo.
     *
     * @return the kryo
     */
    public CloseableKryo borrow() {
        return super.obtain();
    }

    /**
     * Release.
     *
     * @param kryo the kryo
     */
    public void release(final CloseableKryo kryo) {
        free(kryo);
    }

    @Override
    @SneakyThrows
    protected CloseableKryo create() {
        return this.factory.createInstance();
    }
}
