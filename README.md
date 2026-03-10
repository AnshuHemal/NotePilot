# NotePilot 📝

A modern, feature-rich note-taking application for Android built with Jetpack Compose and Material Design 3.

## 🌟 Features

### 📱 Core Functionality

#### Rich Text Editor
- **Advanced Text Formatting**: Bold, italic, underline, strikethrough
- **Heading Styles**: Multiple heading levels for better organization
- **Lists**: Bullet points and numbered lists
- **Links**: Add and manage hyperlinks within notes
- **Code Blocks**: Syntax highlighting for code snippets
- **Real-time Preview**: See formatting as you type

#### Note Management
- **Create & Edit**: Intuitive note creation and editing interface
- **Color-Coded Notes**: Automatically assigned colors for visual organization
- **Search**: Fast full-text search across all notes
- **Sorting**: Multiple sort options (date, title, custom)
- **Filtering**: Filter by categories, date, and other criteria

### 🏷️ Categories & Organization

#### Smart Categorization
- **Custom Categories**: Create unlimited categories with custom names and colors
- **Color Picker**: HSV color picker with hue, saturation, and brightness controls
- **Single Category Assignment**: Each note can be assigned to one category
- **Category Management**: Edit, delete, and organize categories
- **Visual Icons**: All categories use a consistent label icon design
- **Category Filtering**: Filter notes by specific categories

#### Category Features
- Unique category names (case-insensitive validation)
- Firebase sync for cross-device category access
- Smooth animations (300ms transitions)
- Internet connectivity check before creation
- Vertical scrolling for long category lists

### 🖼️ Image Attachments

#### Local & Cloud Storage
- **Multiple Images**: Attach multiple images to any note
- **Local Storage**: Images saved immediately to device
- **Cloudinary Integration**: Automatic cloud backup
- **Offline Support**: Works without internet, syncs when online
- **Image Viewer**: Full-screen image viewer with zoom support
- **Image Management**: Add, view, and delete images

#### Image Sync Features
- Automatic sync during note creation/editing
- Manual sync via Image Sync Test Screen
- Sync status tracking (synced/unsynced)
- Cloudinary deletion on note deletion
- Comprehensive error handling and logging
- Visual feedback during sync operations

### 🔄 Sync & Backup

#### Firebase Integration
- **Real-time Sync**: Automatic synchronization with Firebase Firestore
- **Offline Mode**: Full functionality without internet
- **Background Sync**: Automatic sync when connection restored
- **Conflict Resolution**: Smart handling of sync conflicts
- **Sync Status**: Visual indicators for sync state

#### Sync Features
- Note sync (create, update, delete)
- Category sync across devices
- Image sync to Cloudinary
- Unsynced notes tracking
- Manual sync trigger
- Network connectivity monitoring

### 🔐 Authentication

#### Google Sign-In
- **One-Tap Sign-In**: Quick authentication with Google account
- **Secure**: Firebase Authentication integration
- **Profile Management**: User profile with avatar and details
- **Sign Out**: Secure sign-out functionality

### 🗑️ Recycle Bin

#### Soft Delete System
- **Temporary Deletion**: Notes moved to recycle bin instead of permanent deletion
- **Restore**: Recover deleted notes easily
- **Permanent Delete**: Remove notes permanently when ready
- **Bulk Operations**: Delete or restore multiple notes at once
- **Auto-cleanup**: Automatic cleanup of old deleted notes (optional)

#### Recycle Bin Features
- View all deleted notes
- Restore individual or multiple notes
- Permanently delete with confirmation
- Image cleanup on permanent deletion
- Cloudinary cleanup on permanent deletion

### 🔔 Notifications

#### Smart Notifications
- **In-App Notifications**: Custom notification system
- **Firebase Cloud Messaging**: Push notifications support
- **Notification Types**: Welcome, sync status, reminders
- **Notification Center**: Dedicated screen for all notifications
- **Badge Indicators**: Unread notification badges
- **Mark as Read**: Individual or bulk mark as read

#### Notification Features
- Welcome notification on first sign-in
- Sync completion notifications
- Error notifications
- Custom notification icons
- Notification preferences
- Background sync notifications

### 🎨 UI/UX

#### Modern Design
- **Material Design 3**: Latest Material Design guidelines
- **Dark/Light Theme**: Automatic theme switching
- **Smooth Animations**: 300ms transitions throughout
- **Custom Bottom Bar**: Animated bottom navigation with FAB
- **Responsive Layout**: Adapts to different screen sizes
- **Professional Icons**: Consistent icon design

#### Navigation
- **Bottom Navigation**: Home, Notifications, Settings, Account
- **Floating Action Button**: Quick note creation
- **Gesture Support**: Swipe to delete notes
- **Back Navigation**: Proper back stack management

### 🔍 Search & Filter

#### Advanced Search
- **Full-Text Search**: Search across titles and content
- **Real-time Results**: Instant search results as you type
- **Search History**: Recent searches saved
- **Clear Search**: Quick clear button

#### Filtering Options
- Filter by category
- Filter by date range
- Filter by sync status
- Sort by date (ascending/descending)
- Sort by title
- Combined filters

### ⚙️ Settings

#### Customization
- **Theme Selection**: Light, Dark, System default
- **Notification Preferences**: Enable/disable notifications
- **Background Sync**: Toggle automatic sync
- **Account Management**: View and manage account details
- **App Information**: Version, build info

#### Support & Information
- **About Screen**: App version, privacy policy, terms of use, developer credits
- **Report & Feedback System**: Comprehensive feedback collection with Firebase integration
- **Professional Layout**: Consistent styling across all settings sections

### 📝 Feedback System

#### Report & Feedback
- **Feedback Types**: Bug Report, Feature Request, General Feedback, UI/UX Issues, Performance Issues
- **Smart Form**: Auto-populated user email from authenticated account
- **Character Limits**: Subject field limited to 25 characters with real-time counter
- **Keyboard Navigation**: IME actions for smooth field navigation (Next/Done)
- **Network Awareness**: Automatic internet connectivity detection
- **Firebase Integration**: Direct submission to Firestore database
- **Device Information**: Automatic collection of device and app version details
- **Progress Indicators**: Visual feedback during submission process
- **Error Handling**: Specific error messages for network and submission issues

#### About Screen
- **App Information**: Version, description, and branding
- **Legal Links**: Privacy Policy, Terms of Use, Open Source Licenses
- **Developer Credits**: Professional developer attribution at bottom
- **External Links**: Rate app on Play Store, open external URLs
- **Consistent Design**: Matches app's overall design language

### 📊 Data Management

#### Database
- **Room Database**: Local SQLite database
- **Version 6**: Latest schema with image support
- **Migrations**: Automatic database migrations
- **Relationships**: Proper foreign key relationships
- **Indexes**: Optimized queries

#### Data Models
- Notes with rich text content
- Categories with colors
- Note-Category relationships (many-to-many)
- Images with local and cloud paths
- Notifications
- User preferences
- Feedback with types and status tracking

### 🛠️ Technical Features

#### Architecture
- **MVVM Pattern**: Clean architecture with ViewModel
- **Dependency Injection**: Hilt for DI
- **Coroutines**: Asynchronous operations
- **Flow**: Reactive data streams
- **Repository Pattern**: Data layer abstraction

#### Libraries & Technologies
- **Jetpack Compose**: Modern UI toolkit
- **Material 3**: Latest Material Design components
- **Firebase**: Authentication, Firestore, Cloud Messaging
- **Cloudinary**: Image storage and CDN
- **Room**: Local database
- **Hilt**: Dependency injection
- **Coil**: Image loading
- **OkHttp**: Network requests
- **Gson**: JSON parsing
- **WorkManager**: Background tasks
- **DataStore**: Preferences storage

#### Performance
- Lazy loading for large lists
- Image caching with Coil
- Efficient database queries
- Background processing
- Memory optimization

### 🔧 Developer Features

#### Debugging Tools
- **Unsynced Notes Screen**: View and sync unsynced notes
- **Comprehensive Logging**: Detailed logs for debugging
- **Error Handling**: Graceful error handling throughout

#### Code Quality
- Clean code architecture
- Proper error handling
- Comprehensive logging
- Type-safe navigation
- Null safety

### 💰 Monetization

#### AdMob Integration
- **Banner Ads**: Non-intrusive banner ads in CreateNoteScreen
- **Strategic Placement**: Ads placed below header, above content
- **Test Ads**: Google test ads for development
- **Easy Configuration**: Simple setup with AdMobConfig

For AdMob setup instructions, see [ADMOB_SETUP.md](ADMOB_SETUP.md)

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or later
- Android SDK 24 or higher
- Kotlin 1.9+
- Google account for Firebase
- Cloudinary account for image storage

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/notepilot.git
   cd notepilot
   ```

2. **Configure Firebase**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
   - Add an Android app to your Firebase project
   - Download `google-services.json`
   - Place it in `app/` directory
   - Enable Authentication (Google Sign-In)
   - Enable Firestore Database
   - Enable Cloud Messaging

3. **Configure Cloudinary**
   - Create account at [Cloudinary](https://cloudinary.com)
   - Get your credentials (Cloud Name, API Key, API Secret)
   - Create an unsigned upload preset named `notepilot_images`
   - Update `CloudinaryConfig.kt`:
     ```kotlin
     object CloudinaryConfig {
         const val CLOUD_NAME = "your_cloud_name"
         const val API_KEY = "your_api_key"
         const val API_SECRET = "your_api_secret"
         const val UPLOAD_PRESET = "notepilot_images"
     }
     ```

4. **Build and Run**
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

## 📱 Screenshots

### Home Screen
- Colorful note cards with titles
- Search, filter, and unsynced notes buttons
- Animated bottom navigation
- Floating action button for quick note creation

### Note Editor
- Rich text formatting toolbar
- Category selection
- Image attachments
- Auto-save functionality

### Categories
- Color-coded category chips
- Category management screen
- HSV color picker
- Add/edit/delete categories

### Image Attachments
- Multiple image support
- Image viewer with zoom
- Sync status indicators
- Local and cloud storage

## 🏗️ Project Structure

```
app/
├── src/main/java/com/white/notepilot/
│   ├── data/
│   │   ├── auth/           # Authentication logic
│   │   ├── dao/            # Room DAOs
│   │   ├── database/       # Database configuration
│   │   ├── model/          # Data models
│   │   ├── preferences/    # DataStore preferences
│   │   ├── remote/         # Cloudinary integration
│   │   └── repository/     # Data repositories
│   ├── di/                 # Dependency injection modules
│   ├── enums/              # Enumerations
│   ├── services/           # Background services
│   ├── states/             # UI states
│   ├── ui/
│   │   ├── components/     # Reusable UI components
│   │   ├── events/         # UI events
│   │   ├── navigation/     # Navigation setup
│   │   ├── screens/        # App screens
│   │   └── theme/          # Theme configuration
│   ├── utils/              # Utility classes
│   ├── viewmodel/          # ViewModels
│   ├── workers/            # WorkManager workers
│   ├── BaseApplication.kt  # Application class
│   └── MainActivity.kt     # Main activity
└── res/                    # Resources (layouts, drawables, etc.)
```

## 🔑 Key Components

### ViewModels
- **NotesViewModel**: Note operations and state management
- **AuthViewModel**: Authentication state
- **CategoryViewModel**: Category management
- **SettingsViewModel**: App settings and preferences
- **FeedbackViewModel**: Feedback form state and submission

### Repositories
- **NoteRepository**: Note CRUD operations
- **CategoryRepository**: Category operations
- **ImageRepository**: Image storage and sync
- **FirebaseRepository**: Firebase Firestore operations
- **NotificationRepository**: Notification management
- **FeedbackRepository**: Feedback submission and network handling

### UI Screens
- **HomeScreen**: Main note list
- **CreateNoteScreen**: Note editor
- **NoteDetailScreen**: View note details
- **SearchNoteScreen**: Search functionality
- **CategoryManagementScreen**: Manage categories
- **UnsyncedNotesScreen**: View unsynced notes and recycle bin
- **NotificationsScreen**: Notification center
- **SettingsScreen**: App settings
- **AccountScreen**: User profile
- **AboutScreen**: App information and legal links
- **ReportFeedbackScreen**: Feedback submission form

## 🎯 Features in Detail

### Note Creation Flow
1. User taps FAB
2. Opens CreateNoteScreen
3. User enters title and content
4. User can add categories
5. User can attach images
6. Images save locally immediately
7. Images sync to Cloudinary in background
8. Note saves to local database
9. Note syncs to Firebase when online
10. User sees sync status

### Image Sync Flow
1. User selects images
2. Images save to local storage
3. Database records created
4. Automatic sync attempt to Cloudinary
5. If successful: database updated with cloud URL
6. If failed: marked as unsynced for retry
7. Manual sync available from test screen
8. Background sync when connection restored

### Category Assignment Flow
1. User taps category button in editor
2. Bottom sheet shows available categories
3. User selects one category
4. Category chip appears in editor
5. User can remove category
6. Category saves with note
7. Category syncs to Firebase

### Delete Flow
1. User swipes note left
2. Note marked as deleted (soft delete)
3. Note moves to recycle bin
4. User can restore from recycle bin
5. User can permanently delete
6. Permanent delete removes:
   - Note from database
   - Note from Firebase
   - All associated images (local + cloud)
   - All image database records

## 🔒 Security

- Firebase Authentication for secure sign-in
- Secure API key storage
- HTTPS for all network requests
- Cloudinary signed URLs for images
- Local data encryption (Room)
- Proper permission handling

## 🌐 Offline Support

- Full note creation/editing offline
- Local image storage
- Automatic sync when online
- Offline indicator
- Sync queue management
- Conflict resolution

## 📈 Performance Optimizations

- Lazy loading for note lists
- Image caching with Coil
- Database indexing
- Coroutine-based async operations
- Memory-efficient image handling
- Background sync with WorkManager

## 🐛 Known Issues

None currently. All features are working properly.

## 🔮 Future Enhancements

- [ ] Note sharing with other users
- [ ] Collaborative editing
- [ ] Voice notes
- [ ] Drawing/sketching support
- [ ] Note templates
- [ ] Export to PDF
- [ ] Backup/restore functionality
- [ ] Widget support
- [ ] Tablet optimization
- [ ] Wear OS support
- [ ] Feedback attachment support
- [ ] Admin panel for feedback management

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Author

**Hemal Katariya**
- GitHub: [@hemukatariya](https://github.com/AnshuHemal)
- Email: connect.hemal@gmail.com

## 🙏 Acknowledgments

- Material Design 3 guidelines
- Jetpack Compose documentation
- Firebase documentation
- Cloudinary documentation
- Android developer community

## 📞 Support

For support, email connect.hemal@gmail.com or open an issue in the GitHub repository.

## 🔄 Version History

### Version 1.0.0 (Current)
- Initial release
- Rich text editor
- Category management
- Image attachments with Cloudinary
- Firebase sync
- Google authentication
- Recycle bin
- Notifications
- Search and filter
- Dark/Light theme
- AdMob integration
- About screen with app information
- Report & Feedback system with Firebase integration
- Professional settings layout
- Category deletion with Firebase sync
- Native ads in note lists
- Interstitial ads for navigation tracking


**Built with ❤️ using Jetpack Compose and Material Design 3 by  HEMAL KATARIYA**
