package org.jetbrains.demo.kotlinfractals

import kotlinext.js.require
import kotlin.math.absoluteValue

private val lib = require("color-convert")

actual fun Colors.hsl(h: Double,
                      s: Double,
                      l: Double): Color {
  val x = lib.hsl.rgb(h, s, l)
  fun N(i : Int)
          = (x[i] as Int).absoluteValue % 256
  return Color(N(0), N(1), N(2))
}

actual class Color(val r: Int, val g: Int, val b: Int)

actual val Colors.BLACK
  get() = Color(0, 0, 0)

val Colors.GRAY
  get() = Color(255, 255, 255)

