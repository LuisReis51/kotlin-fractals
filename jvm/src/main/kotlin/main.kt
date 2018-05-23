package org.jetbrains.demo.kotlinfractals

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.content.OutgoingContent
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.html.respondHtml
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.cacheControl
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.experimental.io.ByteWriteChannel
import kotlinx.coroutines.experimental.io.jvm.javaio.toOutputStream
import kotlinx.html.body
import kotlinx.html.h1
import java.awt.Font
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO


fun main(args: Array<String>) {
  System.setProperty("java.awt.headless", "true")

  val server = embeddedServer(Netty, 8888, module = Application::main)
  server.start(wait = true)
}

fun Application.main() {
  install(DefaultHeaders)
  install(CallLogging)
  install(Routing) {
    get("/") {
      call.respondHtml {
        body {
          h1 { +"Kotlin Fractals" }
        }
      }
      call.respondText("Fractal rendering service!")
    }

    get("/mandelbrot") {

      println("Rendering image!")
      val jvm = !call.request.queryParameters["jvm"].equals("false", ignoreCase = true)

      val width = call.request.queryParameters["width"]?.toInt() ?: 600
      val height = call.request.queryParameters["height"]?.toInt() ?: 600

      val top = call.request.queryParameters["top"]?.toDouble() ?: MandelbrotRender.initialArea.top
      val right = call.request.queryParameters["right"]?.toDouble() ?: MandelbrotRender.initialArea.right
      val bottom = call.request.queryParameters["bottom"]?.toDouble() ?: MandelbrotRender.initialArea.bottom
      val left = call.request.queryParameters["left"]?.toDouble() ?: MandelbrotRender.initialArea.left

      val img = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
      MandelbrotRender(image = FractalGraphics(img), maxIterations = 5_000).apply {
        setArea(Rect(top = top, left = left, bottom = bottom, right = right))
        render()
      }

      if (jvm) {
        img.graphics {
          font = Font("Arial", Font.BOLD, 64)
          drawString("JVM", 460, 570)
        }
      }

      call.response.cacheControl(CacheControl.NoCache(CacheControl.Visibility.Public))
      call.respond(status = HttpStatusCode.OK, message = img.toMessage())
    }
  }
}

inline fun BufferedImage.graphics(paint : Graphics2D.() -> Unit) {
  val g = createGraphics()
  try {
    g.paint()
  } finally {
    g.dispose()
  }
}

class FractalGraphics(val g: BufferedImage) : FractalImage {
  override val pixelRect
    get() = Rect(0, 0, g.width, g.height)

  override fun putPixel(p: Pixel, c: Color) {
    g.setRGB(p.x, p.y, c.rgb)
  }
}


private fun BufferedImage.toMessage() = let { img ->
  object : OutgoingContent.WriteChannelContent() {
    override val contentType: ContentType?
      get() = ContentType.Image.PNG

    override suspend fun writeTo(channel: ByteWriteChannel) {
      channel.toOutputStream().use {
        ImageIO.write(img, "png", it)
      }
    }
  }
}


