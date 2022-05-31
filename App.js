import React, {useEffect} from 'react';
import { Image, SafeAreaView, StyleSheet, Button } from 'react-native';
import ImCamera from './src/camera/ImCamera';

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
  const [frameData, setFrame] = React.useState(null);
  
  return (
    <SafeAreaView style={styles.container}>
      <ImCamera setAnchor={setFrame} active={!Boolean(frameData)} />
      <Image source={{ uri: frameData + "" }} style={styles.image} />
      <Button title='Reset' onPress={() => setFrame(null)}/>
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