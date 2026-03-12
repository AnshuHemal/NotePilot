# Subscription System - NotePilot

## Overview
The subscription system allows users to purchase premium features using coins earned through the rewards system.

## Features
- **Coin-based purchasing**: Users spend coins instead of real money
- **Multiple subscription tiers**: Monthly, Yearly, and Lifetime options
- **Premium benefits**: Ad-free experience, unlimited cloud storage, advanced features
- **Visual indicators**: Premium crown icon above user avatar
- **Modern UI**: Animated cards, gradient backgrounds, professional design

## Architecture

### Models
- `Subscription.kt`: User subscription data model
- `SubscriptionPlan.kt`: Available subscription plans with features and pricing

### Repository
- `SubscriptionRepository.kt`: Handles Firebase operations and coin deduction
- Integrates with `RewardsRepository` for coin management

### ViewModel
- `SubscriptionViewModel.kt`: Manages subscription state and purchase flow
- Handles success/error dialogs and loading states

### UI Components
- `SubscriptionScreen.kt`: Main subscription purchase interface
- `AccountScreen.kt`: Shows premium status and crown icon
- Animated cards with spring animations and gradient backgrounds

## Subscription Plans

### Premium Monthly
- **Cost**: 2,500 coins
- **Duration**: 30 days
- **Features**: Ad-free, unlimited storage, advanced organization

### Premium Yearly (Popular)
- **Cost**: 20,000 coins
- **Duration**: 365 days
- **Features**: All monthly features + analytics, collaboration tools

### Premium Lifetime
- **Cost**: 50,000 coins
- **Duration**: Lifetime
- **Features**: All features forever + VIP support, beta access

## Premium Benefits
- ✅ Ad-free experience
- ✅ Unlimited cloud storage
- ✅ Advanced note organization
- ✅ Priority sync
- ✅ Premium themes
- ✅ Export to multiple formats
- ✅ Advanced analytics (Yearly+)
- ✅ Collaboration tools (Yearly+)
- ✅ VIP customer support (Lifetime)

## Technical Implementation

### Firebase Structure
```
user_subscriptions/{userId}
├── isPremium: boolean
├── subscriptionType: SubscriptionType
├── purchaseDate: Timestamp
├── expiryDate: Timestamp (null for lifetime)
├── coinsSpent: number
└── isActive: boolean
```

### Purchase Flow
1. User selects subscription plan
2. System checks coin balance
3. Deducts coins from user rewards
4. Creates/updates subscription in Firebase
5. Updates premium status
6. Shows success dialog

### Expiry Handling
- Automatic expiry checking on subscription load
- Updates status when subscription expires
- Graceful degradation to free tier

## UI/UX Features
- **Animated entrance**: Staggered card animations with spring physics
- **Premium indicators**: Golden crown icon and "PREMIUM" badge
- **Visual feedback**: Loading states, success/error dialogs
- **Professional design**: Gradient backgrounds, proper spacing, Material Design 3
- **Accessibility**: Proper content descriptions and keyboard navigation

## Integration Points
- **AccountScreen**: Shows premium crown icon above avatar
- **RewardsSystem**: Coin deduction for purchases
- **Firebase**: Subscription data persistence
- **Navigation**: Seamless flow between screens

## Error Handling
- Insufficient coins validation
- Network error handling
- Firebase operation failures
- User-friendly error messages

## Future Enhancements
- Push notifications for expiry reminders
- Subscription renewal prompts
- Usage analytics for premium features
- Referral bonuses for premium users