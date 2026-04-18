package com.example.keycloakintegra;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.io.IOException;

@Component
@RequestScope
public class LoginBean {

    private final String KEYCLOAK_URL = "http://localhost:8080/realms/treinamento-realm/protocol/openid-connect/auth";
    private final String CLIENT_ID = "treinamento";
    // Removi o .xhtml para bater com sua solicitação inicial e evitei encode manual
    private final String REDIRECT_URI = "http://localhost:8181/logado.xhtml";

    public void login() throws IOException {
        String url = KEYCLOAK_URL + 
                     "?client_id=" + CLIENT_ID +
                     "&response_type=code" +
                     "&scope=openid" +
                     "&redirect_uri=" + REDIRECT_URI;

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect(url);
    }
}
