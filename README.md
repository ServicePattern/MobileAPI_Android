# BPMobileMessaging

[![](https://jitpack.io/v/ServicePattern/MobileAPI_Android.svg)](https://jitpack.io/#ServicePattern/MobileAPI_Android)

## Example

To run the example project, you have to request for valid credantials, than clone the repo and run it

## Requirements

## Quick Start
### Adding the SDK to your project

1. Add jitpack to the root build.gradle file of your project at the end of repositories.
```
     allprojects {
         repositories {
             ...
             maven { url 'https://jitpack.io' }
         }
     }

```

2. Add the dependency
```
dependencies {
  implementation 'com.github.ServicePattern:MobileAPI_Android:<LATEST_VERSION>'
}
```

### How to use

3. Generate the unique `clientID` for your application. The `clientID` should be generated when application runs for the first time on the  mobile device and saved in the local storage. The application should use same value until it is deleted from the device. The `clientID` should be unique for the application / device combination. 
```kotlin
  val clientID = UUID.randomUUID().toString()
```

4. Сreate an instance of the `ContactCenterCommunicator` class which would handle communications with the BPCC server:
```kotlin
  val baseURL = "https://<your server URL>"
  val tenantURL = "<your tenant URL>"
  var appID = "<your messaging scenario entry ID>"
  val api = ContactCenterCommunicator.init(baseURL, tenantURL, appID, clientID, applicationContext)
```

5. Register for push notifications. Request for a token and register it in the API
```kotlin
FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
  if (!task.isSuccessful) {
    Log.w("*****", "Fetching FCM registration token failed", task.exception)
    // TODO: handle it !!!!!
  }
   api.subscribeForRemoteNotificationsFirebase(chatID, task.result) { r -> resultProcessing(r) /* define a result processing function */}
}
```

6. Creeate FireBaseMessaging service
```kotlin
class CDFirebaseMessagingService : FirebaseMessagingService() {
    val api: ContactCenterCommunicator? = <API reference>
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.containsKey("chatID"))
            <API reference>?.appDidReceiveMessage(remoteMessage.data.toMap())
    }

    override fun onNewToken(token: String) {
        ifNotNull(token.isNotEmpty(), ChatDemo.chatID) {
            <API reference>?.subscribeForRemoteNotificationsFirebase(token, ChatDemo.chatID) {
                Log.e("onNewToken", " subscribeForRemoteNotificationsFirebase > $it")
            }
        }
    }
}
```

7. Add the callback
```kotlin
api.callback = object : ContactCenterEventsInterface {
  override fun chatSessionEvents(result: Result<List<ContactCenterEvent>, Error>) {
    /* process the callback' result */
    this@<ACTIVITY>.resultProcessing(result)
  }
}
```

## Author

[BrightPattern](https://brightpattern.com)

## License

BPMobileMessaging is available under the MIT license. See the LICENSE file for more info.
