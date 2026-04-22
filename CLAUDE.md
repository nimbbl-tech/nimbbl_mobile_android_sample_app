# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

This is the **Nimbbl Android Sample App** — a demonstration host app for the Nimbbl WebView Checkout SDK. It is not the SDK itself; it consumes the SDK from Maven Central and exercises its public API.

SDK dependency: `tech.nimbbl:webview-sdk:4.0.13` (in `app/build.gradle.kts`)

---

## Commands

```bash
# Build
./gradlew assembleDebug        # Debug APK
./gradlew assembleRelease      # Release APK (signed with debug key)

# Clean
./gradlew clean

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedDebugAndroidTest

# Run a single instrumented test class
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=tech.nimbbl.exmaple.NimbblCheckoutSDKTest

# Lint
./gradlew lintDebug
```

---

## Architecture

### Flow

```
OrderCreateActivity (launcher)
    → NimbblConfigActivity    (settings: environment, experience, access token)
    → DebugLogsActivity       (live logcat viewer, debug-menu only)
    → OrderSucessPageAcitivty (payment result display)
```

### Order creation paths

`OrderCreateActivity` supports two distinct API paths selected at runtime:

1. **No access token** → calls the Nimbbl shop proxy (`/create-shop`) to create a sample order. The shop URL is derived from the API base URL by replacing `api` → `sonicshopapi` in the host.
2. **Access token present** → calls Nimbbl Core API v3 (`/api/v3/create-order`) directly with a `Bearer` token. The token is set in Settings and stored under `AppPreferenceKeys.ACCESS_TOKEN`.

After an order token is obtained, `NimbblCheckoutSDK.getInstance().checkout(options)` is invoked to open the WebView checkout.

### Environment configuration

Stored in `SharedPreferences` under key `app_configs_prefs` (`AppPreferenceKeys.APP_PREFERENCE`):

| Key | Purpose |
|-----|---------|
| `shop_base_url` | Resolved API base URL (prod/pre-prod/QA) |
| `qa_environment_url` | Raw QA URL entered by user |
| `access_token` | Optional Core API auth token |
| `sample_app_mode` | `"Webview"` or `"Native"` |
| `debug_logs_enabled` | SDK debug logging toggle |
| `debug_menu_unlocked` | Hidden debug section (tap header 7×) |

`AppConstants` holds the environment/experience string values. `ApiConstants` holds the URL constants. `AppUtilExtensions.formatUrl()` normalises URLs (adds trailing slash, fixes double slashes).

### IP-based URL handling

If the configured base URL resolves to an IPv4 host, `resolveShopBaseUrl()` falls back to `ApiConstants.BASE_URL_QA1` for the sample-app API calls while still passing the raw IP URL to the SDK via `setEnvironmentUrl()`.

### Key SDK integration points

```kotlin
NimbblCheckoutSDK.getInstance().setEnvironmentUrl(url)   // must be called before checkout
NimbblCheckoutSDK.getInstance().init(activity)
NimbblCheckoutSDK.getInstance().checkout(options)
// Result delivered via NimbblCheckoutPaymentListener.onCheckoutResponse(data)
```

### BuildConfig requirement

`buildConfig = true` must be present in `buildFeatures` (already set). `BuildConfig` lives in package `tech.nimbbl.exmaple`; any class in a sub-package (e.g. `tech.nimbbl.exmaple.ui`) must import it explicitly: `import tech.nimbbl.exmaple.BuildConfig`.

### Local SDK development

To swap Maven SDK for a local module, uncomment the relevant lines in `settings.gradle.kts` and switch the dependency comment in `app/build.gradle.kts`.
