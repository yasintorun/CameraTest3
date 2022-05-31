package com.cameratest3.frameProcessor;
import androidx.camera.core.ImageProxy;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.cameratest3.readOptic.ReadOptic;
import com.facebook.react.bridge.WritableNativeMap;
import com.mrousavy.camera.frameprocessor.FrameProcessorPlugin;
import com.cameratest3.*;

public class ImageFrameProcessorPlugin extends FrameProcessorPlugin {
  @Override
  public Object callback(ImageProxy image, Object[] params) {
    WritableNativeMap map = new WritableNativeMap();

    @SuppressLint("UnsafeOptInUsageError")
    Bitmap bitmap = BitmapUtils.getBitmap(image);
    String base64 = ImageUtil.convert(bitmap);

    map.putString("imageBase64", base64);

    return map;
  }

  ImageFrameProcessorPlugin() {
    super("GetImageData");
  }
}