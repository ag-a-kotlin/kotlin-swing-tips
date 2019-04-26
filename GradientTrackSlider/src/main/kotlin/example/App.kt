package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.awt.image.PixelGrabber
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.plaf.metal.MetalSliderUI

class MainPanel : JPanel(BorderLayout()) {
  init {
    UIManager.put("Slider.horizontalThumbIcon", object : Icon {
      override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        /* Empty icon */
      }

      override fun getIconWidth() = 15

      override fun getIconHeight() = 64
    })
    println(UIManager.get("Slider.trackWidth"))
    println(UIManager.get("Slider.majorTickLength"))
    println(UIManager.getInt("Slider.trackWidth"))
    println(UIManager.getInt("Slider.majorTickLength"))
    UIManager.put("Slider.trackWidth", 64)
    UIManager.put("Slider.majorTickLength", 6)

    val slider0 = makeSlider()
    val slider1 = makeSlider()
    slider1.setUI(GradientPalletSliderUI())
    slider1.setModel(slider0.getModel())

    val box = Box.createVerticalBox()
    box.add(makeTitledPanel("Default:", slider0))
    box.add(Box.createVerticalStrut(5))
    box.add(makeTitledPanel("Gradient translucent track JSlider:", slider1))
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
    add(box, BorderLayout.NORTH)
    setOpaque(false)
    setPreferredSize(Dimension(320, 240))
  }

  protected override fun paintComponent(g: Graphics) {
    val g2 = g.create() as Graphics2D
    g2.setPaint(TEXTURE)
    g2.fillRect(0, 0, getWidth(), getHeight())
    g2.dispose()
    super.paintComponent(g)
  }

  companion object {
    private val TEXTURE = TextureUtils.createCheckerTexture(6, Color(200, 150, 100, 50))

    private fun makeSlider(): JSlider {
      val slider = JSlider(SwingConstants.HORIZONTAL, 0, 100, 50)
      slider.setBackground(Color.GRAY)
      slider.setOpaque(false)
      val ma = object : MouseAdapter() {
        override fun mouseDragged(e: MouseEvent) {
          e.getComponent().repaint()
        }

        override fun mouseWheelMoved(e: MouseWheelEvent) {
          val source = e.getComponent() as JSlider
          val intValue = source.getValue().toInt() - e.getWheelRotation()
          val model = source.getModel()
          if (model.getMaximum() >= intValue && model.getMinimum() <= intValue) {
            source.setValue(intValue)
          }
        }
      }
      slider.addMouseMotionListener(ma)
      slider.addMouseWheelListener(ma)
      return slider
    }

    private fun makeTitledPanel(title: String, c: Component): Component {
      val p = JPanel(BorderLayout())
      p.setBorder(BorderFactory.createTitledBorder(title))
      p.setOpaque(false)
      p.add(c)
      return p
    }
  }
}

internal class GradientPalletSliderUI : MetalSliderUI() {
  protected var controlDarkShadow = Color(0x64_64_64) // MetalLookAndFeel.getControlDarkShadow()
  protected var controlHighlight = Color(0xC8_FF_C8) // MetalLookAndFeel.getControlHighlight()
  protected var controlShadow = Color(0x00_64_00) // MetalLookAndFeel.getControlShadow()

  override fun paintTrack(g: Graphics) {
    // val trackColor = if (!slider.isEnabled()) MetalLookAndFeel.getControlShadow() else slider.getForeground()
    // val leftToRight = MetalUtils.isLeftToRight(slider)

    g.translate(trackRect.x, trackRect.y)

    var trackLeft = 0
    var trackTop = 0
    var trackRight: Int // = 0
    var trackBottom: Int // = 0
    if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
      trackBottom = trackRect.height - 1 - getThumbOverhang()
      trackTop = trackBottom - getTrackWidth() + 1
      trackRight = trackRect.width - 1
    } else {
      // if (leftToRight) {
      trackLeft = trackRect.width - getThumbOverhang() - getTrackWidth()
      trackRight = trackRect.width - getThumbOverhang() - 1
      // } else {
      //   trackLeft = getThumbOverhang()
      //   trackRight = getThumbOverhang() + getTrackWidth() - 1
      // }
      trackBottom = trackRect.height - 1
    }

    // Draw the track
    paintTrackBase(g, trackTop, trackLeft, trackBottom, trackRight)

    // Draw the fill
    paintTrackFill(g, trackTop, trackLeft, trackBottom, trackRight)

    // Draw the highlight
    paintTrackHighlight(g, trackTop, trackLeft, trackBottom, trackRight)

    g.translate(-trackRect.x, -trackRect.y)
  }

  protected fun paintTrackBase(g: Graphics, trackTop: Int, trackLeft: Int, trackBottom: Int, trackRight: Int) {
    if (slider.isEnabled()) {
      g.setColor(controlDarkShadow)
      g.drawRect(trackLeft, trackTop, trackRight - trackLeft - 1, trackBottom - trackTop - 1)

      g.setColor(controlHighlight)
      g.drawLine(trackLeft + 1, trackBottom, trackRight, trackBottom)
      g.drawLine(trackRight, trackTop + 1, trackRight, trackBottom)

      g.setColor(controlShadow)
      g.drawLine(trackLeft + 1, trackTop + 1, trackRight - 2, trackTop + 1)
      g.drawLine(trackLeft + 1, trackTop + 1, trackLeft + 1, trackBottom - 2)
    } else {
      g.setColor(controlShadow)
      g.drawRect(trackLeft, trackTop, trackRight - trackLeft - 1, trackBottom - trackTop - 1)
    }
  }

  protected fun paintTrackFill(g: Graphics, trackTop: Int, trackLeft: Int, trackBottom: Int, trackRight: Int) {
    var middleOfThumb: Int // = 0
    var fillTop: Int // = 0
    var fillLeft: Int // = 0
    var fillBottom: Int // = 0
    var fillRight: Int // = 0

    if (slider.getOrientation() == SwingConstants.HORIZONTAL) {
      middleOfThumb = thumbRect.x + thumbRect.width / 2
      middleOfThumb -= trackRect.x // To compensate for the g.translate()
      fillTop = trackTop + 1
      fillBottom = trackBottom - 2
      fillLeft = trackLeft + 1
      fillRight = middleOfThumb - 2
    } else {
      middleOfThumb = thumbRect.y + thumbRect.height / 2
      middleOfThumb -= trackRect.y // To compensate for the g.translate()
      fillLeft = trackLeft
      fillRight = trackRight - 1
      fillTop = middleOfThumb
      fillBottom = trackBottom - 1
    }

    if (slider.isEnabled()) {
      val x = (fillRight - fillLeft) / (trackRight - trackLeft).toFloat()
      g.setColor(GradientPalletUtils.getColorFromPallet(GRADIENT_PALLET, x, 0x64 shl 24))
      g.fillRect(fillLeft + 1, fillTop + 1, fillRight - fillLeft, fillBottom - fillTop)
    } else {
      g.setColor(controlShadow)
      g.fillRect(fillLeft, fillTop, fillRight - fillLeft, trackBottom - trackTop)
    }
  }

  protected fun paintTrackHighlight(g: Graphics, trackTop: Int, trackLeft: Int, trackBottom: Int, trackRight: Int) {
    var yy = trackTop + (trackBottom - trackTop) / 2
    for (i in 10 downTo 0) {
      g.setColor(makeColor(i * .07f))
      g.drawLine(trackLeft + 2, yy, trackRight - trackLeft - 2, yy)
      yy--
    }
  }

  private fun makeColor(alpha: Float) = Color(1f, 1f, 1f, alpha)

  companion object {
    private val GRADIENT_PALLET = GradientPalletUtils.makeGradientPallet()
  }
}

internal object GradientPalletUtils {
  fun makeGradientPallet(): IntArray {
    val image = BufferedImage(100, 1, BufferedImage.TYPE_INT_RGB)
    val g2 = image.createGraphics()
    val start = Point2D.Float()
    val end = Point2D.Float(99f, 0f)
    val dist = floatArrayOf(.0f, .5f, 1f)
    val colors = arrayOf<Color>(Color.RED, Color.YELLOW, Color.GREEN)
    g2.setPaint(LinearGradientPaint(start, end, dist, colors))
    g2.fillRect(0, 0, 100, 1)
    g2.dispose()

    val width = image.getWidth(null)
    val pallet = IntArray(width)
    val pg = PixelGrabber(image, 0, 0, width, 1, pallet, 0, width)
    try {
      pg.grabPixels()
    } catch (ex: InterruptedException) {
      ex.printStackTrace()
      Toolkit.getDefaultToolkit().beep()
    }

    return pallet
  }

  fun getColorFromPallet(pallet: IntArray, x: Float, alpha: Int): Color {
    val i = (pallet.size * x).toInt()
    val max = pallet.size - 1
    val index = Math.min(Math.max(i, 0), max)
    val pix = pallet[index] and 0x00_FF_FF_FF
    // int alpha = 0x64 << 24;
    return Color(alpha or pix, true)
  }
} /* HideUtilityClassConstructor */

internal object TextureUtils {
  fun createCheckerTexture(cs: Int, color: Color): TexturePaint {
    val size = cs * cs
    val img = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
    val g2 = img.createGraphics()
    g2.setPaint(color)
    g2.fillRect(0, 0, size, size)
    var i = 0
    while (i * cs < size) {
      var j = 0
      while (j * cs < size) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cs, j * cs, cs, cs)
        }
        j++
      }
      i++
    }
    g2.dispose()
    return TexturePaint(img, Rectangle(size, size))
  }
} /* HideUtilityClassConstructor */

fun main() {
  EventQueue.invokeLater {
    runCatching {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    }.onFailure {
      it.printStackTrace()
      Toolkit.getDefaultToolkit().beep()
    }
    JFrame().apply {
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      getContentPane().add(MainPanel())
      pack()
      setLocationRelativeTo(null)
      setVisible(true)
    }
  }
}
