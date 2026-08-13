package com.micron.core.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.micron.core.models.GuidesAnchorUtil;

class GuidesAnchorUtilTest {

    @Test
    void fromPathNormalizesPagePaths() {
        assertEquals("content-micron-us-en-my-topic", GuidesAnchorUtil.fromPath("/content/micron/us/en/My Topic"));
    }

    @Test
    void fromPathFallsBackWhenBlankOrOnlySymbols() {
        assertEquals("topic", GuidesAnchorUtil.fromPath(null));
        assertEquals("topic", GuidesAnchorUtil.fromPath("  "));
        assertEquals("topic", GuidesAnchorUtil.fromPath("///---"));
    }
}
