package com.example.keycloakintegra;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequestScope
public class LoginBean {

    private final String KEYCLOAK_BASE_URL = "http://localhost:8080/realms/treinamento-realm/protocol/openid-connect/auth";
    private final String CLIENT_ID = "treinamento";
    private final String REDIRECT_URI = "http://localhost:8181/logado.xhtml";
    private final String RESPONSE_TYPE = "code";
    private final String SCOPE = "openid";

    public void login() throws IOException {
        String url = KEYCLOAK_BASE_URL +
                     "?client_id=" + CLIENT_ID +
                     "&response_type=" + RESPONSE_TYPE +
                     "&scope=" + SCOPE +
                     "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect(url);
    }

    public void loginWithGoogle() throws IOException {
        String url = KEYCLOAK_BASE_URL +
                     "?client_id=" + CLIENT_ID +
                     "&response_type=" + RESPONSE_TYPE +
                     "&scope=" + SCOPE +
                     "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                     "&kc_idp_hint=google"; // Adiciona o hint para o Google

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        externalContext.redirect(url);
    }
}
