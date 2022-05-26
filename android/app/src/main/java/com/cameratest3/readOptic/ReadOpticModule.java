package com.cameratest3.readOptic;

import android.graphics.Bitmap;

import com.cameratest3.OpenCV.ImOpenCV;
import com.cameratest3.models.Fmt;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableNativeMap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReadOpticModule {
    private Fmt fmt;
    private ImOpenCV imOpenCV;
    private Point barcode;
    private Mat showMat;

    private List<RotatedRect> dotPointRowList;
    private List<Rect> rowRects, colRects;

    public ReadOpticModule(ReadableNativeMap readableNativeMap) {
        this.setFmt(readableNativeMap);
        this.setBarcode(readableNativeMap);

        this.showMat = new Mat(this.fmt.formSize, CvType.CV_8UC1);
        this.fmt.unitSize = (this.showMat.width() / fmt.dimensions[0]);

        this.imOpenCV = new ImOpenCV();
    }

    //Fmt
    public Fmt getFmt() { return this.fmt; }

    public void setFmt(ReadableNativeMap map) {
        ReadableNativeMap fmtReadableMap = map.getMap("fmt");
        fmt = new Fmt(fmtReadableMap);
    }
    /*********/

    //Barcode
    public Point getBarcode() { return this.barcode; }

    public void setBarcode(ReadableNativeMap map) {
        ReadableArray pointArray = map.getArray("point");
        assert pointArray != null;
        this.barcode = new Point(pointArray.getInt(0), pointArray.getInt(1));
    }
    /*********/

    public boolean runReader(Bitmap bitmap) {
        Mat processMat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, processMat);
        List<MatOfPoint> contours = imOpenCV.findContours(processMat, true, "not");
        int largestContourIndex = imOpenCV.getLargestContourIndex(contours);

        MatOfPoint2f approxContours = new MatOfPoint2f();
        MatOfPoint2f cnt = new MatOfPoint2f(contours.get(largestContourIndex).toArray());
        Imgproc.approxPolyDP(cnt, approxContours, Imgproc.arcLength(cnt, true) * 0.04, true);

        List<Point> sortedPoints = imOpenCV.getSortedPoints(approxContours.toList(), this.barcode, this.fmt.orderCorner);

        if(sortedPoints == null) {
            return false;
        }
        processMat = imOpenCV.perspective(processMat, sortedPoints, Core.ROTATE_90_COUNTERCLOCKWISE);

        Imgproc.resize(processMat, this.showMat, this.fmt.formSize);

        dotPointRowList = new ArrayList<>();

        boolean isFoundDots = this.findDataDots();
        if(!isFoundDots) {
            return false;
        }

        return true;
    }

    public void readPanel() {
        //Panelleri okumaya başlıyoruz.
    }

    private void fillRects() {
        this.rowRects = new ArrayList<>();
        for (int i = 0; i< this.fmt.rows; i++) {
            Point point = new Point(0, this.dotPointRowList.get(i).center.y - (this.fmt.unitSize * 0.5 * 0.5) + 5);
            Rect rowRect = new Rect(point, new Size(showMat.width(), this.fmt.unitSize * 0.5));
            rowRects.add(rowRect);
        }

        this.colRects = new ArrayList<>();
        Point refPointOffset = new Point((this.fmt.unitSize * 0.1) + (this.fmt.unitSize * 2.5), 0);
        for(int i = 0; i<this.fmt.dimensions[0]; i++) {
            double tmpX = (this.fmt.unitSize*i) + refPointOffset.x + this.fmt.unitSize * 0.25;
            Rect colRect = new Rect(new Point(tmpX, 0), new Size((this.fmt.unitSize*0.5), showMat.height()));
            colRects.add(colRect);
        }

        this.fmt.unitArea = rowRects.get(0).height * colRects.get(0).width;
    }

    private Mat getReadableContours() {
        List<MatOfPoint> contours = imOpenCV.findContours(this.showMat.clone(), false, "or");
        Mat drawConMat = this.showMat.clone();
        for(int i = 0; i < contours.size(); i++) {
            Rect boundingBox = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray())).boundingRect();
            Imgproc.rectangle(drawConMat, boundingBox.tl(), boundingBox.br(), new Scalar(255, 0, 0, 255), 1);
        }
        return drawConMat;
    }

    private boolean findDataDots() {
        Mat tmpMat = this.showMat.clone();
        //Core.flip(tmpMat, tmpMat, 0);
        imOpenCV.findRoiDot(tmpMat, this.fmt.unitSize);
        boolean check = false;
        Collections.sort(dotPointRowList, (o1, o2) -> (int) (o2.center.y - o1.center.y));
        if(dotPointRowList.size() == this.fmt.rows) {
            check = true;
        }
        return check;
    }

}
