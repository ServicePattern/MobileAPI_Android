# CHANGELOG

The changelog for `BPContactCenter`. Also see the [releases](https://github.com/ServicePattern/MobileAPI_Android/releases) on GitHub.

## [0.1.3](https://github.com/ServicePattern/MobileAPI_Android/releases/tag/0.1.3)

### Fixed
- [Issue 38](https://github.com/ServicePattern/MobileAPI_Android/issues/38): Messages are not properly escaped.
- [Issue 34](https://github.com/ServicePattern/MobileAPI_Android/issues/34): Receiving same message with same timestamp 70 times per second.
- [Issue 36](https://github.com/ServicePattern/MobileAPI_Android/issues/36): Duplicate events after calling startPolling() when polling is already started.
- [Issue 39](https://github.com/ServicePattern/MobileAPI_Android/issues/39): checkAvailability serialization exception if service not available.

### Changed
 - Polling HTTP requests add Cache-Control header and nonce URL parameter. Cache-Control:no-cache prevents caching f the poll request in HTTP proxies (Nginx). Nonce prevents local Volley caching.

### Added
 - Debug logging of entire HTTP requests and responses including HTTP headers
 - Reporting client device OS/SDK versions
 - To address issue #38, the text chat message content added to the chatSessionMessage event (text content is created from HTML content by the server) and 'format' parameter (text or HTML) added to the sendChatMessage request. Application may choose either text or HTML version to display, depending on application rendering capabilities.

## [0.1.2](https://github.com/ServicePattern/MobileAPI_Android/releases/tag/0.1.2)

### Fixed
- [Issue 32](https://github.com/ServicePattern/MobileAPI_Android/issues/32): Crash when connecting to new URL.

### Changed
 - Sample application improvements (localized push notification content; buttons reordering)

## [0.1.1](https://github.com/ServicePattern/MobileAPI_Android/releases/tag/0.1.1)

### Fixed
- [Issue 20](https://github.com/ServicePattern/MobileAPI_Android/issues/20): Demo crashes immediately after entering credentials.
- [Issue 23](https://github.com/ServicePattern/MobileAPI_Android/issues/23): Missing fields in Android compared to iOS.

### Added
 - Missing error codes added to the `ContactCenterError` enumeration
 - startPolling() and stopPolling() methods
 - getVersion() method and corresponding ContactCenterVersion class
 
### Changed
 - Sample application improvements

### Removed
 - subscribeForRemoteNotificationsAPNs() method (not applicable to Android environment)


## [0.1.0-beta](https://github.com/ServicePattern/MobileAPI_Android/releases/tag/0.1.0-beta)

### Fixed

### Added

### Changed

### Removed


## [0.0.2](https://github.com/ServicePattern/MobileAPI_Android/releases/tag/0.0.2)

### Fixed

### Added

### Changed

### Removed


## [0.0.1](https://github.com/ServicePattern/MobileAPI_Android/releases/tag/0.0.1)

### Fixed

### Added

### Changed

### Removed
