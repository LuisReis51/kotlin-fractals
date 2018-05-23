package org.jetbrains.demo.kotlinfractals

import Underscore
import kotlinx.html.js.onMouseMoveFunction
import org.w3c.dom.events.Event
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.setState
import styled.css
import styled.styledCanvas
import kotlin.browser.window

data class ScreenInfo(val width : Int, val height: Int)
data class PixelInfo(val x : Int, val y: Int)

interface AutoResizeCanvasControlProps : RProps {
  var renderImage : (JSFractalImage.(ScreenInfo) -> Unit)?

  var onMouseMove : ((PixelInfo, ScreenInfo) -> Unit)?
  var onMouseDown: ((PixelInfo, ScreenInfo) -> Unit)?
  var onMouseUp: ((PixelInfo, ScreenInfo) -> Unit)?
}

class AutoResizeCanvasControl : RComponent<AutoResizeCanvasControlProps, AutoResizeCanvasControl.CanvasState>() {
  init {
    println("Init called")
    state.updateSizeImpl()
  }

  interface CanvasState : RState {
    var height: Int
    var width: Int
  }

  override fun RBuilder.render() {
    styledCanvas {
      //cache it just in case
      val screenInfo = ScreenInfo(state.width, state.height)

      props.renderImage?.let { builder ->
        ref {
          if (it != null) {
            println("ref called")
            builder(fractalImageFromCanvas(it), screenInfo)
          }
        }
      }

      css { +Styles.canvas }

      attrs {
        width = screenInfo.width.toString()
        height = screenInfo.height.toString()

        props.onMouseMove?.let {
          onMouseMoveFunction = { e : dynamic ->
            it(PixelInfo(e.nativeEvent.offsetX, e.nativeEvent.offsetY), screenInfo)
          }
        }
      }
    }
  }

  private val onResize = Underscore.debounce(100) { _: Event ->
    setState {
      updateSizeImpl()
    }
  }

  private fun CanvasState.updateSizeImpl() {
    width = window.innerWidth - Styles.canvasBorder * 2
    height = window.innerHeight - Styles.canvasBorder * 2 - Styles.canvasOffsetBottom - Styles.canvasOffsetBottom
  }

  override fun componentDidMount() {
    println("mount")
    window.addEventListener("resize", onResize)
  }

  override fun componentWillUnmount() {
    window.removeEventListener("resize", onResize)
  }
}
