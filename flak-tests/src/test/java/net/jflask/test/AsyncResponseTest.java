package net.jflask.test;

import java.io.IOException;

import flak.Response;
import flak.annotations.Route;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Misc POST tests.
 */
public class AsyncResponseTest extends AbstractAppTest {

  @Route("/sync")
  public Response replySync() throws IOException {
    return reply(app.getResponse());
  }

  private Response reply(Response resp) throws IOException {
    resp.setStatus(200);
    resp.getOutputStream().write("foobar".getBytes());
    resp.getOutputStream().close();
    return resp;
  }

  @Route("/async")
  public Response replyAsync() throws IOException {
    final Response r = app.getResponse();
    new Thread() {
      @Override
      public void run() {
        try {
          reply(r);
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }.start();
    return r;
  }

  @Test
  public void testSync() throws Exception {
    assertEquals("foobar", client.get("/sync"));
  }

  /**
   * HttpServer does not support replying asynchronously to requests.
   */
  @Ignore
  @Test
  public void testAsync() throws Exception {
    assertEquals("foobar", client.get("/async"));
  }
}
