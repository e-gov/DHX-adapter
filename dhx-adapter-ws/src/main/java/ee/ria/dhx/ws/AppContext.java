package ee.ria.dhx.ws;

import org.springframework.context.ApplicationContext;

public class AppContext {

  private static ApplicationContext context;

  public static void setApplicationContext(
      ApplicationContext applicationContext) {
    context = applicationContext;
  }

  public static ApplicationContext getApplicationContext() {
    return context;
  }
}
