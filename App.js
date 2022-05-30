import * as React from 'react';
import { Image, SafeAreaView, StyleSheet } from 'react-native';
import * as REA from 'react-native-reanimated';
import { Camera, useCameraDevices, useFrameProcessor } from 'react-native-vision-camera';
import { decode } from 'vision-camera-dynamsoft-barcode-reader';
import { FmtManager } from './src/fmt/fmtManager';
import { GetImageData } from './src/plugins/GetImageData';

const fmt =
`20=43=03=D=*= =/DIMENSIONS=46,24,0//ORDER=1032/=
11=20=02=07=K=D=0123456789=X2=ogr_no=/STUDENTNUMBER/=
01=20=10=13=K=Y=ABCD=X2=turkce=/EXAM0/=
01=10=16=19=K=Y=ABCD=X2=inkilap=/EXAM1/=
01=10=22=25=K=Y=ABCD=X2=din=/EXAM2/=
01=10=28=31=K=Y=ABCD=X2=ingilizce=/EXAM3/=
01=20=35=38=K=Y=ABCD=X2=matematik=/EXAM4/=
01=20=41=44=K=Y=ABCD=X2=fen=/EXAM5/=`

export default function App() {
  const [hasPermission, setHasPermission] = React.useState(false);
  const [frameData, setFrame] = React.useState(null);
  const [fmtModel, setFmtModel] = React.useState()

  const devices = useCameraDevices();
  const device = devices.back;
  const frameProcessor = useFrameProcessor((frame) => {
    'worklet'    
    const config = {};
    config.template = "{\"ImageParameter\":{\"BarcodeFormatIds\":[\"BF_QR_CODE\"],\"Description\":\"\",\"Name\":\"Settings\"},\"Version\":\"3.0\"}"; //scan qrcode only
    const results = decode(frame, config)
    if (results && results[0] && fmtModel) {
      const r = results[0]
      const point = [r.x4, r.y4]
      console.log(point)
      const data = GetImageData(frame, {rotate: -1, point: point, ok: 2, fmt: {...fmtModel.fmt}})
      console.log(Object.keys(data))
      REA.runOnJS(setFrame)({...data});
    }
  }, [fmtModel])

  React.useEffect(() => {
    (async () => {
      const status = await Camera.requestCameraPermission();
      setHasPermission(status === 'authorized');
    })();

    const _fmtModel = new FmtManager().parser(fmt);
    setFmtModel(_fmtModel)
  }, []);
  console.log(frameData?.sorted)
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
            <Image source={{ uri: `data:image/jpeg;base64,${frameData?.after}` }} style={{ width: 345, height: 180, resizeMode: "contain" }} />
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