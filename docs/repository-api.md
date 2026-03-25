# Repository API (JPA Datenzugriff)

Diese Datei dokumentiert die aktuell implementierten Repository-Funktionen
fuer den Datenzugriff (Interface + JPA-Implementierung).

## Geltungsbereich

- `business/customers/repository/*`
- `business/products/repository/*`
- `business/orders/repository/*`

## Gemeinsame Konventionen

- Paging-Parameter werden ueber `RepositoryQueryUtils` bereinigt:
  - negativer `offset` wird zu `0`
  - `limit <= 0` wird zu einem Default-Limit
  - sehr grosse `limit`-Werte werden auf ein Maximum begrenzt
- Suchbegriffe werden case-insensitive verarbeitet.
- `save(...)` nutzt:
  - `persist` fuer neue Entities (ohne ID)
  - `merge` fuer bestehende Entities (mit ID)

## CustomerRepository

Datei: `src/main/java/de/pdbm/team18/business/customers/repository/CustomerRepository.java`

- `List<Customer> findPage(String searchTerm, int offset, int limit)`
  Liefert eine paginierte Kundenliste (optional gefiltert ueber Name/E-Mail).
- `long count(String searchTerm)`
  Liefert die Gesamtanzahl passend zum Suchfilter.
- `Optional<Customer> findById(UUID id)`
  Liefert einen Kunden per ID.
- `Customer save(Customer customer)`
  Speichert neuen oder geaenderten Kunden.

## ProductRepository

Datei: `src/main/java/de/pdbm/team18/business/products/repository/ProductRepository.java`

- `List<Product> findPage(String searchTerm, int offset, int limit)`
  Liefert eine paginierte Produktliste (optional gefiltert ueber Name/Nummer).
- `long count(String searchTerm)`
  Liefert die Gesamtanzahl passend zum Suchfilter.
- `Optional<Product> findById(UUID id)`
  Liefert ein Produkt per ID.
- `List<Product> findByIds(Set<UUID> ids)`
  Liefert mehrere Produkte in einem Query anhand einer ID-Menge.
- `Optional<Product> findByProductNumber(String productNumber)`
  Liefert ein Produkt ueber die Produktnummer.
- `Product save(Product product)`
  Speichert neues oder geaendertes Produkt.
- `boolean updateStock(UUID productId, int newStock)`
  Setzt den Lagerbestand eines Produkts; `false`, falls Produkt nicht gefunden.

## OrderRepository

Datei: `src/main/java/de/pdbm/team18/business/orders/repository/OrderRepository.java`

- `List<SalesOrder> findPage(UUID customerId, String orderStatus, int offset, int limit)`
  Liefert paginierte Bestellungen, optional nach Kunde und Status gefiltert.
- `long count(UUID customerId, String orderStatus)`
  Liefert die Gesamtanzahl passend zum Filter.
- `Optional<SalesOrder> findById(UUID id)`
  Liefert eine Bestellung per ID.
- `Optional<SalesOrder> findByIdWithPositions(UUID id)`
  Liefert eine Bestellung inklusive Positionen und zugehoeriger Produkte.
- `SalesOrder save(SalesOrder order)`
  Speichert neue oder geaenderte Bestellung.
- `boolean updateStatus(UUID orderId, String orderStatus, String shippingStatus)`
  Setzt Bestell- und Versandstatus; `false`, falls Bestellung nicht gefunden.

## Hinweise fuer spaetere Iterationen

- Atomare Stock-Reservierung/-Reduktion fuer konkurrierende Zugriffe.
- Optional Delete-Methoden erst bei konkretem Use-Case einfuehren.
