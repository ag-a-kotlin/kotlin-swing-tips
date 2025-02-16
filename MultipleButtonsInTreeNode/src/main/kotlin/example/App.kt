package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.MouseEvent
import java.util.EventObject
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreeCellEditor
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreeNode

fun makeUI(): Component {
  val tree = object : JTree() {
    override fun updateUI() {
      setCellRenderer(null)
      setCellEditor(null)
      super.updateUI()
      setCellRenderer(ButtonCellRenderer())
      setCellEditor(ButtonCellEditor())
      setRowHeight(0)
    }
  }
  tree.isEditable = true
  return JPanel(BorderLayout()).also {
    it.add(JScrollPane(tree))
    it.preferredSize = Dimension(320, 240)
  }
}

private class ButtonPanel : JPanel() {
  val renderer = DefaultTreeCellRenderer()
  val b1 = ColorButton(ColorIcon(Color.RED))
  val b2 = ColorButton(ColorIcon(Color.GREEN))
  val b3 = ColorButton(ColorIcon(Color.BLUE))

  init {
    isOpaque = false
  }

  fun remakePanel(c: Component): Component {
    removeAll()
    listOf(b1, b2, b3, c).forEach { add(it) }
    return this
  }
}

private class ButtonCellRenderer : TreeCellRenderer {
  private val panel = ButtonPanel()
  override fun getTreeCellRendererComponent(
    tree: JTree,
    value: Any?,
    selected: Boolean,
    expanded: Boolean,
    leaf: Boolean,
    row: Int,
    hasFocus: Boolean
  ): Component {
    val c = panel.renderer.getTreeCellRendererComponent(
      tree,
      value,
      selected,
      expanded,
      leaf,
      row,
      hasFocus
    )
    return panel.remakePanel(c)
  }
}

private class ButtonCellEditor : AbstractCellEditor(), TreeCellEditor {
  private val panel = ButtonPanel()

  init {
    panel.b1.addActionListener {
      println("b1: " + panel.renderer.text)
      stopCellEditing()
    }
    panel.b2.addActionListener {
      println("b2: " + panel.renderer.text)
      stopCellEditing()
    }
    panel.b3.addActionListener {
      println("b3: " + panel.renderer.text)
      stopCellEditing()
    }
  }

  override fun getTreeCellEditorComponent(
    tree: JTree,
    value: Any?,
    isSelected: Boolean,
    expanded: Boolean,
    leaf: Boolean,
    row: Int
  ): Component {
    val c = panel.renderer.getTreeCellRendererComponent(
      tree,
      value,
      true,
      expanded,
      leaf,
      row,
      true
    )
    return panel.remakePanel(c)
  }

  override fun getCellEditorValue(): Any = panel.renderer.text

  override fun isCellEditable(e: EventObject): Boolean {
    val tree = e.source
    if (tree !is JTree || e !is MouseEvent) {
      return false
    }
    val p = e.point
    val path = tree.getPathForLocation(p.x, p.y)
    val r = tree.getPathBounds(path)
    val node = path?.lastPathComponent
    return if (node is TreeNode && r != null && r.contains(p)) {
      val row = tree.getRowForLocation(p.x, p.y)
      val renderer = tree.cellRenderer
      val c = renderer.getTreeCellRendererComponent(
        tree,
        " ",
        true,
        true,
        node.isLeaf,
        row,
        true
      )
      c.bounds = r
      c.setLocation(0, 0)
      // tree.doLayout()
      tree.revalidate()
      p.translate(-r.x, -r.y)
      SwingUtilities.getDeepestComponentAt(c, p.x, p.y) is JButton
    } else {
      false
    }
  }
}

private class ColorButton(icon: ColorIcon) : JButton(icon) {
  init {
    pressedIcon = ColorIcon(icon.color.darker())
    isFocusable = false
    isFocusPainted = false
    isBorderPainted = false
    isContentAreaFilled = false
    border = BorderFactory.createEmptyBorder()
  }
}

private class ColorIcon(val color: Color) : Icon {
  override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
    val g2 = g.create() as? Graphics2D ?: return
    g2.translate(x, y)
    g2.paint = color
    g2.fillRect(0, 0, iconWidth, iconHeight)
    g2.dispose()
  }

  override fun getIconWidth() = 8

  override fun getIconHeight() = 8
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
