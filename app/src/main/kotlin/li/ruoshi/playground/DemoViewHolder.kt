package li.ruoshi.playground

import android.support.v7.widget.RecyclerView
import android.widget.TextView

/**
 * Created by ruoshili on 7/13/2016.
 */
class DemoViewHolder(val view: TextView) : RecyclerView.ViewHolder(view) {
    companion object {
        var seq = 0
    }

    val id = seq++

    fun update(i : Int) {
        view.text = "$i"
    }
}