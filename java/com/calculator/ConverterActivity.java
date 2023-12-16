package com.calculator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ConverterActivity extends AppCompatActivity implements View.OnClickListener {

    TextView calculationTV, resultTV;
    Spinner dropdown1, dropdown2;
    MaterialButton buttonCalculator, buttonD, buttonC, buttonDecimalPoint;
    MaterialButton button0, button1, button2, button3, button4, button5, button6, button7, button8, button9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);

        // get spinners from xml
        dropdown1 = findViewById(R.id.spinner1);
        dropdown2 = findViewById(R.id.spinner2);

        // get TextViews from xml
        calculationTV = findViewById(R.id.calculationTV);
        resultTV = findViewById(R.id.resultTV);

        // get the MaterialButtons from xml
        assignAllMaterialButtons();

        // switch to calculator activity
        switchToCalculator();

        // check for network connection
        boolean connNet = checkConnection("Network");
        boolean connInt = checkConnection("Internet");

        if (connNet && connInt) {
            loadConverter();
        } else if (!connNet) {
            // let the user know they need access to a network to use the converter
            UserAlertNetworkAccess();
        } else if (connNet && !connInt) {
            // let the user know they need access to the internet to use the converter
            UserAlertInternetAccess();
        }
    }

    void assignID(MaterialButton btn, int id) {
        btn = findViewById(id);
        btn.setOnClickListener(this);
    }

    void assignAllMaterialButtons() {
        assignID(buttonCalculator, R.id.button_calculator);
        assignID(buttonD, R.id.button_delete);
        assignID(buttonC, R.id.button_c);
        assignID(buttonDecimalPoint, R.id.button_decimal_point);

        assignID(button0, R.id.button_0);
        assignID(button1, R.id.button_1);
        assignID(button2, R.id.button_2);
        assignID(button3, R.id.button_3);
        assignID(button4, R.id.button_4);
        assignID(button5, R.id.button_5);
        assignID(button6, R.id.button_6);
        assignID(button7, R.id.button_7);
        assignID(button8, R.id.button_8);
        assignID(button9, R.id.button_9);
    }

    public void onClick(View view) {
        // get clicked button's content
        MaterialButton btn = (MaterialButton) view;
        String btnText = btn.getText().toString();

        // if the calculator does not take place
        if (!btnText.equals("Calculator")) {
            // get all previous content
            String prevData = calculationTV.getText().toString();

            // send all collected data to appear in app
            setValue(prevData, btnText);
        }
    }

    void switchToCalculator() {
        buttonCalculator = findViewById(R.id.button_calculator);
        buttonCalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    void loadConverter() {
        // get rate codes from API fixer.io and assign them to spinners
        findRates();

        // everytime a spinner changes, change the conversion instantly
        dropdown1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // convert the currency
                convertCurrency();
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        dropdown2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // convert the currency
                convertCurrency();
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    void UserAlertNetworkAccess() {
        AlertDialog dialogNetwork = new AlertDialog.Builder(ConverterActivity.this)
                .setTitle("Warning")
                .setMessage(R.string.networkAccessMessage)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // check for network connection
                        boolean connNet = checkConnection("Network");

                        if (!connNet) {
                            finish(); // switch to calculator
                        } else {
                            // make sure the network has access to the internet
                            boolean coonInt = checkConnection("Internet");

                            if (coonInt) {
                                loadConverter(); // load the content
                            } else {
                                // let the user know they need access to the internet to use the converter
                                UserAlertInternetAccess();
                            }
                        }
                        dialogInterface.dismiss();
                    }
                })
                .create();
        dialogNetwork.show();
    }

    void UserAlertInternetAccess() {
        // let the user know they need access to the internet to use the converter
        AlertDialog dialogInternet = new AlertDialog.Builder(ConverterActivity.this)
                .setTitle("Warning")
                .setMessage(R.string.internetAccessMessage)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // check for internet connection
                        boolean coonInt = checkConnection("Internet");

                        if (!coonInt) {
                            finish(); // switch to calculator
                        } else {
                            loadConverter(); // load the content
                        }
                        dialogInterface.dismiss();
                    }
                })
                .create();
        dialogInternet.show();
    }

    boolean checkConnection(String type) {
        if (type == "Network") {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean connection = (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);

            return connection;
        } else { // type == "Internet"
            try {
                String command = "ping -c 1 google.com";
                return (Runtime.getRuntime().exec(command).waitFor() == 0);
            } catch (Exception e) {
                return false;
            }
        }
    }

    void findRates() {
        // set API Endpoint and API key
        String endpoint = "latest"; // get latest rates
        String access_key = "b8e1c36dea19658a7fd56486f9f00d6b"; // private access key

        // set a permit-all policy to allow execution
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // build client
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // build request
        Request request = new Request.Builder()
                .url("http://data.fixer.io/api/"+endpoint+"?access_key="+access_key+"")
                .method("GET", null).build();

        // execute request and get response
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // parse response data to string type
        String jsonData = null;
        try {
            jsonData = response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // parse string data to json object type
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(jsonData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // get rates from json
        JSONObject rates = null;
        try {
            if (jObject.has("rates")) {
                rates = jObject.getJSONObject("rates");
            } else {
                System.out.println("Response didn't return rates.");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // get all rate keys (three-letter currency codes)
        Iterator<String> keys =  rates.keys();

        int keysNum = 0;
        do {
            String k = keys.next().toString();
            keysNum++; // find number of keys

        } while(keys.hasNext());

        // insert keys in codes array
        keys =  rates.keys();
        String[] codes = new String[keysNum];
        for (int i = 0; i < keysNum; i++) {
            String k = keys.next().toString();
            codes[i] = k;
        }

        // create an adapter to describe how the items are displayed
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, codes);

        //set the adapters to each spinner
        dropdown1.setAdapter(adapter);
        dropdown2.setAdapter(adapter);
    }

    public void setValue(String prevData, String newData) {
        // check if the newly pressed button was C (delete all characters)
        if(newData.equals("C")) {
            calculationTV.setText("");
            resultTV.setText("");
            return;
        }

        // add a zero in front of the decimal when there isn't any
        if(newData.equals(".") && prevData.length() == 0) {
            calculationTV.setText("0.");
            resultTV.setText("");
            return;
        }

        // don't allow second decimal point
        if(newData.equals(".") && prevData.contains(".")) {
            calculationTV.setText(prevData);
            return;
        }

        // check if the newly pressed button was D (delete one character)
        if (newData.equals("D")) {
            if (prevData.length() == 0 || prevData.length() == 1) {
                prevData = "";
            } else {
                prevData = prevData.substring(0, prevData.length() - 1);
            }
            newData = prevData;

            // set new data as calculation
            calculationTV.setText(newData);

            // execute the conversion
            convertCurrency();
            return;
        }

        /* if zero is the first digit, don't allow second zero to be added.
         * otherwise update normally */
        if (!(prevData.startsWith("0") && prevData.length() == 1 && !newData.equals("."))) {
            newData = prevData + newData;
        }

        // if max length reached, don't allow more digits
        if (prevData.length() == 8) {
            newData = prevData;
            // let the user know they exceeded the limit
            Snackbar.make(findViewById(R.id.relative_layout_converter), R.string.maximumLengthMessage, Snackbar.LENGTH_SHORT).show();
        }

        // set new data as calculation
        calculationTV.setText(newData);

        // execute the conversion
        convertCurrency();
    }

    void convertCurrency() {
        // set API Endpoint and API key
        String endpoint = "convert"; // do conversion
        String access_key = "b8e1c36dea19658a7fd56486f9f00d6b"; // private access key
        String from = dropdown1.getSelectedItem().toString();
        String to = dropdown2.getSelectedItem().toString();
        double amount = 0;
        if (!calculationTV.getText().toString().equals("")) {
            amount = Double.valueOf(calculationTV.getText().toString());
        }

        // build client
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        // build request
        Request request = new Request.Builder()
                .url("http://data.fixer.io/api/"+endpoint+"?access_key="+access_key+"&from="+from+"&to="+to+"&amount="+amount)
                .method("GET", null).build();

        // execute request and get response
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // parse response data to string type
        String jsonData = null;
        try {
            jsonData = response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // parse string data to json object type
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(jsonData);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // get conversion result from json and set it in app
        try {
            if (jObject.has("result")) {
                // parse json data to string type
                String finalResult = null;
                finalResult = jObject.getString("result");
                // set result in app
                resultTV.setText(finalResult);
            } else {
                System.out.println("It doesn't have result.");
                // set result in app
                resultTV.setText("");
            }
        } catch (JSONException e) {
            // set result in app
            resultTV.setText("");
            throw new RuntimeException(e);
        }
    }
}