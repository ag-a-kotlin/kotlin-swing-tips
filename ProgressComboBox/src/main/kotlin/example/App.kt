package example

import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports

class MainPanel : JPanel(BorderLayout(5, 5)) {
  private val combo = object : JComboBox<String>() {
    override fun updateUI() {
      super.updateUI()
      setRenderer(ProgressCellRenderer())
    }
  }
  private val button = JButton("load")
  @Transient
  private var worker: SwingWorker<Array<String>, Int>? = null
  private var counter = 0

  init {
    button.addActionListener {
      button.isEnabled = false
      combo.isEnabled = false
      // combo.removeAllItems()
      worker = ComboTask().also {
        it.execute()
      }
    }
    add(makeTitledPanel("ProgressComboBox: ", combo, button), BorderLayout.NORTH)
    add(JScrollPane(JTextArea()))
    border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
    preferredSize = Dimension(320, 240)
  }

  private inner class ComboTask : BackgroundTask() {
    override fun process(chunks: List<Int>) {
      if (isCancelled) {
        return
      }
      if (!isDisplayable) {
        println("process: DISPOSE_ON_CLOSE")
        cancel(true)
        return
      }
      for (i in chunks) {
        counter = i
      }
      combo.selectedIndex = -1
      combo.repaint()
    }

    public override fun done() {
      if (!isDisplayable) {
        println("done: DISPOSE_ON_CLOSE")
        cancel(true)
        return
      }
      runCatching {
        if (!isCancelled) {
          val array = get()
          combo.setModel(DefaultComboBoxModel(array))
          combo.selectedIndex = 0
        }
      }
      combo.isEnabled = true
      button.isEnabled = true
      counter = 0
    }
  }

  private inner class ProgressCellRenderer<E> : ListCellRenderer<E> {
    private val renderer: ListCellRenderer<in E> = DefaultListCellRenderer()
    private val bar = JProgressBar()
    override fun getListCellRendererComponent(
      list: JList<out E>,
      value: E?,
      index: Int,
      isSelected: Boolean,
      cellHasFocus: Boolean
    ): Component {
      if (index < 0 && worker?.isDone != true) {
        bar.font = list.font
        bar.border = BorderFactory.createEmptyBorder()
        bar.value = counter
        return bar
      }
      return renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
    }
  }

  companion object {
    fun makeTitledPanel(title: String, cmp: Component, btn: Component): Component {
      val c = GridBagConstraints()
      val p = JPanel(GridBagLayout())
      c.insets = Insets(5, 5, 5, 0)
      p.add(JLabel(title), c)
      c.weightx = 1.0
      c.fill = GridBagConstraints.HORIZONTAL
      p.add(cmp, c)
      c.weightx = 0.0
      c.fill = GridBagConstraints.NONE
      c.insets = Insets(5, 5, 5, 5)
      p.add(btn, c)
      return p
    }
  }
}

open class BackgroundTask : SwingWorker<Array<String>, Int>() {
  @Throws(InterruptedException::class)
  public override fun doInBackground(): Array<String> {
    var current = 0
    val list: MutableList<String> = ArrayList()
    while (current <= MAX && !isCancelled) {
      Thread.sleep(50)
      val iv = 100 * current / MAX
      publish(iv)
      // setProgress(iv)
      list.add("Test: $current")
      current++
    }
    return list.toTypedArray()
  }

  companion object {
    private const val MAX = 30
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
      contentPane.add(MainPanel())
      pack()
      setLocationRelativeTo(null)
      isVisible = true
    }
  }
}
