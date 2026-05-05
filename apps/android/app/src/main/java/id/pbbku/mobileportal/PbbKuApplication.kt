package id.pbbku.mobileportal

import android.app.Application
import id.pbbku.mobileportal.data.local.PbbKuDatabase
import id.pbbku.mobileportal.data.session.SessionDataStore

class PbbKuApplication : Application() {
    val database: PbbKuDatabase by lazy {
        PbbKuDatabase.create(this)
    }

    val sessionDataStore: SessionDataStore by lazy {
        SessionDataStore(this)
    }
}
