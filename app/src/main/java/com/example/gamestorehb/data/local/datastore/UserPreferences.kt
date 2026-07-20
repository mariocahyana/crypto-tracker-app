package com.example.gamestorehb.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
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
    private val RISK_SCORE_KEY = intPreferencesKey("risk_score")
    private val VIRTUAL_BALANCE_KEY = doublePreferencesKey("virtual_balance")

    val riskScore: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[RISK_SCORE_KEY]
        }

    val virtualBalance: Flow<Double> = context.dataStore.data
        .map { preferences ->
            preferences[VIRTUAL_BALANCE_KEY] ?: 10000.0 // Default $10,000 USD
        }

    suspend fun saveRiskScore(score: Int) {
        context.dataStore.edit { preferences ->
            preferences[RISK_SCORE_KEY] = score
        }
    }

    suspend fun updateVirtualBalance(newBalance: Double) {
        context.dataStore.edit { preferences ->
            preferences[VIRTUAL_BALANCE_KEY] = newBalance
        }
    }
}
