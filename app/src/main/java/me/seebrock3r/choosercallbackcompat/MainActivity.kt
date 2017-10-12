package me.seebrock3r.choosercallbackcompat

import android.annotation.TargetApi
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {

        private const val REQUEST_CODE_PICKER = 3456
        private const val REQUEST_CODE_CHOOSER = 4567
        private const val ACTION_SHARE_CHOOSER_CALLBACK = "me.seebrock3r.pickervschooser.ShareActivity.ACTION_SHARE_CHOOSER_CALLBACK"
        private const val KEY_PICKER_ALREADY_SHOWN = "me.seebrock3r.pickervschooser.ShareActivity.KEY_PICKER_ALREADY_SHOWN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState?.getBoolean(KEY_PICKER_ALREADY_SHOWN, false) == true) {
            return
        }

        picker.setOnClickListener { createShareIntent().startWithPicker(this) }
        chooser.setOnClickListener { createShareIntent().startWithChooser(this) }
    }

    private fun createShareIntent(): Intent {
        val shareText = getString(R.string.sharing_text)
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
    }

    private fun Intent.startWithPicker(activity: Activity) {
        val pickerIntent = createLegacyPickerIntent(this, getString(R.string.picker_dialog_title))
        activity.startActivityForResult(pickerIntent, REQUEST_CODE_PICKER)
    }

    private fun createLegacyPickerIntent(shareIntent: Intent, title: String) =
            Intent(Intent.ACTION_PICK_ACTIVITY).apply {
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(Intent.EXTRA_INTENT, shareIntent)
            }

    @TargetApi(VERSION_CODES.LOLLIPOP_MR1)
    private fun Intent.startWithChooser(activity: Activity) {
        if (!android.isAtLeastLollipopMR1) {
            Snackbar.make(content_root, getString(R.string.min_sdk_warning), Snackbar.LENGTH_LONG).show()
            return
        }

        val pendingIntent = PendingIntent.getBroadcast(activity, REQUEST_CODE_CHOOSER, chooserCallbackIntent(), PendingIntent.FLAG_ONE_SHOT)
        val chooserIntent = createChooserIntent(this, pendingIntent, getString(R.string.share_dialog_title))
        startActivity(chooserIntent)
    }

    private fun chooserCallbackIntent() = Intent(ACTION_SHARE_CHOOSER_CALLBACK)
            .apply { `package` = packageName }

    @RequiresApi(VERSION_CODES.LOLLIPOP_MR1)
    private fun createChooserIntent(shareIntent: Intent, callbackPendingIntent: PendingIntent, title: String) =
            Intent.createChooser(shareIntent, title, callbackPendingIntent.intentSender)

    override fun onStart() {
        super.onStart()
        registerReceiver(chooserCallbackReceiver, IntentFilter(ACTION_SHARE_CHOOSER_CALLBACK))
    }

    private val chooserCallbackReceiver = object : BroadcastReceiver() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
        override fun onReceive(context: Context, intent: Intent) {
            trackSharingTo(intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PICKER && resultCode == Activity.RESULT_OK) {
            onShareTargetSelected(data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun onShareTargetSelected(shareTargetIntent: Intent?) {
        shareTargetIntent?.component?.let {
            trackSharingTo(shareTargetIntent.component)
            startActivity(shareTargetIntent)
        }
    }

    private fun trackSharingTo(component: ComponentName) {
        // TODO proper tracking of the sharing completion
        Snackbar.make(content_root, getString(R.string.sharing_message, component.packageName), Snackbar.LENGTH_LONG).show()
        Log.i("Sharing", "Shared to ${component.packageName}")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(KEY_PICKER_ALREADY_SHOWN, true)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        unregisterReceiver(chooserCallbackReceiver)
        super.onStop()
    }

}
