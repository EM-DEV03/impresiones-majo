$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
$sql = Get-Content (Join-Path $project 'database/impresiones-majo.sql') -Raw
$base = Join-Path $project 'src/main/java/com/impresionesmajo/model'
$utf8 = New-Object System.Text.UTF8Encoding($false)
function Pascal([string]$s) { return (($s -split '_' | ForEach-Object { $_.Substring(0,1).ToUpper() + $_.Substring(1) }) -join '') }
function Camel([string]$s) { $p = Pascal $s; return $p.Substring(0,1).ToLower() + $p.Substring(1) }
New-Item "$base/vo","$base/dao" -ItemType Directory -Force | Out-Null
foreach ($table in [regex]::Matches($sql, '(?s)CREATE TABLE IF NOT EXISTS (\w+) \((.*?)\) ENGINE')) {
    $name = $table.Groups[1].Value
    $class = Pascal $name
    $cols = @()
    foreach ($line in ($table.Groups[2].Value -split "`n")) {
        if ($line -match '^  (\w+) (BIGINT|INT|VARCHAR|ENUM|DECIMAL|DATETIME|DATE|TINYINT)\b') {
            $type = switch ($Matches[2]) { BIGINT {'Long'} INT {'Integer'} VARCHAR {'String'} ENUM {'String'} DECIMAL {'BigDecimal'} DATETIME {'LocalDateTime'} DATE {'LocalDate'} TINYINT {'Boolean'} }
            $cols += [pscustomobject]@{Column=$Matches[1]; Field=(Camel $Matches[1]); Method=(Pascal $Matches[1]); Type=$type; Auto=($line -match 'AUTO_INCREMENT')}
        }
    }
    $fields = ($cols | ForEach-Object { "    private $($_.Type) $($_.Field);" }) -join "`n"
    $params = ($cols | ForEach-Object { "$($_.Type) $($_.Field)" }) -join ', '
    $assign = ($cols | ForEach-Object { "        this.$($_.Field) = $($_.Field);" }) -join "`n"
    $methods = ($cols | ForEach-Object { "    public $($_.Type) get$($_.Method)() { return $($_.Field); }`n    public void set$($_.Method)($($_.Type) value) { this.$($_.Field) = value; }" }) -join "`n"
    $identity = if ($cols.Column -contains 'id') {'"id=" + id'} else {'"relacion"'}
    $vo = @"
package com.impresionesmajo.model.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Mapeo de la tabla $name. No expone datos personales ni secretos en logs. */
public class ${class}VO {
$fields
    public ${class}VO() {}
    public ${class}VO($params) {
$assign
    }
$methods
    @Override public String toString() { return "${class}VO[" + $identity + "]"; }
}
"@
    [IO.File]::WriteAllText("$base/vo/${class}VO.java", $vo, $utf8)
    if ($cols.Column -contains 'id') {
        $dao = @"
package com.impresionesmajo.model.dao;

import com.impresionesmajo.model.vo.${class}VO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ${class}DAO extends BaseDAO<${class}VO> {
    public ${class}DAO(JdbcTemplate jdbc) {
        super(jdbc, "$name", ${class}VO.class, "$(($cols.Column) -join ',')");
    }
}
"@
    } else {
        $a = $cols[0]; $b = $cols[1]
        $dao = @"
package com.impresionesmajo.model.dao;

import java.util.List;
import com.impresionesmajo.model.vo.${class}VO;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ${class}DAO {
    private final JdbcTemplate jdbc;
    public ${class}DAO(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<${class}VO> findAll() {
        return jdbc.query("SELECT $($a.Column), $($b.Column) FROM $name", new BeanPropertyRowMapper<>(${class}VO.class));
    }
    public void insert(${class}VO value) {
        jdbc.update("INSERT INTO $name ($($a.Column), $($b.Column)) VALUES (?, ?)", value.get$($a.Method)(), value.get$($b.Method)());
    }
    public void remove(Long first, Long second) {
        jdbc.update("DELETE FROM $name WHERE $($a.Column)=? AND $($b.Column)=?", first, second);
    }
}
"@
    }
    [IO.File]::WriteAllText("$base/dao/${class}DAO.java", $dao, $utf8)
}
