#!/bin/bash

# Script untuk cek device yang terhubung
# Usage: ./check-device.sh

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

echo "📱 Checking for connected devices..."
echo ""
"$ADB" devices
echo ""

DEVICES=$("$ADB" devices | grep -v "List" | grep "device$" | wc -l | tr -d ' ')

if [ "$DEVICES" -eq 0 ]; then
    echo "❌ No device or emulator connected!"
    echo ""
    echo "To connect a device:"
    echo "1. For Emulator: Open Android Studio > Device Manager > Start an emulator"
    echo "2. For Physical Device:"
    echo "   - Enable Developer Options on your device"
    echo "   - Enable USB Debugging"
    echo "   - Connect via USB"
    echo "   - Accept the USB debugging prompt on your device"
else
    echo "✅ Found $DEVICES connected device(s)"
fi

