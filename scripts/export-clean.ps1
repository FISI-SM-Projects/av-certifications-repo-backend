param(
    [string]$OutputDir = "dist/export"
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$ExportRoot = Join-Path $ProjectRoot $OutputDir
$ZipPath = Join-Path $ExportRoot "av-certifications-repo-backend-clean.zip"
$StagingRoot = Join-Path $ExportRoot "_staging"

function Should-Exclude([string]$RelativePath) {
    $path = $RelativePath -replace "\\", "/"
    return $path -match '(^|/)\.git(/|$)' `
        -or $path -match '(^|/)target(/|$)' `
        -or $path -match '(^|/)dist(/|$)' `
        -or $path -match '(^|/)exports(/|$)' `
        -or $path -match '(^|/)storage/(?!\.gitkeep$)' `
        -or $path -match '(^|/)\.idea(/|$)' `
        -or $path -match '(^|/)\.vscode(/|$)' `
        -or ($path -match '(^|/)\.env(\..*)?$' -and $path -ne ".env.example") `
        -or $path -match '\.(log|tmp|pdf)$' `
        -or $path -match '(^|/)(request|metadata|source-summary)\.json$' `
        -or $path -match '(^|/)Thumbs\.db$' `
        -or $path -match '(^|/)\.DS_Store$'
}

if (Test-Path $StagingRoot) {
    Remove-Item -LiteralPath $StagingRoot -Recurse -Force
}
New-Item -ItemType Directory -Force -Path $StagingRoot | Out-Null

Get-ChildItem -LiteralPath $ProjectRoot -Force | ForEach-Object {
    $relative = $_.Name
    if (-not (Should-Exclude $relative)) {
        Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $StagingRoot $relative) -Recurse -Force
    }
}

Get-ChildItem -LiteralPath $StagingRoot -Force -Recurse | Where-Object {
    $relative = $_.FullName.Substring($StagingRoot.Length).TrimStart("\", "/")
    Should-Exclude $relative
} | Sort-Object FullName -Descending | ForEach-Object {
    Remove-Item -LiteralPath $_.FullName -Recurse -Force
}

if (Test-Path $ZipPath) {
    Remove-Item -LiteralPath $ZipPath -Force
}
Compress-Archive -Path (Join-Path $StagingRoot "*") -DestinationPath $ZipPath -Force
Remove-Item -LiteralPath $StagingRoot -Recurse -Force

Write-Host "ZIP limpio generado: $ZipPath"
