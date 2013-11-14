package com.goodow.drive.server.attachment;

import java.util.HashSet;
import java.util.Set;

class AttachmentUtil {
  private static final Set<String> binaryPrefixes = new HashSet<String>();
  static {
    binaryPrefixes.add("image/");
    binaryPrefixes.add("audio/");
    binaryPrefixes.add("video/");
    binaryPrefixes.add("application/x-shockwave-flash");
  }

  public static final boolean isBinary(String contentType) {
    for (String prefix : binaryPrefixes) {
      if (contentType.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }
}
