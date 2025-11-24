package com.fathan.e_commerce.data

import android.content.Context

import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

val Context.userDataStore by preferencesDataStore("user_prefs")
@Singleton
open class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
){
    companion object {
        val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_EMAIL = stringPreferencesKey("email")
    }

    // Expose cold Flows only – ViewModel will convert to StateFlow if needed
    open val isLoggedInFlow: Flow<Boolean> =
        context.userDataStore.data.map { prefs -> prefs[KEY_LOGGED_IN] ?: false }

    open val userNameFlow: Flow<String> =
        context.userDataStore.data.map { prefs -> prefs[KEY_USERNAME] ?: "" }

    open val userEmailFlow: Flow<String> =
        context.userDataStore.data.map { prefs -> prefs[KEY_EMAIL] ?: "" }

    open suspend fun saveUser(name: String, email: String) {
        context.userDataStore.edit {
            it[KEY_LOGGED_IN] = true
            it[KEY_USERNAME] = name
            it[KEY_EMAIL] = email
        }
    }

    open suspend fun logout() {
        context.userDataStore.edit {
            it.clear()
        }
    }
}