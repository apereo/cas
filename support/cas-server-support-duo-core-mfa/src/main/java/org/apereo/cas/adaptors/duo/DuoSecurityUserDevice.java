package org.apereo.cas.adaptors.duo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link DuoSecurityUserDevice}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@ToString
@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
public class DuoSecurityUserDevice implements Serializable {
    private static final long serialVersionUID = -6631171454545763954L;

    private final String name;

    private final String type;

    private boolean activated;

    private String lastSeen;

    private String number;

    private String platform;

    private String id;

    private String model;

    private List<String> capabilities = new ArrayList<>();
}
