package com.example.keycloakintegra;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.Map;

@Component
@RequestScope
public class LogadoBean implements Serializable {

    private String code;
    private String accessToken;
    private String decodedToken;

    @PostConstruct
    public void init() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        this.code = params.get("code");

        // Se o código estiver presente e ainda não tivermos o token, buscamos.
        if (this.code != null && this.accessToken == null) {
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
        // A redirect_uri aqui DEVE ser idêntica à enviada no LoginBean
        map.add("redirect_uri", "http://localhost:8181/logado.xhtml");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                this.accessToken = (String) response.getBody().get("access_token");
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

    public String getAccessToken() {
        return accessToken;
    }

    public String getDecodedToken() {
        return decodedToken;
    }

    public String getCode() {
        return code;
    }
}
