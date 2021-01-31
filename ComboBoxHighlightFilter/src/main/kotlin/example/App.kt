package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter
import javax.swing.text.Highlighter

fun makeUI(): Component {
  val list = listOf(
    "1111", "1111222", "111122233", "111122233444",
    "12345", "67890", "55551", "555512"
  )
  val combo = makeComboBox(list)
  combo.isEditable = true
  combo.selectedIndex = -1

  (combo.editor.editorComponent as? JTextField)?.also {
    it.text = ""
    it.addKeyListener(ComboKeyHandler(combo))
  }

  val p = JPanel(BorderLayout())
  p.border = BorderFactory.createTitledBorder("Highlight ComboBox")
  p.add(combo, BorderLayout.NORTH)

  val box = Box.createVerticalBox().also {
    it.add(makeHelpPanel())
    it.add(Box.createVerticalStrut(5))
    it.add(p)
  }

  return JPanel(BorderLayout()).also {
    it.add(box, BorderLayout.NORTH)
    it.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
    it.preferredSize = Dimension(320, 240)
  }
}

private fun makeComboBox(model: List<String>): JComboBox<String> {
  val highlightPainter = DefaultHighlightPainter(Color.YELLOW)
  return object : JComboBox<String>(model.toTypedArray()) {
    override fun updateUI() {
      super.updateUI()
      val field = JTextField(" ")
      field.isOpaque = true
      field.border = BorderFactory.createEmptyBorder()
      val renderer = getRenderer()
      setRenderer { list, value, index, isSelected, cellHasFocus ->
        val pattern = (getEditor().editorComponent as? JTextField)?.text ?: ""
        if (index >= 0 && pattern.isNotEmpty()) {
          val highlighter = field.highlighter
          highlighter.removeAllHighlights()
          val txt = value?.toString() ?: ""
          field.text = txt
          addHighlight(highlighter, Pattern.compile(pattern).matcher(txt))
          field.background = if (isSelected) Color(0xAA_64_AA_FF.toInt(), true) else Color.WHITE
          field
        } else {
          renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
        }
      }
    }

    private fun addHighlight(highlighter: Highlighter, matcher: Matcher) {
      runCatching {
        var pos = 0
        while (matcher.find(pos) && matcher.group().isNotEmpty()) {
          val start = matcher.start()
          val end = matcher.end()
          highlighter.addHighlight(start, end, highlightPainter)
          pos = end
        }
      }
    }
  }
}

private fun makeHelpPanel(): Component {
  val lp = JPanel(GridLayout(2, 1, 2, 2))
  lp.add(JLabel("Char: show Popup"))
  lp.add(JLabel("ESC: hide Popup"))

  val rp = JPanel(GridLayout(2, 1, 2, 2))
  rp.add(JLabel("RIGHT: Completion"))
  rp.add(JLabel("ENTER: Add/Selection"))

  val p = JPanel(GridBagLayout())
  p.border = BorderFactory.createTitledBorder("Help")

  val c = GridBagConstraints()
  c.insets = Insets(0, 5, 0, 5)
  c.fill = GridBagConstraints.BOTH
  c.weighty = 1.0

  c.weightx = 1.0
  p.add(lp, c)

  c.weightx = 0.0
  p.add(JSeparator(SwingConstants.VERTICAL), c)

  c.weightx = 1.0
  p.add(rp, c)

  return p
}

private class ComboKeyHandler(private val comboBox: JComboBox<String>) : KeyAdapter() {
  private val list = mutableListOf<String>()
  private var shouldHide = false

  init {
    for (i in 0 until comboBox.model.size) {
      list.add(comboBox.getItemAt(i))
    }
  }

  override fun keyTyped(e: KeyEvent) {
    EventQueue.invokeLater {
      val text = (e.component as? JTextField)?.text ?: ""
      if (text.isEmpty()) {
        val m = DefaultComboBoxModel(list.toTypedArray())
        setSuggestionModel(comboBox, m, "")
        comboBox.hidePopup()
      } else {
        val m = getSuggestedModel(list, text)
        if (m.size == 0 || shouldHide) {
          comboBox.hidePopup()
        } else {
          setSuggestionModel(comboBox, m, text)
          comboBox.showPopup()
        }
      }
    }
  }

  override fun keyPressed(e: KeyEvent) {
    val textField = e.component as? JTextField ?: return
    val text = textField.text
    shouldHide = false
    when (e.keyCode) {
      KeyEvent.VK_RIGHT -> for (s in list) {
        if (s.contains(text)) {
          textField.text = s
          return
        }
      }
      KeyEvent.VK_ENTER -> {
        if (!list.contains(text)) {
          list.add(text)
          list.sort()
          setSuggestionModel(comboBox, getSuggestedModel(list, text), text)
        }
        shouldHide = true
      }
      KeyEvent.VK_ESCAPE -> shouldHide = true
      // else -> {}
    }
  }

  private fun setSuggestionModel(comboBox: JComboBox<String>, mdl: ComboBoxModel<String>, str: String) {
    comboBox.model = mdl
    comboBox.selectedIndex = -1
    (comboBox.editor.editorComponent as? JTextField)?.text = str
  }

  private fun getSuggestedModel(list: List<String>, text: String): ComboBoxModel<String> {
    val m = DefaultComboBoxModel<String>()
    for (s in list) {
      if (s.contains(text)) {
        m.addElement(s)
      }
    }
    return m
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
