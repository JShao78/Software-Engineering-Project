package de.pdbm.team18.htmlunit.security;

import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class FailedLoginIT {

    private static final URI BASE_URI = createBaseUri();

    private WebClient webClient;

    @BeforeEach
    void setUp() {
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    }

    @AfterEach
    void tearDown() {
        webClient.close();
    }

    @Test
    void failedLoginCannotAccessProtectedPages() throws Exception {
        HtmlPage loginPage = openPage("login.xhtml");

        findInputByIdSuffix(loginPage, "username").setValueAttribute("not-a-user@bikes.shop");
        findInputByIdSuffix(loginPage, "password").setValueAttribute("definitely-wrong");
        HtmlPage afterFailedLogin = clickLoginButton(loginPage);

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
        String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return webClient.getPage(BASE_URI.resolve(path).toString());
    }

    private HtmlInput findInputByIdSuffix(HtmlPage page, String idSuffix) {
        List<HtmlInput> inputs = page.getByXPath("//input");
        for (HtmlInput input : inputs) {
            String id = input.getId();
            String name = input.getNameAttribute();
            if (idSuffix.equals(id) || id.endsWith(":" + idSuffix)
                    || idSuffix.equals(name) || name.endsWith(":" + idSuffix)) {
                return input;
            }
        }
        fail("Could not find input field ending with '" + idSuffix + "' on " + page.getUrl());
        return null;
    }

    private HtmlPage clickLoginButton(HtmlPage page) throws IOException {
        List<HtmlElement> submitButtons = page.getByXPath("//input[@type='submit']");
        for (HtmlElement submitButton : submitButtons) {
            if ("login".equalsIgnoreCase(submitButton.getAttribute("value"))) {
                return submitButton.click();
            }
        }
        fail("Could not find Login submit button on " + page.getUrl());
        return null;
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

    private static URI createBaseUri() {
        String baseUrl = System.getProperty("app.baseUrl", "http://localhost:8080/team-18/");
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        return URI.create(baseUrl);
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
