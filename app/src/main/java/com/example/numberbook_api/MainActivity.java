package com.example.numberbook_api;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnLoad, btnSync, btnSearch;
    private EditText etKeyword;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ContactAdapter adapter;
    private List<Contact> contactList = new ArrayList<>();
    private ContactApi contactApi;

    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) loadContacts();
                else Toast.makeText(this, "Permission refusée", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLoad = findViewById(R.id.btnLoadContacts);
        btnSync = findViewById(R.id.btnSyncContacts);
        btnSearch = findViewById(R.id.btnSearch);
        etKeyword = findViewById(R.id.etKeyword);
        recyclerView = findViewById(R.id.recyclerViewContacts);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ContactAdapter();
        recyclerView.setAdapter(adapter);

        contactApi = RetrofitClient.getClient().create(ContactApi.class);

        btnLoad.setOnClickListener(v -> checkPermissionAndLoad());
        btnSync.setOnClickListener(v -> syncContacts());
        btnSearch.setOnClickListener(v -> searchContacts());
    }

    private void checkPermissionAndLoad() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            loadContacts();
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS);
        }
    }

    private void loadContacts() {
        contactList.clear();
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (cursor.moveToNext()) {
                String name = cursor.getString(nameIdx);
                String phone = cursor.getString(phoneIdx);
                contactList.add(new Contact(name, phone));
            }
            cursor.close();
        }
        adapter.setContacts(contactList);
        Toast.makeText(this, contactList.size() + " contacts chargés", Toast.LENGTH_SHORT).show();
    }

    private void syncContacts() {
        if (contactList.isEmpty()) {
            Toast.makeText(this, "Aucun contact à synchroniser", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSync.setEnabled(false);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        int total = contactList.size();

        for (Contact contact : contactList) {
            // Nettoyer le numéro (supprimer espaces, tirets, etc.)
            String cleanPhone = contact.getPhone().replaceAll("[\\s\\-()]", "");
            contact.setPhone(cleanPhone);

            contactApi.insertContact(contact).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                    checkCompletion(total, successCount.get(), failCount.get());
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                    failCount.incrementAndGet();
                    checkCompletion(total, successCount.get(), failCount.get());
                }
            });
        }
    }

    private void checkCompletion(int total, int success, int fail) {
        if (success + fail == total) {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnSync.setEnabled(true);
                Toast.makeText(MainActivity.this,
                        "Sync terminée : " + success + " réussis, " + fail + " échecs",
                        Toast.LENGTH_LONG).show();
            });
        }
    }

    private void searchContacts() {
        String keyword = etKeyword.getText().toString().trim();
        if (keyword.isEmpty()) {
            Toast.makeText(this, "Saisissez un nom ou numéro", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        contactApi.searchContacts(keyword).enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(@NonNull Call<List<Contact>> call, @NonNull Response<List<Contact>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setContacts(response.body());
                    Toast.makeText(MainActivity.this,
                            response.body().size() + " résultat(s)", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Erreur recherche", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Contact>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        });
    }
}