package org.apereo.cas.adaptors.yubikey;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This is {@link YubiKeyAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Entity
@Table(name = "YubiKeyAccount")
@Slf4j
@ToString
@Getter
@Setter
@AllArgsConstructor
public class YubiKeyAccount {

    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id = -1;

    @Column(nullable = false, length = 4096)
    private String publicId;

    @Column(nullable = false, unique = true)
    private String username;

    public YubiKeyAccount() {
        this.id = System.currentTimeMillis();
    }
}
