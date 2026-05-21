package de.pdbm.team18.htmlunit.customers;

import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSubmitInput;
import org.htmlunit.html.HtmlTableRow;
import org.htmlunit.html.HtmlTextInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public class CustomerDeleteHtmlUnitIT {

    private static final String baseUrl = System.getProperty("app.baseUrl", "http://localhost:8080/team-18/");
    private WebClient webClient;

    @BeforeEach
    public void setUp() {
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.setIncorrectnessListener((message, origin) -> {});
    }

    @AfterEach
    public void tearDown() {
        if (webClient != null) webClient.close();
    }

    @Test
    public void testDeleteCustomerSuccessfully() throws Exception {
        HtmlPage loginPage = webClient.getPage(baseUrl + "login.xhtml");
        findInputByIdSuffix(loginPage, "username").setValueAttribute("fabiola.jackson@bikes.shop");
        findInputByIdSuffix(loginPage, "password").setValueAttribute("555-5554");
        clickLoginButton(loginPage);
        webClient.waitForBackgroundJavaScript(2000);

        HtmlPage page = webClient.getPage(baseUrl + "kunden.xhtml");
        webClient.waitForBackgroundJavaScript(2000);

        String email = "test.delete." + System.currentTimeMillis() + "@test.com";
        HtmlElement add = page.getFirstByXPath("//a[contains(@class,'add-modal-btn')]");
        page = add.click();
        webClient.waitForBackgroundJavaScript(1000);

        page.<HtmlTextInput>getHtmlElementById("kundenForm:vorname").setValueAttribute("Test");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:nachname").setValueAttribute("Delete");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:email").setValueAttribute(email);
        page.<HtmlTextInput>getHtmlElementById("kundenForm:phone").setValueAttribute("000-0000");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:street").setValueAttribute("Test Street 1");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:state").setValueAttribute("TestState");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:zipCode").setValueAttribute("00000");

        HtmlSubmitInput save = page.getFirstByXPath("//input[@value='Speichern']");
        save.click();
        webClient.waitForBackgroundJavaScript(2000);

        page = webClient.getPage(baseUrl + "kunden.xhtml");
        webClient.waitForBackgroundJavaScript(2000);
        HtmlTableRow row = findRowByEmail(page, email);
        assertNotNull(row, "Der angelegte Kunde " + email + " sollte in der Liste erscheinen");

        HtmlSubmitInput deleteBtn = row.getFirstByXPath(".//td[contains(@class,'delete')]//input[@value='×']");
        deleteBtn.click();
        webClient.waitForBackgroundJavaScript(2000);

        page = webClient.getPage(baseUrl + "kunden.xhtml");
        webClient.waitForBackgroundJavaScript(2000);
        assertNull(findRowByEmail(page, email),
                "Kunde " + email + " sollte gelöscht sein");
    }

    @Test
    public void testDeleteCustomerWithOrders() throws Exception {
        String email = "daryl.spence@aol.com";

        HtmlPage loginPage = webClient.getPage(baseUrl + "login.xhtml");
        findInputByIdSuffix(loginPage, "username").setValueAttribute("fabiola.jackson@bikes.shop");
        findInputByIdSuffix(loginPage, "password").setValueAttribute("555-5554");
        clickLoginButton(loginPage);
        webClient.waitForBackgroundJavaScript(2000);

        HtmlPage page = webClient.getPage(baseUrl + "kunden.xhtml");
        webClient.waitForBackgroundJavaScript(2000);

        HtmlTableRow row = findRowByEmail(page, email);
        assertNotNull(row, "Vorbedingung: Kunde " + email + " muss in den Seed-Daten existieren");

        HtmlSubmitInput deleteBtn = row.getFirstByXPath(".//td[contains(@class,'delete')]//input[@value='×']");
        deleteBtn.click();
        webClient.waitForBackgroundJavaScript(2000);

        page = webClient.getPage(baseUrl + "kunden.xhtml");
        webClient.waitForBackgroundJavaScript(2000);
        assertNotNull(findRowByEmail(page, email),
                "Kunde " + email + " (hat Bestellungen) sollte noch existieren — FK-Constraint muss das Löschen verhindern");
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

    private HtmlTableRow findRowByEmail(HtmlPage page, String email) throws IOException {
        String xpath = "//form[@id='customerTableForm']//tr["
                + "td[contains(@class,'email') and normalize-space(.)='" + email + "']]";

        for (int i = 0; i < 200; i++) {
            HtmlTableRow row = page.getFirstByXPath(xpath);
            if (row != null) return row;

            HtmlSubmitInput next = page.getFirstByXPath("//input[@value='›']");
            if (next == null || next.hasAttribute("disabled")) break;
            page = next.click();
            webClient.waitForBackgroundJavaScript(1000);
        }
        return null;
    }
}