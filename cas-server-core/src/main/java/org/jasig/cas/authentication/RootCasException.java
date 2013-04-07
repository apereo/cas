/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.authentication;

import org.springframework.util.Assert;

/**
 * Generic CAS exception that sits at the top of the exception hierarchy. Provides
 * unified logic around retrieval and configuration of exception codes that may be
 * mapped inside an external resource bundle for internationalization of error messages.
 *
 * @author Misagh Moayyed
 * @see org.jasig.cas.authentication.handler.AuthenticationException
 * @see org.jasig.cas.ticket.TicketException
 * @since 4.0.0
 */
public abstract class RootCasException extends Exception {

  private static final long serialVersionUID = -2384466176716541689L;

  /** The code description of the exception. */
  private String code;

  /**
   * Constructor that takes a <code>code</code> description of the error along with the exception
   * <code>msg</code> generally for logging purposes. These codes normally have a corresponding
   * entries in the messages file for the internationalization of error messages.
   *
   * @param code the code to describe what type of exception this is.
   */
  public RootCasException(final String code) {
      initException(code);
  }

  /**
   * Constructs a new exception with the code identifying the exception
   * and the error message.
   *
   * @param code the code to describe what type of exception this is.
   * @param msg The error message associated with this exception for additional logging purposes.
   */
  public RootCasException(final String code, final String msg) {
      super(msg);
      initException(code);
  }

  /**
   * Constructs a new exception with the code identifying the exception
   * and the original throwable.
   *
   * @param code the code to describe what type of exception this is.
   * @param throwable the original exception we are chaining.
   */
  public RootCasException(final String code, final Throwable throwable) {
      super(throwable);
      initException(code);
  }

  /**
   * @return Returns the code. If there is a chained exception it recursively
   * calls {@link #getCode()} of the chained exception rather than the returning
   * the code itself.
   */
  public final String getCode() {
      final Throwable cause = this.getCause();
      if (cause != null && (cause instanceof RootCasException)) {
        return ((RootCasException) cause).getCode();
      }
      return this.code;
  }

  private void initException(final String code) {
      Assert.hasLength(code, "The exception code cannot be blank");
      this.code = code;
  }

  @Override
  public String toString() {
    return this.getCode();
  }
}
