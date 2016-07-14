package li.ruoshi.playground

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import java.util.*

/**
 * Created by ruoshili on 7/13/2016.
 */

private const val TAG = "DemoAdapter"

class DemoAdapter() : RecyclerView.Adapter<DemoViewHolder>() {
    private val items: MutableList<Int> = ArrayList()

    override fun onBindViewHolder(holder: DemoViewHolder?, position: Int) {
        if(holder == null) {
            return
        }

        if(position < 0 || position >= items.size) {
            return
        }

        Log.v(TAG, "onBindViewHolder pos: $position, value: ${items[position]}, holder id: ${holder.id}")

        holder.update(items[position])
    }

    override fun onFailedToRecycleView(holder: DemoViewHolder?): Boolean {
        Log.e(TAG, "onFailedToRecycleView")
        return super.onFailedToRecycleView(holder)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): DemoViewHolder {
        val textView = TextView(parent!!.context!!)
        val holder = DemoViewHolder(textView)
        Log.v(TAG, "onCreateViewHolder, holder id: ${holder.id}")

        return holder
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem(i : Int) {
        items.add(i)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(pos : Int) {
        if(pos < 0 || pos >= items.size) {
            return
        }

        items.removeAt(pos)
        notifyItemRemoved(pos)
    }

    fun removeOddItems() {
        while (true) {
            val pos = items.indexOfFirst { it % 2 == 1 }
            if(pos < 0) {
                break
            } else {
                removeItem(pos)
            }
        }
    }

}