package com.goodow.drive.server.attachment;

import com.goodow.drive.server.bootstrap.DriveModule;
import com.goodow.realtime.operation.id.IdGenerator;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.http.HttpServerRequest;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FormUploadHandler implements Handler<HttpServerRequest> {
  private static final Logger log = Logger.getLogger(FormUploadHandler.class.getName());
  @Inject @Named(DriveModule.ATTACHMENTS_DIR) private String attachmentsDir;
  @Inject Client client;
  @Inject IdGenerator idGenerator;
  @Inject Vertx vertx;

  @Override
  public void handle(final HttpServerRequest req) {
    req.expectMultiPart(true);
    req.uploadHandler(new Handler<HttpServerFileUpload>() {
      @Override
      public void handle(final HttpServerFileUpload upload) {
        final String id = idGenerator.next(115);
        upload.exceptionHandler(new Handler<Throwable>() {
          @Override
          public void handle(Throwable e) {
            sendError(req, "Upload failed", e);
          }
        });
        upload.endHandler(new Handler<Void>() {
          @Override
          public void handle(Void event) {
            indexAttachment(id, req, upload);
          }
        });
        upload.streamToFileSystem(attachmentsDir + "/" + id);
      }
    });
  }

  private void indexAttachment(final String id, final HttpServerRequest req,
      final HttpServerFileUpload upload) {
    vertx.fileSystem().readFile(attachmentsDir + "/" + id, new AsyncResultHandler<Buffer>() {
      @Override
      public void handle(AsyncResult<Buffer> ar) {
        if (ar.succeeded()) {
          IndexRequestBuilder indexRequestBuilder;
          try {
            indexRequestBuilder =
                client.prepareIndex(DriveModule.INDEX, DriveModule.TYPE, id).setSource(
                    XContentFactory.jsonBuilder().startObject().startObject("file").field("_name",
                        upload.filename()).field("content", ar.result().getBytes()).endObject()
                        .endObject());
            indexRequestBuilder.execute(new ActionListener<IndexResponse>() {
              @Override
              public void onFailure(Throwable e) {
                sendError(req, "Failed to index", e);
              }

              @Override
              public void onResponse(IndexResponse response) {
                sendResponse(req, response);
              }
            });
          } catch (IOException e) {
            sendError(req, "Failed to index", e);
          }
        } else {
          sendError(req, "Failed to read", ar.cause());
        }
      }
    });
  }

  private void sendError(final HttpServerRequest req, String msg, Throwable e) {
    log.log(Level.SEVERE, msg, e);
    req.response().setStatusMessage(msg).setStatusCode(500).end();
  }

  private void sendResponse(final HttpServerRequest req, IndexResponse response) {
    if (response.isCreated()) {
      req.response().end(response.getId());
    } else {
      sendError(req, "id was already exits", null);
    }
  }
}