package de.pdbm.team18.business.shared.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Named
@RequestScoped
public class LoginController {

    private String username;
    private String password;

    @Inject
    private SecurityContext securityContext;

    @Inject
    private FacesContext facesContext;

    public void login() throws IOException {
        AuthenticationStatus status = securityContext.authenticate(
                (HttpServletRequest) facesContext.getExternalContext().getRequest(),
                (HttpServletResponse) facesContext.getExternalContext().getResponse(),
                AuthenticationParameters.withParams().credential(new UsernamePasswordCredential(username, password))
        );

        if (status == AuthenticationStatus.SEND_CONTINUE) {
            facesContext.responseComplete();
            return;
        }

        if (status == AuthenticationStatus.SEND_FAILURE) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Login fehlgeschlagen", "Bitte Benutzername und Passwort prüfen."));
            return;
        }

        if (status == AuthenticationStatus.SUCCESS) {
            facesContext.getExternalContext().redirect(facesContext.getExternalContext().getRequestContextPath() + "/index.xhtml");
            facesContext.responseComplete();
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
