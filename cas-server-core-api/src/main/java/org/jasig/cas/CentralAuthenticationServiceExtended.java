package org.jasig.cas;

import org.jasig.cas.ticket.registry.TicketRegistry;

public interface CentralAuthenticationServiceExtended extends CentralAuthenticationService {
	public TicketRegistry getTicketRegistry();
}
