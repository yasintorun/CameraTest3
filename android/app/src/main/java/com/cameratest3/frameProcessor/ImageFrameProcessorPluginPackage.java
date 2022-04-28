package com.cameratest3.frameProcessor;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.mrousavy.camera.frameprocessor.FrameProcessorPlugin;
import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import androidx.annotation.NonNull;

public class ImageFrameProcessorPluginPackage implements ReactPackage {
  @NonNull
  @Override
  public List<NativeModule> createNativeModules(@NonNull ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        FrameProcessorPlugin.register(new ImageFrameProcessorPlugin());
    return modules;
  }

  @Nonnull
  @Override
  public List<ViewManager> createViewManagers(@Nonnull ReactApplicationContext reactContext) {
    return Collections.emptyList();
  }
}