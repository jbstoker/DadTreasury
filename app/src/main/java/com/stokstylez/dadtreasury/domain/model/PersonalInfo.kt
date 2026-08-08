package com.stokstylez.dadtreasury.domain.model

/**
 * Household personal info: dad, mom, child, sizes.
 * Fillable by parent; read-only for child.
 */
data class PersonalInfo(
    val dadName: String = "",
    val dadPhone: String = "",
    val dadEmail: String = "",
    val dadAddress: String = "",
    val momName: String = "",
    val momPhone: String = "",
    val momEmail: String = "",
    val momAddress: String = "",
    val childName: String = "",
    val childEmail: String = "",
    val childMobile: String = "",
    val childBirthdate: String = "",
    val shoeSize: String = "",
    val jeansSize: String = "",
    val shirtSize: String = "",
    val jacketSize: String = "",
    val hatSize: String = "",
    val dressSize: String = "",
)