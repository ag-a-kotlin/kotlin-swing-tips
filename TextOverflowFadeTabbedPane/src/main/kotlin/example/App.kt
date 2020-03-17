package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.font.TextLayout
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.plaf.synth.Region
import javax.swing.plaf.synth.SynthConstants
import javax.swing.plaf.synth.SynthContext
import javax.swing.plaf.synth.SynthLookAndFeel

class MainPanel : JPanel(BorderLayout()) {
  init {
    val list = listOf(makeTabbedPane(ClippedTitleTabbedPane()), makeTabbedPane(TextOverflowFadeTabbedPane()))

    val p = JPanel(GridLayout(list.size, 1))
    list.forEach { p.add(it) }

    val check = JCheckBox("LEFT")
    check.addActionListener { e ->
      val b = (e.getSource() as? JCheckBox)?.isSelected() ?: false
      val tabPlacement = if (b) JTabbedPane.LEFT else JTabbedPane.TOP
      list.forEach { it.setTabPlacement(tabPlacement) }
    }

    add(check, BorderLayout.NORTH)
    add(p)
    setPreferredSize(Dimension(320, 240))
  }

  private fun makeTabbedPane(tabbedPane: JTabbedPane) = tabbedPane.also {
    it.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT)
    it.addTab("1111111111111111111", ColorIcon(Color.RED), JScrollPane(JTree()))
    it.addTab("2", ColorIcon(Color.GREEN), JLabel("bbbbbbbbb"))
    it.addTab("33333333333333", ColorIcon(Color.BLUE), JScrollPane(JTree()))
    it.addTab("444444444444444", ColorIcon(Color.ORANGE), JLabel("dddddddddd"))
    it.addTab("55555555555555555555555555555555", ColorIcon(Color.CYAN), JLabel("e"))
  }
}

open class ClippedTitleTabbedPane : JTabbedPane {
  private val tabInsets = UIManager.getInsets("TabbedPane.tabInsets") ?: getSynthTabInsets()
  private val tabAreaInsets = UIManager.getInsets("TabbedPane.tabAreaInsets") ?: getSynthTabAreaInsets()

  private fun getSynthTabInsets(): Insets {
    val style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB)
    val context = SynthContext(this, Region.TABBED_PANE_TAB, style, SynthConstants.ENABLED)
    return style.getInsets(context, null)
  }

  private fun getSynthTabAreaInsets(): Insets {
    val style = SynthLookAndFeel.getStyle(this, Region.TABBED_PANE_TAB_AREA)
    val context = SynthContext(this, Region.TABBED_PANE_TAB_AREA, style, SynthConstants.ENABLED)
    return style.getInsets(context, null)
  }

  constructor() : super()

  constructor(tabPlacement: Int) : super(tabPlacement)

  override fun doLayout() {
    val tabCount = getTabCount()
    if (tabCount == 0 || !isVisible()) {
      super.doLayout()
      return
    }
    val tabInsets = tabInsets
    val tabAreaInsets = tabAreaInsets
    val insets = getInsets()
    val tabPlacement = getTabPlacement()
    val areaWidth = getWidth() - tabAreaInsets.left - tabAreaInsets.right - insets.left - insets.right
    val isSide = tabPlacement == SwingConstants.LEFT || tabPlacement == SwingConstants.RIGHT
    var tabWidth = if (isSide) areaWidth / 4 else areaWidth / tabCount
    val gap = if (isSide) 0 else areaWidth - tabWidth * tabCount

    // "3" is magic number @see BasicTabbedPaneUI#calculateTabWidth
    tabWidth -= tabInsets.left + tabInsets.right + 3
    updateAllTabWidth(tabWidth, gap)

    super.doLayout()
  }

  override fun insertTab(title: String, icon: Icon, component: Component, tip: String?, index: Int) {
    super.insertTab(title, icon, component, tip ?: title, index)
    setTabComponentAt(index, JLabel(title, icon, SwingConstants.LEADING))
  }

  private fun updateAllTabWidth(tabWidth: Int, gap: Int) {
    val dim = Dimension()
    var rest = gap
    for (i in 0 until getTabCount()) {
      val tab = getTabComponentAt(i) as? JComponent ?: continue
      val a = if (i == getTabCount() - 1) rest else 1
      val w = if (rest > 0) tabWidth + a else tabWidth
      dim.setSize(w, tab.getPreferredSize().height)
      tab.setPreferredSize(dim)
      rest -= a
    }
  }
}

open class TextOverflowFadeTabbedPane : ClippedTitleTabbedPane {
  constructor() : super()

  constructor(tabPlacement: Int) : super(tabPlacement)

  override fun insertTab(title: String, icon: Icon, component: Component, tip: String?, index: Int) {
    super.insertTab(title, icon, component, tip ?: title, index)
    setTabComponentAt(index, JPanel(BorderLayout(2, 0)).also {
      it.setOpaque(false)
      it.add(JLabel(icon), BorderLayout.WEST)
      it.add(TextOverflowFadeLabel(title))
    })
  }
}

internal class TextOverflowFadeLabel(text: String) : JLabel(text) {
  override fun paintComponent(g: Graphics) {
    val i = getInsets()
    val w = getWidth() - i.left - i.right
    val h = getHeight() - i.top - i.bottom
    val rect = Rectangle(i.left, i.top, w - LENGTH, h)

    val g2 = g.create() as Graphics2D
    g2.setFont(g.getFont())
    g2.setPaint(getForeground())

    // val frc = g2.getFontRenderContext()
    val tl = TextLayout(getText(), getFont(), g2.getFontRenderContext())
    val baseline = getBaseline(w, h).toFloat()
    val fx = i.left.toFloat()

    g2.setClip(rect)
    tl.draw(g2, fx, baseline)

    rect.width = 1
    var alpha = 1f
    (w - LENGTH until w).forEach {
      rect.x = it
      alpha = maxOf(0f, alpha - DIFF)
      g2.setComposite(AlphaComposite.SrcOver.derive(alpha))
      g2.setClip(rect)
      tl.draw(g2, fx, baseline)
    }
    g2.dispose()
  }

  companion object {
    private const val LENGTH = 20
    private const val DIFF = .05f
  }
}

internal class ColorIcon(private val color: Color) : Icon {
  override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
    val g2 = g.create() as Graphics2D
    g2.translate(x, y)
    g2.setPaint(color)
    g2.fillRect(1, 2, getIconWidth() - 2, getIconHeight() - 2)
    g2.dispose()
  }

  override fun getIconWidth() = 16

  override fun getIconHeight() = 16
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
      contentPane.add(MainPanel())
      pack()
      setLocationRelativeTo(null)
      isVisible = true
    }
  }
}
