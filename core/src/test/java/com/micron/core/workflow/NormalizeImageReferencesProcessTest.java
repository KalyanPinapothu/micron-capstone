package com.micron.core.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.micron.core.testcontext.AppAemContext;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class NormalizeImageReferencesProcessTest {

    private final AemContext context = AppAemContext.newAemContext();

    private NormalizeImageReferencesProcess process;

    @Mock
    private WorkItem workItem;

    @Mock
    private WorkflowSession workflowSession;

    @Mock
    private WorkflowData workflowData;

    @Mock
    private MetaDataMap metaDataMap;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        process = new NormalizeImageReferencesProcess();
    }

    @Test
    void normalizesFileReferencesRecursively() throws Exception {
        context.create().resource(
            "/content/site/page/jcr:content/root/image",
            "sling:resourceType", "micron/components/image",
            "fileReference",
            "/content/dam/sample-content/content/RD8_RD8_tCCD5_3ds_core.svg"
                + "/jcr:content/renditions/original"
        );
        context.create().resource(
            "/content/site/page/jcr:content/root/figure/image",
            "sling:resourceType", "micron/components/image",
            "fileReference",
            "/content/dam/sample-content/content/RD8_RD8_tCCD6_3ds_core.svg"
                + "/jcr:content/renditions/original"
        );

        when(workItem.getWorkflowData()).thenReturn(workflowData);
        when(workflowData.getPayload()).thenReturn("/content/site/page");
        when(workflowSession.adaptTo(ResourceResolver.class))
            .thenReturn(context.resourceResolver());

        process.execute(workItem, workflowSession, metaDataMap);

        assertEquals(
            "/content/dam/sample-content/content/RD8_RD8_tCCD5_3ds_core.svg",
            context.resourceResolver()
                .getResource("/content/site/page/jcr:content/root/image")
                .getValueMap()
                .get("fileReference", String.class)
        );
        assertEquals(
            "/content/dam/sample-content/content/RD8_RD8_tCCD6_3ds_core.svg",
            context.resourceResolver()
                .getResource("/content/site/page/jcr:content/root/figure/image")
                .getValueMap()
                .get("fileReference", String.class)
        );
    }

    @Test
    void leavesAlreadyNormalizedFileReferenceUnchanged() {
        final Resource resource = context.create().resource(
            "/content/site/page/jcr:content/root/image",
            "fileReference",
            "/content/dam/sample-content/content/RD8_RD8_tCCD5_3ds_core.svg"
        );

        final int updatedCount = process.normalizeImageReferences(resource);

        assertEquals(0, updatedCount);
        assertEquals(
            "/content/dam/sample-content/content/RD8_RD8_tCCD5_3ds_core.svg",
            resource.getValueMap().get("fileReference", String.class)
        );
    }
}
