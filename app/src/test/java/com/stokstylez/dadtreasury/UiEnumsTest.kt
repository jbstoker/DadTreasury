package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.ui.Routes
import com.stokstylez.dadtreasury.ui.screens.PinSetupMode
import org.junit.Assert.assertEquals
import org.junit.Test

class UiEnumsTest {

    @Test
    fun `pin setup mode setup has correct labels`() {
        assertEquals("Set Up PIN", PinSetupMode.SETUP.title)
        assertEquals("Enable PIN 🔒", PinSetupMode.SETUP.buttonText)
    }

    @Test
    fun `pin setup mode change has correct labels`() {
        assertEquals("Change PIN", PinSetupMode.CHANGE.title)
        assertEquals("Change PIN", PinSetupMode.CHANGE.buttonText)
    }

    @Test
    fun `routes constants are unique and well-formed`() {
        assertEquals("onboarding", Routes.ONBOARDING)
        assertEquals("home", Routes.HOME)
        assertEquals("tasks", Routes.TASKS)
        assertEquals("wallet", Routes.WALLET)
        assertEquals("calendar", Routes.CALENDAR)
        assertEquals("chat", Routes.CHAT)
        assertEquals("library", Routes.LIBRARY)
        assertEquals("location", Routes.LOCATION)
        assertEquals("pairing", Routes.PAIRING)
        assertEquals("diagnostics", Routes.DIAGNOSTICS)
        assertEquals("connect_parents", Routes.CONNECT_PARENTS)
        assertEquals("settings", Routes.SETTINGS)
        assertEquals("pin_setup", Routes.PIN_SETUP)
    }

    @Test
    fun `task detail route embeds task id`() {
        assertEquals("task/abc-123", Routes.taskDetail("abc-123"))
        assertEquals("task/", Routes.taskDetail(""))
    }
}