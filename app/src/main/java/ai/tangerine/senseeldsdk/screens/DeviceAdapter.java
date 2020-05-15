package ai.tangerine.senseeldsdk.screens;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ai.tangerine.senseeldsdk.R;


public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.TripHistoryVH> {

    private List<String> devices = new ArrayList<>();
    private Context context;
    private OnDeviceClickListener deviceClickListener;

    DeviceAdapter(Context context, OnDeviceClickListener onDeviceClickListener) {
        this.context = context;
        this.deviceClickListener = onDeviceClickListener;
    }

    @NonNull
    @Override
    public TripHistoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int LayoutId = R.layout.layout_device_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(LayoutId, parent, false);
        return new TripHistoryVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripHistoryVH holder, int position) {
        String device = devices.get(position);

        holder.title.setText(device);
        holder.cardView.setOnClickListener(v -> {
            if(deviceClickListener != null) {
                deviceClickListener.onDeviceClicked(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (devices != null) {
            return devices.size();
        }
        return 0;
    }

    public interface OnDeviceClickListener {
        void onDeviceClicked(String device);
    }

    public void setList(List<String> list) {
        devices.clear();
        devices.addAll(list);
        notifyDataSetChanged();
    }

    public void addDevice(String device) {
        if(!devices.contains(device)) {
            devices.add(device);
            notifyDataSetChanged();
        }
    }

    class TripHistoryVH extends RecyclerView.ViewHolder {

        AppCompatTextView title;

        ConstraintLayout cardView;

        TripHistoryVH(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            cardView = itemView.findViewById(R.id.parent_view);
        }
    }

}
