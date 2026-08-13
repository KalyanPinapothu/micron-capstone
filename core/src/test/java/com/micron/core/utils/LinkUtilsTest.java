package com.micron.core.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.apache.sling.api.resource.ResourceResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkUtilsTest {

    @Mock
    private ResourceResolver resolver;

    @BeforeEach
    void setUp() {
        lenient().when(resolver.map(org.mockito.ArgumentMatchers.anyString()))
            .thenAnswer(invocation -> invocation.getArgument(0, String.class));
    }

    @Test
    void appendsHtmlToInternalContentLinks() {
        assertEquals(
            "/content/micron/us/en.html",
            LinkUtils.getFormattedLink("/content/micron/us/en", resolver)
        );
    }

    @Test
    void preservesAlreadyFormattedInternalContentLinks() {
        assertEquals(
            "/content/micron/us/en.html",
            LinkUtils.getFormattedLink("/content/micron/us/en.html", resolver)
        );
    }

    @Test
    void insertsHtmlBeforeAnchorOrQueryString() {
        assertEquals(
            "/content/micron/us/en.html#overview",
            LinkUtils.getFormattedLink("/content/micron/us/en#overview", resolver)
        );
        assertEquals(
            "/content/micron/us/en.html?wcmmode=disabled",
            LinkUtils.getFormattedLink(
                "/content/micron/us/en?wcmmode=disabled",
                resolver
            )
        );
    }

    @Test
    void leavesDamExternalHashBlankAndNullLinksUnchanged() {
        assertEquals(
            "/content/dam/micron/logo.svg",
            LinkUtils.getFormattedLink("/content/dam/micron/logo.svg", resolver)
        );
        assertEquals(
            "https://in.micron.com/",
            LinkUtils.getFormattedLink("https://in.micron.com/", resolver)
        );
        assertEquals(
            "https://www.google.com",
            LinkUtils.getFormattedLink("www.google.com", resolver)
        );
        assertEquals(
            "https://google.com/search?q=memory",
            LinkUtils.getFormattedLink("google.com/search?q=memory", resolver)
        );
        assertEquals("#", LinkUtils.getFormattedLink("#", resolver));
        assertEquals(" ", LinkUtils.getFormattedLink(" ", resolver));
        assertEquals(null, LinkUtils.getFormattedLink(null, resolver));
    }

    @Test
    void returnsOriginalLinkWhenResolverIsMissing() {
        assertEquals(
            "/content/micron/us/en",
            LinkUtils.getFormattedLink("/content/micron/us/en", null)
        );
        verify(resolver, never()).map("/content/micron/us/en.html");
    }
}
