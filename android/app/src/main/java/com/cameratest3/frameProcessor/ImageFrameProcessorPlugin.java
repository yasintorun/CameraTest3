package com.cameratest3.frameProcessor;
import androidx.annotation.RequiresApi;
import androidx.camera.core.ImageProxy;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Debug;
import android.util.Log;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.ReadableNativeMap;
import com.mrousavy.camera.frameprocessor.FrameProcessorPlugin;
import com.cameratest3.*;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class ImageFrameProcessorPlugin extends FrameProcessorPlugin {

  private int[] barcodePoint4;
  private int[] orderCorner;

  @Override
  public Object callback(ImageProxy image, Object[] params) {
    // code goes here
    WritableNativeMap map = new WritableNativeMap();
    ReadableNativeMap config = getConfig(params);
    ReadableArray pointArray = config.getArray("point");
    ReadableArray orderCorner = config.getArray("corners");
    this.barcodePoint4 = new int[] {
            pointArray.getInt(0),
            pointArray.getInt(1),
    };

    this.orderCorner = new int[] {
            orderCorner.getInt(0),
            orderCorner.getInt(1),
            orderCorner.getInt(2),
            orderCorner.getInt(3),

    };


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


    MatOfPoint2f approxContours = new MatOfPoint2f();
    MatOfPoint2f cnt = new MatOfPoint2f(contours.get(largest_contour_index).toArray());
    Imgproc.approxPolyDP(cnt, approxContours, Imgproc.arcLength(cnt, true) * 0.04, true);

    List<Point> sortedPoints = getSortedPoints(approxContours.toList());
    if(sortedPoints != null) {
      processMat = Perspective(processMat, sortedPoints);
    }

    return processMat;
  }

  private Mat Perspective(Mat src, List<Point> points) {
    Mat result = src.clone();
    Mat inputMat = new Mat(4, 1, CvType.CV_32FC2);
    Mat resultMat = new Mat(4, 1, CvType.CV_32FC2);

    Point tl = points.get(0);
    Point tr = points.get(1);
    Point br = points.get(2);
    Point bl = points.get(3);

    Log.i("YASİN TORUN DEBUG",points.toString());
    Log.i("YASİN TORUN DEBUG",points.get(0).toString());


    double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
    double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));

    double dw = Math.max(widthA, widthB);
    int maxWidth = Double.valueOf(dw).intValue();

    double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
    double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));

    double dh = Math.max(heightA, heightB);
    int maxHeight = Double.valueOf(dh).intValue();


    Mat doc = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

    inputMat.put(0, 0, tl.x, tl.y, tr.x, tr.y, br.x, br.y, bl.x, bl.y);
    resultMat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh);

    Mat perspectiveTransform = Imgproc.getPerspectiveTransform(inputMat, resultMat);
    Imgproc.warpPerspective(result, result, perspectiveTransform,
              doc.size());
    Core.rotate(result, result, Core.ROTATE_90_COUNTERCLOCKWISE);
    return result;
  }

  private List<Point> getSortedPoints(List<Point> points) {
    List<Point> result = new ArrayList<>();
    List<OrderedPoint> orderedPoints = new ArrayList<>();

    for(int i = 0; i<points.size(); i++ ) {
        OrderedPoint op = new OrderedPoint();
        op.point = points.get(i);
        op.distance = Math.sqrt((Math.pow(op.point.x - barcodePoint4[0], 2) + Math.pow(op.point.y - barcodePoint4[1], 2)));
        orderedPoints.add(op);
    }
    Collections.sort(orderedPoints, (o1, o2) -> (int) (o1.distance - o2.distance));

    for (int j : this.orderCorner) {
      try {
        result.add(orderedPoints.get(j).point);
      } catch (Exception e) {
        return null;
      }
    }

    return result;
  }

  ImageFrameProcessorPlugin() {
    super("GetImageData");
  }
}



class OrderedPoint {
  public Point point;
  public double distance;
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