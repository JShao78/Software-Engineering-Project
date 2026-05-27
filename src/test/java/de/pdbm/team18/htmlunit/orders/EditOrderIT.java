package de.pdbm.team18.htmlunit.orders;

import de.pdbm.team18.shared.HtmlUnitTestUtils;
import org.htmlunit.BrowserVersion;
import org.htmlunit.ElementNotFoundException;
import org.htmlunit.WebClient;
import org.htmlunit.html.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class EditOrderIT {

    private static final String baseUrl = System.getProperty("baseUrl", "http://localhost:8080/team-18/");
    private WebClient webClient;
    private HtmlPage page;

    @BeforeEach
    public void setUp() throws IOException {
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.setIncorrectnessListener((message, origin) -> {});

        HtmlUnitTestUtils.login(webClient, HtmlUnitTestUtils.UserGroup.ADMIN);
        page = webClient.getPage(baseUrl + "bestellungen.xhtml");
        page = HtmlUnitTestUtils.changePage(page, 1);
        webClient.waitForBackgroundJavaScript(10000);
    }

    @AfterEach
    public void tearDown() {
        if (webClient != null) webClient.close();
    }

    private String getStatus(HtmlElement row) {
        return row.<HtmlElement>getFirstByXPath(
                ".//span[contains(@class, 'status-pill')]"
        ).getVisibleText().strip();
    }

    private HtmlElement findRow(int id) {
        return page.getFirstByXPath("//tr[td[span[text()='" + id + "']]]");
    }

    private HtmlElement openEditModal() throws IOException {
        int pageNumber = 1;
        HtmlElement row = page.getFirstByXPath(
                "//tr[td[@class='status']//span[@class='status-pill status-processing']]"
        );
        while (row == null) {
            page = HtmlUnitTestUtils.changePage(page, ++pageNumber);
            webClient.waitForBackgroundJavaScript(10000);
            row = page.getFirstByXPath(
                    "//tr[td[@class='status']//span[@class='status-pill status-processing']]"
            );
        }

        HtmlElement editButton = row.getFirstByXPath(".//*[text()='Bearbeiten']");
        page = editButton.click();
        webClient.waitForBackgroundJavaScript(10000);

        HtmlElement editModal = page.getHtmlElementById("ordersForm:editOrderPanel");
        Assertions.assertNotNull(editModal, "Order Edit Modal Nicht geöffnet");
        return editModal;
    }

    private int editOrder(boolean confirm) throws IOException {
        HtmlElement editModal = openEditModal();
        HtmlElement idCard = editModal.getFirstByXPath(
                ".//div[parent::div[.//div[contains(., 'ID')]] and @class='edit-info-value']"
        );
        int id = Integer.parseInt(idCard.getVisibleText().strip());

        HtmlSelect statusSelect = editModal.getFirstByXPath(".//select");
        HtmlOption newStatusSelected = statusSelect.getFirstByXPath(".//option[contains(., 'Completed')]");
        newStatusSelected.setSelected(true);

        if (confirm) {
            page = editModal.<HtmlInput>getFirstByXPath(".//input[@value='Speichern']").click();
        } else {
            page = editModal.<HtmlInput>getFirstByXPath(".//input[@value='Abbrechen']").click();
        }
        return id;
    }

    @Test
    public void testEditOrderSuccess() throws IOException {
        int id = editOrder(true);
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);
        webClient.waitForBackgroundJavaScript(10000);
        HtmlElement row = findRow(id);
        String status = getStatus(row);
        Assertions.assertEquals("Completed", status);
    }

    @Test
    public void testEditOrderCancel() throws IOException {
        int id = editOrder(false);
        HtmlElement row = findRow(id);
        String status = getStatus(row);
        Assertions.assertNotEquals("Completed", status);
    }

    @Test
    public void testEditOrderCompleted() throws IOException {
        int pageNumber = 1;
        HtmlElement row = page.getFirstByXPath(
                "//tr[td[@class='status']//span[contains(@class, 'status-completed')]]"
        );
        while (row == null) {
            row = page.getFirstByXPath(
                    "//tr[td[@class='status']//span[contains(@class, 'status-completed')]]"
            );
            HtmlUnitTestUtils.changePage(page, ++pageNumber);
        }

        HtmlElement button = row.getFirstByXPath(".//*[text()='Bearbeiten']");
        if (button != null) page = button.click();
        webClient.waitForBackgroundJavaScript(10000);
        Assertions.assertThrowsExactly(ElementNotFoundException.class,
                () -> page.getHtmlElementById("ordersForm:editOrderPanel"),
                "Order Edit Modal für Bestellstatus 'Completed' geöffnet"
        );
    }

}
