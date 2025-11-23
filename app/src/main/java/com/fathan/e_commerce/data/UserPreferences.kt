package com.fathan.e_commerce.data

import android.content.Context

import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.userDataStore by preferencesDataStore("user_prefs")

class UserPreferences(private val context: Context) {
    companion object {
        val KEY_LOGGED_IN = booleanPreferencesKey("logged_in")
        val KEY_USERNAME = stringPreferencesKey("username")
        val KEY_EMAIL = stringPreferencesKey("email")
    }

    val isLoggedInFlow = context.userDataStore.data.map {
        it[KEY_LOGGED_IN] ?: false
    }

    val userNameFlow = context.userDataStore.data.map { it[KEY_USERNAME] ?: "" }

    val userEmailFlow = context.userDataStore.data.map { it[KEY_EMAIL] ?: "" }

    suspend fun saveUser(name: String, email: String) {
        context.userDataStore.edit {
            it[KEY_LOGGED_IN] = true
            it[KEY_USERNAME] = name
            it[KEY_EMAIL] = email
        }
    }

    suspend fun logout(){
        context.userDataStore.edit { it.clear()
        it[KEY_LOGGED_IN] = false
        it[KEY_USERNAME] = ""
        it[KEY_EMAIL] = ""
        }
    }
}