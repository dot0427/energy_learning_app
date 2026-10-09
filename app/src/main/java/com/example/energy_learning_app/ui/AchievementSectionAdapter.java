package com.example.energy_learning_app.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.energy_learning_app.R;
import com.example.energy_learning_app.model.*;

import java.util.ArrayList;
import java.util.List;

public class AchievementSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public interface OnAchievementClickListener {
        void onClick(Achievement achievement);
    }

    private final List<AchievementSection> sections;
    private final OnAchievementClickListener itemClick;

    // 展開後的扁平列表（header + items）
    private final List<Row> rows = new ArrayList<>();

    private static class Row {
        int type;
        AchievementSection section;
        Achievement achievement; // null if header

        static Row header(AchievementSection s) {
            Row r = new Row(); r.type = TYPE_HEADER; r.section = s; return r;
        }
        static Row item(AchievementSection s, Achievement a) {
            Row r = new Row(); r.type = TYPE_ITEM; r.section = s; r.achievement = a; return r;
        }
    }

    public AchievementSectionAdapter(List<AchievementSection> sections, OnAchievementClickListener itemClick) {
        this.sections = sections;
        this.itemClick = itemClick;
        rebuildRows();
    }

    private void rebuildRows() {
        rows.clear();
        for (AchievementSection s : sections) {
            rows.add(Row.header(s));
            if (!s.collapsed) {
                for (Achievement a : s.achievements) {
                    rows.add(Row.item(s, a));
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_achievement_section_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_achievement, parent, false);
            return new ItemVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Row row = rows.get(position);

        if (row.type == TYPE_HEADER) {
            HeaderVH vh = (HeaderVH) holder;
            AchievementSection section = row.section;

            vh.tvTitle.setText(section.title);
            vh.tvSubtitle.setText(section.subtitle);
            vh.ivArrow.setRotation(section.collapsed ? 0f : 180f);
            vh.itemView.setOnClickListener(v -> {
                section.collapsed = !section.collapsed;
                vh.ivArrow.animate()
                        .rotation(section.collapsed ? 0f : 180f)
                        .setDuration(200)
                        .start();

                rebuildRows();
                notifyDataSetChanged();
            });
        }
        else {
            ItemVH vh = (ItemVH) holder;
            Achievement a = row.achievement;
            vh.tvTitle.setText(a.title);
            vh.tvProgress.setText(a.current + " / " + a.target);
            vh.itemView.setAlpha(a.unlocked ? 1.0f : 0.5f);
            vh.itemView.setOnClickListener(v -> {
                if (itemClick != null) itemClick.onClick(a);
            });
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle;
        ImageView ivArrow;

        HeaderVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvSectionTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSectionSubtitle);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }


    static class ItemVH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvProgress;
        ItemVH(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvAchievementTitle);
            tvProgress = itemView.findViewById(R.id.tvAchievementProgress);
        }
    }
}
