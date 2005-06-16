package edu.yale.its.tp.cas.auth.provider;

import java.util.*;
import edu.yale.its.tp.cas.auth.*;

/**
 * A PasswordHandler base class that implements logic to block IP addresses
 * that engage in too many unsuccessful login attempts.  The goal is to
 * limit the damage that a dictionary-based password attack can achieve.
 * We implement this with a token-based strategy; failures are regularly
 * forgotten, and only build up when they occur faster than expiry.
 * 
 * Note that it is possible to extend this abstract class without taking advantage of any
 * of the watchful behavior it is trying to offer.  In particular, an implementer extending
 * this class must take care to call super.authenticate() and fail the authentication if
 * that call returns false.  Also, an implementation extending this class must
 * explicitly registerFailure() for every failed authentication attempt.
 * 
 * This class is deprecated because it is part of an API that is deprecated.
 * 
 * @since CAS 2.0
 * @version $Revision$ $Date$
 * @deprecated Use the CAS 3 authentication APIs.
 */
public abstract class WatchfulPasswordHandler implements PasswordHandler {

  //*********************************************************************
  // Constants

  /**
   * The number of unexpired failed login attempts to allow before locking out
   * the source IP address.  (Note that failed login attempts "expire" regularly.)
   */
  private static final int FAILURE_THRESHOLD = 100;

  /**
   * The interval, in seconds, to wait between expiring one failed authentication
   * attempt for each IP address from which failed attempts are originating.
   * 
   * That is, if this failure timeout is sixy, then we will 'forgive' each IP address one
   * failed authentication attempt every minute.
   */
  private static final int FAILURE_TIMEOUT = 60;


  //*********************************************************************
  // Private state

  /** 
   * Map from IP addresses to their number of unexpired authentication failures. 
   * Keys in this map a Strings from request.getRemoteAddress(), dotted quads.
   * Values in this map are Integers.
   */
  private static Map offenders = new HashMap();

  /** 
   * Thread to periodically expire recorded authentication failures. 
   * Every FAILURE_TIMEOUT seconds, this thread will 'forgive' one failed authentication
   * for each IP address from which failed authentications are originating.
   */
  private static Thread offenderThread = new Thread() {
    public void run() {
      try {
        while (true) {
          Thread.sleep(FAILURE_TIMEOUT * 1000);
          expireFailures();
        }
      } catch (InterruptedException ex) {
        // ignore
      }
    }
  };

   static {
     offenderThread.setDaemon(true);
     offenderThread.start();
   }

  //*********************************************************************
  // Gating logic

  /**
   * Returns true if the given request comes from an IP address whose
   * allotment of failed login attemps is within reasonable bounds; 
   * false otherwise.  Note:  We don't actually validate the user
   * and password; this functionality must be implemented by subclasses.
   * 
   * Implementers should override this method and in the implementation of this method
   * call this method via super.  If this method returns false, subclasses should also return
   * false.
   * 
   * @return false if the remote address of the request has had too many failures
   * recently, true otherwise.
   */
  public synchronized boolean authenticate(javax.servlet.ServletRequest request,
                              String netid,
                              String password) {
    return (getFailures(request.getRemoteAddr()) < FAILURE_THRESHOLD);
  }

  /** Registers a login failure initiated by the given address. */
  protected synchronized void registerFailure(javax.servlet.ServletRequest r) {
    String address = r.getRemoteAddr();
    offenders.put(address, new Integer(getFailures(address) + 1));
  }

  /** Returns the number of "active" failures for the given address. */
  private synchronized static int getFailures(String address) {
    Object o = offenders.get(address);
    if (o == null)
      return 0;
    else
      return ((Integer) o).intValue();
  }

  /**
   * Removes one failure record from each offender; if any offender's
   * resulting total is zero, remove it from the list.
   */
  private synchronized static void expireFailures() {
    // scoop up addresses from Map so as to avoid modifying the Map in-place
    Set keys = offenders.keySet();
    Iterator ki = keys.iterator();
    List l = new ArrayList();
    while (ki.hasNext())
      l.add(ki.next());

    // now, decrement and prune as appropriate
    for (int i = 0; i < l.size(); i++) {
      String address = (String) l.get(i);
      int failures = getFailures(address) - 1;
      if (failures > 0)
        offenders.put(address, new Integer(failures));
      else
        offenders.remove(address);
    }
  }

}
