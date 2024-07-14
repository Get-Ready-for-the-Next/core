package com.getreadyforthenext.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponseType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@Controller
public class OAuth2Controller {

    private static final Logger log = LoggerFactory.getLogger(OAuth2Controller.class);
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String callbackUri;

    @Value("${spring.security.oauth2.client.provider.google.token-uri}")
    private String tokenUri;

    @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
    private String userInfoUri;

    @Value("${oauth.default.state}")
    private String defaultStateValue;

    @GetMapping("/custom-oauth2/authorization/google")
    public RedirectView authorize(@RequestParam(value = "state", defaultValue = "defaultStateValue") String state) {
        if (state.equals("defaultStateValue")) state = defaultStateValue;

        String authorizationRequest = UriComponentsBuilder.fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam(OAuth2ParameterNames.RESPONSE_TYPE, OAuth2AuthorizationResponseType.CODE.getValue())
                .queryParam(OAuth2ParameterNames.CLIENT_ID, clientId)
                .queryParam(OAuth2ParameterNames.REDIRECT_URI, callbackUri)
                .queryParam(OAuth2ParameterNames.SCOPE, "openid email profile")
                .queryParam(OAuth2ParameterNames.STATE, state)
                .queryParam("prompt", "consent")
                .build()
                .toUriString();

        return new RedirectView(authorizationRequest);
    }

    @GetMapping("/oauth2/callback")
    public RedirectView handleAuthorizationCode(String code, @RequestParam("state") String state) {
        RestTemplate restTemplate = new RestTemplate();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("code", code);
        requestBody.put("client_id", clientId);
        requestBody.put("client_secret", clientSecret);
        requestBody.put("redirect_uri", callbackUri);
        requestBody.put("grant_type", "authorization_code");

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenUri, requestBody, Map.class);
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        org.springframework.http.HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUri, HttpMethod.GET, entity, Map.class);

        Map<String, Object> userInfo = response.getBody();
        log.info("User Info: " + userInfo);

        return new RedirectView(state);
    }
}
