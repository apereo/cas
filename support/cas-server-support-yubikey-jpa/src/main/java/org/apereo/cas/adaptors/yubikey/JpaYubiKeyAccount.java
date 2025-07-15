package org.apereo.cas.adaptors.yubikey;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.io.Serial;

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
@NoArgsConstructor
public class JpaYubiKeyAccount extends YubiKeyAccount {
    @Serial
    private static final long serialVersionUID = 8996204730235225057L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Builder.Default
    private long id = -1;

    @Override
    public YubiKeyAccount setId(final long id) {
        super.setId(id);
        this.id = id;
        return this;
    }
}
