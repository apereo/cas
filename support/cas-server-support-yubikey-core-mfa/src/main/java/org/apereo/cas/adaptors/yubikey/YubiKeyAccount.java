package org.apereo.cas.adaptors.yubikey;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This is {@link YubiKeyAccount}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Entity
@Table(name = "YubiKeyAccount")
@ToString
@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(of = {"id", "username"})
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class YubiKeyAccount implements Serializable {
    /**
     * uername field.
     */
    public static final String FIELD_USERNAME = "username";

    /**
     * Device identifiers field.
     */
    public static final String FIELD_DEVICE_IDENTIFIERS = "deviceIdentifiers";

    private static final long serialVersionUID = 311869140885521905L;

    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @JsonProperty
    private long id = -1;

    @Lob
    @Column(name = "deviceIdentifiers", length = Integer.MAX_VALUE)
    @JsonProperty
    private ArrayList<String> deviceIdentifiers = new ArrayList<>(0);

    @Column(nullable = false)
    @JsonProperty
    private String username;

    public YubiKeyAccount() {
        this.id = System.currentTimeMillis();
    }

    /**
     * Register device.
     *
     * @param device the device
     */
    @JsonIgnore
    public void registerDevice(final String device) {
        this.deviceIdentifiers.add(device);
    }
}
