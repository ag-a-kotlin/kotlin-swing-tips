package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Ellipse2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import javax.swing.* // ktlint-disable no-wildcard-imports

class MainPanel : JPanel(BorderLayout()) {
  init {

    val button1 = RadialGradientButton("JButton JButton JButton JButton")
    button1.setForeground(Color.WHITE)

    val button2 = RadialGradientPaintButton("JButton JButton JButton JButton")
    button2.setForeground(Color.WHITE)

    val p = object : JPanel(FlowLayout(FlowLayout.CENTER, 20, 50)) {
      private val texture = TextureUtils.createCheckerTexture(16, Color(-0x11cdcdce, true))
      override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setPaint(texture)
        g2.fillRect(0, 0, getWidth(), getHeight())
        g2.dispose()
        super.paintComponent(g)
      }
    }
    p.setOpaque(false)
    p.add(button1)
    p.add(button2)

    add(p)
    setPreferredSize(Dimension(320, 240))
  }
}

internal class RadialGradientButton(title: String) : JButton(title) {
  private val timer1 = Timer(10, null)
  private val timer2 = Timer(10, null)
  private val pt = Point()
  private var radius = 0f
  protected var shape: Shape? = null
  protected var base: Rectangle? = null

  init {
    timer1.addActionListener {
      radius = Math.min(200f, radius + DELTA)
      repaint()
    }
    timer2.addActionListener {
      radius = Math.max(0f, radius - DELTA)
      repaint()
    }
    val listener = object : MouseAdapter() {
      override fun mouseEntered(e: MouseEvent?) {
        timer2.stop()
        if (!timer1.isRunning()) {
          timer1.start()
        }
      }

      override fun mouseExited(e: MouseEvent?) {
        timer1.stop()
        if (!timer2.isRunning()) {
          timer2.start()
        }
      }

      override fun mouseMoved(e: MouseEvent) {
        pt.setLocation(e.getPoint())
        repaint()
      }

      override fun mouseDragged(e: MouseEvent) {
        pt.setLocation(e.getPoint())
        repaint()
      }
    }
    addMouseListener(listener)
    addMouseMotionListener(listener)
  }

  override fun updateUI() {
    super.updateUI()
    setOpaque(false)
    setContentAreaFilled(false)
    setFocusPainted(false)
    setBackground(Color(0xF7_23_59))
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
    update()
  }

  protected fun update() {
    if (getBounds() != base) {
      base = getBounds()
      shape = RoundRectangle2D.Float(0f, 0f, getWidth() - 1f, getHeight() - 1f, ARC_WIDTH, ARC_HEIGHT)
    }
  }

  override fun contains(x: Int, y: Int): Boolean {
    update()
    // return Optional.ofNullable(shape).map { s -> s.contains(x.toFloat(), y.toDouble()) }.orElse(false)
    return shape?.let { it.contains(x.toDouble(), y.toDouble()) } ?: false
  }

  // @Override protected void paintBorder(Graphics g) {
  //   update();
  //   Graphics2D g2 = (Graphics2D) g.create();
  //   g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
  //   // g2.setStroke(new BasicStroke(2.5f));
  //   if (getModel().isArmed()) {
  //     g2.setPaint(new Color(0x64_44_05_F7, true));
  //   } else {
  //     g2.setPaint(new Color(0xF7_23_59).darker());
  //   }
  //   g2.draw(shape);
  //   g2.dispose();
  // }

  override fun paintComponent(g: Graphics) {
    update()

    val g2 = g.create() as Graphics2D
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

    // Stunning hover effects with CSS variables ? Prototypr
    // https://blog.prototypr.io/stunning-hover-effects-with-css-variables-f855e7b95330
    val c1 = Color(0x00_F7_23_59, true)
    val c2 = Color(0x64_44_05_F7, true)

    // g2.setComposite(AlphaComposite.Clear);
    // g2.setPaint(new Color(0x0, true));
    // g2.fillRect(0, 0, getWidth(), getHeight());

    g2.setComposite(AlphaComposite.Src)
    g2.setPaint(Color(if (getModel().isArmed()) 0xFF_AA_AA else 0xF7_23_59))
    g2.fill(shape)

    if (radius > 0) {
      val r2 = radius + radius
      val dist = floatArrayOf(0f, 1f)
      val colors = arrayOf<Color>(c2, c1)
      g2.setPaint(RadialGradientPaint(pt, r2.toFloat(), dist, colors))
      val oval = Ellipse2D.Float(pt.x - radius, pt.y - radius, r2, r2)
      g2.setComposite(AlphaComposite.SrcAtop)
      g2.setClip(shape)
      g2.fill(oval)
    }
    g2.dispose()

    super.paintComponent(g)
  }

  companion object {
    private const val DELTA = 10f
    private const val ARC_WIDTH = 32f
    private const val ARC_HEIGHT = 32f
  }
}

internal class RadialGradientPaintButton(title: String) : JButton(title) {
  private val timer1 = Timer(10, null)
  private val timer2 = Timer(10, null)
  private val pt = Point()
  private var radius = 0f
  protected var shape: Shape? = null
  protected var base: Rectangle? = null
  @Transient
  private var buf: BufferedImage? = null

  init {
    timer1.addActionListener {
      radius = Math.min(200f, radius + DELTA)
      repaint()
    }
    timer2.addActionListener {
      radius = Math.max(0f, radius - DELTA)
      repaint()
    }
    val listener = object : MouseAdapter() {
      override fun mouseEntered(e: MouseEvent?) {
        timer2.stop()
        if (!timer1.isRunning()) {
          timer1.start()
        }
      }

      override fun mouseExited(e: MouseEvent?) {
        timer1.stop()
        if (!timer2.isRunning()) {
          timer2.start()
        }
      }

      override fun mouseMoved(e: MouseEvent) {
        pt.setLocation(e.getPoint())
        repaint()
      }

      override fun mouseDragged(e: MouseEvent) {
        pt.setLocation(e.getPoint())
        repaint()
      }
    }
    addMouseListener(listener)
    addMouseMotionListener(listener)
  }

  override fun updateUI() {
    super.updateUI()
    setOpaque(false)
    setContentAreaFilled(false)
    setFocusPainted(false)
    setBackground(Color(0xF7_23_59))
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10))
    update()
  }

  protected fun update() {
    if (getBounds() != base) {
      base = getBounds()
      val w = getWidth()
      val h = getHeight()
      if (w > 0 && h > 0) {
        buf = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
      }
      shape = RoundRectangle2D.Float(0f, 0f, w - 1f, h - 1f, ARC_WIDTH, ARC_HEIGHT)
    }
    // if (buf == null) {
    //   return
    // }
    val g2 = buf?.createGraphics() ?: return
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    val c1 = Color(0x00_F7_23_59, true)
    val c2 = Color(0x64_44_05_F7, true)

    g2.setComposite(AlphaComposite.Clear)
    g2.fillRect(0, 0, getWidth(), getHeight())

    g2.setComposite(AlphaComposite.Src)
    g2.setPaint(Color(if (getModel().isArmed()) 0xFF_AA_AA else 0xF7_23_59))
    g2.fill(shape)

    if (radius > 0) {
      val r2 = radius + radius
      val dist = floatArrayOf(0f, 1f)
      val colors = arrayOf<Color>(c2, c1)
      g2.setPaint(RadialGradientPaint(pt, r2.toFloat(), dist, colors))
      val oval = Ellipse2D.Float(pt.x - radius, pt.y - radius, r2, r2)
      g2.setComposite(AlphaComposite.SrcAtop)
      // g2.setClip(shape)
      g2.fill(oval)
    }
    g2.dispose()
  }

  override fun contains(x: Int, y: Int): Boolean {
    update()
    // return Optional.ofNullable(shape).map { s -> s.contains(x.toDouble(), y.toDouble()) }.orElse(false)
    return shape?.let { it.contains(x.toDouble(), y.toDouble()) } ?: false
  }

  override fun paintComponent(g: Graphics) {
    update()
    g.drawImage(buf, 0, 0, this)
    super.paintComponent(g)
  }

  companion object {
    private const val DELTA = 10f
    private const val ARC_WIDTH = 32f
    private const val ARC_HEIGHT = 32f
  }
}

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
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    } catch (ex: ClassNotFoundException) {
      ex.printStackTrace()
    } catch (ex: InstantiationException) {
      ex.printStackTrace()
    } catch (ex: IllegalAccessException) {
      ex.printStackTrace()
    } catch (ex: UnsupportedLookAndFeelException) {
      ex.printStackTrace()
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
