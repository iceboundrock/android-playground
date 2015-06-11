package li.ruoshi.playground

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import java.util.Arrays
import java.util.Collections
import li.ruoshi.playground.R

/**
 * Created by ruoshili on 5/9/15.
 */
public class ActivitiesAdapter(context: Activity)
: ArrayAdapter<ActivityInfo>(context, R.layout.activity_item, R.id.lanuch_activity_button) {

    val TAG = javaClass<ActivitiesAdapter>().getSimpleName()
    val PackageName = "li.ruoshi.playground"
    val activity = context
    init {
        val pkgMgr = context.getPackageManager()
        val pkgInfo = pkgMgr.getPackageInfo(PackageName, PackageManager.GET_ACTIVITIES)

        val launchIntent = pkgMgr.getLaunchIntentForPackage(PackageName)
        pkgInfo.activities.filter { a ->
            !a.name.equals(launchIntent.resolveActivityInfo(pkgMgr, 0).name)

        }.forEach { a ->
            this.add(a)
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val ret = super.getView(position, convertView, parent)
        val btn = ret.findViewById(R.id.lanuch_activity_button) as Button

        val ai = getItem(position)
        val btnText = getContext().getResources().getString(ai.labelRes)
        Log.d(TAG, "Button text: $btnText, position: $position")
        btn.setText(btnText)
        val cls = Class.forName (ai.name)
        btn.setOnClickListener { v ->
            val intent = Intent(activity, cls);
            activity.startActivity(intent)

        }
        return ret
    }
}