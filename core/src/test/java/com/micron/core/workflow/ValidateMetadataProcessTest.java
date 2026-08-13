package com.micron.core.workflow;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;

class ValidateMetadataProcessTest {

    private ValidateMetadataProcess process;

    @Mock
    private WorkItem workItem;

    @Mock
    private WorkflowSession workflowSession;

    @Mock
    private MetaDataMap metaDataMap;

    @Mock
    private WorkflowData workflowData;

    @Mock
    private ResourceResolver resolver;

    @Mock
    private Resource metadataResource;

    @Mock
    private ModifiableValueMap modifiableValueMap;

    @Mock
    private ValueMap valueMap;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        process = new ValidateMetadataProcess();
    }

    @Test
    void executeMarksMetadataAsValidWhenAllRequiredFieldsArePresent() throws Exception {
        String assetPath = "/content/dam/sample-asset";

        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getPayload()).thenReturn(assetPath);
        when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(resolver);
        when(resolver.getResource(assetPath + "/jcr:content/metadata")).thenReturn(metadataResource);
        when(metadataResource.getValueMap()).thenReturn(valueMap);
        when(valueMap.get("pversion", "")).thenReturn("1.0");
        when(valueMap.get("audience", "")).thenReturn("Internal");
        when(valueMap.get("complianceRegion", "")).thenReturn("US");
        when(metadataResource.adaptTo(ModifiableValueMap.class)).thenReturn(modifiableValueMap);
        when(resolver.isLive()).thenReturn(true);

        process.execute(workItem, workflowSession, metaDataMap);

        verify(modifiableValueMap).put("validationStatus", "VALID");
        verify(modifiableValueMap).put("validationErrors", "");
        verify(resolver).commit();
        verify(resolver).close();
    }

    @Test
    void executeMarksMetadataAsInvalidWhenAnyRequiredFieldIsMissing() throws Exception {
        String assetPath = "/content/dam/sample-asset";

        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getPayload()).thenReturn(assetPath);
        when(workflowSession.adaptTo(ResourceResolver.class)).thenReturn(resolver);
        when(resolver.getResource(assetPath + "/jcr:content/metadata")).thenReturn(metadataResource);
        when(metadataResource.getValueMap()).thenReturn(valueMap);
        when(valueMap.get("pversion", "")).thenReturn("");
        when(valueMap.get("audience", "")).thenReturn("");
        when(valueMap.get("complianceRegion", "")).thenReturn("");
        when(metadataResource.adaptTo(ModifiableValueMap.class)).thenReturn(modifiableValueMap);
        when(resolver.isLive()).thenReturn(true);

        process.execute(workItem, workflowSession, metaDataMap);

        verify(modifiableValueMap).put("validationStatus", "INVALID");
        verify(modifiableValueMap).put("validationErrors", "Product Version, Audience, Compliance Region");
        verify(resolver).commit();
        verify(resolver).close();
    }
}
