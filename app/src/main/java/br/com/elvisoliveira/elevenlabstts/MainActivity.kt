package br.com.elvisoliveira.elevenlabstts

import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Intent.EXTRA_PROCESS_TEXT support from 23 or above
                val text: String? = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
                if (text != null) {
                    showAlertDialog(text)
                }
            }
        }
    }

    private fun showAlertDialog(selectedText: String) {
        val mBuilder = MaterialAlertDialogBuilder(this)
            .setMessage(selectedText)
            .setNeutralButton(getResources().getString(R.string.play), null)
            .setNegativeButton(getResources().getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .create().apply {
                window?.setGravity(Gravity.TOP)
                show()
            }

        val mPositiveButton = mBuilder.getButton(AlertDialog.BUTTON_NEUTRAL)
        mPositiveButton.setOnClickListener {
            mPositiveButton.apply {
                isClickable = false
                text = getString(R.string.loading)
            }
            lifecycleScope.launch(Dispatchers.IO) {
                val audioData = ElevenLabsService().textToSpeech(selectedText)
                val audioFile = File("$cacheDir/music_file")

                FileOutputStream(audioFile).use { fos ->
                    fos.write(audioData)
                }

                val mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioFile.path)
                    prepare()
                    start()
                }

                mediaPlayer.setOnCompletionListener {
                    mPositiveButton.apply {
                        isClickable = true
                        text = getString(R.string.play)
                    }
                }

                withContext(Dispatchers.Main) {
                    mPositiveButton.text = getString(R.string.playing)
                }
            }
        }
    }
}