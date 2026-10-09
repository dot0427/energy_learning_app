package com.example.energy_learning_app.ui.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.energy_learning_app.R;

public class ToastUtil {

    public static void show(Context context, String message) {
        show(context, "✨", message);
    }

    public static void show(Context context, String icon, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.toast, null);

        TextView tvIcon = layout.findViewById(R.id.ToastIcon);
        TextView tvMessage = layout.findViewById(R.id.ToastMessage);

        tvIcon.setText(icon);
        tvMessage.setText(message);

        Toast toast = new Toast(context);
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }
}