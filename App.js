import * as React from 'react';
import { SafeAreaView, StyleSheet, Text, Image } from 'react-native';
import { Camera, useCameraDevices, useFrameProcessor } from 'react-native-vision-camera';
import { DBRConfig, decode, TextResult } from 'vision-camera-dynamsoft-barcode-reader';
import * as REA from 'react-native-reanimated';
import { GetImageData } from './src/plugins/GetImageData'
import { base64ToArrayBuffer } from './src/utils/Converters';
import { FmtManager, FmtModelType } from './src/fmt/fmtManager';

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
  let ok = 0;
  const frameProcessor = useFrameProcessor((frame) => {
    'worklet'    
    const config = {};
    config.template = "{\"ImageParameter\":{\"BarcodeFormatIds\":[\"BF_QR_CODE\"],\"Description\":\"\",\"Name\":\"Settings\"},\"Version\":\"3.0\"}"; //scan qrcode only
    const results = decode(frame, config)
    if (results && results[0] && fmtModel) {
      const r = results[0]
      const point = [r.x4, r.y4]
      const corners = [1,0, 2, 3]
      const check = [2, 11]
      console.log(typeof fmtModel.fmt.orderCorner[0])
      const data = GetImageData(frame, {point: point, corners: corners, check: check, ok: ok, fmt: {...fmtModel.fmt}})
      console.log(data.fmtt)
      ok = data.base64?.length > 10 ? 0 : 1
      if(data?.base64) {
        REA.runOnJS(setFrame)({...data});
        // console.log(data?.contours)
      }
    }
  }, [fmtModel])

  React.useEffect(() => {
    (async () => {
      const status = await Camera.requestCameraPermission();
      setHasPermission(status === 'authorized');
    })();

    const _fmtModel = new FmtManager().parser(fmt);
    setFmtModel(_fmtModel)
    console.log(_fmtModel)
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