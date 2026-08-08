package com.stokstylez.dadtreasury.domain.model

/**
 * Offline library / wiki knowledge base.
 *
 * Per spec §11: categories, pages, revisions, search, tags, simple/detailed views.
 */
data class LibraryCategory(
    val id: String,
    val name: String,
    val parentId: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

data class LibraryPage(
    val id: String,
    val categoryId: String,
    val title: String,
    val body: String = "",
    val tags: List<String> = emptyList(),
    val revision: Int = 1,
    val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
)

data class LibraryRevision(
    val id: String,
    val pageId: String,
    val revision: Int,
    val body: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)