package io.github.bgavyus.splash.storage

import android.content.Context
import android.media.MediaFormat
import android.util.Log
import io.github.bgavyus.splash.R
import java.text.SimpleDateFormat
import java.util.*

class VideoFile(context: Context) {
    companion object {
        private val TAG = VideoFile::class.simpleName

        private const val VIDEO_MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC
        private const val VIDEO_FILE_EXTENSION = "mp4"
    }

    private val pendingFile = {
        val standardDirectory = StandardDirectory.Movies
        val appDirectoryName = context.getString(R.string.video_folder_name)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${context.getString(R.string.video_file_prefix)}_$timestamp.$VIDEO_FILE_EXTENSION"

        if (Storage.scoped) {
            PendingScopedStorageFile(context, VIDEO_MIME_TYPE, standardDirectory, appDirectoryName, fileName)
        } else {
            PendingLegacyStorageFile(context, VIDEO_MIME_TYPE, standardDirectory, appDirectoryName, fileName)
        }
    }()

    val descriptor = pendingFile.descriptor
    var contentValid = false

    fun close() {
        if (contentValid) {
            Log.i(TAG, "Saving video")
            pendingFile.save()
        } else {
            Log.i(TAG, "Discarding video")
            pendingFile.discard()
        }
    }
}