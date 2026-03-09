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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.cloudstack.framework.config.ConfigKey;
import org.apache.cloudstack.framework.config.Configurable;
import org.apache.cloudstack.managed.context.ManagedContextRunnable;

import com.cloud.utils.component.ManagerBase;
import com.cloud.utils.concurrency.NamedThreadFactory;

public class CloudStackIntegrationManagerImpl extends ManagerBase
        implements CloudStackIntegrationService, Configurable {

    private volatile ExecutorService executorService;
    private volatile List<CloudStackIntegrationProvider> providers = Collections.emptyList();

    @Override
    public boolean start() {
        if (isFrameworkEnabled()) {
            ensureExecutorService();
        }
        return true;
    }

    @Override
    public boolean stop() {
        ExecutorService currentExecutor = executorService;
        executorService = null;
        if (currentExecutor != null) {
            currentExecutor.shutdownNow();
        }
        return true;
    }

    @Override
    public CloudStackIntegrationExecutionResult execute(CloudStackIntegrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Integration request is required");
        }

        long startedAt = System.currentTimeMillis();
        List<CloudStackIntegrationResult> results = new ArrayList<>();

        if (!isFrameworkEnabled()) {
            results.add(CloudStackIntegrationResult.skipped(
                    getName(), "CloudStack integration framework is disabled"));
            return new CloudStackIntegrationExecutionResult(
                    request, results, startedAt, System.currentTimeMillis());
        }

        Set<String> unresolvedTargets = new LinkedHashSet<>(request.getTargetProviders());
        List<ProviderExecution> submittedExecutions = new ArrayList<>();

        for (CloudStackIntegrationProvider provider : getProviders()) {
            if (!request.targetsProvider(provider.getName())) {
                continue;
            }

            removeResolvedTarget(unresolvedTargets, provider.getName());

            if (!provider.isEnabled()) {
                results.add(CloudStackIntegrationResult.skipped(provider.getName(), "Provider is disabled"));
                continue;
            }

            if (!provider.supports(request)) {
                results.add(CloudStackIntegrationResult.skipped(
                        provider.getName(), "Provider does not support this request"));
                continue;
            }

            submittedExecutions.add(submitProvider(provider, request));
        }

        for (String unresolvedTarget : unresolvedTargets) {
            results.add(CloudStackIntegrationResult.skipped(unresolvedTarget, "Provider is not registered"));
        }

        collectExecutionResults(results, submittedExecutions, request.isFailOnError());

        return new CloudStackIntegrationExecutionResult(
                request, results, startedAt, System.currentTimeMillis());
    }

    protected void collectExecutionResults(List<CloudStackIntegrationResult> results,
            List<ProviderExecution> submittedExecutions,
            boolean failOnError) {
        for (int index = 0; index < submittedExecutions.size(); index++) {
            ProviderExecution execution = submittedExecutions.get(index);
            CloudStackIntegrationResult result = waitForResult(execution);
            results.add(result);

            if (failOnError && result.isFailure()) {
                cancelOutstandingExecutions(results, submittedExecutions, index + 1);
                return;
            }
        }
    }

    protected void cancelOutstandingExecutions(List<CloudStackIntegrationResult> results,
            List<ProviderExecution> submittedExecutions, int startIndex) {
        for (int index = startIndex; index < submittedExecutions.size(); index++) {
            ProviderExecution pendingExecution = submittedExecutions.get(index);
            if (pendingExecution.future.cancel(true)) {
                results.add(CloudStackIntegrationResult.failure(pendingExecution.providerName,
                        "Execution cancelled after another provider failed"));
            } else {
                results.add(waitForResult(pendingExecution));
            }
        }
    }

    protected CloudStackIntegrationResult waitForResult(ProviderExecution execution) {
        try {
            return execution.future.get(execution.timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            execution.future.cancel(true);
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("timeoutMs", execution.timeoutMs);
            return CloudStackIntegrationResult.failure(
                    execution.providerName, "Provider execution timed out", details);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return failureResult(execution.providerName, "Provider execution interrupted", e);
        } catch (CancellationException e) {
            return failureResult(execution.providerName, "Provider execution cancelled", e);
        } catch (ExecutionException e) {
            return failureResult(execution.providerName, "Provider execution failed", e.getCause());
        } catch (Exception e) {
            return failureResult(execution.providerName, "Provider execution failed", e);
        }
    }

    protected ProviderExecution submitProvider(final CloudStackIntegrationProvider provider,
            final CloudStackIntegrationRequest request) {
        final CompletableFuture<CloudStackIntegrationResult> future = new CompletableFuture<>();
        final long startedAt = System.currentTimeMillis();

        ensureExecutorService().execute(new ManagedContextRunnable() {
            @Override
            protected void runInContext() {
                try {
                    CloudStackIntegrationResult result = provider.integrate(request);
                    future.complete(normalizeProviderResult(
                            provider.getName(), result, startedAt, System.currentTimeMillis()));
                } catch (Throwable t) {
                    future.complete(failureResult(provider.getName(),
                            "Provider execution failed", t, startedAt, System.currentTimeMillis()));
                }
            }
        });

        return new ProviderExecution(provider.getName(), future, getProviderTimeoutMs(provider));
    }

    protected CloudStackIntegrationResult normalizeProviderResult(String providerName,
            CloudStackIntegrationResult result,
            long startedAt, long finishedAt) {
        if (result == null) {
            return failureResult(providerName, "Provider returned no result", null, startedAt, finishedAt);
        }

        CloudStackIntegrationResult.Builder builder = CloudStackIntegrationResult.builder(
                resolveProviderName(providerName, result))
                .message(result.getMessage())
                .startedAt(result.getStartedAt() > 0 ? result.getStartedAt() : startedAt)
                .finishedAt(result.getFinishedAt() > 0 ? result.getFinishedAt() : finishedAt)
                .details(result.getDetails());

        if (result.isSkipped()) {
            return builder.skipped().build();
        }
        if (result.isSuccessful()) {
            return builder.success().build();
        }
        return builder.failure().build();
    }

    protected String resolveProviderName(String defaultProviderName, CloudStackIntegrationResult result) {
        String providerName = result.getProviderName();
        if (providerName == null || providerName.trim().isEmpty()) {
            return defaultProviderName;
        }
        return providerName;
    }

    protected CloudStackIntegrationResult failureResult(String providerName, String message, Throwable throwable) {
        long now = System.currentTimeMillis();
        return failureResult(providerName, message, throwable, now, now);
    }

    protected CloudStackIntegrationResult failureResult(String providerName, String message, Throwable throwable,
            long startedAt, long finishedAt) {
        CloudStackIntegrationResult.Builder builder = CloudStackIntegrationResult.builder(providerName)
                .failure()
                .message(buildFailureMessage(message, throwable))
                .startedAt(startedAt)
                .finishedAt(finishedAt);

        if (throwable != null) {
            builder.detail("exceptionClass", throwable.getClass().getName());
            if (throwable.getMessage() != null) {
                builder.detail("exceptionMessage", throwable.getMessage());
            }
        }

        return builder.build();
    }

    protected String buildFailureMessage(String message, Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null || throwable.getMessage().trim().isEmpty()) {
            return message;
        }
        return String.format("%s: %s", message, throwable.getMessage());
    }

    protected synchronized ExecutorService ensureExecutorService() {
        if (executorService == null || executorService.isShutdown()) {
            executorService = createExecutorService(getExecutorThreadCount());
        }
        return executorService;
    }

    protected ExecutorService createExecutorService(int executorThreadCount) {
        return Executors.newFixedThreadPool(Math.max(1, executorThreadCount),
                new NamedThreadFactory("CloudStack-Integration"));
    }

    protected void removeResolvedTarget(Set<String> unresolvedTargets, String providerName) {
        unresolvedTargets.removeIf(targetProvider -> targetProvider.equalsIgnoreCase(providerName));
    }

    protected int getExecutorThreadCount() {
        return Math.max(1, IntegrationFrameworkParallelism.value());
    }

    protected long getProviderTimeoutMs(CloudStackIntegrationProvider provider) {
        Long providerTimeout = provider.getTimeoutOverrideMs();
        if (providerTimeout != null && providerTimeout > 0L) {
            return providerTimeout;
        }
        return Math.max(1L, IntegrationFrameworkProviderTimeoutMs.value());
    }

    protected boolean isFrameworkEnabled() {
        return IntegrationFrameworkEnabled.value();
    }

    @Override
    public String getConfigComponentName() {
        return CloudStackIntegrationService.class.getSimpleName();
    }

    @Override
    public ConfigKey<?>[] getConfigKeys() {
        return new ConfigKey<?>[] {
            IntegrationFrameworkEnabled,
            IntegrationFrameworkParallelism,
            IntegrationFrameworkProviderTimeoutMs
        };
    }

    @Override
    public List<CloudStackIntegrationProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<CloudStackIntegrationProvider> providers) {
        this.providers = providers == null ? Collections.emptyList() : providers;
    }

    protected static final class ProviderExecution {
        private final String providerName;
        private final Future<CloudStackIntegrationResult> future;
        private final long timeoutMs;

        protected ProviderExecution(String providerName, Future<CloudStackIntegrationResult> future, long timeoutMs) {
            this.providerName = providerName;
            this.future = future;
            this.timeoutMs = timeoutMs;
        }
    }
}
