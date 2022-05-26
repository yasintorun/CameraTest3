package com.cameratest3.frameProcessor;
import androidx.annotation.RequiresApi;
import androidx.camera.core.ImageProxy;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Debug;
import android.util.Log;

import com.cameratest3.models.Fmt;
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
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Rect2d;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

public class ImageFrameProcessorPlugin extends FrameProcessorPlugin {
  WritableNativeMap map;
  Fmt fmt;

  private int[] checkPoint;

  private int[] barcodePoint4;
  Mat showMat;
  Size fmtFormSize = new Size(640, 334);
  private List<RotatedRect> _dotPointRowList;
  private List<Rect> rowRects, colRects;

  @Override
  public Object callback(ImageProxy image, Object[] params) {
    map = new WritableNativeMap();
    ReadableNativeMap config = getConfig(params);
    if(config.getInt("ok") == 1) return map;
    ReadableNativeMap fmtReadableMap = config.getMap("fmt");
    if(fmt == null) {
      if(fmtReadableMap != null) {
        fmt = new Fmt(fmtReadableMap);
      }
    }
    else {
      map.putString("fmtt", fmt.toString());
      map.putString("fm", fmtReadableMap.toString());
    }
    fmtFormSize.height = Math.round(fmtFormSize.width * (fmt.dimensions[1] / fmt.dimensions[0]));
    // code goes here
    ReadableArray pointArray = config.getArray("point");
    ReadableArray checkArray = config.getArray("check");
    this.barcodePoint4 = new int[] {
            pointArray.getInt(0),
            pointArray.getInt(1),
    };

    this.checkPoint = new int[] {
            checkArray.getInt(0),
            checkArray.getInt(1),
    };

    @SuppressLint("UnsafeOptInUsageError")
    Bitmap bitmap = BitmapUtils.getBitmap(image);

    Mat contourMat = CropAndPerspective(bitmap);
    if(contourMat == null) {
      map.putString("base64", String.valueOf(_dotPointRowList.size()));
      return map;
    }
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

    MatOfPoint2f approxContours = new MatOfPoint2f();
    MatOfPoint2f cnt = new MatOfPoint2f(contours.get(largest_contour_index).toArray());
    Imgproc.approxPolyDP(cnt, approxContours, Imgproc.arcLength(cnt, true) * 0.04, true);

    List<Point> sortedPoints = getSortedPoints(approxContours.toList());
    if(sortedPoints != null) {
      processMat = Perspective(processMat, sortedPoints);
    }

    showMat = new Mat(fmtFormSize, processMat.type());
    Imgproc.resize(processMat, showMat, fmtFormSize);

    this.fmt.unitSize = (showMat.width() / fmt.dimensions[0]);

    _dotPointRowList = new ArrayList<>();

    if(this.findDataDots()) {
      this.readFmtPaper();
      return showMat;
    }
    return null;
  }

  private Mat Perspective(Mat src, List<Point> points) {
    Mat result = src.clone();
    Mat inputMat = new Mat(4, 1, CvType.CV_32FC2);
    Mat resultMat = new Mat(4, 1, CvType.CV_32FC2);

    Point tl = points.get(0);
    Point tr = points.get(1);
    Point br = points.get(2);
    Point bl = points.get(3);

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

    for (int j : this.fmt.orderCorner) {
      try {
        result.add(orderedPoints.get(j).point);
      } catch (Exception e) {
        return null;
      }
    }

    return result;
  }


  private Boolean findDataDots() {
    Mat tmpMat = showMat.clone();
    //Core.flip(tmpMat, tmpMat, 0);
    findRoiDot(tmpMat);
    boolean check = false;
    Collections.sort(_dotPointRowList, (o1, o2) -> (int) (o2.center.y - o1.center.y));
    if(_dotPointRowList.size() == fmt.rows) {
      check = true;
    }
    return check;
  }

  private void findRoiDot(Mat srcMat) {
    Rect tmpRect;
    Point rectPos;
    double rectHeight, rectWidth;
    Size rectSize;
    double posY, posX;

    _dotPointRowList.clear();
    rectHeight = showMat.height() - 10;
    rectWidth = this.fmt.unitSize * 2;
    rectSize = new Size(rectWidth, rectHeight);
    posY = 5;
    rectPos = new Point(5, posY);

    tmpRect = new Rect(rectPos, rectSize);

    Mat srcMat1 = new Mat(srcMat, tmpRect);
    save(srcMat1.clone(), "srcMat1");

    List<MatOfPoint> contours = new ArrayList<>();
    List<MatOfPoint2f> contoursMOP2F = new ArrayList<>();
    contours = this.findContours(srcMat1);

    map.putString("contours", String.valueOf(contours.size()));

    for (int i = 0; i < contours.size(); i++) {
      contoursMOP2F.add(new MatOfPoint2f(contours.get(i).toArray()));
      RotatedRect boundingBox = Imgproc.minAreaRect(new MatOfPoint2f(contoursMOP2F.get(i)));

      _dotPointRowList.add(boundingBox);
    }
  }

  ImageFrameProcessorPlugin() {
    super("GetImageData");
  }

  private void save(Mat mat, String name) {
    Bitmap newBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);

    Utils.matToBitmap(mat, newBitmap);

    String base64 = ImageUtil.convert(newBitmap);
    map.putString(name, base64);
  }

  private void readFmtPaper() {
    this.rowRects = new ArrayList<>();
    for (int i = 0; i< this._dotPointRowList.size(); i++) {
      Point point = new Point(0, _dotPointRowList.get(i).center.y - (this.fmt.unitSize * 0.5 * 0.5) + 5);
      Rect rowRect = new Rect(point, new Size(showMat.width(), this.fmt.unitSize * 0.5));
      rowRects.add(rowRect);
      //Imgproc.rectangle(this.showMat, rowRect.tl(), rowRect.br(), new Scalar(0, 255, 0, 255), 1);
    }
//    Imgproc.rectangle(this.showMat, rowRects.get(0).tl(), rowRects.get(0).br(), new Scalar(0, 255, 0, 255), 1);
//    Imgproc.rectangle(this.showMat, rowRects.get(rowRects.size() -1).tl(), rowRects.get(rowRects.size() -1).br(), new Scalar(0, 255, 0, 255), 1);
    this.colRects = new ArrayList<>();
    Point refPointOffset = new Point((this.fmt.unitSize * 0.1) + (this.fmt.unitSize * 2.5), 0);
    for(int i = 0; i<this.fmt.dimensions[0]; i++) {
      double tmpX = (this.fmt.unitSize*i) + refPointOffset.x + this.fmt.unitSize * 0.25;
      Rect colRect = new Rect(new Point(tmpX, 0), new Size((this.fmt.unitSize*0.5), showMat.height()));
      colRects.add(colRect);
      //Imgproc.rectangle(this.showMat, colRect.tl(), colRect.br(), new Scalar(0, 0, 255, 255), 1);
    }
    this.fmt.unitArea = rowRects.get(0).height * colRects.get(0).width;


    Mat hierarchy = new Mat();
    Mat grayMat = new Mat();

    Imgproc.cvtColor(this.showMat, grayMat, Imgproc.COLOR_RGB2GRAY);
    Imgproc.adaptiveThreshold(grayMat, grayMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 75, 30);
    save(grayMat.clone(), "gray");
    Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5), 3, 0);
    save(grayMat.clone(), "gaus");
    if(this.checkPoint[0] == 1)
      org.opencv.core.Core.bitwise_or(this.showMat, grayMat, grayMat);
    save(grayMat.clone(), "or");


    //Canny gerek yok
//    Mat cannyOutput = grayMat.clone();
//    Imgproc.Canny(grayMat, cannyOutput, 100, 200, 5);

    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    Imgproc.findContours(grayMat, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

    // Her bir kesişimleri aldık.
    // içerisi dolu mu (siyah) boş mu(beyaz) kontrolü yapmak gerekiyor.
    // Kare kontrolü yaptıktan sonra


//    Point rowTl = rowRects.get(rowRects.size() - 1).tl();
//    Point rowBr = rowRects.get(0).br();
//    Point colTl = colRects.get(0).tl();
//    Point colBr = colRects.get(colRects.size() - 1).br();
//    for(int i = 0; i<contours.size(); i++) {
//      Rect boundingBox = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray())).boundingRect();
//      if(boundingBox.tl().x < colTl.x
//        || boundingBox.br().x > colBr.x + 5
//        || boundingBox.tl().y < rowTl.y + 5
//        || boundingBox.br().y > rowBr.y
//      ) {
//        contours.remove(i);
//      }
//    }
    Mat drawConMat = this.showMat.clone();
    for(int i = 0; i < contours.size(); i++) {
      Rect boundingBox = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray())).boundingRect();
      Imgproc.rectangle(drawConMat, boundingBox.tl(), boundingBox.br(), new Scalar(255, 0, 0, 255), 1);
//      Imgproc.drawContours(drawConMat, contours, i, new Scalar(255,0,0,255),1,Imgproc.LINE_AA);
    }

    Mat srcc = this.showMat.clone();
    int co = 0;
    for(int i = rowRects.size() - 1; i>rowRects.size() - 3; i--) {
      for(int j = 9; j<12; j++) {
        Rect rect = contain(grayMat, i, j);
        Imgproc.rectangle(drawConMat, rect.tl(), rect.br(), new Scalar(0, 255, 0, 255), 1);
        for(int k = 0; k<contours.size(); k++) {
          Rect boundingBox = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(k).toArray())).boundingRect();
//          Imgproc.rectangle(drawConMat, boundingBox.tl(), boundingBox.br(), new Scalar(255, 0, 0, 255), 1);
//          Imgproc.rectangle(srcc, boundingBox.tl(), boundingBox.br(), new Scalar(255, 0, 0, 255), 1);
          boolean isIntersect = this.intersect3(rect, boundingBox);
          if(isIntersect) {
            co += 1;
            Imgproc.rectangle(drawConMat, boundingBox.tl(), boundingBox.br(), new Scalar(0, 0, 255, 255), 1);
          }
        }
      }
    }
    map.putString("co", String.valueOf(co));
    save(drawConMat.clone(), "sect");
    save(srcc.clone(), "srcc");

//    RotatedRect boundingBox = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(0).toArray()));

//    Mat m = new Mat()
//    Core.bitwise_and(this.showMat, );
//
//    Core.countNonZero();


//    Imgproc.rectangle(this.showMat, colRects.get(0).tl(), colRects.get(0).br(), new Scalar(0, 0, 255, 255), 1);
//    Imgproc.rectangle(this.showMat, colRects.get(colRects.size() - 1).tl(), colRects.get(colRects.size() -1).br(), new Scalar(0, 0, 255, 255), 1);
//    double unitArea = rowRects.get(0).height * colRects.get(0).width;
//
//    Map<Integer, List<RotatedRect>> rowContours = new HashMap<>();
//
//    List<MatOfPoint> contours = this.findContours(this.showMat);
//    List<MatOfPoint2f> contoursMOP2F = new ArrayList<MatOfPoint2f>();
//
//    double radiusSizeMin = unitArea * 0.55;
//
//    Point minPointX = this.getPointByCellAddress(0, 0);
//    Point maxPointX = this.getPointByCellAddress(this.fmtX, 0);
//
//    for(int i = 0; i<contours.size(); i++) {
//      contoursMOP2F.add(new MatOfPoint2f(contours.get(i).toArray()));
//      RotatedRect boundingBox = Imgproc.minAreaRect(new MatOfPoint2f(contoursMOP2F.get(i)));
//
//      double area = Imgproc.contourArea(contours.get(i));
//
//      if(area > 10000) continue;
//
//      if(boundingBox.center.x >= minPointX.x && boundingBox.center.x <= maxPointX.x) {
//        for(int j = 0; j<rowRects.size(); j++) {
//
//        }
//      }
//    }

  }

  boolean intersect3 (Rect a, Rect b) {
    return (  (  a.tl().x  <  b.br().x  )  &&  (   a.br().x   >  b.tl().x  )  &&
            (  a.tl().y  <  b.br().y  )  &&
            (  a.br().y  >  b.tl().y  )  );
  }

  boolean intersect2(Rect a, Rect b) {
    boolean noOverlap = a.tl().x > b.br().x ||
            b.tl().x > a.br().x ||
            a.tl().y > b.br().y ||
            b.tl().y > a.br().y;

    return !noOverlap;
  }

  private List<MatOfPoint> findContours(Mat srcMat) {
    Mat hierarchy = new Mat();
    Mat grayMat = new Mat();

    Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_RGB2GRAY);
    Imgproc.adaptiveThreshold(grayMat, grayMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 75, 30);
    save(grayMat.clone(), "gray");
    Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5), 3, 0);
    save(grayMat.clone(), "gaus");
    org.opencv.core.Core.bitwise_not(grayMat, grayMat);

    //Canny gerek yok
//    Mat cannyOutput = grayMat.clone();
//    Imgproc.Canny(grayMat, cannyOutput, 100, 200, 5);

    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    Imgproc.findContours(grayMat, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
    return contours;
  }

  private Point getPointByCellAddress(double col, double row) {
    Point refPointOffset = new Point((this.fmt.unitSize * 0.1) + (this.fmt.unitSize * 2.5), 0);
    double pointDistanceX = (col * this.fmt.unitSize) + refPointOffset.x +(this.fmt.unitSize * 0.25);
    row = row < 0 ? 0 : row;
    double pointY = _dotPointRowList.get((int) row).center.y + refPointOffset.y + 5;
    return new Point(pointDistanceX, pointY);
  }

  private Rect contain(Mat srcMat, int row, int col) {
    return this.intersectWith(this.rowRects.get(row), this.colRects.get(col));
  }

  Rect intersectWith(Rect a, Rect b) {
    return  new Rect(new Point(b.x, a.y), new Size(b.width, a.height));
  }

}

class OrderedPoint {
  public Point point;
  public double distance;
}
