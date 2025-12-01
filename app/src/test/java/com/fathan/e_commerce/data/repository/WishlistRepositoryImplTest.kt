package com.fathan.e_commerce.data.repository

import com.fathan.e_commerce.data.local.WishlistCollectionEntity
import com.fathan.e_commerce.data.local.WishlistDao
import com.fathan.e_commerce.data.local.WishlistItemEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class WishlistRepositoryImplTest {

    private lateinit var wishlistDao: WishlistDao
    private lateinit var repository: WishlistRepositoryImpl

    @Before
    fun setUp() {
        wishlistDao = mock()
        repository = WishlistRepositoryImpl(wishlistDao)
    }

    @Test
    fun `getCollections - positive - returns list from dao`() = runTest {
        // Given
        val mockCollections = listOf(
            WishlistCollectionEntity(id = 1, name = "Liburan"),
            WishlistCollectionEntity(id = 2, name = "Elektronik")
        )
        whenever(wishlistDao.getCollections()).thenReturn(flowOf(mockCollections))

        // When
        val result = repository.getCollections().first()

        // Then
        assertEquals(mockCollections.size, result.size)
        assertEquals("Liburan", result[0].name)
        verify(wishlistDao).getCollections()
    }

    @Test
    fun `getCollections - negative - returns empty list when dao is empty`() = runTest {
        // Given
        whenever(wishlistDao.getCollections()).thenReturn(flowOf(emptyList()))

        // When
        val result = repository.getCollections().first()

        // Then
        assertTrue(result.isEmpty())
        verify(wishlistDao).getCollections()
    }

    @Test
    fun `createCollection - positive - calls insertCollection on dao with correct name`() = runTest {
        // Given
        val newCollectionName = "New Gadget"
        val captor = argumentCaptor<WishlistCollectionEntity>()

        // When
        repository.createCollection(newCollectionName)

        // Then
        verify(wishlistDao).insertCollection(captor.capture())

        val capturedEntity = captor.firstValue
        assertEquals(newCollectionName, capturedEntity.name)
    }

    @Test(expected = RuntimeException::class)
    fun `createCollection - negative - propagates error from dao`() = runTest {
        // Given
        val newCollectionName = "New Gadget"
        doThrow(RuntimeException("DB error"))
            .whenever(wishlistDao)
            .insertCollection(any())

        // When
        // Will throw RuntimeException -> expected by @Test annotation
        repository.createCollection(newCollectionName)
    }

    @Test
    fun `getItemsCollection - positive - returns items for specific id`() = runTest {
        // Given
        val collectionId = 1
        val mockWishlist = listOf(
            WishlistItemEntity(101, collectionId, "Kamera", 50_000.0, ""),
            WishlistItemEntity(102, collectionId, "Test", 20_000.0, "")
        )

        whenever(wishlistDao.getItemsByCollection(collectionId))
            .thenReturn(flowOf(mockWishlist))

        // When
        val result = repository.getItemsByCollection(collectionId).first()

        // Then
        assertEquals(2, result.size)
        assertEquals("Kamera", result[0].name)
        verify(wishlistDao).getItemsByCollection(collectionId)
    }

    @Test
    fun `getItemsCollection - negative - returns empty list when dao returns empty`() = runTest {
        // Given
        val collectionId = 999 // misal id yang tidak punya item
        whenever(wishlistDao.getItemsByCollection(collectionId))
            .thenReturn(flowOf(emptyList()))

        // When
        val result = repository.getItemsByCollection(collectionId).first()

        // Then
        assertTrue(result.isEmpty())
        verify(wishlistDao).getItemsByCollection(collectionId)
    }

    // =========================================================================
    // addItemToCollection
    // =========================================================================

    @Test
    fun `addItemCollection - positive - calls insertItem on dao`() = runTest {
        // Given
        val item = WishlistItemEntity(
            productId = 101,
            collectionId = 1,
            name = "Kamera",
            price = 50_000.0,
            imageUrl = ""
        )

        // When
        repository.addItemToCollection(item)

        // Then
        verify(wishlistDao).insertItem(item)
    }

    @Test
    fun `deleteItem - positive - calls deleteItem on dao`() = runTest {
        // Given
        val productId = 123

        // When
        repository.deleteItem(productId)

        // Then
        verify(wishlistDao).deleteItem(productId)
    }

    @Test
    fun `updateCollectionName - positive - calls updateCollectionName on dao`() = runTest {
        // Given
        val id = 1
        val newName = "Updated Name"

        // When
        repository.updateCollectionName(id, newName)

        // Then
        verify(wishlistDao).updateCollectionName(id, newName)
    }
}
