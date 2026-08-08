package com.stokstylez.dadtreasury.domain.model

/**
 * Supported app languages.
 * SYSTEM_DEFAULT follows the device locale.
 */
enum class Language(val localeTag: String?) {
    SYSTEM_DEFAULT(null),
    ENGLISH("en"),
    NEDERLANDS("nl"),
    DEUTSCH("de"),
    ESPANOL("es"),
    FRANCAIS("fr"),
    CHINESE("zh"),
    FRYSLAN("fy")
}