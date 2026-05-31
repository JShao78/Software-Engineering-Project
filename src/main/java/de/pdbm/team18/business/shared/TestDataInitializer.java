package de.pdbm.team18.business.shared;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Singleton
@Startup
public class TestDataInitializer {

    private static final String TEST_DATA_SCRIPT = "META-INF/test-data.sql";

    @PersistenceContext
    private EntityManager em;

    @Resource(lookup = "java:jboss/datasources/bikes18")
    private DataSource dataSource;

    @PostConstruct
    public void loadTestDataIfAvailable() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader.getResource(TEST_DATA_SCRIPT) == null) {
            return;
        }

        if (countStaffs().intValue() > 0) {
            return;
        }

        try (InputStream inputStream = classLoader.getResourceAsStream(TEST_DATA_SCRIPT)) {
            if (inputStream == null) {
                return;
            }
            executeScript(inputStream);
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Could not load H2 test data from " + TEST_DATA_SCRIPT, e);
        }
    }

    private Number countStaffs() {
        return (Number) em.createNativeQuery("SELECT COUNT(*) FROM staffs").getSingleResult();
    }

    private void executeScript(InputStream inputStream) throws IOException, SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }

                sql.append(line).append('\n');
                if (trimmed.endsWith(";")) {
                    statement.execute(sql.toString());
                    sql.setLength(0);
                }
            }

            if (!sql.isEmpty()) {
                statement.execute(sql.toString());
            }
        }
    }
}
