package de.pdbm.team18.htmlunit.orders;

import de.pdbm.team18.shared.HtmlUnitTestUtils;
import org.htmlunit.ElementNotFoundException;
import org.htmlunit.WebClient;
import org.htmlunit.html.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

public class EditOrderIT {

    private WebClient webClient;
    private HtmlPage page;

    @BeforeEach
    public void setUp() throws IOException {
        webClient = HtmlUnitTestUtils.createWebClient(true);

        HtmlUnitTestUtils.login(webClient, HtmlUnitTestUtils.UserGroup.ADMIN);
        page = webClient.getPage(HtmlUnitTestUtils.resolveUrl("bestellungen.xhtml"));
        page = HtmlUnitTestUtils.changePage(page, 1);
        HtmlUnitTestUtils.waitForJs(webClient);
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

    private HtmlElement openEditModal() throws IOException {
        int pageNumber = 1;
        HtmlElement row = page.getFirstByXPath(
                "//tr[td[@class='status']//span[@class='status-pill status-processing']]"
        );
        while (row == null) {
            page = HtmlUnitTestUtils.changePage(page, ++pageNumber);
            HtmlUnitTestUtils.waitForJs(webClient);
            row = page.getFirstByXPath(
                    "//tr[td[@class='status']//span[@class='status-pill status-processing']]"
            );
        }

        HtmlElement editButton = row.getFirstByXPath(".//*[text()='Bearbeiten']");
        page = editButton.click();
        HtmlUnitTestUtils.waitForJs(webClient);

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

    private Map<String, Integer> getStockAmount(String storeName, Collection<String> productNames) throws IOException {
        Map<String, Integer> stocks = new HashMap<>();
        HtmlPage stocksPage = HtmlUnitTestUtils.login(HtmlUnitTestUtils.createWebClient(true), HtmlUnitTestUtils.UserGroup.ADMIN)
                .getPage(HtmlUnitTestUtils.baseUrl + "stocks.xhtml");
        for (String productName : productNames) {
            stocks.put(productName, sumStockAmount(stocksPage, storeName, productName));
        }
        return stocks;
    }

    private int sumStockAmount(HtmlPage stocksPage, String storeName, String productName) throws IOException {
        int pageNumber = 1;
        int stockAmount = 0;
        boolean found = false;
        HtmlPage currentPage = stocksPage;

        while (true) {
            List<HtmlElement> rows = currentPage.getByXPath(
                    ".//tbody/tr[td[@class='storeName' and normalize-space(.)=\"" + storeName + "\"]"
                            + " and td[@class='productName' and normalize-space(.)=\"" + productName + "\"]]"
            );

            for (HtmlElement row : rows) {
                stockAmount += Integer.parseInt(
                        row.<HtmlElement>getFirstByXPath(".//td[@class='stock']").getVisibleText().strip()
                );
                found = true;
            }

            HtmlPage nextPage = HtmlUnitTestUtils.changePage(currentPage, ++pageNumber);
            if (nextPage == currentPage) {
                break;
            }
            currentPage = nextPage;
        }

        Assertions.assertTrue(found, "No stock row found for " + productName + " in " + storeName);
        return stockAmount;
    }

    @Test
    public void testStockUpdateOnOrderComplete() throws IOException {
        int orderID = editOrder(false);
        HtmlElement row = HtmlUnitTestUtils.findRow(page, orderID);
        String storeName = row.<HtmlElement>getFirstByXPath(
                ".//td[@class='storeName']"
        ).getVisibleText().strip();
        page = row.<HtmlElement>getFirstByXPath(".//a[span[@class='items-toggle-icon']]").click();
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);
        webClient.waitForBackgroundJavaScript(10000);
        HtmlElement itemsTable = page.getFirstByXPath("//table[@class='items-table']");
        HtmlElement itemsTableBody = itemsTable.getFirstByXPath(".//tbody");

        Map<String, Integer> items = new HashMap<>();

        List<HtmlElement> orderItems = itemsTableBody.getByXPath(".//tr");
        for (HtmlElement orderItem : orderItems) {
            List<HtmlElement> cells = orderItem.getByXPath(".//td");
            List<String> cellValues = cells.stream().map(DomNode::getVisibleText).toList();

            String productName = cellValues.get(1).strip();
            int splitIndex = productName.lastIndexOf('-');
            if (splitIndex != -1) productName = productName.substring(0, splitIndex-1);
            int quantity = Integer.parseInt(cellValues.get(2).strip());

            items.put(productName, quantity);
        }

        Map<String, Integer> preEditStocks = getStockAmount(storeName, items.keySet());

        editOrder(true);
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);
        webClient.waitForBackgroundJavaScript(10000);
        Map<String, Integer> postEditStocks = getStockAmount(storeName, items.keySet());

        for (String productName : items.keySet()) {
            Assertions.assertEquals(
                    preEditStocks.get(productName) - items.get(productName),
                    postEditStocks.get(productName),
                    "Failed to update " + productName +
                            " from " + preEditStocks.get(productName) +  " to " + postEditStocks.get(productName)
            );
        }
    }

    @Test
    public void testEditOrderSuccess() throws IOException {
        int id = editOrder(true);
        HtmlUnitTestUtils.waitForJs(webClient);
        HtmlElement row = HtmlUnitTestUtils.findRow(page, id);
        String status = getStatus(row);
        Assertions.assertEquals("Completed", status);
    }

    @Test
    public void testEditOrderCancel() throws IOException {
        int id = editOrder(false);
        HtmlElement row = HtmlUnitTestUtils.findRow(page, id);
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
            page = HtmlUnitTestUtils.changePage(page, ++pageNumber);
        }

        HtmlElement button = row.getFirstByXPath(".//*[text()='Bearbeiten']");
        if (button != null) page = button.click();
        HtmlUnitTestUtils.waitForJs(webClient);
        Assertions.assertThrowsExactly(ElementNotFoundException.class,
                () -> page.getHtmlElementById("ordersForm:editOrderPanel"),
                "Order Edit Modal für Bestellstatus 'Completed' geöffnet"
        );
    }

}
