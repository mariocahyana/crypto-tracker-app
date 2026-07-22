package com.example.gamestorehb.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val RISK_SCORE_KEY        = intPreferencesKey("risk_score")
    private val VIRTUAL_BALANCE_KEY   = doublePreferencesKey("virtual_balance")
    // ── Auth session keys ──────────────────────────────────────────────────────
    private val LOGGED_IN_USER_ID_KEY = intPreferencesKey("logged_in_user_id")
    private val LOGGED_IN_USERNAME_KEY = stringPreferencesKey("logged_in_username")

    val riskScore: Flow<Int?> = context.dataStore.data
        .map { it[RISK_SCORE_KEY] }

    val virtualBalance: Flow<Double> = context.dataStore.data
        .map { it[VIRTUAL_BALANCE_KEY] ?: 10000.0 }

    /** Emits the logged-in user's ID, or -1 if not logged in. */
    val loggedInUserId: Flow<Int> = context.dataStore.data
        .map { it[LOGGED_IN_USER_ID_KEY] ?: -1 }

    /** Emits the logged-in username, or empty string if not logged in. */
    val loggedInUsername: Flow<String> = context.dataStore.data
        .map { it[LOGGED_IN_USERNAME_KEY] ?: "" }

    suspend fun saveRiskScore(score: Int) {
        context.dataStore.edit { it[RISK_SCORE_KEY] = score }
    }

    suspend fun updateVirtualBalance(newBalance: Double) {
        context.dataStore.edit { it[VIRTUAL_BALANCE_KEY] = newBalance }
    }

    /** Persist the user session after a successful login. */
    suspend fun saveSession(userId: Int, username: String) {
        context.dataStore.edit {
            it[LOGGED_IN_USER_ID_KEY]  = userId
            it[LOGGED_IN_USERNAME_KEY] = username
        }
    }

    /** Clear session on logout. */
    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(LOGGED_IN_USER_ID_KEY)
            it.remove(LOGGED_IN_USERNAME_KEY)
        }
    }
}
