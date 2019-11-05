<!-- 
    Couple of points about editing:
    
    1. Keep it SIMPLE.
    2. Refer to reference docs and other external sources when possible.
    3. Remember that the file must be useful for new / external developers, and stand as a documentation basis on its own.
    4. Try to make it as informative as possible.
    5. Do not put data that can be easily found in code.
    6. Include this file on ALL branches.
-->

[![Build Status]( https://app.bitrise.io/app/f771060e296f1f5e/status.svg?token=UkluW_9d1sfVP2c5lklYWg&branch=master)](https://app.bitrise.io/app/f771060e296f1f5e#)

<!-- Put your project's name -->
# Baby monitor client android

<!-- METADATA -->
<!-- Add links to JIRA, Google Drive, mailing list and other relevant resources -->
<!-- Add links to CI configs with build status and deployment environment, e.g.: -->
| environment | deployment            | 
|-------------|-----------------------|
| Release     | https://appcenter.ms/orgs/office-4dmm/apps/Baby-Monitor-Client|
| Preprod     | https://appcenter.ms/orgs/office-4dmm/apps/Baby-Monitor-Preprod|
<!--- If applies, add link to app on Google Play -->

## Synopsis
<!-- Describe the project in few sentences -->
Baby Guard is a free, offline, and cross-platform application that replaces electronic baby monitors. The app will notify you on your smartphone whenever your baby starts crying.
## Development

### Integrations
<!-- Describe external service and hardware integrations, link to reference docs, use #### headings -->

### Coding guidelines
[Netguru Android code style guide](https://netguru.atlassian.net/wiki/display/ANDROID/Android+best+practices)
<!-- OPTIONAL: Describe any additional coding guidelines (if non-standard) -->

### Workflow & code review
[Netguru development workflow](https://netguru.atlassian.net/wiki/display/DT2015/Netguru+development+flow)
<!-- OPTIONAL: Describe workflow and code review process (if non-standard) --> 

## Building
<!-- Aim to explain the process so that any new or external developer not familiar with the project can perform build and deploy -->

### Build types
<!-- List and describe build types -->
 1. Debug - for development
 2. ReleasePreprod - for testing
 3. Release - app releases to the store
 
#### debug
 - debuggable
 - disabled ProGuard
 
#### release
 - uses full ProGuard configuration
 - enables zipAlign, shrinkResources
 - non-debuggable

### Build properties
<!-- List all build properties that have to be supplied, including secrets. Describe the method of supplying them, both on local builds and CI -->

| Property         | External property name | Environment variable |
|------------------|------------------------|----------------------|
| Firebase Cloud Messaging Server Key | FirebaseCloudMessagingServerKey | FIREBASE_CLOUD_MESSAGING_SERVER_KEY |

#### Secrets
Follow [this guide](https://netguru.atlassian.net/wiki/pages/viewpage.action?pageId=33030753) 

#### Supported devices
SDK 21+ (5.0 Lollipop)