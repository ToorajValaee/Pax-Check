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
    fun `readMsr returns data when successful`() = runBlocking {
        // Given
        val trackData = mockk<TrackData>(relaxed = true)
        every { trackData.track1 } returns "T1Data"
        every { trackData.track2 } returns "T2Data"
        every { trackData.track3 } returns "T3Data"
        
        every { mag.isSwiped() } returns true
        every { mag.read() } returns trackData

        // When
        val result = hardwareService.readMsr()

        // Then
        assertTrue(result is HardwareResult.Success)
        val data = (result as HardwareResult.Success).data
        assertEquals("T1Data", data.track1)
        assertEquals("T2Data", data.track2)
        assertEquals("T3Data", data.track3)
        verify { mag.open() }
        verify { mag.reset() }
        verify { mag.read() }
        verify { mag.close() }
    }

    @Test
    fun `readMsr returns error when DAL is not initialized`() = runBlocking {
        // Given
        every { sdkManager.getDal() } returns null

        // When
        val result = hardwareService.readMsr()

        // Then
        assertTrue(result is HardwareResult.Error)
        assertEquals("DAL not initialized", (result as HardwareResult.Error).message)
    }

    @Test
    fun `printText returns success when successful`() = runBlocking {
        // Given
        val text = "Hello World"
        every { printer.start() } returns 0

        // When
        val result = hardwareService.printText(text)

        // Then
        assertTrue(result is HardwareResult.Success)
        verify { printer.init() }
        verify { printer.fontSet(any(), any()) }
        verify { printer.printStr(text, null) }
        verify { printer.start() }
    }

    @Test
    fun `printText returns error when printer fails`() = runBlocking {
        // Given
        val text = "Hello World"
        every { printer.start() } returns 1 // Out of paper

        // When
        val result = hardwareService.printText(text)

        // Then
        assertTrue(result is HardwareResult.Error)
        assertEquals("Out of paper", (result as HardwareResult.Error).message)
    }
}
