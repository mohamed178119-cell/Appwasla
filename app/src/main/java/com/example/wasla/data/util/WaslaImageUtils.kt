package com.example.wasla.data.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Base64
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object WaslaImageUtils {

    /**
     * Process an image picked by the user:
     * - Resizes to max dimension (e.g. 900px)
     * - Fixes EXIF orientation
     * - Saves to app internal cache
     * - Returns both local file path and base64 string for cloud transmission
     */
    fun processPickedImage(context: Context, uri: Uri): Pair<String, String>? {
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(uri) ?: return null
            val rawBytes = inputStream.readBytes()
            inputStream.close()

            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, boundsOptions)

            val maxDimension = 900
            val scale = calculateInSampleSize(boundsOptions, maxDimension, maxDimension)

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = scale
            }
            var bitmap = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOptions) ?: return null

            // Handle rotation if needed
            try {
                val exifStream = context.contentResolver.openInputStream(uri)
                if (exifStream != null) {
                    val exif = ExifInterface(exifStream)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    exifStream.close()
                    bitmap = rotateBitmap(bitmap, orientation)
                }
            } catch (_: Exception) { }

            // Compress to JPEG
            val byteStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteStream)
            val compressedBytes = byteStream.toByteArray()

            // Save to app internal cache
            val imagesDir = File(context.filesDir, "wasla_images").apply { if (!exists()) mkdirs() }
            val imageFile = File(imagesDir, "img_${UUID.randomUUID()}.jpg")
            FileOutputStream(imageFile).use { it.write(compressedBytes) }

            // Base64 for cloud transmission
            val base64 = Base64.encodeToString(compressedBytes, Base64.NO_WRAP)

            Pair(imageFile.absolutePath, base64)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save base64 image received from cloud into local file and return the file path
     */
    fun saveBase64Image(context: Context, base64: String): String? {
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val imagesDir = File(context.filesDir, "wasla_images").apply { if (!exists()) mkdirs() }
            val imageFile = File(imagesDir, "cloud_${UUID.randomUUID()}.jpg")
            FileOutputStream(imageFile).use { it.write(bytes) }
            imageFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            else -> return bitmap
        }
        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            bitmap
        }
    }
}
