package abkabk.azbarkon.features.profile.util

import platform.Foundation.NSBundle

actual fun versionCode(): Int =
    (NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String)?.toIntOrNull() ?: 0
