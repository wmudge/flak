package net.jflask.test;

import flak.Response;
import flak.annotations.LoginNotRequired;
import flak.annotations.LoginPage;
import flak.annotations.Route;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Variant of LoginTest that uses @LoginNotRequired and
 * {@link flak.App#setRequireLoggedInByDefault(boolean)}.
 *
 * @author pcdv
 */
public class LoginTest2 extends AbstractAppTest {

  @LoginPage
  @Route("/login")
  public String loginPage() {
    return "Please login";
  }

  @Route("/logout")
  public Response logout() {
    app.logoutUser();
    return app.redirect("/login");
  }

  @Route("/app")
  public String appPage() {
    return "Welcome " + app.getCurrentLogin();
  }

  @Route(value = "/login", method = "POST")
  @LoginNotRequired
  public Response login() {
    String login = app.getRequest().getForm("login");
    String pass = app.getRequest().getForm("password");

    if (login.equals("foo") && pass.equals("bar")) {
      app.loginUser(login);
      return app.redirect("/app");
    }

    return app.redirect("/login");
  }

  @Test
  public void testLogin() throws Exception {

    app.setRequireLoggedInByDefault(true);

    // app redirects to login page when not logged in
    assertEquals("Please login", client.get("/app"));

    // wrong login/password redirects to login page
    assertEquals("Please login", client.post("/login", "login=foo&password="));

    // good login/password redirects to app
    assertEquals("Welcome foo",
                 client.post("/login", "login=foo&password=bar"));

    // app remains accessible thanks to session cookie
    assertEquals("Welcome foo", client.get("/app"));

    // logout link redirects to login page
    assertEquals("Please login", client.get("/logout"));

    // app redirects to login page when not logged in
    assertEquals("Please login", client.get("/app"));
  }
}
