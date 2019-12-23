package org.apereo.cas.ticket.registry;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;

import java.util.Map;

@WebEndpoint(id = "ticketRegistry")
public class JdbcTicketRegistryEndpoint {

  private JdbcTicketRegistry ticketRegistry;

  public JdbcTicketRegistryEndpoint(JdbcTicketRegistry ticketRegistry) {
    this.ticketRegistry = ticketRegistry;
  }

  @ReadOperation(produces = "application/json")
  public JdbcTicketRegistryStats stats() {
    return new JdbcTicketRegistryStats(ticketRegistry.getStats());
  }

  @DeleteOperation(produces = "application/json")
  public Map<String, Long> deleteTickets(final String principalId) {
    if (principalId != null && principalId.length() > 0) {
      return ticketRegistry.deleteByPrincipalId(principalId);
    } else {
      return ticketRegistry.cleanExpiredTickets();
    }
  }

  @AllArgsConstructor
  @Getter
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public static class JdbcTicketRegistryTableStats {
    private Long count;
  }

  @AllArgsConstructor
  @Getter
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public static class JdbcTicketRegistryStats {

    private Map<String, JdbcTicketRegistryTableStats> stats;

  }

}
