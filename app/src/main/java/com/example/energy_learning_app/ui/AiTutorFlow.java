package com.example.energy_learning_app.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.*;
import android.widget.*;

import com.example.energy_learning_app.ui.dialog.SimpleMessageDialog;
import com.example.energy_learning_app.ui.util.ToastUtil;
import com.google.android.material.button.*;
import com.google.android.material.dialog.*;

import com.example.energy_learning_app.R;

import androidx.annotation.Nullable;

import java.util.List;

public class AiTutorFlow {

    public interface ResultCallback {
        void onFinish(int correct, int wrong, int disputed);
    }

    private final Context context;
    private final AiTeacher aiTeacher;
    private okhttp3.Call runningCall;

    @Nullable
    private AiTeacher.TutorPack cachedPack;
    @Nullable
    private android.view.View[] disableWhileLoadingViews;
    public AiTutorFlow(Context context, AiTeacher aiTeacher) {
        this.context = context;
        this.aiTeacher = aiTeacher;
    }

    public void clearCache() {
        cachedPack = null;
    }

    public void setDisableWhileLoadingViews(@Nullable android.view.View... views) {
        this.disableWhileLoadingViews = views;
    }

    public void showExplain(String prompt) {
        ensurePack(prompt, new PackReadyCallback() {
            @Override public void onReady(AiTeacher.TutorPack pack) {
                showExplainDialog(pack.explanation, prompt);
            }
        });
    }

    public void startQuiz(String prompt) {
        startQuiz(prompt, null);
    }
    public void startQuiz(String prompt, @Nullable ResultCallback resultCallback) {
        ensurePack(prompt, new PackReadyCallback() {
            @Override public void onReady(AiTeacher.TutorPack pack) {
                showQuizAtIndex(prompt, 0, 0, 0, resultCallback);
            }
        });
    }

    private interface PackReadyCallback {
        void onReady(AiTeacher.TutorPack pack);
    }

    private void ensurePack(String prompt, PackReadyCallback cb) {
        if (cachedPack != null && cachedPack.quiz != null && cachedPack.quiz.size() >= 4) {
            cb.onReady(cachedPack);
            return;
        }

        setLoadingUi(false);

        runningCall = aiTeacher.fetchTutorPack(prompt, new AiTeacher.TutorCallback() {
            @Override
            public void onSuccess(AiTeacher.TutorPack pack) {
                runOnUiThread(() -> {
                    cachedPack = pack;
                    setLoadingUi(true);
                    cb.onReady(pack);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    setLoadingUi(true);
                    new AlertDialog.Builder(context)
                            .setTitle("AI 取得失敗")
                            .setMessage(e.getMessage())
                            .setPositiveButton("好", null)
                            .show();
                });
            }
        });
    }

    public void cancelRunning() {
        if (runningCall != null) {
            runningCall.cancel();
            runningCall = null;
        }
    }

    private void showExplainDialog(String explanation, String prompt) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_explain, null);

        TextView tv = view.findViewById(R.id.tvExplanation);
        MaterialButton ok = view.findViewById(R.id.btnExplainOk);
        MaterialButton goQuiz = view.findViewById(R.id.btnExplainQuiz);

        tv.setText(explanation == null ? "" : explanation);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setCancelable(false)
                .create();

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ok.setOnClickListener(v -> dialog.dismiss());

        goQuiz.setOnClickListener(v -> {
            dialog.dismiss();
            startQuiz(prompt, null);
        });
    }


    private void showQuizAtIndex(String prompt, int index, int correctCount, int disputedCount, @Nullable ResultCallback resultCallback)
    {
        if (cachedPack == null || cachedPack.quiz == null) {
            SimpleMessageDialog.show(
                    context,
                    "題庫尚未準備好",
                    "AI 題目仍在產生中，請稍後再試一次 🙏");
            return;
        }

        if (index >= 4) {
            int total = 4;

            int effectiveTotal = total - disputedCount;
            if (effectiveTotal < 0) effectiveTotal = 0;

            int wrong = effectiveTotal - correctCount;
            if (wrong < 0) wrong = 0;

            boolean isPerfect = (effectiveTotal > 0 && wrong == 0);
            boolean isZero = (effectiveTotal > 0 && correctCount == 0);

            if (disputedCount > 0) {
                com.example.energy_learning_app.data.AchievementManager.onEvent(
                        context,
                        com.example.energy_learning_app.data.AchievementEvent.REPORT_QUESTION,
                        0,
                        false,
                        false,
                        disputedCount
                );
            }

            List<com.example.energy_learning_app.model.Achievement> unlocked =
                    com.example.energy_learning_app.data.AchievementManager.onEvent(
                            context,
                            com.example.energy_learning_app.data.AchievementEvent.ANSWER_SUBMIT,
                            effectiveTotal,
                            isPerfect,
                            isZero,
                            disputedCount
                    );

            if (context instanceof androidx.fragment.app.FragmentActivity) {
                com.example.energy_learning_app.ui.AchievementNotifier.notifyUnlocked(
                        (androidx.fragment.app.FragmentActivity) context,
                        unlocked
                );
            }

            if (resultCallback != null) {
                resultCallback.onFinish(correctCount, wrong, disputedCount);
            }

            showResultDialog(total, correctCount, wrong, disputedCount, resultCallback);
            return;
        }


        List<AiTeacher.QuizItem> quizList = cachedPack.quiz;
        AiTeacher.QuizItem q = quizList.get(index);
        boolean isLast = (index == 3);

        //android.util.Log.d("QUIZ_OPT", "keys=" + (q.options == null ? "null" : q.options.keySet()));
        //android.util.Log.d("QUIZ_OPT", "map=" + q.options);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quiz, null);

        TextView tvTitle = view.findViewById(R.id.tvQuizTitle);
        TextView tvQ = view.findViewById(R.id.tvQuizQuestion);
        TextView tvRationale = view.findViewById(R.id.tvRationale);

        RadioGroup rg = view.findViewById(R.id.rgOptions);
        RadioButton rbA = view.findViewById(R.id.rbA);
        RadioButton rbB = view.findViewById(R.id.rbB);
        RadioButton rbC = view.findViewById(R.id.rbC);
        RadioButton rbD = view.findViewById(R.id.rbD);

        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialButton btnSubmit = view.findViewById(R.id.btnSubmit);
        MaterialButton btnNext = view.findViewById(R.id.btnNext);
        MaterialButton btnReport = view.findViewById(R.id.btnReport);

        tvTitle.setText("🧠 練習題 " + (index + 1) + "/4");
        tvQ.setText(q.question == null ? "" : q.question);

        String a = q.options != null ? q.options.get("A") : "";
        String b = q.options != null ? q.options.get("B") : "";
        String c = q.options != null ? q.options.get("C") : "";
        String d = q.options != null ? q.options.get("D") : "";
        String positiveText = isLast ? "結果" : "下一題";

        rbA.setText("A. " + a);
        rbB.setText("B. " + b);
        rbC.setText("C. " + c);
        rbD.setText("D. " + d);
        btnNext.setText(positiveText);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setCancelable(false)
                .create();

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        final int[] newCorrectHolder = new int[]{correctCount};
        final int[] disputedHolder = {disputedCount};
        final boolean[] lastAnswerCorrect = {false};

        btnSubmit.setOnClickListener(v -> {
            int checkedId = rg.getCheckedRadioButtonId();
            if (checkedId == View.NO_ID) {
                ToastUtil.show(context, "請先選一個答案～");
                return;
            }

            String chosen =
                    checkedId == R.id.rbA ? "A" :
                            checkedId == R.id.rbB ? "B" :
                                    checkedId == R.id.rbC ? "C" : "D";

            boolean correct = chosen.equalsIgnoreCase(q.answer);
            if (correct) newCorrectHolder[0]++;

            String feedback =
                    (correct ? "🎉 答對了！\n" : "❌ 答錯了！正確答案：" + (q.answer == null ? "?" : q.answer) + "\n")
                            + "解析：" + (q.rationale == null ? "" : q.rationale);

            lastAnswerCorrect[0] = correct;

            tvRationale.setText(feedback);
            tvRationale.setVisibility(View.VISIBLE);

            for (int i = 0; i < rg.getChildCount(); i++) rg.getChildAt(i).setEnabled(false);

            btnSubmit.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
            btnReport.setVisibility(View.VISIBLE);
        });

        btnNext.setOnClickListener(v -> {
            dialog.dismiss();
            showQuizAtIndex(prompt, index + 1, newCorrectHolder[0], disputedHolder[0], resultCallback);
        });

        btnReport.setOnClickListener(v -> {
            disputedHolder[0]++;

            if (!lastAnswerCorrect[0]) {
                newCorrectHolder[0]++;
            }

            ToastUtil.show(context, "已收到回報～這題不會算進正確率");
            dialog.dismiss();

            if (isLast) {
                showQuizAtIndex(prompt, 4, newCorrectHolder[0], disputedHolder[0], resultCallback);
            } else {
                showQuizAtIndex(prompt, index + 1, newCorrectHolder[0], disputedHolder[0], resultCallback);
            }
        });

    }

    private void setLoadingUi(boolean enabled) {
        if (disableWhileLoadingViews != null) {
            for (android.view.View v : disableWhileLoadingViews) {
                if (v != null) v.setEnabled(enabled);
            }
        }
    }

    private void runOnUiThread(Runnable r) {
        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).runOnUiThread(r);
        } else {
            r.run();
        }
    }

    private void showResultDialog(int total, int correctCount, int wrong, int disputedCount, @Nullable ResultCallback resultCallback)
    {
        if (resultCallback != null) {
            resultCallback.onFinish(correctCount, wrong, disputedCount);
        }

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quiz_result, null);
        TextView tv = view.findViewById(R.id.tvResultSummary);
        MaterialButton done = view.findViewById(R.id.btnResultDone);

        tv.setText(
                "共 " + total + " 題\n"
                        + "答對：" + correctCount + "\n"
                        + "答錯：" + wrong + "\n"
                        + "已檢舉：" + disputedCount
        );

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(view)
                .setCancelable(false)
                .create();

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        done.setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent intent = new android.content.Intent(context, com.example.energy_learning_app.StartActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        });
    }

}
