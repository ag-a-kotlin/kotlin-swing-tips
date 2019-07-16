package example

import com.sun.java.swing.plaf.windows.WindowsFileChooserUI
import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.plaf.metal.MetalFileChooserUI

class MainPanel : JPanel(BorderLayout()) {
  init {
    val log = JTextArea()

    val button1 = JButton("Metal")
    button1.addActionListener {
      val fileChooser = object : JFileChooser() {
        override fun updateUI() {
          super.updateUI()
          setUI(EncodingFileChooserUI(this))
          resetChoosableFileFilters()
        }
      }
      val retvalue = fileChooser.showSaveDialog(getRootPane())
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        (fileChooser.getUI() as? EncodingFileChooserUI)?.also {
          val enc = "\nEncoding: ${it.combo.getSelectedItem()}"
          log.setText(fileChooser.getSelectedFile().getAbsolutePath() + enc)
        }
      }
    }

    val button2 = JButton("Alignment: Right")
    button2.addActionListener {
      val fileChooser = object : JFileChooser() {
        override fun updateUI() {
          super.updateUI()
          setUI(object : WindowsFileChooserUI(this) {
            override fun installComponents(fc: JFileChooser) {
              super.installComponents(fc)
              SwingUtils.children(getBottomPanel())
                .filterIsInstance(JLabel::class.java)
                .forEach {
                  it.setAlignmentX(1f)
                  it.setHorizontalAlignment(SwingConstants.RIGHT)
                }
            }
          })
          resetChoosableFileFilters()
        }
      }
      val retvalue = fileChooser.showSaveDialog(getRootPane())
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath())
      }
    }

    val button3 = JButton("Default")
    button3.addActionListener {
      val fileChooser = JFileChooser()
      val retvalue = fileChooser.showSaveDialog(getRootPane())
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(fileChooser.getSelectedFile().getAbsolutePath())
      }
    }

    val p = JPanel().also {
      it.setBorder(BorderFactory.createTitledBorder("JFileChooser"))
      it.add(button1)
      it.add(button2)
      it.add(button3)
    }
    add(p, BorderLayout.NORTH)
    add(JScrollPane(log))
    setPreferredSize(Dimension(320, 240))
  }
}

class EncodingFileChooserUI(filechooser: JFileChooser) : MetalFileChooserUI(filechooser) {
  val combo = JComboBox<String>(arrayOf("UTF-8", "UTF-16", "Shift_JIS", "EUC-JP"))

  override fun installComponents(fc: JFileChooser) {
    super.installComponents(fc)
    val bottomPanel = getBottomPanel()

    val label = object : JLabel("Encoding:") {
      override fun getPreferredSize() = SwingUtils.children(bottomPanel)
          .filterIsInstance(JLabel::class.java)
          .firstOrNull()
          ?.let { it.getPreferredSize() }
          ?: super.getPreferredSize()
    }
    label.setDisplayedMnemonic('E')
    label.setLabelFor(combo)

    val panel = JPanel()
    panel.setLayout(BoxLayout(panel, BoxLayout.LINE_AXIS))
    panel.add(label)
    panel.add(combo)

    // 0: fileNamePanel
    // 1: RigidArea
    // 2: filesOfTypePanel
    bottomPanel.add(Box.createRigidArea(Dimension(1, 5)), 3)
    bottomPanel.add(panel, 4)
    SwingUtils.children(bottomPanel)
      .filterIsInstance(JLabel::class.java)
      .forEach {
        it.setHorizontalAlignment(SwingConstants.RIGHT)
        it.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5))
      }
  }
}

object SwingUtils {
  fun children(parent: Container): List<Component> = parent.getComponents()
    .filterIsInstance(Container::class.java)
    .map { children(it) }
    .fold(listOf<Component>(parent)) { a, b -> a + b }
//  fun stream(parent: Container): Stream<Component> {
//    return Stream.of(*parent.getComponents())
//      .filter { Container::class.java!!.isInstance(it) }
//      .map { c -> stream(Container::class.java.cast(c)) }
//      .reduce { a, b -> Stream.concat(a, b) }
//  }
} /* Singleton */

fun main() {
  EventQueue.invokeLater {
    runCatching {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
    }.onFailure {
      it.printStackTrace()
      Toolkit.getDefaultToolkit().beep()
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
