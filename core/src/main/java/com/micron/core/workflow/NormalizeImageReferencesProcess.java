package com.micron.core.workflow;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;

@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=Micron Normalize Image References Process"
    }
)
public class NormalizeImageReferencesProcess implements WorkflowProcess {

    private static final Logger LOG =
        LoggerFactory.getLogger(NormalizeImageReferencesProcess.class);

    private static final String FILE_REFERENCE = "fileReference";

    private static final String RENDITION_SEGMENT =
        "/jcr:content/renditions/";

    @Override
    public void execute(
            final WorkItem workItem,
            final WorkflowSession workflowSession,
            final MetaDataMap metaDataMap) throws WorkflowException {

        final String payloadPath =
            String.valueOf(workItem.getWorkflowData().getPayload());
        final ResourceResolver resolver =
            workflowSession.adaptTo(ResourceResolver.class);

        if (resolver == null) {
            LOG.warn("Could not adapt workflow session to resource resolver");
            return;
        }

        final Resource payloadResource = resolver.getResource(payloadPath);

        if (payloadResource == null) {
            LOG.warn("Workflow payload resource not found: {}", payloadPath);
            return;
        }

        try {
            final int updatedCount =
                normalizeImageReferences(payloadResource);

            if (updatedCount > 0) {
                resolver.commit();
                LOG.info(
                    "Normalized {} image fileReference value(s) under {}",
                    updatedCount,
                    payloadPath
                );
            }
        } catch (PersistenceException e) {
            throw new WorkflowException(
                "Failed to normalize image references under " + payloadPath,
                e
            );
        }
    }

    int normalizeImageReferences(final Resource resource) {
        int updatedCount = normalizeImageReference(resource) ? 1 : 0;

        for (Resource child : resource.getChildren()) {
            updatedCount += normalizeImageReferences(child);
        }

        return updatedCount;
    }

    private boolean normalizeImageReference(final Resource resource) {
        final ModifiableValueMap properties =
            resource.adaptTo(ModifiableValueMap.class);

        if (properties == null) {
            return false;
        }

        final String fileReference =
            properties.get(FILE_REFERENCE, String.class);
        final String normalizedReference =
            normalizeAssetPath(fileReference);

        if (
            StringUtils.isBlank(normalizedReference)
                || StringUtils.equals(fileReference, normalizedReference)
        ) {
            return false;
        }

        properties.put(FILE_REFERENCE, normalizedReference);
        return true;
    }

    static String normalizeAssetPath(final String sourcePath) {
        final String path = StringUtils.trimToNull(sourcePath);

        if (path == null) {
            return null;
        }

        final int renditionIndex =
            path.indexOf(RENDITION_SEGMENT);

        return renditionIndex >= 0
            ? path.substring(0, renditionIndex)
            : path;
    }
}
