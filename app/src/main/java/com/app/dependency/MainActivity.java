package com.app.dependency;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.dependency.ServiceApi.APIServiceFactory;
import com.app.dependency.ServiceApi.ApiService;
import com.app.dependency.model.FaceSearch;
import com.app.dependency.model.SearchHeaderr;
import com.app.detection.DetectorActivity;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import in.mayanknagwanshi.imagepicker.ImageSelectActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    ApiService apiService;

    TextView tv;

    Button result;

    ImageView capture_iv, result_iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tvvv);
        result = findViewById(R.id.button);


        capture_iv = findViewById(R.id.capture_view);
        result_iv = findViewById(R.id.result);
        apiService = APIServiceFactory.getRetrofit().create(ApiService.class);

        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                startActivity(new Intent(MainActivity.this, DetectorActivity.class));

//                openCamera();
            }
        });

    }


    private void openCamera() {
        Intent intent = new Intent(this, ImageSelectActivity.class);
        intent.putExtra(ImageSelectActivity.FLAG_COMPRESS, false);//default is true
        intent.putExtra(ImageSelectActivity.FLAG_CAMERA, true);//default is true
        intent.putExtra(ImageSelectActivity.FLAG_GALLERY, true);//default is true
        startActivityForResult(intent, 1213);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1213 && resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(ImageSelectActivity.RESULT_FILE_PATH);
            Bitmap selectedImage = BitmapFactory.decodeFile(filePath);

            Matrix matrix = new Matrix();

            matrix.postRotate(270);

            selectedImage = Bitmap.createBitmap(selectedImage, 0, 0, selectedImage.getWidth(), selectedImage.getHeight(), matrix, false);
            capture_iv.setImageBitmap(selectedImage);


            getFaceresult(getBase64String(selectedImage));
        }
    }

    private String getBase64String(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }


    public String getFaceresult(String base64String) {

        final String[] output = new String[1];
        try {

            SearchHeaderr searchHeaderr = new SearchHeaderr();
            searchHeaderr.setImage_encoded(base64String);
            searchHeaderr.setUser_id("5d0a8ef72ad9c04228140739");


            apiService.getresult(searchHeaderr).enqueue(new Callback<FaceSearch>() {
                @Override
                public void onResponse(Call<FaceSearch> call, Response<FaceSearch> response) {


                    output[0] =response.body().getMessage();
                    if (response.raw().code() == 200 && response.body().getStatus().equalsIgnoreCase("ok")) {

                        List<com.app.dependency.model.User> users = response.body().getData().getUser();
                        if (users.size() > 0) {
                            String id = null;
                            for (com.app.dependency.model.User user : users) {
                                id = user.getId();
                                Log.e("user", new Gson().toJson(user));
                                String jsonResponse = new Gson().toJson(user);
                                JSONObject jsonObject = null;
                                try {
                                    jsonObject = new JSONObject(jsonResponse);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Calendar calendar = Calendar.getInstance();
                                String image_url = APIServiceFactory.BASE_URL + user.getFileDirectory();


                                List<String> imageUrl = new ArrayList<>();

                                imageUrl.add(image_url);

                                try {
                                    result_iv.setImageBitmap(getBitmapFromURL(image_url));
                                } catch (Exception e) {

                                }


                                if (jsonObject.has("person_name")) {

                                    try {
                                        tv.setText(jsonObject.getString("person_name"));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    break;
                                }
                            }

                        } else {

                            tv.setText(response.body().getMessage());
                            Toast.makeText(MainActivity.this, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {

                        Toast.makeText(MainActivity.this, "" + response.body().getMessage(), Toast.LENGTH_SHORT).show();


                    }
                }

                @Override
                public void onFailure(Call<FaceSearch> call, Throwable t) {
                    Log.e("onFailure", t.getMessage());

                    output[0] =t.getMessage();
                }
            });
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }


        return  output[0];
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            Log.e("src", src);
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            Log.e("Bitmap", "returned");
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", e.getMessage());
            return null;
        }
    }

}
