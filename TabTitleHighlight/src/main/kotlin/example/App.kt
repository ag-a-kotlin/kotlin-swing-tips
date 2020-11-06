package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import javax.swing.* // ktlint-disable no-wildcard-imports

fun makeUI(): Component {
  val tabbedPane = object : JTabbedPane() {
    private var hoverHandler: MouseMotionListener? = null
    override fun updateUI() {
      removeMouseMotionListener(hoverHandler)
      super.updateUI()
      hoverHandler = object : MouseAdapter() {
        override fun mouseMoved(e: MouseEvent) {
          (e.component as? JTabbedPane)?.also {
            val num = it.indexAtLocation(e.x, e.y)
            for (i in 0 until it.tabCount) {
              it.setForegroundAt(i, if (i == num) Color.GREEN else Color.BLACK)
            }
          }
        }
      }
      addMouseMotionListener(hoverHandler)
    }
  }
  tabbedPane.addTab("11111", JScrollPane(JTree()))
  tabbedPane.addTab("22222", JScrollPane(JLabel("1234567890")))
  tabbedPane.addTab("33333", JScrollPane(JTree()))
  tabbedPane.addTab("44444", JScrollPane(JLabel("0987654321")))
  tabbedPane.addTab("55555", JScrollPane(JTree()))

  return JPanel(BorderLayout()).also {
    it.add(tabbedPane)
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
