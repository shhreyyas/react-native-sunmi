package com.sunmi

import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.sunmi.printerx.PrinterSdk
import com.sunmi.printerx.api.LineApi
import com.sunmi.printerx.enums.Align
import com.sunmi.printerx.enums.PrinterInfo
import com.sunmi.printerx.enums.Status
import com.sunmi.printerx.enums.Symbology
import com.sunmi.printerx.style.BarcodeStyle
import com.sunmi.printerx.style.BaseStyle
import com.sunmi.printerx.style.QrStyle
import java.io.ByteArrayOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SunmiModule(reactContext: ReactApplicationContext) :
  NativeSunmiSpec(reactContext) {

  companion object {
    private const val TAG = "SunmiModule"
    const val NAME = NativeSunmiSpec.NAME
  }

  private val printerSdk = PrinterSdk.getInstance()
  private var printer: PrinterSdk.Printer? = null
  private var isPrinterInitialized = false
  private var isPrinterConnecting = false
  private var isPrinting = false

  init {
    connectPrinterInternal()
  }

  private fun connectPrinterInternal() {
    if (printer != null || isPrinterConnecting) return
    isPrinterConnecting = true
    try {
      printerSdk.getPrinter(reactApplicationContext, object : PrinterSdk.PrinterListen {
        override fun onDefPrinter(defPrinter: PrinterSdk.Printer?) {
          printer = defPrinter
          isPrinterInitialized = defPrinter != null
          isPrinterConnecting = false
          Log.i(TAG, "PrinterX default printer connected")
        }

        override fun onPrinters(printers: MutableList<PrinterSdk.Printer>?) {
          if (printer == null && !printers.isNullOrEmpty()) {
            printer = printers[0]
            isPrinterInitialized = true
          }
          isPrinterConnecting = false
        }
      })
    } catch (e: Exception) {
      isPrinterConnecting = false
      Log.e(TAG, "Failed to connect PrinterX service", e)
    }
  }

  private fun ensurePrinterReadySync(): Boolean {
    if (printer != null) return true
    val latch = CountDownLatch(1)
    try {
      printerSdk.getPrinter(reactApplicationContext, object : PrinterSdk.PrinterListen {
        override fun onDefPrinter(defPrinter: PrinterSdk.Printer?) {
          printer = defPrinter
          isPrinterInitialized = defPrinter != null
          latch.countDown()
        }

        override fun onPrinters(printers: MutableList<PrinterSdk.Printer>?) {
          if (printer == null && !printers.isNullOrEmpty()) {
            printer = printers[0]
            isPrinterInitialized = true
          }
          latch.countDown()
        }
      })
      latch.await(1200, TimeUnit.MILLISECONDS)
    } catch (e: Exception) {
      Log.e(TAG, "ensurePrinterReadySync failed", e)
    }
    return printer != null
  }

  private fun lineApiOrThrow(): LineApi {
    return printer?.lineApi() ?: throw IllegalStateException("Printer is not connected")
  }

  // ESC/POS helpers
  private fun escPosInit(): ByteArray = byteArrayOf(0x1B, 0x40)
  private fun escPosAlign(align: String): ByteArray = byteArrayOf(0x1B, 0x61, when (align) {
    "center" -> 0x01
    "right" -> 0x02
    else -> 0x00
  })
  private fun escPosBold(on: Boolean): ByteArray = byteArrayOf(0x1B, 0x45, if (on) 0x01 else 0x00)
  private fun escPosSize(fontSize: Int): ByteArray {
    val n: Byte = if (fontSize >= 28) 0x11 else 0x00
    return byteArrayOf(0x1D, 0x21, n)
  }
  private fun escPosFeed(lines: Int): ByteArray = byteArrayOf(0x1B, 0x64, lines.toByte())

  override fun connectPrinter(promise: Promise) {
    try {
      connectPrinterInternal()
      promise.resolve(ensurePrinterReadySync())
    } catch (e: Exception) {
      promise.reject("CONNECT_ERROR", e.message, e)
    }
  }

  override fun isPrinterAvailable(promise: Promise) {
    if (printer == null) connectPrinterInternal()
    promise.resolve(ensurePrinterReadySync())
  }

  override fun printText(text: String, promise: Promise) {
    try {
      if (!ensurePrinterReadySync()) {
        promise.reject("PRINTER_NULL", "Printer service is not connected")
        return
      }
      val cmdApi = printer?.commandApi() ?: throw IllegalStateException("Printer is not connected")

      cmdApi.sendEscCommand(escPosInit())
      cmdApi.sendEscCommand(escPosAlign("left"))

      val lines = text.split("\n")
      for (line in lines) {
        cmdApi.sendEscCommand("$line\n".toByteArray(Charsets.UTF_8))
      }

      cmdApi.sendEscCommand(escPosFeed(4))
      promise.resolve(true)
    } catch (e: Exception) {
      promise.reject("PRINT_ERROR", e.message, e)
    }
  }

  override fun printQRCode(data: String, promise: Promise) {
    try {
      if (!ensurePrinterReadySync()) {
        promise.reject("PRINTER_NULL", "Printer service is not connected")
        return
      }
      val lineApi = lineApiOrThrow()
      lineApi.initLine(BaseStyle.getStyle())
      lineApi.printQrCode(data, QrStyle.getStyle().setDot(8).setAlign(Align.CENTER))
      lineApi.autoOut()
      promise.resolve(true)
    } catch (e: Exception) {
      promise.reject("PRINT_ERROR", e.message, e)
    }
  }

  override fun printBarcode(data: String, promise: Promise) {
    try {
      if (!ensurePrinterReadySync()) {
        promise.reject("PRINTER_NULL", "Printer service is not connected")
        return
      }
      val lineApi = lineApiOrThrow()
      lineApi.initLine(BaseStyle.getStyle())
      lineApi.printBarCode(data, BarcodeStyle.getStyle().setSymbology(Symbology.CODE128).setAlign(Align.CENTER))
      lineApi.autoOut()
      promise.resolve(true)
    } catch (e: Exception) {
      promise.reject("PRINT_ERROR", e.message, e)
    }
  }

  override fun printFormattedReceipt(lines: ReadableArray, promise: Promise) {
    try {
      if (!ensurePrinterReadySync()) {
        promise.reject("PRINTER_NULL", "Printer service is not connected.")
        return
      }
      if (lines.size() == 0) {
        promise.reject("PRINT_ERROR", "No lines to print.")
        return
      }
      if (isPrinting) {
        promise.reject("PRINT_BUSY", "A print job is already in progress.")
        return
      }
      isPrinting = true
      Log.i(TAG, "printFormattedReceipt: START — ${lines.size()} lines (CommandApi ESC/POS)")

      val cmdApi = printer?.commandApi() ?: throw IllegalStateException("Printer is not connected")

      cmdApi.sendEscCommand(escPosInit())

      for (i in 0 until lines.size()) {
        val line = lines.getMap(i) ?: continue
        val text = if (line.hasKey("text")) line.getString("text") ?: "" else ""
        val align = if (line.hasKey("align")) line.getString("align") ?: "left" else "left"
        val bold = line.hasKey("bold") && line.getBoolean("bold")
        val fontSize = if (line.hasKey("fontSize")) line.getInt("fontSize") else 24

        val buf = ByteArrayOutputStream()
        buf.write(escPosAlign(align))
        buf.write(escPosSize(fontSize))
        if (bold) buf.write(escPosBold(true))

        if (text.isEmpty()) {
          buf.write("\n".toByteArray(Charsets.UTF_8))
        } else {
          buf.write("$text\n".toByteArray(Charsets.UTF_8))
        }

        if (bold) buf.write(escPosBold(false))
        if (fontSize >= 28) buf.write(escPosSize(24))

        cmdApi.sendEscCommand(buf.toByteArray())
      }

      cmdApi.sendEscCommand(escPosFeed(4))

      Log.i(TAG, "printFormattedReceipt: DONE — all ${lines.size()} lines sent (CommandApi)")
      isPrinting = false
      promise.resolve(true)
    } catch (e: Exception) {
      isPrinting = false
      Log.e(TAG, "printFormattedReceipt FAILED", e)
      promise.reject("PRINT_ERROR", e.message, e)
    }
  }

  override fun getPrinterStatus(promise: Promise) {
    try {
      if (!ensurePrinterReadySync()) {
        promise.reject("PRINTER_NULL", "Printer service is not connected")
        return
      }
      val status: Status? = printer?.queryApi()?.getStatus()
      promise.resolve((status?.code ?: -1).toDouble())
    } catch (e: Exception) {
      promise.reject("STATUS_ERROR", e.message, e)
    }
  }

  override fun getPrinterDebugInfo(promise: Promise) {
    val map = Arguments.createMap()
    map.putBoolean("serviceBound", printer != null)
    map.putBoolean("initialized", isPrinterInitialized)
    try {
      val query = printer?.queryApi()
      if (query != null) {
        map.putInt("printerState", query.getStatus()?.code ?: -1)
        map.putString("serialNo", query.getInfo(PrinterInfo.ID) ?: "")
        map.putString("firmwareVersion", query.getInfo(PrinterInfo.VERSION) ?: "")
        map.putString("serviceVersion", query.getInfo(PrinterInfo.NAME) ?: "")
      }
    } catch (e: Exception) {
      map.putString("printerStateError", e.message ?: "unknown")
    }
    promise.resolve(map)
  }

  override fun onCatalystInstanceDestroy() {
    super.onCatalystInstanceDestroy()
    try {
      printerSdk.destroy()
    } catch (_: Exception) {
    }
    printer = null
    isPrinterInitialized = false
    isPrinterConnecting = false
  }
}
