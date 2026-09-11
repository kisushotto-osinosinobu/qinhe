$ErrorActionPreference = 'Stop'

if (-not (Test-Path (Join-Path $PSScriptRoot '..\.env'))) {
    throw '缺少 .env。请先 Copy-Item .env.example .env 并修改密码和 JWT_SECRET。'
}

Push-Location (Join-Path $PSScriptRoot '..')
try {
    docker compose up -d --build
    docker compose ps
    Write-Host '后端启动中，请稍后访问 http://localhost:8080/actuator/health'
    Write-Host 'Web 请在另一 PowerShell 中运行：Set-Location web; pnpm dev'
} finally {
    Pop-Location
}

