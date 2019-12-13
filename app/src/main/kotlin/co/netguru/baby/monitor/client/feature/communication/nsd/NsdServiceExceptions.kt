package co.netguru.baby.monitor.client.feature.communication.nsd

import java.lang.RuntimeException

class ResolveFailedException: RuntimeException("Service resolution failed")
class RegistrationFailedExcetpion: RuntimeException("Service registration failed")
class StartDiscoveryFailedException: RuntimeException("Service discovery failed")
