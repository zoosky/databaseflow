import licensing._

object Entrypoint extends App {
  println("License Generator")

  //KeyProvider.generateKeys()

  //println(KeyProvider.decryptKey.isPrivateKey + ", " + KeyProvider.encryptKey.isPublicKey)

  //val encrypted = EncryptUtils.encrypt("test")
  //val decrypted = EncryptUtils.decrypt(encrypted)
  //println("Decrypted: " + decrypted)

  LicenseGenerator.saveLicense("kyle@databaseflow.com", "please work...", overwrite = true)
  LicenseGenerator.listLicenses().foreach(println)

  println(LicenseGenerator.loadLicense("kyle@databaseflow.com"))
}
