# NotePilot — Feature Ideas & Improvements

> A living document of features to make the app more attractive, modern, and engaging.
> Organized by priority and effort level.

---

## 🔥 High Impact / Quick Wins

### 1. Recycle Bin Screen
- The route is already commented out in `NavGraph.kt` — just needs the screen built
- Show soft-deleted notes with restore / permanent delete options
- Add auto-cleanup after 30 days with a countdown badge

### 2. Note Widgets (Home Screen)
- A 2x2 or 4x1 widget showing recent/pinned notes
- Tap to open the note directly
- Glance API (Jetpack) is the modern way to do this on Android

### 3. Note Sharing (Export)
- Share note as plain text, formatted HTML, or PDF
- "Copy to clipboard" one-tap action
- `ShareBottomSheet` already exists — extend it with export formats

### 4. Pinned Notes Section
- `isPinned` already exists on the `Note` model
- Show pinned notes in a separate horizontal scroll row at the top of HomeScreen
- Drag-to-reorder pinned notes

### 5. Note Color Themes (Gradient Support)
- Currently uses a single `colorCode` — extend to support gradient pairs
- Let users pick from a palette of gradient presets in CreateNoteScreen
- Makes the note grid look much more visually rich

---

## ✨ UX & Visual Polish

### 6. Animated Note Cards
- Add subtle entrance animations when notes load (staggered fade-in / slide-up)
- Shared element transitions when opening a note from the list
- `AnimatedVisibility` + `animateItemPlacement` in LazyVerticalGrid

### 7. Skeleton Loading Improvements
- `NoteListSkeleton` already exists — make sure it's used consistently on every list load
- Add shimmer effect (Compose shimmer library or custom)

### 8. Empty State Illustrations
- Custom illustrations for: no notes, no search results, no notifications, empty recycle bin
- Use Lottie animations for a modern feel

### 9. Swipe Actions on Note Cards
- Swipe right → Pin/Unpin
- Swipe left → Delete (already partially done with `SwipeToDeleteNoteItem`)
- Add haptic feedback on swipe

### 10. Note Grid / List Toggle
- Let users switch between grid view and list view on HomeScreen
- Save preference in DataStore
- Grid view shows color + title only; list view shows a content preview

### 11. Dark Mode AMOLED Option
- Add a true black AMOLED dark theme option in Settings
- Great for battery life on OLED screens and very popular with power users

### 12. Dynamic Color (Material You)
- Use `dynamicColorScheme()` on Android 12+ to match the user's wallpaper colors
- Already using Material3 — this is a one-line addition in your theme setup

---

## 🎙️ Content & Editing

### 13. Voice Notes (Audio Recording)
- `VoiceToTextHelper` exists for speech-to-text, but actual audio recording is missing
- Record audio clips and attach them to notes
- Show a waveform playback UI inside the note

### 14. Drawing / Sketch Canvas
- Add a drawing canvas screen (using `Canvas` composable or a library like `signature-pad`)
- Save sketches as images and attach to notes
- Great for quick diagrams or handwritten notes

### 15. Note Templates
- Pre-built templates: Meeting Notes, Daily Journal, To-Do List, Shopping List, etc.
- Show a template picker when creating a new note
- Templates stored locally, optionally synced

### 16. Checklist / To-Do Mode
- Add a checklist note type alongside rich text
- Tap to check/uncheck items
- Show completion progress on the note card (e.g., "3/5 done")

### 17. Markdown Support
- Let users write in Markdown and toggle between edit/preview mode
- Pairs well with the existing rich text editor as an alternative input mode

### 18. Table Support in Rich Text
- Add a simple table insertion option to the rich text toolbar
- Useful for structured notes like comparisons or schedules

---

## 🔔 Notifications & Reminders

### 19. Note Reminders
- Set a date/time reminder on any note
- Trigger a local notification at the set time
- Show reminder badge on note cards
- `Notification` model and `NotificationHelper` already exist — extend them

### 20. Daily Digest Notification
- Optional daily push notification: "You have X unsynced notes" or "Your note from last week..."
- Configurable time in Settings

### 21. Notification Actions
- Add action buttons to notifications: "Open Note", "Mark as Read", "Snooze"
- Uses `NotificationCompat.Action`

---

## 🔐 Security & Privacy

### 22. App Lock (PIN / Biometric)
- Lock the entire app on launch, not just individual notes
- Biometric support already exists — extend it to app-level lock
- Toggle in Settings

### 23. Incognito / Private Notes Folder
- A hidden folder that requires authentication to access
- Notes inside don't appear in search or the main list

### 24. Note Expiry
- Set an expiry date on a note — it auto-deletes (moves to recycle bin) after that date
- Useful for temporary notes or reminders

---

## 🤝 Social & Collaboration

### 25. Share Note via QR Code
- `QRCodeDialog` and `QRScannerScreen` already exist
- Encode note content into a QR code and let another user scan to import it
- Works offline — no server needed

### 26. Note Collaboration (Advanced)
- Share a note with another user by email
- Real-time co-editing using Firestore listeners
- Show collaborator avatars on the note card

### 27. Public Note Links
- Generate a read-only shareable link for a note
- Hosted as a simple Firebase-backed web page

---

## 📊 Analytics & Insights (for the User)

### 28. Writing Stats Dashboard
- Notes created this week/month
- Total words written
- Most active writing time
- Category breakdown pie chart
- Show in AccountScreen or a dedicated Stats screen

### 29. Streak System
- Track consecutive days the user created or edited a note
- Show streak badge on HomeScreen
- Reward coins for maintaining streaks (ties into existing rewards system)

### 30. Note Word Count & Read Time
- Show word count and estimated read time on NoteDetailScreen
- Small, non-intrusive stat below the note content

---

## ⚙️ Settings & Customization

### 31. Font Size & Font Family
- Let users pick their preferred font size (Small / Medium / Large)
- Optionally offer 2-3 font family choices
- Save in DataStore, apply app-wide

### 32. Note Card Density
- Compact / Normal / Comfortable spacing options for the note list
- Similar to Gmail's density setting

### 33. Default Note Color
- Let users set a default color for new notes instead of random assignment

### 34. Auto-Save Indicator
- Show a subtle "Saved" / "Saving..." indicator in CreateNoteScreen
- Reassures users their work isn't lost

### 35. Backup & Restore
- Manual export of all notes to a JSON file
- Import from a previously exported file
- Works as a safety net alongside Firebase sync

---

## 🏆 Gamification & Engagement

### 36. Achievements / Badges
- "First Note", "100 Notes", "7-Day Streak", "Power User" etc.
- Show in AccountScreen
- Award coins for unlocking achievements (ties into rewards system)

### 37. Onboarding Flow
- A 3-4 step onboarding after first login
- Highlights key features: create note, add category, sync, lock a note
- Skip option always visible

### 38. What's New Dialog
- Show a bottom sheet after an app update listing new features
- Triggered by version comparison in `UpdateViewModel`

---

## 🛠️ Developer / Quality of Life

### 39. Proper Recycle Bin with Auto-Cleanup
- Auto-delete notes in recycle bin after 30 days
- Show days remaining on each deleted note card
- WorkManager scheduled task for cleanup

### 40. Feedback Attachments
- `Feedback` model already has `attachmentUrls` field
- Add image picker to `ReportFeedbackScreen` to attach screenshots
- Upload to Cloudinary (already integrated)

### 41. Offline Queue for Images
- If image upload to Cloudinary fails, queue it and retry automatically
- Show upload progress per image in CreateNoteScreen

### 42. Search Filters in SearchNoteScreen
- Filter search results by category, date range, color, or lock status
- `FilterBottomSheet` already exists — wire it into search

---

## 📱 Platform Expansion

### 43. Tablet / Foldable Layout
- Two-pane layout: note list on left, note detail on right
- Use `WindowSizeClass` from Compose to detect screen size

### 44. Wear OS Companion
- View recent/pinned notes on a smartwatch
- Quick voice note creation from the wrist

### 45. Chrome OS / Desktop Support
- Keyboard shortcuts for common actions (Ctrl+N, Ctrl+S, etc.)
- Mouse hover states on note cards

---

## 💡 AI-Powered Features (Future)

### 46. AI Note Summarization
- One-tap summary of a long note using Gemini API (free tier available)
- Show summary as a collapsible section at the top of NoteDetailScreen

### 47. Smart Tagging
- Auto-suggest categories based on note content using on-device ML
- ML Kit Text Recognition is already a dependency

### 48. Grammar & Spell Check
- Integrate Android's built-in spell check or a lightweight library
- Highlight errors inline in the rich text editor

### 49. AI-Powered Search
- Semantic search: find notes by meaning, not just exact keywords
- "Find my note about the meeting with the client last month"

---

## Priority Matrix

| Feature | Impact | Effort | Do First? |
|---|---|---|---|
| Recycle Bin Screen | High | Low | ✅ Yes |
| Pinned Notes Row | High | Low | ✅ Yes |
| Note Reminders | High | Medium | ✅ Yes |
| Animated Note Cards | High | Low | ✅ Yes |
| Empty State Illustrations | Medium | Low | ✅ Yes |
| Dynamic Color (Material You) | High | Very Low | ✅ Yes |
| Note Grid/List Toggle | Medium | Low | ✅ Yes |
| Checklist / To-Do Mode | High | Medium | 👍 Soon |
| Writing Stats Dashboard | Medium | Medium | 👍 Soon |
| App Lock | High | Medium | 👍 Soon |
| Voice Notes | Medium | High | ⏳ Later |
| Drawing Canvas | Medium | High | ⏳ Later |
| AI Summarization | High | Medium | ⏳ Later |
| Collaboration | High | Very High | 🔮 Future |
| Wear OS | Low | Very High | 🔮 Future |
