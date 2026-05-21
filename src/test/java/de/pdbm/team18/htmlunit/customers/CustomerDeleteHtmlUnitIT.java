package de.pdbm.team18.htmlunit.customers;

import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSubmitInput;
import org.htmlunit.html.HtmlTextInput;
import org.htmlunit.html.HtmlTableRow;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class CustomerDeleteHtmlUnitIT {

    private WebClient webClient;
    private final String baseUrl = System.getProperty("app.baseUrl", "http://localhost:8080/team-18/");

    @BeforeEach
    public void setUp() {
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
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
        HtmlPage page = clickLoginButton(loginPage);
        webClient.waitForBackgroundJavaScript(2000);

        page = webClient.getPage(baseUrl + "kunden.xhtml");
        webClient.waitForBackgroundJavaScript(2000);

        HtmlElement addButton = page.getFirstByXPath("//a[contains(@class, 'add-modal-btn')]");
        page = addButton.click();
        webClient.waitForBackgroundJavaScript(1000);

        String testEmail = "test.delete.me@test.com";
        page.<HtmlTextInput>getHtmlElementById("kundenForm:vorname").setValueAttribute("Test");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:nachname").setValueAttribute("DeleteMe");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:email").setValueAttribute(testEmail);
        page.<HtmlTextInput>getHtmlElementById("kundenForm:phone").setValueAttribute("000-0000");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:street").setValueAttribute("Test Street 1");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:state").setValueAttribute("TestState");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:zipCode").setValueAttribute("00000");

        HtmlSubmitInput saveButton = page.getFirstByXPath("//input[@value='Speichern']");
        page = saveButton.click();

        boolean isCustomerFound = page.asNormalizedText().contains(testEmail);

        if (!isCustomerFound) {
            HtmlSubmitInput lastPageBtn = page.getFirstByXPath("//input[@value='›']/preceding-sibling::input[contains(@class, 'btn-page')][1]");
            if (lastPageBtn != null && !lastPageBtn.hasAttribute("disabled") && !lastPageBtn.getAttribute("class").contains("btn-page-active")) {
                page = lastPageBtn.click();
                isCustomerFound = page.asNormalizedText().contains(testEmail);
            }
        }

        if (!isCustomerFound) {
            HtmlSubmitInput firstPageBtn = page.getFirstByXPath("//input[@value='1']");
            if (firstPageBtn != null) page = firstPageBtn.click();

            int maxPagesToSearch = 200;
            int currentPage = 1;

            while (!isCustomerFound && currentPage < maxPagesToSearch) {
                HtmlSubmitInput nextButton = page.getFirstByXPath("//input[@value='›']");
                if (nextButton == null || nextButton.hasAttribute("disabled")) break;

                page = nextButton.click();
                isCustomerFound = page.asNormalizedText().contains(testEmail);
                currentPage++;
            }
        }

        HtmlTableRow targetRow = page.getFirstByXPath(
                "//form[@id='customerTableForm']//tr[td[contains(., '" + testEmail + "')]]");
        HtmlSubmitInput deleteButton = targetRow.getFirstByXPath(".//input[@value='×']");
        page = deleteButton.click();

        page = webClient.getPage(baseUrl + "kunden.xhtml");
        assertFalse(page.asNormalizedText().contains(testEmail),
                "Der Kunde mit der E-Mail " + testEmail + " sollte nach dem Löschen nicht mehr sichtbar sein.");
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
}
