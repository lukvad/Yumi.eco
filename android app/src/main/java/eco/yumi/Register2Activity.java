package eco.yumi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import eco.yumi.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Register2Activity extends AppCompatActivity {
    private EditText  nId, nAddress1, nPhone ;
    private Button nRegister;
    private Button bFront, bRear;
    private FirebaseAuth nAuth;
    private static String  address="", id="";
    protected String user_id="", email="", password="", firstname="", surname="", phone="";
    protected DatabaseReference current_user_db;
    static  final  int FRONT = 1, REAR = 2;
    private Uri uFront, uRear;
    private StorageReference mStorage;
    String mCurrentPhotoPath, mFirebasePath;
    private static FirebaseAuth.AuthStateListener firebaseAuthListener;
    Boolean addFront =false, addRear = false;
    private Target front, rear;
    private CheckBox cTerms, cTerms2, cTerms3;
    private TextView back, readme, policy , rodo, progress;
    private ProgressBar progressBar;
    private static final int REQUEST_CODE_ASK_PERMISSIONS=123;
    private static int day, month, year;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(eco.yumi.R.layout.activity_register2);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_ASK_PERMISSIONS);
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showExplanation(getResources().getString(R.string.StoragePermitTitle), getResources().getString(R.string.StoragePermitText), Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
        }
        if(savedInstanceState==null) {
            Bundle register = getIntent().getExtras();
            if (register != null) {
                email = register.getString("email");
                password = register.getString("password");
                firstname = register.getString("firstname");
                surname= register.getString("surname");
                day= register.getInt("day");
                month= register.getInt("month");
                year= register.getInt("year");
            }
            else {
                email= (String) savedInstanceState.getSerializable("email");
                password= (String) savedInstanceState.getSerializable("password");
                firstname= (String) savedInstanceState.getSerializable("firsname");
                surname= (String) savedInstanceState.getSerializable("surname");
                email = register.getString("email");
                password = register.getString("password");
                firstname = register.getString("firstname");
                surname= register.getString("surname");
                day= register.getInt("day");
                month= register.getInt("month");
                year= register.getInt("year");
            }
        }
        RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        nAuth = FirebaseAuth.getInstance();
        readme = findViewById(R.id.readme);
        policy = findViewById(R.id.policy);
        rodo = findViewById(R.id.RODO);
        cTerms = findViewById(R.id.cTerms);
        progress = findViewById(R.id.foot);
        cTerms2 = findViewById(R.id.cTerms2);
        cTerms3 = findViewById(R.id.cTerms3);
        nPhone = findViewById(R.id.phone);
        readme.setMovementMethod(LinkMovementMethod.getInstance());
        policy.setMovementMethod(LinkMovementMethod.getInstance());
        rodo.setMovementMethod(LinkMovementMethod.getInstance());
        nAddress1 = findViewById(R.id.address);
        nId = findViewById(R.id.idno);
        nRegister = findViewById(R.id.register);
        back = findViewById(R.id.back2);
        bFront =findViewById(R.id.frontCapture);
        bRear = findViewById(R.id.rearCapture);
        progressBar = new ProgressBar(Register2Activity.this,null,android.R.attr.progressBarStyleLarge);
        progressBar.setVisibility(View.INVISIBLE);
        relativeLayout.addView(progressBar,params);
        mStorage = FirebaseStorage.getInstance().getReference();
        final ImagePopup frontPopUp = new ImagePopup(this);
        final ImagePopup rearPopUp = new ImagePopup(this);
        frontPopUp.setWindowHeight(800); // Optional
        frontPopUp.setWindowWidth(800); // Optional
        frontPopUp.setBackgroundColor(Color.BLACK);  // Optional
        frontPopUp.setFullScreen(true); // Optional
        frontPopUp.setHideCloseIcon(true);  // Optional
        frontPopUp.setImageOnClickClose(true);  // Optional
        rearPopUp.setWindowHeight(800); // Optional
        rearPopUp.setWindowWidth(800); // Optional
        rearPopUp.setBackgroundColor(Color.BLACK);  // Optional
        rearPopUp.setFullScreen(true); // Optional
        rearPopUp.setHideCloseIcon(true);  // Optional
        rearPopUp.setImageOnClickClose(true);  // Optional
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        front = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Bitmap bitmap1 = bitmap;
                Drawable image = new BitmapDrawable(getResources(), bitmap1);
                frontPopUp.initiatePopup(image);
                addFront =true;
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {}

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };
        rear = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Bitmap bitmap1 = bitmap;
                Drawable image = new BitmapDrawable(getResources(), bitmap1);
                rearPopUp.initiatePopup(image);
                addRear =true;
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {}

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };

        bFront.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(addFront)
                    frontPopUp.viewPopup();
                else {
                   uFront = dispatchTakePictureIntent(1);
                }
            }
        });
        bRear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addRear)
                    rearPopUp.viewPopup();
                else {
                   uRear =  dispatchTakePictureIntent(2);
                }
            }
        });



        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    if (!user.isEmailVerified()) {
                        if(addFront&&addRear){
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Documents/DriverLicense");
                            final StorageReference frontRef = storageRef.child("license+" + email);
                            final StorageReference rearRef = storageRef.child("rear+" + email);
                            final UploadTask uploadTask = frontRef.putFile(uFront);
                            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    long full = taskSnapshot.getTotalByteCount();
                                    long uploaded = taskSnapshot.getBytesTransferred();
                                    double upl = (100.0 *uploaded)/full;
                                    progress.setText("Plik 1/2 : " + (int) upl + "%");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(Register2Activity.this, "uploading image failed", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                            final UploadTask uploadTask = rearRef.putFile(uRear);
                                                            final UploadTask.TaskSnapshot taskFront = taskSnapshot;
                                                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(Register2Activity.this, "uploading image failed", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                                    postUser(taskSnapshot, taskFront, user);
                                                                }
                                                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                                                @Override
                                                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                                                    long full = taskSnapshot.getTotalByteCount();
                                                                    long uploaded = taskSnapshot.getBytesTransferred();
                                                                    double upl = (100.0 * uploaded)/full;
                                                                    progress.setText("Plik 2/2 : " + (int) upl + "%");
                                                                }
                                                            });
                                                        }
                                                    }
                                );
                            }
                        }
                    }
                    return;
                }
            };

        nRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address = nAddress1.getText().toString();
                phone = nPhone.getText().toString();
                id = nId.getText().toString();
                if((!address.equals(""))&&(!id.equals(""))&&(!phone.equals(""))&&(cTerms.isChecked())&&(cTerms2.isChecked())&&(cTerms3.isChecked())) {
                    if(addFront&&addRear) {
                        if(ageCheck(day,month,year)) {
                            signIn();
                        }else confirmRegistering(getResources().getString(R.string.dlTitle),getResources().getString(R.string.dl),true);
                    } else confirmRegistering(getResources().getString(R.string.longerTitle),getResources().getString(R.string.longer),false);
                }
                else Toast.makeText(Register2Activity.this, getResources().getString(R.string.signupBad),Toast.LENGTH_LONG).show();
            }
        });
    }
private void confirmRegistering (String title, String text, Boolean a ) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(text);
        if(a){
            builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    finish();
                }
            }).setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    signIn();
                    dialogInterface.dismiss();
                }
            });
        }
        builder.create().show();
    }

    private Boolean ageCheck (int d, int m, int y){
        if(y<1995)
            return true;
        else{
            if(y==1995){
                if(m>1)
                    return false;
                else{
                    if(d<18){
                        return true;
                    }else
                        return false;
                }
            }else
                return false;
        }
    }
    private void signIn(){
        progressBar.setVisibility(View.VISIBLE);
        nAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Register2Activity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(Register2Activity.this, getResources().getString(R.string.signupError), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        nAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        nAuth.removeAuthStateListener(firebaseAuthListener);
    }
    private File createImageFile(int code) throws IOException {
        // Create an image file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + code +"_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FRONT && resultCode == RESULT_OK){
                Picasso.with(Register2Activity.this).load(uFront).into(front);
        }
        if(requestCode == REAR && resultCode == RESULT_OK){
            Picasso.with(Register2Activity.this).load(uRear).into(rear);
        }
    }
    void postUser(UploadTask.TaskSnapshot taskSnapshot, UploadTask.TaskSnapshot taskSnapshot2, FirebaseUser user){
        String frontUrl = "";
        String rearUrl = "";
        if(taskSnapshot!=null&&taskSnapshot2!=null) {
            frontUrl = taskSnapshot.getDownloadUrl().toString();
            rearUrl = taskSnapshot2.getDownloadUrl().toString();
        }
        user_id = user.getUid();
        user.sendEmailVerification();
        current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        if(cTerms.isChecked()){
            current_user_db.child("policyCheck").setValue(true);
        }
        if(cTerms2.isChecked()){
            current_user_db.child("rodoCheck").setValue(ServerValue.TIMESTAMP);
        }
        if(cTerms2.isChecked()){
            current_user_db.child("privacyCheck").setValue(ServerValue.TIMESTAMP);
        }
        current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        current_user_db.child("firstname").setValue(firstname);
        current_user_db.child("surname").setValue(surname);
        current_user_db.child("idno").setValue(id);
        current_user_db.child("email").setValue(email);
        current_user_db.child("phone").setValue(phone);
        current_user_db.child("address").setValue(address);
        current_user_db.child("url").setValue(frontUrl);
        current_user_db.child("rear").setValue(rearUrl);
        current_user_db.child("date").setValue(day+"/"+month+"/"+year);
        Intent intent = new Intent(Register2Activity.this, VerificationActivity.class);
        intent.putExtra(user.getEmail(),1);
        progressBar.setVisibility(View.INVISIBLE);
        startActivity(intent);
        finish();
    }
    private Uri dispatchTakePictureIntent(int code) {
        Uri uri = null;
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return uri ;
            }
            File photoFile = null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go

            try {
                photoFile = createImageFile(code);
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created


            if (photoFile != null) {
                uri = FileProvider.getUriForFile(this,
                        "eco.yumi.fileprovider",
                        photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            }

            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            else {
                ClipData clip=
                        ClipData.newUri(getContentResolver(), "yumi.eco", uri);
                takePictureIntent.setClipData(clip);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            startActivityForResult(takePictureIntent, code);
        }

        return uri;
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
                    showExplanation(getResources().getString(R.string.StoragePermitTitle), getResources().getString(R.string.StoragePermitText), Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_CODE_ASK_PERMISSIONS);
                    Toast.makeText(this, getResources().getString(R.string.PermisionDenied), Toast.LENGTH_SHORT).show();
                }
        }
    }
}