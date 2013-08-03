package com.goodow.drive.server.module;

import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class DriveAppEngineModule extends AbstractModule {

  @Override
  protected void configure() {
  }

  @Provides
  @Singleton
  BlobInfoFactory provideBlobInfoFactory() {
    return new BlobInfoFactory();
  }

  @Provides
  BlobstoreService provideBlobstoreService() {
    return BlobstoreServiceFactory.getBlobstoreService();
  }

  @Provides
  ImagesService provideImagesService() {
    return ImagesServiceFactory.getImagesService();
  }

  @Provides
  URLFetchService provideUrlFetchService() {
    return URLFetchServiceFactory.getURLFetchService();
  }
}
