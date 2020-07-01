package org.apache.syncope.common.lib.to;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.syncope.common.lib.Attr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This is {@link UserTO}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@Accessors(chain = true)
public class UserTO implements Serializable {
    private static final long serialVersionUID = 7791304495192615740L;

    private final List<String> dynRealms = new ArrayList<>();

    private final List<RelationshipTO> relationships = new ArrayList<>();

    private final List<MembershipTO> memberships = new ArrayList<>();

    private final List<MembershipTO> dynMemberships = new ArrayList<>();

    private final Set<Attr> plainAttrs = new HashSet<>();

    private final List<String> roles = new ArrayList<>();

    private final List<String> dynRoles = new ArrayList<>();

    private final Set<String> privileges = new HashSet<>();

    private String password;

    private String token;

    private Date tokenExpireTime;

    private String username;

    private Date lastLoginDate;

    private Date changePwdDate;

    private Integer failedLogins;

    private String securityQuestion;

    private String securityAnswer;

    private String type;

    private String realm;

    private String status;

    private String creator;

    private Date creationDate;

    private String creationContext;

    private String lastModifier;

    private Date lastChangeDate;

    private boolean suspended;

    private boolean mustChangePassword;
}
