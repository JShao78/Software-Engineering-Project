package de.pdbm.team18.htmlunit.orders;

import de.pdbm.team18.shared.HtmlUnitTestUtils;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlInput;
import org.htmlunit.html.HtmlOption;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSelect;
import org.htmlunit.html.HtmlSubmitInput;
import org.htmlunit.html.HtmlTextInput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateOrderIT {
    private WebClient webClient;

    @BeforeEach
    void setUp() throws IOException {
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
    void shouldNotCreateOrderWhenProductHasNoStock() throws Exception {
        String productName = "IT Zero Stock Product " + System.currentTimeMillis();
        createProduct(productName);

        HtmlPage ordersPage = webClient.getPage(HtmlUnitTestUtils.resolveUrl("bestellungen.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);
        int ordersBefore = readCountFromSubtitle(ordersPage, "Bestellungen");

        String storeName = findStoreNameById(1);
        ordersPage = submitOrder(ordersPage, "1", storeName, "1", productName);

        String messageText = readCreateOrderMessages(ordersPage);
        assertTrue(
                messageText.contains("Systemfehler")
                        || messageText.contains("Bestellung konnte nicht erstellt werden")
                        || messageText.contains("Bestellung konnte nicht erzeugt werden"),
                "Es wurde keine erwartete Fehlermeldung angezeigt.\nNachricht: " + messageText
        );

        HtmlPage refreshedOrdersPage = webClient.getPage(HtmlUnitTestUtils.resolveUrl("bestellungen.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);
        int ordersAfter = readCountFromSubtitle(refreshedOrdersPage, "Bestellungen");
        assertEquals(ordersBefore, ordersAfter, "Trotz fehlendem Lagerbestand wurde eine Bestellung erstellt.");
    }

    @Test
    void shouldCreateOrderForProductWithStockInStoreOne() throws Exception {
        HtmlPage ordersPage = webClient.getPage(HtmlUnitTestUtils.resolveUrl("bestellungen.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);
        int ordersBefore = readCountFromSubtitle(ordersPage, "Bestellungen");

        String storeName = findStoreNameById(1);
        ordersPage = submitOrder(ordersPage, "1", storeName, "1", "1");

        String messageText = readCreateOrderMessages(ordersPage);
        assertTrue(
                messageText.contains("Bestellung erfolgreich erstellt"),
                "Es wurde keine Erfolgsmeldung angezeigt.\nNachricht: " + messageText
        );

        HtmlPage refreshedOrdersPage = webClient.getPage(HtmlUnitTestUtils.resolveUrl("bestellungen.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);
        int ordersAfter = readCountFromSubtitle(refreshedOrdersPage, "Bestellungen");
        assertEquals(
                ordersBefore + 1,
                ordersAfter,
                "Die Bestellung mit Produkt-ID 1 in Niederlassung-ID 1 wurde nicht erstellt."
        );
    }

    @Test
    void shouldCreateOrderWithTwoProductsWithStockInStoreOne() throws Exception {
        HtmlPage ordersPage = webClient.getPage(HtmlUnitTestUtils.resolveUrl("bestellungen.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);
        int ordersBefore = readCountFromSubtitle(ordersPage, "Bestellungen");

        String storeName = findStoreNameById(1);
        ordersPage = submitOrder(ordersPage, "1", storeName, "1", "1", "2");

        String messageText = readCreateOrderMessages(ordersPage);
        assertTrue(
                messageText.contains("Bestellung erfolgreich erstellt"),
                "Es wurde keine Erfolgsmeldung für die Bestellung mit zwei Produkten angezeigt.\nNachricht: " + messageText
        );

        HtmlPage refreshedOrdersPage = webClient.getPage(HtmlUnitTestUtils.resolveUrl("bestellungen.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);
        int ordersAfter = readCountFromSubtitle(refreshedOrdersPage, "Bestellungen");
        assertEquals(
                ordersBefore + 1,
                ordersAfter,
                "Die Bestellung mit Produkt-ID 1 und Produkt-ID 2 in Niederlassung-ID 1 wurde nicht erstellt."
        );
    }

    private void createProduct(String productName) throws IOException {
        HtmlPage productPage = webClient.getPage(HtmlUnitTestUtils.resolveUrl("produkte.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);

        int countBefore = readCountFromSubtitle(productPage, "Produkte");

        productPage.<HtmlTextInput>getHtmlElementById("addProductForm:productName")
                .setValueAttribute(productName);
        productPage.<HtmlInput>getHtmlElementById("addProductForm:modelYear")
                .setValueAttribute(String.valueOf(LocalDate.now().getYear()));
        productPage.<HtmlInput>getHtmlElementById("addProductForm:listPrice")
                .setValueAttribute("999.99");

        selectFirstRealOption(productPage.<HtmlSelect>getHtmlElementById("addProductForm:brandId"));
        selectFirstRealOption(productPage.<HtmlSelect>getHtmlElementById("addProductForm:categoryId"));

        HtmlSubmitInput saveProduct = productPage.getFirstByXPath(
                "//form[@id='addProductForm']//input[@type='submit' and @value='Speichern']"
        );
        assertNotNull(saveProduct, "Speichern-Button im Produktformular wurde nicht gefunden.");
        saveProduct.click();
        HtmlUnitTestUtils.waitForJs(webClient);

        HtmlPage refreshedProductPage = webClient.getPage(HtmlUnitTestUtils.resolveUrl("produkte.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);
        int countAfter = readCountFromSubtitle(refreshedProductPage, "Produkte");
        assertEquals(countBefore + 1, countAfter, "Das neue Produkt wurde nicht angelegt.");
    }

    private HtmlPage submitOrder(
            HtmlPage ordersPage,
            String customerRef,
            String storeName,
            String staffRef,
            String... productRefs
    ) throws IOException {
        ordersPage.<HtmlInput>getHtmlElementById("ordersForm:createCustomerInput")
                .setValueAttribute(customerRef);
        ordersPage.<HtmlInput>getHtmlElementById("ordersForm:createStoreInput")
                .setValueAttribute(storeName);
        ordersPage.<HtmlInput>getHtmlElementById("ordersForm:createStaffInput")
                .setValueAttribute(staffRef);
        ordersPage.<HtmlInput>getHtmlElementById("ordersForm:createRequiredDateInput")
                .setValueAttribute(LocalDate.now().plusDays(10).toString());

        for (String productRef : productRefs) {
            ordersPage.<HtmlInput>getHtmlElementById("ordersForm:createProductInput")
                    .setValueAttribute(productRef);
            ordersPage.<HtmlInput>getHtmlElementById("ordersForm:createQuantityInput")
                    .setValueAttribute("1");
            ordersPage.<HtmlInput>getHtmlElementById("ordersForm:createDiscountInput")
                    .setValueAttribute("0");

            HtmlSubmitInput addProductButton = ordersPage.getHtmlElementById("ordersForm:addProductBtn");
            ordersPage = addProductButton.click();
            HtmlUnitTestUtils.waitForJs(webClient);
        }

        HtmlElement itemsPanel = ordersPage.getHtmlElementById("ordersForm:createOrderItemsPanel");
        assertFalse(
                itemsPanel.asNormalizedText().contains("Noch keine Produkte hinzugefügt."),
                "Das Produkt wurde nicht zur Bestellung hinzugefügt."
        );

        HtmlSubmitInput saveOrderButton = ordersPage.getFirstByXPath(
                "//form[@id='ordersForm']//input[@type='submit' and @value='Bestellung speichern']"
        );
        assertNotNull(saveOrderButton, "Bestellung speichern-Button wurde nicht gefunden.");

        ordersPage = saveOrderButton.click();
        HtmlUnitTestUtils.waitForJs(webClient);
        return ordersPage;
    }

    private void selectFirstRealOption(HtmlSelect select) {
        for (HtmlOption option : select.getOptions()) {
            String value = option.getValueAttribute();
            if (value != null && !value.isBlank() && !"null".equalsIgnoreCase(value)) {
                select.setSelectedAttribute(option, true);
                return;
            }
        }
        throw new IllegalStateException("Keine gültige Option in Selectbox gefunden: " + select.getId());
    }

    private String findStoreNameById(int storeId) throws IOException {
        HtmlPage storePage = webClient.getPage(HtmlUnitTestUtils.resolveUrl("stores.xhtml"));
        HtmlUnitTestUtils.waitForJs(webClient);

        HtmlElement row = storePage.getFirstByXPath(
                "//tr[td[contains(@class,'tableId') and normalize-space()='" + storeId + "']]"
        );
        assertNotNull(row, "Niederlassung mit ID " + storeId + " wurde nicht gefunden.");

        HtmlElement storeNameCell = row.getFirstByXPath(".//td[contains(@class,'storeName')]");
        assertNotNull(storeNameCell, "Niederlassungsname für ID " + storeId + " wurde nicht gefunden.");
        return storeNameCell.asNormalizedText();
    }

    private String readCreateOrderMessages(HtmlPage ordersPage) {
        HtmlElement messageBox = ordersPage.getFirstByXPath(
                "//*[@id='ordersForm:createOrderMessages' or @id='createOrderMessages']"
        );
        assertNotNull(messageBox, "Nachrichten-Container im Bestellmodal wurde nicht gefunden.");
        return messageBox.asNormalizedText();
    }

    private int readCountFromSubtitle(HtmlPage page, String label) {
        HtmlElement subtitle = page.getFirstByXPath("//p[contains(@class,'page-subtitle')]");
        assertNotNull(subtitle, "Seitenuntertitel mit Anzahl für '" + label + "' wurde nicht gefunden.");

        Matcher matcher = Pattern.compile("\\d+").matcher(subtitle.asNormalizedText());
        assertTrue(matcher.find(), "Keine Anzahl im Untertitel gefunden: " + subtitle.asNormalizedText());
        return Integer.parseInt(matcher.group());
    }
}
