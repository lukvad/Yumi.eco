package eco.yumi;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {
    private EditText nEmail, nPassword1, nPassword2, nFirstname, nSurname ;
    private Button nRegister;
    private TextView back;
    private EditText nDisplayDate;
    private static String firstname="", surname="", email ="", password="";
    private static final int REQUEST_CODE_ASK_PERMISSIONS=123;
    private static Boolean bPassword=false;
    private DatePickerDialog.OnDateSetListener nDataListener;
    private Integer day, month, year;
    private Intent intent;
    private Boolean dateSet = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(eco.yumi.R.layout.activity_register);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CODE_ASK_PERMISSIONS);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {
                showExplanation(getResources().getString(R.string.CameraPermitTitle), getResources().getString(R.string.CameraPermitText), android.Manifest.permission.CAMERA, REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
        intent = new Intent(RegisterActivity.this, Register2Activity.class);
        nEmail = findViewById(R.id.email);
        nDisplayDate = findViewById(R.id.data);
        nPassword1 = findViewById(R.id.password);
        back = findViewById(R.id.back);
        nPassword2 = findViewById(R.id.confirmPassword);
        nFirstname = findViewById(R.id.firstName);
        nSurname = findViewById(R.id.surName);
        nRegister = findViewById(R.id.register);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        final ColorStateList colorStateList = ColorStateList.valueOf(getResources().getColor(R.color.RED));
        final ColorStateList colorStateList1 = ColorStateList.valueOf(getResources().getColor(R.color.colorAccent));
        nDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        RegisterActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth, nDataListener,
                        year, month,day );
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        nDataListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                dateSet = true;
                year = i;
                month = i1+1;
                day = i2;
                String date = i2+"/"+i1+1+"/"+i;
                intent.putExtra("day", day);
                intent.putExtra("month", month);
                intent.putExtra("year", year);
                nDisplayDate.setText(date);
                dateSet = true;
            }
        };
        nPassword1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence                charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password1 = nPassword1.getText().toString();
                String password2 = nPassword2.getText().toString();
                if ((!password1.equals(""))&&(password1.equals(password2))){
                    bPassword=true;
                    if(Build.VERSION.SDK_INT >= 21) {
                        nPassword1.setBackgroundTintList(colorStateList1);
                        nPassword2.setBackgroundTintList(colorStateList1);
                    }
                }
                else {
                    bPassword=false;
                    if(Build.VERSION.SDK_INT >= 21) {
                        nPassword1.setBackgroundTintList(colorStateList);
                        nPassword2.setBackgroundTintList(colorStateList);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        nPassword2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String password1 = nPassword1.getText().toString();
                String password2 = nPassword2.getText().toString();
                if ((!password2.equals(""))&&(password1.equals(password2))){
                    bPassword=true;
                    if(Build.VERSION.SDK_INT >= 21) {
                        nPassword1.setBackgroundTintList(colorStateList1);
                        nPassword2.setBackgroundTintList(colorStateList1);
                    }
                }
                else {
                    bPassword=false;
                    if(Build.VERSION.SDK_INT >= 21) {
                        nPassword1.setBackgroundTintList(colorStateList);
                        nPassword2.setBackgroundTintList(colorStateList);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        nRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = nEmail.getText().toString();
                password = nPassword1.getText().toString();
                firstname = nFirstname.getText().toString();
                surname = nSurname.getText().toString();
                if(bPassword) {
                    if ((!email.equals("")) && (!password.equals("")) && (!firstname.equals("")) && (!surname.equals("")) && dateSet) {
                        if (isDigit(password) && (password.length() > 7)) {
                            if((email.contains("@"))&&(email.contains("."))) {
                                intent.putExtra("email", email);
                                intent.putExtra("password", password);
                                intent.putExtra("firstname", firstname);
                                intent.putExtra("surname", surname);
                                startActivity(intent);
                                }
                            else
                                Toast.makeText(RegisterActivity.this, getResources().getString(R.string.signupEmail),Toast.LENGTH_SHORT).show();
                         } else
                            Toast.makeText(RegisterActivity.this, getResources().getString(R.string.passwordWrong), Toast.LENGTH_SHORT).show();

                    } else
                        Toast.makeText(RegisterActivity.this, getResources().getString(R.string.signupBad), Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(RegisterActivity.this, getResources().getString(R.string.differentPass), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Boolean isDigit(String str) {
        for(int i=str.length()-1; i>=0; i--) {
            if(Character.isDigit(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    private void showExplanation(String title,String message,final String permission,final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }
    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[],int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getResources().getString(R.string.PermisionGranted), Toast.LENGTH_SHORT).show();
                } else {
                    showExplanation(getResources().getString(R.string.CameraPermitTitle), getResources().getString(R.string.CameraPermitText), android.Manifest.permission.CAMERA, REQUEST_CODE_ASK_PERMISSIONS);
                    Toast.makeText(this, getResources().getString(R.string.PermisionDenied), Toast.LENGTH_SHORT).show();
                }
        }
    }

}