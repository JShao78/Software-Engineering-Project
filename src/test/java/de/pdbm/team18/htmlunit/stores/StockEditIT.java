package de.pdbm.team18.htmlunit.stores;

import org.htmlunit.BrowserVersion;
import org.htmlunit.WebClient;
import org.htmlunit.html.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StockEditIT {
    private WebClient webClient;
    private String baseUrl = System.getProperty("app.baseUrl", "http://localhost:8080/team-18/");

    @BeforeEach
    void setUp() throws Exception {
        webClient = new WebClient(BrowserVersion.BEST_SUPPORTED);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.setIncorrectnessListener((message, origin) -> {});
        HtmlPage loginPage = webClient.getPage(baseUrl + "login.xhtml");
        loginPage.<HtmlInput>getHtmlElementById("loginForm:username")
                .type("fabiola.jackson@bikes.shop");
        loginPage.<HtmlInput>getHtmlElementById("loginForm:password")
                .type("555-5554");
        HtmlPage afterLogin = loginPage
                .<HtmlElement>getHtmlElementById("loginForm:loginButton")
                .click();
        webClient.waitForBackgroundJavaScript(2000);

        String urlAfterLogin = afterLogin.getUrl().toString();
        assertFalse(urlAfterLogin.contains("login.xhtml"),
                "Login fehlgeschlagen – URL nach Login: " + urlAfterLogin);
    }

    @AfterEach
    void tearDown() {
        if (webClient != null) {
            webClient.close();
        }
    }

    @Test
    public void testEditStock() throws Exception {

        HtmlPage stockPage = webClient.getPage(baseUrl + "stocks.xhtml");
        webClient.waitForBackgroundJavaScript(2000);


        int originalQuantity = readFirstRowQuantity(stockPage);
        int newQuantity = originalQuantity + 10;


        HtmlTable table = (HtmlTable) stockPage.getFirstByXPath("//form[@id='stocksTableForm']//table");
        HtmlTableRow firstDataRow = table.getRows().stream()
                .filter(r -> r.getCell(0) instanceof HtmlTableDataCell)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Keine Datenzeile gefunden"));


        HtmlSubmitInput editButton = firstDataRow.getFirstByXPath(".//input[@value='Bearbeiten']");
        assertNotNull(editButton, "Bearbeiten-Button nicht gefunden");
        HtmlPage modalPage = editButton.click();
        webClient.waitForBackgroundJavaScript(2000);


        HtmlInput quantityInput = (HtmlInput) modalPage.getElementById("editStockForm:quantity");
        assertNotNull(quantityInput, "Input-Feld für Quantity nicht gefunden");
        quantityInput.setValueAttribute(String.valueOf(newQuantity));


        HtmlSubmitInput saveButton = modalPage.getFirstByXPath("//form[@id='editStockForm']//input[@value='Speichern']");
        assertNotNull(saveButton, "Speichern-Button nicht gefunden");
        HtmlPage updatedPage = saveButton.click();
        webClient.waitForBackgroundJavaScript(2000);


        int updatedQuantity = readFirstRowQuantity(updatedPage);
        assertEquals(newQuantity, updatedQuantity, "Der Lagerbestand wurde nicht korrekt aktualisiert");
    }

    private int readFirstRowQuantity(HtmlPage stockPage) {
        HtmlTable table = (HtmlTable) stockPage.getFirstByXPath("//form[@id='stocksTableForm']//table");
        assertNotNull(table, "Stocks Tabelle nicht gefunden");

        HtmlTableRow row = table.getRows().stream()
                .filter(r -> r.getCell(0) instanceof HtmlTableDataCell)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Keine Datenzeile gefunden"));
        HtmlTableCell cell = row.getCell(4);
        return Integer.parseInt(cell.getTextContent().trim());
    }
}