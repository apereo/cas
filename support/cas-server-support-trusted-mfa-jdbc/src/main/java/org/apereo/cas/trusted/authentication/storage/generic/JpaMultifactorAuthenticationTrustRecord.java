package org.apereo.cas.trusted.authentication.storage.generic;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;

/**
 * This is {@link JpaMultifactorAuthenticationTrustRecord}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity(name = "JpaMultifactorAuthenticationTrustRecord")
@Table(name = "JpaMultifactorAuthenticationTrustRecord")
@Getter
@DiscriminatorValue("JPA")
public class JpaMultifactorAuthenticationTrustRecord extends MultifactorAuthenticationTrustRecord {
    @Serial
    private static final long serialVersionUID = -5834988860677211091L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;

    @Override
    public void setId(final long id) {
        super.setId(id);
        this.id = id;
    }
}
