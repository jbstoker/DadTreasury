package com.stokstylez.dadtreasury.domain.model

/**
 * App role - parent or child.
 *
 * The spec defines two app flavors (parent/child), but for this single-module
 * prototype we keep the role as an in-app setting that can be chosen at onboarding.
 */
enum class Role {
    PARENT,
    CHILD
}