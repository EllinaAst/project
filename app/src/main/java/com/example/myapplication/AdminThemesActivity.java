package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.AlertDialog;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminThemesActivity extends AppCompatActivity {

    private DatabaseReference themesRef;
    private LinearLayout themesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_themes);

        // Toolbar + стрелка
        Toolbar toolbar = findViewById(R.id.adminThemesToolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.btn_add_theme).setOnClickListener(v ->
                startActivity(new Intent(this, AdminCreateThemeActivity.class))
        );

        themesContainer = findViewById(R.id.themesContainer);
        themesRef = FirebaseDatabase.getInstance().getReference("themes");

        loadThemes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadThemes();
    }

    private void loadThemes() {
        themesRef.get().addOnSuccessListener(snapshot -> {
            themesContainer.removeAllViews();

            for (DataSnapshot child : snapshot.getChildren()) {

                String id = child.getKey();
                String title = child.child("title").getValue(String.class);

                View item = getLayoutInflater().inflate(R.layout.item_admin_theme, themesContainer, false);

                TextView titleTxt = item.findViewById(R.id.themeName);
                titleTxt.setText(title != null ? title : "Без названия");

                // Открытие редактирования темы
                item.findViewById(R.id.btnEditTheme).setOnClickListener(v -> {
                    Intent i = new Intent(this, AdminEditThemeActivity.class);
                    i.putExtra("themeId", id);
                    startActivity(i);
                });

                // Открытие списка тестов для темы
                // Открытие списка тестов для темы
                item.findViewById(R.id.btnTests).setOnClickListener(v -> {
                    Intent i = new Intent(this, AdminTestsActivity.class);
                    i.putExtra("themeId", id);   // передаём СТРОКУ, как и нужно
                    startActivity(i);
                });


                // Удаление темы (и связанных тестов)
                item.findViewById(R.id.btnDeleteTheme).setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Удалить тему?")
                            .setMessage("При удалении темы будут также удалены все связанные тесты. Продолжить?")
                            .setPositiveButton("Удалить", (dialog, which) -> {
                                if (id != null) {
                                    themesRef.child(id).removeValue()
                                            .addOnSuccessListener(aVoid -> {
                                                // удалить тесты с тем же id (если есть)
                                                FirebaseDatabase.getInstance().getReference("tests").child(id).removeValue();
                                                Toast.makeText(this, "Тема и тесты удалены", Toast.LENGTH_SHORT).show();
                                                loadThemes();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show());
                                }
                            })
                            .setNegativeButton("Отмена", null)
                            .show();
                });

                themesContainer.addView(item);
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Ошибка загрузки тем", Toast.LENGTH_SHORT).show()
        );
    }
}
