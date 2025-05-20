package com.example.proyectofinalandroid.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream
import androidx.core.content.FileProvider
import android.os.Build
import androidx.core.net.toUri
import android.graphics.ImageDecoder
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


fun base64ToBitmap(base64String: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun base64ToImageBitmap(base64: String): ImageBitmap? {
    return try {
        val cleanBase64 = base64.substringAfter("base64,") // Por si lleva prefijo
        val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun getImageBitmapSafely(base64String: String): ImageBitmap? {
    return try {
        base64ToImageBitmap(base64String)
    } catch (e: Exception) {
        Log.e("ProfileScreen", "Error al convertir imagen: ${e.message}")
        null
    }
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}


fun createTempImageFile(context: Context): File {
    val fileName = "temp_photo_${System.currentTimeMillis()}"
    val storageDir = context.cacheDir
    return File.createTempFile(fileName, ".jpg", storageDir)
}


fun createTempImageUri(context: Context): Uri {
    val tempFile = createTempImageFile(context)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}

fun resizeBitmap(bitmap: Bitmap, maxWidth: Int = 800, maxHeight: Int = 800): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    if (width <= maxWidth && height <= maxHeight) {
        return bitmap
    }

    val ratio = width.toFloat() / height.toFloat()

    val newWidth: Int
    val newHeight: Int

    if (width > height) {
        newWidth = maxWidth
        newHeight = (newWidth / ratio).toInt()
    } else {
        newHeight = maxHeight
        newWidth = (newHeight * ratio).toInt()
    }

    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
}

fun uriToBase64(context: Context, imageUri: Uri): String {
    return try {
        // Use BitmapFactory.Options to sample down the image during initial load
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true // Only decode image bounds without loading
        }

        // First pass to get dimensions without loading the full bitmap
        var inputStream = context.contentResolver.openInputStream(imageUri)
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()

        // Calculate sample size based on a target width/height (e.g., 1200px)
        val maxDimension = 1200
        val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, maxDimension)

        // Second pass with sampling to load a smaller bitmap
        val loadOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }

        inputStream = context.contentResolver.openInputStream(imageUri)
        val sampledBitmap = BitmapFactory.decodeStream(inputStream, null, loadOptions)
        inputStream?.close()

        // Further resize if necessary to ensure it's not too large
        val bitmap = sampledBitmap?.let {
            if (it.width > maxDimension || it.height > maxDimension) {
                resizeBitmap(it, maxDimension, maxDimension)
            } else {
                it
            }
        } ?: throw IllegalStateException("Failed to decode image")

        // Use JPEG with 85% quality for better compression
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)

        if (bitmap != sampledBitmap) {
            // Free memory if we created a new bitmap
            sampledBitmap.recycle()
        }

        val byteArray = outputStream.toByteArray()
        Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (e: Exception) {
        Log.e("ImageUtils", "Error converting image: ${e.message}", e)
        ""
    }
}

// Helper function to calculate appropriate sample size
private fun calculateSampleSize(width: Int, height: Int, targetSize: Int): Int {
    var sampleSize = 1

    if (width > targetSize || height > targetSize) {
        val halfWidth = width / 2
        val halfHeight = height / 2

        // Calculate the largest inSampleSize value that is a power of 2
        // and keeps both dimensions larger than the target size
        while ((halfWidth / sampleSize) >= targetSize || (halfHeight / sampleSize) >= targetSize) {
            sampleSize *= 2
        }
    }

    return sampleSize
}