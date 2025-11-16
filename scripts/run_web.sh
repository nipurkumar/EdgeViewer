#!/bin/bash


echo "🚀 Starting Real-Time Edge Detection Web Viewer..."

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'
if ! command -v node &> /dev/null; then
    echo -e "${RED}❌ Node.js is not installed. Please install Node.js first.${NC}"
    exit 1
fi

if ! command -v tsc &> /dev/null; then
    echo -e "${YELLOW}⚠️  TypeScript not found. Installing...${NC}"
    npm install -g typescript
fi

cd ../web || exit

if [ -f "package.json" ]; then
    echo -e "${GREEN}📦 Installing dependencies...${NC}"
    npm install
fi

echo -e "${GREEN}🔨 Compiling TypeScript...${NC}"
tsc

if command -v python3 &> /dev/null; then
    echo -e "${GREEN}🌐 Starting web server on http://localhost:8000${NC}"
    python3 -m http.server 8000
elif command -v python &> /dev/null; then
    echo -e "${GREEN}🌐 Starting web server on http://localhost:8000${NC}"
    python -m SimpleHTTPServer 8000
else
    echo -e "${YELLOW}⚠️  Python not found. Using Node.js http-server...${NC}"

    if ! command -v http-server &> /dev/null; then
        npm install -g http-server
    fi

    echo -e "${GREEN}🌐 Starting web server on http://localhost:8000${NC}"
    http-server -p 8000
fi