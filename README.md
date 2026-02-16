# Firebase Cloud Messaging - Titanium Module

Use the native Firebase SDK (iOS/Android) in Axway Titanium. This repository is part of the [Titanium Firebase](https://github.com/hansemannn/titanium-firebase) project.

## Supporting this effort

The whole Firebase support in Titanium is developed and maintained by the community (`@hansemannn` and `@m1ga`). To keep this project maintained and be able to use the latest Firebase SDK's, please see the "Sponsor" button of this repository, thank you!

---

## Table of Contents

- [Requirements](#requirements)
- [Download](#download)
- [Platform Setup](#platform-setup)
  - [iOS Setup](#ios-setup)
  - [Android Setup](#android-setup)
- [API Reference](#api-reference)
  - [Methods](#methods)
  - [Properties](#properties)
  - [Events](#events)
- [Basic Usage Example](#basic-usage-example)
- [Advanced Android Features](#advanced-android-features)
- [Sending Push Messages](#sending-push-messages)
- [Integration with Parse](#integration-with-parse)
- [Build from Source](#build-from-source)
- [Legal](#legal)

---

## Requirements

### iOS
- Firebase-Core module: [titanium-firebase-core](https://github.com/hansemannn/titanium-firebase-core)
- Titanium SDK 10.0.0 or later

### Android
- Titanium SDK 13.0.0 or later
- Ti.PlayServices module: [ti.playservices](https://github.com/appcelerator-modules/ti.playservices)

### Getting Started
Read the [Titanium-Firebase installation guide](https://github.com/hansemannn/titanium-firebase#installation) if you are setting up a new project.

---

## Download

- **Stable releases:** [GitHub Releases](https://github.com/hansemannn/titanium-firebase-cloud-messaging/releases)
- **gitTio:** [![gitTio](http://hans-knoechel.de/shields/shield-gittio.svg)](http://gitt.io/component/firebase.cloudmessaging)

---

## Platform Setup

### iOS Setup

**Visual Examples:**

<table>
<tr>
<td style="vertical-align:top;">
<img src="example/ios_push1.jpg"/>
</td>
<td style="vertical-align:top;">
<img src="example/ios_push2.jpg"/>
</td>
</tr>
</table>

To register for push notifications on iOS, use the standard Titanium methods:

```javascript
// Listen to the notification settings event
Ti.App.iOS.addEventListener('usernotificationsettings', function eventUserNotificationSettings() {
  // Remove the event again to prevent duplicate calls through the Firebase API
  Ti.App.iOS.removeEventListener('usernotificationsettings', eventUserNotificationSettings);

  // Register for push notifications
  Ti.Network.registerForPushNotifications({
    success: function () { /* ... */ },
    error: function () { /* ... */ },
    callback: function () { /* ... */ } // Fired for all kind of notifications (foreground, background & closed)
  });
});

// Register for the notification settings event
Ti.App.iOS.registerUserNotificationSettings({
  types: [
    Ti.App.iOS.USER_NOTIFICATION_TYPE_ALERT,
    Ti.App.iOS.USER_NOTIFICATION_TYPE_SOUND,
    Ti.App.iOS.USER_NOTIFICATION_TYPE_BADGE
  ]
});
```

---

### Android Setup

**Visual Examples:**

<table>
<tr>
<td style="vertical-align:top;">
<img src="example/android_big_image.png" valign="top"/>
</td>
<td style="vertical-align:top;">
<img src="example/android_big_text.png"/>
</td>
</tr>
<tr>
<td>Big image notification with colored icon/appname</td>
<td>Big text notification with colored icon/appname</td>
</tr>
</table>

#### Runtime Permissions (Android 13+)

**For Titanium 12.0.0 and above:**

```javascript
Ti.Network.registerForPushNotifications({
  success: function () { 
    // Permission granted - now register for FCM token
    FirebaseCloudMessaging.registerForPushNotifications();
  },
  error: function () { 
    // Permission denied
  }
});
```

All versions below Android 13 will call the `success` function immediately.

**For older Titanium versions:**

```javascript
var permissions = ['android.permission.POST_NOTIFICATIONS'];
Ti.Android.requestPermissions(permissions, function(e) {
  if (e.success) {
    Ti.API.info('SUCCESS');
    FirebaseCloudMessaging.registerForPushNotifications();
  } else {
    Ti.API.info('ERROR: ' + e.error);
  }
});
```

If you have runtime permissions (the `success` event mentioned above or `Ti.Network.remoteNotificationsEnabled` is true), you can call `FirebaseCloudMessaging.registerForPushNotifications()` to request a token.

---

#### Notification Icon Configuration

**Icon File Location:**

Place your notification icon `notificationicon.png` in one of these directories:

- `[application_name]/[app*]/platform/android/res/drawable/`
- `[application_name]/[app*]/platform/android/res/drawable-*` (if you use custom DPI folders)

**Note:** `[app*]` = Alloy apps

**tiapp.xml Configuration:**

To use the custom icon for notification messages, add this attribute within the `<application/>` section of your `tiapp.xml`:

```xml
<meta-data android:name="com.google.firebase.messaging.default_notification_icon" 
           android:resource="@drawable/notificationicon"/>
```

**Icon Design Requirements:**

- Flat design (no gradients)
- White foreground on transparent background
- Face-on perspective
- Only the outline/shape will be visible in the notification

**Required Icon Resolutions:**

```
22 × 22 area in 24 × 24 (mdpi)
33 × 33 area in 36 × 36 (hdpi)
44 × 44 area in 48 × 48 (xhdpi)
66 × 66 area in 72 × 72 (xxhdpi)
88 × 88 area in 96 × 96 (xxxhdpi)
```

**Auto-Generation Script:**

Place your icon in `drawable-xxxhdpi/notificationicon.png` and run this script.

Requirements: ImageMagick ([macOS install](https://formulae.brew.sh/formula/imagemagick), [Windows download](https://imagemagick.org/script/download.php))

```bash
#!/bin/sh

ICON_SOURCE="app/platform/android/res/drawable-xxxhdpi/notificationicon.png"
if [ -f "$ICON_SOURCE" ]; then
    mkdir -p "app/platform/android/res/drawable-xxhdpi"
    mkdir -p "app/platform/android/res/drawable-xhdpi"
    mkdir -p "app/platform/android/res/drawable-hdpi"
    mkdir -p "app/platform/android/res/drawable-mdpi"
    convert "$ICON_SOURCE" -resize 72x72 "app/platform/android/res/drawable-xxhdpi/notificationicon.png"
    convert "$ICON_SOURCE" -resize 48x48 "app/platform/android/res/drawable-xhdpi/notificationicon.png"
    convert "$ICON_SOURCE" -resize 36x36 "app/platform/android/res/drawable-hdpi/notificationicon.png"
    convert "$ICON_SOURCE" -resize 24x24 "app/platform/android/res/drawable-mdpi/notificationicon.png"
else
    echo "No 'notificationicon.png' file found in app/platform/android/res/drawable-xxxhdpi"
fi
```

---

#### Data vs Notification Messages

On Android there are two types of messages:

1. **Notification Messages:** Processed by the system
2. **Data Messages:** Handled by `showNotification()` in `TiFirebaseMessagingService`

Using the `notification` block inside the POST payload will send a Notification message.

**Supported Data Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `title` | String | Notification title |
| `message` | String | Notification message body |
| `big_text` | String | Expanded notification text |
| `big_text_summary` | String | Summary text for big text style |
| `icon` | String | Remote URL for large icon |
| `image` | String | Remote URL for big picture |
| `rounded_large_icon` | Boolean | Display large icon as rounded image |
| `force_show_in_foreground` | Boolean | Show notification even when app is in foreground |
| `id` | Integer | Notification ID |
| `color` | String | Tint color for app name and small icon |
| `vibrate` | Boolean | Enable vibration |
| `sound` | String | Custom sound file (e.g., "notification.mp3" from `/platform/android/res/raw/`) |
| `badge` | Integer | Badge number (if supported by device) |
| `channelId` | String | Notification channel ID for data messages |
| `actions` | String (JSON) | Array of action buttons (see [Notification Actions](#notification-actions)) |

**Supported Notification Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `title` | String | Notification title |
| `body` | String | Notification message body |
| `color` | String | Notification color (e.g., "#00ff00") |
| `tag` | String | Custom tag (notifications with same tag replace each other) |
| `sound` | String | Custom sound file |
| `android_channel_id` | String | Channel ID for notification messages |

---

#### Custom Sound Configuration

To use a custom sound, you must create a notification channel with that sound. The default channel always uses the system's default notification sound.

**For Data Messages:** Set the `channelId` field in the data payload
**For Notification Messages:** Set the `android_channel_id` field in the notification payload

See the [Extended PHP Android Example](#sending-push-messages) for implementation details.

**Important Note for Module Version Upgrades:**

When upgrading from version ≤2.0.2 to ≥2.0.3:

- Prior versions used resource IDs for sound URIs, which can change between app versions ([Android Issue](https://issuetracker.google.com/issues/131303134))
- Version 2.0.3+ uses string filenames for stable sound references
- If upgrading, test thoroughly with installed version ≤2.0.2 before upgrading (don't uninstall first)
- If issues occur, delete the old channel using `deleteNotificationChannel()` and recreate with a new ID
- Update your push server to send to the new channel ID for newer app versions

---

#### Troubleshooting

**Errors with firebase.analytics:**

If you encounter errors like `Error: Attempt to invoke virtual method 'getInstanceId()' on a null object reference`, add this to your `tiapp.xml`:

```xml
<service android:name="com.google.firebase.components.ComponentDiscoveryService">
    <meta-data android:name="com.google.firebase.components:com.google.firebase.iid.Registrar"
               android:value="com.google.firebase.components.ComponentRegistrar" />
</service>
```

---

## API Reference

### Methods

#### Core Methods

**`registerForPushNotifications()`**

Registers the app for push notifications and requests an FCM token.

```javascript
FirebaseCloudMessaging.registerForPushNotifications();
```

---

**`appDidReceiveMessage(parameters)`** (iOS only)

- **parameters** (Object): Message parameters

**Note:** Only call this method if method swizzling is disabled (enabled by default). Messages are received via native delegates instead. Use the `gcm.message_id` key from the notification payload.

---

**`sendMessage(parameters)`**

**Deprecated:** This function was decommissioned along with FCM upstream messaging in June 2023. See [FCM FAQ](https://firebase.google.com/support/faq#fcm-23-deprecation).

- **parameters** (Object)
  - `messageID` (String): Unique message identifier
  - `to` (String): Recipient token or topic
  - `timeToLive` (Number): Message TTL in seconds
  - `data` (Object): Custom data payload

---

**`subscribeToTopic(topic)`**

Subscribe to a Firebase Cloud Messaging topic.

- **topic** (String): Topic name

```javascript
FirebaseCloudMessaging.subscribeToTopic('news');
```

---

**`unsubscribeFromTopic(topic)`**

Unsubscribe from a Firebase Cloud Messaging topic.

- **topic** (String): Topic name

```javascript
FirebaseCloudMessaging.unsubscribeFromTopic('news');
```

---

**`getToken()`** (Android only)

Requests and returns the current FCM token. Automatically persists token to SharedPreferences.

```javascript
FirebaseCloudMessaging.getToken();
```

---

**`deleteToken()`** (Android only)

Removes the current FCM token.

```javascript
FirebaseCloudMessaging.deleteToken();
```

---

#### Notification Channel Management (Android only)

**`setNotificationChannel(channel)`**

Sets the notification channel using a Titanium NotificationChannel object.

- **channel** (NotificationChannel Object): Channel created with `Ti.Android.NotificationManager.createNotificationChannel()`

**Preferred method** for setting a channel. See [Titanium.Android.NotificationChannel](https://docs.appcelerator.com/platform/latest/#!/api/Titanium.Android.NotificationChannel) for details.

```javascript
const channel = Ti.Android.NotificationManager.createNotificationChannel({
    id: 'default',
    name: 'Default Channel',
    importance: Ti.Android.IMPORTANCE_DEFAULT
});
FirebaseCloudMessaging.setNotificationChannel(channel);
```

---

**`createNotificationChannel(parameters)`**

Creates a notification channel with custom configuration.

- **parameters** (Object)
  - `sound` (String, optional): Sound file name without extension from `platform/android/res/raw/`. Use "default" for system sound, "silent" for no sound
  - `channelId` (String, optional): Channel identifier. Defaults to "default"
  - `channelName` (String, optional): Display name. Defaults to `channelId`
  - `importance` (String, optional): "low", "high", or "default". Defaults to "default" (or "low" if sound is "silent")
  - `vibrate` (Boolean, optional): Enable vibration. Defaults to false
  - `lights` (Boolean, optional): Enable notification lights. Defaults to false
  - `showBadge` (Boolean, optional): Show badge icon. Defaults to false

Read more in the [official Android documentation](https://developer.android.com/reference/android/app/NotificationChannel).

```javascript
FirebaseCloudMessaging.createNotificationChannel({
    channelId: 'alerts',
    channelName: 'Alert Notifications',
    sound: 'alert_sound',
    importance: 'high',
    vibrate: true,
    lights: true,
    showBadge: true
});
```

---

**`deleteNotificationChannel(channelId)`**

Deletes a notification channel.

- **channelId** (String): Channel ID to delete

```javascript
FirebaseCloudMessaging.deleteNotificationChannel('old_channel');
```

---

**`fetchNotificationChannel(channelId)`** (Android only)

Retrieves information about a specific notification channel.

- **channelId** (String): Channel ID to query
- **Returns:** (Object) Channel information or null if not found

```javascript
const channelInfo = FirebaseCloudMessaging.fetchNotificationChannel('default');
if (channelInfo) {
    Ti.API.info('Channel name: ' + channelInfo.name);
    Ti.API.info('Importance: ' + channelInfo.importance);
    Ti.API.info('Vibration enabled: ' + channelInfo.vibrationEnabled);
}
```

**Returned Object Properties:**
- `id` (String): Channel ID
- `name` (String): Channel name
- `importance` (Integer): Importance level
- `description` (String): Channel description
- `vibrationEnabled` (Boolean): Vibration status
- `lightsEnabled` (Boolean): Lights status
- `badgeEnabled` (Boolean): Badge status

---

**`getNotificationChannels()`** (Android only)

Returns a list of all notification channels.

- **Returns:** (Array) Array of channel objects

```javascript
const channels = FirebaseCloudMessaging.getNotificationChannels();
channels.forEach(function(channel) {
    Ti.API.info('Channel: ' + channel.name + ' (ID: ' + channel.id + ')');
});
```

---

#### Notification Display Settings (Android only)

**`setForceShowInForeground(showInForeground)`**

Forces notifications to be displayed even when the app is in foreground.

- **showInForeground** (Boolean): True to force display in foreground

```javascript
FirebaseCloudMessaging.setForceShowInForeground(true);
```

---

**`clearLastData()`**

Clears the stored lastData values.

```javascript
FirebaseCloudMessaging.clearLastData();
```

---

#### Notification Management (Android only)

**`cancelNotification(notificationId)`**

Cancels a specific notification by ID.

- **notificationId** (Integer): The notification ID to cancel

```javascript
FirebaseCloudMessaging.cancelNotification(12345);
```

---

**`cancelAllNotifications()`**

Cancels all active notifications from this app.

```javascript
FirebaseCloudMessaging.cancelAllNotifications();
```

---

**`getActiveNotificationsCount()`**

Returns the number of currently active notifications.

- **Returns:** (Integer) Number of active notifications, or -1 if not available (Android < 6.0)

```javascript
const count = FirebaseCloudMessaging.getActiveNotificationsCount();
Ti.API.info('Active notifications: ' + count);
```

---

#### Permission and Settings Management (Android only)

**`areNotificationsEnabled()`**

Checks if notifications are enabled for the app.

- **Returns:** (Boolean) True if notifications are enabled

```javascript
if (!FirebaseCloudMessaging.areNotificationsEnabled()) {
    Ti.UI.createAlertDialog({
        title: 'Notifications Disabled',
        message: 'Please enable notifications in settings',
        buttonNames: ['Open Settings', 'Cancel']
    }).addEventListener('click', function(e) {
        if (e.index === 0) {
            FirebaseCloudMessaging.openNotificationSettings();
        }
    }).show();
}
```

---

**`openNotificationSettings()`**

Opens the system notification settings for the app.

```javascript
FirebaseCloudMessaging.openNotificationSettings();
```

---

**`isIgnoringBatteryOptimizations()`**

Checks if the app is exempt from battery optimization.

- **Returns:** (Boolean) True if exempt from battery optimization

```javascript
if (!FirebaseCloudMessaging.isIgnoringBatteryOptimizations()) {
    Ti.API.warn('App may not receive notifications reliably when in background');
}
```

---

**`requestIgnoreBatteryOptimizations()`**

Requests user permission to exempt the app from battery optimization. This helps ensure reliable notification delivery.

```javascript
if (!FirebaseCloudMessaging.isIgnoringBatteryOptimizations()) {
    const dialog = Ti.UI.createAlertDialog({
        title: 'Enable Reliable Notifications',
        message: 'To receive notifications reliably, please disable battery optimization for this app.',
        buttonNames: ['Configure', 'Not Now']
    });
    
    dialog.addEventListener('click', function(e) {
        if (e.index === 0) {
            FirebaseCloudMessaging.requestIgnoreBatteryOptimizations();
        }
    });
    
    dialog.show();
}
```

**Note:** Requires `<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>` in tiapp.xml

---

**`checkPlayServices()`** (Android only)

Checks if Google Play Services is available and up to date.

- **Returns:** (Object) Play Services availability information

```javascript
const playServicesStatus = FirebaseCloudMessaging.checkPlayServices();

if (!playServicesStatus.available) {
    Ti.API.error('Google Play Services error: ' + playServicesStatus.errorString);
    
    if (playServicesStatus.isUserResolvableError) {
        FirebaseCloudMessaging.showPlayServicesErrorDialog();
    }
}
```

**Returned Object Properties:**
- `available` (Boolean): True if Play Services is available
- `resultCode` (Integer): Status code from Google Play Services
- `errorString` (String): Human-readable error message
- `isUserResolvableError` (Boolean): True if user can resolve the error

---

**`showPlayServicesErrorDialog()`** (Android only)

Shows a system dialog to help the user resolve Google Play Services issues.

```javascript
FirebaseCloudMessaging.showPlayServicesErrorDialog();
```

---

### Properties

**`shouldEstablishDirectChannel`** (Number, get/set)

Controls whether a direct channel should be established.

```javascript
FirebaseCloudMessaging.shouldEstablishDirectChannel = 1;
```

---

**`fcmToken`** (String, get)

The current Firebase Cloud Messaging token.

```javascript
const token = FirebaseCloudMessaging.fcmToken;
if (token) {
    Ti.API.info('FCM Token: ' + token);
}
```

---

**`apnsToken`** (String, set) (iOS only)

Sets the Apple Push Notification Service token.

```javascript
FirebaseCloudMessaging.apnsToken = deviceToken;
```

---

**`lastData`** (Object, get) (Android only)

Contains the data payload when receiving a notification message. Read this before calling `registerForPushNotifications()`.

```javascript
const lastData = FirebaseCloudMessaging.lastData;
if (lastData && lastData.message) {
    Ti.API.info('Last notification data: ' + JSON.stringify(lastData.message));
}
```

---

**`forceShowInForeground`** (Boolean, get) (Android only)

Returns whether notifications are forced to show in foreground.

```javascript
const isForced = FirebaseCloudMessaging.forceShowInForeground;
```

---

### Events

**`didReceiveMessage`**

Fired when a direct message is received.

- **message** (Object): The message payload

```javascript
FirebaseCloudMessaging.addEventListener('didReceiveMessage', function(e) {
    Ti.API.info('Message received: ' + JSON.stringify(e.message));
});
```

**iOS Note:** This event is only called on iOS 10+ for direct messages sent by Firebase. Normal Firebase push notifications are delivered via standard Titanium notification events:

```javascript
// Foreground notification (iOS)
Ti.App.iOS.addEventListener('notification', function(event) {
    // Handle foreground notification
});

// Background notification action (iOS)
Ti.App.iOS.addEventListener('remotenotificationaction', function(event) {
    // Handle background notification action click
});
```

---

**`didRefreshRegistrationToken`**

Fired when the FCM token is registered or refreshed.

- **fcmToken** (String): The new FCM token

```javascript
FirebaseCloudMessaging.addEventListener('didRefreshRegistrationToken', function(e) {
    Ti.API.info('New FCM token: ' + e.fcmToken);
    // Send token to your server
    sendTokenToServer(e.fcmToken);
});
```

---

**`success`** (Android only)

Fires on Android 13+ after calling `registerForPushNotifications()` when the user grants permission.

```javascript
FirebaseCloudMessaging.addEventListener('success', function(e) {
    Ti.API.info('Push notification permission granted');
});
```

---

**`error`** (Android only)

Fires when token registration fails or the user denies `registerForPushNotifications()`.

- **error** (String): Error description

```javascript
FirebaseCloudMessaging.addEventListener('error', function(e) {
    Ti.API.error('Push registration error: ' + e.error);
});
```

---

**`subscribe`** (Android only)

Fires after attempting to subscribe to a topic.

- **success** (Boolean): True if subscription succeeded
- **topic** (String): The topic name

```javascript
FirebaseCloudMessaging.addEventListener('subscribe', function(e) {
    if (e.success) {
        Ti.API.info('Successfully subscribed to topic');
    } else {
        Ti.API.error('Subscription failed');
    }
});
```

---

**`unsubscribe`** (Android only)

Fires after attempting to unsubscribe from a topic.

- **success** (Boolean): True if unsubscription succeeded

```javascript
FirebaseCloudMessaging.addEventListener('unsubscribe', function(e) {
    if (e.success) {
        Ti.API.info('Successfully unsubscribed from topic');
    }
});
```

---

**`tokenRemoved`** (Android only)

Fires after attempting to delete the FCM token.

- **success** (Boolean): True if token deletion succeeded

```javascript
FirebaseCloudMessaging.addEventListener('tokenRemoved', function(e) {
    if (e.success) {
        Ti.API.info('FCM token successfully removed');
    }
});
```

---

**`messagesDeleted`** (Android only)

Fires when messages were deleted on the FCM server (typically when more than 100 messages are queued).

- **messagesDeleted** (Boolean): Always true when this event fires

```javascript
FirebaseCloudMessaging.addEventListener('messagesDeleted', function(e) {
    Ti.API.warn('Some messages were deleted on the server due to queue overflow');
    // Consider syncing with your server to retrieve missed messages
});
```

---

**`notificationAction`** (Android only)

Fires when the user taps a notification action button.

- **actionId** (String): The ID of the action that was tapped
- **data** (Object): The original notification data payload

```javascript
FirebaseCloudMessaging.addEventListener('notificationAction', function(e) {
    Ti.API.info('Action clicked: ' + e.actionId);
    
    if (e.actionId === 'reply') {
        openReplyScreen(e.data);
    } else if (e.actionId === 'dismiss') {
        Ti.API.info('User dismissed notification');
    }
});
```

---

## Basic Usage Example

```javascript
if (OS_IOS) {
    const FirebaseCore = require('firebase.core');
    FirebaseCore.configure();
}

// Important: Import cloud messaging module AFTER configure()
const FirebaseCloudMessaging = require('firebase.cloudmessaging');

// Listen for token registration/refresh
FirebaseCloudMessaging.addEventListener('didRefreshRegistrationToken', onToken);

// Listen for direct messages
FirebaseCloudMessaging.addEventListener('didReceiveMessage', function(e) {
    Ti.API.info('Message: ' + JSON.stringify(e.message));
});

if (OS_ANDROID) {
    // Android Setup
    
    // Create notification channel
    const channel = Ti.Android.NotificationManager.createNotificationChannel({
        id: 'default',
        name: 'Default channel',
        importance: Ti.Android.IMPORTANCE_DEFAULT,
        enableLights: true,
        enableVibration: true,
        showBadge: true
    });
    FirebaseCloudMessaging.setNotificationChannel(channel);
    
    // Display last push data if available
    Ti.API.info('Last data: ' + JSON.stringify(FirebaseCloudMessaging.lastData));
    
    // Request push permission
    requestPushPermissions();
    
} else {
    // iOS Setup
    
    Ti.App.iOS.addEventListener('usernotificationsettings', function eventUserNotificationSettings() {
        Ti.App.iOS.removeEventListener('usernotificationsettings', eventUserNotificationSettings);
        requestPushPermissions();
    });
    
    Ti.App.iOS.registerUserNotificationSettings({
        types: [
            Ti.App.iOS.USER_NOTIFICATION_TYPE_ALERT,
            Ti.App.iOS.USER_NOTIFICATION_TYPE_SOUND,
            Ti.App.iOS.USER_NOTIFICATION_TYPE_BADGE
        ]
    });
}

function requestPushPermissions() {
    Ti.Network.registerForPushNotifications({
        success: function(e) {
            if (OS_ANDROID) {
                // Register for FCM token
                FirebaseCloudMessaging.registerForPushNotifications();
            } else {
                // iOS
                onToken(e);
            }
        },
        error: function(e) {
            Ti.API.error('Push registration error: ' + JSON.stringify(e));
        },
        callback: function(e) {
            // Fired for all notifications (foreground, background, closed)
            Ti.API.info('Notification data: ' + JSON.stringify(e.data));
        }
    });
}

function onToken(e) {
    if (OS_ANDROID) {
        Ti.API.info('New FCM token: ' + e.fcmToken);
    } else {
        if (FirebaseCloudMessaging != null) {
            Ti.API.info('New FCM token: ' + FirebaseCloudMessaging.fcmToken);
        }
    }
}

// Check if token is already available
if (FirebaseCloudMessaging.fcmToken) {
    Ti.API.info('FCM Token: ' + FirebaseCloudMessaging.fcmToken);
} else {
    Ti.API.info('Token is empty. Waiting for the token callback...');
}

// Subscribe to a topic
FirebaseCloudMessaging.subscribeToTopic('testTopic');
```

---

### Handling Notification Click Data (Android)

```javascript
const handleNotificationData = function(notifObj) {
    if (notifObj) {
        const notifData = JSON.parse(notifObj);
        // Process notification data
        Ti.API.info('Notification data: ' + JSON.stringify(notifData));
        FirebaseCloudMessaging.clearLastData();
    }
};

// Check if app was launched from notification click
const launchIntent = Ti.Android.rootActivity.intent;
handleNotificationData(launchIntent.getStringExtra('fcm_data'));

// Handle app resume from notification click
Ti.App.addEventListener('resumed', function() {
    const currIntent = Titanium.Android.currentActivity.intent;
    const notifData = currIntent.getStringExtra('fcm_data');
    handleNotificationData(notifData);
});
```

---

## Advanced Android Features

### Battery Optimization Management

To ensure reliable notification delivery, especially for time-sensitive apps, request exemption from battery optimization:

```javascript
// Check battery optimization status on app launch
if (OS_ANDROID) {
    if (!FirebaseCloudMessaging.isIgnoringBatteryOptimizations()) {
        const dialog = Ti.UI.createAlertDialog({
            title: 'Reliable Notifications',
            message: 'For the best experience, please allow this app to run in the background without restrictions.',
            buttonNames: ['Configure', 'Not Now'],
            cancel: 1
        });
        
        dialog.addEventListener('click', function(e) {
            if (e.index === 0) {
                FirebaseCloudMessaging.requestIgnoreBatteryOptimizations();
            }
        });
        
        dialog.show();
    }
}
```

**Required Permission in tiapp.xml:**

```xml
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>
```

---

### Notification Actions

Add interactive action buttons to your notifications.

#### Setup

1. **Register the BroadcastReceiver** in your tiapp.xml:

```xml
<android xmlns:android="http://schemas.android.com/apk/res/android">
    <manifest>
        <application>
            <receiver 
                android:name="firebase.cloudmessaging.NotificationActionReceiver"
                android:enabled="true"
                android:exported="false">
            </receiver>
        </application>
    </manifest>
</android>
```

2. **Listen for action events** in your app:

```javascript
FirebaseCloudMessaging.addEventListener('notificationAction', function(e) {
    Ti.API.info('User tapped action: ' + e.actionId);
    Ti.API.info('Notification data: ' + JSON.stringify(e.data));
    
    switch(e.actionId) {
        case 'reply':
            openReplyScreen(e.data);
            break;
        case 'dismiss':
            // User dismissed the notification
            break;
        case 'view':
            openDetailScreen(e.data);
            break;
    }
});
```

#### Sending Notifications with Actions

**From your server (JSON payload):**

```json
{
  "to": "FCM_TOKEN_HERE",
  "data": {
    "title": "New Message",
    "message": "John sent you a message",
    "badge": "1",
    "actions": "[{\"id\":\"reply\",\"title\":\"Reply\"},{\"id\":\"dismiss\",\"title\":\"Dismiss\"}]"
  }
}
```

**Python Example:**

```python
import json
import requests

actions = [
    {"id": "reply", "title": "Reply"},
    {"id": "dismiss", "title": "Dismiss"}
]

message = {
    "to": fcm_token,
    "data": {
        "title": "New Message",
        "message": "John sent you a message",
        "badge": "1",
        "actions": json.dumps(actions)
    }
}

response = requests.post(
    'https://fcm.googleapis.com/fcm/send',
    json=message,
    headers={'Authorization': 'key=YOUR_SERVER_KEY'}
)
```

**PHP Example:**

```php
$actions = [
    ["id" => "reply", "title" => "Reply"],
    ["id" => "dismiss", "title" => "Dismiss"]
];

$message = [
    "to" => $fcmToken,
    "data" => [
        "title" => "New Message",
        "message" => "John sent you a message",
        "badge" => "1",
        "actions" => json_encode($actions)
    ]
];
```

**Limitations:**
- Maximum of 3 actions per notification (Android recommendation)
- Actions work on Android 4.1+ (appearance varies by version)
- Icons can be added to actions (see module source for details)

---

### Notification Health Monitoring

Monitor notification delivery and token status:

```javascript
// Periodic health check (every 30 minutes)
setInterval(function() {
    const currentToken = FirebaseCloudMessaging.fcmToken;
    
    if (!currentToken) {
        Ti.API.warn('FCM Token is empty! Attempting to refresh...');
        FirebaseCloudMessaging.getToken();
    }
    
    // Check notification status
    if (!FirebaseCloudMessaging.areNotificationsEnabled()) {
        Ti.API.warn('Notifications are disabled by user');
    }
    
    // Check Play Services
    const playServices = FirebaseCloudMessaging.checkPlayServices();
    if (!playServices.available) {
        Ti.API.error('Google Play Services unavailable: ' + playServices.errorString);
    }
}, 1800000); // 30 minutes
```

---

### Managing Notification Channels

```javascript
// Create multiple channels for different notification types
function createNotificationChannels() {
    // High priority channel for alerts
    FirebaseCloudMessaging.createNotificationChannel({
        channelId: 'alerts',
        channelName: 'Urgent Alerts',
        importance: 'high',
        sound: 'alert_sound',
        vibrate: true,
        lights: true,
        showBadge: true
    });
    
    // Low priority channel for updates
    FirebaseCloudMessaging.createNotificationChannel({
        channelId: 'updates',
        channelName: 'General Updates',
        importance: 'low',
        sound: 'silent',
        vibrate: false,
        lights: false,
        showBadge: false
    });
}

// List all channels
function listChannels() {
    const channels = FirebaseCloudMessaging.getNotificationChannels();
    Ti.API.info('Total channels: ' + channels.length);
    
    channels.forEach(function(channel) {
        Ti.API.info('Channel: ' + channel.name);
        Ti.API.info('  ID: ' + channel.id);
        Ti.API.info('  Importance: ' + channel.importance);
        Ti.API.info('  Vibration: ' + channel.vibrationEnabled);
    });
}

// Get specific channel info
function checkChannel(channelId) {
    const channel = FirebaseCloudMessaging.fetchNotificationChannel(channelId);
    
    if (channel) {
        Ti.API.info('Channel found: ' + channel.name);
        return true;
    } else {
        Ti.API.warn('Channel not found: ' + channelId);
        return false;
    }
}

// Delete old channel
function cleanupOldChannels() {
    FirebaseCloudMessaging.deleteNotificationChannel('old_channel_v1');
}
```

---

### Active Notification Management

```javascript
// Cancel a specific notification
function dismissNotification(notificationId) {
    FirebaseCloudMessaging.cancelNotification(notificationId);
}

// Clear all notifications
function clearAllNotifications() {
    FirebaseCloudMessaging.cancelAllNotifications();
}

// Check notification count
function checkNotificationCount() {
    const count = FirebaseCloudMessaging.getActiveNotificationsCount();
    
    if (count > 0) {
        Ti.API.info('You have ' + count + ' active notifications');
    }
    
    // Clear if too many notifications
    if (count > 10) {
        FirebaseCloudMessaging.cancelAllNotifications();
    }
}
```

---

## Sending Push Messages

Refer to the following resources for sending push messages:

- [Firebase Cloud Messaging Server Documentation](https://firebase.google.com/docs/cloud-messaging/server)
- [Firebase PHP SDK by Kreait](https://github.com/kreait/firebase-php/)

### Example Server Implementations

**PHP Example:**

```php
<?php
$url = 'https://fcm.googleapis.com/fcm/send';
$serverKey = 'YOUR_SERVER_KEY';

$notification = [
    'title' => 'Test Notification',
    'body' => 'This is a test message',
    'sound' => 'default',
    'badge' => '1'
];

$data = [
    'title' => 'Test Notification',
    'message' => 'This is a test message',
    'custom_key' => 'custom_value'
];

$fields = [
    'to' => $deviceToken,
    'notification' => $notification,
    'data' => $data,
    'priority' => 'high'
];

$headers = [
    'Authorization: key=' . $serverKey,
    'Content-Type: application/json'
];

$ch = curl_init();
curl_setopt($ch, CURLOPT_URL, $url);
curl_setopt($ch, CURLOPT_POST, true);
curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));

$result = curl_exec($ch);
curl_close($ch);

echo $result;
?>
```

**Node.js Example:**

```javascript
const admin = require('firebase-admin');

admin.initializeApp({
    credential: admin.credential.applicationDefault()
});

const message = {
    notification: {
        title: 'Test Notification',
        body: 'This is a test message'
    },
    data: {
        title: 'Test Notification',
        message: 'This is a test message',
        custom_key: 'custom_value'
    },
    token: deviceToken
};

admin.messaging().send(message)
    .then((response) => {
        console.log('Successfully sent message:', response);
    })
    .catch((error) => {
        console.log('Error sending message:', error);
    });
```

---

## Integration with Parse

You can use Parse with this module in combination with Firebase.

**Setup:**
1. Include and configure both the Firebase module and Parse module
2. Send your FCM device token to the Parse backend

**Parse Integration Pull Request:**
[Parse Titanium Module with Firebase Support](https://github.com/timanrebel/Parse/pull/59)

### Sending Push via Parse/Sashido

You can send either plain text or JSON payloads:

**Plain Text:**
```
"Test notification from Sashido"
```

**JSON Payload:**
```json
{
    "alert": "Test from Sashido",
    "text": "Additional notification text"
}
```

The JSON format allows you to set both the title/alert and the body text of the notification.

---

## Build from Source

### iOS

```bash
cd ios
ti build -p ios --build-only
```

### Android

```bash
cd android
ti build -p android --build-only
```

---

## Legal

Copyright (c) 2017-Present by Hans Knöchel & Michael Gangolf

---

## Changelog

### Recent Improvements

**Android Enhancements:**
- Added battery optimization management methods
- Implemented notification action buttons support
- Added notification channel query and management methods
- Improved token persistence using SharedPreferences
- Added Google Play Services availability checking
- Enhanced notification state management
- Added active notification count tracking
- Implemented automatic token refresh on device boot
- Added deleted messages callback for queue overflow detection

**Reliability Improvements:**
- Automatic token persistence prevents token loss on app restart
- Battery optimization exemption improves background notification delivery
- Boot receiver ensures token refresh after device restart
- Play Services verification helps diagnose notification issues

---

## Support

For issues, questions, or contributions:
- GitHub Issues: [titanium-firebase-cloud-messaging/issues](https://github.com/hansemannn/titanium-firebase-cloud-messaging/issues)
- Community Support: [TiSlack](http://tislack.org)

If you find this module helpful, consider sponsoring the maintainers via the GitHub Sponsors button.