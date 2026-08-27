# AutoClicker Pro - Modern Android Auto Clicker

A modern, feature-rich auto clicker application for Android built with Kotlin and Material Design 3.

## Features

- **Automatic Clicking**: Perform automated clicks at specified intervals
- **Customizable Settings**:
  - Configurable click interval (milliseconds)
  - Target X/Y position selection
  - Random offset to avoid detection
  - Unlimited or limited click count
- **Modern UI**: Built with Material Design 3 components
- **Accessibility Service**: Uses Android's Accessibility API for reliable clicking
- **Overlay Permission**: Supports drawing over other apps
- **Vibration Feedback**: Optional haptic feedback on start/stop
- **Persistent Settings**: Saves your preferences between sessions

## Requirements

- Android 7.0 (API 24) or higher
- Android Studio Arctic Fox or later
- Gradle 8.0+
- JDK 17

## Permissions

The app requires the following permissions:

1. **SYSTEM_ALERT_WINDOW**: To show overlay controls over other apps
2. **BIND_ACCESSIBILITY_SERVICE**: To perform automatic clicks using Accessibility API
3. **FOREGROUND_SERVICE**: To run the clicking service in the background
4. **POST_NOTIFICATIONS**: For notification channel (Android 13+)

## Building the App

1. Clone this repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run on your device or emulator

```bash
./gradlew assembleDebug
```

## How to Use

1. **Grant Permissions**:
   - Launch the app
   - Grant overlay permission when prompted
   - Enable the Accessibility Service from settings

2. **Configure Settings**:
   - Set click interval (time between clicks in milliseconds)
   - Set target X and Y coordinates (or use screen position picker in future updates)
   - Optionally enable random offset to vary click positions
   - Set maximum click count or leave unlimited

3. **Start Clicking**:
   - Press the "Start Clicking" button
   - The service will begin clicking at the specified position
   - Press "Stop Clicking" to stop

## Project Structure

```
app/
├── src/main/
│   ├── java/com/autoclicker/app/
│   │   ├── ui/
│   │   │   └── MainActivity.kt          # Main UI activity
│   │   ├── service/
│   │   │   └── ClickAccessibilityService.kt  # Accessibility service for clicking
│   │   ├── util/
│   │   │   └── ClickPreferences.kt      # SharedPreferences helper
│   │   └── AutoClickerApplication.kt    # Application class
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml        # Main activity layout
│   │   ├── values/
│   │   │   ├── strings.xml              # String resources
│   │   │   ├── colors.xml               # Color definitions
│   │   │   └── themes.xml               # App themes
│   │   └── xml/
│   │       └── accessibility_service_config.xml  # Accessibility service config
│   └── AndroidManifest.xml
├── build.gradle
└── proguard-rules.pro
```

## Technologies Used

- **Kotlin**: Primary programming language
- **Material Design 3**: Modern UI components
- **ViewBinding**: Type-safe view access
- **SharedPreferences**: Local data persistence
- **AccessibilityService**: Core clicking functionality
- **Coroutines**: Async operations (future enhancement)
- **Lifecycle Components**: Architecture components

## Future Enhancements

- [ ] Multi-point clicking support
- [ ] Gesture recording and playback
- [ ] Screen position picker UI
- [ ] Click patterns (swipe, long press, etc.)
- [ ] Profiles for different games/apps
- [ ] Floating control panel
- [ ] Click statistics and history
- [ ] Dark mode support
- [ ] Multiple languages

## Disclaimer

This app is intended for legitimate automation purposes such as:
- Accessibility assistance
- Testing applications
- Automating repetitive tasks

Please use responsibly and respect the terms of service of other applications. Do not use for cheating in games or violating app policies.

## License

MIT License - Feel free to use and modify for personal or commercial projects.

## Support

For issues and feature requests, please open an issue on GitHub.
