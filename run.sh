#!/bin/bash

# Script untuk run aplikasi Android
# Usage: ./run.sh

# Get Android SDK path
if [ -z "$ANDROID_HOME" ]; then
    ANDROID_HOME="$HOME/Library/Android/sdk"
fi

ADB="$ANDROID_HOME/platform-tools/adb"

if [ ! -f "$ADB" ]; then
    echo "❌ Error: adb not found at $ADB"
    echo "Please set ANDROID_HOME environment variable"
    exit 1
fi

# Check if device is connected
echo "📱 Checking for connected devices..."
DEVICES=$("$ADB" devices | grep -v "List" | grep "device$" | wc -l | tr -d ' ')

if [ "$DEVICES" -eq 0 ]; then
    echo "❌ No device or emulator connected!"
    echo ""
    echo "Please:"
    echo "1. Start an Android emulator, OR"
    echo "2. Connect a physical device via USB with USB debugging enabled"
    echo ""
    echo "To check devices, run: adb devices"
    exit 1
fi

echo "✅ Found $DEVICES connected device(s)"
echo "🚀 Building and installing app..."
./gradlew installDebug

if [ $? -eq 0 ]; then
    echo "✅ App installed successfully!"
    echo "▶️  Launching app..."
    "$ADB" shell am start -n com.fathan.e_commerce/.MainActivity
else
    echo "❌ Build failed!"
    exit 1
fi

