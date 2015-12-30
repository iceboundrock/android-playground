package li.ruoshi.playground.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import li.ruoshi.playground.R;

/**
 * Created by ruoshili on 7/7/15.
 */
public class ApptDurationBlockViewHolder extends RecyclerView.ViewHolder {


    private TextView textView;

    public ApptDurationBlockViewHolder(View itemView, String s) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.duration_time);
        textView.setText(s);
    }

    public void setDuration(String s) {
        textView.setText(s);
    }

}
