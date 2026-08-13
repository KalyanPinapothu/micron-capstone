package com.micron.core.workflow;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.metadata.MetaDataMap;

@Component(
        service = WorkflowProcess.class,
        property = {
                "process.label=Micron Metadata Validation Process"
        }
)
public class ValidateMetadataProcess implements WorkflowProcess {

    private static final Logger LOG =
            LoggerFactory.getLogger(ValidateMetadataProcess.class);


    @Override
    public void execute(WorkItem workItem,
                        WorkflowSession workflowSession,
                        MetaDataMap metaDataMap)
            throws WorkflowException {

        String assetPath =
                workItem.getWorkflowData().getPayload().toString();

        ResourceResolver resolver = null;

        try {

            resolver= workflowSession.adaptTo(ResourceResolver.class);

            Resource metadataResource =
                    resolver.getResource(assetPath + "/jcr:content/metadata");

            if (metadataResource == null) {
                LOG.error("Metadata node not found for {}", assetPath);
                return;
            }

            ValueMap valueMap = metadataResource.getValueMap();

            String productVersion = valueMap.get("pversion", "");

            String audience = valueMap.get("audience", "");

            String complianceRegion = valueMap.get("complianceRegion", "");

            List<String> errors = new ArrayList<>();

            if (StringUtils.isBlank(productVersion)) {
                errors.add("Product Version");
            }

            if (StringUtils.isBlank(audience)) {
                errors.add("Audience");
            }

            if (StringUtils.isBlank(complianceRegion)) {
                errors.add("Compliance Region");
            }

            ModifiableValueMap modifiable = metadataResource.adaptTo(ModifiableValueMap.class);

            if (errors.isEmpty()) {

                modifiable.put("validationStatus", "VALID");
                modifiable.put("validationErrors", "");

                LOG.info("Metadata validation successful for {}",
                        assetPath);

            } else {

                modifiable.put("validationStatus", "INVALID");
                modifiable.put("validationErrors",
                        String.join(", ", errors));

                LOG.info("Metadata validation failed for {} : {}",
                        assetPath,
                        String.join(", ", errors));
            }

            resolver.commit();

        } catch (Exception e) {
            LOG.error("Metadata validation failed", e);
        } finally {
            if (resolver != null && resolver.isLive()) {
                resolver.close();
            }
        }
    }
}