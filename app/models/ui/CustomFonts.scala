package models.ui

object CustomFonts {
  private[this] val roboto = {
    val fontStream = this.getClass.getClassLoader.getResourceAsStream("font/roboto.ttf")
    val f = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontStream)
    fontStream.close()
    f
  }

  private[this] val fontAwesome = {
    val fontStream = this.getClass.getClassLoader.getResourceAsStream("font/fontawesome.ttf")
    val f = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontStream)
    fontStream.close()
    f
  }

  val regularText = roboto.deriveFont(java.awt.Font.PLAIN, 16)
  val largeText = roboto.deriveFont(java.awt.Font.PLAIN, 24)
  val titleText = roboto.deriveFont(java.awt.Font.PLAIN, 36)
  val icons = fontAwesome.deriveFont(java.awt.Font.PLAIN, 36)
}
