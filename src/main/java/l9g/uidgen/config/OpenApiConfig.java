package l9g.uidgen.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
  info =
  @Info(
    title = "L9G UID Generator API",
    version = "v1",
    description = "API for generating Unique User IDs",
    contact =
    @Contact(
      name = "Thorsten Ludewig",
      email = "t.ludewig@gmail.com"
    ),
    license =
    @License(
      name = "Apache 2.0",
      url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
    )
  ),
  servers = {
    @Server(url = "http://localhost:8080", description = "Local Development")
  }
)
@SecurityScheme(
  name = "bearerAuth",
  type = SecuritySchemeType.HTTP,
  scheme = "bearer",
  bearerFormat = "random",
  description = "Bearer Token authentication"
)
public class OpenApiConfig
{
}
