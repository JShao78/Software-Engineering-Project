package de.pdbm.team18.htmlunit.stores;

import de.pdbm.team18.shared.HtmlUnitTestUtils;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StaffForbiddenIT {

    private WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = HtmlUnitTestUtils.createWebClient();
    }

    @AfterEach
    void tearDown() {
        if (webClient != null) {
            webClient.close();
        }
    }

    @Test
    void shouldReturnForbiddenUser1() throws IOException {
        assertForbidden(HtmlUnitTestUtils.UserGroup.USER1);
    }

    @Test
    void shouldReturnForbiddenUser2() throws IOException {
        assertForbidden(HtmlUnitTestUtils.UserGroup.USER2);
    }

    private void assertForbidden(HtmlUnitTestUtils.UserGroup userGroup) throws IOException {
        HtmlUnitTestUtils.login(webClient, userGroup);
        HtmlPage page = webClient.getPage(HtmlUnitTestUtils.resolveUrl("staffs.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);

        int statusCode = page.getWebResponse().getStatusCode();
        assertEquals(403, statusCode,
                userGroup.name() + " sollte nicht auf staffs.xhtml zugreifen können, erwartet 403 Forbidden aber Status war " + statusCode);
    }
}
