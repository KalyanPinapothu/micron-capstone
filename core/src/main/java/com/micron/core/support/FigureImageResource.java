package com.micron.core.support;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceMetadata;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceWrapper;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FigureImageResource extends ResourceWrapper {

    private static final String RESOURCE_NAME = "image";

    private final ValueMap valueMap;
    private final String resourceType;
    private final String path;

    public FigureImageResource(
            final Resource figureResource,
            final String resourceType,
            final Map<String, Object> properties) {

        super(figureResource);

        this.resourceType = resourceType;
        this.path = figureResource.getPath() + "/" + RESOURCE_NAME;
        this.valueMap = new ValueMapDecorator(properties);
    }

    @Override
    public @NotNull String getPath() {
        return path;
    }

    @Override
    public @NotNull String getName() {
        return RESOURCE_NAME;
    }

    @Override
    public @NotNull String getResourceType() {
        return resourceType;
    }

    @Override
    public @Nullable String getResourceSuperType() {
        return null;
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

    @Override
    public boolean hasChildren() {
        return false;
    }

    @Override
    public @NotNull Iterator<Resource> listChildren() {
        return Collections.emptyIterator();
    }

    @Override
    public @Nullable Resource getChild(final @NotNull String relPath) {
        return null;
    }

    @Override
    public @NotNull ResourceResolver getResourceResolver() {
        return getResource().getResourceResolver();
    }

    @Override
    public @NotNull ResourceMetadata getResourceMetadata() {
        return getResource().getResourceMetadata();
    }
}