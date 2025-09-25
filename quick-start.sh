#!/bin/bash

echo "🚀 OAuth2 Authorization Server - Quick Start"
echo "============================================="

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven first."
    echo "   Ubuntu/Debian: sudo apt-get install maven"
    echo "   macOS: brew install maven"
    exit 1
fi

# Check if Java is available
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 11 or higher."
    exit 1
fi

echo "✅ Prerequisites check passed"

# Build and run the application
echo "🏗️ Building the application..."
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo "✅ Build successful!"
    
    echo "🚀 Starting the OAuth2 Authorization Server..."
    echo "   Application will be available at: http://localhost:9000/auth"
    echo "   Press Ctrl+C to stop the server"
    echo ""
    
    # Run the application
    java -jar target/*.jar
else
    echo "❌ Build failed. Please check the error messages above."
    exit 1
fi

