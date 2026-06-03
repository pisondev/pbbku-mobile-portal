package id.pbbku.mobileportal.data.demo

import id.pbbku.mobileportal.domain.model.BuildingDetail
import id.pbbku.mobileportal.domain.model.BuildingFacility
import id.pbbku.mobileportal.domain.model.BuildingSummary
import id.pbbku.mobileportal.domain.model.Nop
import id.pbbku.mobileportal.domain.model.ObjekPajakDetail
import id.pbbku.mobileportal.domain.model.ObjekPajakSummary
import id.pbbku.mobileportal.domain.model.PaymentStatus
import id.pbbku.mobileportal.domain.model.TaxBillDetail
import id.pbbku.mobileportal.domain.model.TaxBillSummary
import java.time.LocalDate

object DemoTaxpayerDirectory {
    const val NIK_SITI = "3276010101010001"
    const val NIK_BUDI = "3276010101010002"
    const val NIK_DEWI = "3276010101010003"

    val sampleNiks: List<String> = listOf(NIK_SITI, NIK_BUDI, NIK_DEWI)

    private val records = listOf(
        demoRecord(
            ownerNik = NIK_SITI,
            displayName = "Siti Rahayu",
            nopText = "320401000100100010",
            namaWajibPajak = "SITI RAHAYU",
            alamatObjek = "Jl. Merdeka No. 12, Kelurahan Demo Barat",
            alamatWajibPajak = "Jl. Melati No. 8, Kota Bandung",
            luasBumi = 210.0,
            njopBumi = 189_000_000.0,
            buildings = listOf(
                DemoBuilding(
                    noBng = "1",
                    luasBangunan = 96.0,
                    jumlahLantai = 2,
                    jenisBangunan = "Rumah tinggal",
                    nilaiSistemBangunan = 156_000_000.0,
                ),
            ),
            bills = listOf(
                DemoBill(2026, 1_275_000.0, PaymentStatus.UNPAID, "2026-09-30", 0.0, null),
                DemoBill(2025, 1_190_000.0, PaymentStatus.PAID, "2025-09-30", 0.0, "2025-08-21"),
                DemoBill(2024, 1_145_000.0, PaymentStatus.OVERDUE, "2024-09-30", 183_200.0, null),
                DemoBill(2023, 1_080_000.0, PaymentStatus.PAID, "2023-09-30", 0.0, "2023-08-18"),
            ),
        ),
        demoRecord(
            ownerNik = NIK_SITI,
            displayName = "Siti Rahayu",
            nopText = "320401000100200020",
            namaWajibPajak = "SITI RAHAYU",
            alamatObjek = "Kavling Harmoni Blok C2, Kelurahan Demo Barat",
            alamatWajibPajak = "Jl. Melati No. 8, Kota Bandung",
            luasBumi = 128.0,
            njopBumi = 96_000_000.0,
            buildings = emptyList(),
            bills = listOf(
                DemoBill(2026, 645_000.0, PaymentStatus.UNPAID, "2026-09-30", 0.0, null),
                DemoBill(2025, 610_000.0, PaymentStatus.OVERDUE, "2025-09-30", 48_800.0, null),
                DemoBill(2024, 610_000.0, PaymentStatus.PAID, "2024-09-30", 0.0, "2024-09-02"),
            ),
        ),
        demoRecord(
            ownerNik = NIK_SITI,
            displayName = "Siti Rahayu",
            nopText = "320401000100300030",
            namaWajibPajak = "SITI RAHAYU",
            alamatObjek = "Jl. Anggrek Raya No. 21, Kelurahan Demo Barat",
            alamatWajibPajak = "Jl. Melati No. 8, Kota Bandung",
            luasBumi = 164.0,
            njopBumi = 132_000_000.0,
            buildings = listOf(
                DemoBuilding(
                    noBng = "1",
                    luasBangunan = 84.0,
                    jumlahLantai = 1,
                    jenisBangunan = "Rumah tinggal",
                    nilaiSistemBangunan = 118_000_000.0,
                ),
            ),
            bills = listOf(
                DemoBill(2026, 920_000.0, PaymentStatus.UNPAID, "2026-08-31", 0.0, null),
                DemoBill(2025, 875_000.0, PaymentStatus.PAID, "2025-09-30", 0.0, "2025-08-11"),
                DemoBill(2024, 845_000.0, PaymentStatus.PAID, "2024-09-30", 0.0, "2024-08-29"),
            ),
        ),
        demoRecord(
            ownerNik = NIK_SITI,
            displayName = "Siti Rahayu",
            nopText = "320401000100400040",
            namaWajibPajak = "SITI RAHAYU",
            alamatObjek = "Ruko Melati Unit 03, Kelurahan Demo Tengah",
            alamatWajibPajak = "Jl. Melati No. 8, Kota Bandung",
            luasBumi = 76.0,
            njopBumi = 154_000_000.0,
            buildings = listOf(
                DemoBuilding(
                    noBng = "1",
                    luasBangunan = 108.0,
                    jumlahLantai = 2,
                    jenisBangunan = "Ruko",
                    nilaiSistemBangunan = 235_000_000.0,
                ),
            ),
            bills = listOf(
                DemoBill(2026, 1_540_000.0, PaymentStatus.OVERDUE, "2026-03-31", 123_200.0, null),
                DemoBill(2025, 1_420_000.0, PaymentStatus.OVERDUE, "2025-09-30", 113_600.0, null),
                DemoBill(2024, 1_360_000.0, PaymentStatus.PAID, "2024-09-30", 0.0, "2024-07-19"),
            ),
        ),
        demoRecord(
            ownerNik = NIK_SITI,
            displayName = "Siti Rahayu",
            nopText = "320401000100500050",
            namaWajibPajak = "SITI RAHAYU",
            alamatObjek = "Kebun Produktif Blok D5, Kelurahan Demo Selatan",
            alamatWajibPajak = "Jl. Melati No. 8, Kota Bandung",
            luasBumi = 540.0,
            njopBumi = 121_500_000.0,
            buildings = emptyList(),
            bills = listOf(
                DemoBill(2026, 510_000.0, PaymentStatus.UNPAID, "2026-09-30", 0.0, null),
                DemoBill(2025, 490_000.0, PaymentStatus.PAID, "2025-09-30", 0.0, "2025-09-09"),
                DemoBill(2024, 470_000.0, PaymentStatus.PAID, "2024-09-30", 0.0, "2024-08-26"),
            ),
        ),
        demoRecord(
            ownerNik = NIK_BUDI,
            displayName = "Budi Santoso",
            nopText = "320401000200100030",
            namaWajibPajak = "BUDI SANTOSO",
            alamatObjek = "Jl. Pajajaran No. 45, Kelurahan Demo Timur",
            alamatWajibPajak = "Jl. Pajajaran No. 45, Kota Bandung",
            luasBumi = 180.0,
            njopBumi = 162_000_000.0,
            buildings = listOf(
                DemoBuilding(
                    noBng = "1",
                    luasBangunan = 72.0,
                    jumlahLantai = 1,
                    jenisBangunan = "Rumah tinggal",
                    nilaiSistemBangunan = 104_000_000.0,
                ),
                DemoBuilding(
                    noBng = "2",
                    luasBangunan = 24.0,
                    jumlahLantai = 1,
                    jenisBangunan = "Garasi",
                    nilaiSistemBangunan = 28_000_000.0,
                ),
            ),
            bills = listOf(
                DemoBill(2026, 1_060_000.0, PaymentStatus.OVERDUE, "2026-03-31", 84_800.0, null),
                DemoBill(2025, 990_000.0, PaymentStatus.PAID, "2025-09-30", 0.0, "2025-07-15"),
            ),
        ),
        demoRecord(
            ownerNik = NIK_DEWI,
            displayName = "Dewi Lestari",
            nopText = "320401000300100040",
            namaWajibPajak = "DEWI LESTARI",
            alamatObjek = "Ruko Sentra Pajak Unit 7, Kelurahan Demo Utara",
            alamatWajibPajak = "Jl. Kenanga No. 19, Kota Bandung",
            luasBumi = 92.0,
            njopBumi = 138_000_000.0,
            buildings = listOf(
                DemoBuilding(
                    noBng = "1",
                    luasBangunan = 144.0,
                    jumlahLantai = 2,
                    jenisBangunan = "Ruko",
                    nilaiSistemBangunan = 260_000_000.0,
                ),
            ),
            bills = listOf(
                DemoBill(2026, 1_820_000.0, PaymentStatus.UNPAID, "2026-09-30", 0.0, null),
                DemoBill(2025, 1_740_000.0, PaymentStatus.PAID, "2025-09-30", 0.0, "2025-08-03"),
            ),
        ),
    )

    fun displayNameForNik(nik: String): String {
        return records.firstOrNull { it.ownerNik == nik }?.displayName ?: "Warga Demo"
    }

    fun profileForNik(nik: String): DemoTaxpayerProfile {
        return when (nik) {
            NIK_SITI -> DemoTaxpayerProfile(
                nik = nik,
                name = "Siti Rahayu",
                address = "Jl. Melati No. 8, Kota Bandung",
                phone = "0812-3204-0001",
                email = "siti.rahayu@example.test",
                relationType = "Pemilik",
            )
            NIK_BUDI -> DemoTaxpayerProfile(
                nik = nik,
                name = "Budi Santoso",
                address = "Jl. Pajajaran No. 45, Kota Bandung",
                phone = "0812-3204-0002",
                email = "budi.santoso@example.test",
                relationType = "Pemilik",
            )
            NIK_DEWI -> DemoTaxpayerProfile(
                nik = nik,
                name = "Dewi Lestari",
                address = "Jl. Kenanga No. 19, Kota Bandung",
                phone = "0812-3204-0003",
                email = "dewi.lestari@example.test",
                relationType = "Pemilik",
            )
            else -> DemoTaxpayerProfile(
                nik = nik,
                name = "Warga Demo",
                address = "Data alamat belum tersedia",
                phone = "Data nomor HP belum tersedia",
                email = "Data email belum tersedia",
                relationType = "Belum memiliki objek demo",
            )
        }
    }

    fun recordsForNik(nik: String): List<DemoTaxObjectRecord> {
        return records.filter { it.ownerNik == nik }
    }

    fun recordForNikAndNop(nik: String, nop: Nop): DemoTaxObjectRecord? {
        val nopDisplay = nop.asDisplayText()
        return records.firstOrNull { it.ownerNik == nik && it.nop.asDisplayText() == nopDisplay }
    }

    fun ownerNikForNop(nop: Nop): String? {
        val nopDisplay = nop.asDisplayText()
        return records.firstOrNull { it.nop.asDisplayText() == nopDisplay }?.ownerNik
    }

    private fun demoRecord(
        ownerNik: String,
        displayName: String,
        nopText: String,
        namaWajibPajak: String,
        alamatObjek: String,
        alamatWajibPajak: String,
        luasBumi: Double,
        njopBumi: Double,
        buildings: List<DemoBuilding>,
        bills: List<DemoBill>,
    ): DemoTaxObjectRecord {
        val nop = requireNotNull(Nop.parseOrNull(nopText)) { "Invalid demo NOP $nopText" }
        val buildingDetails = buildings.map {
            BuildingDetail(
                nop = nop,
                noBng = it.noBng,
                luasBangunan = it.luasBangunan,
                jumlahLantai = it.jumlahLantai,
                jenisBangunan = it.jenisBangunan,
                jpb = it.jenisBangunan,
                tahunDibangun = 2015,
                tahunRenovasi = 2023,
                kondisi = "Baik",
                konstruksi = "Beton",
                atap = "Genteng",
                dinding = "Bata",
                lantai = "Keramik",
                langitLangit = "Gypsum",
                nilaiSistemBangunan = it.nilaiSistemBangunan,
            )
        }
        val totalLuasBangunan = buildingDetails.mapNotNull { it.luasBangunan }.sum().takeIf { it > 0.0 }
        val totalNilaiBangunan = buildingDetails.mapNotNull { it.nilaiSistemBangunan }.sum().takeIf { it > 0.0 }
        val billDetails = bills.map { bill ->
            TaxBillDetail(
                nop = nop,
                taxYear = bill.taxYear,
                status = bill.status,
                amount = bill.amount,
                dueDate = LocalDate.parse(bill.dueDate),
                fine = bill.fine,
                paymentDate = bill.paymentDate?.let(LocalDate::parse),
                njopBumi = njopBumi,
                njopBangunan = totalNilaiBangunan,
                njopTotal = njopBumi + (totalNilaiBangunan ?: 0.0),
                njoptkp = 12_000_000.0,
                tarif = 0.001,
                pbbTerutang = bill.amount,
            )
        }
        return DemoTaxObjectRecord(
            ownerNik = ownerNik,
            displayName = displayName,
            nop = nop,
            summary = ObjekPajakSummary(
                nop = nop,
                namaWajibPajak = namaWajibPajak,
                alamatObjekPajak = alamatObjek,
                luasBumi = luasBumi,
                njopBumi = njopBumi,
                totalLuasBangunan = totalLuasBangunan,
                totalNilaiBangunan = totalNilaiBangunan,
            ),
            detail = ObjekPajakDetail(
                nop = nop,
                alamatObjekPajak = alamatObjek,
                luasBumi = luasBumi,
                nilaiSistemBumi = njopBumi,
                jenisBumi = "Tanah dan bangunan",
                statusWajibPajak = "Pemilik",
                namaWajibPajak = namaWajibPajak,
                alamatWajibPajak = alamatWajibPajak,
                statusPekerjaanWajibPajak = "Warga demo",
            ),
            buildings = buildingDetails,
            facilities = listOf(
                BuildingFacility(
                    name = "Listrik",
                    quantity = 1.0,
                    unit = "unit",
                    description = "Fasilitas demo terikat NIK session",
                ),
            ),
            taxBills = billDetails,
        )
    }

    private data class DemoBuilding(
        val noBng: String,
        val luasBangunan: Double,
        val jumlahLantai: Int,
        val jenisBangunan: String,
        val nilaiSistemBangunan: Double,
    )

    private data class DemoBill(
        val taxYear: Int,
        val amount: Double,
        val status: PaymentStatus,
        val dueDate: String,
        val fine: Double,
        val paymentDate: String?,
    )
}

data class DemoTaxpayerProfile(
    val nik: String,
    val name: String,
    val address: String,
    val phone: String,
    val email: String,
    val relationType: String,
)

data class DemoTaxObjectRecord(
    val ownerNik: String,
    val displayName: String,
    val nop: Nop,
    val summary: ObjekPajakSummary,
    val detail: ObjekPajakDetail,
    val buildings: List<BuildingDetail>,
    val facilities: List<BuildingFacility>,
    val taxBills: List<TaxBillDetail>,
) {
    val buildingSummaries: List<BuildingSummary> = buildings.map {
        BuildingSummary(
            nop = it.nop,
            noBng = it.noBng,
            luasBangunan = it.luasBangunan,
            jumlahLantai = it.jumlahLantai,
            jenisBangunan = it.jenisBangunan,
            jpb = it.jpb,
            nilaiSistemBangunan = it.nilaiSistemBangunan,
        )
    }

    val taxBillSummaries: List<TaxBillSummary> = taxBills.map {
        TaxBillSummary(
            nop = it.nop,
            taxYear = it.taxYear,
            amount = it.amount,
            status = it.status,
            dueDate = it.dueDate,
            fine = it.fine,
        )
    }.sortedByDescending { it.taxYear }
}
