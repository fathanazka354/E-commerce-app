package com.fathan.e_commerce.viewmodels

import com.fathan.e_commerce.data.UserPreferences
import com.fathan.e_commerce.ui.login.LoginViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var prefs: UserPreferences
    private lateinit var viewModel: LoginViewModel

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        prefs = mock(UserPreferences::class.java)
        viewModel = LoginViewModel(prefs)
    }

    @Test
    fun email_empty_should_return_error() {
        viewModel.email.value = ""
        viewModel.password.value = "12345678"

        val result = viewModel.validate()

        assertFalse(result)
        assertEquals("Email cannot be empty", viewModel.emailError.value)
    }

    @Test
    fun invalid_email_should_return_error() {
        viewModel.email.value = "wrongemail"
        viewModel.password.value = "12345678"

        val result = viewModel.validate()

        assertFalse(result)
        assertEquals("Invalid email format", viewModel.emailError.value)
    }

    @Test
    fun password_less_than_8_chars_should_return_error() {
        viewModel.email.value = "test@mail.com"
        viewModel.password.value = "12345"

        val result = viewModel.validate()

        assertFalse(result)
        assertEquals("Password must be at least 8 characters", viewModel.passwordError.value)
    }

    @Test
    fun login_should_call_preferences_saveUser() = runTest {
        viewModel.email.value = "test@mail.com"
        viewModel.password.value = "12345678"

        viewModel.login { }

        advanceUntilIdle()

        verify(prefs).saveUser(
            name = "test",
            email = "test@mail.com"
        )
    }
}
