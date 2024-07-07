package br.com.elvisoliveira.elevenlabstts

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentLinkedQueue

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : AppCompatActivity() {
    private val audioChunksQueue = ConcurrentLinkedQueue<ByteArray?>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Intent.EXTRA_PROCESS_TEXT  support from 23 or above
                val text: String? = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
                if (text != null) {
                    showAlertDialog(text)
                }
            }
        }
    }

    /**
     * Here is the material alert dialog
     *
     * @param selectedText
     */
    @SuppressLint("SetTextI18n")
    private fun showAlertDialog(selectedText: String) {
        val mBuilder = MaterialAlertDialogBuilder(this)
            .setTitle("ElevenLabs TTS")
            .setMessage(selectedText)
            .setNeutralButton("Play", null)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create().apply {
                window?.setGravity(Gravity.TOP)
                show()
            }
        val mPositiveButton = mBuilder.getButton(AlertDialog.BUTTON_NEUTRAL)
        mPositiveButton.setOnClickListener {
            mPositiveButton.isClickable = false
            mPositiveButton.text = "Loading..."

            lifecycleScope.launch(Dispatchers.IO) {
                val audio = ElevenLabsService().textToSpeech(selectedText)
                val path = File("$cacheDir/musicfile")

                val fos = FileOutputStream(path)
                fos.write(audio)
                fos.close()

                val mp = MediaPlayer()
                mp.setDataSource("$cacheDir/musicfile");
                mp.prepare()
                mp.start()

                withContext(Dispatchers.Main) {
                    mPositiveButton.isClickable = true
                    mPositiveButton.text = "Play"
                }
            }
        }
    }
}