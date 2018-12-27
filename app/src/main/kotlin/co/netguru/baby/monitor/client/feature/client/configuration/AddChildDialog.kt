package co.netguru.baby.monitor.client.feature.client.configuration

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.Observer
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.net.nsd.NsdServiceInfo
import android.support.v7.app.AppCompatActivity
import android.view.View
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.data.ChildData
import co.netguru.baby.monitor.client.data.ChildRepository
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.dialog_new_child_selection.*
import javax.inject.Inject

//TODO if designer will decide that dialog is our final solution convert it to DialogFragment
class AddChildDialog @Inject constructor(
        private val nsdServiceManager: NsdServiceManager,
        private val childRepository: ChildRepository
) : LifecycleObserver {

    private val compositeDisposable = CompositeDisposable()
    private var dialog: DialogViewHolder? = null

    internal fun showDialog(
            activity: AppCompatActivity,
            onChildAdded: (String) -> Unit,
            onServiceConnectionError: () -> Unit
    ) {
        activity.lifecycle.addObserver(this)

        val adapter = createAdapter(onChildAdded)
        dialog = DialogViewHolder(activity).also { it.addAdapter(adapter) }

        nsdServiceManager.serviceInfoData.observe(activity, Observer { serviceList ->
            filterData(serviceList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy { filteredList ->
                        adapter.list = filteredList
                    }.addTo(compositeDisposable)
        })
        nsdServiceManager.discoverService(object : NsdServiceManager.OnServiceConnectedListener {
            override fun onServiceConnectionError() {
                onServiceConnectionError()
            }
        })
    }

    private fun filterData(value: List<NsdServiceInfo>?) =
            Single.fromCallable {
                val list = mutableListOf<NsdServiceInfo>()
                value?.forEach { info ->
                    if (childRepository.childList.value?.find { it.address == info.host.hostAddress } == null) {
                        list.add(info)
                    }
                }
                list
            }


    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    internal fun onPause() {
        dialog?.dismiss()
        nsdServiceManager.stopServiceDiscovery()
        compositeDisposable.dispose()
    }

    private fun createAdapter(onChildAdded: (String) -> Unit) =
            ServiceAdapter { service ->
                childRepository.appendChildrenList(
                        ChildData("ws://${service.host.hostAddress}:${service.port}", name = service.host.hostAddress)
                )
                        .subscribeOn(Schedulers.io())
                        .subscribeBy { wasAdded ->
                            if (wasAdded) {
                                dialog?.dismiss()
                                nsdServiceManager.stopServiceDiscovery()
                                onChildAdded(service.host.hostAddress)
                            }
                        }.addTo(compositeDisposable)
            }

    internal class DialogViewHolder(
            context: Context
    ) : LayoutContainer {
        internal val dialog = MaterialDialog.Builder(context)
                .title(context.getString(R.string.add_child_dialog_title))
                .customView(R.layout.dialog_new_child_selection, true)
                .show()

        override val containerView: View = dialog.view

        fun addAdapter(adapter: ServiceAdapter) {
            dialogAddChildRv.adapter = adapter
        }

        fun dismiss() {
            dialog.dismiss()
        }
    }

}
