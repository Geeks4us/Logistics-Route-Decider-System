#!/usr/bin/env python3
"""Convert the draw.io SVG deployment diagram to PDF."""

from __future__ import annotations

import shutil
import subprocess
import sys
from pathlib import Path


SVG_PATH = Path("models/Deployment.drawio.svg")
PDF_PATH = SVG_PATH.with_suffix(".pdf")


def convert_with_cairosvg(svg_path: Path, pdf_path: Path) -> None:
    try:
        import cairosvg
    except ImportError as exc:
        raise RuntimeError("cairosvg is not installed. Run: pip install cairosvg") from exc

    cairosvg.svg2pdf(url=str(svg_path), write_to=str(pdf_path))


def convert_with_inkscape(svg_path: Path, pdf_path: Path) -> None:
    inkscape = shutil.which("inkscape")
    if not inkscape:
        raise RuntimeError("inkscape is not installed or not on PATH.")

    subprocess.run(
        [inkscape, str(svg_path), "--export-type=pdf", f"--export-filename={pdf_path}"],
        check=True,
    )


def main() -> int:
    if not SVG_PATH.exists():
        print(f"SVG not found: {SVG_PATH}")
        return 1

    for converter in (convert_with_cairosvg, convert_with_inkscape):
        try:
            converter(SVG_PATH, PDF_PATH)
            print(f"Created: {PDF_PATH}")
            return 0
        except Exception:
            continue

    print(
        "Could not convert the SVG to PDF. Install one of the following and rerun:\n"
        "  pip install cairosvg\n"
        "  or install Inkscape from https://inkscape.org/"
    )
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
