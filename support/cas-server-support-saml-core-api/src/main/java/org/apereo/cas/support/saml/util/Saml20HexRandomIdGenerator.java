package org.apereo.cas.support.saml.util;

import module java.base;
import org.apereo.cas.util.gen.HexRandomStringGenerator;

/**
 * This is {@link Saml20HexRandomIdGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class Saml20HexRandomIdGenerator extends HexRandomStringGenerator {
    /**
     * Default instance of the id generator.
     */
    public static final Saml20HexRandomIdGenerator INSTANCE = new Saml20HexRandomIdGenerator();

    private static final int RANDOM_ID_SIZE = 16;

    public Saml20HexRandomIdGenerator() {
        super(RANDOM_ID_SIZE);
    }

    @Override
    public String getNewString() {
        return '_' + super.getNewString();
    }
}
