package com.example.paxcheck.hardware

import com.example.paxcheck.sdk.PaxSdkManager
import com.pax.dal.IDal
import com.pax.dal.IMsr
import com.pax.dal.IPrinter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PaxHardwareServiceTest {

    private lateinit var sdkManager: PaxSdkManager
    private lateinit var dal: IDal
    private lateinit var msr: IMsr
    private lateinit var printer: IPrinter
    private lateinit var hardwareService: PaxHardwareService

    @Before
    fun setUp() {
        sdkManager = mockk()
        dal = mockk()
        msr = mockk(relaxed = true)
        printer = mockk(relaxed = true)
        hardwareService = PaxHardwareService(sdkManager)

        every { sdkManager.getDal() } returns dal
        every { dal.getMsr() } returns msr
        every { dal.getPrinter() } returns printer
    }

    @Test
    fun `readMsr returns data when successful`() = runBlocking {
        // Given
        val expectedData = "TrackData123"
        every { msr.read() } returns expectedData

        // When
        val result = hardwareService.readMsr()

        // Then
        assertEquals(expectedData, result)
        verify { msr.open() }
        verify { msr.reset() }
        verify { msr.read() }
        verify { msr.close() }
    }

    @Test
    fun `readMsr returns null when DAL is not initialized`() = runBlocking {
        // Given
        every { sdkManager.getDal() } returns null

        // When
        val result = hardwareService.readMsr()

        // Then
        assertNull(result)
    }

    @Test
    fun `printText returns true when successful`() = runBlocking {
        // Given
        val text = "Hello World"
        every { printer.start() } returns 0

        // When
        val result = hardwareService.printText(text)

        // Then
        assertTrue(result)
        verify { printer.init() }
        verify { printer.printStr(text, null) }
        verify { printer.start() }
    }

    @Test
    fun `printText returns false when printer fails`() = runBlocking {
        // Given
        val text = "Hello World"
        every { printer.start() } returns -1

        // When
        val result = hardwareService.printText(text)

        // Then
        assertEquals(false, result)
    }
}
