package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;

import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This is {@link JpaMultifactorAuthenticationTrustRecord}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity
@Table(name = "MultifactorAuthenticationTrustRecord")
@Getter
public class JpaMultifactorAuthenticationTrustRecord extends MultifactorAuthenticationTrustRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;
}
