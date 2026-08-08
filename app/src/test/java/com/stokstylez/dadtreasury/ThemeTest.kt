package com.stokstylez.dadtreasury

import com.stokstylez.dadtreasury.ui.theme.AppSettingsState
import com.stokstylez.dadtreasury.ui.theme.CalmTokens
import com.stokstylez.dadtreasury.ui.theme.HighContrastTokens
import com.stokstylez.dadtreasury.ui.theme.NatureTokens
import com.stokstylez.dadtreasury.ui.theme.RetroFuturistTokens
import com.stokstylez.dadtreasury.ui.theme.ThemeChoice
import com.stokstylez.dadtreasury.ui.theme.tokensFor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTest {

    @Test
    fun `tokensFor maps retro futurist`() {
        assertEquals(RetroFuturistTokens, tokensFor(ThemeChoice.RETRO_FUTURIST))
    }

    @Test
    fun `tokensFor maps high contrast`() {
        assertEquals(HighContrastTokens, tokensFor(ThemeChoice.HIGH_CONTRAST))
    }

    @Test
    fun `tokensFor maps calm`() {
        assertEquals(CalmTokens, tokensFor(ThemeChoice.CALM))
    }

    @Test
    fun `tokensFor maps nature`() {
        assertEquals(NatureTokens, tokensFor(ThemeChoice.NATURE))
    }

    @Test
    fun `app settings state defaults`() {
        val state = AppSettingsState()
        assertEquals(null, state.role)
        assertFalse(state.calmMode)
        assertFalse(state.reducedMotion)
        assertFalse(state.highContrast)
        assertEquals(1.0f, state.textScale)
        assertEquals(ThemeChoice.RETRO_FUTURIST, state.theme)
    }

    @Test
    fun `app settings state can be mutated`() {
        val state = AppSettingsState()
        state.role = "PARENT"
        state.calmMode = true
        state.reducedMotion = true
        state.highContrast = true
        state.textScale = 1.5f
        state.theme = ThemeChoice.NATURE

        assertEquals("PARENT", state.role)
        assertTrue(state.calmMode)
        assertTrue(state.reducedMotion)
        assertTrue(state.highContrast)
        assertEquals(1.5f, state.textScale)
        assertEquals(ThemeChoice.NATURE, state.theme)
    }
}