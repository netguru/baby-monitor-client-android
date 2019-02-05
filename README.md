<!-- 
    Couple of points about editing:
    
    1. Keep it SIMPLE.
    2. Refer to reference docs and other external sources when possible.
    3. Remember that the file must be useful for new / external developers, and stand as a documentation basis on its own.
    4. Try to make it as informative as possible.
    5. Do not put data that can be easily found in code.
    6. Include this file on ALL branches.
-->

<!-- Put your project's name -->
# Baby monitor client android

<!-- METADATA -->
<!-- Add links to JIRA, Google Drive, mailing list and other relevant resources -->
<!-- Add links to CI configs with build status and deployment environment, e.g.: -->
| environment | deployment            | status             |
|-------------|-----------------------|--------------------|
| mockRelease        | https://rink.hockeyapp.net/manage/apps/821156| https://app.bitrise.io/app/f771060e296f1f5e/status.svg?token=UkluW_9d1sfVP2c5lklYWg&branch=master |
| productionRelease  | https://rink.hockeyapp.net/manage/apps/821318| https://app.bitrise.io/app/f771060e296f1f5e/status.svg?token=UkluW_9d1sfVP2c5lklYWg&branch=master |
<!--- If applies, add link to app on Google Play -->

## Synopsis
<!-- Describe the project in few sentences -->

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
#### debug
 - debuggable
 - disabled ProGuard
 - uses built-in shrinking (no obfuscation)
 
#### release
 - uses full ProGuard configuration
 - enables zipAlign, shrinkResources
 - non-debuggable

### Product flavors
<!-- List and describe product flavors, purposes and dedicated deployment channels -->
 
#### mock
 - preview API, functional testing
 
#### production
 - production API, release

### Build properties
<!-- List all build properties that have to be supplied, including secrets. Describe the method of supplying them, both on local builds and CI -->

| Property         | External property name | Environment variable |
|------------------|------------------------|----------------------|
| HockeyApp App ID mock | HockeyAppIdMock            | HOCKEY_APP_ID_MOCK|
| HockeyApp App Secret mock | HockeyAppSecretMock            | HOCKEY_APP_SECRET_MOCK|
| HockeyApp App ID production | HockeyAppIdProduction           | HOCKEY_APP_ID_PRODUCTION|
| HockeyApp App Secret production | HockeyAppSecretProduction           | HOCKEY_APP_Secret_PRODUCTION|

#### Secrets
Follow [this guide](https://netguru.atlassian.net/wiki/pages/viewpage.action?pageId=33030753) 

#### Supported devices
SDK 21+ (5.0 Lollipop)
