package co.netguru.baby.monitor.client.feature.analytics

enum class Screen(val screenName: String) {
    SPLASH("Splash"),
    ONBOARDING("Onboarding"),
    INFO_ABOUT_DEVICES("InfoAboutDevices"),
    SPECIFY_DEVICE("SpecifyDevice"),
    VOICE_RECORDINGS_SETTING("VoiceRecordingsSetting"),
    CONNECT_WIFI("ConnectWifi"),
    PERMISSION_CAMERA("PermissionCamera"),
    PERMISSION_MICROPHONE("PermissionMicrophone"),
    PERMISSION_DENIED("PermissionDenied"),
    SETUP_INFORMATION("SetupInformation"),
    CHILD_MONITOR("ChildMonitor"),
    PARENT_DEVICE_INFO("ParentDeviceInfo"),
    SERVICE_DISCOVERY("ServiceDiscovery"),
    PAIRING("Pairing"),
    CONNECTION_FAILED("ConnectionFailed"),
    ALL_DONE("AllDone"),
    CLIENT_LIVE_CAMERA("ClientLiveCamera"),
    CLIENT_DASHBOARD("ClientDashboard"),
    CLIENT_ACTIVITY_LOG("ClientActivityLog")
}
