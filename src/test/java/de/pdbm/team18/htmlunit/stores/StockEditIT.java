package de.pdbm.team18.htmlunit.stores;

import de.pdbm.team18.shared.HtmlUnitTestUtils;
import org.htmlunit.WebClient;
import org.htmlunit.html.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StockEditIT {
    private WebClient webClient;

    @BeforeEach
    void setUp() throws Exception {
        webClient = HtmlUnitTestUtils.createWebClient();
        HtmlUnitTestUtils.login(webClient, HtmlUnitTestUtils.UserGroup.ADMIN);
    }

    @AfterEach
    void tearDown() {
        if (webClient != null) {
            webClient.close();
        }
    }

    @Test
    public void testEditStock() throws Exception {

        HtmlPage stockPage = webClient.getPage(HtmlUnitTestUtils.resolveUrl("stocks.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);


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
        HtmlUnitTestUtils.waitForJs(webClient);


        HtmlInput quantityInput = (HtmlInput) modalPage.getElementById("editStockForm:quantity");
        assertNotNull(quantityInput, "Input-Feld für Quantity nicht gefunden");
        quantityInput.setValueAttribute(String.valueOf(newQuantity));


        HtmlSubmitInput saveButton = modalPage.getFirstByXPath("//form[@id='editStockForm']//input[@value='Speichern']");
        assertNotNull(saveButton, "Speichern-Button nicht gefunden");
        HtmlPage updatedPage = saveButton.click();
        HtmlUnitTestUtils.waitForJs(webClient);


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
