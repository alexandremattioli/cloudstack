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
import java.util.List;

public class CloudStackIntegrationExecutionResult {

    private final CloudStackIntegrationRequest request;
    private final List<CloudStackIntegrationResult> providerResults;
    private final long startedAt;
    private final long finishedAt;

    public CloudStackIntegrationExecutionResult(CloudStackIntegrationRequest request,
            List<CloudStackIntegrationResult> providerResults, long startedAt, long finishedAt) {
        this.request = request;
        this.providerResults = Collections.unmodifiableList(new ArrayList<>(providerResults));
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public CloudStackIntegrationRequest getRequest() {
        return request;
    }

    public List<CloudStackIntegrationResult> getProviderResults() {
        return providerResults;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getFinishedAt() {
        return finishedAt;
    }

    public long getDuration() {
        return finishedAt - startedAt;
    }

    public boolean isSuccessful() {
        for (CloudStackIntegrationResult result : providerResults) {
            if (result.isFailure()) {
                return false;
            }
        }
        return true;
    }

    public List<CloudStackIntegrationResult> getSuccessfulResults() {
        return filterByState(State.SUCCESS);
    }

    public List<CloudStackIntegrationResult> getFailedResults() {
        return filterByState(State.FAILURE);
    }

    public List<CloudStackIntegrationResult> getSkippedResults() {
        return filterByState(State.SKIPPED);
    }

    protected List<CloudStackIntegrationResult> filterByState(State state) {
        List<CloudStackIntegrationResult> filteredResults = new ArrayList<>();
        for (CloudStackIntegrationResult result : providerResults) {
            switch (state) {
                case SUCCESS:
                    if (result.isSuccessful()) {
                        filteredResults.add(result);
                    }
                    break;
                case FAILURE:
                    if (result.isFailure()) {
                        filteredResults.add(result);
                    }
                    break;
                case SKIPPED:
                    if (result.isSkipped()) {
                        filteredResults.add(result);
                    }
                    break;
                default:
                    break;
            }
        }
        return filteredResults;
    }

    private enum State {
        SUCCESS,
        FAILURE,
        SKIPPED
    }
}
