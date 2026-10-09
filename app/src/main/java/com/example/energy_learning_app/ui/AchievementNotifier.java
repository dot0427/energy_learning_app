package com.example.energy_learning_app.ui;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.energy_learning_app.R;
import com.example.energy_learning_app.model.*;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class AchievementNotifier {

    public static void notifyUnlocked(
            FragmentActivity activity,
            List<Achievement> unlockedList
    ) {
        if (unlockedList == null || unlockedList.isEmpty()) return;

        // 通常一次只解一個，先這樣就好
        Achievement a = unlockedList.get(0);

        Snackbar snackbar = Snackbar.make(
                activity.findViewById(android.R.id.content),
                "🎉 解鎖成就：" + a.title,
                Snackbar.LENGTH_LONG
        );

        View sbView = snackbar.getView();

        sbView.setBackgroundTintList(
                ColorStateList.valueOf(
                        MaterialColors.getColor(
                                sbView,
                                com.google.android.material.R.attr.colorPrimaryContainer
                        )
                )
        );
        // 陰影（讓它像卡片）
        sbView.setElevation(6f);
        // 文字樣式
        TextView tv = sbView.findViewById(
                com.google.android.material.R.id.snackbar_text
        );
        tv.setTextColor(
                MaterialColors.getColor(
                        sbView,
                        com.google.android.material.R.attr.colorOnPrimaryContainer
                )
        );
        tv.setTypeface(
                ResourcesCompat.getFont(
                        activity,
                        R.font.gensenrounded_medium
                )
        );
        // Action
        snackbar.setAction("查看", v -> {
            AchievementDetailBottomSheet sheet =
                    AchievementDetailBottomSheet.newInstance(
                            a.title,
                            a.description,
                            a.current,
                            a.target,
                            true,
                            a.unlockedAt
                    );
            sheet.show(
                    activity.getSupportFragmentManager(),
                    "ACH_UNLOCKED"
            );
        });
        snackbar.setActionTextColor(
                MaterialColors.getColor(
                        sbView,
                        com.google.android.material.R.attr.colorSecondary
                )
        );

        snackbar.show();


    }
}
