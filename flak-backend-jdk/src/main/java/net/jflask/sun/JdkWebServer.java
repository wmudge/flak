package net.jflask.sun;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;
import flak.RequestHandler;
import flak.WebServer;
import net.jflask.JdkApp;

/**
 * Wrapper for the HTTP server embedded in the JDK. It may be shared by several
 * apps.
 *
 * @author pcdv
 * @see <a
 * href="http://docs.oracle.com/javase/7/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html">Documentation
 * for HTTPServer</a>
 */
public class JdkWebServer implements WebServer {

  private HttpServer srv;

  private ExecutorService pool;

  private InetSocketAddress address;

  private final Map<String, RequestHandler> handlers = new Hashtable<>();

  private Vector<JdkApp> apps = new Vector<>();

  public JdkWebServer(int port, ExecutorService pool) {
    this.pool = pool;
    this.address = new InetSocketAddress(port);
  }

  public JdkWebServer() {
    // TODO: allow to set port and pool later
  }

  public void addApp(JdkApp app) {
    apps.add(app);
  }

  public void removeApp(JdkApp app) {
    apps.remove(app);
    if (apps.isEmpty())
      stop();
  }

  public JdkWebServer addHandler(String path, RequestHandler handler) {
    RequestHandler old = handlers.get(path);
    if (old != null) {
      if (old.getApp() != handler.getApp())
        throw new RuntimeException(String.format(
            "Path %s already used by app %s",
            path,
            old.getApp()));
      else
        throw new RuntimeException("Path already used: " + path);
    }

    handlers.put(path, handler);
    if (srv != null)
      srv.createContext(path, handler.asHttpHandler());
    return this;
  }

  public void setPort(int port) {
    this.address = new InetSocketAddress(port);
  }

  private void checkNotStarted() {
    if (srv != null)
      throw new IllegalStateException("Already started");
  }

  /**
   * Shuts down the web sever.
   * <p/>
   * WARNING: with JDK6, HttpServer creates a zombie thread (blocked on a
   * sleep()). No problem with JDK 1.7.0_40.
   */
  public void stop() {
    this.srv.stop(0);
    pool.shutdownNow();
  }

  public int getPort() {
    return srv.getAddress().getPort();
  }

  public void start() throws IOException {
    if (pool == null)
      pool = Executors.newCachedThreadPool();

    if (address == null)
      throw new IllegalStateException("Address not set. Call AppFactory.setHttpPort()");

    this.srv = HttpServer.create(address, 0);
    this.srv.setExecutor(pool);
    for (Map.Entry<String, RequestHandler> e : handlers.entrySet()) {
      String path = e.getKey();
      if (path.isEmpty())
        path = "/";
      srv.createContext(path, e.getValue().asHttpHandler());
    }

    this.srv.start();
  }

  public boolean isStarted() {
    return srv != null;
  }
}
