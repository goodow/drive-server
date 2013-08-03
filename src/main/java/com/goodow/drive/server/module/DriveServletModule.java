package com.goodow.drive.server.module;

import com.goodow.drive.server.attachment.ServeServlet;
import com.goodow.drive.server.attachment.UploadServlet;

import com.google.inject.servlet.ServletModule;

public class DriveServletModule extends ServletModule {

  @Override
  protected void configureServlets() {
    serve("/upload").with(UploadServlet.class);
    serve("/serve").with(ServeServlet.class);
  }
}
