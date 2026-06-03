package api

import (
	"database/sql"
	"errors"
	"net/http"
	"strconv"
	"strings"
)

func (s *Server) restListPropinsi(w http.ResponseWriter, r *http.Request) {
	rows, err := s.db.QueryContext(r.Context(), "SELECT kd_propinsi, nm_propinsi FROM propinsi ORDER BY kd_propinsi")
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	writeRESTRegionRows(w, rows, "kdPropinsi", "nmPropinsi")
}

func (s *Server) restListDati2(w http.ResponseWriter, r *http.Request) {
	rows, err := s.db.QueryContext(r.Context(), "SELECT kd_dati2, nm_dati2 FROM dati2 WHERE kd_propinsi=? ORDER BY kd_dati2", r.URL.Query().Get("kdPropinsi"))
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	writeRESTRegionRows(w, rows, "kdDati2", "nmDati2")
}

func (s *Server) restListKecamatan(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query()
	rows, err := s.db.QueryContext(r.Context(), "SELECT kd_kecamatan, nm_kecamatan FROM kecamatan WHERE kd_propinsi=? AND kd_dati2=? ORDER BY kd_kecamatan", q.Get("kdPropinsi"), q.Get("kdDati2"))
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	writeRESTRegionRows(w, rows, "kdKecamatan", "nmKecamatan")
}

func (s *Server) restListKelurahan(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query()
	rows, err := s.db.QueryContext(r.Context(), "SELECT kd_kelurahan, nm_kelurahan FROM kelurahan WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? ORDER BY kd_kelurahan", q.Get("kdPropinsi"), q.Get("kdDati2"), q.Get("kdKecamatan"))
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	writeRESTRegionRows(w, rows, "kdKelurahan", "nmKelurahan")
}

func (s *Server) restListBlok(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query()
	rows, err := s.db.QueryContext(r.Context(), "SELECT kd_blok, nm_blok FROM blok WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? ORDER BY kd_blok", q.Get("kdPropinsi"), q.Get("kdDati2"), q.Get("kdKecamatan"), q.Get("kdKelurahan"))
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	defer rows.Close()
	writeRESTRegionRows(w, rows, "kdBlok", "nmBlok")
}

func (s *Server) restSearchObjekPajak(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query()
	query := strings.TrimSpace(q.Get("query"))
	limit := intQuery(q.Get("limit"), 10)
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
	writeRaw(w, http.StatusOK, result)
}

func (s *Server) restListObjekPajakDetails(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query()
	limit := intQuery(q.Get("limit"), 20)
	offset := intQuery(q.Get("offset"), 0)
	if limit <= 0 || limit > 200 {
		limit = 20
	}
	where := []string{"1=1"}
	args := []any{}
	if v := q.Get("kdPropinsi"); v != "" {
		where = append(where, "t.kd_propinsi = ?")
		args = append(args, v)
	}
	if v := q.Get("kdDati2"); v != "" {
		where = append(where, "t.kd_dati2 = ?")
		args = append(args, v)
	}
	if v := q.Get("search"); v != "" {
		where = append(where, "(lower(t.nm_wp) LIKE lower(?) OR lower(t.jalan_op) LIKE lower(?) OR t.no_urut LIKE ?)")
		args = append(args, like(v), like(v), like(v))
	}
	var total int
	if err := s.db.QueryRowContext(r.Context(), "SELECT COUNT(*) FROM tax_objects t WHERE "+strings.Join(where, " AND "), args...).Scan(&total); err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	args = append(args, limit, offset)
	rows, err := s.db.QueryContext(r.Context(), objectSummarySQL("WHERE "+strings.Join(where, " AND "), `
		ORDER BY t.kd_propinsi, t.kd_dati2, t.kd_kecamatan, t.kd_kelurahan, t.kd_blok, t.no_urut
		LIMIT ? OFFSET ?`), args...)
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
	writeRaw(w, http.StatusOK, map[string]any{"rows": resultRows, "total": total})
}

func (s *Server) restGetObjekPajakByNop(w http.ResponseWriter, r *http.Request) {
	key, err := nopFromText(r.PathValue("nop"))
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	obj, err := s.getObject(r, key)
	if errors.Is(err, sql.ErrNoRows) {
		writeRaw(w, http.StatusNotFound, map[string]any{"error": map[string]any{"message": "object not found"}})
		return
	}
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeRaw(w, http.StatusOK, obj)
}

func (s *Server) restListSpptByNop(w http.ResponseWriter, r *http.Request) {
	key, err := nopFromText(r.PathValue("nop"))
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	s.writeSpptByNop(w, r, key, false)
}

func (s *Server) restGetTunggakan(w http.ResponseWriter, r *http.Request) {
	key, err := nopFromText(r.PathValue("nop"))
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	s.writeSpptByNop(w, r, key, true)
}

func (s *Server) restListBuildingsByNop(w http.ResponseWriter, r *http.Request) {
	key, err := nopFromText(r.PathValue("nop"))
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	rows, err := s.db.QueryContext(r.Context(), buildingSQL()+`
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=? AND no_urut=? AND kd_jns_op=?
		ORDER BY no_bng`, key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp)
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
	writeRaw(w, http.StatusOK, result)
}

func (s *Server) restGetBuilding(w http.ResponseWriter, r *http.Request) {
	key, noBng, err := restBuildingKey(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	row := s.db.QueryRowContext(r.Context(), buildingSQL()+`
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=? AND no_urut=? AND kd_jns_op=? AND no_bng=?`,
		key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp, noBng)
	result, err := scanBuilding(row)
	if errors.Is(err, sql.ErrNoRows) {
		writeRaw(w, http.StatusNotFound, map[string]any{"error": map[string]any{"message": "building not found"}})
		return
	}
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeRaw(w, http.StatusOK, result)
}

func (s *Server) restListFacilities(w http.ResponseWriter, r *http.Request) {
	key, noBng, err := restBuildingKey(r)
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	rows, err := s.db.QueryContext(r.Context(), `
		SELECT nama_fasilitas, jumlah, satuan, keterangan
		FROM building_facilities
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=? AND no_urut=? AND kd_jns_op=? AND no_bng=?
		ORDER BY nama_fasilitas`, key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp, noBng)
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
	writeRaw(w, http.StatusOK, result)
}

func (s *Server) restGetSppt(w http.ResponseWriter, r *http.Request) {
	key, err := nopFromText(r.PathValue("nop"))
	if err != nil {
		writeError(w, http.StatusBadRequest, err)
		return
	}
	year, err := strconv.Atoi(r.PathValue("year"))
	if err != nil || year < 1900 || year > 2200 {
		writeError(w, http.StatusBadRequest, "year must be valid")
		return
	}
	row := s.db.QueryRowContext(r.Context(), spptSQL()+`
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=? AND no_urut=? AND kd_jns_op=? AND thn_pajak_sppt=?`,
		key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp, year)
	result, err := scanSppt(row)
	if errors.Is(err, sql.ErrNoRows) {
		writeRaw(w, http.StatusNotFound, map[string]any{"error": map[string]any{"message": "SPPT not found"}})
		return
	}
	if err != nil {
		writeError(w, http.StatusInternalServerError, err)
		return
	}
	writeRaw(w, http.StatusOK, result)
}

func (s *Server) restListSppt(w http.ResponseWriter, r *http.Request) {
	q := r.URL.Query()
	limit := intQuery(q.Get("limit"), 20)
	offset := intQuery(q.Get("offset"), 0)
	if limit <= 0 || limit > 200 {
		limit = 20
	}
	where := []string{"1=1"}
	args := []any{}
	if year := intQuery(q.Get("thnPajak"), 0); year > 0 {
		where = append(where, "thn_pajak_sppt = ?")
		args = append(args, year)
	}
	if v := q.Get("kdPropinsi"); v != "" {
		where = append(where, "kd_propinsi = ?")
		args = append(args, v)
	}
	if v := q.Get("statusPembayaran"); v != "" {
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
	writeRaw(w, http.StatusOK, map[string]any{"rows": resultRows, "total": total})
}

func (s *Server) writeSpptByNop(w http.ResponseWriter, r *http.Request, key nopKey, onlyUnpaid bool) {
	filter := ""
	if onlyUnpaid {
		filter = " AND (status_pembayaran_sppt = '0' OR tgl_pembayaran_sppt IS NULL)"
	}
	rows, err := s.db.QueryContext(r.Context(), spptSQL()+`
		WHERE kd_propinsi=? AND kd_dati2=? AND kd_kecamatan=? AND kd_kelurahan=? AND kd_blok=? AND no_urut=? AND kd_jns_op=?`+filter+`
		ORDER BY thn_pajak_sppt DESC`, key.KdPropinsi, key.KdDati2, key.KdKecamatan, key.KdKelurahan, key.KdBlok, key.NoUrut, key.KdJnsOp)
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
	writeRaw(w, http.StatusOK, result)
}

func writeRESTRegionRows(w http.ResponseWriter, rows *sql.Rows, codeKey, nameKey string) {
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
	writeRaw(w, http.StatusOK, result)
}

func nopFromText(value string) (nopKey, error) {
	digits := digitsOnly(value)
	if len(digits) != 18 {
		return nopKey{}, errors.New("NOP must contain 18 digits")
	}
	key := nopKey{
		KdPropinsi:  digits[0:2],
		KdDati2:     digits[2:4],
		KdKecamatan: digits[4:7],
		KdKelurahan: digits[7:10],
		KdBlok:      digits[10:13],
		NoUrut:      digits[13:17],
		KdJnsOp:     digits[17:18],
	}
	return key, key.validate()
}

func restBuildingKey(r *http.Request) (nopKey, int, error) {
	key, err := nopFromText(r.PathValue("nop"))
	if err != nil {
		return nopKey{}, 0, err
	}
	noBng, err := strconv.Atoi(r.PathValue("noBng"))
	if err != nil || noBng < 1 {
		return nopKey{}, 0, errors.New("noBng must be a positive integer")
	}
	return key, noBng, nil
}

func intQuery(value string, fallback int) int {
	if strings.TrimSpace(value) == "" {
		return fallback
	}
	parsed, err := strconv.Atoi(value)
	if err != nil {
		return fallback
	}
	return parsed
}
