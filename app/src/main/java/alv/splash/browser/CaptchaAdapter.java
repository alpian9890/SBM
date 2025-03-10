package alv.splash.browser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CaptchaAdapter extends RecyclerView.Adapter<CaptchaViewHolder> {
    private List<CaptchaDataManager.CaptchaEntry> entries;
    private MainActivity activity;

    public CaptchaAdapter(MainActivity activity, List<CaptchaDataManager.CaptchaEntry> entries) {
        this.activity = activity;
        this.entries = entries;
    }

    @NonNull
    @Override
    public CaptchaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_captcha, parent, false);
        return new CaptchaViewHolder(activity, view);
    }

    @Override
    public void onBindViewHolder(@NonNull CaptchaViewHolder holder, int position) {
        CaptchaDataManager.CaptchaEntry entry = entries.get(position);
        holder.bind(entry);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    public void updateData(List<CaptchaDataManager.CaptchaEntry> newEntries) {
        this.entries = newEntries;
        notifyDataSetChanged();
    }
}