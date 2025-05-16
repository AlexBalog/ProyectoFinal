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

fun uriToBase64(context: Context, imageUri: Uri): String {
    val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, imageUri)
        ImageDecoder.decodeBitmap(source)
    } else {
        MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
    }

    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
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