# Team 18 - SE Projekt (Iteration 1)

Dieses Repository enthaelt das technische Grundgeruest fuer Team 18 auf Basis des Jakarta-EE-Starters.
Die fachlichen Features aus `docs/planung-18.txt` sind absichtlich noch nicht umgesetzt.

## Team-18 Konfiguration

- Deployment-Name: `team-18`
- Artefact-Id: `team-18`
- JNDI-DataSource: `java:jboss/datasources/team-18`
- Datenbankname: `team18`

Relevante Konfigurationsdateien:

- `pom.xml`
- `src/main/resources/META-INF/persistence.xml`
- `first-iteration-ds.xml`
- `db/schema/001_initial_schema.sql`
- `db/seeds/001_sample_data.sql`

## Einstieg

- UI: [http://localhost:8080/team-18](http://localhost:8080/team-18)

## Struktur im Projekt (ECB-orientiert)

Im Projekt ist die Struktur nach fachlichen Bereichen aufgebaut (`customers`, `products`, `orders`):

- `boundary`: Eingangsgrenze (JSF/CDI-Beans als fachliche Schnittstelle)
- `control`: Anwendungslogik/Use-Case-Orchestrierung
- `entity`: Fachdatenmodell (JPA-Entities)

Beispiel je Bereich:

- `business/customers/boundary/CustomerBoundary.java`
- `business/customers/control/CustomerControl.java`
- `business/customers/entity/Customer.java`

## Aktuelle Ordnerstruktur

```text
db/
  README.md
  migrations/
    README.md
  schema/
    001_initial_schema.sql
  seeds/
    001_sample_data.sql

docs/
  planung-18.txt
  reflexion-18.txt

src/main/java/de/pdbm/team18/
  business/
    customers/
      boundary/CustomerBoundary.java
      control/CustomerControl.java
      entity/Customer.java
    products/
      boundary/ProductBoundary.java
      control/ProductControl.java
      entity/Product.java
    orders/
      boundary/OrderBoundary.java
      control/OrderControl.java
      entity/SalesOrder.java
      entity/OrderPosition.java

src/main/resources/META-INF/
  persistence.xml

src/main/webapp/
  index.xhtml
  WEB-INF/
    web.xml
    beans.xml
    faces-config.xml
```

## Datenbank-Setup (PostgreSQL)

SQL-Skripte fuer eine lokale Testdatenbank liegen unter `db/`.

Schema einspielen:

```bash
psql -U postgres -d team18 -f db/schema/001_initial_schema.sql
```

Beispieldaten einspielen:

```bash
psql -U postgres -d team18 -f db/seeds/001_sample_data.sql
```

## Was bereits umgesetzt ist

- Team-18 Build- und Deployment-Konfiguration
- Datenquellen-/Persistence-Verdrahtung
- ECB-Grundstruktur (Boundary, Control, Entity) fuer Kunden, Produkte und Bestellungen
- Startseite als technischer Einstiegspunkt

## Was noch nicht umgesetzt ist

- Anlegen von Kunden/Produkten/Bestellungen
- Listenansichten mit Datenbankinhalten
- Popup mit Bestellpositionen und Mengen
- Bereichsspezifische UI-Seiten und End-to-End-Interaktionsfluss

Diese Punkte folgen in den naechsten Arbeitsschritten gemaess `docs/planung-18.txt`.
