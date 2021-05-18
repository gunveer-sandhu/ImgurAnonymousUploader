package gunveer.codes.imgbb_uploader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "here";
    Handler handler = new Handler();
    EditText etImageLink2, etDeleteHash;
    Button btnSelect, btnUpload, btnOpen, btnDelete;
    ImageView imageView;
    TextView textView;
    public static Uri uri = null;
    public static String deleteHash = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etImageLink2 = findViewById(R.id.etImageLink2);
        etDeleteHash = findViewById(R.id.etDeleteHash);
        btnSelect = findViewById(R.id.btnSelect);
        btnUpload = findViewById(R.id.btnUpload);
        btnOpen = findViewById(R.id.btnOpen);
        btnDelete = findViewById(R.id.btnDelete);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityForResult(intent, 1);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String encImage = encodeImage();
               if(encImage!=null){
                   imgurUpload(encImage);
               }
            }
        });

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etImageLink2.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Cannot open a blank link", Toast.LENGTH_SHORT).show();
                }else{
                    Uri uri = Uri.parse(etImageLink2.getText().toString());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etDeleteHash.getText().toString().isEmpty() && deleteHash == null){
                    Toast.makeText(MainActivity.this, "Cannot Delete an Empty Hash", Toast.LENGTH_SHORT).show();
                }else if(!etDeleteHash.getText().toString().isEmpty()){
                    deleteImage(etDeleteHash.getText().toString());
                }else{
                    Toast.makeText(MainActivity.this, "Put in a delete hash.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteImage(String deleteHash) {

        Thread thread =  new Thread(){
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("text/plain");
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, "{}");
                Request request = new Request.Builder()
                        .url("https://api.imgur.com/3/image/"+deleteHash)
                        .method("DELETE", body)
                        .addHeader("Authorization", "Client-ID 049abfd3cc2e2ff")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String responser = response.body().string();
                    JSONObject object = new JSONObject(responser);
                    String status = object.getString("status");
                    if(status.contains("200")){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Image Successfully Deleted.", Toast.LENGTH_SHORT).show();
                                etImageLink2.setText("");
                                etDeleteHash.setText("");
                                imageView.setImageURI(null);
                                textView.setText("");
                                uri = null;
                            }
                        });
                    }else{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Image cannot be deleted due to Error Code: "+status, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    private String encodeImage() {
        if(uri==null){
            Toast.makeText(this, "Select an Image before you upload", Toast.LENGTH_SHORT).show();
            return null;
        }else{
            String encImage = "";
            try {
                final InputStream imageStream;
                imageStream = getContentResolver().openInputStream(uri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte[] b = baos.toByteArray();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    encImage= Base64.getEncoder().encodeToString(b);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return encImage;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == 1){
            uri = data.getData();
            imageView.setImageURI(uri);
        }
    }

    private void imgurUpload(String encImage) {
//        Thread thread =  new Thread(){
//            @Override
//            public void run() {
//                Log.d(TAG, "run: Running");
//                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
//                String url = "https://api.imgur.com/3/image";
//                JsonArrayRequest request = new JsonArrayRequest(Request.Method.POST,
//                        url, null, new Response.Listener<org.json.JSONArray>() {
//                    @Override
//                    public void onResponse(JSONArray response) {
//                        try {
//                            JSONObject object = response.getJSONObject(0);
//                            String link = object.getString("link");
//                            Log.d(TAG, "onResponse: Link is  "+ link);
////                            Toast.makeText(MainActivity.this, link, Toast.LENGTH_SHORT).show();
//                            etImageLink2.setText(link);
//                        } catch (JSONException e) {
//                            Log.d(TAG, "onResponse: Some Error in on Response");
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        Log.d(TAG, "onErrorResponse: " + error.toString());
//                    }
//                }) {
//                    @Override
//                    protected Map<String, String> getParams(){
//                        Map<String, String> params = new HashMap<String, String>();
//                        params.put("image", encImage);
//                        Log.d(TAG, "getParams:");
//                        return params;
//                    }
//
//                    @Override
//                    public Map<String, String> getHeaders() throws AuthFailureError {
//                        Map<String, String> headers = new HashMap<String, String>();
//                        headers.put("Authorization", "Client-ID {{94d855dd7a7b681}}");
//                        Log.d(TAG, "getHeaders: ");
//                        return headers;
//                    }
//                };
//                request.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//                queue.add(request);
//            }
//        };
//        thread.run();
        Thread thread =new Thread(){
            @Override
            public void run() {
                Log.d(TAG, "run: Running");
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("image", encImage)
                        .build();
                Request request = new Request.Builder()
                        .url("https://api.imgur.com/3/image")
                        .method("POST", body)
                        .addHeader("Authorization", "Client-ID 049abfd3cc2e2ff")
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String responser = response.body().string();
                    JSONObject object = new JSONObject(responser);
                    JSONObject jsonObject = object.getJSONObject("data");
                    String link = jsonObject.getString("link");
                    deleteHash = jsonObject.getString("deletehash");
                    Log.d(TAG, "onResponse: response is  "+ object);
                    String status = object.getString("status");
                    Log.d(TAG, "run: "+ status);
                    if(status.contains("200")){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Image Successfully uploaded. ", Toast.LENGTH_SHORT).show();
                                etImageLink2.setText(link);
                                etDeleteHash.setText(deleteHash);
                                textView.setText("Save the above delete Hash for deletion purposes.");
                            }
                        });
                    }else{
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Image cannot be uploaded due to Error Code: "+ status, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (IOException | JSONException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Due to Some reason Image cannot be uploaded.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.d(TAG, "imgurUpload: catching" + e);
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }
}