package alv.splash.browser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DatabaseManagerFragment extends Fragment {
    private static final int REQUEST_OPEN_DATABASE = 1;
    private TextView currentDatabasePathText;
    private Button openDatabaseButton;
    private Button useInternalButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_database_manager, container, false);

        currentDatabasePathText = view.findViewById(R.id.textCurrentDatabase);
        openDatabaseButton = view.findViewById(R.id.buttonOpenDatabase);
        useInternalButton = view.findViewById(R.id.buttonUseInternalDatabase);

        openDatabaseButton.setOnClickListener(v -> openDatabaseFilePicker());
        useInternalButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).useInternalDatabase();
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    private void openDatabaseFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        // Filter for database files
        String[] mimeTypes = {"application/x-sqlite3", "application/octet-stream"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

        startActivityForResult(intent, REQUEST_OPEN_DATABASE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_OPEN_DATABASE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                handleSelectedDatabaseFile(uri);
            }
        }
    }

    private void handleSelectedDatabaseFile(Uri uri) {
        try {
            // Copy the file to app's storage
            File tempDbFile = new File(getContext().getFilesDir(), "external_database.db");
            copyFileFromUri(uri, tempDbFile);

            currentDatabasePathText.setText("Database: " + tempDbFile.getAbsolutePath());

            // Notify activity
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).onExternalDatabaseSelected(tempDbFile.getAbsolutePath());
            }

            Toast.makeText(getContext(), "Database opened successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to open database: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void copyFileFromUri(Uri uri, File destFile) throws Exception {
        InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
        FileOutputStream outputStream = new FileOutputStream(destFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        inputStream.close();
        outputStream.close();
    }
}