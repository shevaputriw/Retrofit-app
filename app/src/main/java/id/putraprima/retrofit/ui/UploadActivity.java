package id.putraprima.retrofit.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import id.putraprima.retrofit.R;
import id.putraprima.retrofit.api.helper.ServiceGenerator;
import id.putraprima.retrofit.api.models.ApiError;
import id.putraprima.retrofit.api.models.ErrorUtils;
import id.putraprima.retrofit.api.services.ApiInterface;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends AppCompatActivity {

    private Uri imageUri;
    private Bitmap imageBitmap;
    private ImageButton foto;
    private String nama_resep, deskripsi, bahan, langkah_pembuatan;
    private EditText namaResepTxt, deskripsiTxt, bahanTxt, langkah_pembuatanTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        namaResepTxt = findViewById(R.id.edtNamaResep);
        deskripsiTxt = findViewById(R.id.edtDeskripsi);
        bahanTxt = findViewById(R.id.edtBahan);
        langkah_pembuatanTxt = findViewById(R.id.edtLangkahPembuatan);
        foto = findViewById(R.id.imageButton);
    }
    private void requestCameraPermission() {
        Dexter.withActivity(this).withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        openCamera();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                            showDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    private File createTempFile(Bitmap bitmap){
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), System.currentTimeMillis()+"_image.png");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapdata = bos.toByteArray();

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return file;
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }
    private void openCamera(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);

    }
    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
        builder.setTitle("Need Permission");
        builder.setMessage("Apk ini membutuhkan akses camera anda ");
        builder.setPositiveButton("BOLEH", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                openCamera();
            }
        });
        builder.setNegativeButton("JANGAN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.show();
    }


    public void doUpload(){
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        nama_resep = namaResepTxt.getText().toString();
        deskripsi = deskripsiTxt.getText().toString();
        bahan = bahanTxt.getText().toString();
        langkah_pembuatan = langkah_pembuatanTxt.getText().toString();

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("fk_user", createPartFromString("1"));
        map.put("nama_resep", createPartFromString(nama_resep));
        map.put("deskripsi", createPartFromString(deskripsi));
        map.put("bahan", createPartFromString(bahan));
        map.put("langkah_pembuatan", createPartFromString(langkah_pembuatan));
        map.put("token", createPartFromString(preference.getString("token", null)));
        File file = createTempFile(imageBitmap);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("foto", file.getName(), reqFile);

        ApiInterface service = ServiceGenerator.createService(ApiInterface.class);
        Call<ResponseBody> call = service.doUpload(body, map);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    Toast.makeText(UploadActivity.this, "Upload sukses", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(UploadActivity.this,RecipeActivity.class);
                    startActivity(i);
                }
                else{
                    ApiError error = ErrorUtils.parseError(response);
                    if(error.getError().getNama_resep()!=null){
                        Toast.makeText(UploadActivity.this, error.getError().getNama_resep().get(0), Toast.LENGTH_SHORT).show();
                    }
                    else if (error.getError().getDeskripsi()!=null){
                        Toast.makeText(UploadActivity.this, error.getError().getDeskripsi().get(0), Toast.LENGTH_SHORT).show();
                    }
                    else if (error.getError().getBahan()!=null){
                        Toast.makeText(UploadActivity.this, error.getError().getBahan().get(0), Toast.LENGTH_SHORT).show();
                    }
                    else if (error.getError().getLangkah_pembuatan()!=null){
                        Toast.makeText(UploadActivity.this, error.getError().getLangkah_pembuatan().get(0), Toast.LENGTH_SHORT).show();
                    }
                    else if (error.getError().getFoto()!=null){
                        Toast.makeText(UploadActivity.this, error.getError().getFoto().get(0), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(UploadActivity.this, "Koneksi gagal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            return;
        }
        if (requestCode == 1) {
            if (data != null) {
                imageUri = data.getData();
                imageBitmap = (Bitmap) data.getExtras().get("data");
                foto.setImageBitmap(imageBitmap);
            }
        }
    }

    public void handlePilihGambar(View view) {
            requestCameraPermission();
    }

    public void handleUploadBaru(View view) {
        if (imageBitmap!=null){
            doUpload();
        }else{
            Toast.makeText(this, "Capture image first", Toast.LENGTH_SHORT).show();
        }
    }
}
