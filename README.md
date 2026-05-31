# Team 18 – Bikes Vertriebsunterstützung

Software Engineering Projekt – Ostfalia, SS 2026

---

## Projektbeschreibung

Das System unterstützt den internen Vertrieb eines Fahrradhändlers mit mehreren Filialen und wurde in vier agilen Iterationen entwickelt. 

---


## Voraussetzungen

- **Java 25**
- **WildFly 39**
- **PostgreSQL 18**

---

## Deployment

**Starten:**

```bash
mvn clean package

# Linux / macOS
$JBOSS_HOME/bin/standalone.sh

# Windows
%JBOSS_HOME%\bin\standalone.bat
```

Danach deployen:

```bash
mvn wildfly:deploy
```

**Beenden:**

```bash
mvn wildfly:shutdown
```

**Anwendung aufrufen:**

```
http://localhost:8080/team-18/
```

---

## Anmeldung

Die Authentifizierung basiert auf den Einträgen in der `staffs`-Tabelle. Die Rolle wird anhand der Spalte `manager_id` bestimmt:

| Rolle | Zugriff | Bedingung |
|---|---|---|
| `ADMIN` | Vollzugriff inkl. Mitarbeiterverwaltung | `manager_id IS NULL` |
| `USER1` | Alle Seiten außer Mitarbeiterverwaltung | `manager_id = 1` |
| `USER2` | Nur lesender Zugriff auf alle Masterseiten | beliebige andere `manager_id` |

- **Benutzername**: E-Mail-Adresse des Mitarbeiters
- **Passwort**: Telefonnummer ohne Vorwahl
---

## Tests ausführen

Die Integrationstests verwenden **HtmlUnit** und **Selenium** und setzen einen laufenden Anwendungsserver mit der deployten Anwendung voraus.

### Alle Integrationstests ausführen

```bash
mvn verify
```

Das Failsafe-Plugin führt alle Klassen nach dem Muster `**/*IT.java` aus und testet gegen:

```
http://localhost:8080/team-18/
```

Es darf währenddessen **kein anderer** WildFly mit der Applikation auf derselben Maschine laufen.


### Einzelne Integrationstestklasse ausführen

```bash
mvn failsafe:integration-test -Dit.test=ProductEditIT
```

---

## Projektstruktur

```
src/
├── main/
│   ├── java/de/pdbm/team18/business/
│   │   ├── customers/       # Kunden: Entity, Boundary, Controller, Repository
│   │   ├── orders/          # Bestellungen und Bestellpositionen
│   │   ├── products/        # Produkte, Marken, Kategorien
│   │   ├── stores/          # Filialen, Mitarbeiter, Lagerbestand
│   │   └── shared/          # Security-Konfiguration, gemeinsame Hilfsmittel
│   └── webapp/
│       ├── WEB-INF/         # web.xml, faces-config.xml, Datasource-Konfiguration
│       ├── *.xhtml          # JSF-Seiten (kunden, produkte, bestellungen, …)
│       └── resources/       # CSS, wiederverwendbare Komponenten (Pagination)
└── test/
    ├── htmlunit/            # HtmlUnit-Integrationstests
    └── selenium/            # Selenium-Integrationstests
```
