package com.cameratest3.frameProcessor;
import androidx.camera.core.ImageProxy;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.cameratest3.readOptic.ReadOptic;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.ReadableNativeMap;
import com.mrousavy.camera.frameprocessor.FrameProcessorPlugin;
import com.cameratest3.*;

public class ImageFrameProcessorPlugin extends FrameProcessorPlugin {
  private WritableNativeMap map;
  private ReadOptic readOptic;
  @Override
  public Object callback(ImageProxy image, Object[] params) {
    ReadableNativeMap config = this.getConfig(params);

    if(readOptic == null) {
      readOptic = ReadOptic.getInstance(config);
    }

    @SuppressLint("UnsafeOptInUsageError")
    Bitmap bitmap = BitmapUtils.getBitmap(image);

    boolean isSuccess = readOptic.runReader(bitmap);
    map = readOptic.getMap();
    if(isSuccess) {
      Bitmap resultBitmap = readOptic.getResult();
      String base64 = ImageUtil.convert(resultBitmap);

      map.putString("base64", base64);
    }

    return map;
  }

  private ReadableNativeMap getConfig(Object[] params){
    if (params.length>0) {
      if (params[0] instanceof ReadableNativeMap) {
        ReadableNativeMap config = (ReadableNativeMap) params[0];
        return config;
      }
    }
    return null;
  }

  ImageFrameProcessorPlugin() {
    super("GetImageData");
  }
}