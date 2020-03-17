package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.* // ktlint-disable no-wildcard-imports

class MainPanel : JPanel(BorderLayout()) {
  init {
    val combo01 = AlternateRowColorComboBox<String>(makeModel())

    val combo02 = AlternateRowColorComboBox<String>(makeModel())
    combo02.setEditable(true)

    add(Box.createVerticalBox().also {
      it.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5))
      it.add(makeTitledPanel("setEditable(false)", combo01))
      it.add(Box.createVerticalStrut(5))
      it.add(makeTitledPanel("setEditable(true)", combo02))
    }, BorderLayout.NORTH)
    setPreferredSize(Dimension(320, 240))
  }

  private fun makeTitledPanel(title: String, c: Component) = JPanel(BorderLayout()).also {
    it.setBorder(BorderFactory.createTitledBorder(title))
    it.add(c)
  }

  private fun makeModel() = DefaultComboBoxModel<String>().also {
    it.addElement("aaa")
    it.addElement("aaa111")
    it.addElement("aaa222bb")
    it.addElement("1234123512351234")
    it.addElement("bbb1")
    it.addElement("bbb12")
  }
}

class AlternateRowColorComboBox<E>(model: ComboBoxModel<E>) : JComboBox<E>(model) {
  @Transient
  private var itemColorListener: ItemListener? = null

  // constructor() : super()

  // constructor(model: ComboBoxModel<E>) : super(model)

  // constructor(items: Array<E>) : super(items)

  override fun setEditable(flag: Boolean) {
    super.setEditable(flag)
    if (flag) {
      val editor = getEditor().getEditorComponent()
      (editor as? JTextField)?.setOpaque(true)
      editor.setBackground(getAlternateRowColor(getSelectedIndex()))
    }
  }

  override fun updateUI() {
    removeItemListener(itemColorListener)
    setRenderer(null)
    super.updateUI()
    val renderer = getRenderer()
    setRenderer { list, value, index, isSelected, cellHasFocus ->
      val c = renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
      (c as? JLabel)?.setOpaque(true)
      if (!isSelected) {
        c.setBackground(getAlternateRowColor(index))
      }
      return@setRenderer c
    }
    itemColorListener = ItemListener { e ->
      val cb = e.getItemSelectable()
      if (e.getStateChange() == ItemEvent.SELECTED && cb is JComboBox<*>) {
        val rc = getAlternateRowColor(cb.getSelectedIndex())
        if (cb.isEditable()) {
          (cb.getEditor().getEditorComponent() as? JTextField)?.setBackground(rc)
        } else {
          cb.setBackground(rc)
        }
      }
    }
    addItemListener(itemColorListener)
    EventQueue.invokeLater {
      (getEditor().getEditorComponent() as? JTextField)?.also {
        it.setOpaque(true)
        it.setBackground(getAlternateRowColor(getSelectedIndex()))
      }
    }
  }

  private fun getAlternateRowColor(idx: Int) = if (idx % 2 == 0) Color(0xE1_FF_E1) else Color.WHITE
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
