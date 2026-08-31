package com.example.paxcheck.hardware

import com.example.paxcheck.sdk.PaxSdkManager
import com.pax.dal.IDAL
import com.pax.dal.IMag
import com.pax.dal.IPrinter
import com.pax.dal.entity.TrackData
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PaxHardwareServiceTest {

    private lateinit var sdkManager: PaxSdkManager
    private lateinit var dal: IDAL
    private lateinit var mag: IMag
    private lateinit var printer: IPrinter
    private lateinit var hardwareService: PaxHardwareService

    @Before
    fun setUp() {
        sdkManager = mockk()
        dal = mockk()
        mag = mockk(relaxed = true)
        printer = mockk(relaxed = true)
        hardwareService = PaxHardwareService(sdkManager)

        every { sdkManager.getDal() } returns dal
        every { dal.getMag() } returns mag
        every { dal.getPrinter() } returns printer
    }

    @Test
    fun `readMsr returns track data after swipe`() = runBlocking {
        val trackData = mockk<TrackData>(relaxed = true)
        every { trackData.track1 } returns "T1Data"
        every { trackData.track2 } returns "T2Data"
        every { trackData.track3 } returns "T3Data"
        every { mag.isSwiped() } returns true
        every { mag.read() } returns trackData

        val result = hardwareService.readMsr()

        assertTrue(result is HardwareResult.Success)
        val data = (result as HardwareResult.Success).data
        assertEquals("T1Data", data.track1)
        assertEquals("T2Data", data.track2)
        assertEquals("T3Data", data.track3)
        verify(exactly = 1) { mag.open() }
        verify(exactly = 1) { mag.reset() }
        verify(exactly = 1) { mag.read() }
        verify(exactly = 1) { mag.close() }
    }

    @Test
    fun `readMsr returns error when DAL is unavailable`() = runBlocking {
        every { sdkManager.getDal() } returns null

        val result = hardwareService.readMsr()

        assertTrue(result is HardwareResult.Error)
        assertEquals("DAL not initialized", (result as HardwareResult.Error).message)
    }

    @Test
    fun `printText returns error when DAL is unavailable`() = runBlocking {
        every { sdkManager.getDal() } returns null

        val result = hardwareService.printText("hello")

        assertTrue(result is HardwareResult.Error)
        assertEquals("DAL not initialized", (result as HardwareResult.Error).message)
    }

    @Test
    fun `printText reports printer status before rendering bitmap`() = runBlocking {
        every { printer.getStatus() } returns 1

        val result = hardwareService.printText("hello")

        assertTrue(result is HardwareResult.Error)
        assertEquals("Printer Error: Out of paper (1)", (result as HardwareResult.Error).message)
        verify(exactly = 1) { printer.init() }
        verify(exactly = 1) { printer.getStatus() }
        verify(exactly = 0) { printer.start() }
    }
}
