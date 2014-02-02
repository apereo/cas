@echo off

echo "Switching code page..."
chcp 65001

echo.
echo "Invoking groovy script to anchorize .md files..."
cmd /c "groovy build/Anchor.groovy -d current"

echo.
echo "Invoking Jekyll..."
cmd /c "jekyll build --safe"

echo. 
rem echo "Generating javadocs..."
rem cmd /c "mvn site site:stage --file /cas-server/pom.xml"

echo.
rem echo "Copying javadocs to documentation..."
rem cmd /c "cp -r /cas-server/target/staging/* current/javadocs"