package com.example.energy_learning_app.ui;


import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.energy_learning_app.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Date;

public class AchievementDetailBottomSheet
        extends BottomSheetDialogFragment {

    private static final String ARG_TITLE = "title";
    private static final String ARG_DESC = "desc";
    private static final String ARG_CURRENT = "current";
    private static final String ARG_TARGET = "target";
    private static final String ARG_UNLOCKED = "unlocked";
    private static final String ARG_UNLOCKED_AT = "unlocked_at";

    public static AchievementDetailBottomSheet newInstance(
            String title,
            String desc,
            int current,
            int target,
            boolean unlocked,
            long unlockedAt
    ) {
        AchievementDetailBottomSheet sheet =
                new AchievementDetailBottomSheet();

        Bundle b = new Bundle();
        b.putString(ARG_TITLE, title);
        b.putString(ARG_DESC, desc);
        b.putInt(ARG_CURRENT, current);
        b.putInt(ARG_TARGET, target);
        b.putBoolean(ARG_UNLOCKED, unlocked);
        b.putLong(ARG_UNLOCKED_AT, unlockedAt);
        sheet.setArguments(b);

        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View v = inflater.inflate(
                R.layout.bottomsheet_achievement_detail,
                container,
                false
        );

        TextView tvTitle = v.findViewById(R.id.tvTitle);
        TextView tvDesc = v.findViewById(R.id.tvDescription);
        TextView tvProgress = v.findViewById(R.id.tvProgress);
        TextView tvUnlockedAt = v.findViewById(R.id.tvUnlockedAt);

        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString(ARG_TITLE);
            String desc = args.getString(ARG_DESC);
            int current = args.getInt(ARG_CURRENT);
            int target = args.getInt(ARG_TARGET);
            boolean unlocked = args.getBoolean(ARG_UNLOCKED);
            long unlockedAt = args.getLong(ARG_UNLOCKED_AT);

            tvTitle.setText(title);
            tvDesc.setText(desc);
            tvProgress.setText("進度：" + current + " / " + target);

            if (unlocked && unlockedAt > 0) {
                String time = DateFormat.format(
                        "yyyy/MM/dd HH:mm",
                        new Date(unlockedAt)
                ).toString();
                tvUnlockedAt.setText("解鎖時間：" + time);
                tvUnlockedAt.setVisibility(View.VISIBLE);
            } else {
                tvUnlockedAt.setText("尚未解鎖");
                tvUnlockedAt.setVisibility(View.VISIBLE);
            }
        }

        return v;
    }
}
