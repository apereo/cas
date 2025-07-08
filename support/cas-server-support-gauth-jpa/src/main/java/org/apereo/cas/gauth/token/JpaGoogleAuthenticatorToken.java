package org.apereo.cas.gauth.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.Id;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import java.io.Serial;

/**
 * This is {@link JpaGoogleAuthenticatorToken}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity
@Table(name = "GoogleAuthenticatorToken")
@Setter
@Getter
@NoArgsConstructor
public class JpaGoogleAuthenticatorToken extends GoogleAuthenticatorToken {
    @Serial
    private static final long serialVersionUID = 9047539820264192234L;

    @jakarta.persistence.Id
    @Id
    @JsonProperty("id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @SuppressWarnings("UnusedVariable")
    private long id;

    @Override
    public void setId(final long id) {
        super.setId(id);
        this.id = id;
    }
}
