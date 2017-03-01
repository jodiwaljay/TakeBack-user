package com.elegance.feedback;

import android.Manifest;
import android.app.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import android.widget.Toast;

import com.elegance.feedback.Sql_offline_form.DatabaseController;
import com.elegance.feedback.Sql_offline_form.PairDataType;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by root on 2/4/16.
 */

public class Form_Creation extends AppCompatActivity {

    public static ArrayList<String> formEditMode;
    public static ArrayList<String> Questions_Loaded;
    public static ArrayList<String> User_Details;
    public static ArrayList<String> Comments;
    public static ArrayList<String> companyLoaded;
    public static Activity form_create;
    public ImageView imgLogo;

    private static int RESULT_LOAD_IMG = 1;

    public static ArrayList<String> companyDetails;
    public static ArrayList<String> form_unique;


    LinearLayout layout_form;


    private String url_submit_feedback;
    private JSONParser jsonParser;
    private ProgressDialog pDialog;
    private static final String TAG_SUCCESS = "success";
    public String uniqueId = "";
    public String pin = "";
    Cursor c_q, c_i, c_p;

    public ArrayList<String> quora;
    public ArrayList<String> comments;
    public ArrayList<String> personal;

    Upload upload;

    DatabaseController db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_creating);

        url_submit_feedback = getResources().getString(R.string.submit_url);

        formEditMode = new ArrayList<String>();
        companyDetails = new ArrayList<String>();
        form_unique = new ArrayList<String>(1);
        Questions_Loaded = new ArrayList<String>();
        User_Details = new ArrayList<String>();
        Comments = new ArrayList<String>();
        companyLoaded = new ArrayList<String>();

        form_create = this;

        layout_form = (LinearLayout) findViewById(R.id.layout_form);
        imgLogo = (ImageView) findViewById(R.id.imgLogo);
        final EditText edPin = (EditText) findViewById(R.id.edPin);
        final Button bLoadForm = (Button) findViewById(R.id.bLoadForm);
        sql_logo sl = new sql_logo(Form_Creation.this);
        try {
            sl.open();
            sl.checkDefaultImage(getApplicationContext());
            imgLogo.setImageBitmap(sl.getData());
            sl.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }




        bLoadForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Load_Form.class);
                if (edPin.getText().toString().equals("") || edPin.getText().toString().length() < 4 || edPin.getText().toString().length() > 5) {
                    Toast.makeText(getApplicationContext(), "Enter proper credentials",
                            Toast.LENGTH_SHORT).show();
                } else {
                    i.putExtra("pin", edPin.getText().toString());

                    startActivity(i);
                    edPin.setText("");
                }
            }
        });

    }


    private boolean shouldAskPermission(){

        return(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadImagefromGallery();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Data", "" + formEditMode.size());
    }

    public void loadImagefromGallery() {

        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(
                Intent.createChooser(intent, "Select File"),
                RESULT_LOAD_IMG);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        /*try {

            if (resultCode == RESULT_OK) {
                if (requestCode == RESULT_LOAD_IMG) {
                    Uri selectedImageUri = data.getData();
                    String[] projection = {MediaStore.MediaColumns.DATA};
                    CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                            null);
                    Cursor cursor = cursorLoader.loadInBackground();
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor.moveToFirst();
                    String selectedImagePath = cursor.getString(column_index);
                    Bitmap bm;
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(selectedImagePath, options);
                    final int REQUIRED_SIZE = 200;
                    int scale = 1;
                    while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                            && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                        scale *= 2;
                    options.inSampleSize = scale;
                    options.inJustDecodeBounds = false;
                    bm = BitmapFactory.decodeFile(selectedImagePath, options);
                    sql_logo sl = new sql_logo(Form_Creation.this);
                    sl.open();
                    sl.checkDefaultImage(getApplicationContext());

                    Log.d("Entry", "" + sl.createEntry(bm));
                    imgLogo.setImageBitmap(sl.getData());
                    sl.close();
                }
            }else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch ( SQLException er) {
            er.printStackTrace();
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }*/

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMG)
                onSelectFromGalleryResult(data);
        }




    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Uri selectedImageUri = data.getData();
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = managedQuery(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);

        Bitmap bm;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 300;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(selectedImagePath, options);

        imgLogo.setImageBitmap(bm);
        sql_logo sl = new sql_logo(Form_Creation.this);
        try {
            sl.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sl.checkDefaultImage(getApplicationContext());

        Log.d("Entry", "" + sl.createEntry(bm));
        imgLogo.setImageBitmap(sl.getData());
        sl.close();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.change_logo) {

            if(shouldAskPermission()) {

                Log.d("Perms", "should ASk true");
                String[] perms = {"android.permission. WRITE_EXTERNAL_STORAGE"};
            /*if(hasPermission(perms[0])){
                loadImagefromGallery();

            }*/
                int permsRequestCode = 200;

                if (ContextCompat.checkSelfPermission(Form_Creation.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    Log.d("Perms", "ASk true");

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(Form_Creation.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        ActivityCompat.requestPermissions(Form_Creation.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                permsRequestCode);
                        Log.d("Perms", "true");
                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(Form_Creation.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                permsRequestCode);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.

                    }

                }
                else {
                    loadImagefromGallery();
                }


            }

            else {
                loadImagefromGallery();
            }

            return true;
        }



        /*********************************/
        /* Options :: Getting Offline Form */
        if(id == R.id.loaded_form){



            /*********************************/
            /* Opening Database */
            Form_Creation.companyLoaded = new ArrayList<String>();
            SQLiteDatabase database = openOrCreateDatabase("offline_form", Context.MODE_PRIVATE,null);




            /*********************************/
            /* Getting All tables initialized */
            DatabaseController db = new DatabaseController(database);

            PairDataType name = new PairDataType("NAME",2);
            PairDataType address = new PairDataType("ADDRESS",2);
            PairDataType phone = new PairDataType("PHONE",2);
            PairDataType fax = new PairDataType("FAX",2);
            PairDataType email = new PairDataType("EMAIL",2);
            PairDataType description = new PairDataType("DESCRIPTION",2);
            PairDataType logo = new PairDataType("LOGO",2);
            db.createTable("company_loaded",name,address,phone,fax,email,description,logo);



            PairDataType one = new PairDataType("ONE",2);
            PairDataType two = new PairDataType("TWO",2);
            PairDataType three = new PairDataType("THREE",2);
            PairDataType four = new PairDataType("FOUR",2);
            PairDataType five = new PairDataType("FIVE",2);
            PairDataType six = new PairDataType("SIX",2);
            PairDataType seven = new PairDataType("SEVEN",2);
            PairDataType eight = new PairDataType("EIGHT",2);
            PairDataType nine = new PairDataType("NINE",2);
            PairDataType ten = new PairDataType("TEN",2);
            db.createTable("form_loaded",one,two,three,four,five,six,seven,eight,nine,ten);



            PairDataType pin_got = new PairDataType("PINT",2);
            PairDataType unique_got = new PairDataType("UNIQUEID",2);
            db.createTable("form_identifiers",pin_got,unique_got);



            /*********************************/
            /* Getting all components of the form */
            Cursor f_i, c_l, f_l;
            f_i= db.getDatabase().rawQuery("SELECT * FROM form_identifiers",null);
            c_l= db.getDatabase().rawQuery("SELECT * FROM company_loaded",null);
            f_l = db.getDatabase().rawQuery("SELECT * FROM form_loaded", null);

            if(f_i.moveToFirst() && f_l.moveToFirst() && c_l.moveToFirst()) {

                // Form Identifiers
                Load_Form.PIN_got = f_i.getString(0);
                Load_Form.UNIQUEID_got = f_i.getString(1);


                // Company parameters
                for (int i = 0; i < 7; i++) {
                    Form_Creation.companyLoaded.add(c_l.getString(i));
                }

                // Form Questions and types
                for (int i = 0; i < 7; i++) {
                    Form_Creation.formEditMode.add(f_l.getString(i));
                }

                /*********************************/
                /* Closing cursors */
                f_i.close();
                c_l.close();
                f_l.close();


                /*********************************/
                /* Opening Saved Form */
                Intent i = new Intent(getApplicationContext(), Load_Form.class);

                i.putExtra("pin", "DEFAULT");

                startActivity(i);

            }
            else{
                Toast.makeText(getApplicationContext(), "No Saved Form",
                        Toast.LENGTH_SHORT).show();
            }


        }

        if(id == R.id.update){
            jsonParser = new JSONParser();
            pDialog = new ProgressDialog(Form_Creation.this);
            pDialog.setMessage("Submitting..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();



            SQLiteDatabase database = openOrCreateDatabase("feedbacks_submitted", Context.MODE_PRIVATE,null);

            db = new DatabaseController(database);

            initializeTheShit();

            c_i = db.getDatabase().rawQuery("SELECT * FROM identifiers",null);
            c_p = db.getDatabase().rawQuery("SELECT * FROM personal_comments",null);
            c_q = db.getDatabase().rawQuery("SELECT * FROM quora",null);

            if(c_i.moveToFirst() && c_p.moveToFirst() && c_q.moveToFirst()){
                pin = c_i.getString(0);
                uniqueId = c_i.getString(1);




                comments.add(c_p.getString(0));
                for(int i =1; i<5; i++){
                    personal.add(c_p.getString(i));
                }



                for(int i =0; i<10; i++){
                    if(c_q.getString(i).equals("NOT_ANSWERED")){
                        break;
                    }
                    else{
                        quora.add(c_q.getString(i));
                    }
                }


                Upload upload = new Upload();
                upload.execute();
            }
            else {
                Toast.makeText(Form_Creation.this,"No Records Found",Toast.LENGTH_SHORT).show();
                pDialog.dismiss();

            }




        }

        return super.onOptionsItemSelected(item);
    }

    public void clearTheShit(){
        quora.clear();
        personal.clear();
        comments.clear();

    }
    public void initializeTheShit(){
        quora = new ArrayList<String>();
        personal = new ArrayList<String>();
        comments = new ArrayList<String>();


        PairDataType uniqueId = new PairDataType("uniqueid",2);
        PairDataType pin = new PairDataType("pin",2);
        db.createTable("identifiers", uniqueId, pin);


        PairDataType comments = new PairDataType("comments",2);
        PairDataType username = new PairDataType("username",2);
        PairDataType userEmail = new PairDataType("useremail",2);
        PairDataType userPhone = new PairDataType("userphone",2);
        PairDataType userAddress = new PairDataType("useraddress",2);
        db.createTable("personal_comments", comments,username, userEmail, userPhone, userAddress);


        PairDataType one = new PairDataType("one",2);
        PairDataType two = new PairDataType("two",2);
        PairDataType three = new PairDataType("three",2);
        PairDataType four = new PairDataType("four",2);
        PairDataType five = new PairDataType("five",2);
        PairDataType six = new PairDataType("six",2);
        PairDataType seven = new PairDataType("seven",2);
        PairDataType eight = new PairDataType("eight",2);
        PairDataType nine = new PairDataType("nine",2);
        PairDataType ten = new PairDataType("ten",2);
        db.createTable("quora", one, two, three, four, five, six, seven, eight, nine, ten);
    }

    class Upload extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected String doInBackground(ArrayList<String>... args) {


            List<NameValuePair> params = new ArrayList<NameValuePair>();




            params.add(new BasicNameValuePair("UNIQUEID",""+uniqueId));
            params.add(new BasicNameValuePair("PIN", "" + pin));


            int qp;
            for(qp=0; qp< quora.size(); qp++){
                StringTokenizer stringTokenizer = new StringTokenizer(quora.get(qp),";");

                params.add(new BasicNameValuePair(getResources().getStringArray(R.array.numbers)[qp],stringTokenizer.nextToken()));
                Log.d("Added", "QSomething");
                String temp = "";
                while(stringTokenizer.hasMoreTokens()){
                    temp = temp + stringTokenizer.nextToken() + " ";
                }
                params.add(new BasicNameValuePair(getResources().getStringArray(R.array.numbers_ans)[qp],temp));
                Log.d("Added",temp);
            }

            for(; qp<10; qp++ ){
                params.add(new BasicNameValuePair(getResources().getStringArray(R.array.numbers)[qp],"DEFAULT"));
                params.add(new BasicNameValuePair(getResources().getStringArray(R.array.numbers_ans)[qp],"DEFAULT"));
            }
            for(int i=0; i<personal.size(); i++){
                params.add (new BasicNameValuePair(getResources().getStringArray(R.array.userParams)[i],personal.get(i)));
            }
            for(int i=0; i<comments.size(); i++){

                Log.e("Error","I am in comments");
                params.add (new BasicNameValuePair("COMMENTS",comments.get(i)));
            }


            try {

                JSONObject json = jsonParser.makeHttpRequest(url_submit_feedback, "POST", params);
                if (json==null){
                    return "JSONNull";
                }
                Log.d("Create Response", json.toString());
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created product
                    Log.d("Cabme","Booking Done");


                } else {
                    // failed to create product

                    Log.e("Cabme", "Booking Failed");
                    return "JSONNull";
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return "JSONNotNull";
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        protected void onPostExecute(String file_url) {

            if(file_url.equals("JSONNull")){
                Log.d("cabme", "Json is null");
                Toast.makeText(getApplicationContext(), "No Internet Connection or server issue",
                        Toast.LENGTH_SHORT).show();
                if(pDialog != null && pDialog.isShowing()){
                    pDialog.dismiss();
                }

            }

            if (file_url.equals("JSONNotNull")){

                clearTheShit();

                if(c_i.moveToNext() && c_p.moveToNext() && c_q.moveToNext()){
                    pin = c_i.getString(0);
                    uniqueId = c_i.getString(1);

                    comments.add(c_p.getString(0));

                    for(int i =1; i<5; i++){
                        personal.add(c_p.getString(i));
                    }


                    for (int i = 0; i < 10; i++) {
                        if (c_q.getString(i).equals("NOT_ANSWERED")) {
                            break;
                        } else {
                            quora.add(c_q.getString(i));
                        }
                    }

                    upload = new Upload();
                    upload.execute();
                }
                else {
                    Toast.makeText(Form_Creation.this, "Task completed successfully", Toast.LENGTH_SHORT).show();
                    if(pDialog != null && pDialog.isShowing()){
                        pDialog.dismiss();
                    }

                    db.getDatabase().delete("identifiers","1",null);
                    db.getDatabase().delete("quora","1",null);
                    db.getDatabase().delete("personal_comments","1",null);

                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(pDialog != null && pDialog.isShowing()){
            pDialog.dismiss();
        }
    }
}




