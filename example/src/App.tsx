import { useState } from 'react';
import {
  Text,
  View,
  StyleSheet,
  TouchableOpacity,
  ScrollView,
  Alert,
} from 'react-native';
import { SunmiPrinter, type PrintLine } from 'react-native-sunmi';

export default function App() {
  const [status, setStatus] = useState<string>('Unknown');
  const [logs, setLogs] = useState<string[]>([]);

  const log = (msg: string) => {
    setLogs((prev) => [`[${new Date().toLocaleTimeString()}] ${msg}`, ...prev]);
  };

  const checkAvailability = async () => {
    const available = await SunmiPrinter.isPrinterAvailable();
    setStatus(available ? 'Connected' : 'Not Available');
    log(`Printer available: ${available}`);
  };

  const checkStatus = async () => {
    try {
      const code = await SunmiPrinter.getPrinterStatus();
      const labels: Record<number, string> = {
        1: 'Normal',
        2: 'Preparing',
        3: 'Comm Error',
        4: 'Out of Paper',
        5: 'Overheated',
        505: 'No Printer',
        507: 'Firmware Update',
      };
      const label = labels[code] ?? `Unknown (${code})`;
      log(`Printer status: ${label}`);
    } catch (e: any) {
      log(`Status error: ${e.message}`);
    }
  };

  const printText = async () => {
    try {
      await SunmiPrinter.printText(
        'Hello from react-native-sunmi!\n' +
          'This is a test print to verify that the Sunmi thermal printer handles multiple lines of text correctly.\n' +
          'Line 3: The quick brown fox jumps over the lazy dog near the riverbank on a warm sunny afternoon'
      );
      log('Text printed');
    } catch (e: any) {
      log(`Print error: ${e.message}`);
    }
  };

  const printQR = async () => {
    try {
      await SunmiPrinter.printQRCode(
        'https://github.com/shhreyyas/react-native-sunmi'
      );
      log('QR code printed');
    } catch (e: any) {
      log(`QR error: ${e.message}`);
    }
  };

  const printBarcode = async () => {
    try {
      await SunmiPrinter.printBarcode('1234567890');
      log('Barcode printed');
    } catch (e: any) {
      log(`Barcode error: ${e.message}`);
    }
  };

  const printReceipt = async () => {
    const lines: PrintLine[] = [
      { text: 'SAMPLE RECEIPT', align: 'center', bold: true, fontSize: 28 },
      { text: '================================', align: 'center' },
      { text: 'Item 1                    $10.00', align: 'left' },
      { text: 'Item 2                    $25.50', align: 'left' },
      { text: '--------------------------------', align: 'center' },
      { text: 'Total                     $35.50', align: 'left', bold: true },
      { text: '', align: 'left' },
      { text: 'Thank you!', align: 'center' },
    ];
    try {
      await SunmiPrinter.printFormattedReceipt(lines);
      log('Receipt printed');
    } catch (e: any) {
      log(`Receipt error: ${e.message}`);
    }
  };

  const showDebugInfo = async () => {
    try {
      const info = await SunmiPrinter.getPrinterDebugInfo();
      Alert.alert('Debug Info', JSON.stringify(info, null, 2));
      log('Debug info retrieved');
    } catch (e: any) {
      log(`Debug error: ${e.message}`);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Sunmi Printer Test</Text>
      <Text style={styles.status}>Status: {status}</Text>

      <View style={styles.buttons}>
        <TouchableOpacity style={styles.btn} onPress={checkAvailability}>
          <Text style={styles.btnText}>Check Availability</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.btn} onPress={checkStatus}>
          <Text style={styles.btnText}>Printer Status</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.btn} onPress={printText}>
          <Text style={styles.btnText}>Print Text</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.btn} onPress={printQR}>
          <Text style={styles.btnText}>Print QR Code</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.btn} onPress={printBarcode}>
          <Text style={styles.btnText}>Print Barcode</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.btn} onPress={printReceipt}>
          <Text style={styles.btnText}>Print Receipt</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.btn} onPress={showDebugInfo}>
          <Text style={styles.btnText}>Debug Info</Text>
        </TouchableOpacity>
      </View>

      <Text style={styles.logTitle}>Logs:</Text>
      <ScrollView style={styles.logContainer}>
        {logs.map((entry, i) => (
          <Text key={i} style={styles.logEntry}>
            {entry}
          </Text>
        ))}
      </ScrollView>
    </View>
  );
}

const styleSheetCreate = ((StyleSheet as any).create ??
  (StyleSheet as any).default?.create) as <T>(obj: T) => T;

const styles = styleSheetCreate({
  container: {
    flex: 1,
    padding: 20,
    paddingTop: 60,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    textAlign: 'center',
    marginBottom: 8,
  },
  status: {
    fontSize: 16,
    textAlign: 'center',
    marginBottom: 16,
    color: '#666',
  },
  buttons: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    gap: 8,
    marginBottom: 16,
  },
  btn: {
    backgroundColor: '#007AFF',
    paddingHorizontal: 14,
    paddingVertical: 10,
    borderRadius: 8,
  },
  btnText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '600',
  },
  logTitle: {
    fontSize: 16,
    fontWeight: '600',
    marginBottom: 8,
  },
  logContainer: {
    flex: 1,
    backgroundColor: '#1e1e1e',
    borderRadius: 8,
    padding: 12,
  },
  logEntry: {
    color: '#0f0',
    fontSize: 12,
    fontFamily: 'monospace',
    marginBottom: 4,
  },
} as const);
