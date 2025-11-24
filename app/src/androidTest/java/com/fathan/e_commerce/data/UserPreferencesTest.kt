package com.fathan.e_commerce.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fathan.e_commerce.data.userDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserPreferencesTest {

    private lateinit var context: Context
    private lateinit var prefs: UserPreferences

    @Before
    fun setup() = runTest {
        context = ApplicationProvider.getApplicationContext()
        prefs = UserPreferences(context)

        // clear datastore sebelum tiap test
        context.userDataStore.edit { it.clear() }
    }

    @After
    fun tearDown() = runTest {
        context.userDataStore.edit { it.clear() }
    }

    @Test
    fun saveUser_setsLoggedInAndUserData() = runTest {
        prefs.saveUser(name = "Fathan", email = "fathan@example.com")

        val loggedIn = prefs.isLoggedInFlow.first()
        val name = prefs.userNameFlow.first()
        val email = prefs.userEmailFlow.first()

        assertTrue(loggedIn)
        assertEquals("Fathan", name)
        assertEquals("fathan@example.com", email)
    }

    @Test
    fun logout_clearsUserData() = runTest {
        prefs.saveUser(name = "Test", email = "test@example.com")

        prefs.logout()

        val loggedIn = prefs.isLoggedInFlow.first()
        val name = prefs.userNameFlow.first()
        val email = prefs.userEmailFlow.first()

        assertFalse(loggedIn)
        assertEquals("", name)
        assertEquals("", email)
    }
}