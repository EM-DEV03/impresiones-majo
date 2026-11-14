$ErrorActionPreference = 'Stop'
$project = Split-Path $PSScriptRoot -Parent
$names = @('DB_USERNAME','DB_PASSWORD','APP_ADMIN_USERNAME','APP_ADMIN_PASSWORD')
$previous = @{}
foreach ($name in $names) { $previous[$name] = [Environment]::GetEnvironmentVariable($name, 'Process') }
function Read-Secret([string]$prompt) {
    $secret = Read-Host $prompt -AsSecureString
    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer); $secret.Dispose() }
}
try {
    if (-not $env:DB_USERNAME) { $env:DB_USERNAME = Read-Host 'Usuario de MySQL' }
    if (-not $env:DB_PASSWORD) { $env:DB_PASSWORD = Read-Secret 'Contrasena de MySQL (Enter si no tiene)' }
    if (-not $env:DB_USERNAME) { throw 'Debes indicar el usuario de MySQL.' }
    if (-not $env:APP_ADMIN_USERNAME) { $env:APP_ADMIN_USERNAME = Read-Host 'Administrador inicial (Enter si ya existe una cuenta)' }
    if ($env:APP_ADMIN_USERNAME) {
        while ($true) {
            if (-not $env:APP_ADMIN_PASSWORD) {
                $env:APP_ADMIN_PASSWORD = Read-Secret 'Contrasena del administrador inicial (minimo 12 caracteres; maximo 72 bytes UTF-8)'
            }
            if ($env:APP_ADMIN_PASSWORD -and $env:APP_ADMIN_PASSWORD.Length -ge 12 -and [Text.Encoding]::UTF8.GetByteCount($env:APP_ADMIN_PASSWORD) -le 72) {
                break
            }
            Write-Host 'La contrasena no cumple los requisitos. Escribe al menos 12 caracteres y no superes 72 bytes UTF-8.' -ForegroundColor Yellow
            $env:APP_ADMIN_PASSWORD = $null
        }
    }
    Push-Location $project
    try {
        & ./mvnw.cmd spring-boot:run
        if ($LASTEXITCODE -ne 0) { throw "Impresiones Majo termino con codigo $LASTEXITCODE. Revisa el mensaje anterior." }
    } finally { Pop-Location }
} finally {
    foreach ($name in $names) { [Environment]::SetEnvironmentVariable($name, $previous[$name], 'Process') }
}
