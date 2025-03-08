package alv.splash.browser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

public class CaptchaDataManager {
    private static final String TAG = "CaptchaDataManager";
    private CaptchaDbHelper dbHelper;
    private Context context;

    public CaptchaDataManager(Context context) {
        this.context = context;
        // Create database in app's files directory
        File dbDir = new File(context.getExternalFilesDir("Datasets").getPath());
        if (!dbDir.exists()) {
            dbDir.mkdirs();
        }
        dbHelper = new CaptchaDbHelper(context);
    }

    /**
     * Save CAPTCHA data to SQLite
     */
    public boolean saveCaptchaData(String base64Data, String label) {
        Log.d(TAG, "saveCaptchaData() called");

        if (base64Data == null || base64Data.isEmpty()) {
            Log.e(TAG, "Base64 data is empty");
            return false;
        }

        try {
            // Calculate hash
            String imageHash = calculateSHA256(base64Data);

            // Check if exists
            if (getLabelByHash(imageHash) != null) {
                Log.d(TAG, "Image already exists in database");
                return false;
            }

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CaptchaDbHelper.COLUMN_IMAGE_HASH, imageHash);
            values.put(CaptchaDbHelper.COLUMN_LABEL, label);
            values.put(CaptchaDbHelper.COLUMN_IMAGE_DATA, base64Data);

            long newRowId = db.insert(CaptchaDbHelper.TABLE_CAPTCHAS, null, values);
            return newRowId != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error saving captcha data: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Find label by hash
     */
    public String getLabelByHash(String imageHash) {
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String[] projection = {CaptchaDbHelper.COLUMN_LABEL};
            String selection = CaptchaDbHelper.COLUMN_IMAGE_HASH + " = ?";
            String[] selectionArgs = {imageHash};

            Cursor cursor = db.query(
                    CaptchaDbHelper.TABLE_CAPTCHAS,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            String label = null;
            if (cursor.moveToFirst()) {
                label = cursor.getString(cursor.getColumnIndexOrThrow(CaptchaDbHelper.COLUMN_LABEL));
            }
            cursor.close();
            return label;
        } catch (Exception e) {
            Log.e(TAG, "Error getting label by hash: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Find label for an image
     */
    public String getLabelForImage(String base64Data) {
        if (base64Data == null || base64Data.isEmpty()) {
            return null;
        }
        String imageHash = calculateSHA256(base64Data);
        return getLabelByHash(imageHash);
    }

    /**
     * Calculate SHA-256 hash
     */
    private String calculateSHA256(String data) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes());
            return bytesToHex(hash);
        } catch (Exception e) {
            Log.e(TAG, "Error calculating hash: " + e.getMessage(), e);
            return "";
        }
    }

    /**
     * Convert bytes to hex
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Import data from files
     */
    public void importFromFileSystem(String imagesPath, String csvPath) {
        Log.d(TAG, "Importing data from: " + csvPath);

        try {
            File csvFile = new File(csvPath);
            if (!csvFile.exists()) {
                Log.e(TAG, "CSV file not found: " + csvPath);
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(csvFile));
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String imagePath = parts[0];
                    String label = parts[1];

                    // Get full path
                    File imageFile = new File(imagesPath.substring(0, imagesPath.lastIndexOf("/")), imagePath);

                    if (imageFile.exists()) {
                        // Read image
                        byte[] imageBytes;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            imageBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
                        } else {
                            FileInputStream fis = new FileInputStream(imageFile);
                            imageBytes = new byte[(int) imageFile.length()];
                            fis.read(imageBytes);
                            fis.close();
                        }

                        // Convert to base64
                        String extension = imagePath.substring(imagePath.lastIndexOf(".") + 1);
                        String base64 = "data:image/" + extension + ";base64," +
                                android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT);

                        // Save to DB
                        saveCaptchaData(base64, label);
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            Log.e(TAG, "Error importing: " + e.getMessage(), e);
        }
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
