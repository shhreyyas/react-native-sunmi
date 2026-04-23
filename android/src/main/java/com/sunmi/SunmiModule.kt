package com.sunmi

import com.facebook.react.bridge.ReactApplicationContext

class SunmiModule(reactContext: ReactApplicationContext) :
  NativeSunmiSpec(reactContext) {

  override fun multiply(a: Double, b: Double): Double {
    return a * b
  }

  companion object {
    const val NAME = NativeSunmiSpec.NAME
  }
}
