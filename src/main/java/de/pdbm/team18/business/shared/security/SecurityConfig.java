package de.pdbm.team18.business.shared.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.CustomFormAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.LoginToContinue;
import jakarta.security.enterprise.identitystore.DatabaseIdentityStoreDefinition;

@CustomFormAuthenticationMechanismDefinition(
        loginToContinue = @LoginToContinue(
                loginPage = "/login.xhtml",
                errorPage = "/login.xhtml"
        )
)

@DatabaseIdentityStoreDefinition(
        dataSourceLookup = "java:jboss/datasources/bikes18",
        callerQuery = "SELECT TRIM(SPLIT_PART(phone, ')',2)) FROM staffs WHERE email = ?",
        groupsQuery = "SELECT CASE WHEN manager_id IS NULL THEN 'ADMIN' " +
                "WHEN manager_id = 1 THEN 'USER1' " +
                "ELSE 'USER2' " +
                "END FROM staffs WHERE email = ?"


)

@ApplicationScoped
public class SecurityConfig {

}
