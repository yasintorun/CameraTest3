import React from 'react';
import { Dimensions, StyleSheet } from 'react-native';
import { Camera, useCameraDevices, useFrameProcessor } from 'react-native-vision-camera';
import { decode } from 'vision-camera-dynamsoft-barcode-reader';
import { GetImageData } from './plugins/GetImageData'
import * as REA from 'react-native-reanimated'

const { width, height } = Dimensions.get("screen")

const ImCamera = ({ setAnchor, style, active=true }) => {
    const [hasPermission, setHasPermission] = React.useState(false);

    const devices = useCameraDevices();
    const device = devices.back;

    const frameProcessor = useFrameProcessor((frame) => {
        'worklet'
        const config = {};
        config.template = "{\"ImageParameter\":{\"BarcodeFormatIds\":[\"BF_QR_CODE\"],\"Description\":\"\",\"Name\":\"Settings\"},\"Version\":\"3.0\"}"; //scan qrcode only
        const results = decode(frame, config)

        if (results && results[0]) {
            //const qrPoint = [results[0].x4, results[0].y4]

            const { imageBase64 } = GetImageData(frame)
            const base64 = `data:image/jpeg;base64,${imageBase64}`
            
            setAnchor && REA.runOnJS(setAnchor)(base64)
        }

    }, [])

    React.useEffect(() => {
        (async () => {
            const status = await Camera.requestCameraPermission();
            setHasPermission(status === 'authorized');
        })();
    }, []);

    return device != null && hasPermission && (
        <Camera
            style={{...styles.camera, ...style}}
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