package org.apereo.cas.uma.ticket.resource;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
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

    @Column
    private String name;

    @Lob
    @Column
    private HashSet<String> claimsRequired = new HashSet<>();

    @Lob
    @Column
    private HashSet<String> scopes = new HashSet<>();

    public ResourceSetPolicy() {
        id = System.currentTimeMillis();
    }
}
