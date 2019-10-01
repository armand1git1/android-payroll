package z.myapplication;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import android.app.Activity;
import static z.myapplication.Constants.FIRST_COLUMN;
import static z.myapplication.Constants.SECOND_COLUMN;

public class MainActivity extends AppCompatActivity {
    TextView mTextView_person;  // fieldset where the employee wage are displayed
    // Create an object of the class where the functionality are done
    // details of the employees from the csv file
    employees_infos mydata            = new employees_infos();
    payment_details employee_payment  = new payment_details();
    int action                        = 0;                     // Deciding on the action to take
    private ArrayList<HashMap<String, String>> list;
    @Override

    // defining the content of the main page at the creation of the application
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // View log
        Log.d("MainActivity",	"Payroll");
        Toolbar toolbar               = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_home);
        // calling the display notification function with the data to display in the main page
        String[] the_payment    = this.displayNotification();
        ArrayAdapter adapter    = new ArrayAdapter<String>(this,R.layout.activity_listview, the_payment);
        ListView     listView   = (ListView) findViewById(R.id.payment_list);
        listView.setTextFilterEnabled(true);
        listView.setAdapter(adapter);
        listView.setFocusable(true);
        // Click on the listview open anoter view with details on payment
        listView.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?>adapter,View v, int position, long id){
                // selected item
                String persone  = ((TextView) v).getText().toString();
                // Launching new Activity after selecting single List Item
                Intent i        = new Intent(getApplicationContext(), payment_details.class);
                // sending data to new activity
                i.putExtra("payment_list", persone);
                startActivity(i);
            }});
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Function which displays the final results into the main screen
    public String[]  displayNotification() {
        Log.i("Start", "notification");

        Map<String, String> employee_lists = new HashMap<String, String>();
        InputStreamReader inputStreamReader;
        Context context = myapplication.getAppContext();
        //setContentView(R.layout.content_main);
        String TableName1 = "personel";
        String TableName2 = "working_period";
        // Added in May 2016
        // inserting the data into an SQlite database
        // 1- Creation of the database
        SQLiteDatabase mydatabase = openOrCreateDatabase("payroll_sqlite_db", MODE_PRIVATE, null); // It creates the database if it is not yet created
        // 2- Table deletion
        //mydatabase.execSQL("DROP TABLE IF EXISTS working_period");
        //mydatabase.execSQL("DROP TABLE IF EXISTS personel");

        // 3- Creation of the table personel
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TableName1 + " (personel_id VARCHAR, personel VARCHAR);");   // It creates the table personel if it is not yet created
        // mydatabase.execSQL("DROP TABLE personel;");
        // 4- Creation of the table working_period
        //mydatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TableName2 + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, personel_id VARCHAR,working_day VARCHAR,regular_hour REAL, evening_hour REAL);"); // It creates the table working_period if it is not yet created
        mydatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TableName2 + " (working_id INTEGER, personel_id VARCHAR,working_day DATE,regular_hour DOUBLE, evening_hour DOUBLE);"); // It creates the table working_period if it is not yet created
        // Insertion into a tables
        // int ii                          = 0;
        // insert all the data in the table
        // empty table
        // mydatabase.execSQL("delete from personel");
        // empty the content of the table personnel
        // Read Csv file
        InputStream inputStream = getResources().openRawResource(R.raw.hourlist201403);
        employee_lists = mydata.parsing_csv_data(inputStream, action); // parsing the data of the csv file to the function and getting compuerized value
        //System.exit(0);
        // getting the number of rows of the table personel
        long numRows = DatabaseUtils.queryNumEntries(mydatabase, "personel");
        if (numRows == 0) {
            for (Map.Entry entry : employee_lists.entrySet()) {
                Object values = entry.getValue();                // values of the map
                String[] RowData = values.toString().split("-");    // deviding the string base on the character "-"
                Object key_value = entry.getKey();                  // Getting the value of the key
                String[] key_data = key_value.toString().split("-"); // Deviding the value of the key based on the character "-"
                // Printing data before insertion
                Cursor res = mydatabase.rawQuery("select * from personel where personel_id=" + key_data[0] + "", null);
                // checking if the table does not contain the value then insert.
                if (!(res.moveToFirst()) || res.getCount() == 0) {
                    // insert into table pesonel
                    mydatabase.execSQL("INSERT or replace INTO  personel  (personel_id, personel) VALUES('" + key_data[0] + "','" + RowData[0] + "')");
                }
            }
        }

        // list of current employees
        Cursor resultSet_employees           = mydata.employees_list("");
        ArrayList<String> current_employees  = new ArrayList<String>();
        String[] person_payment              = new String[(int) numRows]; // OK

        int ii = 0;
        if (resultSet_employees.moveToFirst()) {
            do {
                String employee_id           = resultSet_employees.getString(0);
                String employee              = resultSet_employees.getString(1);

                double payment               = 0;
                String amount_final          = "";
                // list of working days group in daily basis
                Cursor resultSet2            = mydata.employees_recap(employee_id.trim());
                String sample_data           = "";
                payment                      = employee_payment.computerized_payment(resultSet2, sample_data,1);
                amount_final                 = String.format("%.2f", payment);
                person_payment[ii] = employee_id.trim().concat("-").concat(employee).concat(", ").concat(amount_final).concat("$");
             ii++;
            }while (resultSet_employees.moveToNext());
            resultSet_employees.close();
        }
        mydatabase.close();
        return person_payment;
    }
}
