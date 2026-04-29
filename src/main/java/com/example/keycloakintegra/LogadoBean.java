package com.example.keycloakintegra;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@SessionScope
public class LogadoBean implements Serializable {

    private String code;
    private String accessToken;
    private String idToken;
    private String decodedToken;

    private final String KEYCLOAK_LOGOUT_URL = "http://localhost:8080/realms/treinamento-realm/protocol/openid-connect/logout";
    private final String POST_LOGOUT_REDIRECT_URI = "http://localhost:8181/index.xhtml";

    @PostConstruct
    public void init() {
        // O init no SessionScope só roda uma vez por sessão na criação do bean.
        // Mas podemos forçar a verificação do código se necessário.
        checkAndExchangeCode();
    }

    public void checkAndExchangeCode() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String newCode = params.get("code");

        // Se houver um novo código na URL, fazemos a troca.
        if (newCode != null && !newCode.equals(this.code)) {
            this.code = newCode;
            this.accessToken = null; // Limpa token antigo se houver
            exchangeCodeForToken();
        }
    }

    private void exchangeCodeForToken() {
        String tokenUrl = "http://localhost:8080/realms/treinamento-realm/protocol/openid-connect/token";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "authorization_code");
        map.add("client_id", "treinamento");
        map.add("code", this.code);
        map.add("redirect_uri", "http://localhost:8181/logado.xhtml");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                this.accessToken = (String) responseBody.get("access_token");
                this.idToken = (String) responseBody.get("id_token");
                decodeJwt();
            }
        } catch (Exception e) {
            this.accessToken = "Erro ao obter token: " + e.getMessage();
        }
    }

    private void decodeJwt() {
        try {
            if (this.accessToken != null && !this.accessToken.startsWith("Erro")) {
                DecodedJWT jwt = JWT.decode(this.accessToken);
                this.decodedToken = "Subject: " + jwt.getSubject() + "\n" +
                                    "Issuer: " + jwt.getIssuer() + "\n" +
                                    "Payload: " + new String(java.util.Base64.getDecoder().decode(jwt.getPayload()));
            }
        } catch (Exception e) {
            this.decodedToken = "Erro ao decodificar token: " + e.getMessage();
        }
    }

    public void logout() throws IOException {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        
        if (this.idToken != null) {
            String url = KEYCLOAK_LOGOUT_URL +
                         "?id_token_hint=" + this.idToken +
                         "&post_logout_redirect_uri=" + URLEncoder.encode(POST_LOGOUT_REDIRECT_URI, StandardCharsets.UTF_8);
            
            // Invalida a sessão local antes de redirecionar
            externalContext.invalidateSession();
            
            externalContext.redirect(url);
        } else {
            externalContext.redirect(POST_LOGOUT_REDIRECT_URI);
        }
    }

    public String getAccessToken() {
        checkAndExchangeCode(); // Garante que verifique o código se recarregar a página
        return accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getDecodedToken() {
        return decodedToken;
    }

    public String getCode() {
        return code;
    }
}
