@echo off

echo "Switching code page..."
chcp 65001

echo.
echo "Invoking Jekyll..."
cmd /c "bundle exec jekyll build --safe --trace"
