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
package l9g.uidgen.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import l9g.uidgen.token.BearerTokenConfig;
import l9g.uidgen.token.BearerTokenConfig.BearerToken;
import l9g.uidgen.service.UidgenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import l9g.uidgen.token.AuthenticatedBearerToken;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@RestController
@RequestMapping(path = "/api/v1/uidgen",
                produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class UidgenController
{
  private final UidgenService uidgenService;

  private final BearerTokenConfig tokenConfig;

  @Operation(summary = "Generate unique user IDs",
             description = "Generate unique user IDs. Authentication is required via a Bearer Token in the Authorization header.",
             security =
             @SecurityRequirement(name = "bearerAuth"),
             responses =
             {
               @ApiResponse(responseCode = "200", description = "UIDs successfully generated",
                            content =
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                     schema =
                                     @Schema(oneOf =
                                     {
                                       UidgenResponse.class
                                   }))),
               @ApiResponse(responseCode = "400", description = "Bad request, e.g., no parameter or multiple parameters provided, or invalid token",
                            content =
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                     schema =
                                     @Schema(implementation = UidgenResponse.class))),
               @ApiResponse(responseCode = "401", description = "Unauthorized, if no or invalid Bearer token is provided",
                            content =
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                     schema =
                                     @Schema(implementation = UidgenResponse.class))),
             })
  @GetMapping
  public ResponseEntity<UidgenResponse> serveNewUids(
    @RequestParam(name = "n", required = false, defaultValue = "1") int numberOfRequestedUids,
    @AuthenticatedBearerToken BearerToken token
  )
  {
    log.info("owner={}", token.getOwner());
    log.debug("token={}", token);

    log.info("numberOfRequestedUids={}", numberOfRequestedUids);
    List<String> uids = uidgenService.findUids(numberOfRequestedUids);

    return ResponseEntity.ok(
      new UidgenResponse(
        uids, "ok", uidgenService.getAvailableUids(), uids.size()
      )
    );
  }

  @Operation(summary = "Get avalable unique user IDs",
             description = "Get avalable unique user IDs. Authentication is required via a Bearer Token in the Authorization header.",
             security =
             @SecurityRequirement(name = "bearerAuth"),
             responses =
             {
               @ApiResponse(responseCode = "200", description = "Get avalable unique user IDs",
                            content =
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                     schema =
                                     @Schema(oneOf =
                                     {
                                       UidgenResponse.class
                                   }))),
             })
  @GetMapping(path = "/status")
  public ResponseEntity<UidgenResponse> serveAvailableUids(
    @AuthenticatedBearerToken BearerToken token
  )
  {
    log.info("owner={}", token.getOwner());
    log.debug("token={}", token);

    log.info("serveAvailableUids={}", uidgenService.getAvailableUids());
    return ResponseEntity.ok(
      new UidgenResponse(null, "ok", uidgenService.getAvailableUids(), 0));
  }

  @Operation(summary = "Initialize unique user id field from LDAP and show avalable unique user IDs",
             description = "Initialize unique user id field from LDAP and show avalable unique user IDs. Authentication is required via a Bearer Token in the Authorization header.",
             security =
             @SecurityRequirement(name = "bearerAuth"),
             responses =
             {
               @ApiResponse(responseCode = "200", description = "Initialize unique user id field from LDAP and show avalable unique user IDs",
                            content =
                            @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                     schema =
                                     @Schema(oneOf =
                                     {
                                       UidgenResponse.class
                                   }))),
             })
  @GetMapping(path = "/initialize")
  public ResponseEntity<UidgenResponse> serveInitialize(@AuthenticatedBearerToken BearerToken token)
    throws Throwable
  {
    log.info("serveInitialize={}");
    uidgenService.initialize();
    return serveAvailableUids(token);
  }

}
