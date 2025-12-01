package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestActivity extends AppCompatActivity {

    private TextView testTitle, questionText;
    private RadioGroup answersGroup;
    private RadioButton ans1, ans2, ans3, ans4;
    private Button nextBtn;

    private final List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;
    private int correctAnswers = 0;

    private int themeId;
    private String uid;

    private DatabaseReference testRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        testTitle = findViewById(R.id.testTitle);
        questionText = findViewById(R.id.questionText);
        answersGroup = findViewById(R.id.answersGroup);

        ans1 = findViewById(R.id.answer1);
        ans2 = findViewById(R.id.answer2);
        ans3 = findViewById(R.id.answer3);
        ans4 = findViewById(R.id.answer4);

        nextBtn = findViewById(R.id.nextButton);

        themeId = getIntent().getIntExtra("themeId", 1);
        uid = FirebaseAuth.getInstance().getUid();

        testRef = FirebaseDatabase.getInstance()
                .getReference("tests")
                .child(String.valueOf(themeId));

        loadQuestions();
    }

    private void loadQuestions() {
        testRef.get().addOnSuccessListener(snapshot -> {
            questions.clear();

            if (!snapshot.exists()) {
                Toast.makeText(this, "Нет вопросов по этой теме", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            for (DataSnapshot qSnap : snapshot.getChildren()) {
                String qText = qSnap.child("question").getValue(String.class);
                String a1 = qSnap.child("ans1").getValue(String.class);
                String a2 = qSnap.child("ans2").getValue(String.class);
                String a3 = qSnap.child("ans3").getValue(String.class);
                String a4 = qSnap.child("ans4").getValue(String.class);
                Integer correct = qSnap.child("correct").getValue(Integer.class);

                if (qText == null || a1 == null || a2 == null || a3 == null || a4 == null || correct == null)
                    continue;

                questions.add(new Question(qText, a1, a2, a3, a4, correct));
            }

            if (questions.isEmpty()) {
                Toast.makeText(this, "Вопросов нет", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // 🔥 Перемешиваем вопросы
            Collections.shuffle(questions);

            showQuestion();

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showQuestion() {
        if (currentIndex >= questions.size()) {
            showResult();
            return;
        }

        Question q = questions.get(currentIndex);

        // обновляем заголовок
        testTitle.setText("Вопрос " + (currentIndex + 1) + " из " + questions.size());
        questionText.setText(q.question);

        // 🔥 ПЕРЕМЕШИВАЕМ ОТВЕТЫ
        List<AnswerOption> opts = new ArrayList<>();
        opts.add(new AnswerOption(q.ans1, 1));
        opts.add(new AnswerOption(q.ans2, 2));
        opts.add(new AnswerOption(q.ans3, 3));
        opts.add(new AnswerOption(q.ans4, 4));
        Collections.shuffle(opts);

        // присваиваем текст
        ans1.setText(opts.get(0).text);
        ans2.setText(opts.get(1).text);
        ans3.setText(opts.get(2).text);
        ans4.setText(opts.get(3).text);

        // сохраняем как "к какой кнопке относится правильный ответ"
        ans1.setTag(opts.get(0).index);
        ans2.setTag(opts.get(1).index);
        ans3.setTag(opts.get(2).index);
        ans4.setTag(opts.get(3).index);

        answersGroup.clearCheck();

        nextBtn.setText(currentIndex == questions.size() - 1 ? "Проверить" : "Далее");

        nextBtn.setOnClickListener(v -> checkAnswer(q.correct));
    }

    private void checkAnswer(int correctIndex) {
        int selectedId = answersGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this, "Выберите ответ", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selected = findViewById(selectedId);
        int chosen = (int) selected.getTag();

        if (chosen == correctIndex) correctAnswers++;

        currentIndex++;
        showQuestion();
    }

    private void showResult() {
        int total = questions.size();
        int percent = (correctAnswers * 100) / total;

        if (uid != null) {
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("progress")
                    .child("theme" + themeId)
                    .setValue(percent);
        }

        new AlertDialog.Builder(this)
                .setTitle("Результат")
                .setMessage("Правильно: " + correctAnswers + " из " + total +
                        "\nПроцент: " + percent + "%")
                .setCancelable(false)
                .setPositiveButton("К темам", (dialog, w) -> {
                    startActivity(new Intent(this, ThemeSelectionActivity.class));
                    finish();
                })
                .show();
    }

    // --- структуры данных ---
    private static class Question {
        String question, ans1, ans2, ans3, ans4;
        int correct;

        Question(String q, String a1, String a2, String a3, String a4, int c) {
            question = q;
            ans1 = a1;
            ans2 = a2;
            ans3 = a3;
            ans4 = a4;
            correct = c;
        }
    }

    private static class AnswerOption {
        String text;
        int index;

        AnswerOption(String t, int i) {
            text = t;
            index = i;
        }
    }
}
