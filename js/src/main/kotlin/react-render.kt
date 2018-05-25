package org.jetbrains.demo.kotlinfractals

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.cancelAndJoin
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.yield

object ReactRenderer {
  suspend fun CoroutineScope.renderJS(image: JSFractalImage, area: Rect<Double>) {
    MandelbrotRender.justRenderTasks(
            chunk = 2_000,
            maxIterations = 200,
            isActive = {isActive},
            image = image,
            area = area).forEach {
      it()
      yield()
    }

    yield()
    image.commit()
  }

  suspend fun CoroutineScope.renderJVM(image: JSFractalImage, area: Rect<Double>) {
    BackendRender.apply {
      val htmlImage = renderOnTheServer(image.screenInfo, area)

      println("Image loaded active = $isActive")

      delay(2_500)

      println("Image is about to deliver active = $isActive")

      image.loadFromImage(htmlImage)
    }
  }

  suspend fun CoroutineScope.renderMixed(image: JSFractalImage, area: Rect<Double>) {

    val jvm = async(coroutineContext) {
      renderJVM(image, area)
    }

    val js = async(coroutineContext) {
      renderJS(image, area)
    }

    jvm.await()

    ///stop render in JS
    // when JVM is done
    js.cancelAndJoin()
  }
}

val JSFractalImage.screenInfo get() = ScreenInfo(width, height)
