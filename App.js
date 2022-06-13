import React, { useEffect } from 'react';
import { Image, SafeAreaView, StyleSheet, Button, NativeModules } from 'react-native';
import ImCamera from './src/camera/ImCamera';
import ReadOptic from './src/readOptic/readOptic';
import { FmtManager } from './src/fmt/fmtManager'
import * as REA from 'react-native-reanimated'

export default function App() {
  const [frameData, setFrame] = React.useState(null);
  const [result, setResult] = React.useState(null)

  const asd = async () => {
    try {
      const config = {
        fmt: frameData.fmt,
        point: frameData.qrPoint
      }
      const data = await ReadOptic.runReader(frameData.frame, config);
      console.log(typeof data)
      REA.runOnJS(setResult)(data)
    } catch (e) {
      console.error(e);
    }
  }

  useEffect(() => {
    if (frameData) {
      asd()
    }
  }, [frameData])

  return (
    <SafeAreaView style={styles.container}>
      <ImCamera setAnchor={setFrame} active={!Boolean(frameData)} />
      <Image source={{ uri: `data:image/jpeg;base64,${result}` }} style={styles.image} />
      <Button title='Reset' onPress={() => setFrame(null)} />
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
  image: {
    width: 345,
    height: 180,
    resizeMode: "contain"
  }
});