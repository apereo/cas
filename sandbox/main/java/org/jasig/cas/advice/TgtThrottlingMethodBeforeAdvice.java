/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.advice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.springframework.aop.MethodBeforeAdvice;

/**
 * Prevents a single TGT from obtaining too many service tickets in too little
 * time. A broken, or adversarial, CAS-using service or end user client could
 * re-present to CAS server a valid TGT over and over again very rapidly,
 * thereby consuming an "unfair" amount of CAS server resources and degrading
 * performance for other users. Used properly, CAS-using services do not need to
 * redirect the end user back to CAS so very often - generally, once for each
 * service the end user is trying to use. This Advice is intended to reserve
 * resources for such proper CAS-using services and end users and block the use
 * of CAS resources beyond what can reasonably occur with well-behaved clients
 * and CAS-using services.
 * <p>
 * This Advice is intended to be applied in front of a
 * {@link CentralAuthenticationService} instance to add checking that a TGT not
 * be used too often. If a particular TGT wears out its welcome, this Advice
 * will abort the method call by throwing {@link TgtOveruseException}. This
 * exception, or a superclass, should be mapped to an appropriate error
 * response. This advice is intended for use on the CentralAuthenticationService
 * grantServiceTicket methods.
 * </p>
 * <p>
 * Policy is implemented in terms of "unexpired TGT presentations". Each time a
 * TGT is presented, it accumulates one unexpired TGT presentation. Each
 * {@link #expirationIntervalSeconds} (default 60) we expire
 * {@link #expireQuantity} (default 60) TGT presentations. If a TGT presentation
 * would cause the qauntity of unexpired presentations for a particular TGT to
 * exceed {@link #quantityAllowed} (default 60), then we block the TGT
 * presentation (do not charge it as a presentation) and throw
 * {@link TgtOveruseException} in the hopes of prompting an appropriate
 * response.
 * </p>
 * <p>
 * Under default policy, a TGT would have to be presented to CAS server 60 times
 * within one minute to trigger the blocking response.
 * </p>
 * <p>
 * Use of this advice introduces a synchronization cost on every TGT presentation and intermittent
 * delays while the cleanup thread runs to expire presentations.
 * </p>
 * 
 * @version $Revision$ $Date$
 */
public class TgtThrottlingMethodBeforeAdvice implements MethodBeforeAdvice {

    /**
     * The number of unexpired TGT presentations that are to be allowed before
     * we start blocking.
     */
    private int quantityAllowed = 60;

    /**
     * Number of seconds we should allow to elapse between expiring presentations of TGTs.
     * Changes to this field will take effect upon conclusion of the currently running interval.
     */
    private int expirationIntervalSeconds = 60;

    /**
     * The number of TGT presentations we should expire for each TGT each
     * expiration interval.
     */
    private int expireQuantity = 60;
    
    private Map unexpiredTgtPresentations = new HashMap();

    private Thread unexpiredTgtPresentationsExiprationThread = new Thread() {
        public void run() {
            try {
              while (true) {
                Thread.sleep(expirationIntervalSeconds * 1000);
                expireTgtPresentations();
              }
            } catch (InterruptedException ex) {
              // ignore
            }
          }
    };
    
    public TgtThrottlingMethodBeforeAdvice() {
        this.unexpiredTgtPresentationsExiprationThread.setDaemon(true);
        this.unexpiredTgtPresentationsExiprationThread.start();
    }
    

    
    public void before(Method method, Object[] arguments, Object target)
        throws Throwable {

        String tgtId = (String) arguments[0];
        Service service = (Service) arguments[1];
        
        synchronized (this) {
            if (getUnexpiredTgtPresentations(tgtId) >= this.quantityAllowed) {
                throw new TgtOveruseException(tgtId, service);
            } else {
                registerTgtPresentation(tgtId);
            }
        }

    }
    
    /**
     * Method our expiration thread invokes to expire TGT presentations.
     * Synchronized so that we do not access the Map in an inconsistent state and so that nothing
     * attempts to read the map as we modify it.
     */
    private synchronized void expireTgtPresentations() {
        // scoop up presented TGT IDs from Map so as to avoid modifying the Map in-place
        Set tgtIds = this.unexpiredTgtPresentations.keySet();
        Iterator ki = tgtIds.iterator();

        // now, decrement and prune as appropriate
        while (ki.hasNext()) {
          String tgtId = (String) ki.next();
          int remainingUnexpiredTgtPresentations = getUnexpiredTgtPresentations(tgtId) - this.expireQuantity;
          if (remainingUnexpiredTgtPresentations > 0) {
              // after expiry, there are still active presentations we need to remember
            this.unexpiredTgtPresentations.put(tgtId, new Integer(remainingUnexpiredTgtPresentations));
          } else {
              // after expiry, there are no remaining active presentations for this TGT that we need to remember.
            this.unexpiredTgtPresentations.remove(tgtId);
          }
        }
    }
    
    /** 
     * Returns the number of "active" presentations for the given TGT ID. 
     * Synconrized so that we do not access the Map in an inconsistent state.
     * @param tgtId the tgtId for which we seen the active presentation count
     * @return the number of unexpired presentations for the given tgt ID.
     */
    private synchronized int getUnexpiredTgtPresentations(String tgtId) {
      Object o = this.unexpiredTgtPresentations.get(tgtId);
      if (o == null) {
        return 0;
      } else {
        return ((Integer) o).intValue();
      }
    }
    
    /**
     * Increments the recorded number of presentations of a particular TGT id, creating a new entry in our backing Map
     * as necessary.
     * @param tgtId Presented ticket granting ticket identifier.
     */
    private synchronized void registerTgtPresentation(String tgtId) {
        this.unexpiredTgtPresentations.put(tgtId, new Integer(getUnexpiredTgtPresentations(tgtId) + 1));
      }


    /**
     * Get the number of seconds between our expiring tgtId presentations.
     * @return seconds between expiring tgtid presentations
     */
    public int getExpirationIntervalSeconds() {
        return expirationIntervalSeconds;
    }

    /**
     * Set the number of seconds between expirations of tgtId presentations.
     * Changes to the expiration interval take effect at the conclusion of the
     * interval currently in progress (i.e., when the thread wakes up, does its work, 
     * and then goes to sleep again for the then-current interval.)
     * @param expirationIntervalSeconds seconds between expiring tgtid presentations
     */
    public void setExpirationIntervalSeconds(int expirationIntervalSeconds) {
        this.expirationIntervalSeconds = expirationIntervalSeconds;
    }
    
    
    /**
     * Get the number of presentations of each ticket granting ticket id that we will expire
     * each time we expire tgtid presentations.
     * @return number of tgtid presentations expired each interval
     */
    public int getExpireQuantity() {
        return expireQuantity;
    }

    /**
     * Set the number of presentations of each ticket granting ticket id that we will expire
     * each time we expire tgtid presentations.
     * @param expireQuantity number of tgtid presentations to expire at a time
     */
    public void setExpireQuantity(int expireQuantity) {
        this.expireQuantity = expireQuantity;
    }

    
    /**
     * Get the number of active tgtid presentations this Advice will allow before it blocks
     * an attempt to use a tgt id.
     * @return allowed quantity of unexpired tgtid presentations.
     */
    public int getQuantityAllowed() {
        return quantityAllowed;
    }

    /**
     * Set the number of active tgtID presentations this Advice will allow before it blocks
     * an attempt to use a tgt id.
     * @param quantityAllowed allowed quantity of active tgt ID presentations.
     */
    public void setQuantityAllowed(int quantityAllowed) {
        this.quantityAllowed = quantityAllowed;
    }

}
