# Force Update System Documentation

## Overview
The NotePilot app includes a comprehensive Force Update system that ensures users are always running the latest version of the app. The system can enforce mandatory updates or show optional update prompts based on Firebase configuration.

## Features

### 🎯 Core Functionality
- **Version Checking**: Automatic version comparison on app startup
- **Force Update**: Mandatory updates that prevent app usage until updated
- **Optional Updates**: Non-blocking update prompts for new features
- **Animated UI**: Beautiful bottom sheet with smooth animations
- **Firebase Integration**: Remote configuration for update parameters
- **Play Store Integration**: Direct links to app store for updates

### 🔧 Technical Implementation

#### 1. Data Models
- **AppUpdate**: Main update configuration model with Firebase integration
- **Version Comparison**: Semantic version comparison logic
- **Update States**: Force update vs optional update handling

#### 2. Repository Layer
- **UpdateRepository**: Handles Firebase Firestore operations
- **Version Management**: Current app version detection
- **Update Logic**: Version comparison and update requirement checks

#### 3. ViewModel
- **UpdateViewModel**: Business logic and state management
- **Update Checking**: Automatic update verification on app start
- **State Management**: Update dialog visibility and force update flags

#### 4. UI Components
- **ForceUpdateBottomSheet**: Animated bottom sheet with modern design
- **Update Animations**: Smooth slide-in, fade, and scale animations
- **Responsive Design**: Adapts to different screen sizes

### Firebase Configuration
```
Collection: app_config
Document ID: update_info
Fields:
- latestVersion: String (e.g., "1.2.0")
- minimumVersion: String (e.g., "1.0.0")
- forceUpdate: Boolean
- updateTitle: String
- updateMessage: String
- playStoreUrl: String (optional)
- isEnabled: Boolean
```

### Configuration Examples

#### Force Update Configuration
```kotlin
AppUpdate(
    latestVersion = "1.2.0",
    minimumVersion = "1.1.0",
    forceUpdate = true,
    updateTitle = "Critical Update Required!",
    updateMessage = "This update includes important security fixes and performance improvements.",
    playStoreUrl = "https://play.google.com/store/apps/details?id=com.white.notepilot",
    isEnabled = true
)
```

#### Optional Update Configuration
```kotlin
AppUpdate(
    latestVersion = "1.2.0",
    minimumVersion = "1.0.0",
    forceUpdate = false,
    updateTitle = "New Update Available!",
    updateMessage = "We've added new features and fixed some bugs to make your experience as smooth as possible",
    playStoreUrl = "",
    isEnabled = true
)
```

## Update Flow

### Automatic Check Process
1. App starts and MainActivity initializes
2. UpdateViewModel automatically checks for updates
3. Firebase configuration is fetched
4. Current app version is compared with remote versions
5. Update requirement is determined based on configuration

### Force Update Flow
1. User opens app
2. System detects version below minimum required
3. Force update bottom sheet appears with animations
4. User can only "Update Now" or "Exit"
5. Update button opens Play Store
6. Exit button closes the app completely
7. Dialog cannot be dismissed until update

### Optional Update Flow
1. User opens app
2. System detects newer version available
3. Optional update bottom sheet appears
4. User can "Update Now" or dismiss
5. Update button opens Play Store
6. User can continue using app if dismissed

## UI/UX Features

### Animations
- **Slide-in Animation**: Bottom sheet slides up from bottom
- **Fade Animations**: Smooth opacity transitions for content
- **Scale Animation**: Icon and card scaling effects
- **Staggered Animations**: Sequential appearance of elements

### Design Elements
- **Material Design 3**: Modern design language
- **Rounded Corners**: 24dp corner radius for modern look
- **Elevation**: 16dp elevation for depth
- **Color Scheme**: Adaptive colors based on theme
- **Typography**: Clear hierarchy with proper font weights

### Accessibility
- **Screen Reader Support**: Proper content descriptions
- **High Contrast**: Readable text and button colors
- **Touch Targets**: Minimum 48dp touch targets
- **Focus Management**: Proper focus handling

## Security Features

### Version Validation
- **Semantic Versioning**: Proper version comparison logic
- **Input Validation**: Safe parsing of version strings
- **Error Handling**: Graceful handling of malformed versions

### Firebase Security
- **Read-only Access**: App only reads update configuration
- **Firestore Rules**: Proper security rules for update collection
- **Offline Handling**: Graceful degradation when offline

## Testing

### Manual Testing
1. **Force Update Test**: Set minimum version higher than current
2. **Optional Update Test**: Set latest version higher than current
3. **No Update Test**: Set versions equal to current
4. **Offline Test**: Test behavior without internet connection

### Firebase Test Data
```javascript
// Force update scenario
{
  "latestVersion": "2.0.0",
  "minimumVersion": "1.5.0", // Higher than current app version
  "forceUpdate": true,
  "updateTitle": "Critical Update Required!",
  "updateMessage": "This update includes important security fixes.",
  "playStoreUrl": "",
  "isEnabled": true
}
```

## Deployment Checklist

### Before Release
- [ ] Update app version in build.gradle
- [ ] Test force update flow
- [ ] Test optional update flow
- [ ] Verify Play Store URL
- [ ] Test offline behavior
- [ ] Update Firebase configuration

### Firebase Setup
- [ ] Create app_config collection
- [ ] Add update_info document
- [ ] Set appropriate Firestore rules
- [ ] Test configuration changes

## Troubleshooting

### Common Issues
1. **Update not showing**: Check Firebase configuration and app version
2. **Play Store not opening**: Verify Play Store URL format
3. **Animation issues**: Check device performance and animation settings
4. **Version comparison errors**: Verify version string format

### Debug Information
- Enable logging in UpdateRepository for version comparison
- Check Firebase console for configuration updates
- Monitor app version detection in logs
- Verify network connectivity for Firebase access

## Future Enhancements

### Potential Features
- **Update Scheduling**: Allow users to schedule updates
- **Changelog Display**: Show what's new in updates
- **Progressive Updates**: Staged rollout support
- **Custom Update Sources**: Support for alternative app stores
- **Update Analytics**: Track update adoption rates