package co.netguru.baby.monitor.client.feature.communication.nsd

import com.jaredrummler.android.device.DeviceName
import javax.inject.Inject

class DeviceNameProvider @Inject constructor() : IDeviceNameProvider {
    override fun getDeviceName(): String = DeviceName.getDeviceName()
}
