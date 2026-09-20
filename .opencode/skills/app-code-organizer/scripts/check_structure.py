#!/usr/bin/env python3
"""Diagnóstico da organização de código Kotlin/Java.

Verifica:
  1. package declarado != pasta onde o ficheiro está
  2. ficheiros com demasiadas linhas
  3. nome do ficheiro != nome da única classe/interface/object de topo
  4. pastas com demasiados ficheiros soltos

Uso:
  python check_structure.py app/src
  python check_structure.py app/src --max-lines 400 --max-files-per-dir 15

Código de saída: 0 sem problemas, 1 se houver problemas.
"""
import argparse
import re
import sys
from pathlib import Path

IGNORED_DIRS = {"build", ".gradle", ".git", "node_modules", ".idea", "generated", ".opencode"}
EXTENSIONS = {".kt", ".java"}

PACKAGE_RE = re.compile(r"^\s*package\s+([\w.]+)", re.M)
# Declaração de topo: começa na coluna 0, com modificadores opcionais.
TOP_LEVEL_RE = re.compile(
    r"^(?:@\w+(?:\([^)]*\))?\s+)*"
    r"(?:(?:public|internal|private|protected|abstract|open|sealed|data|enum|"
    r"annotation|value|inline|fun|final|static)\s+)*"
    r"(?:class|interface|object)\s+(\w+)",
    re.M,
)
SOURCE_ROOT_RE = re.compile(r"src/[^/]+/(?:java|kotlin)/(.+)/[^/]+$")


def iter_source_files(root: Path):
    for path in root.rglob("*"):
        if path.suffix not in EXTENSIONS or not path.is_file():
            continue
        if any(part in IGNORED_DIRS for part in path.parts):
            continue
        yield path


def expected_package(path: Path):
    match = SOURCE_ROOT_RE.search(path.as_posix())
    return match.group(1).replace("/", ".") if match else None


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("path", nargs="?", default=".", help="pasta a analisar (ex.: app/src)")
    parser.add_argument("--max-lines", type=int, default=300, help="limite de linhas por ficheiro (300)")
    parser.add_argument("--max-files-per-dir", type=int, default=12, help="limite de ficheiros por pasta (12)")
    args = parser.parse_args()

    root = Path(args.path)
    if not root.exists():
        print(f"Caminho não encontrado: {root}", file=sys.stderr)
        return 2

    package_issues, size_issues, name_issues = [], [], []
    files_per_dir = {}
    total = 0

    for path in sorted(iter_source_files(root)):
        total += 1
        files_per_dir.setdefault(path.parent, []).append(path)
        try:
            text = path.read_text(encoding="utf-8", errors="replace")
        except OSError as exc:
            print(f"Não foi possível ler {path}: {exc}", file=sys.stderr)
            continue

        # 1. package vs pasta
        declared = PACKAGE_RE.search(text)
        expected = expected_package(path)
        if declared and expected and declared.group(1) != expected:
            package_issues.append((path, declared.group(1), expected))

        # 2. tamanho
        lines = text.count("\n") + 1
        if lines > args.max_lines:
            size_issues.append((path, lines))

        # 3. nome do ficheiro vs classe de topo (só quando há exatamente uma)
        top_levels = TOP_LEVEL_RE.findall(text)
        if len(top_levels) == 1 and top_levels[0] != path.stem:
            name_issues.append((path, top_levels[0]))

    crowded = [(d, len(f)) for d, f in files_per_dir.items() if len(f) > args.max_files_per_dir]

    print(f"Ficheiros analisados: {total}\n")

    def section(title, items, fmt):
        print(f"## {title} ({len(items)})")
        for item in items:
            print("  - " + fmt(item))
        if not items:
            print("  ok")
        print()

    section("Package diferente da pasta", package_issues,
            lambda i: f"{i[0]}\n      declarado: {i[1]}\n      esperado:  {i[2]}")
    section(f"Ficheiros com mais de {args.max_lines} linhas", sorted(size_issues, key=lambda i: -i[1]),
            lambda i: f"{i[0]} ({i[1]} linhas)")
    section("Nome do ficheiro diferente da classe", name_issues,
            lambda i: f"{i[0]} contém '{i[1]}'")
    section(f"Pastas com mais de {args.max_files_per_dir} ficheiros", sorted(crowded, key=lambda i: -i[1]),
            lambda i: f"{i[0]} ({i[1]} ficheiros)")

    problems = len(package_issues) + len(size_issues) + len(name_issues) + len(crowded)
    print(f"Total de problemas: {problems}")
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
