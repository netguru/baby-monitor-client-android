package co.netguru.baby.monitor.feature.client.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import co.netguru.baby.monitor.R
import co.netguru.baby.monitor.application.GlideApp
import com.bumptech.glide.request.RequestOptions

class ChildSpinnerAdapter(
        spinner: Spinner,
        resourceId: Int,
        textViewId: Int,
        list: List<ChildSpinnerData>,
        private val onChildSelected: (child: ChildSpinnerData) -> Unit
) : ArrayAdapter<ChildSpinnerData>(spinner.context, resourceId, textViewId, list), AdapterView.OnItemSelectedListener {

    init {
        spinner.adapter = this
        spinner.onItemSelectedListener = this
    }

    override fun onNothingSelected(parent: AdapterView<*>?) = Unit

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        onChildSelected(
                getItem(position)
                        ?: throw IllegalStateException("There is no data for position $position")
        )
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_baby_spinner, parent, false)

        val data = getItem(position)
                ?: throw IllegalStateException("There is no data for position $position")

        val textView = view.findViewById<TextView>(R.id.itemSpinnerBabyNameTv)
        val imageView = view.findViewById<ImageView>(R.id.itemSpinnerBabyIv)

        textView.text = data.name
        GlideApp
                .with(context)
                .load(data.image)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)

        return view
    }
}
