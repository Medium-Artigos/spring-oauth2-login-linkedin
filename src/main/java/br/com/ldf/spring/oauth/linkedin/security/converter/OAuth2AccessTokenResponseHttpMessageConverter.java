package br.com.ldf.spring.oauth.linkedin.security.converter;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A {@link HttpMessageConverter} for an {@link OAuth2AccessTokenResponse OAuth 2.0 Access Token Response}.
 *
 * @author Joe Grandja
 * @see AbstractHttpMessageConverter
 * @see OAuth2AccessTokenResponse
 * @since 5.1
 */
public class OAuth2AccessTokenResponseHttpMessageConverter extends AbstractHttpMessageConverter<OAuth2AccessTokenResponse> {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final ParameterizedTypeReference<Map<String, Object>> PARAMETERIZED_RESPONSE_TYPE =
        new ParameterizedTypeReference<Map<String, Object>>() {
        };

    private GenericHttpMessageConverter<Object> jsonMessageConverter = new MappingJackson2HttpMessageConverter();

    protected Converter<Map<String, Object>, OAuth2AccessTokenResponse> tokenResponseConverter =
        new OAuth2AccessTokenResponseConverter();

    protected Converter<OAuth2AccessTokenResponse, Map<String, String>> tokenResponseParametersConverter =
        new OAuth2AccessTokenResponseParametersConverter();

    public OAuth2AccessTokenResponseHttpMessageConverter() {
        super(DEFAULT_CHARSET, MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return OAuth2AccessTokenResponse.class.isAssignableFrom(clazz);
    }

    @Override
    protected OAuth2AccessTokenResponse readInternal(Class<? extends OAuth2AccessTokenResponse> clazz, HttpInputMessage inputMessage)
        throws IOException, HttpMessageNotReadableException {

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> tokenResponseParameters = (Map<String, Object>) this.jsonMessageConverter.read(
                PARAMETERIZED_RESPONSE_TYPE.getType(), null, inputMessage);
            return this.tokenResponseConverter.convert(tokenResponseParameters);
        } catch (Exception ex) {
            throw new HttpMessageNotReadableException("An error occurred reading the OAuth 2.0 Access Token Response: " +
                ex.getMessage(), ex, inputMessage);
        }
    }

    @Override
    protected void writeInternal(OAuth2AccessTokenResponse tokenResponse, HttpOutputMessage outputMessage)
        throws IOException, HttpMessageNotWritableException {

        try {
            Map<String, String> tokenResponseParameters = this.tokenResponseParametersConverter.convert(tokenResponse);
            this.jsonMessageConverter.write(
                tokenResponseParameters, PARAMETERIZED_RESPONSE_TYPE.getType(), MediaType.APPLICATION_JSON, outputMessage);
        } catch (Exception ex) {
            throw new HttpMessageNotWritableException("An error occurred writing the OAuth 2.0 Access Token Response: " + ex.getMessage(), ex);
        }
    }

    /**
     * Sets the {@link Converter} used for converting the OAuth 2.0 Access Token Response parameters
     * to an {@link OAuth2AccessTokenResponse}.
     *
     * @param tokenResponseConverter the {@link Converter} used for converting to an {@link OAuth2AccessTokenResponse}
     */
    public final void setTokenResponseConverter(Converter<Map<String, Object>, OAuth2AccessTokenResponse> tokenResponseConverter) {
        Assert.notNull(tokenResponseConverter, "tokenResponseConverter cannot be null");
        this.tokenResponseConverter = tokenResponseConverter;
    }

    /**
     * Sets the {@link Converter} used for converting the {@link OAuth2AccessTokenResponse}
     * to a {@code Map} representation of the OAuth 2.0 Access Token Response parameters.
     *
     * @param tokenResponseParametersConverter the {@link Converter} used for converting to a {@code Map} representation of the Access Token Response parameters
     */
    public final void setTokenResponseParametersConverter(Converter<OAuth2AccessTokenResponse, Map<String, String>> tokenResponseParametersConverter) {
        Assert.notNull(tokenResponseParametersConverter, "tokenResponseParametersConverter cannot be null");
        this.tokenResponseParametersConverter = tokenResponseParametersConverter;
    }

    /**
     * A {@link Converter} that converts the provided
     * OAuth 2.0 Access Token Response parameters to an {@link OAuth2AccessTokenResponse}.
     */
    private static class OAuth2AccessTokenResponseConverter implements Converter<Map<String, Object>, OAuth2AccessTokenResponse> {
        private static final Set<String> TOKEN_RESPONSE_PARAMETER_NAMES = Stream.of(
            OAuth2ParameterNames.ACCESS_TOKEN,
            OAuth2ParameterNames.TOKEN_TYPE,
            OAuth2ParameterNames.EXPIRES_IN,
            OAuth2ParameterNames.REFRESH_TOKEN,
            OAuth2ParameterNames.SCOPE).collect(Collectors.toSet());

        @Override
        public OAuth2AccessTokenResponse convert(Map<String, Object> tokenResponseParameters) {
            String accessToken = tokenResponseParameters.get(OAuth2ParameterNames.ACCESS_TOKEN).toString();

            long expiresIn = 0;
            if (tokenResponseParameters.containsKey(OAuth2ParameterNames.EXPIRES_IN)) {
                try {
                    expiresIn = Long.valueOf(tokenResponseParameters.get(OAuth2ParameterNames.EXPIRES_IN).toString());
                } catch (NumberFormatException ex) {
                }
            }

            Set<String> scopes = Collections.emptySet();
            if (tokenResponseParameters.containsKey(OAuth2ParameterNames.SCOPE)) {
                String scope = tokenResponseParameters.get(OAuth2ParameterNames.SCOPE).toString();
                scopes = Arrays.stream(StringUtils.delimitedListToStringArray(scope, " ")).collect(Collectors.toSet());
            }

            Map<String, Object> additionalParameters = new LinkedHashMap<>();
            tokenResponseParameters.entrySet().stream()
                .filter(e -> !TOKEN_RESPONSE_PARAMETER_NAMES.contains(e.getKey()))
                .forEach(e -> additionalParameters.put(e.getKey(), e.getValue()));

            return OAuth2AccessTokenResponse.withToken(accessToken)
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(expiresIn)
                .scopes(scopes)
                .additionalParameters(additionalParameters)
                .build();
        }
    }

    /**
     * A {@link Converter} that converts the provided {@link OAuth2AccessTokenResponse}
     * to a {@code Map} representation of the OAuth 2.0 Access Token Response parameters.
     */
    private static class OAuth2AccessTokenResponseParametersConverter implements Converter<OAuth2AccessTokenResponse, Map<String, String>> {

        @Override
        public Map<String, String> convert(OAuth2AccessTokenResponse tokenResponse) {
            Map<String, String> parameters = new HashMap<>();

            long expiresIn = -1;
            if (tokenResponse.getAccessToken().getExpiresAt() != null) {
                expiresIn = ChronoUnit.SECONDS.between(Instant.now(), tokenResponse.getAccessToken().getExpiresAt());
            }

            parameters.put(OAuth2ParameterNames.ACCESS_TOKEN, tokenResponse.getAccessToken().getTokenValue());
            parameters.put(OAuth2ParameterNames.TOKEN_TYPE, tokenResponse.getAccessToken().getTokenType().getValue());
            parameters.put(OAuth2ParameterNames.EXPIRES_IN, String.valueOf(expiresIn));
            if (!CollectionUtils.isEmpty(tokenResponse.getAccessToken().getScopes())) {
                parameters.put(OAuth2ParameterNames.SCOPE,
                    StringUtils.collectionToDelimitedString(tokenResponse.getAccessToken().getScopes(), " "));
            }
            if (tokenResponse.getRefreshToken() != null) {
                parameters.put(OAuth2ParameterNames.REFRESH_TOKEN, tokenResponse.getRefreshToken().getTokenValue());
            }
            if (!CollectionUtils.isEmpty(tokenResponse.getAdditionalParameters())) {
                tokenResponse.getAdditionalParameters().entrySet().stream()
                    .forEach(e -> parameters.put(e.getKey(), e.getValue().toString()));
            }

            return parameters;
        }
    }
}
