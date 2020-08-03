package org.apereo.cas.trusted.authentication.storage.oracle;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * This is {@link OracleJpaMultifactorAuthenticationTrustRecord}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@NoArgsConstructor
@AttributeOverrides({
    @AttributeOverride(
        name = "recordKey",
        column = @Column(columnDefinition = "varchar2(4000)")
    ),
    @AttributeOverride(
        name = "name",
        column = @Column(columnDefinition = "varchar2(4000)")
    ),
    @AttributeOverride(
        name = "principal",
        column = @Column(columnDefinition = "varchar2(2048)")
    )
})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Entity(name = "JpaMultifactorAuthenticationTrustRecord")
@Getter
@DiscriminatorValue("ORACLE")
public class OracleJpaMultifactorAuthenticationTrustRecord extends MultifactorAuthenticationTrustRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;
}
