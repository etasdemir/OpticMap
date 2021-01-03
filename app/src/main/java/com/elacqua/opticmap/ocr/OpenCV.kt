package com.elacqua.opticmap.ocr

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.MSER
import org.opencv.imgproc.Imgproc

class OpenCV {

    private lateinit var imageMat: Mat
    private lateinit var imageMat2: Mat

    fun getText(bitmap: Bitmap, mat1: Mat, mat2: Mat): Bitmap {
        imageMat = mat1
        imageMat2 = mat2

        Utils.bitmapToMat(bitmap, imageMat)
        detectText(imageMat)
        val newBitmap: Bitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(imageMat, newBitmap)
        return newBitmap
    }

    private fun detectText(mat: Mat) {
        Imgproc.cvtColor(imageMat, imageMat2, Imgproc.COLOR_RGB2GRAY)
        val mGray = imageMat2

        val CONTOUR_COLOR = Scalar(1.0, 255.0, 128.0, 0.0)
        val keyPoint = MatOfKeyPoint()
        var kPoint: KeyPoint
        val mask = Mat.zeros(mGray.size(), CvType.CV_8UC1)
        var rectanx1: Int
        var rectany1: Int
        var rectanx2: Int
        var rectany2: Int

        val zeros = Scalar(0.0, 0.0, 0.0)
        val contour2 = ArrayList<MatOfPoint>()
        val kernel = Mat(1, 50, CvType.CV_8UC1, Scalar.all(255.0))
        val morByte = Mat()
        val hierarchy = Mat()

        var rectan3: Rect
        val imgSize = mat.height() * mat.width()

//        val detector = ORB.create()
//        detector.detect(mGray, keyPoint)
        val detector = MSER.create()
        detector.detect(mGray, keyPoint)

        val listPoint = keyPoint.toArray()
        for (element in listPoint) {
            kPoint = element
            rectanx1 = (kPoint.pt.x - 0.5 * kPoint.size).toInt()
            rectany1 = (kPoint.pt.y - 0.5 * kPoint.size).toInt()

            rectanx2 = (kPoint.size).toInt()
            rectany2 = (kPoint.size).toInt()
            if (rectanx1 <= 0) {
                rectanx1 = 1
            }
            if (rectany1 <= 0) {
                rectany1 = 1
            }
            if ((rectanx1 + rectanx2) > mGray.width()) {
                rectanx2 = mGray.width() - rectanx1
            }
            if ((rectany1 + rectany2) > mGray.height()) {
                rectany2 = mGray.height() - rectany1
            }
            val rectant = Rect(rectanx1, rectany1, rectanx2, rectany2)
            val roi = Mat(mask, rectant)
            roi.setTo(CONTOUR_COLOR)
        }
        Imgproc.morphologyEx(mask, morByte, Imgproc.MORPH_DILATE, kernel)
        Imgproc.findContours(
            morByte,
            contour2,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_NONE
        )
        for (j in 0 until contour2.size) {
            rectan3 = Imgproc.boundingRect(contour2[j])
            if (rectan3.area() > 0.5 * imgSize || rectan3.area() < 100 || rectan3.width / rectan3.height < 2) {
                val roi = Mat(morByte, rectan3)
                roi.setTo(zeros)
            } else {
                Imgproc.rectangle(mat, rectan3.br(), rectan3.tl(), CONTOUR_COLOR)
            }
        }
    }
}