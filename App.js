import * as React from 'react';
import { SafeAreaView, StyleSheet, Text, Image } from 'react-native';
import { Camera, useCameraDevices, useFrameProcessor } from 'react-native-vision-camera';
import { DBRConfig, decode, TextResult } from 'vision-camera-dynamsoft-barcode-reader';
import * as REA from 'react-native-reanimated';
import { GetImageData } from './src/plugins/GetImageData'
import { base64ToArrayBuffer } from './src/utils/Converters';
// import cv from './opencv'

export default function App() {
  const [hasPermission, setHasPermission] = React.useState(false);
  const [frameData, setFrame] = React.useState(null);
  const devices = useCameraDevices();
  const device = devices.back;
  let t = null;
  const frameProcessor = useFrameProcessor((frame) => {
    'worklet'
    const config = {};
    config.template = "{\"ImageParameter\":{\"BarcodeFormatIds\":[\"BF_QR_CODE\"],\"Description\":\"\",\"Name\":\"Settings\"},\"Version\":\"3.0\"}"; //scan qrcode only
    const results = decode(frame, config)

    if (results && results[0] && !frameData) {
      const base64 = GetImageData(frame).base64
      const frameData = { ...results[0], width: frame.width, height: frame.height, imageData: base64 }
      REA.runOnJS(setFrame)(frameData);
      // setFrame(frameData)
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
            {frameData ? (
                <Image source={{ uri: `data:image/jpeg;base64,${frameData.imageData}`}} style={{ width: 200, height: 200 }} />
              )
              : (

                <Camera
                  style={StyleSheet.absoluteFill}
                  device={device}
                  isActive={true}
                  frameProcessor={frameProcessor}
                  frameProcessorFps={1}
                />
              )}
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
});

// /**
//  * Sample React Native App
//  * https://github.com/facebook/react-native
//  *
//  * @format
//  * @flow strict-local
//  */

// import React from 'react';
// import type {Node} from 'react';
// import {
//   SafeAreaView,
//   ScrollView,
//   StatusBar,
//   StyleSheet,
//   Text,
//   useColorScheme,
//   View,
// } from 'react-native';

// import {
//   Colors,
//   DebugInstructions,
//   Header,
//   LearnMoreLinks,
//   ReloadInstructions,
// } from 'react-native/Libraries/NewAppScreen';

// const Section = ({children, title}): Node => {
//   const isDarkMode = useColorScheme() === 'dark';
//   return (
//     <View style={styles.sectionContainer}>
//       <Text
//         style={[
//           styles.sectionTitle,
//           {
//             color: isDarkMode ? Colors.white : Colors.black,
//           },
//         ]}>
//         {title}
//       </Text>
//       <Text
//         style={[
//           styles.sectionDescription,
//           {
//             color: isDarkMode ? Colors.light : Colors.dark,
//           },
//         ]}>
//         {children}
//       </Text>
//     </View>
//   );
// };

// const App: () => Node = () => {
//   const isDarkMode = useColorScheme() === 'dark';

//   const backgroundStyle = {
//     backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
//   };

//   return (
//     <SafeAreaView style={backgroundStyle}>
//       <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
//       <ScrollView
//         contentInsetAdjustmentBehavior="automatic"
//         style={backgroundStyle}>
//         <Header />
//         <View
//           style={{
//             backgroundColor: isDarkMode ? Colors.black : Colors.white,
//           }}>
//           <Section title="Step One">
//             Edit <Text style={styles.highlight}>App.js</Text> to change this
//             screen and then come back to see your edits.
//           </Section>
//           <Section title="See Your Changes">
//             <ReloadInstructions />
//           </Section>
//           <Section title="Debug">
//             <DebugInstructions />
//           </Section>
//           <Section title="Learn More">
//             Read the docs to discover what to do next:
//           </Section>
//           <LearnMoreLinks />
//         </View>
//       </ScrollView>
//     </SafeAreaView>
//   );
// };

// const styles = StyleSheet.create({
//   sectionContainer: {
//     marginTop: 32,
//     paddingHorizontal: 24,
//   },
//   sectionTitle: {
//     fontSize: 24,
//     fontWeight: '600',
//   },
//   sectionDescription: {
//     marginTop: 8,
//     fontSize: 18,
//     fontWeight: '400',
//   },
//   highlight: {
//     fontWeight: '700',
//   },
// });

// export default App;