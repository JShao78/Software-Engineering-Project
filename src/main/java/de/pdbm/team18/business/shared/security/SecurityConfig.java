package de.pdbm.team18.business.shared.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;
import jakarta.security.enterprise.identitystore.PasswordHash;

@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/login.xhtml",
                errorPage = "/login-error.xhtml",
                useForwardToLogin = false
        )
)

@DatabaseIdentityStoreDefinition(
        dataSourceLookup = "java:jboss/datasources/bikes18",
        callerQuery = "SELECT TRIM(SUBSTRING(phone FROM POSITION(')' IN phone) + 1)) FROM staffs WHERE email = ?",
        hashAlgorithm = SecurityConfig.PlainTextPasswordHash.class,
        groupsQuery = "SELECT CASE WHEN manager_id IS NULL THEN 'ADMIN' " +
                "WHEN manager_id = 1 THEN 'USER1' " +
                "ELSE 'USER2' " +
                "END FROM staffs WHERE email = ?"
)

@ApplicationScoped
public class SecurityConfig {

    @Dependent
    public static class PlainTextPasswordHash implements PasswordHash {
        @Override
        public String generate(char[] password) {
            return new String(password);
        }

        @Override
        public boolean verify(char[] password, String hashedPassword) {
            return hashedPassword != null && hashedPassword.equals(new String(password));
        }
    }
}
