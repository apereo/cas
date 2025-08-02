package org.apereo.cas.consent;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;

/**
 * This is {@link JpaConsentDecision}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "ConsentDecision")
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class JpaConsentDecision extends ConsentDecision {
    @Serial
    private static final long serialVersionUID = -1370242034035828803L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id = -1;

    @Override
    public void setId(final long id) {
        super.setId(id);
        this.id = id;
    }
}
