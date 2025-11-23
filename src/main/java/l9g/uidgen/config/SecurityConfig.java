/*
 * Copyright 2025 Thorsten Ludewig (t.ludewig@gmail.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package l9g.uidgen.config;

import l9g.uidgen.token.BearerTokenConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import l9g.uidgen.crypto.CryptoHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig
{
  private final BearerTokenConfig bearerTokenConfig;

  private final CryptoHandler cryptoHandler;

  @Bean
  public AuthenticationEntryPoint authenticationEntryPoint(
    @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
  )
  {
    return (request, response, authException) -> {
      resolver.resolveException(request, response, null, authException);
    };
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationEntryPoint authenticationEntryPoint)
    throws Exception
  {
    http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(sm -> sm.disable())
      .httpBasic(hb -> hb.disable())
      .formLogin(fl -> fl.disable())
      .logout(lo -> lo.disable());

    http.exceptionHandling(eh -> eh
      .authenticationEntryPoint(authenticationEntryPoint)
    );

    http.addFilterBefore(new StaticBearerTokenFilter(bearerTokenConfig, cryptoHandler),
      AbstractPreAuthenticatedProcessingFilter.class);

    http.authorizeHttpRequests(auth -> auth
      .requestMatchers(HttpMethod.GET, "/api/v1/uidgen").authenticated()
      .anyRequest().permitAll()
    );

    return http.build();
  }

  static class StaticBearerTokenFilter extends OncePerRequestFilter
  {
    private final Map<String, BearerTokenConfig.BearerToken> tokensByName;

    private final Map<String, String> tokenIndex;

    StaticBearerTokenFilter(BearerTokenConfig config, CryptoHandler cryptoHandler)
    {
      this.tokensByName = config.getMap();
      this.tokenIndex = tokensByName.entrySet().stream()
        .collect(java.util.stream.Collectors.toUnmodifiableMap(
          e -> cryptoHandler.decrypt(e.getValue().getToken()),
          Map.Entry :: getKey,
          (a, b) -> a
        ));
    }

    @Override
    protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException
    {

      String auth = request.getHeader(HttpHeaders.AUTHORIZATION);

      if(auth == null ||  ! auth.startsWith("Bearer "))
      {
        chain.doFilter(request, response);
        return;
      }

      String token = auth.substring("Bearer ".length()).trim();
      if(token.isEmpty())
      {
        chain.doFilter(request, response);
        return;
      }

      String name = tokenIndex.get(token);
      if(name == null)
      {
        chain.doFilter(request, response);
        return;
      }

      BearerTokenConfig.BearerToken bt = tokensByName.get(name);
      if(bt == null ||  ! bt.isEnabled())
      {
        chain.doFilter(request, response);
        return;
      }

      Authentication authToken = new StaticBearerAuthenticationToken(
        name,
        bt.getOwner(),
        AuthorityUtils.NO_AUTHORITIES
      );
      SecurityContextHolder.getContext().setAuthentication(authToken);

      try
      {
        chain.doFilter(request, response);
      }
      finally
      {
        SecurityContextHolder.clearContext();
      }
    }

  }

  static class StaticBearerAuthenticationToken extends AbstractAuthenticationToken
  {
    private final String principalName;

    private final String owner;

    StaticBearerAuthenticationToken(String principalName, String owner,
      java.util.Collection authorities)
    {
      super(authorities);
      this.principalName = principalName;
      this.owner = owner;
      setAuthenticated(true);
    }

    @Override
    public Object getCredentials()
    {
      return ""; // kein Geheimnis mehr speichern
    }

    @Override
    public Object getPrincipal()
    {
      return principalName;
    }

    @Override
    public Object getDetails()
    {
      return owner;
    }

  }

}
