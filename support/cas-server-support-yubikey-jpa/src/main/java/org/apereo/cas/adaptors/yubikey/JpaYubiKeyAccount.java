package org.apereo.cas.adaptors.yubikey;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.List;

/**
 * This is {@link JpaYubiKeyAccount}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Getter
@Entity
@Setter
@Table(name = "YubiKeyAccount")
@SuperBuilder
@Accessors(chain = true)
public class JpaYubiKeyAccount extends YubiKeyAccount {
    private static final long serialVersionUID = 8996204730235225057L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Builder.Default
    private long id = -1;

    public JpaYubiKeyAccount() {
        this.id = System.currentTimeMillis();
    }

    public JpaYubiKeyAccount(final long id,
                             final List<YubiKeyRegisteredDevice> deviceIdentifiers,
                             final String username) {
        super(id, deviceIdentifiers, username);
        this.id = id;
    }
}
