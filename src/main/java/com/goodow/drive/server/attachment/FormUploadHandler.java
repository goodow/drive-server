package com.goodow.drive.server.attachment;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.http.HttpServerRequest;

public class FormUploadHandler implements Handler<HttpServerRequest> {
  private final String webRoot;

  public FormUploadHandler(String webRoot) {
    this.webRoot = webRoot;
  }

  @Override
  public void handle(final HttpServerRequest req) {
    req.expectMultiPart(true);
    req.uploadHandler(new Handler<HttpServerFileUpload>() {
      @Override
      public void handle(final HttpServerFileUpload upload) {
        upload.exceptionHandler(new Handler<Throwable>() {
          @Override
          public void handle(Throwable event) {
            req.response().end("Upload failed");
          }
        });
        upload.endHandler(new Handler<Void>() {
          @Override
          public void handle(Void event) {
            req.response()
                .end("Upload successful, you should see the file in the server directory");
          }
        });
        upload.streamToFileSystem(webRoot + "/" + upload.filename());
      }
    });
  }
}
