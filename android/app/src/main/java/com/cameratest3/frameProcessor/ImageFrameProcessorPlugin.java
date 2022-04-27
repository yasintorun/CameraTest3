package com.cameratest3.frameprocessor;
import androidx.camera.core.ImageProxy;
import android.graphics.Bitmap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.ReadableNativeMap;
import com.mrousavy.camera.frameprocessor.FrameProcessorPlugin;
import com.cameratest3.*;
public class ImageFrameProcessorPlugin extends FrameProcessorPlugin {

  @Override
  public Object callback(ImageProxy image, Object[] params) {
    // code goes here
    WritableNativeMap map = new WritableNativeMap();
    Bitmap bitmap = BitmapUtils.getBitmap(image);
    String base64 = ImageUtil.convert(bitmap);
    map.putString("base64", base64);
    return map;
  }

  ImageFrameProcessorPlugin() {
    super("GetImageData");
  }
}