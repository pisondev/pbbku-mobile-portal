package id.pbbku.mobileportal

import android.app.Application
import id.pbbku.mobileportal.data.api.SimpbbApiClient
import id.pbbku.mobileportal.data.api.SimpbbApiService
import id.pbbku.mobileportal.data.local.PbbKuDatabase
import id.pbbku.mobileportal.data.repository.SimpbbRepository
import id.pbbku.mobileportal.data.session.SessionDataStore

class PbbKuApplication : Application() {
    val simpbbApiService: SimpbbApiService by lazy {
        SimpbbApiClient.create()
    }

    val simpbbRepository: SimpbbRepository by lazy {
        SimpbbRepository(simpbbApiService)
    }

    val database: PbbKuDatabase by lazy {
        PbbKuDatabase.create(this)
    }

    val sessionDataStore: SessionDataStore by lazy {
        SessionDataStore(this)
    }
}
