@file:JvmName("VersionChecker")

package me.seebrock3r.choosercallbackcompat

import android.annotation.SuppressLint
import android.os.Build
import android.support.annotation.VisibleForTesting

val android: OSVersionChecker
    @SuppressLint("VisibleForTests") // Accessing it as private would be correct here, probably a Lint bug with Kotlin visibilities
    @JvmName("android")
    get() = AndroidOSVersionChecker()

// Adapted from https://gist.github.com/rock3r/f62c5330de539c9bd36558106fd62200
interface OSVersionChecker {

    val isAtLeastLollipopMR1: Boolean
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal class AndroidOSVersionChecker(sdkVersionInt: Int = Build.VERSION.SDK_INT) : OSVersionChecker {

    override val isAtLeastLollipopMR1 = sdkVersionInt >= Build.VERSION_CODES.LOLLIPOP_MR1
}
