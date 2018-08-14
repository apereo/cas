package org.apereo.cas.uma.ticket.resource;

import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.RandomUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;

/**
 * This is {@link ResourceSetPolicy}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Embeddable
@Table(name = "UMA_ResourceSetPolicy")
@EqualsAndHashCode
public class ResourceSetPolicy implements Serializable {

    private static final long serialVersionUID = 1664113523427391736L;

    @org.springframework.data.annotation.Id
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    private long id;

    @Lob
    @Column
    private HashSet<ResourceSetPolicyPermission> permissions = new HashSet<>();

    public ResourceSetPolicy() {
        id = Math.abs(RandomUtils.getNativeInstance().nextInt());
    }

    /**
     * As json string.
     *
     * @return the string
     */
    @JsonIgnore
    public String toJson() {
        return OAuth20Utils.toJson(this);
    }
}
