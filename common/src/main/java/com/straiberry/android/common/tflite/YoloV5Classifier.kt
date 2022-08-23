/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/
package com.straiberry.android.common.tflite

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Wrapper for frozen detection models trained using the Tensorflow Object Detection API:
 * - https://github.com/tensorflow/models/tree/master/research/object_detection
 * where you can find the training code.
 *
 *
 * To use pretrained models in the API or convert to TF Lite models, please see docs for details:
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
 * - https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/running_on_mobile_tensorflowlite.md#running-our-model-on-android
 */
object YoloV5Classifier : Classifier {
    override fun enableStatLogging(debug: Boolean) {}
    override val statString: String
        get() = ""

    override fun close() {
        tfLite!!.close()
        tfLite = null
        if (gpuDelegate != null) {
            gpuDelegate!!.close()
            gpuDelegate = null
        }
        if (nnapiDelegate != null) {
            nnapiDelegate!!.close()
            nnapiDelegate = null
        }
        tfliteModel = null
    }

    private fun recreateInterpreter() {
        if (tfLite != null) {
            tfLite!!.close()
            tfLite = Interpreter(tfliteModel!!, tfliteOptions)
        }
    }

    fun useGpu() {
        if (gpuDelegate == null) {
            gpuDelegate = GpuDelegate()
            tfliteOptions.addDelegate(gpuDelegate)
            recreateInterpreter()
        }
    }

    fun useCPU() {
        recreateInterpreter()
    }

    fun useNNAPI() {
        nnapiDelegate = NnApiDelegate()
        tfliteOptions.addDelegate(nnapiDelegate)
        recreateInterpreter()
    }

    override val objThresh: Float
        get() = 0.9f

    // Float model
    private const val IMAGE_MEAN = 0f
    private const val IMAGE_STD = 255.0f

    //config yolo
    var inputSize = -1
        private set

    private var output_box = 0
    private var isModelQuantized = false

    /** holds a gpu delegate  */
    var gpuDelegate: GpuDelegate? = null

    /** holds an nnapi delegate  */
    var nnapiDelegate: NnApiDelegate? = null

    /** The loaded TensorFlow Lite model.  */
    private var tfliteModel: MappedByteBuffer? = null

    /** Options for configuring the Interpreter.  */
    private val tfliteOptions = Interpreter.Options()

    // Config values.
    // Pre-allocated buffers.
    private val labels = Vector<String>()
    private var intValues: IntArray = intArrayOf()
    private var imgData: ByteBuffer? = null
    private var outData: ByteBuffer? = null
    private var tfLite: Interpreter? = null
    private var inp_scale = 0f
    private var inp_zero_point = 0
    private var oup_scale = 0f
    private var oup_zero_point = 0
    private var numClass = 0

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager  The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     * @param isQuantized   Boolean representing model is quantized or not
     */
    @Throws(IOException::class)
    fun create(
        assetManager: AssetManager,
        modelFilename: String?,
        labelFilename: String,
        isQuantized: Boolean,
        inputSize: Int
    ): YoloV5Classifier {
        val d = YoloV5Classifier
        val actualFilename = labelFilename.split("file:///android_asset/").toTypedArray()[1]
        val labelsInput = assetManager.open(actualFilename)
        val br = BufferedReader(InputStreamReader(labelsInput))
        var line: String?
        while (br.readLine().also { line = it }.isNullOrBlank().not()) {
            //LOGGER.w(line);
            d.labels.add(line)
        }
        br.close()
        try {
            val options = Interpreter.Options()
            options.numThreads = NUM_THREADS
            if (isNNAPI) {
                d.nnapiDelegate = null
                // Initialize interpreter with NNAPI delegate for Android Pie or above
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    d.nnapiDelegate = NnApiDelegate()
                    options.addDelegate(d.nnapiDelegate)
                    options.numThreads = NUM_THREADS

                    options.useNNAPI = true
                }
            }
            if (isGPU) {
                val gpuOptions = GpuDelegate.Options()
                gpuOptions.setPrecisionLossAllowed(true) // It seems that the default is true
                gpuOptions.setInferencePreference(GpuDelegate.Options.INFERENCE_PREFERENCE_SUSTAINED_SPEED)
                d.gpuDelegate = GpuDelegate(gpuOptions)
                options.addDelegate(d.gpuDelegate)
            }
            d.tfliteModel = loadModelFile(assetManager, modelFilename)
            d.tfLite = Interpreter(d.tfliteModel!!, options)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        d.isModelQuantized = isQuantized
        // Pre-allocate buffers.
        val numBytesPerChannel: Int = if (isQuantized) {
            1 // Quantized
        } else {
            4 // Floating point
        }
        d.inputSize = inputSize
        d.imgData =
            ByteBuffer.allocateDirect(1 * d.inputSize * d.inputSize * 3 * numBytesPerChannel)
        d.imgData!!.order(ByteOrder.nativeOrder())
        val shape = d.tfLite!!.getOutputTensor(0).shape()
        d.intValues = IntArray(d.inputSize * d.inputSize)
        d.output_box = shape[1]

        if (d.isModelQuantized) {
            val inpten = d.tfLite!!.getInputTensor(0)
            d.inp_scale = inpten.quantizationParams().scale
            d.inp_zero_point = inpten.quantizationParams().zeroPoint
            val oupten = d.tfLite!!.getOutputTensor(0)
            d.oup_scale = oupten.quantizationParams().scale
            d.oup_zero_point = oupten.quantizationParams().zeroPoint
        }
        val numClass = shape[shape.size - 1] - 5
        d.numClass = numClass
        d.outData =
            ByteBuffer.allocateDirect(d.output_box * (numClass + 5) * numBytesPerChannel)
        d.outData!!.order(ByteOrder.nativeOrder())
        return d
    }

    //non maximum suppression
    fun nms(list: ArrayList<Classifier.Recognition>): ArrayList<Classifier.Recognition?> {
        val nmsList = ArrayList<Classifier.Recognition?>()
        for (k in labels.indices) {
            //1.find max confidence per class
            val pq = PriorityQueue<Classifier.Recognition?>(
                50
            ) { lhs, rhs -> // Intentionally reversed to put high confidence at the head of the queue.
                (rhs.confidence!!).compareTo(lhs.confidence!!)
            }
            for (i in list.indices) {
                if (list[i].detectedClass == k) {
                    pq.add(list[i])
                }
            }

            //2.do non maximum suppression
            while (pq.size > 0) {
                //insert detection with max confidence
                val a = arrayOfNulls<Classifier.Recognition>(pq.size)
                val detections = pq.toArray(a)
                val max = detections[0]
                nmsList.add(max)
                pq.clear()
                for (j in 1 until detections.size) {
                    val detection = detections[j]
                    val b = detection!!.getLocation()
                    if (boxIou(max!!.getLocation(), b) < mNmsThresh) {
                        pq.add(detection)
                    }
                }
            }
        }
        return nmsList
    }

    var mNmsThresh = 0.6f
    private fun boxIou(a: RectF, b: RectF): Float {
        return boxIntersection(a, b) / boxUnion(a, b)
    }

    private fun boxIntersection(a: RectF, b: RectF): Float {
        val w = overlap(
            (a.left + a.right) / 2, a.right - a.left,
            (b.left + b.right) / 2, b.right - b.left
        )
        val h = overlap(
            (a.top + a.bottom) / 2, a.bottom - a.top,
            (b.top + b.bottom) / 2, b.bottom - b.top
        )
        return if (w < 0 || h < 0) 0F else w * h
    }

    private fun boxUnion(a: RectF, b: RectF): Float {
        val i = boxIntersection(a, b)
        return (a.right - a.left) * (a.bottom - a.top) + (b.right - b.left) * (b.bottom - b.top) - i
    }

    fun overlap(x1: Float, w1: Float, x2: Float, w2: Float): Float {
        val l1 = x1 - w1 / 2
        val l2 = x2 - w2 / 2
        val left = if (l1 > l2) l1 else l2
        val r1 = x1 + w1 / 2
        val r2 = x2 + w2 / 2
        val right = if (r1 < r2) r1 else r2
        return right - left
    }

    /**
     * Writes Image data into a `ByteBuffer`.
     */
    fun convertBitmapToByteBuffer(bitmap: Bitmap?): ByteBuffer? {
        bitmap!!.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        imgData!!.rewind()
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val pixelValue = intValues[i * inputSize + j]
                if (isModelQuantized) {
                    // Quantized model
                    imgData!!.put(
                        (((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD / inp_scale + inp_zero_point).toInt()
                            .toByte()
                    )
                    imgData!!.put(
                        (((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD / inp_scale + inp_zero_point).toInt()
                            .toByte()
                    )
                    imgData!!.put(
                        (((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD / inp_scale + inp_zero_point).toInt()
                            .toByte()
                    )
                } else { // Float model
                    imgData!!.putFloat(((pixelValue shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData!!.putFloat(((pixelValue shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                    imgData!!.putFloat(((pixelValue and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                }
            }
        }
        return imgData
    }

    override fun recognizeImage(bitmap: Bitmap?): ArrayList<Classifier.Recognition?> {
        convertBitmapToByteBuffer(bitmap)
        val outputMap: MutableMap<Int, Any?> =
            HashMap()

        outData!!.rewind()
        outputMap[0] = outData
        Log.d("YoloV5Classifier", "mObjThresh: $objThresh")
        val inputArray = arrayOf<Any?>(imgData)
        tfLite!!.runForMultipleInputsOutputs(inputArray, outputMap)
        val byteBuffer = outputMap[0] as ByteBuffer?
        byteBuffer!!.rewind()
        val detections =
            ArrayList<Classifier.Recognition>()
        val out =
            Array(1) {
                Array(output_box) { FloatArray(numClass + 5) }
            }
        Log.d("YoloV5Classifier", "out[0] detect start")
        for (i in 0 until output_box) {
            for (j in 0 until numClass + 5) {
                if (isModelQuantized) {
                    out[0][i][j] =
                        oup_scale * ((byteBuffer.get().toInt() and 0xFF) - oup_zero_point)
                } else {
                    out[0][i][j] = byteBuffer.float
                }
            }
            // Denormalize xywh
            for (j in 0..3) {
                out[0][i][j] *= inputSize.toFloat()
            }
        }
        for (i in 0 until output_box) {
            val offset = 0
            val confidence = out[0][i][4]
            var detectedClass = -1
            var maxClass = 0f
            val classes = FloatArray(labels.size)
            for (c in labels.indices) {
                try {
                    classes[c] = out[0][i][5 + c]
                } catch (e: Exception) {

                }
            }
            for (c in labels.indices) {
                if (classes[c] > maxClass) {
                    detectedClass = c
                    maxClass = classes[c]
                }
            }
            val confidenceInClass = maxClass * confidence
            if (confidenceInClass > objThresh) {
                val xPos = out[0][i][0]
                val yPos = out[0][i][1]
                val w = out[0][i][2]
                val h = out[0][i][3]
                Log.d(
                    "YoloV5Classifier",
                    "$xPos,$yPos,$w,$h"
                )
                val rect = RectF(
                    max(0f, xPos - w / 2),
                    max(0f, yPos - h / 2),
                    min((bitmap!!.width - 1).toFloat(), xPos + w / 2),
                    min((bitmap.height - 1).toFloat(), yPos + h / 2)
                )
                detections.add(
                    Classifier.Recognition(
                        "" + offset, labels[detectedClass],
                        confidenceInClass, rect, detectedClass
                    )
                )
            }
        }
        imgData!!.clear()
        Log.d("YoloV5Classifier", "detect end")
        //        final ArrayList<Recognition> recognitions = detections;
        return nms(detections)
    }

    fun checkInvalidateBox(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        oriW: Float,
        oriH: Float,
        inputSize: Int
    ): Boolean {
        // (1) (x, y, w, h) --> (xmin, ymin, xmax, ymax)
        val halfHeight = height / 2.0f
        val halfWidth = width / 2.0f
        val priedCor = floatArrayOf(x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight)

        // (2) (xmin, ymin, xmax, ymax) -> (xmin_org, ymin_org, xmax_org, ymax_org)
        val resizeRatioW = 1.0f * inputSize / oriW
        val resizeRatioH = 1.0f * inputSize / oriH
        val resizeRatio = if (resizeRatioW > resizeRatioH) resizeRatioH else resizeRatioW //min
        val dw = (inputSize - resizeRatio * oriW) / 2
        val dh = (inputSize - resizeRatio * oriH) / 2
        priedCor[0] = 1.0f * (priedCor[0] - dw) / resizeRatio
        priedCor[2] = 1.0f * (priedCor[2] - dw) / resizeRatio
        priedCor[1] = 1.0f * (priedCor[1] - dh) / resizeRatio
        priedCor[3] = 1.0f * (priedCor[3] - dh) / resizeRatio

        // (3) clip some boxes those are out of range
        priedCor[0] = (if (priedCor[0] > 0) priedCor[0] else 0) as Float
        priedCor[1] = (if (priedCor[1] > 0) priedCor[1] else 0) as Float
        priedCor[2] = if (priedCor[2] < oriW - 1) priedCor[2] else oriW - 1
        priedCor[3] = if (priedCor[3] < oriH - 1) priedCor[3] else oriH - 1
        if (priedCor[0] > priedCor[2] || priedCor[1] > priedCor[3]) {
            priedCor[0] = 0F
            priedCor[1] = 0F
            priedCor[2] = 0F
            priedCor[3] = 0F
        }

        // (4) discard some invalid boxes
        val temp1 = priedCor[2] - priedCor[0]
        val temp2 = priedCor[3] - priedCor[1]
        val temp = temp1 * temp2
        if (temp < 0) {
            Log.e("checkInvalidateBox", "temp < 0")
            return false
        }
        if (sqrt(temp.toDouble()) > Float.MAX_VALUE) {
            Log.e("checkInvalidateBox", "temp max")
            return false
        }
        return true
    }

    /**
     * Memory-map the model file in Assets.
     */
    @Throws(IOException::class)
    fun loadModelFile(assets: AssetManager, modelFilename: String?): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFilename!!)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }


    // Number of threads in the java app
    private const val NUM_THREADS = 1
    private const val isNNAPI = false
    private const val isGPU = false

}