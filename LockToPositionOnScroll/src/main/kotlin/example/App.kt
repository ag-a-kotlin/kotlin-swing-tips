package example

import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports

private val EVEN_BACKGROUND = Color(0xAA_DD_FF_FF.toInt(), true)

fun makeUI(): Component {
  val key = "List.lockToPositionOnScroll"
  // UIManager.put(key, false)
  val model = DefaultListModel<String>()
  for (i in 0 until 1000) {
    model.addElement(i.toString())
  }
  val list = object : JList<String>(model) {
    override fun updateUI() {
      cellRenderer = null
      super.updateUI()
      val r = cellRenderer
      cellRenderer = ListCellRenderer { list, value, index, isSelected, cellHasFocus ->
        val c = r.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        if (isSelected) {
          c.foreground = list.selectionForeground
          c.background = list.selectionBackground
        } else {
          c.foreground = list.foreground
          c.background = if (index % 2 == 0) EVEN_BACKGROUND else list.background
        }
        c
      }
    }
  }
  list.fixedCellHeight = 64
  val check = JCheckBox(key, UIManager.getBoolean(key))
  check.addActionListener { e ->
    UIManager.put(key, (e.source as? JCheckBox)?.isSelected == true)
  }
  return JPanel(BorderLayout()).also {
    it.add(check, BorderLayout.NORTH)
    it.add(JScrollPane(list))
    it.preferredSize = Dimension(320, 240)
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
