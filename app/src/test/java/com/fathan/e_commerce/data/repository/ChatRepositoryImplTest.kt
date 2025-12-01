package com.fathan.e_commerce.data.repository

import android.net.Uri
import com.fathan.e_commerce.data.local.MessageType
import com.fathan.e_commerce.data.local.WishlistCollectionEntity
import com.fathan.e_commerce.data.local.WishlistDao
import com.fathan.e_commerce.data.local.WishlistItemEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock

class ChatRepositoryImplTest {
    class FakeChatDao : WishlistDao {
        override fun getCollections(): Flow<List<WishlistCollectionEntity>> {
            TODO("Not yet implemented")
        }

        override suspend fun insertCollection(collection: WishlistCollectionEntity) {
            TODO("Not yet implemented")
        }

        override fun getItemsByCollection(collectionId: Int): Flow<List<WishlistItemEntity>> {
            TODO("Not yet implemented")
        }

        override suspend fun insertItem(item: WishlistItemEntity) {
            TODO("Not yet implemented")
        }

        override suspend fun deleteItem(productId: Int) {
            TODO("Not yet implemented")
        }

        override suspend fun getPreviewImage(collectionId: Int): String? {
            TODO("Not yet implemented")
        }

        override suspend fun getItemCount(collectionId: Int): Int {
            TODO("Not yet implemented")
        }

        override suspend fun updateCollectionName(
            collectionId: Int,
            newName: String
        ) {
            TODO("Not yet implemented")
        }

        override suspend fun deleteCollection(collectionId: Int) {
            TODO("Not yet implemented")
        }

        override suspend fun deleteItemsByCollection(collectionId: Int) {
            TODO("Not yet implemented")
        }

        override fun isProductInWishlist(productId: Int): Flow<Boolean> {
            TODO("Not yet implemented")
        }

    }

    @Test
    fun `getMessages returns initial dummy messages`() = runBlocking {
        val repo = ChatRepositoryImpl()

        val messages = repo.getMessages()

        assertTrue(messages.isNotEmpty())
        assertEquals("Hey team, let's start the meeting.", messages.first().text)
    }

    @Test
    fun sendText_with_blankTextDoesNotAddMessage() = runBlocking {
        val repo = ChatRepositoryImpl()
        val initialSize = repo.getMessages().size

        repo.sendText("  ") // blank

        val finalSize = repo.getMessages().size
        Assert.assertEquals(initialSize, finalSize)
    }

    @Test
    fun `sendText with valid text adds new TEXT message from me`() = runBlocking {
        val repo = ChatRepositoryImpl()
        val initialSize = repo.getMessages().size

        repo.sendText("Hello world")

        val messages = repo.getMessages()
        Assert.assertEquals(initialSize + 1, messages.size)

        val last = messages.last()
        assertEquals("Hello world", last.text)
        assertTrue(last.isMe)
        assertEquals(MessageType.TEXT, last.type)
    }

    @Test
    fun `sendImage adds IMAGE message with given uri`() = runBlocking {
        val repo = ChatRepositoryImpl()
        val initialSize = repo.getMessages().size

        // FIX: Use a mock instead of Uri.parse
        val imageUri = mock(Uri::class.java)

        repo.sendImage(imageUri)

        val messages = repo.getMessages()
        Assert.assertEquals(initialSize + 1, messages.size)

        val last = messages.last()
        assertEquals(MessageType.IMAGE, last.type)
        assertEquals(imageUri, last.imageUri)
        assertTrue(last.isMe)
    }

    @Test
    fun `sendAudio adds AUDIO message with uri and duration`() = runBlocking {
        val repo = ChatRepositoryImpl()
        val initialSize = repo.getMessages().size

        // FIX: Use a mock instead of Uri.parse
        val audioUri = mock(Uri::class.java)
        val durationText = "0:05"

        repo.sendAudio(audioUri, durationText)

        val messages = repo.getMessages()
        Assert.assertEquals(initialSize + 1, messages.size)

        val last = messages.last()
        assertEquals(MessageType.AUDIO, last.type)
        assertEquals(audioUri, last.audioUri)
        assertEquals(durationText, last.audioDuration)
        assertTrue(last.isMe)
    }
}
