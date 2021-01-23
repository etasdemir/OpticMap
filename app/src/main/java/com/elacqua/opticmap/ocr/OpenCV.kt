package com.elacqua.opticmap.ocr

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.MSER
import org.opencv.imgproc.Imgproc

class OpenCV {


    fun getBitmap(bitmap: Bitmap): Bitmap {
        val imageMat = Mat()
        Utils.bitmapToMat(bitmap, imageMat)
        detectText(imageMat)
        val newBitmap: Bitmap = bitmap.copy(bitmap.config, true)
        Utils.matToBitmap(imageMat, newBitmap)
        imageMat.release()
        return newBitmap
    }

    fun getBitmap(mat: Mat): Bitmap {
        detectText(mat)
        val conf = Bitmap.Config.ARGB_8888
        val bitmap: Bitmap = Bitmap.createBitmap(mat.width(), mat.height(), conf)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }

    fun getMat(mat: Mat): Mat {
        detectText(mat)
        return mat
    }

    private fun detectText(mat: Mat) {
        val CONTOUR_COLOR = Scalar(1.0, 255.0, 128.0, 0.0)
        val keyPoint = MatOfKeyPoint()
        var kPoint: KeyPoint
        var rectanx1: Int
        var rectany1: Int
        var rectanx2: Int
        var rectany2: Int
        val zeros = Scalar(0.0, 0.0, 0.0)
        var rectan3: Rect
        val imgSize = mat.height() * mat.width()

//        val detector = ORB.create()
//        detector.detect(mGray, keyPoint)

        val detector = MSER.create()

        val mGray = Mat(mat.size(),mat.type())
        Imgproc.cvtColor(mat, mGray, Imgproc.COLOR_RGBA2GRAY)

        //Imgproc.adaptiveThreshold(mGray,mGray,255.0,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY_INV,3,2.0)
        Imgproc.threshold(mGray, mGray, 127.0, 255.0, Imgproc.THRESH_OTSU)
        Imgproc.Canny(mGray,mGray,-80.0,-100.0)
        detector.detect(mGray, keyPoint)

        detector.clear()

        val mask = Mat.zeros(mGray.size(), CvType.CV_8UC1)
        val listPoint = keyPoint.toArray()

        keyPoint.release()

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
            roi.release()
        }

        mGray.release()

        val morByte = Mat()
        val kernel = Mat(1, 50, CvType.CV_8UC1, Scalar.all(255.0))
        val hierarchy = Mat()

        Imgproc.morphologyEx(mask, morByte, Imgproc.MORPH_DILATE, kernel)
        Imgproc.morphologyEx(mask, morByte, Imgproc.MORPH_OPEN, kernel)
        Imgproc.morphologyEx(mask, morByte, Imgproc.MORPH_ERODE, kernel)
        Imgproc.morphologyEx(mask, morByte, Imgproc.MORPH_CLOSE, kernel)

        mask.release()

        val contour2 = ArrayList<MatOfPoint>()

        Imgproc.findContours(
            morByte,
            contour2,
            hierarchy,
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_NONE
        )

        kernel.release()
        hierarchy.release()

        for (j in 0 until contour2.size) {
            rectan3 = Imgproc.boundingRect(contour2[j])
            if (rectan3.area() > 0.5 * imgSize || rectan3.area() < 100 || rectan3.width / rectan3.height < 2) {
                val roi = Mat(morByte, rectan3)
                roi.setTo(zeros)
                roi.release()
            } else {
                Imgproc.rectangle(mat, rectan3.br(), rectan3.tl(), CONTOUR_COLOR)
            }
        }
        morByte.release()
        for(i in contour2){
            i.release()
        }

    }
}