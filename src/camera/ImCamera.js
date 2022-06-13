import React from 'react';
import { Dimensions, StyleSheet } from 'react-native';
import { Camera, useCameraDevices, useFrameProcessor } from 'react-native-vision-camera';
import { decode } from 'vision-camera-dynamsoft-barcode-reader';
import { GetImageData } from './plugins/GetImageData'
import * as REA from 'react-native-reanimated'
import { FmtManager } from '../fmt/fmtManager';
import ReadOptic from '../readOptic/readOptic';

const { width, height } = Dimensions.get("screen")

const fmt =
    `20=43=03=D=*= =/DIMENSIONS=46,24,0//ORDER=1032/=
11=20=02=07=K=D=0123456789=X2=ogr_no=/STUDENTNUMBER/=
01=20=10=13=K=Y=ABCD=X2=turkce=/EXAM0/=
01=10=16=19=K=Y=ABCD=X2=inkilap=/EXAM1/=
01=10=22=25=K=Y=ABCD=X2=din=/EXAM2/=
01=10=28=31=K=Y=ABCD=X2=ingilizce=/EXAM3/=
01=20=35=38=K=Y=ABCD=X2=matematik=/EXAM4/=
01=20=41=44=K=Y=ABCD=X2=fen=/EXAM5/=`

const ImCamera = ({ setAnchor, style, active = true }) => {
    const [hasPermission, setHasPermission] = React.useState(false);
    const [number, setNumber] = React.useState(0)

    const devices = useCameraDevices();
    const device = devices.back;

    const fmtModel = new FmtManager().parser(fmt);

    const frameProcessor = useFrameProcessor((frame) => {
        'worklet'
        const config = {};
        config.template = "{\"ImageParameter\":{\"BarcodeFormatIds\":[\"BF_QR_CODE\"],\"Description\":\"\",\"Name\":\"Settings\"},\"Version\":\"3.0\"}"; //scan qrcode only
        const results = decode(frame, config)

        if (results && results[0] && fmtModel) {
            const qrPoint = [results[0].x4, results[0].y4]
            const { imageBase64 } = GetImageData(frame)
            const config = {
                fmt: fmtModel.fmt,
                point: qrPoint
            }
            ReadOptic.multiply(12, 20, (a, b) => {
                console.log(a, b)
            })
            // ReadOptic.runReader(imageBase64, config)?.then(data => console.log(typeof data))?.catch((data) => { console.log(data) })
            const frameData = {
                frame: imageBase64,
                qrPoint: qrPoint,
                fmt: fmtModel.fmt,
            }
            setAnchor && REA.runOnJS(setAnchor)(frameData)
        }

    }, [])

    console.log(number)

    React.useEffect(() => {
        (async () => {
            const status = await Camera.requestCameraPermission();
            setHasPermission(status === 'authorized');
        })();
    }, []);

    return device != null && hasPermission && (
        <Camera
            style={{ ...styles.camera, ...style }}
            device={device}
            isActive={active}
            frameProcessor={frameProcessor}
            frameProcessorFps={1}
        />
    )
}

const styles = StyleSheet.create({
    camera: {
        width: "100%",
        height: height * 0.4,
    }
});

export default ImCamera