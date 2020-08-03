package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.jpa.AbstractJpaEntityFactory;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.storage.generic.JpaMultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.storage.oracle.OracleJpaMultifactorAuthenticationTrustRecord;

/**
 * This is {@link JpaMultifactorAuthenticationTrustRecordEntityFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class JpaMultifactorAuthenticationTrustRecordEntityFactory extends AbstractJpaEntityFactory<MultifactorAuthenticationTrustRecord> {
    public JpaMultifactorAuthenticationTrustRecordEntityFactory(final String dialect) {
        super(dialect);
    }

    @Override
    public Class<MultifactorAuthenticationTrustRecord> getType() {
        return (Class<MultifactorAuthenticationTrustRecord>) buildEntityTypeClass();
    }

    private Class<? extends MultifactorAuthenticationTrustRecord> buildEntityTypeClass() {
        if (isOracle()) {
            return OracleJpaMultifactorAuthenticationTrustRecord.class;
        }
        return JpaMultifactorAuthenticationTrustRecord.class;
    }
}
