package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.color.ColorSpace
import java.awt.event.MouseEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.plaf.LayerUI

fun makeUI(): Component {
  val split = JSplitPane()
  split.isContinuousLayout = true
  split.resizeWeight = .5
  split.dividerSize = 0

  val cl = Thread.currentThread().contextClassLoader
  val icon = ImageIcon(cl.getResource("example/test.png"))

  val source = BufferedImage(icon.iconWidth, icon.iconHeight, BufferedImage.TYPE_INT_ARGB)
  val g = source.createGraphics()
  g.drawImage(icon.image, 0, 0, null)
  g.dispose()
  val colorConvert = ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null)
  val destination = colorConvert.filter(source, null)

  val beforeCanvas = object : JComponent() {
    override fun paintComponent(g: Graphics) {
      super.paintComponent(g)
      val iw = icon.iconWidth
      val ih = icon.iconHeight
      val dim = split.size
      val x = (dim.width - iw) / 2
      val y = (dim.height - ih) / 2
      g.drawImage(icon.image, x, y, iw, ih, this)
    }
  }
  split.leftComponent = beforeCanvas

  val afterCanvas = object : JComponent() {
    override fun paintComponent(g: Graphics) {
      super.paintComponent(g)
      val g2 = g.create() as? Graphics2D ?: return
      val iw = destination.getWidth(this)
      val ih = destination.getHeight(this)
      val pt = location
      g2.translate(-pt.x + split.insets.left, 0)

      val dim = split.size
      val x = (dim.width - iw) / 2
      val y = (dim.height - ih) / 2
      g2.drawImage(destination, x, y, iw, ih, this)
      g2.dispose()
    }
  }
  split.rightComponent = afterCanvas

  val layerUI = DividerLocationDragLayerUI()
  val check = JCheckBox("Paint divider")
  check.addActionListener { e ->
    layerUI.setPaintDividerEnabled((e.source as? JCheckBox)?.isSelected == true)
  }

  return JPanel(BorderLayout()).also {
    it.add(JLayer(split, layerUI))
    it.add(check, BorderLayout.SOUTH)
    it.isOpaque = false
    it.preferredSize = Dimension(320, 240)
  }
}

private class DividerLocationDragLayerUI : LayerUI<JSplitPane>() {
  private val startPt = Point()
  private val dc = Cursor.getDefaultCursor()
  private val wc = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR)
  private val thumb = Ellipse2D.Double()
  private var dividerLocation = 0
  private var isDragging = false
  private var isEnter = false
  private var dividerEnabled = false

  override fun installUI(c: JComponent) {
    super.installUI(c)
    (c as? JLayer<*>)?.layerEventMask = AWTEvent.MOUSE_EVENT_MASK or AWTEvent.MOUSE_MOTION_EVENT_MASK
  }

  override fun uninstallUI(c: JComponent) {
    (c as? JLayer<*>)?.layerEventMask = 0
    super.uninstallUI(c)
  }

  override fun paint(g: Graphics, c: JComponent) {
    super.paint(g, c)
    if ((isEnter || isDragging) && c is JLayer<*>) {
      updateThumbLocation(c.view, thumb)
      val g2 = g.create() as? Graphics2D ?: return
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
      g2.paint = Color(255, 100, 100, 100)
      g2.fill(thumb)
      if (dividerEnabled) {
        paintDivider(g2)
      }
      g2.dispose()
    }
  }

  private fun paintDivider(g2: Graphics2D) {
    g2.stroke = BasicStroke(5f)
    g2.paint = Color.WHITE
    g2.draw(thumb)

    val cx = thumb.centerX
    val cy = thumb.centerY

    val line = Line2D.Double(cx, 0.0, cx, thumb.minY)
    g2.draw(line)

    val v = 8.0
    val mx = cx - thumb.getWidth() / 4.0 + v / 2.0
    val triangle = Path2D.Double()
    triangle.moveTo(mx, cy - v)
    triangle.lineTo(mx - v, cy)
    triangle.lineTo(mx, cy + v)
    triangle.lineTo(mx, cy - v)
    triangle.closePath()
    g2.fill(triangle)

    val at = AffineTransform.getQuadrantRotateInstance(2, cx, cy)
    g2.draw(at.createTransformedShape(line))
    g2.fill(at.createTransformedShape(triangle))
  }

  fun setPaintDividerEnabled(flg: Boolean) {
    this.dividerEnabled = flg
  }

  override fun processMouseEvent(e: MouseEvent, l: JLayer<out JSplitPane>) {
    val splitPane = l.view
    when (e.id) {
      MouseEvent.MOUSE_ENTERED -> isEnter = true
      MouseEvent.MOUSE_EXITED -> isEnter = false
      MouseEvent.MOUSE_RELEASED -> isDragging = false
      MouseEvent.MOUSE_PRESSED -> {
        val c = e.component
        if (isDraggableComponent(splitPane, c)) {
          val pt = SwingUtilities.convertPoint(c, e.point, splitPane)
          isDragging = thumb.contains(pt)
          startPt.location = SwingUtilities.convertPoint(c, e.point, splitPane)
          dividerLocation = splitPane.dividerLocation
        }
      }
      // else -> {}
    }
    splitPane.repaint()
  }

  override fun processMouseMotionEvent(e: MouseEvent, l: JLayer<out JSplitPane>) {
    val splitPane = l.view
    val c = e.component
    val pt = SwingUtilities.convertPoint(c, e.point, splitPane)
    if (e.id == MouseEvent.MOUSE_MOVED) {
      splitPane.cursor = if (thumb.contains(e.point)) wc else dc
    } else if (isDragging && isDraggableComponent(splitPane, c) && e.id == MouseEvent.MOUSE_DRAGGED) {
      val delta = when (splitPane.orientation) {
        JSplitPane.HORIZONTAL_SPLIT -> pt.x - startPt.x
        else -> pt.y - startPt.y
      }
      splitPane.dividerLocation = maxOf(0, dividerLocation + delta)
    }
  }

  private fun isDraggableComponent(splitPane: JSplitPane, c: Component) =
    splitPane == c || splitPane == SwingUtilities.getUnwrappedParent(c)

  private fun updateThumbLocation(c: Component, thumb: Ellipse2D) {
    val splitPane = c as? JSplitPane ?: return
    val pos = splitPane.dividerLocation
    if (splitPane.orientation == JSplitPane.HORIZONTAL_SPLIT) {
      thumb.setFrame(pos - R, splitPane.height / 2.0 - R, R + R, R + R)
    } else {
      thumb.setFrame(splitPane.width / 2.0 - R, pos - R, R + R, R + R)
    }
  }

  companion object {
    private const val R = 25.0
  }
}

fun main() {
  EventQueue.invokeLater {
    runCatching {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    }.onFailure {
      it.printStackTrace()
      Toolkit.getDefaultToolkit().beep()
    }
    JFrame().apply {
      defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
      contentPane.add(makeUI())
      pack()
      setLocationRelativeTo(null)
      isVisible = true
    }
  }
}
