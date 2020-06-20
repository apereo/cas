package org.apereo.cas.gauth.credential;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This is {@link JpaGoogleAuthenticatorAccount}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity
@Table(name = "GoogleAuthenticatorRegistrationRecord")
@Getter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@SuperBuilder
public class JpaGoogleAuthenticatorAccount extends GoogleAuthenticatorAccount {
    private static final long serialVersionUID = -4546447152725241946L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Builder.Default
    private long id = -1;
}
