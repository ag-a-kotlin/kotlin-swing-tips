package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.awt.event.KeyEvent
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.event.ListDataEvent
import javax.swing.plaf.basic.ComboPopup

fun makeUI(): Component {
  val c = GridBagConstraints()
  val p = JPanel(GridBagLayout())
  p.border = BorderFactory.createEmptyBorder(10, 20, 10, 20)

  c.insets = Insets(10, 5, 5, 0)
  c.gridheight = 1
  c.gridwidth = 1
  c.gridy = 0

  c.gridx = 0
  c.weightx = 0.0
  c.anchor = GridBagConstraints.WEST
  listOf(
    "setSelectedIndex(-1/idx):",
    "contentsChanged(...):",
    "repaint():",
    "(remove/insert)ItemAt(...):",
    "fireContentsChanged(...):"
  ).map { JLabel(it) }
    .forEach {
      p.add(it, c)
      c.gridy += 1
    }

  c.gridy = 0
  c.gridx = 1
  c.weightx = 1.0
  c.fill = GridBagConstraints.HORIZONTAL

  val m = arrayOf(
    CheckableItem("aaa", false),
    CheckableItem("00000", true),
    CheckableItem("111", false),
    CheckableItem("33333", true),
    CheckableItem("2222", true),
    CheckableItem("444444", false)
  )

  val combo0 = CheckedComboBox(DefaultComboBoxModel(m))
  val combo1 = CheckedComboBox1(DefaultComboBoxModel(m))
  val combo2 = CheckedComboBox2(DefaultComboBoxModel(m))
  val combo3 = CheckedComboBox3(DefaultComboBoxModel(m))
  val combo4 = CheckedComboBox4(CheckableComboBoxModel(m))

  listOf(combo0, combo1, combo2, combo3, combo4)
    .forEach {
      p.add(it, c)
      c.gridy += 1
    }

  return JPanel(BorderLayout()).also {
    it.add(p, BorderLayout.NORTH)
    it.preferredSize = Dimension(320, 240)
  }
}

private open class CheckableItem(private val text: String, var isSelected: Boolean) {
  override fun toString() = text
}

private class CheckBoxCellRenderer<E : CheckableItem> : ListCellRenderer<E> {
  private val label = JLabel(" ")
  private val check = JCheckBox(" ")

  override fun getListCellRendererComponent(
    list: JList<out E>,
    value: E,
    index: Int,
    isSelected: Boolean,
    cellHasFocus: Boolean
  ): Component {
    if (index < 0) {
      // val txt = getCheckedItemString(list.model)
      // label.setText(if (txt.isEmpty()) " " else txt)
      // label.text = txt.takeUnless { it.isEmpty() } ?: " "
      label.text = getCheckedItemString(list.model).ifEmpty { " " }
      return label
    } else {
      check.text = value.toString()
      check.isSelected = value.isSelected
      if (isSelected) {
        check.background = list.selectionBackground
        check.foreground = list.selectionForeground
      } else {
        check.background = list.background
        check.foreground = list.foreground
      }
      return check
    }
  }

  private fun <E : CheckableItem> getCheckedItemString(model: ListModel<E>): String {
    return (0 until model.size)
      .asSequence()
      .map { model.getElementAt(it) }
      .filter { it.isSelected }
      .map { it.toString() }
      .sorted()
      .joinToString()
  }
}

private open class CheckedComboBox<E : CheckableItem>(
  model: ComboBoxModel<E>
) : JComboBox<E>(model) {
  private var keepOpen = false
  private var listener: ActionListener? = null

  // constructor() : super()

  override fun getPreferredSize() = Dimension(200, 20)

  override fun updateUI() {
    setRenderer(null)
    removeActionListener(listener)
    super.updateUI()
    listener = ActionListener { e ->
      if (e.modifiers and AWTEvent.MOUSE_EVENT_MASK.toInt() != 0) {
        updateItem(selectedIndex)
        keepOpen = true
      }
    }
    setRenderer(CheckBoxCellRenderer<CheckableItem>())
    addActionListener(listener)

    val im = getInputMap(JComponent.WHEN_FOCUSED)
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "checkbox-select")

    val action = object : AbstractAction() {
      override fun actionPerformed(e: ActionEvent) {
        val a = getAccessibleContext().getAccessibleChild(0)
        if (a is ComboPopup) {
          updateItem(a.list.selectedIndex)
        }
      }
    }
    actionMap.put("checkbox-select", action)
  }

  open fun updateItem(index: Int) {
    if (isPopupVisible) {
      val item = getItemAt(index)
      item.isSelected = !item.isSelected
      selectedIndex = -1
      selectedItem = item
    }
  }

  override fun setPopupVisible(v: Boolean) {
    if (keepOpen) {
      keepOpen = false
    } else {
      super.setPopupVisible(v)
    }
  }
}

private class CheckedComboBox1<E : CheckableItem>(
  model: ComboBoxModel<E>
) : CheckedComboBox<E>(model) {
  override fun updateItem(index: Int) {
    if (isPopupVisible) {
      val item = getItemAt(index)
      item.isSelected = !item.isSelected
      contentsChanged(ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, index, index))
    }
  }
}

private class CheckedComboBox2<E : CheckableItem>(
  model: ComboBoxModel<E>
) : CheckedComboBox<E>(model) {
  override fun updateItem(index: Int) {
    if (isPopupVisible) {
      val item = getItemAt(index)
      item.isSelected = !item.isSelected
      repaint()
      val a = getAccessibleContext().getAccessibleChild(0)
      (a as? ComboPopup)?.list?.repaint()
    }
  }
}

private class CheckedComboBox3<E : CheckableItem>(
  model: ComboBoxModel<E>
) : CheckedComboBox<E>(model) {
  override fun updateItem(index: Int) {
    if (isPopupVisible) {
      val item = getItemAt(index)
      item.isSelected = !item.isSelected
      removeItemAt(index)
      insertItemAt(item, index)
      selectedItem = item
    }
  }
}

private class CheckableComboBoxModel<E>(
  items: Array<E>
) : DefaultComboBoxModel<E>(items) {
  fun fireContentsChanged(index: Int) {
    super.fireContentsChanged(this, index, index)
  }
}

private class CheckedComboBox4<E : CheckableItem>(
  model: ComboBoxModel<E>
) : CheckedComboBox<E>(model) {
  override fun updateItem(index: Int) {
    if (isPopupVisible) {
      val item = getItemAt(index)
      item.isSelected = !item.isSelected
      (model as? CheckableComboBoxModel<E>)?.fireContentsChanged(index)
    }
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
