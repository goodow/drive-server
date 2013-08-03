package com.goodow.drive.server.module;

import com.goodow.drive.server.attachment.AttachmentEndpoint;
import com.goodow.drive.server.proxy.LocalDevServerFilter;
import com.goodow.drive.server.proxy.ProxyFilter;
import com.goodow.realtime.channel.http.HttpTransport;

import com.google.api.server.spi.guice.GuiceSystemServiceServletModule;
import com.google.appengine.api.utils.SystemProperty;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;

import java.util.HashSet;
import java.util.Set;

public class DriveApisModule extends GuiceSystemServiceServletModule {
  public static final String FRONTEND_ROOT = HttpTransport.DEFAULT + ProxyFilter.PROXY_PATH + "api";
  public static final String BACKENDROOT_ROOT = HttpTransport.DEFAULT + ProxyFilter.PROXY_PATH
      + "spi";
  public static final String DEFAULT_VERSION = "v0.0.1";

  @Override
  protected void configureServlets() {
    install(new JpaPersistModule("transactions-optional"));
    filterRegex("^/(?!_ah/(upload|admin)).*$").through(PersistFilter.class);

    filter(ProxyFilter.PROXY_PATH + "*").through(ProxyFilter.class);
    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
      filter("/_ah/api/" + "*").through(LocalDevServerFilter.class);
    }

    Set<Class<?>> serviceClasses = new HashSet<Class<?>>();
    serviceClasses.add(AttachmentEndpoint.class);
    this.serveGuiceSystemServiceServlet("/_ah/spi/*", serviceClasses);
  }
}
