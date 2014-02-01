@echo off

echo "Switching code page..."
chcp 65001

echo.
echo "Invoking groovy script to anchorize .md files..."
cmd /c "groovy build/Anchor.groovy -d current"

echo.
echo "Invoking Jekyll..."
cmd /c "jekyll build --safe"
