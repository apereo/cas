package org.apereo.cas.ticket.refreshtoken;

import com.google.common.collect.ImmutableMap;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenImpl;
import org.apereo.cas.ticket.code.OAuthCodeImpl;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An OAuth refresh token implementation.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Entity
@DiscriminatorValue(RefreshToken.PREFIX)
public class RefreshTokenImpl extends OAuthCodeImpl implements RefreshToken {

    private static final long serialVersionUID = 4310599856914389467L;

    /**
     * The services associated to this ticket.
     */
    @Lob
    @Column(name = "ACCESS_TOKEN_GRANTED_TO", nullable = false, length = Integer.MAX_VALUE)
    @OneToMany(targetEntity = AccessTokenImpl.class, mappedBy = "ticketGrantingTicket")
    @MapKey(name = "id")
    private Map<String, AccessToken> accessTokenHashMap = new HashMap<>();


    /**
     * Flag to enforce manual expiration.
     */
    @Column(name = "EXPIRED")
    private Boolean expired = Boolean.FALSE;

    /**
     * Instantiates a new OAuth refresh token.
     */
    public RefreshTokenImpl() {
        // exists for JPA purposes
    }

    /**
     * Constructs a new refresh token with unique id for a service and authentication.
     *
     * @param id               the unique identifier for the ticket.
     * @param service          the service this ticket is for.
     * @param authentication   the authentication.
     * @param expirationPolicy the expiration policy.
     * @throws IllegalArgumentException if the service or authentication are null.
     */
    public RefreshTokenImpl(final String id,
                            final Service service, final Authentication authentication,
                            final ExpirationPolicy expirationPolicy) {
        super(id, service, authentication, expirationPolicy);
    }

    /**
     * Update service and track session.
     *
     * @param id                         the id
     * @param accessToken                the accessToken
     * @param onlyTrackMostRecentSession the only track most recent session
     */
    protected void updateStateAndTrackServiceSession(final String id, final AccessToken accessToken, final boolean onlyTrackMostRecentSession) {
        update();
        this.getAccessTokenHashMap().put(id, accessToken);
    }

    @Override
    public ServiceTicket grantServiceTicket(String id, Service service, ExpirationPolicy expirationPolicy, Authentication currentAuthentication, boolean onlyTrackMostRecentSession) {
        AccessTokenImpl accessToken = new AccessTokenImpl(id, this.getService(), this.getAuthentication(), expirationPolicy);
        accessToken.setRefreshToken(this);
        updateStateAndTrackServiceSession(id, accessToken, onlyTrackMostRecentSession);
        return accessToken;
    }

    @Override
    public Map<String, Service> getServices() {
        return ImmutableMap.copyOf(this.accessTokenHashMap);
    }

    @Override
    public Collection<ProxyGrantingTicket> getProxyGrantingTickets() {
        return null;
    }

    @Override
    public void removeAllServices() {
        this.accessTokenHashMap.clear();
    }

    @Override
    public void markTicketExpired() {
        this.expired = Boolean.TRUE;
    }

    @Override
    protected boolean isExpiredInternal() {
        return this.expired;
    }

    @Override
    public boolean isRoot() {
        return true;
    }

    @Override
    public TicketGrantingTicket getRoot() {
        return this;
    }

    @Override
    public List<Authentication> getChainedAuthentications() {
        final List<Authentication> list = new ArrayList<>();
        list.add(getAuthentication());
        return Collections.unmodifiableList(list);
    }

    @Override
    public Service getProxiedBy() {
        return null;
    }

    public Map<String, AccessToken> getAccessTokenHashMap() {
        return accessTokenHashMap;
    }

    public void setAccessTokenHashMap(Map<String, AccessToken> accessTokenHashMap) {
        this.accessTokenHashMap = accessTokenHashMap;
    }
}
