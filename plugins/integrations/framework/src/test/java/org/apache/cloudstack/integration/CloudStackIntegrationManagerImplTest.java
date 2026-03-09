/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
package org.apache.cloudstack.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class CloudStackIntegrationManagerImplTest {

    @Test
    public void executeRunsProvidersInParallel() {
        AtomicInteger currentConcurrency = new AtomicInteger();
        AtomicInteger maxConcurrency = new AtomicInteger();

        TestProvider firstProvider = new TestProvider(
                "FirstProvider", true, 150L, null, currentConcurrency, maxConcurrency);
        TestProvider secondProvider = new TestProvider(
                "SecondProvider", true, 150L, null, currentConcurrency, maxConcurrency);
        TestManager manager = new TestManager(Arrays.asList(firstProvider, secondProvider));

        try {
            CloudStackIntegrationExecutionResult result = manager.execute(
                    CloudStackIntegrationRequest.builder("vm.create").build());

            assertEquals(2, result.getSuccessfulResults().size());
            assertEquals(0, result.getFailedResults().size());
            assertTrue("Expected providers to overlap in execution", maxConcurrency.get() > 1);
        } finally {
            manager.stop();
        }
    }

    @Test
    public void executeReportsUnsupportedAndUnknownProviders() {
        TestProvider unsupportedProvider = new TestProvider(
                "UnsupportedProvider", false, 0L, null, null, null);
        TestManager manager = new TestManager(Arrays.asList(unsupportedProvider));

        try {
            CloudStackIntegrationRequest request = CloudStackIntegrationRequest.builder("network.sync")
                    .targetProvider("UnsupportedProvider")
                    .targetProvider("MissingProvider")
                    .build();

            CloudStackIntegrationExecutionResult result = manager.execute(request);

            assertEquals(2, result.getSkippedResults().size());
            assertEquals(0, result.getSuccessfulResults().size());
            assertEquals(0, result.getFailedResults().size());
        } finally {
            manager.stop();
        }
    }

    @Test
    public void executeCapturesProviderFailures() {
        TestProvider successProvider = new TestProvider("SuccessProvider", true, 0L, null, null, null);
        TestProvider failureProvider = new TestProvider("FailureProvider", true, 0L,
                new IllegalStateException("integration exploded"), null, null);
        TestManager manager = new TestManager(Arrays.asList(successProvider, failureProvider));

        try {
            CloudStackIntegrationExecutionResult result = manager.execute(
                    CloudStackIntegrationRequest.builder("vm.delete").build());

            assertEquals(1, result.getSuccessfulResults().size());
            assertEquals(1, result.getFailedResults().size());
            assertEquals("FailureProvider", result.getFailedResults().get(0).getProviderName());
            assertEquals("java.lang.IllegalStateException",
                    result.getFailedResults().get(0).getDetails().get("exceptionClass"));
        } finally {
            manager.stop();
        }
    }

    protected static class TestManager extends CloudStackIntegrationManagerImpl {
        protected TestManager(List<CloudStackIntegrationProvider> providers) {
            setProviders(providers);
        }

        @Override
        protected boolean isFrameworkEnabled() {
            return true;
        }

        @Override
        protected int getExecutorThreadCount() {
            return 4;
        }

        @Override
        protected long getProviderTimeoutMs(CloudStackIntegrationProvider provider) {
            return 1000L;
        }
    }

    protected static class TestProvider extends CloudStackIntegrationProviderBase {
        private final boolean supports;
        private final long delayMs;
        private final RuntimeException exception;
        private final AtomicInteger currentConcurrency;
        private final AtomicInteger maxConcurrency;

        protected TestProvider(String name, boolean supports, long delayMs, RuntimeException exception,
                AtomicInteger currentConcurrency, AtomicInteger maxConcurrency) {
            setName(name);
            this.supports = supports;
            this.delayMs = delayMs;
            this.exception = exception;
            this.currentConcurrency = currentConcurrency;
            this.maxConcurrency = maxConcurrency;
        }

        @Override
        public boolean supports(CloudStackIntegrationRequest request) {
            return supports;
        }

        @Override
        public CloudStackIntegrationResult integrate(CloudStackIntegrationRequest request) {
            if (exception != null) {
                throw exception;
            }

            if (currentConcurrency != null && maxConcurrency != null) {
                int running = currentConcurrency.incrementAndGet();
                maxConcurrency.accumulateAndGet(running, Math::max);
            }

            try {
                if (delayMs > 0L) {
                    Thread.sleep(delayMs);
                }
                return success("Processed request");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return failure("Interrupted");
            } finally {
                if (currentConcurrency != null) {
                    currentConcurrency.decrementAndGet();
                }
            }
        }
    }
}
