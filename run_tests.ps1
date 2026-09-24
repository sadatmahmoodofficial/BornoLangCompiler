# Set console to UTF-8
chcp 65001 | Out-Null
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "       BornoLang Test Suite Runner      " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Compile Java Sources
Write-Host "`n[1/4] Compiling Java sources..." -ForegroundColor Yellow
javac -encoding UTF-8 -cp ".;antlr-4.13.1-complete.jar" *.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "Compilation successful." -ForegroundColor Green

# 2. Run Test 1: Valid General Test
Write-Host "`n----------------------------------------"
Write-Host "[2/4] Testing: tests/01_valid_general.bn" -ForegroundColor Yellow
Write-Host "----------------------------------------"
java -cp ".;antlr-4.13.1-complete.jar" Main tests/01_valid_general.bn

# Execute generated Python code
if (Test-Path "output.py") {
    Write-Host "`nExecuting Generated Python Code (output.py):" -ForegroundColor Magenta
    python output.py
}

# 3. Run Test 2: Semantic Errors
Write-Host "`n----------------------------------------"
Write-Host "[3/4] Testing: tests/02_semantic_error.bn" -ForegroundColor Yellow
Write-Host "----------------------------------------"
java -cp ".;antlr-4.13.1-complete.jar" Main tests/02_semantic_error.bn

# 4. Run Test 3: Syntax Errors
Write-Host "`n----------------------------------------"
Write-Host "[4/4] Testing: tests/03_syntax_error.bn" -ForegroundColor Yellow
Write-Host "----------------------------------------"
java -cp ".;antlr-4.13.1-complete.jar" Main tests/03_syntax_error.bn

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "           All Tests Completed          " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 5. Run Test 4: String Literals & Concatenation
Write-Host "`n----------------------------------------"
Write-Host "[5/5] Testing: tests/04_strings_input.bn" -ForegroundColor Yellow
Write-Host "----------------------------------------"
java -cp ".;antlr-4.13.1-complete.jar" Main tests/04_strings_input.bn

if (Test-Path "output.py") {
    Write-Host "`nExecuting Generated Python Code (output.py):" -ForegroundColor Magenta
    py output.py
}