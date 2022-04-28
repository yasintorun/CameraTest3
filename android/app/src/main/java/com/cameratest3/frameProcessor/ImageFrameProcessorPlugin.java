package com.cameratest3.frameProcessor;
import androidx.camera.core.ImageProxy;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.ReadableNativeMap;
import com.mrousavy.camera.frameprocessor.FrameProcessorPlugin;
import com.cameratest3.*;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ImageFrameProcessorPlugin extends FrameProcessorPlugin {

  @Override
  public Object callback(ImageProxy image, Object[] params) {
    // code goes here
    WritableNativeMap map = new WritableNativeMap();

    @SuppressLint("UnsafeOptInUsageError")
    Bitmap bitmap = BitmapUtils.getBitmap(image);

    Mat contourMat = CropAndPerspective(bitmap);
    Bitmap newBitmap = Bitmap.createBitmap(contourMat.cols(), contourMat.rows(), Bitmap.Config.ARGB_8888);

    Utils.matToBitmap(contourMat, newBitmap);

    String base64 = ImageUtil.convert(newBitmap);
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

  private Mat CropAndPerspective(Bitmap bitmap) {
    Mat processMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
    Utils.bitmapToMat(bitmap, processMat);

    Mat grayMat = new Mat();
    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();

    Imgproc.cvtColor(processMat, grayMat, Imgproc.COLOR_RGB2GRAY);
    Imgproc.adaptiveThreshold(grayMat, grayMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 75, 30);
    Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5), 3, 0);

    org.opencv.core.Core.bitwise_not(grayMat, grayMat);

    Mat cannyOutput = grayMat.clone();
    Mat hierarchy = new Mat();

    Imgproc.Canny(grayMat, cannyOutput, 100, 200, 5);

    Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

    double largest_area =0;
    int largest_contour_index = 0;
    for (int i = 0; i < contours.size(); i++) {
      double contourArea = Imgproc.contourArea(contours.get(i));
      if (contourArea > largest_area) {
        largest_area = contourArea;
        largest_contour_index = i;
      }
    }
    Imgproc.drawContours(processMat, contours, largest_contour_index, new Scalar(0, 255, 0, 255), 3);
    return processMat;
  }

  ImageFrameProcessorPlugin() {
    super("GetImageData");
  }
}



//  @Override
//  public Object callback(ImageProxy image, Object[] params) {
//    // code goes here
//    WritableNativeMap map = new WritableNativeMap();
//    ReadableNativeMap config = getConfig(params);
//    int x4 = config.getInt("x4");
//    int y4 = config.getInt("y4");
//
//    @SuppressLint("UnsafeOptInUsageError")
//    Bitmap bitmap = BitmapUtils.getBitmap(image);
////    Mat mat = new Mat(bitmap.getWidth(), bitmap.getHeight(), CvType.CV_8UC1);
////    Utils.bitmapToMat(bitmap, mat);
////    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
////    Imgproc.rectangle(mat, new Point(x4, y4), new Point(x4+10, y4+10), new Scalar(0, 0, 255), 5);
////    Utils.matToBitmap(mat, bitmap);
//    String base64 = ImageUtil.convert(bitmap);
//    map.putString("base64", base64);
//    return map;
//  }