import * as React from 'react';
import { SafeAreaView, StyleSheet, Text, Image } from 'react-native';
import { Camera, useCameraDevices, useFrameProcessor } from 'react-native-vision-camera';
import { DBRConfig, decode, TextResult } from 'vision-camera-dynamsoft-barcode-reader';
import * as REA from 'react-native-reanimated';
import { GetImageData } from './src/plugins/GetImageData'
import { base64ToArrayBuffer } from './src/utils/Converters';

export default function App() {
  const [hasPermission, setHasPermission] = React.useState(false);
  const [frameData, setFrame] = React.useState(null);
  const devices = useCameraDevices();
  const device = devices.back;
  let t = null;
  const frameProcessor = useFrameProcessor((frame) => {
    'worklet'
    const base64 = GetImageData(frame).base64
    REA.runOnJS(setFrame)({imageData: base64});
    // setFrame({imageData: base64})

    // const config = {};
    // config.template = "{\"ImageParameter\":{\"BarcodeFormatIds\":[\"BF_QR_CODE\"],\"Description\":\"\",\"Name\":\"Settings\"},\"Version\":\"3.0\"}"; //scan qrcode only
    // const results = decode(frame, config)
    // if (results && results[0] && !frameData) {
    //   const base64 = GetImageData(frame).base64
    //   const frameData = { ...results[0], width: frame.width, height: frame.height, imageData: base64 }
    //   REA.runOnJS(setFrame)(frameData);
    //   RNCv.imageToMap()
    // }
  }, [])
  React.useEffect(() => {
    (async () => {
      const status = await Camera.requestCameraPermission();
      setHasPermission(status === 'authorized');
    })();
  }, []);

  return (
    <SafeAreaView style={styles.container}>
      {device != null &&
        hasPermission && (
          <>
            <Camera
              style={styles.camera}
              device={device}
              isActive={true}
              frameProcessor={frameProcessor}
              frameProcessorFps={1}
            />
            <Image source={{ uri: `data:image/jpeg;base64,${frameData?.imageData}` }} style={{ width: 200, height: 200 }} />
          </>
        )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  barcodeText: {
    fontSize: 20,
    color: 'white',
    fontWeight: 'bold',
  },
  camera: {
    width: 200,
    height: 200,
  }
});