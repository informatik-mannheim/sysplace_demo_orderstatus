package de.hs_mannheim.carsalesmansbuddy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Customer> mCustomers = new ArrayList<>();
    private int mCurrentStatus = -1;
    private String mCurrentCustomer = "", mCurrentMessage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        mCustomers.add(new Customer("Kirstin Kohler"));
        mCustomers.add(new Customer("Uta Diehl"));
        mCustomers.add(new Customer("Volker Weckbach"));
        mCustomers.add(new Customer("Valentina Burjan"));
        mCustomers.add(new Customer("Cristin Volz"));
        mCustomers.add(new Customer("Horst Schneider"));
        mCustomers.add(new Customer("Dominick Madden"));

        ListView listView = (ListView) findViewById(R.id.listView);

        ArrayAdapter<Customer> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, mCustomers);

        if (adapter.getCount() > 3) {
            View item = adapter.getView(0, null, listView);
            item.measure(0, 0);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) (3.5 * item.getMeasuredHeight()));
            listView.setLayoutParams(params);
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrentCustomer = mCustomers.get(position).getName();
                mCurrentStatus = mCustomers.get(position).getPercentage();
                mCurrentMessage = mCustomers.get(position).getMessage();
                updateUI(mCustomers.get(position));
            }
        });

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentStatus != -1) {

                    EditText editText = (EditText) findViewById(R.id.messageText);
                    mCurrentMessage = editText.getText().toString();

                    JSONObject jsonBody = new JSONObject();
                    try {
                        jsonBody.put("customer", mCurrentCustomer);
                        jsonBody.put("status", mCurrentStatus + "");
                        jsonBody.put("message", mCurrentMessage);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String jsonTranslation = jsonBody.toString();
                    Log.d("JSON", jsonTranslation);

                    final String requestBody = "key=bestellstatus&value=" + jsonBody.toString();
                    Log.d("BODY", requestBody);

                    RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                    String url = "http://37.61.204.167:8080/string-store/set";
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    Log.d("Response POST", response + "");
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    // TODO Auto-generated method stub
                                    Log.d("ERROR", "error => " + error.toString());
                                }
                            }
                    ) {
                        @Override
                        public String getBodyContentType() {
                            return "application/x-www-form-urlencoded";
                        }

                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            try {
                                return requestBody == null ? null : requestBody.getBytes("utf-8");
                            } catch (UnsupportedEncodingException uee) {
                                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                                return null;
                            }
                        }
                    };
                    queue.add(stringRequest);
                }
            }
        });

        getStatus();
    }

    private void getStatus() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://37.61.204.167:8080/string-store/get?key=bestellstatus";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);

                        JSONObject customerJSON = null;
                        try {
                            customerJSON = new JSONObject(response);
                            String responseCustomer = customerJSON.getString("customer");
                            int responsePercentage = customerJSON.getInt("status");
                            String responseMessage = customerJSON.getString("message");


                            Customer newCustomer = new Customer("TEST");
                            Customer updatedCustomer = new Customer("ERROR");
                            for (Customer customer : mCustomers) {

                                if (customer.getName().equals(responseCustomer)) {
                                    updatedCustomer = customer;

                                    newCustomer = new Customer(responseCustomer, responsePercentage,
                                            responseMessage);
                                }
                            }
                            mCustomers.remove(updatedCustomer);
                            mCustomers.add(newCustomer);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("ERROR", "error => " + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Accept", "*/*");

                return params;
            }
        };


        queue.add(stringRequest);
    }

    private void updateUI(Customer customer) {
        final TextView percentageView = (TextView) findViewById(R.id.percentageView);
        TextView nameView = (TextView) findViewById(R.id.nameTextView);
        EditText messageView = (EditText) findViewById(R.id.messageText);
        messageView.setVisibility(View.VISIBLE);
        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                percentageView.setText("Bestellungsstatus: " + progress + "%");
                if (progress == 100) {
                    percentageView.setTextColor(getResources().getColor(R.color.green));
                } else {
                    percentageView.setTextColor(getResources().getColor(android.R.color.primary_text_light));
                }
                mCurrentStatus = progress;
            }
        });

        customer.getPercentage();
        messageView.setText(customer.getMessage());
        nameView.setText(customer.getName());
        seekBar.setProgress(customer.getPercentage());
    }
}
