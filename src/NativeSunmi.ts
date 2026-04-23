import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface Spec extends TurboModule {
  isPrinterAvailable(): Promise<boolean>;
  connectPrinter(): Promise<boolean>;
  printText(text: string): Promise<boolean>;
  printQRCode(data: string): Promise<boolean>;
  printBarcode(data: string): Promise<boolean>;
  printFormattedReceipt(lines: Object[]): Promise<boolean>;
  getPrinterStatus(): Promise<number>;
  getPrinterDebugInfo(): Promise<Object>;
}

export default TurboModuleRegistry.getEnforcing<Spec>('Sunmi');
