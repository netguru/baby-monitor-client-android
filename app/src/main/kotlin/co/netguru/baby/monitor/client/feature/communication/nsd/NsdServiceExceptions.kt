package co.netguru.baby.monitor.client.feature.communication.nsd

import java.lang.RuntimeException

class ResolveFailedException : RuntimeException("Service resolution failed")
class RegistrationFailedException : RuntimeException("Service registration failed")
class StartDiscoveryFailedException : RuntimeException("Service discovery failed")
