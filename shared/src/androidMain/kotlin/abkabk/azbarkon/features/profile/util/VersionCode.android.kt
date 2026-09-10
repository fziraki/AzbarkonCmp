package abkabk.azbarkon.features.profile.util

import android.content.Context
import org.koin.core.context.GlobalContext

actual fun versionCode(): Int {
    val context = GlobalContext.get().get<Context>()
    val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    return pInfo.longVersionCode.toInt()
}
