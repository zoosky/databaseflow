package services.licensing

import java.util.Base64

import licensing.{DecryptUtils, License, LicenseEdition}
import models.settings.SettingKey
import services.config.ConfigFileService
import services.settings.SettingsService
import utils.Logging

import scala.util.{Failure, Success, Try}

object LicenseService extends Logging {
  private[this] var licenseContent: Option[String] = None
  private[this] var license: Option[License] = None

  def readLicense() = {
    val content = SettingsService(SettingKey.LicenseContent)
    if (content.isEmpty) {
      log.warn(s"${utils.Config.projectName} Trial Edition started.")
      license = None
      licenseContent = None
    } else {
      license = parseLicense(content) match {
        case Success(lic) =>
          log.warn(s"${utils.Config.projectName} ${lic.edition.title}, registered to [${lic.name}] (${lic.email}).")
          licenseContent = Some(content)
          Some(lic)
        case Failure(x) =>
          log.warn(s"Unable to parse license [$content].", x)
          None
      }
    }
    if (ConfigFileService.isDocker) {
      log.warn(" - Head to http://[docker address]:4260 to get started!")
      log.warn(" - Since this is a docker container, you'll need to expose port 4260, by using the command flag [-p 4260:4260].")
    } else {
      log.warn(" - Head to http://localhost:4260 to get started!")
    }
    license
  }

  def parseLicense(content: String) = Try {
    val decoded = Base64.getDecoder.decode(content)
    val decrypted = DecryptUtils.decrypt(decoded)
    License.fromString(decrypted) match {
      case Success(l) => l
      case Failure(x) => throw x
    }
  }

  def getLicense = license
  def getLicenseContent = licenseContent
  def hasLicense = license.isDefined
  def isNonCommercial = license.exists(_.edition == LicenseEdition.NonCommercial)
  def isPersonalEdition = license.exists(_.edition == LicenseEdition.Personal)
  def isTeamEdition = license.exists(_.edition == LicenseEdition.Team)
}
