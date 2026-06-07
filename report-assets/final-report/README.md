# Final Report PBB-Ku

File utama:

- `final_report_pbbku_ieee.tex`

Format:

- IEEE conference LaTeX template melalui `report-assets/ref/IEEE-conference-template-062824/IEEEtran.cls`.
- Struktur IMRAD pada badan utama laporan.
- Lampiran berisi weekly progress, mentoring dengan Mas Ariefan, source code/development process, video demonstration link, dan AI declaration.

Catatan placeholder:

- Bagian `Video Demonstration Link` sengaja berisi `-`.
- Bagian `AI Declaration` sengaja berisi `-`.

Kompilasi dari root repository:

```powershell
pdflatex -interaction=nonstopmode -output-directory=report-assets/final-report report-assets/final-report/final_report_pbbku_ieee.tex
pdflatex -interaction=nonstopmode -output-directory=report-assets/final-report report-assets/final-report/final_report_pbbku_ieee.tex
```

Jika menggunakan Overleaf, upload folder berikut agar semua path tersedia:

- `report-assets/final-report`
- `report-assets/ref/IEEE-conference-template-062824`
- `report-assets/media`
- `docs/diagram`

