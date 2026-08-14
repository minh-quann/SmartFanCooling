#!/usr/bin/env bash
# Smart Fan Cooling - Continuous Auto-Build & Launch Script

echo "⚡ Starting Smart Fan Cooling in Continuous Watch Mode (-t)..."
echo "💡 Gradle will automatically re-build & push to your device on every file save (Ctrl + S)!"

# Initial compile and launch
./gradlew installDebug && adb shell am start -n com.buwin.smartfancooling/.MainActivity

# Keep watching for file changes continuously
./gradlew installDebug -t
