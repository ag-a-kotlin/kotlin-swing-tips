package example

import java.awt.* // ktlint-disable no-wildcard-imports
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.swing.* // ktlint-disable no-wildcard-imports
import javax.swing.text.html.HTMLEditorKit
import javax.swing.text.html.StyleSheet

class MainPanel : JPanel(BorderLayout()) {
  init {
    val styleSheet = StyleSheet()
    // styleSheet.addRule("body {font-size: 24pt; font-family: IPAexGothic;}")
    val htmlEditorKit = HTMLEditorKit()
    htmlEditorKit.setStyleSheet(styleSheet)
    val editor1 = JEditorPane()
    editor1.setEditorKit(htmlEditorKit)

    val url = javaClass.getResource("SurrogatePair.html")
    try {
      InputStreamReader(url.openStream(), StandardCharsets.UTF_8).use { editor1.read(it, "html") }
    } catch (ex: IOException) {
      editor1.setText("<html><p>(&#xD85B;&#xDE40;) (&#x26E40;)<br />(&#xD842;&#xDF9F;) (&#x00020B9F;)</p></html>")
    }

    val editor2 = JEditorPane()
    // editor2.setFont(new Font("IPAexGothic", Font.PLAIN, 24))
    editor2.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)
    editor2.setText("(\uD85B\uDE40) (\u26E40)\n(\uD842\uDF9F) (\u20B9F)")
    // editor2.setText("(𦹀) (𦹀)\n(𠮟) (𠮟)")

    val p = JPanel(GridLayout(0, 1))
    p.add(makeTitledPanel("Numeric character reference", editor1))
    p.add(makeTitledPanel("Unicode escapes", editor2))

    val button = JButton("browse: SurrogatePair.html")
    button.addActionListener { browseCacheFile(url) }

    add(p)
    add(button, BorderLayout.SOUTH)
    setPreferredSize(Dimension(320, 240))
  }

  private fun browseCacheFile(url: URL) {
    if (Desktop.isDesktopSupported()) {
      try {
        BufferedInputStream(url.openStream()).use {
          val path = Files.createTempFile("_tmp", ".html")
          path.toFile().deleteOnExit()
          Files.copy(it, path, StandardCopyOption.REPLACE_EXISTING)
          Desktop.getDesktop().browse(path.toUri())
        }
      } catch (ex: IOException) {
        ex.printStackTrace()
        Toolkit.getDefaultToolkit().beep()
      }
    }
  }

  private fun makeTitledPanel(title: String, c: Component) = JScrollPane(c).also {
    it.setBorder(BorderFactory.createTitledBorder(title))
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
      setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
      getContentPane().add(MainPanel())
      pack()
      setLocationRelativeTo(null)
      setVisible(true)
    }
  }
}
