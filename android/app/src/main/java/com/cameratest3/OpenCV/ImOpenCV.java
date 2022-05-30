package com.cameratest3.OpenCV;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ImOpenCV {
    public Mat perspective(Mat src, List<Point> points, int[] orderCorner)
    {
        Mat result = src.clone();
        Mat inputMat = new Mat(4, 1, CvType.CV_32FC2);
        Mat resultMat = new Mat(4, 1, CvType.CV_32FC2);

        inputMat.put(0, 0,
                points.get(orderCorner[0]).x, points.get(orderCorner[0]).y,
                points.get(orderCorner[1]).x, points.get(orderCorner[1]).y,
                points.get(orderCorner[2]).x, points.get(orderCorner[2]).y,
                points.get(orderCorner[3]).x, points.get(orderCorner[3]).y);

        resultMat.put(0, 0,
                0, 0,
                0, result.rows(),
                result.cols(), 0,
                result.cols(), result.rows()
        );

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(inputMat, resultMat);

        Imgproc.warpPerspective(result, result, perspectiveTransform,
                new Size(result.cols(), result.rows()));

        return result;
    }

    public List<MatOfPoint> findContours(Mat srcMat, boolean isCannyActive, String bitwiseMode) {
        Mat hierarchy = new Mat();
        Mat grayMat = new Mat();

        Imgproc.cvtColor(srcMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(grayMat, grayMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 75, 30);
        Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5), 3, 0);

        switch (bitwiseMode) {
            case "not":
                Core.bitwise_not(grayMat, grayMat);
                break;
            case "or":
                Core.bitwise_or(srcMat, grayMat, grayMat);
                break;
            case "and":
                Core.bitwise_and(srcMat, grayMat, grayMat);
                break;
        }

        Mat output = grayMat.clone();
        if(isCannyActive) {
            Imgproc.Canny(grayMat, output, 100, 200, 5);
        }

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(output, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint lhs, MatOfPoint rhs) {
                return Double.compare(Imgproc.contourArea(rhs), Imgproc.contourArea(lhs));
            }
        });

        hierarchy.release();
        grayMat.release();
        output.release();

        return contours;
    }

    public List<Point> sortPoints(List<Point> src) {
        List<Point> result = new ArrayList<>();

        Comparator<Point> sumComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.compare(lhs.y + lhs.x, rhs.y + rhs.x);
            }
        };

        Comparator<Point> diffComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.compare(lhs.y - lhs.x, rhs.y - rhs.x);
            }
        };

        // top-left corner = minimal sum
        result.add(Collections.min(src, sumComparator));

        // top-right corner = minimal difference
        result.add(Collections.min(src, diffComparator));

        // bottom-right corner = maximal sum
        result.add(Collections.max(src, sumComparator));

        // bottom-left corner = maximal difference
        result.add(Collections.max(src, diffComparator));

        return result;
    }

    public static Rect and (Rect a, Rect b) {
        return  new Rect(new Point(b.x, a.y), new Size(b.width, a.height));
    }

    public static boolean intersect (Rect a, Rect b) {
        return (( a.tl().x < b.br().x) && (a.br().x > b.tl().x) &&
                (a.tl().y < b.br().y) &&
                (a.br().y > b.tl().y));
    }

    public List<RotatedRect> findRoiDot(Mat srcMat, int unitSize) {
        Rect tmpRect;
        Point rectPos;
        double rectHeight, rectWidth;
        Size rectSize;
        double posY, posX;
        List<RotatedRect> dotPointRowList = new ArrayList<>();
        rectHeight = srcMat.height() - 10;
        rectWidth = unitSize * 2;
        rectSize = new Size(rectWidth, rectHeight);
        posY = 5;
        rectPos = new Point(5, posY);

        tmpRect = new Rect(rectPos, rectSize);

        Mat srcMat1 = new Mat(srcMat, tmpRect);

        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint2f> contoursMOP2F = new ArrayList<>();
        contours = this.findContours(srcMat1, false, "not");

        for (int i = 0; i < contours.size(); i++) {
            contoursMOP2F.add(new MatOfPoint2f(contours.get(i).toArray()));
            RotatedRect boundingBox = Imgproc.minAreaRect(new MatOfPoint2f(contoursMOP2F.get(i)));
            dotPointRowList.add(boundingBox);
        }

        Collections.sort(dotPointRowList, (o1, o2) -> (int) (o2.center.y - o1.center.y));

        return dotPointRowList;
    }

    public List<Point> getSortedPoints(List<Point> points, Point barcode, int[]orderCorner) {
        List<Point> result = new ArrayList<>();
        List<OrderedPoint> orderedPoints = new ArrayList<>();

        for(int i = 0; i<points.size(); i++ ) {
            OrderedPoint op = new OrderedPoint();
            op.point = points.get(i);
            op.distance = Math.sqrt((Math.pow(op.point.x - barcode.x, 2) + Math.pow(op.point.y - barcode.y, 2)));
            orderedPoints.add(op);
        }
        Collections.sort(orderedPoints, (o1, o2) -> (int) (o1.distance - o2.distance));

        for (int j : orderCorner) {
            try {
                result.add(orderedPoints.get(j).point);
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("ERROR", "getSortedPoints: ERROR YASIN TORUN" + e.getLocalizedMessage());
                return null;
            }
        }

        return result;
    }

    public int getLargestContourIndex(List<MatOfPoint> contours) {
        double largest_area =0;
        int largest_contour_index = 0;
        for (int i = 0; i < contours.size(); i++) {
            double contourArea = Imgproc.contourArea(contours.get(i));
            if (contourArea > largest_area) {
                largest_area = contourArea;
                largest_contour_index = i;
            }
        }
        return largest_contour_index;
    }
}