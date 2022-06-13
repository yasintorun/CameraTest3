import { NativeModules } from 'react-native';
const { ReadOptic } = NativeModules;

interface ReadOpticInterface {
    //Fmt ve qr bilgisini set eder
    // setConfig(config: Object): void;

    //optik okumayı başlatır.
    runReader(base64: string, config: Object): Promise<any>;

    multiply(a:number, b:number, callback: (a:number,b:number) => {}): Promise<number>;

    //okuduğu değeri base64 formatında geri döndürür
    // getResult(): string;
}

export default ReadOptic as ReadOpticInterface;