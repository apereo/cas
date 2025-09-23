package org.apereo.cas.trusted.authentication.storage.oracle;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.io.Serial;

/**
 * This is {@link OracleJpaMultifactorAuthenticationTrustRecord}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@NoArgsConstructor
@AttributeOverrides({
    @AttributeOverride(name = "recordKey", column = @Column(columnDefinition = "varchar2(4000)")),
    @AttributeOverride(name = "name", column = @Column(columnDefinition = "varchar2(4000)")),
    @AttributeOverride(name = "principal", column = @Column(columnDefinition = "varchar2(2048)"))
})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Entity(name = "JpaMultifactorAuthenticationTrustRecord")
@Getter
@DiscriminatorValue("ORACLE")
public class OracleJpaMultifactorAuthenticationTrustRecord extends MultifactorAuthenticationTrustRecord {
    @Serial
    private static final long serialVersionUID = 653723293231219680L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Override
    public void setId(final long id) {
        super.setId(id);
        this.id = id;
    }
}
