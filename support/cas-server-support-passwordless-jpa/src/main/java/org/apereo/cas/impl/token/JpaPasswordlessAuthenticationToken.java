package org.apereo.cas.impl.token;

import lombok.Getter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This is {@link JpaPasswordlessAuthenticationToken}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Entity
@Table(name = "PasswordlessAuthenticationToken")
@Getter
public class JpaPasswordlessAuthenticationToken extends PasswordlessAuthenticationToken {
    private static final long serialVersionUID = -6830552508331229032L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;
}
