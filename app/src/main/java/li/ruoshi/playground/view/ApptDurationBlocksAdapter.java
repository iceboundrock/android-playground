package li.ruoshi.playground.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import li.ruoshi.playground.R;

/**
 * Created by ruoshili on 7/7/15.
 */
public class ApptDurationBlocksAdapter extends RecyclerView.Adapter<ApptDurationBlockViewHolder> {
    final String[] durations = new String[]{
            "14:00\r\n~\r\n15:00",
            "15:00\r\n~\r\n16:00",
            "16:00\r\n~\r\n17:00",
            "17:00\r\n~\r\n18:00",
            "18:00\r\n~\r\n19:00",
            "19:00\r\n~\r\n20:00",
    };

    @Override
    public ApptDurationBlockViewHolder onCreateViewHolder(final ViewGroup parent, final int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.appt_select_time_block, parent, false);
        return new ApptDurationBlockViewHolder(v, durations[i]);
    }

    @Override
    public void onBindViewHolder(ApptDurationBlockViewHolder apptDurationBlockViewHolder, int i) {
        apptDurationBlockViewHolder.setDuration(durations[i]);
    }

    @Override
    public int getItemCount() {
        return durations.length;
    }
}
