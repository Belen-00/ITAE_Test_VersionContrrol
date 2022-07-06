package com.example.android.androidskeletonapp.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import com.example.android.androidskeletonapp.ui.base.ListActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.androidskeletonapp.R;
import com.example.android.androidskeletonapp.data.Sdk;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCreateProjection;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


public class ScreenfireActivity extends ListActivity {

    private FileChooserFragment fragment;
    private Button buttonShowInfo;

    private TextView syncStatusTextScreen = null;
    private ProgressBar progressBarScreen;

    //DHIS2 dataElements IDs
    String pidAtri = "kbW6APe33De";
    String progScreenfire = "YAaoJjDmVrr";
    String hpv16ID = "fGpfPaD7ref";
    String hpv18ID = "N6X9iGf0hYf";
    String hpv31ID = "icPyvk9tiKN";
    String hpv39ID = "KP0voeAqAN4";
    String dateTestUid = "MWuhIWJgEzW";

   // /*
   @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.screenfire_activity);
        setUp(R.layout.screenfire_activity, R.id.screenToolbar, R.id.screenfireProgress);

        syncStatusTextScreen = findViewById(R.id.screenfireProgress);
        //progressBarScreen = findViewById(R.id.syncProgressBar_screen);

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        this.fragment = (FileChooserFragment) fragmentManager.findFragmentById(R.id.fragment_fileChooser);
        this.buttonShowInfo = this.findViewById(R.id.button_showInfo);
        this.buttonShowInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfo();
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            int REQUEST_CODE_PERMISSION_STORAGE = 100;
            String[] permissions = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };

            for (String str : permissions) {
                if (this.checkSelfPermission(str) != PackageManager.PERMISSION_GRANTED) {
                    this.requestPermissions(permissions, REQUEST_CODE_PERMISSION_STORAGE);
                    return;
                }
            }
        }
    }




    private void showInfo()  {
        //syncStatusTextScreen.append("Loading ScreenFire values. Please wait.");
        try {
            Log.d("ROW1","");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                //Verifica permisos para Android 6.0+
                int permissionCheck = ContextCompat.checkSelfPermission(
                        this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    Log.i("Mensaje", "No se tiene permiso para leer.");
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 225);
                } else {
                    Log.i("Mensaje", "Se tiene permiso para leer!");
                }
            }
            readExcelFromStorage(getApplicationContext(), this.fragment.getPath());
        }catch(Exception e){
            Log.e("ERROR", e.toString());
            e.printStackTrace();
        }
        String path = this.fragment.getPath();
        Toast.makeText(this, "Process finished.", Toast.LENGTH_LONG).show();
        SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedpreferences.edit();
        edit.putString("sync", "false");
        edit.commit();
    }

    public static Intent getIntent(Context context) {
        return new Intent(context,ScreenfireActivity.class);
    }

    public void readExcelFromStorage(Context context, String fileName) {
        //syncStatusTextScreen.append("Loading ScrenFire values. Please wait.");
        File file = new File(fileName);
        FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Log.e("SUCCESS", "Reading from Excel" + file);
            // Create instance having reference to .xls file
            Workbook workbook = null;
            try {
                workbook = new HSSFWorkbook(fileInputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Fetch sheet at position 'i' from the workbook
            Sheet sheetResults = workbook.getSheetAt(11);

            // Iterate through each row
            for (Row row : sheetResults) {
                String[] allValuesRow = new String[11];
                int i = 0;
                if (row.getRowNum() > 0) {
                    // Iterate through all the cells in a row (Excluding header row)
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        // Check cell type and format accordingly
                        switch (cell.getCellType()) {
                            case Cell.CELL_TYPE_NUMERIC:
                                // Print cell value
                                allValuesRow[i] = String.valueOf(cell.getNumericCellValue());
                                i++;
                                break;

                            case Cell.CELL_TYPE_STRING:
                                allValuesRow[i] = cell.getStringCellValue();
                                i++;
                                break;
                        }
                    }
                    try {
                        if (null != fileInputStream) {
                            fileInputStream.close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if(allValuesRow[0] != null){
                    Sheet sheetDate = workbook.getSheetAt(1);
                    Row rowDate = sheetDate.getRow(1);
                    String cellDate = rowDate.getCell(10).toString();
                    System.out.println("DATEEE "+ cellDate);
                    addToCore(allValuesRow, cellDate);
                }
            }
       // syncStatusTextScreen.setText("Process finished. You can exit the app now.");
    }

    public void addToCore(String[] values, String date){
        String pid = values[1].split("\\.")[0];
        System.out.println(pid);
        String genotypes = values[2];
        String hpv16 = "";
        String hpv18 = "";
        String hpv31 = "";
        String hpv39 = "";
        if(genotypes.contains("HPV16")){
            hpv16 = values[3];
            System.out.println(hpv16);
        }if(genotypes.contains("HPV18")){
            hpv18 = values[4];
            System.out.println(hpv18);
        }if(genotypes.contains("HPV31")){
            hpv31 = values[5];
            System.out.println(hpv31);
        }if(genotypes.contains("HPV39")){
            hpv39 = values[6];
            System.out.println(hpv39);
        }
        String enrollmentID = getEnrrolmentUid(pid);
        addEvent(enrollmentID, hpv16,hpv18,hpv31,hpv39, date);
    }

    private String getEnrrolmentUid(String pid){
        String enrollmentID = null;
        List<TrackedEntityInstance> instances = Sdk.d2().trackedEntityModule().trackedEntityInstanceQuery()
                .byAttribute(pidAtri).eq(pid)
                .blockingGet();
        if(instances.isEmpty()){
            enrollmentID = enroll(pid);
            return enrollmentID;
        }else{
            String trackerID = instances.get(0).uid();
            List<Enrollment> enrollment = Sdk.d2().enrollmentModule().enrollments().
                    byTrackedEntityInstance().eq(trackerID).blockingGet();
            if(enrollment == null){
                return null;
            }
            return enrollment.get(0).uid();
        }
    }

    private String enroll(String pid){
        String enrollmentID = null;
        try {
            SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            String unitUid = sharedpreferences.getString("unit", "");
            Log.d("ORGUNIT1", unitUid);
            String trackedUid = Sdk.d2().trackedEntityModule().trackedEntityInstances()
                    .blockingAdd(TrackedEntityInstanceCreateProjection.
                            create(unitUid, "RohgTo6O2d1"));
            Sdk.d2().trackedEntityModule().trackedEntityAttributeValues()
                    .value(pidAtri, trackedUid).blockingSet(pid);
            enrollmentID = Sdk.d2().enrollmentModule().enrollments().
                    blockingAdd(EnrollmentCreateProjection.create(unitUid, progScreenfire, trackedUid));
            Sdk.d2().enrollmentModule().enrollments().uid(enrollmentID).setEnrollmentDate(new Date());
        }catch (Exception e){
            Log.e("DHIS2 ENROLLING","ERROR");
            e.printStackTrace();
            return null;
        }
        return enrollmentID;
    }


    private void addEvent(String enrollmentID, String hpv16result, String hpv18result, String hpv31result, String hpv39result, String dateTest){
        try {
            SharedPreferences sharedpreferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
            String unitUid = sharedpreferences.getString("unit", "");
            Log.d("ORGUNIT2", unitUid);
            String eventUid = Sdk.d2().eventModule().events().blockingAdd(EventCreateProjection
                    .create(enrollmentID,progScreenfire, "O8mTcaoWWJy", unitUid, "HllvX50cXC0"));
            Sdk.d2().eventModule().events().uid(eventUid).setEventDate(new Date());
            Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, dateTestUid).blockingSet(dateTest);
            Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, hpv16ID).blockingSet(hpv16result);
            Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, hpv18ID).blockingSet(hpv18result);
            Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, hpv31ID).blockingSet(hpv31result);
            Sdk.d2().trackedEntityModule().trackedEntityDataValues().value(eventUid, hpv39ID).blockingSet(hpv39result);
            Log.d("EVENTUID",eventUid);
        }catch (Exception e){
            Log.e("DHIS2 ADDING EVENT","ERROR");
            e.printStackTrace();
        }
    }

    public void setSyncing() {
        syncStatusTextScreen.setText("Loading ScrenFire values. Please wait.");
    }

    public void setSyncingFinished() {
        syncStatusTextScreen.setText("Process finished. You can exit the app now.");
    }
    // */

}