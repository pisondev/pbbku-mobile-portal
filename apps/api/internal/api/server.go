package api

import (
	"database/sql"
	"encoding/json"
	"errors"
	"fmt"
	"math"
	"net/http"
	"strconv"
	"strings"
	"time"

	"pbbku-mobile-portal/apps/api/internal/db"
)

type Server struct {
	db      *db.Database
	options Options
}

type orpcRequest struct {
	JSON map[string]any `json:"json"`
}

type nopKey struct {
	KdPropinsi  string
	KdDati2     string
	KdKecamatan string
	KdKelurahan string
	KdBlok      string
	NoUrut      string
	KdJnsOp     string
}

func (s *Server) health(w http.ResponseWriter, r *http.Request) {
	if err := s.db.PingContext(r.Context()); err != nil {
		writeError(w, http.StatusServiceUnavailable, err)
		return
	}
	writeRaw(w, http.StatusOK, map[string]any{"status": "ok", "service": "pbbku-internal-api"})
}

func (s *Server) searchObjekPajak(w http.ResponseWriter, r *http.Request) {
	body, err := readORPC(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	query := strings.TrimSpace(stringParam(body, "query"))
	limit := intParam(body, "limit", 10)
	if limit <= 0 || limit > 100 {
		limit = 10
	}
	nopQuery := query
	if digits := digitsOnly(query); digits != "" {
		nopQuery = digits
	}
	rows, err := s.db.QueryContext(r.Context(), objectSummarySQL(`
		WHERE lower(t.nm_wp) LIKE lower(?)
		   OR replace(t.kd_propinsi || t.kd_dati2 || t.kd_kecamatan || t.kd_kelurahan || t.kd_blok || t.no_urut || t.kd_jns_op, '.', '') LIKE ?
		   OR lower(t.jalan_op) LIKE lower(?)
		`, `
		ORDER BY t.kd_propinsi, t.kd_dati2, t.kd_kecamatan, t.kd_kelurahan, t.kd_blok, t.no_urut
		LIMIT ?`), like(query), like(nopQuery), like(query), limit)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	result, err := scanObjectSummaries(rows)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeJSON(w, http.StatusOK, result)
}

func (s *Server) listObjekPajakDetails(w http.ResponseWriter, r *http.Request) {
	body, err := readORPC(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	limit := intParam(body, "limit", 20)
	offset := intParam(body, "offset", 0)
	if limit <= 0 || limit > 200 {
		limit = 20
	}
	where := []string{"1=1"}
	args := []any{}
	if v := stringParam(body, "kdPropinsi"); v != "" {
		where = append(where, "t.kd_propinsi = ?")
		args = append(args, v)
	}
	if v := stringParam(body, "kdDati2"); v != "" {
		where = append(where, "t.kd_dati2 = ?")
		args = append(args, v)
	}
	if v := stringParam(body, "search"); v != "" {
		where = append(where, "(lower(t.nm_wp) LIKE lower(?) OR lower(t.jalan_op) LIKE lower(?) OR t.no_urut LIKE ?)")
		args = append(args, like(v), like(v), like(v))
	}

	countSQL := "SELECT COUNT(*) FROM tax_objects t WHERE " + strings.Join(where, " AND ")
	var total int
	if err := s.db.QueryRowContext(r.Context(), countSQL, args...).Scan(&total); err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}

	listSQL := objectSummarySQL("WHERE "+strings.Join(where, " AND "), `
		ORDER BY t.kd_propinsi, t.kd_dati2, t.kd_kecamatan, t.kd_kelurahan, t.kd_blok, t.no_urut
		LIMIT ? OFFSET ?`)
	args = append(args, limit, offset)
	rows, err := s.db.QueryContext(r.Context(), listSQL, args...)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	resultRows, err := scanObjectSummaries(rows)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"rows": resultRows, "total": total})
}

func (s *Server) getObjekPajakByNop(w http.ResponseWriter, r *http.Request) {
	key, err := readNop(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	obj, err := s.getObject(r, key)
	if errors.Is(err, sql.ErrNoRows) {
		writeJSON(w, http.StatusOK, nil)
		return
	}
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeJSON(w, http.StatusOK, obj)
}

func (s *Server) saveObjekPajak(w http.ResponseWriter, r *http.Request) {
	body, err := readORPC(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	spop, _ := body["spop"].(map[string]any)
	subject, _ := body["subjekPajak"].(map[string]any)
	if spop == nil || subject == nil {
		writeError(w, http.StatusBadRequest, errors.New("spop and subjekPajak are required"))
		return
	}
	key := nopFromMap(spop)
	if err := key.validate(); err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	subjectID := firstString(spop, "subjekPajakId")
	if subjectID == "" {
		subjectID = firstString(subject, "subjekPajakId")
	}
	if subjectID == "" {
		subjectID = key.display()
	}

	tx, err := s.db.BeginTx(r.Context(), nil)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer tx.Rollback()
	_, err = tx.ExecContext(r.Context(), `
		INSERT INTO tax_subjects(subject_id, nm_wp, jalan_wp, blok_kav_no_wp, rt_wp, rw_wp, kelurahan_wp, kota_wp, kd_pos_wp, status_pekerjaan_wp)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
		ON CONFLICT(subject_id) DO UPDATE SET
			nm_wp=excluded.nm_wp, jalan_wp=excluded.jalan_wp, blok_kav_no_wp=excluded.blok_kav_no_wp,
			rt_wp=excluded.rt_wp, rw_wp=excluded.rw_wp, kelurahan_wp=excluded.kelurahan_wp,
			kota_wp=excluded.kota_wp, kd_pos_wp=excluded.kd_pos_wp, status_pekerjaan_wp=excluded.status_pekerjaan_wp
	`, subjectID, firstString(subject, "nmWp"), firstString(subject, "jalanWp"), firstString(subject, "blokKavNoWp"),
		firstString(subject, "rtWp"), firstString(subject, "rwWp"), firstString(subject, "kelurahanWp"),
		firstString(subject, "kotaWp"), firstString(subject, "kdPosWp"), firstString(subject, "statusPekerjaanWp"))
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	_, err = tx.ExecContext(r.Context(), `
		INSERT INTO tax_objects(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op, subject_id, nm_wp, jalan_op, blok_kav_no_op, rt_op, rw_op, kelurahan_op, luas_bumi, nilai_sistem_bumi, njop_bumi, kd_status_wp, jns_bumi, jns_transaksi_op)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
		ON CONFLICT(kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op) DO UPDATE SET
			subject_id=excluded.subject_id, nm_wp=excluded.nm_wp, jalan_op=excluded.jalan_op,
			blok_kav_no_op=excluded.blok_kav_no_op, rt_op=excluded.rt_op, rw_op=excluded.rw_op,
			kelurahan_op=excluded.kelurahan_op, luas_bumi=excluded.luas_bumi,
			nilai_sistem_bumi=excluded.nilai_sistem_bumi, njop_bumi=excluded.njop_bumi,
			kd_status_wp=excluded.kd_status_wp, jns_bumi=excluded.jns_bumi, jns_transaksi_op=excluded.jns_transaksi_op
	`, key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp,
		subjectID, firstString(subject, "nmWp"), firstString(spop, "jalanOp"), firstString(spop, "blokKavNoOp"),
		firstString(spop, "rtOp"), firstString(spop, "rwOp"), firstString(spop, "kelurahanOp"),
		numberParam(spop, "luasBumi"), numberParam(spop, "nilaiSistemBumi"), numberParam(spop, "njopBumi"),
		firstString(spop, "kdStatusWp"), firstString(spop, "jnsBumi"), firstString(spop, "jnsTransaksiOp"))
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	if err := tx.Commit(); err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"message": "Success", "nop": key.display()})
}

func (s *Server) getSpptHistory(w http.ResponseWriter, r *http.Request) {
	s.listSpptByNop(w, r)
}

func (s *Server) getTunggakan(w http.ResponseWriter, r *http.Request) {
	key, err := readNop(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	rows, err := s.db.QueryContext(r.Context(), spptSQL()+`
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=? AND no_urut=? AND kd_jns_op=?
		  AND (status_pembayaran_sppt = '0' OR tgl_pembayaran_sppt IS NULL)
		ORDER BY thn_pajak_sppt DESC`,
		key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	result, err := scanSpptRows(rows)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeJSON(w, http.StatusOK, result)
}

func (s *Server) getNextNoUrut(w http.ResponseWriter, r *http.Request) {
	body, err := readORPC(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	next, err := s.nextNoUrut(r, stringParam(body, "kdPropinsi"), stringParam(body, "kdDati2"), stringParam(body, "kdKecamatan"), stringParam(body, "kdKelurahan"), stringParam(body, "kdBlok"))
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"noUrut": next, "nextNoUrut": next})
}

func (s *Server) getNextNoFormulir(w http.ResponseWriter, r *http.Request) {
	year := time.Now().Year()
	var count int
	_ = s.db.QueryRowContext(r.Context(), "SELECT COUNT(*) FROM tax_objects").Scan(&count)
	no := fmt.Sprintf("SPOP-%d-%05d", year, count+1)
	writeJSON(w, http.StatusOK, map[string]any{"noFormulir": no, "nextNoFormulir": no})
}

func (s *Server) listBuildingsByNop(w http.ResponseWriter, r *http.Request) {
	key, err := readNop(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	rows, err := s.db.QueryContext(r.Context(), buildingSQL()+`
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=? AND no_urut=? AND kd_jns_op=?
		ORDER BY no_bng`,
		key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	result, err := scanBuildings(rows)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeJSON(w, http.StatusOK, result)
}

func (s *Server) getBuilding(w http.ResponseWriter, r *http.Request) {
	body, err := readORPC(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	key := nopFromMap(body)
	if err := key.validate(); err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	noBng := intParam(body, "noBng", 0)
	row := s.db.QueryRowContext(r.Context(), buildingSQL()+`
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=? AND no_urut=? AND kd_jns_op=? AND no_bng=?`,
		key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp, noBng)
	result, err := scanBuilding(row)
	if errors.Is(err, sql.ErrNoRows) {
		writeJSON(w, http.StatusOK, nil)
		return
	}
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeJSON(w, http.StatusOK, result)
}

func (s *Server) listFacilities(w http.ResponseWriter, r *http.Request) {
	body, err := readORPC(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	key := nopFromMap(body)
	if err := key.validate(); err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	noBng := intParam(body, "noBng", 0)
	rows, err := s.db.QueryContext(r.Context(), `
		SELECT nama_fasilitas, jumlah, satuan, keterangan
		FROM building_facilities
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=? AND no_urut=? AND kd_jns_op=? AND no_bng=?
		ORDER BY nama_fasilitas`,
		key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp, noBng)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	result := []map[string]any{}
	for rows.Next() {
		var nama, satuan, ket sql.NullString
		var jumlah sql.NullFloat64
		if err := rows.Scan(&nama, &jumlah, &satuan, &ket); err != nil {
			writeError(w, http.StatusInternalServerError, err)
			return
		}
		result = append(result, map[string]any{
			"namaFasilitas": nullableString(nama),
			"jumlah":        nullableFloat(jumlah),
			"satuan":        nullableString(satuan),
			"keterangan":    nullableString(ket),
		})
	}
	writeJSON(w, http.StatusOK, result)
}

func (s *Server) nextNoBng(w http.ResponseWriter, r *http.Request) {
	key, err := readNop(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	var maxNo sql.NullInt64
	err = s.db.QueryRowContext(r.Context(), `
		SELECT MAX(no_bng) FROM buildings
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=? AND no_urut=? AND kd_jns_op=?`,
		key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp).Scan(&maxNo)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	next := int(maxNo.Int64) + 1
	if !maxNo.Valid {
		next = 1
	}
	writeJSON(w, http.StatusOK, map[string]any{"noBng": next, "nextNoBng": next})
}

func (s *Server) listPropinsi(w http.ResponseWriter, r *http.Request) {
	rows, err := s.db.QueryContext(r.Context(), "SELECT kd_propinsi, nm_propinsi FROM propinsi ORDER BY kd_propinsi")
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	writeRegionRows(w, rows, "kdPropinsi", "nmPropinsi")
}

func (s *Server) listDati2(w http.ResponseWriter, r *http.Request) {
	body, err := readORPC(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	rows, err := s.db.QueryContext(r.Context(), "SELECT kd_dati2, nm_dati2 FROM dati2 WHERE kd_propinsi=? ORDER BY kd_dati2", stringParam(body, "kdPropinsi"))
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	writeRegionRows(w, rows, "kdDati2", "nmDati2")
}

func (s *Server) listKecamatan(w http.ResponseWriter, r *http.Request) {
	body, err := readORPC(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	rows, err := s.db.QueryContext(r.Context(), "SELECT kd_kecamatan, nm_kecamatan FROM kecamatan WHERE kd_propinsi=? AND kd_dati2=? ORDER BY kd_kecamatan", stringParam(body, "kdPropinsi"), stringParam(body, "kdDati2"))
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	writeRegionRows(w, rows, "kdKecamatan", "nmKecamatan")
}

func (s *Server) listKelurahan(w http.ResponseWriter, r *http.Request) {
	body, err := readORPC(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	rows, err := s.db.QueryContext(r.Context(), "SELECT kd_kelurahan, nm_kelurahan FROM kelurahan WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? ORDER BY kd_kelurahan", stringParam(body, "kdPropinsi"), stringParam(body, "kdDati2"), stringParam(body, "kdKecamatan"))
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	writeRegionRows(w, rows, "kdKelurahan", "nmKelurahan")
}

func (s *Server) listBlok(w http.ResponseWriter, r *http.Request) {
	body, err := readORPC(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	rows, err := s.db.QueryContext(r.Context(), "SELECT kd_blok, nm_blok FROM blok WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? ORDER BY kd_blok", stringParam(body, "kdPropinsi"), stringParam(body, "kdDati2"), stringParam(body, "kdKecamatan"), stringParam(body, "kdKelurahan"))
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	writeRegionRows(w, rows, "kdBlok", "nmBlok")
}

func (s *Server) listSpptByNop(w http.ResponseWriter, r *http.Request) {
	key, err := readNop(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	rows, err := s.db.QueryContext(r.Context(), spptSQL()+`
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=? AND no_urut=? AND kd_jns_op=?
		ORDER BY thn_pajak_sppt DESC`,
		key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	result, err := scanSpptRows(rows)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeJSON(w, http.StatusOK, result)
}

func (s *Server) getSppt(w http.ResponseWriter, r *http.Request) {
	body, err := readORPC(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	key := nopFromMap(body)
	if err := key.validate(); err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	year := intParam(body, "thnPajakSppt", 0)
	row := s.db.QueryRowContext(r.Context(), spptSQL()+`
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=? AND no_urut=? AND kd_jns_op=? AND thn_pajak_sppt=?`,
		key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp, year)
	result, err := scanSppt(row)
	if errors.Is(err, sql.ErrNoRows) {
		writeJSON(w, http.StatusOK, nil)
		return
	}
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeJSON(w, http.StatusOK, result)
}

func (s *Server) listSppt(w http.ResponseWriter, r *http.Request) {
	body, err := readORPC(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	limit := intParam(body, "limit", 20)
	offset := intParam(body, "offset", 0)
	if limit <= 0 || limit > 200 {
		limit = 20
	}
	where := []string{"1=1"}
	args := []any{}
	if year := intParam(body, "thnPajak", 0); year > 0 {
		where = append(where, "thn_pajak_sppt = ?")
		args = append(args, year)
	}
	if v := stringParam(body, "kdPropinsi"); v != "" {
		where = append(where, "kd_propinsi = ?")
		args = append(args, v)
	}
	if v := stringParam(body, "statusPembayaran"); v != "" {
		where = append(where, "status_pembayaran_sppt = ?")
		args = append(args, v)
	}
	var total int
	if err := s.db.QueryRowContext(r.Context(), "SELECT COUNT(*) FROM sppt WHERE "+strings.Join(where, " AND "), args...).Scan(&total); err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	args = append(args, limit, offset)
	rows, err := s.db.QueryContext(r.Context(), spptSQL()+" WHERE "+strings.Join(where, " AND ")+" ORDER BY thn_pajak_sppt DESC, kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut LIMIT ? OFFSET ?", args...)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	resultRows, err := scanSpptRows(rows)
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeJSON(w, http.StatusOK, map[string]any{"rows": resultRows, "total": total})
}

func (s *Server) getObject(r *http.Request, key nopKey) (map[string]any, error) {
	row := s.db.QueryRowContext(r.Context(), `
		SELECT t.kd_propinsi, t.kd_dati2, t.kd_kecamatan, t.kd_kelurahan, t.kd_blok, t.no_urut, t.kd_jns_op,
		       t.subject_id, t.nm_wp, t.jalan_op, t.blok_kav_no_op, t.rt_op, t.rw_op, t.kelurahan_op,
		       t.luas_bumi, t.nilai_sistem_bumi, t.njop_bumi, t.kd_status_wp, t.jns_bumi, t.jns_transaksi_op,
		       s.nm_wp, s.jalan_wp, s.blok_kav_no_wp, s.rt_wp, s.rw_wp, s.kelurahan_wp, s.kota_wp, s.kd_pos_wp, s.status_pekerjaan_wp
		FROM tax_objects t
		LEFT JOIN tax_subjects s ON s.subject_id = t.subject_id
		WHERE t.kd_propinsi=? AND t.kd_dati2=? AND t.kd_kecamatan=? AND t.kd_kelurahan=? AND t.kd_blok=? AND t.no_urut=? AND t.kd_jns_op=?`,
		key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp)
	return scanObjectDetail(row)
}

func (s *Server) nextNoUrut(r *http.Request, prop, dati2, kec, kel, blok string) (string, error) {
	var maxNo sql.NullString
	err := s.db.QueryRowContext(r.Context(), `
		SELECT MAX(no_urut) FROM tax_objects
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=?`,
		prop, dati2, kec, kel, blok).Scan(&maxNo)
	if err != nil {
		return "", err
	}
	next := 1
	if maxNo.Valid {
		if parsed, err := strconv.Atoi(maxNo.String); err == nil {
			next = parsed + 1
		}
	}
	return fmt.Sprintf("%04d", next), nil
}

type scanner interface {
	Scan(dest ...any) error
}

func objectSummarySQL(whereClause string, tail string) string {
	return `
		SELECT t.kd_propinsi, t.kd_dati2, t.kd_kecamatan, t.kd_kelurahan, t.kd_blok, t.no_urut, t.kd_jns_op,
		       t.nm_wp, t.jalan_op, t.luas_bumi, t.njop_bumi,
		       COALESCE(SUM(b.luas_bng), 0) AS total_luas_bng,
		       COALESCE(SUM(b.nilai_sistem_bng), 0) AS total_nilai_bng
		FROM tax_objects t
		LEFT JOIN buildings b
		  ON b.kd_propinsi=t.kd_propinsi AND b.kd_dati2=t.kd_dati2 AND b.kd_kecamatan=t.kd_kecamatan
		 AND b.kd_kelurahan=t.kd_kelurahan AND b.kd_blok=t.kd_blok AND b.no_urut=t.no_urut AND b.kd_jns_op=t.kd_jns_op
		` + whereClause + `
		GROUP BY t.kd_propinsi, t.kd_dati2, t.kd_kecamatan, t.kd_kelurahan, t.kd_blok, t.no_urut, t.kd_jns_op
		` + tail
}

func scanObjectSummaries(rows *sql.Rows) ([]map[string]any, error) {
	result := []map[string]any{}
	for rows.Next() {
		var kdProp, kdDati2, kdKec, kdKel, kdBlok, noUrut, kdJns string
		var nmWP, jalan sql.NullString
		var luasBumi, njopBumi, totalLuasBng, totalNilaiBng sql.NullFloat64
		if err := rows.Scan(&kdProp, &kdDati2, &kdKec, &kdKel, &kdBlok, &noUrut, &kdJns, &nmWP, &jalan, &luasBumi, &njopBumi, &totalLuasBng, &totalNilaiBng); err != nil {
			return nil, err
		}
		result = append(result, nopMap(kdProp, kdDati2, kdKec, kdKel, kdBlok, noUrut, kdJns, map[string]any{
			"nmWp":          nullableString(nmWP),
			"jalanOp":       nullableString(jalan),
			"luasBumi":      nullableFloat(luasBumi),
			"njopBumi":      nullableFloat(njopBumi),
			"totalLuasBng":  nullableFloat(totalLuasBng),
			"totalNilaiBng": nullableFloat(totalNilaiBng),
		}))
	}
	return result, rows.Err()
}

func scanObjectDetail(row scanner) (map[string]any, error) {
	var kdProp, kdDati2, kdKec, kdKel, kdBlok, noUrut, kdJns string
	var subjectID, nmWP, jalanOp, blokOp, rtOp, rwOp, kelOp, statusWP, jnsBumi, trx sql.NullString
	var luasBumi, nilaiSistemBumi, njopBumi sql.NullFloat64
	var subNmWP, jalanWP, blokWP, rtWP, rwWP, kelWP, kotaWP, posWP, kerjaWP sql.NullString
	if err := row.Scan(&kdProp, &kdDati2, &kdKec, &kdKel, &kdBlok, &noUrut, &kdJns,
		&subjectID, &nmWP, &jalanOp, &blokOp, &rtOp, &rwOp, &kelOp,
		&luasBumi, &nilaiSistemBumi, &njopBumi, &statusWP, &jnsBumi, &trx,
		&subNmWP, &jalanWP, &blokWP, &rtWP, &rwWP, &kelWP, &kotaWP, &posWP, &kerjaWP); err != nil {
		return nil, err
	}
	obj := nopMap(kdProp, kdDati2, kdKec, kdKel, kdBlok, noUrut, kdJns, map[string]any{
		"subjekPajakId":   nullableString(subjectID),
		"nmWp":            nullableString(nmWP),
		"jalanOp":         nullableString(jalanOp),
		"blokKavNoOp":     nullableString(blokOp),
		"rtOp":            nullableString(rtOp),
		"rwOp":            nullableString(rwOp),
		"kelurahanOp":     nullableString(kelOp),
		"luasBumi":        nullableFloat(luasBumi),
		"nilaiSistemBumi": nullableFloat(nilaiSistemBumi),
		"njopBumi":        nullableFloat(njopBumi),
		"kdStatusWp":      nullableString(statusWP),
		"jnsBumi":         nullableString(jnsBumi),
		"jnsTransaksiOp":  nullableString(trx),
		"subjekPajak": map[string]any{
			"subjekPajakId":     nullableString(subjectID),
			"nmWp":              nullableString(subNmWP),
			"jalanWp":           nullableString(jalanWP),
			"blokKavNoWp":       nullableString(blokWP),
			"rtWp":              nullableString(rtWP),
			"rwWp":              nullableString(rwWP),
			"kelurahanWp":       nullableString(kelWP),
			"kotaWp":            nullableString(kotaWP),
			"kdPosWp":           nullableString(posWP),
			"statusPekerjaanWp": nullableString(kerjaWP),
		},
	})
	return obj, nil
}

func buildingSQL() string {
	return `
		SELECT no_bng, luas_bng, jml_lantai_bng, jenis_bangunan, jpb, thn_dibangun_bng, thn_renovasi_bng,
		       kondisi_bng, konstruksi_bng, atap_bng, dinding_bng, lantai_bng, langit_langit_bng, nilai_sistem_bng
		FROM buildings`
}

func scanBuildings(rows *sql.Rows) ([]map[string]any, error) {
	result := []map[string]any{}
	for rows.Next() {
		item, err := scanBuilding(rows)
		if err != nil {
			return nil, err
		}
		result = append(result, item)
	}
	return result, rows.Err()
}

func scanBuilding(row scanner) (map[string]any, error) {
	var noBng int
	var luas, nilai sql.NullFloat64
	var lantai, dibangun, renovasi sql.NullInt64
	var jenis, jpb, kondisi, konstruksi, atap, dinding, lantaiBng, langit sql.NullString
	if err := row.Scan(&noBng, &luas, &lantai, &jenis, &jpb, &dibangun, &renovasi, &kondisi, &konstruksi, &atap, &dinding, &lantaiBng, &langit, &nilai); err != nil {
		return nil, err
	}
	return map[string]any{
		"noBng":           strconv.Itoa(noBng),
		"luasBng":         nullableFloat(luas),
		"jmlLantaiBng":    nullableInt(lantai),
		"jenisBangunan":   nullableString(jenis),
		"jpb":             nullableString(jpb),
		"thnDibangunBng":  nullableInt(dibangun),
		"thnRenovasiBng":  nullableInt(renovasi),
		"kondisiBng":      nullableString(kondisi),
		"konstruksiBng":   nullableString(konstruksi),
		"atapBng":         nullableString(atap),
		"dindingBng":      nullableString(dinding),
		"lantaiBng":       nullableString(lantaiBng),
		"langitLangitBng": nullableString(langit),
		"nilaiSistemBng":  nullableFloat(nilai),
	}, nil
}

func spptSQL() string {
	return `
		SELECT kd_propinsi, kd_dati2, kd_kecamatan, kd_kelurahan, kd_blok, no_urut, kd_jns_op,
		       thn_pajak_sppt, pbb_yg_harus_dibayar_sppt, status_pembayaran_sppt, tgl_jatuh_tempo_sppt,
		       denda_sppt, tgl_pembayaran_sppt, njop_bumi_sppt, njop_bangunan_sppt, njop_sppt,
		       njoptkp_sppt, tarif_sppt, pbb_terutang_sppt
		FROM sppt`
}

func scanSpptRows(rows *sql.Rows) ([]map[string]any, error) {
	result := []map[string]any{}
	for rows.Next() {
		item, err := scanSppt(rows)
		if err != nil {
			return nil, err
		}
		result = append(result, item)
	}
	return result, rows.Err()
}

func scanSppt(row scanner) (map[string]any, error) {
	var kdProp, kdDati2, kdKec, kdKel, kdBlok, noUrut, kdJns, status string
	var year int
	var due sql.NullString
	var payment sql.NullString
	var amount, fine, njopBumi, njopBng, njop, njoptkp, tarif, terutang sql.NullFloat64
	if err := row.Scan(&kdProp, &kdDati2, &kdKec, &kdKel, &kdBlok, &noUrut, &kdJns, &year, &amount, &status, &due, &fine, &payment, &njopBumi, &njopBng, &njop, &njoptkp, &tarif, &terutang); err != nil {
		return nil, err
	}
	return nopMap(kdProp, kdDati2, kdKec, kdKel, kdBlok, noUrut, kdJns, map[string]any{
		"thnPajakSppt":          year,
		"pbbYgHarusDibayarSppt": nullableFloat(amount),
		"statusPembayaranSppt":  status,
		"tglJatuhTempoSppt":     nullableString(due),
		"dendaSppt":             nullableFloat(fine),
		"denda":                 nullableFloat(fine),
		"tglPembayaranSppt":     nullableString(payment),
		"njopBumiSppt":          nullableFloat(njopBumi),
		"njopBangunanSppt":      nullableFloat(njopBng),
		"njopBng":               nullableFloat(njopBng),
		"njopSppt":              nullableFloat(njop),
		"njoptkpSppt":           nullableFloat(njoptkp),
		"tarifSppt":             nullableFloat(tarif),
		"pbbTerutang":           nullableFloat(terutang),
		"pbbYgHarusDibayar":     nullableFloat(amount),
	}), nil
}

func readNop(r *http.Request) (nopKey, error) {
	body, err := readORPC(r)
	if err != nil {
		return nopKey{}, err
	}
	key := nopFromMap(body)
	return key, key.validate()
}

func readORPC(r *http.Request) (map[string]any, error) {
	defer r.Body.Close()
	var request orpcRequest
	if err := json.NewDecoder(r.Body).Decode(&request); err != nil {
		return nil, err
	}
	if request.JSON == nil {
		request.JSON = map[string]any{}
	}
	return request.JSON, nil
}

func nopFromMap(m map[string]any) nopKey {
	return nopKey{
		KdPropinsi:  stringParam(m, "kdPropinsi"),
		KdDati2:     stringParam(m, "kdDati2"),
		KdKecamatan: stringParam(m, "kdKecamatan"),
		KdKelurahan: stringParam(m, "kdKelurahan"),
		KdBlok:      stringParam(m, "kdBlok"),
		NoUrut:      stringParam(m, "noUrut"),
		KdJnsOp:     stringParam(m, "kdJnsOp"),
	}
}

func (n nopKey) validate() error {
	segments := []struct {
		name  string
		value string
		size  int
	}{
		{name: "kdPropinsi", value: n.KdPropinsi, size: 2},
		{name: "kdDati2", value: n.KdDati2, size: 2},
		{name: "kdKecamatan", value: n.KdKecamatan, size: 3},
		{name: "kdKelurahan", value: n.KdKelurahan, size: 3},
		{name: "kdBlok", value: n.KdBlok, size: 3},
		{name: "noUrut", value: n.NoUrut, size: 4},
		{name: "kdJnsOp", value: n.KdJnsOp, size: 1},
	}
	for _, segment := range segments {
		if len(segment.value) != segment.size || !isDigits(segment.value) {
			return fmt.Errorf("%s must be %d digits", segment.name, segment.size)
		}
	}
	return nil
}

func (n nopKey) display() string {
	return n.KdPropinsi + n.KdDati2 + n.KdKecamatan + n.KdKelurahan + n.KdBlok + n.NoUrut + n.KdJnsOp
}

func nopMap(kdProp, kdDati2, kdKec, kdKel, kdBlok, noUrut, kdJns string, values map[string]any) map[string]any {
	base := map[string]any{
		"kdPropinsi":  kdProp,
		"kdDati2":     kdDati2,
		"kdKecamatan": kdKec,
		"kdKelurahan": kdKel,
		"kdBlok":      kdBlok,
		"noUrut":      noUrut,
		"kdJnsOp":     kdJns,
	}
	for key, value := range values {
		base[key] = value
	}
	return base
}

func writeRegionRows(w http.ResponseWriter, rows *sql.Rows, codeKey, nameKey string) {
	result := []map[string]any{}
	for rows.Next() {
		var code, name string
		if err := rows.Scan(&code, &name); err != nil {
			writeError(w, http.StatusInternalServerError, err)
			return
		}
		result = append(result, map[string]any{codeKey: code, nameKey: name, "kode": code, "nama": name})
	}
	if err := rows.Err(); err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeJSON(w, http.StatusOK, result)
}

func writeJSON(w http.ResponseWriter, status int, payload any) {
	writeRaw(w, status, map[string]any{"json": payload})
}

func writeRaw(w http.ResponseWriter, status int, payload any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(status)
	_ = json.NewEncoder(w).Encode(payload)
}

func writeError(w http.ResponseWriter, status int, err any) {
	message := "request failed"
	switch typed := err.(type) {
	case error:
		message = typed.Error()
	case string:
		message = typed
	default:
		message = fmt.Sprint(typed)
	}
	if status >= http.StatusInternalServerError {
		message = "internal server error"
	}
	writeRaw(w, status, map[string]any{"error": map[string]any{"message": message}})
}

func stringParam(m map[string]any, key string) string {
	value, ok := m[key]
	if !ok || value == nil {
		return ""
	}
	switch typed := value.(type) {
	case string:
		return strings.TrimSpace(typed)
	case float64:
		if math.Trunc(typed) == typed {
			return strconv.Itoa(int(typed))
		}
		return strconv.FormatFloat(typed, 'f', -1, 64)
	default:
		return strings.TrimSpace(fmt.Sprint(typed))
	}
}

func firstString(m map[string]any, key string) any {
	value := stringParam(m, key)
	if value == "" {
		return nil
	}
	return value
}

func intParam(m map[string]any, key string, fallback int) int {
	value, ok := m[key]
	if !ok || value == nil {
		return fallback
	}
	switch typed := value.(type) {
	case float64:
		return int(typed)
	case string:
		parsed, err := strconv.Atoi(strings.TrimSpace(typed))
		if err == nil {
			return parsed
		}
	}
	return fallback
}

func numberParam(m map[string]any, key string) any {
	value, ok := m[key]
	if !ok || value == nil {
		return nil
	}
	switch typed := value.(type) {
	case float64:
		return typed
	case string:
		parsed, err := strconv.ParseFloat(strings.TrimSpace(typed), 64)
		if err == nil {
			return parsed
		}
	}
	return nil
}

func nullableString(value sql.NullString) any {
	if value.Valid {
		return value.String
	}
	return nil
}

func nullableFloat(value sql.NullFloat64) any {
	if value.Valid {
		return value.Float64
	}
	return nil
}

func nullableInt(value sql.NullInt64) any {
	if value.Valid {
		return value.Int64
	}
	return nil
}

func like(value string) string {
	return "%" + value + "%"
}

func digitsOnly(value string) string {
	var builder strings.Builder
	for _, char := range value {
		if char >= '0' && char <= '9' {
			builder.WriteRune(char)
		}
	}
	return builder.String()
}

func isDigits(value string) bool {
	if value == "" {
		return false
	}
	for _, char := range value {
		if char < '0' || char > '9' {
			return false
		}
	}
	return true
}

func (s *Server) requireAdmin(next http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		if s.options.AllowPublicSave {
			next(w, r)
			return
		}
		expected := strings.TrimSpace(s.options.AdminAPIKey)
		if expected == "" {
			writeError(w, http.StatusForbidden, "write endpoint is disabled")
			return
		}
		provided := strings.TrimSpace(r.Header.Get("X-Admin-API-Key"))
		if provided == "" || provided != expected {
			writeError(w, http.StatusUnauthorized, "invalid admin API key")
			return
		}
		next(w, r)
	}
}

func (s *Server) writeSecurityHeaders(w http.ResponseWriter) {
	w.Header().Set("X-Content-Type-Options", "nosniff")
	w.Header().Set("X-Frame-Options", "DENY")
	w.Header().Set("Referrer-Policy", "no-referrer")
	w.Header().Set("Cache-Control", "no-store")
}

func (s *Server) allowedOrigin(origin string) string {
	origin = strings.TrimSpace(origin)
	if origin == "" {
		return ""
	}
	if len(s.options.AllowedOrigins) == 0 {
		return "*"
	}
	for _, allowed := range s.options.AllowedOrigins {
		if allowed == "*" || strings.EqualFold(allowed, origin) {
			return allowed
		}
	}
	return ""
}
