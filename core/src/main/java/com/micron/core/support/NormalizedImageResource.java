package com.micron.core.support;

import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NormalizedImageResource extends ResourceWrapper {

    private final String resourceType;
    private final ValueMap valueMap;

    public NormalizedImageResource(
            final Resource resource,
            final String resourceType,
            final Map<String, Object> properties) {

        super(resource);
        this.resourceType = resourceType;
        this.valueMap = new ValueMapDecorator(new HashMap<>(properties));
    }

    @Override
    public @NotNull String getResourceType() {
        return resourceType;
    }

    @Override
    public @NotNull ValueMap getValueMap() {
        return valueMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <AdapterType> @Nullable AdapterType adaptTo(
            final Class<AdapterType> type) {

        if (type == ValueMap.class || type == Map.class) {
            return (AdapterType) valueMap;
        }

        return super.adaptTo(type);
    }
}
