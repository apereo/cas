package org.apereo.cas.utils;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthentication;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.support.TimeoutExpirationPolicy;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TicketCreator {

    public static TicketGrantingTicketImpl expiredTGT(final String id) {
        TicketGrantingTicketImpl tgt = defaultTGT(id);
        tgt.markTicketExpired();
        return tgt;
    }

    public static TicketGrantingTicketImpl defaultTGT(final String id) {
        Map<String, HandlerResult> successes = new HashMap<>();
        successes.put("something", null);
        final CredentialMetaData meta = new BasicCredentialMetaData(new UsernamePasswordCredential());
        ArrayList<CredentialMetaData> credentials = new ArrayList<>();
        credentials.add(meta);
        Authentication defaultAuthentication = new DefaultAuthentication(ZonedDateTime.now(), credentials, NullPrincipal.getInstance(), new HashMap<>(), successes, new HashMap<>());
        return new TicketGrantingTicketImpl(id, defaultAuthentication, new TimeoutExpirationPolicy(3000));
    }
}