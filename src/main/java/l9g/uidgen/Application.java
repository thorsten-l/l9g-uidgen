package l9g.uidgen;

import l9g.uidgen.crypto.CryptoHandler;
import l9g.uidgen.crypto.PasswordGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@Slf4j
@SpringBootApplication(exclude =
{
  UserDetailsServiceAutoConfiguration.class
})
public class Application
{

  public static void main(String[] args)
  {
    if(args != null)
    {
      CryptoHandler cryptoHandler = CryptoHandler.getInstance();

      if(args.length == 2 && "-e".equals(args[0]))
      {
        System.out.println(args[1] + " = \"" + cryptoHandler.encrypt(args[1]) + "\"");
        System.exit(0);
      }

      if(args.length == 1 && "-g".equals(args[0]))
      {
        String token = PasswordGenerator.generate(32);
        System.out.println("\"" + token + "\" = \"" + cryptoHandler.encrypt(token) + "\"");
        System.exit(0);
      }
      
      if(args.length == 1 && "-i".equals(args[0]))
      {
        cryptoHandler.encrypt("init");
        log.info("Initialize data/secret.bin");
        System.exit(0);
      }

      if(args.length == 1 && "-h".equals(args[0]))
      {
        System.out.println("l9g-uidgen [-e clear text] [-g] [-h]");
        System.out.println("  -e : encrypt clear text");
        System.out.println("  -g : generate new token");
        System.out.println("  -i : initialize data/secret.bin");
        System.out.println("  -h : this help");
        System.exit(0);
      }
    }

    SpringApplication.run(Application.class, args);
  }

}
