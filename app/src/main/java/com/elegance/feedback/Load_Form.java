package com.elegance.feedback;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.elegance.feedback.Sql_offline_form.DatabaseController;
import com.elegance.feedback.Sql_offline_form.PairDataType;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class Load_Form extends AppCompatActivity {
    JSONParser jParser = new JSONParser();
    private static String url_all_products;
    private static String url_submit_feedback;

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_PRODUCTS = "admin_forms";
    public static String UNIQUEID_got;
    ImageView actionLogo;
    public static String PIN_got;
    final int NUM_PAGES = Form_Creation.formEditMode.size();
    JSONArray products = null;
    Button bSubmitFeedback;
    private ProgressDialog pDialog;
    private JSONParser jsonParser = new JSONParser();
    EditText etComments, etUserName, etUserEmail, etUserAddress, etUserPhone;
    TextView tvCompanyName, tvCompanyDesc, tvCompanyPhone, tvCompanyEmail, tvCompanyFax, tvCompanyAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(null);

        setContentView(R.layout.preview_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        url_all_products = getResources().getString(R.string.get_url);
        url_submit_feedback = getResources().getString(R.string.submit_url);

        pDialog = new ProgressDialog(Load_Form.this);

        //getSupportActionBar().setLogo(R.drawable.icon_feedback);
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //toolbar.setTitle("Help us do better");

        bSubmitFeedback = (Button)findViewById(R.id.bSubmitFeedback);
        etComments = (EditText)findViewById(R.id.etComments);
        etUserName = (EditText)findViewById(R.id.etUserName);
        etUserPhone = (EditText)findViewById(R.id.etUserPhone);
        etUserEmail = (EditText)findViewById(R.id.etUserEmail);
        etUserAddress = (EditText)findViewById(R.id.etUserAddress);

        tvCompanyName = (TextView)findViewById(R.id.tvCompanyName);
        tvCompanyDesc = (TextView)findViewById(R.id.tvCompanyDesc);
        tvCompanyPhone = (TextView)findViewById(R.id.tvPhone);
        tvCompanyEmail = (TextView)findViewById(R.id.tvCompanyEmail);
        tvCompanyFax = (TextView)findViewById(R.id.tvFax);
        tvCompanyAdd = (TextView)findViewById(R.id.tvCompanyAdd);
        actionLogo = (ImageView)findViewById(R.id.actionLogo);

        sql_logo sl = new sql_logo(Load_Form.this);
        try {

            sl.open();
            sl.checkDefaultImage(getApplicationContext());
            actionLogo.setImageBitmap(sl.getData());
            Log.d("asd",sl.getData().toString());
            sl.close();
        }
        catch (Exception e ){
            Log.e("Error", e.toString());
        }

        Intent intent = getIntent();
        String pin_req = intent.getStringExtra("pin");
        if(!pin_req.equals("DEFAULT")){
            PIN_got = pin_req;
            new LoadAllProducts().execute(pin_req);
        }

        else new LoadAllProducts().onPostExecute("NULL");

        Log.d("Num Pages", "" + NUM_PAGES);
        bSubmitFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Form_Creation.Comments = new ArrayList<String>();
                Form_Creation.User_Details = new ArrayList<String>();


                Form_Creation.Comments.add(etComments.getText().toString());

                if(!etUserName.getText().toString().equals("")){
                    Form_Creation.User_Details.add(etUserName.getText().toString());
                }
                else {
                    Form_Creation.User_Details.add("Not Entered");
                }
                if(!etUserEmail.getText().toString().equals("")){
                    Form_Creation.User_Details.add(etUserEmail.getText().toString());
                }
                else {
                    Form_Creation.User_Details.add("Not Entered");
                }

                if(!etUserPhone.getText().toString().equals("")){
                    Form_Creation.User_Details.add(etUserPhone.getText().toString());
                }
                else {
                    Form_Creation.User_Details.add("Not Entered");
                }

                if(!etUserAddress.getText().toString().equals("")){
                    Form_Creation.User_Details.add(etUserAddress.getText().toString());
                }
                else {
                    Form_Creation.User_Details.add("Not Entered");
                }


                SQLiteDatabase database = openOrCreateDatabase("feedbacks_submitted", Context.MODE_PRIVATE, null);

                DatabaseController db = new DatabaseController(database);

                PairDataType comments = new PairDataType("comments",2);
                PairDataType username = new PairDataType("username",2);
                PairDataType userEmail = new PairDataType("useremail",2);
                PairDataType userPhone = new PairDataType("userphone",2);
                PairDataType userAddress = new PairDataType("useraddress",2);

                db.createTable("personal_comments", comments,username, userEmail, userPhone, userAddress);
                db.createEntry("personal_comments", Form_Creation.Comments.get(0), Form_Creation.User_Details.get(0), Form_Creation.User_Details.get(1),
                        Form_Creation.User_Details.get(2), Form_Creation.User_Details.get(3));

                Log.d("personal_arraylist",Form_Creation.Comments.toString()+Form_Creation.User_Details.toString());

                Cursor c = db.getDatabase().rawQuery("SELECT * FROM personal_comments",null);
                c.moveToLast();
                for(int i = 0; i<5;i++){
                    Log.d("Personal_database",c.getString(i));
                }
                c.close();


                PairDataType uniqueId = new PairDataType("uniqueid",2);
                PairDataType pin = new PairDataType("pin",2);
                db.createTable("identifiers", uniqueId, pin);
                db.createEntry("identifiers", UNIQUEID_got, PIN_got);

                Log.d("identifiers_arraylist",UNIQUEID_got+PIN_got);

                c = db.getDatabase().rawQuery("SELECT * FROM identifiers",null);
                c.moveToLast();
                for(int i = 0; i<2;i++){
                    Log.d("Database_identifiers",c.getString(i));
                }
                c.close();


                int j = Form_Creation.Questions_Loaded.size();
                for(int i=j; i<10; i++){
                    Form_Creation.Questions_Loaded.add("NOT_ANSWERED");
                }

                Log.d("answers_in_arraylist",Form_Creation.Questions_Loaded.toString());

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
                db.createEntry("quora", Form_Creation.Questions_Loaded.get(0), Form_Creation.Questions_Loaded.get(1),
                        Form_Creation.Questions_Loaded.get(2), Form_Creation.Questions_Loaded.get(3), Form_Creation.Questions_Loaded.get(4),
                        Form_Creation.Questions_Loaded.get(5), Form_Creation.Questions_Loaded.get(6), Form_Creation.Questions_Loaded.get(7),
                        Form_Creation.Questions_Loaded.get(8), Form_Creation.Questions_Loaded.get(9));

                c = db.getDatabase().rawQuery("SELECT * FROM quora",null);
                c.moveToLast();
                for(int i = 0; i<10;i++){
                    Log.d("Database_quora",c.getString(i));
                }

                c.close();

                Toast.makeText(getApplicationContext(), "Success",
                        Toast.LENGTH_SHORT).show();

                Intent i = new Intent(getApplicationContext(), Take_another.class);

                startActivity(i);
                finish();

                //new CreateNewProduct().execute();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Form_Creation.Questions_Loaded.clear();
        Form_Creation.companyLoaded.clear();
        Form_Creation.User_Details.clear();
    }

    public void setCompanyDetails(){
        if(Form_Creation.companyLoaded.size()>0){
        if(Form_Creation.companyLoaded.get(0).equals("DEFAULT")){
            tvCompanyName.setVisibility(View.GONE);
        }
        else{
            tvCompanyName.setText(Form_Creation.companyLoaded.get(0));
        }

        if(Form_Creation.companyLoaded.get(1).equals("DEFAULT")){
            tvCompanyAdd.setVisibility(View.GONE);
        }
        else{
            tvCompanyAdd.setText(Form_Creation.companyLoaded.get(1));
        }

        if(Form_Creation.companyLoaded.get(2).equals("DEFAULT")){
            tvCompanyPhone.setVisibility(View.GONE);
        }
        else{
            tvCompanyPhone.setText(Form_Creation.companyLoaded.get(2));
        }

        if(Form_Creation.companyLoaded.get(3).equals("DEFAULT")){
            tvCompanyFax.setVisibility(View.GONE);
        }
        else{
            tvCompanyFax.setText(Form_Creation.companyLoaded.get(3));
        }

        if(Form_Creation.companyLoaded.get(4).equals("DEFAULT")){
            tvCompanyEmail.setVisibility(View.GONE);
        }
        else{
            tvCompanyEmail.setText(Form_Creation.companyLoaded.get(4));
        }

        if(Form_Creation.companyLoaded.get(5).equals("DEFAULT")){
            tvCompanyDesc.setVisibility(View.GONE);
        }
        else{
            tvCompanyDesc.setText(Form_Creation.companyLoaded.get(5));
        }
        }

    }

    class LoadAllProducts extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Load_Form.this);
            pDialog.setMessage("Loading Form..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();

        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();

            // getting JSON string from URL

            try {
                JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);
                // Checking for SUCCESS TAG
                //Log.d("All Products: ", json.toString());
                int success = json.getInt(TAG_SUCCESS);
                Log.d("JSON Received", json.toString());

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    products = json.getJSONArray(TAG_PRODUCTS);

                    // looping through All Products
                    int j;
                    int uniq_id;
                    for ( j = 0; j < products.length(); j++) {
                        JSONObject c = products.getJSONObject(j);
                        boolean flag = false;
                        // Storing each json item in variable
                        String pins = c.getString("PIN");
                        StringTokenizer stringTokenizer = new StringTokenizer(pins,";");
                        while(stringTokenizer.hasMoreTokens()){
                            if(stringTokenizer.nextToken().equals(args[0])){
                                flag = true;
                                break;
                            }
                        }
                        if(flag){
                            break;
                        }
                    }

                    if(j==products.length()){
                        Log.d("Pin","PIN doesn't exist");
                        return "JSONNull";
                    }
                    Log.d("pin_got",""+j);
                    JSONObject form_required = products.getJSONObject(j);

                    UNIQUEID_got = ""+form_required.getInt("UNIQUEID");

                    Form_Creation.formEditMode = new ArrayList<String >();
                    for(int k = 0; k<getResources().getStringArray(R.array.numbers).length; k++){
                        Form_Creation.formEditMode.add(form_required.getString(getResources().getStringArray(R.array.numbers)[k]));
                    }
                    Log.d("arrayList", Form_Creation.formEditMode.toString());
                    Form_Creation.companyLoaded = new ArrayList<String>();

                    for(int k = 0; k<getResources().getStringArray(R.array.companyParams).length; k++){
                        Form_Creation.companyLoaded.add(form_required.getString(getResources().getStringArray(R.array.companyParams)[k]));
                    }

                    SQLiteDatabase database = openOrCreateDatabase("offline_form", Context.MODE_PRIVATE,null);
                    DatabaseController db = new DatabaseController(database);
                    PairDataType name = new PairDataType("NAME",2);
                    PairDataType address = new PairDataType("ADDRESS",2);
                    PairDataType phone = new PairDataType("PHONE",2);
                    PairDataType fax = new PairDataType("FAX",2);
                    PairDataType email = new PairDataType("EMAIL",2);
                    PairDataType description = new PairDataType("DESCRIPTION",2);
                    PairDataType logo = new PairDataType("LOGO",2);


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


                    PairDataType pin_got = new PairDataType("PINT",2);
                    PairDataType unique_got = new PairDataType("UNIQUEID",2);


                    db.createTable("company_loaded",name,address,phone,fax,email,description,logo);
                    db.getDatabase().delete("company_loaded", null, null);
                    db.createEntry("company_loaded",Form_Creation.companyLoaded.get(0),Form_Creation.companyLoaded.get(1),Form_Creation.companyLoaded.get(2),
                            Form_Creation.companyLoaded.get(3),Form_Creation.companyLoaded.get(4),Form_Creation.companyLoaded.get(5),
                            Form_Creation.companyLoaded.get(6));



                    db.createTable("form_loaded",one,two,three,four,five,six,seven,eight,nine,ten);
                    db.getDatabase().delete("form_loaded", null, null);
                    db.createEntry("form_loaded",Form_Creation.formEditMode.get(0),Form_Creation.formEditMode.get(1),Form_Creation.formEditMode.get(2),
                            Form_Creation.formEditMode.get(3),Form_Creation.formEditMode.get(4),Form_Creation.formEditMode.get(5),
                            Form_Creation.formEditMode.get(6),Form_Creation.formEditMode.get(7),Form_Creation.formEditMode.get(8),
                            Form_Creation.formEditMode.get(9));



                    db.createTable("form_identifiers",pin_got,unique_got);
                    db.getDatabase().delete("form_identifiers", null, null);
                    db.createEntry("form_identifiers",PIN_got, UNIQUEID_got);



                    Cursor c = db.getDatabase().rawQuery("SELECT * FROM company_loaded", null);
                    c.moveToFirst();

                    for(int i = 0; i < 7; i++){
                        Log.d("Company Saved",c.getString(i));
                    }

                    c = db.getDatabase().rawQuery("SELECT * FROM form_loaded" , null);
                    c.moveToFirst();

                    for(int i = 0; i < 10; i++){
                        Log.d("Form Saved",c.getString(i));
                    }

                    c.close();

                    /******************************************/



                    Log.d("arrayList", Form_Creation.companyLoaded.toString());

                } else {
                    // no products found
                    // Launch Add New product Activity
                    Log.d("Form_loading", "No form exists");
                }
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
                return "JSONNull";
                //finish();
                //Toast.makeText(getApplicationContext(), "No Internet Connection or server issue",
                //      Toast.LENGTH_LONG).show();
            }
            Log.e("Error", "Can you see me");
            return "JSONNOtNull";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.equals("JSONNull")){
                Toast.makeText(getApplicationContext(), "Error connecting to server",
                        Toast.LENGTH_SHORT).show();
                pDialog.dismiss();
                finish();
            }




            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            for (int i = 0; i < Form_Creation.formEditMode.size(); i++) {
                String question = Form_Creation.formEditMode.get(i);
                Log.d("Error",question);
                String type = question.substring(0, 2);
                Bundle temp = new Bundle();
                temp.putString("Question", question);
                if(chooseFragment(type,temp)==null){
                    break;
                }
                fragmentTransaction.add(R.id.fragment_container, chooseFragment(type, temp));
            }


            fragmentTransaction.commit();
            setCompanyDetails();
            bSubmitFeedback.setVisibility(View.VISIBLE);
            pDialog.dismiss();


        }


    }
    class CreateNewProduct extends AsyncTask<ArrayList<String>, String, String> {

        @Override
        protected String doInBackground(ArrayList<String>... args) {


            List<NameValuePair> params = new ArrayList<NameValuePair>();


            params.add(new BasicNameValuePair("UNIQUEID",""+UNIQUEID_got));
            params.add(new BasicNameValuePair("PIN", "" + PIN_got));
            //Log.d("formQs",formEditMode.toString());
            int qp;
            for(qp=0; qp< Form_Creation.Questions_Loaded.size(); qp++){
                StringTokenizer stringTokenizer = new StringTokenizer(Form_Creation.Questions_Loaded.get(qp),";");

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
            for(int i=0; i<Form_Creation.User_Details.size(); i++){
                params.add (new BasicNameValuePair(getResources().getStringArray(R.array.userParams)[i],Form_Creation.User_Details.get(i)));
            }
            for(int i=0; i<Form_Creation.Comments.size(); i++){
                /*String temp = formEditMode.get(i).replaceAll("'","\\'");
                Log.d("Error",temp);*/
                //String temp_new  = temp.replaceAll("&","\&");
                Log.e("Error","I am in comments");
                params.add (new BasicNameValuePair("COMMENTS",Form_Creation.Comments.get(i)));
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
            pDialog = new ProgressDialog(Load_Form.this);
            pDialog.setMessage("Submitting..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }
        protected void onPostExecute(String file_url) {

            if(file_url.equals("JSONNull")){
                Log.d("cabme", "Json is null");
                Toast.makeText(getApplicationContext(), "No Internet Connection or server issue",
                        Toast.LENGTH_SHORT).show();

            }

            if (file_url.equals("JSONNotNull")){
                Form_Creation.Questions_Loaded.clear();

                pDialog.dismiss();
                Toast.makeText(getApplicationContext(), "Success",
                        Toast.LENGTH_SHORT).show();

                Intent i = new Intent(getApplicationContext(), Take_another.class);

                startActivity(i);
                finish();
            }

        }
    }
    private Fragment chooseFragment(String type, Bundle temp) {


        if(type.equals("RA")){
            Fragment qFrag = new ratingFragment();
            qFrag.setArguments(temp);
            return qFrag;
        }
        if(type.equals("SM")){
            Fragment qFrag = new smileyFragment();
            qFrag.setArguments(temp);
            return qFrag;
        }
        if(type.equals("DI")){
            Fragment qFrag = new dichotFragment();
            qFrag.setArguments(temp);
            return qFrag;
        }
        if(type.equals("SC")){
            Fragment qFrag = new singleChoiceFragment();
            qFrag.setArguments(temp);
            return qFrag;
        }
        if(type.equals("CH")){
            Fragment qFrag = new checklistFragment();
            qFrag.setArguments(temp);
            return qFrag;
        }
        if(type.equals("DE")){
            Fragment qFrag = new descriptiveFragment();
            qFrag.setArguments(temp);
            return qFrag;
        }
        return null;
    }

}