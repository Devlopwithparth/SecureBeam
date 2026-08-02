package com.securebeam.app.qr

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.securebeam.app.protocol.FramePacket

class CameraFrameAnalyzer(
    private val onFrameDecoded: (FramePacket) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader()

    override fun analyze(image: ImageProxy) {
        val buffer = image.planes[0].buffer
        val data = ByteArray(buffer.remaining())
        buffer.get(data)

        val width = image.width
        val height = image.height

        val source = PlanarYUVLuminanceSource(
            data, width, height, 0, 0, width, height, false
        )
        val bitmap = BinaryBitmap(HybridBinarizer(source))

        try {
            val result = reader.decodeWithState(bitmap)
            val decodedText = result.text
            val packet = FramePacket.deserialize(decodedText)
            if (packet != null) {
                onFrameDecoded(packet)
            }
        } catch (e: NotFoundException) {
            // Frame did not contain a readable QR code
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            reader.reset()
            image.close()
        }
    }
}
