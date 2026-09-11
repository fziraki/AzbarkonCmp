package abkabk.azbarkon.features.profile.util

import platform.UIKit.UIAlertController
import platform.UIKit.UIApplication
import platform.darwin.dispatch_after
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_time

actual fun showToast(message: String) {
    val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
    var topVC = rootVC
    while (topVC.presentedViewController != null) {
        topVC = topVC.presentedViewController!!
    }

    val alert = UIAlertController.alertControllerWithTitle(
        title = null,
        message = message,
        preferredStyle = 1, // UIAlertControllerStyleAlert
    )
    topVC.presentViewController(alert, animated = true, completion = null)

    val delay = dispatch_time(0uL, 2_000_000_000L) // 2 seconds in nanoseconds
    dispatch_after(delay, dispatch_get_main_queue()) {
        alert.dismissViewControllerAnimated(true, completion = null)
    }
}
