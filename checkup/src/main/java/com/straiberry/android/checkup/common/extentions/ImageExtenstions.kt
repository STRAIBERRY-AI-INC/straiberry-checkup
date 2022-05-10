package com.straiberry.android.checkup.common.extentions

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import com.straiberry.android.checkup.R
import com.straiberry.android.checkup.common.helper.Variance
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.*
import java.nio.ByteBuffer
import kotlin.math.abs
import kotlin.math.min


private const val DARKNESS_THRESHOLD = 90

fun Image.toBitmap(): Bitmap? {
    val planes: Array<Image.Plane> = this.planes
    val yBuffer: ByteBuffer = planes[0].buffer
    val uBuffer: ByteBuffer = planes[1].buffer
    val vBuffer: ByteBuffer = planes[2].buffer
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    val nv21 = ByteArray(ySize + uSize + vSize)
    //U and V are swapped
    yBuffer[nv21, 0, ySize]
    vBuffer[nv21, ySize, vSize]
    uBuffer[nv21, ySize + vSize, uSize]
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

@SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
fun ImageProxy.toBitmap(): Bitmap? {
    val planeProxy = image!!.planes[0]
    val buffer: ByteBuffer = planeProxy.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}

fun Bitmap.compressBitmap(
    context: Context,
    rotation: Int
): Bitmap {
    val fos: OutputStream?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

        val resolver: ContentResolver = context.contentResolver

        val contentValues = ContentValues().apply {
            put(
                MediaStore.MediaColumns.DISPLAY_NAME,
                System.currentTimeMillis().toString()
            )
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                "DCIM/${context.getString(R.string.app_name)}"
            )
        }
        val imageUri =
            resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = resolver.openOutputStream(imageUri!!)
    } else {
        val imageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM
        ).toString() + File.separator + context.getString(R.string.app_name)
        val file = File(imageDir)
        if (!file.exists())
            file.mkdir()
        val image = File(imageDir, System.currentTimeMillis().toString() + "jpg")
        fos = FileOutputStream(image)
    }
    val savedBitmap = Bitmap.createBitmap(
        this.correctRotation(rotation),
        0,
        0,
        this.width,
        this.height,
    )
    savedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
    fos?.flush()
    fos?.close()
    return savedBitmap
}

fun Bitmap.resize(maxWidth: Int, maxHeight: Int): Bitmap? {
    var image = this
    return if (maxHeight > 0 && maxWidth > 0) {
        val width = image.width
        val height = image.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
        var finalWidth = maxWidth
        var finalHeight = maxHeight
        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }
        image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
        image
    } else {
        image
    }
}


fun Bitmap.finalCropOnCapturedJaw(
    normalizerLocation: RectF
): Bitmap {
    val correctRotation = this

    val mX = normalizerLocation.left
    val mY = normalizerLocation.top
    val mW = (normalizerLocation.right)
    val mH = (normalizerLocation.bottom)

    val size = (if (mW > mH) mW else mH)
    val newX = (mX + mW / 2) - size / 2

    val newY = (mY + mH / 2) - size / 2

    return Bitmap.createBitmap(
        correctRotation,
        (newX).toInt(),
        (newY).toInt(),
        (size - newX).toInt(),
        (size - newX).toInt(),
    )
}


fun Bitmap.correctRotation(rotation: Int): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(rotation.toFloat())
    return Bitmap.createBitmap(this, 0, 0, this.width, this.height, matrix, false)
}

fun Uri.getImage(context: Context): Bitmap =
    BitmapFactory.decodeStream(
        context.contentResolver.openInputStream(this)
    )


/**
 * Returns a transformation matrix from one reference frame into another. Handles cropping (if
 * maintaining aspect ratio is desired) and rotation.
 *
 * @param srcWidth Width of source frame.
 * @param srcHeight Height of source frame.
 * @param dstWidth Width of destination frame.
 * @param dstHeight Height of destination frame.
 * @param applyRotation Amount of rotation to apply from one frame to another. Must be a multiple
 * of 90.
 * @param maintainAspectRatio If true, will ensure that scaling in x and y remains constant,
 * cropping the image if necessary.
 * @return The transformation fulfilling the desired requirements.
 */
fun getTransformationMatrix(
    srcWidth: Int,
    srcHeight: Int,
    dstWidth: Int,
    dstHeight: Int,
    applyRotation: Int,
    maintainAspectRatio: Boolean
): Matrix {
    val matrix = Matrix()
    if (applyRotation != 0) {
        // Translate so center of image is at origin.
        matrix.postTranslate(-srcWidth / 2.0f, -srcHeight / 2.0f)

        // Rotate around origin.
        matrix.postRotate(applyRotation.toFloat())
    }

    // Account for the already applied rotation, if any, and then determine how
    // much scaling is needed for each axis.
    val transpose = (Math.abs(applyRotation) + 90) % 180 == 0
    val inWidth = if (transpose) srcHeight else srcWidth
    val inHeight = if (transpose) srcWidth else srcHeight

    // Apply scaling if necessary.
    if (inWidth != dstWidth || inHeight != dstHeight) {
        val scaleFactorX = dstWidth / inWidth.toFloat()
        val scaleFactorY = dstHeight / inHeight.toFloat()
        if (maintainAspectRatio) {
            // Scale by minimum factor so that dst is filled completely while
            // maintaining the aspect ratio. Some image may fall off the edge.
            val scaleFactor = Math.max(scaleFactorX, scaleFactorY)
            matrix.postScale(scaleFactor, scaleFactor)
        } else {
            // Scale exactly to fill dst from src.
            matrix.postScale(scaleFactorX, scaleFactorY)
        }
    }
    if (applyRotation != 0) {
        // Translate back from origin centered reference to destination frame.
        matrix.postTranslate(dstWidth / 2.0f, dstHeight / 2.0f)
    }
    return matrix
}


fun Bitmap.convertToFile(context: Context): File {
    val file = File(context.cacheDir, System.currentTimeMillis().toString() + ".jpg").apply {
        createNewFile()
    }
    val bos = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 60, bos)

    //write the bytes in file
    FileOutputStream(file).apply {
        write(bos.toByteArray())
        flush()
        close()
    }

    return file
}

fun Bitmap.compressToBitmap(context: Context): Bitmap {
    val file = File(context.cacheDir, System.currentTimeMillis().toString() + ".jpg").apply {
        createNewFile()
    }
    val bos = ByteArrayOutputStream()
    val savedBitmap = Bitmap.createBitmap(
        this, 0, 0, this.width, this.height
    )
    savedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
    //write the bytes in file
    FileOutputStream(file).apply {
        write(bos.toByteArray())
        flush()
        close()
    }
    return savedBitmap
}

/**
 * Helper function used to map the coordinates for objects coming out of
 * the model into the coordinates that the user sees on the screen.
 */
fun mapOutputCoordinates(
    location: RectF,
    rotationDegrees: Int,
    imageWidth: Int,
    imageHeight: Int,
    modelInputSize: Int,
    camera: PreviewView?,
    width: Int,
    height: Int
): RectF {
    val frameToCropTransform = getTransformationMatrix(
        width,
        height,
        modelInputSize,
        modelInputSize,
        rotationDegrees,
        false
    )

    val cropToFrameTransform = Matrix()
    frameToCropTransform.invert(cropToFrameTransform)

    cropToFrameTransform.mapRect(location)
    val rotated = rotationDegrees % 180 == 90
    val multiplier = min(
        imageHeight / (if (rotated) width else height).toFloat(),
        imageWidth / (if (rotated) height else width).toFloat()
    )
    val frameToCanvasMatrix = getTransformationMatrix(
        width,
        height,
        (multiplier * if (rotated) height else width).toInt(),
        (multiplier * if (rotated) width else height).toInt(),
        rotationDegrees,
        false
    )
    frameToCanvasMatrix.mapRect(location)
    return location
}


fun mapOutputCoordinates(location: RectF, viewFinder: PreviewView): RectF {

    // Step 1: map location to the preview coordinates
    val previewLocation = RectF(
        location.left * viewFinder.width,
        location.top * viewFinder.height,
        location.right * viewFinder.width,
        location.bottom * viewFinder.height
    )

    // Step 2: compensate for camera sensor orientation and mirroring
    val isFrontFacing = false
    val correctedLocation = if (isFrontFacing) {
        RectF(
            viewFinder.width - previewLocation.right,
            previewLocation.top,
            viewFinder.width - previewLocation.left,
            previewLocation.bottom
        )
    } else {
        previewLocation
    }

    // Step 3: compensate for 1:1 to 4:3 aspect ratio conversion + small margin
    val margin = 0.3f

    val requestedRatio = 4f / 3f
    val midX = (correctedLocation.left + correctedLocation.right) / 2f
    val midY = (correctedLocation.top + correctedLocation.bottom) / 2f
    return if (viewFinder.width < viewFinder.height) {
        RectF(
            midX - (1f + margin) * requestedRatio * correctedLocation.width() / 2f,
            midY - (1f - margin) * correctedLocation.height() / 2f,
            midX + (1f + margin) * requestedRatio * correctedLocation.width() / 2f,
            midY + (1f - margin) * correctedLocation.height() / 2f
        )
    } else {
        RectF(
            midX - (1f - margin) * correctedLocation.width() / 2f,
            midY - (1f + margin) * requestedRatio * correctedLocation.height() / 2f,
            midX + (1f - margin) * correctedLocation.width() / 2f,
            midY + (1f + margin) * requestedRatio * correctedLocation.height() / 2f
        )
    }
}


@Throws(IOException::class)
fun Bitmap.modifyOrientation(image_absolute_path: String): Bitmap? {
    val ei = ExifInterface(image_absolute_path)
    return when (ei.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotate(this, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotate(this, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotate(this, 270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flip(
            this,
            horizontal = true,
            vertical = false
        )
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> flip(this, horizontal = false, vertical = true)
        else -> this
    }
}

fun rotate(bitmap: Bitmap, degrees: Float): Bitmap? {
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap? {
    val matrix = Matrix()
    matrix.preScale(if (horizontal) -1f else 1f, if (vertical) -1f else 1f)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun Uri.getPath(context: Context): String {
    var result: String? = null
    val proj = arrayOf(MediaStore.Images.Media.DATA)
    val cursor: Cursor? = context.contentResolver.query(this, proj, null, null, null)
    if (cursor != null) {
        if (cursor.moveToFirst()) {
            val columnIndex: Int = cursor.getColumnIndexOrThrow(proj[0])
            result = cursor.getString(columnIndex)
        }
        cursor.close()
    }
    if (result == null) {
        result = "Not found"
    }
    return result
}

fun String.getFlagIcon(context: Context): Int {
    return context.resources.getIdentifier(
        "country_flag_$this",
        "drawable",
        context.packageName
    )
}

fun Bitmap.isBlurry(): Boolean {
    val options = BitmapFactory.Options()
    options.inDither = true
    options.inPreferredConfig = Bitmap.Config.ARGB_8888
    val image: Bitmap = this
    val l = CvType.CV_8UC1 //8-bit grey scale image
    val matImage = Mat()
    Utils.bitmapToMat(image, matImage)
    val matImageGrey = Mat()
    Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY)
    val dst2 = Mat()
    Utils.bitmapToMat(image, dst2)
    val laplacianImage = Mat()
    dst2.convertTo(laplacianImage, l)
    Imgproc.Laplacian(matImageGrey, laplacianImage, CvType.CV_8U)
    val laplacianImage8bit = Mat()
    laplacianImage.convertTo(laplacianImage8bit, l)
    val bmp = Bitmap.createBitmap(
        laplacianImage8bit.cols(),
        laplacianImage8bit.rows(),
        Bitmap.Config.ARGB_8888
    )
    Utils.matToBitmap(laplacianImage8bit, bmp)
    val pixels = IntArray(bmp.height * bmp.width)
    bmp.getPixels(pixels, 0, bmp.width, 0, 0, bmp.width, bmp.height) // bmp为轮廓图
    var maxLap = -16777216 // 16m
    for (pixel: Int in pixels) {
        if (pixel > maxLap) maxLap = pixel
    }
    val soglia = -6776680
    if (maxLap <= soglia) {
        println("is blur image")
    }

    return maxLap <= soglia
}


fun Bitmap.calculateBrightnessEstimate(pixelSpacing: Int): Boolean {
    var R = 0
    var G = 0
    var B = 0
    val height = this.height
    val width = this.width
    var n = 0
    val pixels = IntArray(width * height)
    this.getPixels(pixels, 0, width, 0, 0, width, height)
    var i = 0
    while (i < pixels.size) {
        val color = pixels[i]
        R += Color.red(color)
        G += Color.green(color)
        B += Color.blue(color)
        n++
        i += pixelSpacing
    }
    return abs((R + B + G) / (n * 3)) < DARKNESS_THRESHOLD
}

fun Bitmap.runBlurDetection(): Boolean {
    val height = this.height
    val width = this.width

    // Make image gray
    val grayImage = getGrayScaleBitmap(this)
    val gray = IntArray(width * height)
    grayImage.getPixels(gray, 0, width, 0, 0, width, height)

    // original picture
    val pixels = IntArray(width * height)
    this.getPixels(pixels, 0, width, 0, 0, width, height)

    val kernel = arrayOf(
        arrayOf(0, 0, -1, 0, 0),
        arrayOf(0, -1, -2, -1, 0),
        arrayOf(-1, -2, 16, -2, -1),
        arrayOf(0, -1, -2, -1, 0),
        arrayOf(0, 0, -1, 0, 0)
    )

    for (i in 2..width - 2) {
        for (j in 2..height - 2)
            pixels[i + j * width] =
                kernel.mapIndexed { index, ints -> ints[index] * gray[i + j * width] }.sum()

    }
    Log.e(
        "VARIANCE=", Variance(pixels).variance.toString()
    )
    return Variance(pixels).variance < 2
}

fun getGrayScaleBitmap(original: Bitmap): Bitmap {
    // You have to make the Bitmap mutable when changing the config because there will be a crash
    // That only mutable Bitmap's should be allowed to change config.
    val bmp = original.copy(Bitmap.Config.ARGB_8888, true)
    val bmpGrayscale = Bitmap.createBitmap(bmp.width, bmp.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmpGrayscale)
    val paint = Paint()
    val colorMatrix = ColorMatrix()
    colorMatrix.setSaturation(0f)
    val colorMatrixFilter = ColorMatrixColorFilter(colorMatrix)
    paint.colorFilter = colorMatrixFilter
    canvas.drawBitmap(bmp, 0F, 0F, paint)
    return bmpGrayscale
}