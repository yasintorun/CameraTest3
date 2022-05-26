package com.cameratest3.models;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import org.opencv.core.Point;
import org.opencv.core.Size;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Arrays;

public class Fmt {
    public final static int DIMENSION = 640; //Quality of optic paper -> 480p: 640, 720p: 1280, 1080p: 1920
    public int rows;
    public int columns;
    public int[] dimensions = new int[3];
    public int[] orderCorner = new int[4];
    public int unitArea;
    public int unitSize;
    public Size formSize;

    public Fmt() {}

    public Fmt(ReadableMap map) {
        if(map == null) {
            return;
        }
        try {
            this.rows = map.getInt("rows");
            this.columns = map.getInt("columns");

            ReadableArray orderCornerArray = map.getArray("orderCorner");
            if(orderCornerArray != null) {
                for (int i = 0; i<4 && i<orderCornerArray.size(); i++) {
                    orderCorner[i] = orderCornerArray.getInt(i);
                }
            }

            ReadableArray dimensionsArray = map.getArray("dimensions");
            if(dimensionsArray != null) {
                for (int i = 0; i<3 && i<dimensionsArray.size(); i++) {
                    dimensions[i] = dimensionsArray.getInt(i);
                }
            }
            this.formSize = new Size(Fmt.DIMENSION, Fmt.DIMENSION * (dimensions[1] / dimensions[0]));
        }
        catch (Exception ignored) {}
    }

    @NonNull
    @Override
    public String toString() {
        if(this.orderCorner == null) {
            return String.format("Rows: %d\tCols: %d", this.rows, this.columns);
        }
        return String.format("Rows: %d\tCols: %d\tOrderCorner: %s\tDimensions: %s\tUnitSize: %d\tUnitArea: %s",
                this.rows, this.columns, Arrays.toString(this.orderCorner), Arrays.toString(this.dimensions), this.unitSize, this.unitArea);
    }
}
