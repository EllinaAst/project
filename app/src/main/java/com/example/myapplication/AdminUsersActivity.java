package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;



public class AdminUsersActivity extends AppCompatActivity {

    private LinearLayout usersContainer;
    private DatabaseReference usersRef;
    private List<UserItem> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);

        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);


        // Стрелка назад — с защитой от null
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Пользователи");
        }

        usersContainer = findViewById(R.id.adminUsersContainer);
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        loadUsers();
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadUsers() {
        usersRef.get().addOnSuccessListener(snapshot -> {
            usersContainer.removeAllViews();
            users.clear();

            for (DataSnapshot child : snapshot.getChildren()) {
                String uid = child.getKey();
                String firstName = child.child("firstName").getValue(String.class);
                String lastName = child.child("lastName").getValue(String.class);
                String email = child.child("email").getValue(String.class);
                String role = child.child("role").getValue(String.class);

                UserItem item = new UserItem(uid, firstName, lastName, email, role);
                users.add(item);
                addUserView(item);
            }

        }).addOnFailureListener(e ->
                Toast.makeText(this, "Ошибка загрузки пользователей", Toast.LENGTH_SHORT).show()
        );
    }

    private void addUserView(UserItem user) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_admin_user, usersContainer, false);

        TextView nameTv = row.findViewById(R.id.itemUserName);
        TextView roleTv = row.findViewById(R.id.itemUserRole);
        Button btnView = row.findViewById(R.id.itemUserViewBtn);

        String fullname = (user.lastName != null ? user.lastName + " " : "") +
                (user.firstName != null ? user.firstName : "");

        nameTv.setText(fullname.trim());
        roleTv.setText(user.role != null ? user.role : "user");

        btnView.setOnClickListener(v -> showUserDialog(user));

        usersContainer.addView(row);
    }

    private void showUserDialog(UserItem user) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Пользователь");

        StringBuilder sb = new StringBuilder();
        sb.append("ФИО: ")
                .append(user.lastName != null ? user.lastName : "")
                .append(" ")
                .append(user.firstName != null ? user.firstName : "")
                .append("\n");

        sb.append("Email: ").append(user.email != null ? user.email : "—").append("\n");
        sb.append("Роль: ").append(user.role != null ? user.role : "user").append("\n\n");

        // —–––––––––––––––––––––
        // ОСТАВЛЯЕМ эту строку
        // —–––––––––––––––––––––
        sb.append("Пароль скрыт.").append("\n\n");

        // —–––––––––––––––––––––––––––––
        // Добавляем красивое предупреждение
        // —–––––––––––––––––––––––––––––
        sb.append("*Удаление аккаунтов доступно только через Firebase Console.");

        b.setMessage(sb.toString());

        // 🔥 ВАЖНО: никаких кнопок удаления больше не показываем
        // b.setNeutralButton("Удалить", ... ) — УДАЛЕНО

        b.setPositiveButton("Закрыть", null);
        b.show();
    }



    private void confirmDeleteUser(String uid) {
        new AlertDialog.Builder(this)
                .setTitle("Удалить пользователя")
                .setMessage("Вы уверены? Действие необратимо.")
                .setPositiveButton("Удалить", (d, w) -> {
                    usersRef.child(uid).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Пользователь удалён", Toast.LENGTH_SHORT).show();
                                loadUsers();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Ошибка удаления", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Отмена", null)
                .show();
    }

    // ⬇⬇⬇ ВОТ ЭТО ОЧЕНЬ ВАЖНО — должен существовать этот класс! ⬇⬇⬇
    private static class UserItem {
        String uid, firstName, lastName, email, role;

        UserItem(String uid, String firstName, String lastName, String email, String role) {
            this.uid = uid;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.role = role;
        }
    }
}
