package id.pbbku.mobileportal

import android.app.Application
import id.pbbku.mobileportal.data.api.SimpbbApiClient
import id.pbbku.mobileportal.data.api.SimpbbApiService
import id.pbbku.mobileportal.data.demo.NikScopedDemoRepository
import id.pbbku.mobileportal.data.local.PbbKuDatabase
import id.pbbku.mobileportal.data.reminder.PaymentReminderRepository
import id.pbbku.mobileportal.data.repository.LocalCacheRepository
import id.pbbku.mobileportal.data.repository.ReportDraftRepository
import id.pbbku.mobileportal.data.repository.SimpbbRepository
import id.pbbku.mobileportal.data.repository.WilayahRepository
import id.pbbku.mobileportal.data.session.SessionDataStore

class PbbKuApplication : Application() {
    val simpbbApiService: SimpbbApiService by lazy {
        SimpbbApiClient.create()
    }

    val simpbbRepository: SimpbbRepository by lazy {
        SimpbbRepository(simpbbApiService)
    }

    val wilayahRepository: WilayahRepository by lazy {
        WilayahRepository(simpbbRepository)
    }

    val database: PbbKuDatabase by lazy {
        PbbKuDatabase.create(this)
    }

    val localCacheRepository: LocalCacheRepository by lazy {
        LocalCacheRepository(database.cacheEntryDao())
    }

    val reportDraftRepository: ReportDraftRepository by lazy {
        ReportDraftRepository(database.reportDraftDao())
    }

    val paymentReminderRepository: PaymentReminderRepository by lazy {
        PaymentReminderRepository(this)
    }

    val sessionDataStore: SessionDataStore by lazy {
        SessionDataStore(this)
    }

    val nikScopedDemoRepository: NikScopedDemoRepository by lazy {
        NikScopedDemoRepository(sessionDataStore)
    }
}
