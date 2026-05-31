package de.pdbm.team18.htmlunit.security;

import de.pdbm.team18.shared.HtmlUnitTestUtils;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FailedLoginIT {

    private WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = HtmlUnitTestUtils.createWebClient();
    }

    @AfterEach
    void tearDown() {
        webClient.close();
    }

    @Test
    void failedLoginCannotAccessProtectedPages() throws Exception {
        HtmlPage loginPage = openPage("login.xhtml");

        HtmlUnitTestUtils.findInputByIdSuffix(loginPage, "username").setValueAttribute("not-a-user@bikes.shop");
        HtmlUnitTestUtils.findInputByIdSuffix(loginPage, "password").setValueAttribute("definitely-wrong");
        HtmlPage afterFailedLogin = HtmlUnitTestUtils.getButton(loginPage, "login").click();

        assertTrue(
                containsAny(afterFailedLogin.asNormalizedText(),
                        "Login fehlgeschlagen",
                        "Benutzername oder Passwort sind nicht korrekt.",
                        "Erneut anmelden"),
                "Expected an error after failed login, but got page: " + afterFailedLogin.getUrl()
        );

        List<String> protectedPages = List.of(
                "index.xhtml",
                "bestellungen.xhtml",
                "kunden.xhtml",
                "produkte.xhtml",
                "stocks.xhtml",
                "stores.xhtml",
                "staffs.xhtml"
        );

        for (String protectedPage : protectedPages) {
            assertRedirectedToLogin(openPage(protectedPage), protectedPage);
        }
    }

    private HtmlPage openPage(String relativePath) throws IOException {
        return webClient.getPage(HtmlUnitTestUtils.resolveUrl(relativePath));
    }

    private void assertRedirectedToLogin(HtmlPage page, String requestedPath) {
        String path = page.getUrl().getPath();
        assertTrue(
                path.endsWith("/login.xhtml") || path.endsWith("/login-error.xhtml"),
                "Expected '" + requestedPath + "' to require login, but landed on " + page.getUrl()
        );

        assertTrue(
                containsAny(page.asNormalizedText(), "Login", "Nutzername", "Erneut anmelden", "Login fehlgeschlagen"),
                "Expected a login screen after requesting '" + requestedPath + "', but got: " + page.getUrl()
        );
    }

    private boolean containsAny(String text, String... expectedFragments) {
        String normalized = text.toLowerCase(Locale.ROOT);
        for (String fragment : expectedFragments) {
            if (normalized.contains(fragment.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
