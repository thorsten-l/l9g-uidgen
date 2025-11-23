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
package l9g.uidgen.handler;

import l9g.uidgen.controller.UidgenResponse;
import l9g.uidgen.token.MissingOrInvalidTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 *
 * @author Thorsten Ludewig (t.ludewig@gmail.com)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{

  @ExceptionHandler(MissingOrInvalidTokenException.class)
  public ResponseEntity<UidgenResponse> handleMissingOrInvalidToken(
    MissingOrInvalidTokenException ex)
  {
    log.error("{}", ex.getMessage());
    return ResponseEntity.badRequest()
      .body(new UidgenResponse(null, "ERROR: " + ex.getMessage(), 0, 0));
  }
  
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<UidgenResponse> handleAuthenticationException(AuthenticationException ex)
  {
      log.error("Authentication failed: {}", ex.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
              .body(new UidgenResponse(null, "ERROR: " + ex.getMessage(), 0, 0));
  }
}
