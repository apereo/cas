package org.apereo.cas.ticket.registry;

import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jose.util.JSONObjectUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.ticket.DefaultTicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import java.text.ParseException;
import java.util.Optional;

@Slf4j
public class JwtFriendlyTicketCatalog extends DefaultTicketCatalog {

  @Override
  public TicketDefinition find(final String ticketId) {
    val jwtTicketDefinition = jwtTicketDefinition(ticketId);
    return jwtTicketDefinition.orElseGet(() -> super.find(ticketId));
  }

  private Optional<TicketDefinition> jwtTicketDefinition(final String ticketId) {
    val firstDotPos = ticketId.indexOf('.');
    if (firstDotPos != -1) {
      val header = new Base64URL(ticketId.substring(0, firstDotPos));
      try {
        JSONObjectUtils.parse(header.decodeToString());
        // that's enough checking - just assume it is an access token at this point
        LOGGER.debug("Found a JWT looking ticket ID - will assume it is an access token: {}", ticketId);
        return Optional.of(super.find(OAuth20AccessToken.PREFIX));
      } catch (final ParseException e) {
      }
    }
    return Optional.empty();
  }
}
