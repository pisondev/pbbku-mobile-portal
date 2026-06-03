package id.pbbku.mobileportal.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import id.pbbku.mobileportal.core.security.maskNik
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pbbku_session",
)

class SessionDataStore(
    context: Context,
) {
    private val dataStore = context.sessionDataStore

    val session: Flow<SimulatedSession> = dataStore.data.map { preferences ->
        val isLoggedIn = preferences[Keys.IS_LOGGED_IN] ?: false
        if (!isLoggedIn) {
            SimulatedSession.LoggedOut
        } else {
            SimulatedSession(
                isLoggedIn = true,
                displayName = preferences[Keys.DISPLAY_NAME] ?: DEMO_DISPLAY_NAME,
                maskedNik = preferences[Keys.MASKED_NIK],
                sessionToken = preferences[Keys.SESSION_TOKEN],
                createdAtEpochMillis = preferences[Keys.CREATED_AT_EPOCH_MILLIS],
            )
        }
    }

    suspend fun saveLogin(nik: String) {
        dataStore.edit { preferences ->
            preferences[Keys.IS_LOGGED_IN] = true
            preferences[Keys.DISPLAY_NAME] = DEMO_DISPLAY_NAME
            preferences[Keys.MASKED_NIK] = nik.maskNik()
            preferences[Keys.SESSION_TOKEN] = UUID.randomUUID().toString()
            preferences[Keys.CREATED_AT_EPOCH_MILLIS] = System.currentTimeMillis()
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private object Keys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val MASKED_NIK = stringPreferencesKey("masked_nik")
        val SESSION_TOKEN = stringPreferencesKey("session_token")
        val CREATED_AT_EPOCH_MILLIS = longPreferencesKey("created_at_epoch_millis")
    }

    private companion object {
        const val DEMO_DISPLAY_NAME = "Wajib Pajak Demo"
    }
}
