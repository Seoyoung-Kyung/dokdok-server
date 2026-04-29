package com.dokdok.oauth2.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository,
                "/oauth2/authorization"
        );
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return authorizationRequest != null ? customizeRequest(authorizationRequest, request) : null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest =
                defaultResolver.resolve(request, clientRegistrationId);
        return authorizationRequest != null ? customizeRequest(authorizationRequest, request) : null;
    }

    /**
     * fe_origin 파라미터를 state에 Base64 인코딩하여 포함시킨다.
     * state 형식: {originalState}|{base64(feOrigin)}
     */
    private OAuth2AuthorizationRequest customizeRequest(OAuth2AuthorizationRequest request,
                                                        HttpServletRequest httpRequest) {
        String feOrigin = httpRequest.getParameter("fe_origin");
        if (feOrigin == null || feOrigin.isEmpty()) {
            return request;
        }

        String encodedFeOrigin = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(feOrigin.getBytes(StandardCharsets.UTF_8));
        String customState = request.getState() + "|" + encodedFeOrigin;

        Map<String, Object> additionalParams = new HashMap<>(request.getAdditionalParameters());

        log.info("fe_origin을 state에 인코딩: {}", feOrigin);

        return OAuth2AuthorizationRequest.from(request)
                .state(customState)
                .additionalParameters(additionalParams)
                .build();
    }
}
