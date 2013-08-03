package com.goodow.drive.server.module;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class DriveServletContextListener extends GuiceServletContextListener {

  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new DriveApisModule(), new DriveServletModule(),
        new DriveAppEngineModule());
  }
}