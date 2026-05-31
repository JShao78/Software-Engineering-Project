package de.pdbm.team18.htmlunit.customers;

import de.pdbm.team18.shared.HtmlUnitTestUtils;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSubmitInput;
import org.htmlunit.html.HtmlTableRow;
import org.htmlunit.html.HtmlTextInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CustomerDeleteHtmlUnitIT {

    private WebClient webClient;

    @BeforeEach
    public void setUp() throws IOException {
        webClient = HtmlUnitTestUtils.createWebClient();
        HtmlUnitTestUtils.login(webClient, HtmlUnitTestUtils.UserGroup.ADMIN);
    }

    @AfterEach
    public void tearDown() {
        if (webClient != null) webClient.close();
    }

    @Test
    public void testDeleteCustomerSuccessfully() throws Exception {
        HtmlPage page = webClient.getPage(HtmlUnitTestUtils.resolveUrl("kunden.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);

        String email = "test.delete." + System.currentTimeMillis() + "@test.com";
        HtmlElement add = page.getFirstByXPath("//a[contains(@class,'add-modal-btn')]");
        page = add.click();
        HtmlUnitTestUtils.waitForJs(webClient);

        page.<HtmlTextInput>getHtmlElementById("kundenForm:vorname").setValueAttribute("Test");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:nachname").setValueAttribute("Delete");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:email").setValueAttribute(email);
        page.<HtmlTextInput>getHtmlElementById("kundenForm:phone").setValueAttribute("000-0000");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:street").setValueAttribute("Test Street 1");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:state").setValueAttribute("TestState");
        page.<HtmlTextInput>getHtmlElementById("kundenForm:zipCode").setValueAttribute("00000");

        HtmlSubmitInput save = page.getFirstByXPath("//input[@value='Speichern']");
        save.click();
        HtmlUnitTestUtils.waitForJs(webClient);

        page = webClient.getPage(HtmlUnitTestUtils.resolveUrl("kunden.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);
        HtmlTableRow row = findRowByEmail(page, email);
        assertNotNull(row, "Der angelegte Kunde " + email + " sollte in der Liste erscheinen");

        HtmlSubmitInput deleteBtn = row.getFirstByXPath(".//td[contains(@class,'delete')]//input[@value='×']");
        deleteBtn.click();
        HtmlUnitTestUtils.waitForJs(webClient);

        page = webClient.getPage(HtmlUnitTestUtils.resolveUrl("kunden.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);
        assertNull(findRowByEmail(page, email),
                "Kunde " + email + " sollte gelöscht sein");
    }

    @Test
    public void testDeleteCustomerWithOrders() throws Exception {
        String email = "daryl.spence@aol.com";

        HtmlPage page = webClient.getPage(HtmlUnitTestUtils.resolveUrl("kunden.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);

        HtmlTableRow row = findRowByEmail(page, email);
        assertNotNull(row, "Vorbedingung: Kunde " + email + " muss in den Seed-Daten existieren");

        HtmlSubmitInput deleteBtn = row.getFirstByXPath(".//td[contains(@class,'delete')]//input[@value='×']");
        deleteBtn.click();
        HtmlUnitTestUtils.waitForJs(webClient);

        page = webClient.getPage(HtmlUnitTestUtils.resolveUrl("kunden.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);
        assertNotNull(findRowByEmail(page, email),
                "Kunde " + email + " (hat Bestellungen) sollte noch existieren — FK-Constraint muss das Löschen verhindern");
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
            HtmlUnitTestUtils.waitForJs(webClient);
        }
        return null;
    }
}
