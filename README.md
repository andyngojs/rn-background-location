# Getting Started

> **Note**: Make sure you have completed the [Set Up Your Environment](https://reactnative.dev/docs/set-up-your-environment) guide before proceeding.

## Step 1: Start Metro

First, you will need to run **Metro**, the JavaScript build tool for React Native.

```sh
yarn start
```

## Step 2: Build and run your app

With Metro running, open a new terminal window/pane from the root of your React Native project, and use one of the following commands to build and run your Android or iOS app:

### Android

```sh
yarn android
```

### iOS

```sh
cd ios && pod install
# or
npx pod-install
```

```sh
yarn ios
```

## Background Location (Native)

### Android
- Create, register Foreground Service with location
- Request permission related to location, post notification
- Send a notification to the OS confuses the app is on foreground
- Handle life-cycle activity for Foreground Service (foreground and background) (onStart, onStop)
- Handle location service API when the app go to background, it launch Foreground Service
- Handle broadcast listen GPS status
  - if GPS is off, it send a notify and stop location update 
  - if GPS is on, continue location updating, and it send only a notify when not first time 

### IOS 

