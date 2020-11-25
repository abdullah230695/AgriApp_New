package com.shivaconsulting.agriapp.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shivaconsulting.agriapp.Models.TimeAmPm;
import com.shivaconsulting.agriapp.R;

import java.util.ArrayList;

import static com.shivaconsulting.agriapp.Home.MapsActivity.area_picker_recyclerview;
import static com.shivaconsulting.agriapp.Home.MapsActivity.datePickerTimeline;
import static com.shivaconsulting.agriapp.Home.MapsActivity.pick_area_text;
import static com.shivaconsulting.agriapp.Home.MapsActivity.pick_date_text;
import static com.shivaconsulting.agriapp.Home.MapsActivity.pick_time_text;
import static com.shivaconsulting.agriapp.Home.MapsActivity.time;
import static com.shivaconsulting.agriapp.Home.MapsActivity.time_picker_recyclerview;

public class TimeAdapterNew extends RecyclerView.Adapter<TimeAdapterNew.viewHolder> {

    ArrayList<TimeAmPm> ArList;
    private Context context;


    public TimeAdapterNew(ArrayList<TimeAmPm> arList, Context context) {
        ArList = arList;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.time_pick_item, parent, false);
        return new TimeAdapterNew.viewHolder(view);
    }
int selectedPosition=-1;
    @Override
    public void onBindViewHolder(@NonNull final viewHolder holder, final int position) {

        TimeAmPm AmPm=ArList.get(position);
        if(selectedPosition==position)
            holder.time.setBackgroundColor(Color.parseColor("#000000"));
        else
            holder.time.setBackgroundColor(Color.TRANSPARENT);
        holder.time.setText(AmPm.getTime());
        holder.ampm.setText(AmPm.getAmpm());

        holder.time.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                datePickerTimeline.setVisibility(View.INVISIBLE);
                time_picker_recyclerview.setVisibility(View.INVISIBLE);
                area_picker_recyclerview.setVisibility(View.VISIBLE);
                pick_time_text.setTextSize(14);
                pick_date_text.setTextSize(14);
                pick_area_text.setTextSize(18);
                if(selectedPosition!=-1)
                {
                    holder.time.setBackgroundColor(Color.TRANSPARENT);
                }
                selectedPosition=position;
                time=ArList.get(position).getTime()+ArList.get(position).getAmpm();
                notifyDataSetChanged();

            }
        });

    }

    @Override
    public int getItemCount() {
        return ArList.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {
        TextView time, ampm;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.time_number_text);
            ampm = itemView.findViewById(R.id.time_ampm);
        }
    }
}
