package com.elegance.feedback;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.elegance.feedback.Sql_offline_form.DatabaseController;
import com.elegance.feedback.Sql_offline_form.PairDataType;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by jodiwaljay on 24/7/16.
 */
public class upload_feedbacks extends AppCompatActivity {

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.upload_feedback);
        url_submit_feedback = getResources().getString(R.string.submit_url);

        jsonParser = new JSONParser();
        pDialog = new ProgressDialog(upload_feedbacks.this);
        pDialog.setMessage("Submitting..");
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();



        SQLiteDatabase database = openOrCreateDatabase("feedbacks_submitted", Context.MODE_PRIVATE,null);

        db = new DatabaseController(database);

        initializeTheShit();

        c_i = db.getDatabase().rawQuery("SELECT * FROM identifiers",null);
        if(c_i.moveToFirst()){
            pin = c_i.getString(0);
            uniqueId = c_i.getString(1);
        }
        else {
            Toast.makeText(upload_feedbacks.this,"No Records Found",Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
            finish();
        }




        c_p = db.getDatabase().rawQuery("SELECT * FROM personal_comments",null);
        if(c_p.moveToFirst()){
            comments.add(c_p.getString(0));

            for(int i =1; i<5; i++){
                personal.add(c_p.getString(i));
            }
        }

        else {
            Toast.makeText(upload_feedbacks.this,"No Records Found",Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
            finish();
        }


        c_q = db.getDatabase().rawQuery("SELECT * FROM quora",null);
        if(c_q.moveToFirst()){


            for(int i =0; i<10; i++){
                if(c_q.getString(i).equals("NOT_ANSWERED")){
                    break;
                }
                else{
                    quora.add(c_q.getString(i));
                }
            }
        }

        else {
            Toast.makeText(upload_feedbacks.this,"No Records Found",Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
            finish();
        }

        Upload upload = new Upload();
        upload.execute();




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
                    //Toast.makeText(upload_feedbacks.this, "Task completed successfully", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                    Log.d("Everything done", "yes");
                    SQLiteDatabase database = openOrCreateDatabase("feedbacks_submitted", Context.MODE_PRIVATE,null);

                    db = new DatabaseController(database);

                    db.getDatabase().execSQL("DROP TABLE IF EXISTS identifiers");
                    db.getDatabase().execSQL("DROP TABLE IF EXISTS personal_comments");
                    db.getDatabase().execSQL("DROP TABLE IF EXISTS quora");

                }


            }


            finish();


        }
    }
}


