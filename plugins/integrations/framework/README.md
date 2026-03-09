# CloudStack Integration Framework

This module adds a reusable integration layer for CloudStack features and external platforms.

## What it provides

- A shared provider contract in `cloud-api`
- A Spring registry that auto-discovers integration providers
- A parallel execution manager that runs multiple providers at once
- Provider ordering and exclusion through global config keys
- A starter provider (`CloudStackIntegrationLoggingProvider`) that demonstrates the extension pattern

## Core extension points

The shared API lives in `org.apache.cloudstack.integration`:

- `CloudStackIntegrationRequest`
- `CloudStackIntegrationResult`
- `CloudStackIntegrationExecutionResult`
- `CloudStackIntegrationProvider`
- `CloudStackIntegrationProviderBase`
- `CloudStackIntegrationService`

## Runtime configuration

The framework exposes these global settings:

- `cloudstack.integration.framework.enabled`
- `cloudstack.integration.framework.parallelism`
- `cloudstack.integration.framework.provider.timeout.ms`
- `cloudstack.integration.providers.order`
- `cloudstack.integration.providers.exclude`

## How to add another platform integration

1. Create a plugin module that depends on `cloud-api`.
2. Set the module parent to `integration` in `META-INF/cloudstack/<module-name>/module.properties`.
3. Implement `CloudStackIntegrationProvider` or extend `CloudStackIntegrationProviderBase`.
4. Register the provider as a Spring bean in the plugin context.
5. Inject `CloudStackIntegrationService` where orchestration should trigger external integrations.

Example provider:

```java
public class MyPlatformIntegrationProvider extends CloudStackIntegrationProviderBase {
    @Override
    public boolean supports(CloudStackIntegrationRequest request) {
        return "vm.create".equalsIgnoreCase(request.getOperation());
    }

    @Override
    public CloudStackIntegrationResult integrate(CloudStackIntegrationRequest request) {
        return success("My platform accepted the request");
    }
}
```

Example request:

```java
CloudStackIntegrationRequest request = CloudStackIntegrationRequest.builder("vm.create")
        .source("VmProvisioningFlow")
        .resourceType("UserVm")
        .resourceId(vmUuid)
        .parameter("serviceOfferingId", offeringId)
        .targetProvider("MyPlatformIntegrationProvider")
        .build();

CloudStackIntegrationExecutionResult result = cloudStackIntegrationService.execute(request);
```

## Design notes

- Provider failures are isolated so one bad integration does not break the others.
- Targeted provider execution supports partial rollouts and per-platform routing.
- The framework uses CloudStack managed-context execution so background work keeps normal CloudStack context handling.
