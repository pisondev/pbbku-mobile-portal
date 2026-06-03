CREATE TABLE propinsi (
	kd_propinsi TEXT PRIMARY KEY,
	nm_propinsi TEXT NOT NULL
);

CREATE TABLE dati2 (
	kd_propinsi TEXT NOT NULL,
	kd_dati2 TEXT NOT NULL,
	nm_dati2 TEXT NOT NULL,
	PRIMARY KEY (kd_propinsi, kd_dati2),
	FOREIGN KEY (kd_propinsi) REFERENCES propinsi(kd_propinsi) ON DELETE CASCADE
);

CREATE TABLE kecamatan (
	kd_propinsi TEXT NOT NULL,
	kd_dati2 TEXT NOT NULL,
	kd_kecamatan TEXT NOT NULL,
	nm_kecamatan TEXT NOT NULL,
	PRIMARY KEY (kd_propinsi, kd_dati2, kd_kecamatan),
	FOREIGN KEY (kd_propinsi, kd_dati2) REFERENCES dati2(kd_propinsi, kd_dati2) ON DELETE CASCADE
);

CREATE TABLE kelurahan (
	kd_propinsi TEXT NOT NULL,
	kd_dati2 TEXT NOT NULL,
	kd_kecamatan TEXT NOT NULL,
	kd_kelurahan TEXT NOT NULL,
	nm_kelurahan TEXT NOT NULL,
	PRIMARY KEY (kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan),
	FOREIGN KEY (kd_propinsi, kd_dati2, kd_kecamatan) REFERENCES kecamatan(kd_propinsi, kd_dati2, kd_kecamatan) ON DELETE CASCADE
);

CREATE TABLE blok (
	kd_propinsi TEXT NOT NULL,
	kd_dati2 TEXT NOT NULL,
	kd_kecamatan TEXT NOT NULL,
	kd_kelurahan TEXT NOT NULL,
	kd_blok TEXT NOT NULL,
	nm_blok TEXT NOT NULL,
	PRIMARY KEY (kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok),
	FOREIGN KEY (kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan) REFERENCES kelurahan(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan) ON DELETE CASCADE
);

CREATE TABLE tax_subjects (
	subject_id TEXT PRIMARY KEY,
	nm_wp TEXT NOT NULL,
	jalan_wp TEXT,
	blok_kav_no_wp TEXT,
	rt_wp TEXT,
	rw_wp TEXT,
	kelurahan_wp TEXT,
	kota_wp TEXT,
	kd_pos_wp TEXT,
	status_pekerjaan_wp TEXT
);

CREATE TABLE tax_objects (
	kd_propinsi TEXT NOT NULL,
	kd_dati2 TEXT NOT NULL,
	kd_kecamatan TEXT NOT NULL,
	kd_kelurahan TEXT NOT NULL,
	kd_blok TEXT NOT NULL,
	no_urut TEXT NOT NULL,
	kd_jns_op TEXT NOT NULL,
	subject_id TEXT NOT NULL,
	nm_wp TEXT NOT NULL,
	jalan_op TEXT,
	blok_kav_no_op TEXT,
	rt_op TEXT,
	rw_op TEXT,
	kelurahan_op TEXT,
	luas_bumi DOUBLE PRECISION,
	nilai_sistem_bumi DOUBLE PRECISION,
	njop_bumi DOUBLE PRECISION,
	kd_status_wp TEXT,
	jns_bumi TEXT,
	jns_transaksi_op TEXT,
	PRIMARY KEY (kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op),
	FOREIGN KEY (kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok) REFERENCES blok(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok),
	FOREIGN KEY (subject_id) REFERENCES tax_subjects(subject_id)
);

CREATE TABLE buildings (
	kd_propinsi TEXT NOT NULL,
	kd_dati2 TEXT NOT NULL,
	kd_kecamatan TEXT NOT NULL,
	kd_kelurahan TEXT NOT NULL,
	kd_blok TEXT NOT NULL,
	no_urut TEXT NOT NULL,
	kd_jns_op TEXT NOT NULL,
	no_bng INTEGER NOT NULL,
	luas_bng DOUBLE PRECISION,
	jml_lantai_bng INTEGER,
	jenis_bangunan TEXT,
	jpb TEXT,
	thn_dibangun_bng INTEGER,
	thn_renovasi_bng INTEGER,
	kondisi_bng TEXT,
	konstruksi_bng TEXT,
	atap_bng TEXT,
	dinding_bng TEXT,
	lantai_bng TEXT,
	langit_langit_bng TEXT,
	nilai_sistem_bng DOUBLE PRECISION,
	PRIMARY KEY (kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op, no_bng),
	FOREIGN KEY (kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op)
		REFERENCES tax_objects(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op) ON DELETE CASCADE
);

CREATE TABLE building_facilities (
	kd_propinsi TEXT NOT NULL,
	kd_dati2 TEXT NOT NULL,
	kd_kecamatan TEXT NOT NULL,
	kd_kelurahan TEXT NOT NULL,
	kd_blok TEXT NOT NULL,
	no_urut TEXT NOT NULL,
	kd_jns_op TEXT NOT NULL,
	no_bng INTEGER NOT NULL,
	nama_fasilitas TEXT NOT NULL,
	jumlah DOUBLE PRECISION,
	satuan TEXT,
	keterangan TEXT,
	PRIMARY KEY (kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op, no_bng, nama_fasilitas),
	FOREIGN KEY (kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op, no_bng)
		REFERENCES buildings(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op, no_bng) ON DELETE CASCADE
);

CREATE TABLE sppt (
	kd_propinsi TEXT NOT NULL,
	kd_dati2 TEXT NOT NULL,
	kd_kecamatan TEXT NOT NULL,
	kd_kelurahan TEXT NOT NULL,
	kd_blok TEXT NOT NULL,
	no_urut TEXT NOT NULL,
	kd_jns_op TEXT NOT NULL,
	thn_pajak_sppt INTEGER NOT NULL,
	pbb_yg_harus_dibayar_sppt DOUBLE PRECISION NOT NULL,
	status_pembayaran_sppt TEXT NOT NULL,
	tgl_jatuh_tempo_sppt TEXT,
	denda_sppt DOUBLE PRECISION,
	tgl_pembayaran_sppt TEXT,
	njop_bumi_sppt DOUBLE PRECISION,
	njop_bangunan_sppt DOUBLE PRECISION,
	njop_sppt DOUBLE PRECISION,
	njoptkp_sppt DOUBLE PRECISION,
	tarif_sppt DOUBLE PRECISION,
	pbb_terutang_sppt DOUBLE PRECISION,
	PRIMARY KEY (kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op, thn_pajak_sppt),
	FOREIGN KEY (kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op)
		REFERENCES tax_objects(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op) ON DELETE CASCADE
);

CREATE INDEX idx_tax_objects_name ON tax_objects(nm_wp);
CREATE INDEX idx_sppt_year_status ON sppt(thn_pajak_sppt, status_pembayaran_sppt);
