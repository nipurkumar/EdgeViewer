#!/bin/bash

echo "🔨 Building Flam Edge Detection Web Viewer..."

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

cd ../web || exit

echo -e "${YELLOW}🧹 Cleaning previous build...${NC}"
rm -rf dist/

if ! command -v tsc &> /dev/null; then
    echo -e "${YELLOW}⚠️  TypeScript not found. Installing...${NC}"
    npm install -g typescript
fi

echo -e "${GREEN}📝 Compiling TypeScript files...${NC}"
tsc

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Build completed successfully!${NC}"

    mkdir -p dist

    cp index.html dist/
    cp styles.css dist/

    cp -r assets dist/

    cp main.js dist/
    cp -r helpers dist/

    echo -e "${GREEN}📦 Production build ready in 'dist' folder${NC}"
    echo -e "${GREEN}📊 Build size:${NC}"
    du -sh dist/

else
    echo -e "${RED}❌ Build failed. Please check for TypeScript errors.${NC}"
    exit 1
fi

if command -v terser &> /dev/null; then
    echo -e "${GREEN}🗜️  Minifying JavaScript...${NC}"
    terser dist/main.js -o dist/main.min.js --compress --mangle
    mv dist/main.min.js dist/main.js
fi

echo -e "${GREEN}🎉 Build process complete!${NC}"