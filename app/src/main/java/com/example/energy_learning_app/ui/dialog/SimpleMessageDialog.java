package com.example.energy_learning_app.ui.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.energy_learning_app.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SimpleMessageDialog {

    public static void show(
            Context context,
            String title,
            String message
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_simple_message, null);

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        MaterialButton btnOk = view.findViewById(R.id.btnOk);

        tvTitle.setText(title);
        tvMessage.setText(message);

        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setCancelable(false)
                .create();

        dialog.show();

        // ✅ 去掉系統 dialog 的外框
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnOk.setOnClickListener(v -> dialog.dismiss());
    }
}
