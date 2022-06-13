package com.cameratest3.readOptic;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.cameratest3.ImageUtil;
import com.cameratest3.OpenCV.ImOpenCV;
import com.cameratest3.models.Fmt;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableNativeMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import org.opencv.android.Utils;
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

public class ReadOpticModule extends ReactContextBaseJavaModule  {
    WritableNativeMap map;
    ReadOptic readOptic;
    public ReadOpticModule(ReactApplicationContext context) {
        super(context);
        this.readOptic = ReadOptic.getInstance();
    }

    @ReactMethod
    public void multiply(int a, int b, final Callback callback) {
        callback.invoke(a, b);
    }

    @ReactMethod
    public void runReader(String base64, ReadableMap config, final Promise promise) {
        try {
            this.readOptic.setConfig(config);
            WritableNativeMap result = this.readOptic.runReader(base64);
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject("RunReader Error: ", e.getMessage());
        }
    }

    public void readPanel() {
        //Panelleri okumaya başlıyoruz.
    }

    public WritableNativeMap getMap() {
        return this.map;
    }

    private void save(Mat mat, String name) {
        Bitmap newBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(mat, newBitmap);

        String base64 = ImageUtil.convert(newBitmap);
        map.putString(name, base64);
    }

    @NonNull
    @Override
    public String getName() {
        return "ReadOptic";
    }
}
