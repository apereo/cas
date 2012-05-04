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
package org.jasig.cas.monitor;

import javax.validation.constraints.NotNull;
import java.util.concurrent.*;

/**
 * Describes a monitor that observes a pool of resources.
 *
 * @author Marvin S. Addison
 * @version $Revision: $
 */
public abstract class AbstractPoolMonitor extends AbstractNamedMonitor<PoolStatus> {


    /** Maximum amount of time in ms to wait while validating pool resources. */
    private int maxWait;

    /** Executor that performs pool resource validation. */
    @NotNull
    private ExecutorService executor;


    /**
     * Sets the executor service responsible for pool resource validation.
     *
     * @param executorService Executor of pool validation operations.
     */
    public void setExecutor(final ExecutorService executorService) {
        this.executor = executorService;
    }


    /**
     * Set the maximum amount of time wait while validating pool resources.
     * If the pool defines a minumum time to wait for a resource, this property
     * should be set less than that value.
     *
     * @param time Wait time in milliseconds.
     */
    public void setMaxWait(final int time) {
        this.maxWait = time;
    }


    /** {@inheritDoc} */
    public PoolStatus observe() {
        final Future<StatusCode> result = executor.submit(new Validator());
        StatusCode code;
        String description = null;
        try {
            code = result.get(maxWait, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            code = StatusCode.UNKNOWN;
            description = "Validator thread interrupted during pool validation.";
        } catch (TimeoutException e) {
            code = StatusCode.WARN;
            description = String.format("Pool validation timed out.  Max wait is %s ms.", maxWait);
        } catch (Exception e) {
            code = StatusCode.ERROR;
            description = e.getMessage();
        }
        return new PoolStatus(code, description, getActiveCount(), getIdleCount());
    }


    /**
     * Performs a health check on a the pool.  The recommended implementation is to
     * obtain a pool resource, validate it, and return it to the pool.
     *
     * @return Status code describing pool health.
     *
     * @throws Exception Thrown to indicate a serious problem with pool validation.
     */
    protected abstract StatusCode checkPool() throws Exception;


    /**
     * Gets the number of pool resources idle at present.
     *
     * @return Number of idle pool resources.
     */
    protected abstract int getIdleCount();


    /**
     * Gets the number of pool resources active at present.
     *
     * @return Number of active pool resources.
     */
    protected abstract int getActiveCount();


    private class Validator implements Callable<StatusCode> {
        public StatusCode call() throws Exception {
            return checkPool();
        }
    }
}
