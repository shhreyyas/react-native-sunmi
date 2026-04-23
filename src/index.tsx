import NativeSunmi from './NativeSunmi';

export type PrintLine = {
  text: string;
  align?: 'left' | 'center' | 'right';
  bold?: boolean;
  fontSize?: number;
};

export type PrinterDebugInfo = {
  serviceBound: boolean;
  initialized: boolean;
  printerState?: number;
  serialNo?: string;
  firmwareVersion?: string;
  serviceVersion?: string;
  printerStateError?: string;
};

export const SunmiPrinter = {
  async isPrinterAvailable(): Promise<boolean> {
    try {
      return await NativeSunmi.isPrinterAvailable();
    } catch {
      return false;
    }
  },

  async connectPrinter(): Promise<boolean> {
    return await NativeSunmi.connectPrinter();
  },

  async printText(text: string): Promise<boolean> {
    return await NativeSunmi.printText(text);
  },

  async printQRCode(data: string): Promise<boolean> {
    return await NativeSunmi.printQRCode(data);
  },

  async printBarcode(data: string): Promise<boolean> {
    return await NativeSunmi.printBarcode(data);
  },

  async printFormattedReceipt(lines: PrintLine[]): Promise<boolean> {
    return await NativeSunmi.printFormattedReceipt(lines);
  },

  async getPrinterStatus(): Promise<number> {
    return await NativeSunmi.getPrinterStatus();
  },

  async getPrinterDebugInfo(): Promise<PrinterDebugInfo> {
    return (await NativeSunmi.getPrinterDebugInfo()) as PrinterDebugInfo;
  },
};
