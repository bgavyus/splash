package io.github.bgavyus.lightningcamera.storage

import android.content.ContentResolver
import android.content.Context
import android.media.MediaFormat
import android.os.Build
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.bgavyus.lightningcamera.R
import io.github.bgavyus.lightningcamera.common.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class Storage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentResolver: ContentResolver,
    private val clock: Clock
) {
    companion object {
        const val MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_AVC
        const val FILE_EXTENSION = "mp4"

        val isScoped
            get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !Environment.isExternalStorageLegacy()
    }

    init {
        Logger.info("Scoped? $isScoped")
    }

    suspend fun generateFile(): StorageFile {
        val timeString = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            .withZone(clock.zone)
            .format(clock.instant())

        return file(
            mimeType = MIME_TYPE,
            standardDirectory = StandardDirectory.Movies,
            appDirectoryName = context.getString(R.string.video_folder_name),
            name = "VID_$timeString.$FILE_EXTENSION"
        )
    }

    private suspend fun file(
        mimeType: String,
        standardDirectory: StandardDirectory,
        appDirectoryName: String,
        name: String
    ) = withContext(Dispatchers.IO) {
        if (isScoped) {
            ScopedStorageFile(
                contentResolver,
                clock,
                mimeType,
                standardDirectory,
                appDirectoryName,
                name
            )
        } else {
            LegacyStorageFile(contentResolver, mimeType, standardDirectory, appDirectoryName, name)
        }
    }
}