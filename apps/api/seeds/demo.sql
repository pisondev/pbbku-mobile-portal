INSERT INTO propinsi(kd_propinsi, nm_propinsi) VALUES
	('51', 'BALI'),
	('32', 'JAWA BARAT')
ON CONFLICT DO NOTHING;

INSERT INTO dati2(kd_propinsi, kd_dati2, nm_dati2) VALUES
	('51', '71', 'KOTA DENPASAR'),
	('51', '72', 'KABUPATEN BADUNG'),
	('32', '04', 'KABUPATEN BANDUNG')
ON CONFLICT DO NOTHING;

INSERT INTO kecamatan(kd_propinsi, kd_dati2, kd_kecamatan, nm_kecamatan) VALUES
	('51', '71', '010', 'DENPASAR SELATAN'),
	('51', '71', '020', 'DENPASAR TIMUR'),
	('51', '72', '010', 'KUTA'),
	('32', '04', '010', 'MARGAHAYU')
ON CONFLICT DO NOTHING;

INSERT INTO kelurahan(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, nm_kelurahan) VALUES
	('51', '71', '010', '001', 'SIDAKARYA'),
	('51', '71', '010', '002', 'SESETAN'),
	('51', '71', '020', '001', 'KESIMAN'),
	('51', '72', '010', '001', 'KUTA'),
	('32', '04', '010', '001', 'MARGAHAYU TENGAH')
ON CONFLICT DO NOTHING;

INSERT INTO blok(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, nm_blok) VALUES
	('51', '71', '010', '001', '054', 'BLOK 054 SIDAKARYA'),
	('51', '71', '010', '001', '055', 'BLOK 055 SIDAKARYA'),
	('51', '71', '010', '002', '010', 'BLOK 010 SESETAN'),
	('32', '04', '010', '001', '001', 'BLOK 001 MARGAHAYU')
ON CONFLICT DO NOTHING;

INSERT INTO tax_subjects(subject_id, nm_wp, jalan_wp, blok_kav_no_wp, rt_wp, rw_wp, kelurahan_wp, kota_wp, kd_pos_wp, status_pekerjaan_wp) VALUES
	('51710100010540032', 'I MADE BUDIARTA', 'JL. TUKAD BARITO NO. 18', 'A-7', '003', '004', 'SIDAKARYA', 'DENPASAR', '80224', '3'),
	('51710100010550011', 'NI KADEK SARI', 'JL. SIDAKARYA INDAH NO. 9', NULL, '002', '003', 'SIDAKARYA', 'DENPASAR', '80224', '2'),
	('32040100010010001', 'I WAYAN SUTARJA', 'JL. TERATAI RAYA NO. 12', NULL, '001', '002', 'MARGAHAYU TENGAH', 'BANDUNG', '40225', '3')
ON CONFLICT DO NOTHING;

INSERT INTO tax_objects(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op, subject_id, nm_wp, jalan_op, blok_kav_no_op, rt_op, rw_op, kelurahan_op, luas_bumi, nilai_sistem_bumi, njop_bumi, kd_status_wp, jns_bumi, jns_transaksi_op) VALUES
	('51', '71', '010', '001', '054', '0032', '0', '51710100010540032', 'I MADE BUDIARTA', 'JL. TUKAD BARITO NO. 18', 'A-7', '003', '004', 'SIDAKARYA', 320, 2380000000, 2380000000, '1', '1', '1'),
	('51', '71', '010', '001', '055', '0011', '0', '51710100010550011', 'NI KADEK SARI', 'JL. SIDAKARYA INDAH NO. 9', NULL, '002', '003', 'SIDAKARYA', 180, 875000000, 875000000, '1', '1', '1'),
	('32', '04', '010', '001', '001', '0001', '0', '32040100010010001', 'I WAYAN SUTARJA', 'JL. TERATAI RAYA NO. 12', NULL, '001', '002', 'MARGAHAYU TENGAH', 250, 500000000, 500000000, '1', '1', '1')
ON CONFLICT DO NOTHING;

INSERT INTO buildings(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op, no_bng, luas_bng, jml_lantai_bng, jenis_bangunan, jpb, thn_dibangun_bng, thn_renovasi_bng, kondisi_bng, konstruksi_bng, atap_bng, dinding_bng, lantai_bng, langit_langit_bng, nilai_sistem_bng) VALUES
	('51', '71', '010', '001', '054', '0032', '0', 1, 168, 2, 'Rumah Tinggal', 'Perumahan', 2014, 2021, 'Baik', 'Beton bertulang', 'Genteng beton', 'Bata diplester', 'Keramik', 'Gypsum', 870132648),
	('51', '71', '010', '001', '055', '0011', '0', 1, 96, 1, 'Rumah Tinggal', 'Perumahan', 2018, NULL, 'Baik', 'Beton bertulang', 'Genteng tanah liat', 'Bata diplester', 'Keramik', 'Tripleks', 315000000),
	('32', '04', '010', '001', '001', '0001', '0', 1, 120, 1, 'Rumah Tinggal', 'Perumahan', 2010, 2022, 'Baik', 'Beton bertulang', 'Genteng metal', 'Bata diplester', 'Keramik', 'Gypsum', 280000000)
ON CONFLICT DO NOTHING;

INSERT INTO building_facilities(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op, no_bng, nama_fasilitas, jumlah, satuan, keterangan) VALUES
	('51', '71', '010', '001', '054', '0032', '0', 1, 'AC Split', 3, 'unit', 'Terpasang di ruang utama dan kamar'),
	('51', '71', '010', '001', '054', '0032', '0', 1, 'Carport', 1, 'unit', 'Kapasitas satu mobil'),
	('51', '71', '010', '001', '054', '0032', '0', 1, 'Pagar Permanen', 22, 'meter', 'Pagar depan dan samping'),
	('32', '04', '010', '001', '001', '0001', '0', 1, 'Kanopi', 1, 'unit', 'Kanopi halaman depan')
ON CONFLICT DO NOTHING;

INSERT INTO sppt(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op, thn_pajak_sppt, pbb_yg_harus_dibayar_sppt, status_pembayaran_sppt, tgl_jatuh_tempo_sppt, denda_sppt, tgl_pembayaran_sppt, njop_bumi_sppt, njop_bangunan_sppt, njop_sppt, njoptkp_sppt, tarif_sppt, pbb_terutang_sppt) VALUES
	('51', '71', '010', '001', '054', '0032', '0', 2026, 1398000, '0', '2026-09-30', 0, NULL, 2448000000, 910000000, 3358000000, 12000000, 0.001, 1398000),
	('51', '71', '010', '001', '054', '0032', '0', 2025, 1284000, '0', '2025-09-30', 154000, NULL, 2380000000, 870132648, 3250132648, 12000000, 0.001, 1284000),
	('51', '71', '010', '001', '054', '0032', '0', 2024, 1186000, '1', '2024-09-30', 0, '2024-08-12', 2320000000, 870132648, 3190132648, 12000000, 0.001, 1186000),
	('51', '71', '010', '001', '054', '0032', '0', 2023, 1094000, '1', '2023-09-30', 0, '2023-07-21', 2200000000, 870132648, 3070132648, 12000000, 0.001, 1094000),
	('51', '71', '010', '001', '055', '0011', '0', 2026, 522000, '0', '2026-09-30', 0, NULL, 910000000, 315000000, 1225000000, 12000000, 0.001, 522000),
	('51', '71', '010', '001', '055', '0011', '0', 2025, 498000, '1', '2025-09-30', 0, '2025-09-02', 875000000, 315000000, 1190000000, 12000000, 0.001, 498000),
	('32', '04', '010', '001', '001', '0001', '0', 2026, 350000, '0', '2026-08-31', 0, NULL, 520000000, 280000000, 800000000, 12000000, 0.001, 350000),
	('32', '04', '010', '001', '001', '0001', '0', 2025, 330000, '1', '2025-08-31', 0, '2025-08-20', 500000000, 280000000, 780000000, 12000000, 0.001, 330000),
	('32', '04', '010', '001', '001', '0001', '0', 2024, 310000, '1', '2024-08-31', 0, '2024-08-10', 480000000, 260000000, 740000000, 12000000, 0.001, 310000)
ON CONFLICT DO NOTHING;
