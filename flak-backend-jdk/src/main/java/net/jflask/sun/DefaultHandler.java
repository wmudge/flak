package net.jflask.sun;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.PrintStream;

import net.jflask.JdkApp;
import flak.RequestHandler;
import flak.util.Log;

public class DefaultHandler implements HttpHandler, RequestHandler {

  protected final JdkApp app;

  public DefaultHandler(JdkApp app) {
    this.app = app;
  }

  @Override
  public JdkApp getApp() {
    return app;
  }

  @Override
  public HttpHandler asHttpHandler() {
    return this;
  }

  public void handle(HttpExchange r) throws IOException {
    try {
      doHandle(r);
    }
    catch (Throwable t) {
      Log.error("Error occurred", t);
      r.sendResponseHeaders(500, 0);
      r.getResponseBody().write("Internal Server Error".getBytes());
      if (Log.DEBUG)
        t.printStackTrace(new PrintStream(r.getResponseBody()));
    }
    finally {
      r.getResponseBody().close();
    }
  }

  private void doHandle(HttpExchange r) throws Exception {

    Log.debug(r.getRequestURI());

    switch (r.getRequestMethod()) {
    case "GET":
      doGet(r);
      break;
    case "POST":
      doPost(r);
      break;
    case "PUT":
      doPut(r);
      break;
    default:
      throw new RuntimeException("Invalid method: " + r.getRequestMethod());
    }
  }

  public void doPut(HttpExchange r) throws Exception {
    throw new RuntimeException("Invalid method");
  }

  public void doPost(HttpExchange r) throws Exception {
    throw new RuntimeException("Invalid method");
  }

  public void doGet(HttpExchange r) throws Exception {
    throw new RuntimeException("Invalid method");
  }
}
