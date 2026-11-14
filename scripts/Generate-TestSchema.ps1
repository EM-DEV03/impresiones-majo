$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
$sql = Get-Content (Join-Path $project 'database/impresiones-majo.sql') -Raw
# H2 en modo MySQL: únicamente adapta opciones físicas de motor y collation.
# La aplicación nunca ejecuta este archivo sobre MySQL.
$tables = [regex]::Matches($sql, '(?s)CREATE TABLE IF NOT EXISTS .*?\) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;')
$schema = ($tables | ForEach-Object { $_.Value -replace ' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci','' }) -join "`n`n"
$inserts = [regex]::Matches($sql, '(?sm)^INSERT IGNORE INTO .*?;')
$schema += "`n`n" + (($inserts | ForEach-Object { $_.Value }) -join "`n`n")
$target = Join-Path $project 'src/test/resources'
New-Item $target -ItemType Directory -Force | Out-Null
[IO.File]::WriteAllText((Join-Path $target 'schema-test.sql'), $schema, (New-Object System.Text.UTF8Encoding($false)))
