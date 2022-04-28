package com.cameratest3.frameProcessor;
import androidx.camera.core.ImageProxy;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.ReadableNativeMap;
import com.mrousavy.camera.frameprocessor.FrameProcessorPlugin;
import com.cameratest3.*;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ImageFrameProcessorPlugin extends FrameProcessorPlugin {

  @Override
  public Object callback(ImageProxy image, Object[] params) {
    // code goes here
    WritableNativeMap map = new WritableNativeMap();
    ReadableNativeMap config = getConfig(params);

    @SuppressLint("UnsafeOptInUsageError")
    Bitmap bitmap = BitmapUtils.getBitmap(image);
    Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
    Utils.bitmapToMat(bitmap, mat);
    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
    Utils.matToBitmap(mat, bitmap);
    String base64 = ImageUtil.convert(bitmap);
    map.putString("base64", base64);
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