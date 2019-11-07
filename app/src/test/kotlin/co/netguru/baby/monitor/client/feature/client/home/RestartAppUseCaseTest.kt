package co.netguru.baby.monitor.client.feature.client.home

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import co.netguru.baby.monitor.RxSchedulersOverrideRule
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Rule
import org.junit.Test

class RestartAppUseCaseTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val restartAppUseCase: RestartAppUseCase = RestartAppUseCase()
    private val intent: Intent = mock()
    private val packageName = "packageName"
    private val activity: AppCompatActivity = mock {
        val context: Context = mock {
            val packageManagerMock: PackageManager = mock {
                on { getLaunchIntentForPackage(packageName) }.doReturn(intent)
            }
            on { packageManager }.doReturn(packageManagerMock)
            on { packageName }.doReturn(packageName)
        }
        on { baseContext }.doReturn(context)
    }

    @Test
    fun `should restart App`() {
        restartAppUseCase
            .restartApp(activity)
            .subscribe()

        verify(activity).startActivity(intent)
        verify(activity).finish()
    }
}
