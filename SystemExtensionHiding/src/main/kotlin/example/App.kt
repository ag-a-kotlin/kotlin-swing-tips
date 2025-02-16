package example

import java.awt.* // ktlint-disable no-wildcard-imports
import javax.swing.* // ktlint-disable no-wildcard-imports

fun makeUI(): Component {
  val log = JTextArea()
  val key = "FileChooser.useSystemExtensionHiding"
  val button1 = JButton("false")
  button1.addActionListener {
    UIManager.put(key, false)
    val fileChooser = JFileChooser()
    val retValue = fileChooser.showOpenDialog(button1.rootPane)
    if (retValue == JFileChooser.APPROVE_OPTION) {
      log.text = fileChooser.selectedFile.absolutePath
    }
  }

  val button2 = JButton("true")
  button2.addActionListener {
    UIManager.put(key, true)
    val fileChooser = JFileChooser()
    val retValue = fileChooser.showOpenDialog(button2.rootPane)
    if (retValue == JFileChooser.APPROVE_OPTION) {
      log.text = fileChooser.selectedFile.absolutePath
    }
  }

  val box = Box.createHorizontalBox().also {
    it.border = BorderFactory.createTitledBorder(key)
    it.add(Box.createHorizontalGlue())
    it.add(button1)
    it.add(Box.createHorizontalStrut(5))
    it.add(button2)
    it.add(Box.createHorizontalGlue())
  }

  val p = JPanel(BorderLayout())
  p.add(box, BorderLayout.NORTH)
  p.add(JScrollPane(log))

  return JPanel(BorderLayout()).also {
    val mb = JMenuBar()
    mb.add(LookAndFeelUtil.createLookAndFeelMenu())
    EventQueue.invokeLater { it.rootPane.jMenuBar = mb }
    it.add(p)
    it.preferredSize = Dimension(320, 240)
  }
}

private object LookAndFeelUtil {
  private var lookAndFeel = UIManager.getLookAndFeel().javaClass.name
  fun createLookAndFeelMenu() = JMenu("LookAndFeel").also {
    val lafRadioGroup = ButtonGroup()
    for (lafInfo in UIManager.getInstalledLookAndFeels()) {
      it.add(createLookAndFeelItem(lafInfo.name, lafInfo.className, lafRadioGroup))
    }
  }

  private fun createLookAndFeelItem(
    lafName: String,
    lafClassName: String,
    lafGroup: ButtonGroup
  ): JMenuItem {
    val lafItem = JRadioButtonMenuItem(lafName, lafClassName == lookAndFeel)
    lafItem.actionCommand = lafClassName
    lafItem.hideActionText = true
    lafItem.addActionListener { e ->
      val m = lafGroup.selection
      runCatching {
        setLookAndFeel(m.actionCommand)
      }.onFailure {
        UIManager.getLookAndFeel().provideErrorFeedback(e.source as? Component)
      }
    }
    lafGroup.add(lafItem)
    return lafItem
  }

  @Throws(
    ClassNotFoundException::class,
    InstantiationException::class,
    IllegalAccessException::class,
    UnsupportedLookAndFeelException::class
  )
  private fun setLookAndFeel(lookAndFeel: String) {
    val oldLookAndFeel = LookAndFeelUtil.lookAndFeel
    if (oldLookAndFeel != lookAndFeel) {
      UIManager.setLookAndFeel(lookAndFeel)
      LookAndFeelUtil.lookAndFeel = lookAndFeel
      updateLookAndFeel()
    }
  }

  private fun updateLookAndFeel() {
    for (window in Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window)
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
