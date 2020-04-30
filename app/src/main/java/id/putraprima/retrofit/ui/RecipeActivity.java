package id.putraprima.retrofit.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import id.putraprima.retrofit.R;
import id.putraprima.retrofit.adapter.RecipeAdapter;
import id.putraprima.retrofit.api.helper.ServiceGenerator;
import id.putraprima.retrofit.api.models.Envelope;
import id.putraprima.retrofit.api.models.Recipe;
import id.putraprima.retrofit.api.services.ApiInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeActivity extends AppCompatActivity {

    ArrayList<Recipe> recipe;
    RecipeAdapter adapter;
    private ConstraintLayout mRecipeLayout;
    int page = 2;
    Button btnLoadMore;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        recipe = new ArrayList<>();

        RecyclerView recipeView = findViewById(R.id.rvRecipe);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recipeView.setLayoutManager(layoutManager);

        adapter = new RecipeAdapter(recipe);
        recipeView.setAdapter(adapter);
        mRecipeLayout = findViewById(R.id.recipeLayout);
        doLoad();

        btnLoadMore = findViewById(R.id.buttonLoad);
    }

    public void doRecipe() {
        ApiInterface service = ServiceGenerator.createService(ApiInterface.class);
        Call<Envelope<List<Recipe>>> call = service.doRecipe();
        call.enqueue(new Callback<Envelope<List<Recipe>>>() {
            @Override
            public void onResponse(Call<Envelope<List<Recipe>>> call, Response<Envelope<List<Recipe>>> response) {
                if (response.isSuccessful()) {
                    for (int i = 0; i < response.body().getData().size(); i++) {
                        int id = response.body().getData().get(i).getId();
                        String namaResep = response.body().getData().get(i).getNama_resep();
                        String deskripsi = response.body().getData().get(i).getDeskripsi();
                        String bahan = response.body().getData().get(i).getBahan();
                        String langkahPembuatan = response.body().getData().get(i).getLangkah_pembuatan();
                        String foto = response.body().getData().get(i).getFoto();
                        recipe.add(new Recipe(id, namaResep, deskripsi, bahan, langkahPembuatan, foto));
                    }
                    Snackbar snackbar = Snackbar.make(mRecipeLayout, "Memuat data, Mohon ditunggu", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }else {
                    Snackbar snackbar = Snackbar.make(mRecipeLayout, "Gagal mengambil data", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }

            @Override
            public void onFailure(Call<Envelope<List<Recipe>>> call, Throwable t) {
                Snackbar snackbar = Snackbar.make(mRecipeLayout, "Gagal koneksi", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        });
    }

    public void doLoad() {
        recipe.clear();
        adapter.notifyDataSetChanged();

        doRecipe();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        }, 3000);
    }

    public void tombolLoad(View view) {
        progressDialog = new ProgressDialog(RecipeActivity.this);
        progressDialog.setMessage("Mohon ditunggu");
        progressDialog.show();
        NextLoad();
    }

    public void NextLoad(){
        ApiInterface service = ServiceGenerator.createService(ApiInterface.class);
        Call<Envelope<List<Recipe>>> call = service.doLoadMore(page++);
        call.enqueue(new Callback<Envelope<List<Recipe>>>() {
            @Override
            public void onResponse(Call<Envelope<List<Recipe>>> call, Response<Envelope<List<Recipe>>> response) {
                if(response.isSuccessful()){
                    progressDialog.dismiss();
                    for(int i=0; i<response.body().getData().size(); i++){
                        int id = response.body().getData().get(i).getId();
                        String namaResep = response.body().getData().get(i).getNama_resep();
                        String deskripsi = response.body().getData().get(i).getDeskripsi();
                        String bahan = response.body().getData().get(i).getBahan();
                        String langkahPembuatan = response.body().getData().get(i).getLangkah_pembuatan();
                        String foto = response.body().getData().get(i).getFoto();
                        recipe.add(new Recipe(id, namaResep, deskripsi, bahan, langkahPembuatan, foto));
                        adapter.notifyDataSetChanged();
                    }
                    Snackbar snackbar = Snackbar.make(mRecipeLayout, "Data ditambah", Snackbar.LENGTH_SHORT);
                    snackbar.show();
                    btnLoadMore.setActivated(true);
                }
                else if(response.errorBody()!=null){
                    progressDialog.dismiss();
                    Snackbar snackbar = Snackbar.make(mRecipeLayout, "load data gagal pada page " +page, Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            }

            @Override
            public void onFailure(Call<Envelope<List<Recipe>>> call, Throwable t) {
                progressDialog.dismiss();
                Snackbar snackbar = Snackbar.make(mRecipeLayout, "Gagal koneksi", Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        });
    }
    public void handleUpload(View view) {
        Intent i = new Intent(this, UploadActivity.class);
        startActivity(i);
    }
}
