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
  let ok = 0;
  const frameProcessor = useFrameProcessor((frame) => {
    'worklet'
    
    const config = {};
    config.template = "{\"ImageParameter\":{\"BarcodeFormatIds\":[\"BF_QR_CODE\"],\"Description\":\"\",\"Name\":\"Settings\"},\"Version\":\"3.0\"}"; //scan qrcode only
    const results = decode(frame, config)
    if (results && results[0]) {
      const r = results[0]
      const point = [r.x4, r.y4]
      const corners = [1,0, 2, 3]
      const check = [2, 11]
      const data = GetImageData(frame, {point: point, corners: corners, check: check, ok: ok})
      console.log(data.co)
      ok = data.base64?.length > 10 ? 0 : 1
      if(data?.base64) {
        REA.runOnJS(setFrame)({...data});
        // console.log(data?.contours)
      }
    }
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
            <Image source={{ uri: `data:image/jpeg;base64,${frameData?.sect}` }} style={{ width: 345, height: 180, resizeMode: "contain" }} />
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
    width: 345,
    height: 300,
  }
});