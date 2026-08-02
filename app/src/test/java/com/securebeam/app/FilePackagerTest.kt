package com.securebeam.app

import com.securebeam.app.protocol.FilePackager
import com.securebeam.app.protocol.FramePacket
import com.securebeam.app.protocol.PacketReassembler
import com.securebeam.app.protocol.ReassemblyState
import org.junit.Assert.*
import org.junit.Test

class FilePackagerTest {

    @Test
    fun testFilePackagingAndReassembly() {
        val fileName = "test_document.txt"
        val originalText = "SecureBeam Packetization and Lost Frame Recovery Protocol Test Payload String with Extra Length to force multiple frame splits across binary chunk boundaries."
        val originalBytes = originalText.toByteArray(Charsets.UTF_8)

        // Package into frame packets with small chunk size to produce multiple packets
        val packets = FilePackager.packageFile(fileName, originalBytes, chunkSize = 32)
        assertTrue(packets.isNotEmpty())

        val reassembler = PacketReassembler()
        var finalState: ReassemblyState? = null

        // Feed packets to reassembler (scrambled order to test out-of-order packet recovery)
        val shuffledPackets = packets.shuffled()
        for (packet in shuffledPackets) {
            val serialized = packet.serialize()
            val deserialized = FramePacket.deserialize(serialized)
            assertNotNull(deserialized)

            finalState = reassembler.processFrame(deserialized!!)
        }

        assertTrue(finalState is ReassemblyState.Completed)
        val completed = finalState as ReassemblyState.Completed

        assertEquals(fileName, completed.fileName)
        val reconstructedText = String(completed.fileBytes, Charsets.UTF_8)
        assertEquals(originalText, reconstructedText)
    }
}
