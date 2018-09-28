package co.netguru.baby.monitor.client.feature.client.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import co.netguru.baby.monitor.client.R
import co.netguru.baby.monitor.client.application.GlideApp
import com.bumptech.glide.request.RequestOptions

class ChildSpinnerAdapter(
        context: Context,
        resourceId: Int,
        textViewId: Int,
        list: List<ChildSpinnerData>
) : ArrayAdapter<ChildSpinnerData>(context, resourceId, textViewId, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_baby_spinner, parent, false)

        val data = getItem(position) ?: return view

        val textView = view.findViewById(R.id.itemSpinnerBabyNameTv) as TextView
        val imageView = view.findViewById(R.id.itemSpinnerBabyIv) as ImageView

        textView.text = data.name
        GlideApp
                .with(context)
                .load(data.image)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView)

        return view
    }
}