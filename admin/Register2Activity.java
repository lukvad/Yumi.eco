package com.lukvad.scooter;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.http.Url;

public class Register2Activity extends AppCompatActivity {
    private EditText  nId, nAddress1, nAdress2, nPhone ;
    private Button nRegister , bCapture;
    private FirebaseAuth nAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private static String  address="", id="";
    protected String user_id="", email="", password="", firstname="", surname="", phone="", url="";
    protected DatabaseReference current_user_db;
    ImageView imageView;
    static  final  int CAM_REQUEST = 1;
    private StorageReference mStorage;
    String mCurrentPhotoPath, mFirebasePath;
    File photoFile = null;
    Uri photoURI = null;
    Boolean fAdded=false;
    private Target target;
    private ProgressBar progressBar;
    private static final int REQUEST_CODE_ASK_PERMISSIONS=123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

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
        RelativeLayout relativeLayout = findViewById(R.id.relativeLayout);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        nAuth = FirebaseAuth.getInstance();
        nAddress1 = findViewById(R.id.address1);
        nPhone = findViewById(R.id.phone);
        nAdress2 = findViewById(R.id.address2);
        nId = findViewById(R.id.idnumber);
        nRegister = findViewById(R.id.register);
        bCapture =findViewById(R.id.buttonCapture);
        progressBar = new ProgressBar(Register2Activity.this,null,android.R.attr.progressBarStyleLarge);
        progressBar.setVisibility(View.INVISIBLE);
        relativeLayout.addView(progressBar,params);
        mStorage = FirebaseStorage.getInstance().getReference();
        final ImagePopup imagePopup = new ImagePopup(this);
        imagePopup.setWindowHeight(800); // Optional
        imagePopup.setWindowWidth(800); // Optional
        imagePopup.setBackgroundColor(Color.BLACK);  // Optional
        imagePopup.setFullScreen(true); // Optional
        imagePopup.setHideCloseIcon(true);  // Optional
        imagePopup.setImageOnClickClose(true);  // Optional
        target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Bitmap bitmap1 = bitmap;
                Drawable image = new BitmapDrawable(getResources(), bitmap1);
                imagePopup.initiatePopup(image);
                fAdded=true;
                bCapture.setText(getResources().getString(R.string.see));
                bCapture.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ok, 0, 0, 0);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {}

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };


        bCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fAdded)
                    imagePopup.viewPopup();
                else {
                    dispatchTakePictureIntent(view);
                }
            }
        });

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    if (!user.isEmailVerified()) {
                        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                        final StorageReference imagesRef = storageRef.child("Documents/DriverLicense").child("license+" + email);
                        final UploadTask uploadTask = imagesRef.putFile(photoURI);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(Register2Activity.this, "uploading image failed", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                user_id = user.getUid();
                                user.sendEmailVerification();
                                url = taskSnapshot.getDownloadUrl().toString();
                                current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
                                current_user_db.child("firstname").setValue(firstname);
                                current_user_db.child("surname").setValue(surname);
                                current_user_db.child("idno").setValue(id);
                                current_user_db.child("email").setValue(email);
                                current_user_db.child("phone").setValue(phone);
                                current_user_db.child("address").setValue(address);
                                current_user_db.child("url").setValue(url);
                                Intent intent = new Intent(Register2Activity.this, VerificationActivity.class);
                                intent.putExtra(user.getEmail(),1);
                                progressBar.setVisibility(View.INVISIBLE);
                                startActivity(intent);
                                finish();
                            }
                        } );
                    }
                    return;
                }
            }
        };
        if(savedInstanceState==null) {
            Bundle register = getIntent().getExtras();
            if (register != null) {
                email = register.getString("email");
                password = register.getString("password");
                firstname = register.getString("firstname");
                surname= register.getString("surname");
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
            }
        }

        nRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                address = nAddress1.getText().toString()+nAdress2.getText().toString();
                phone = nPhone.getText().toString();
                id = nId.getText().toString();
                if((fAdded)&&(!address.equals(""))&&(!id.equals(""))&&(!phone.equals(""))) {
                    progressBar.setVisibility(View.VISIBLE);
                    nRegister.setClickable(false);
                    nAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Register2Activity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(Register2Activity.this, getResources().getString(R.string.signupError), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else Toast.makeText(Register2Activity.this, getResources().getString(R.string.signupBad),Toast.LENGTH_LONG).show();
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
    private File createImageFile() throws IOException {
        // Create an image file name

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
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
        if(requestCode == CAM_REQUEST && resultCode == RESULT_OK){
            if (requestCode == CAM_REQUEST && resultCode == RESULT_OK) {
                Picasso.with(Register2Activity.this).load(photoURI).into(target);}
        }
    }

    private void dispatchTakePictureIntent(View view) {

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("file", ex.toString());
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.lukvad.scooter.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAM_REQUEST);
            }
        }
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
                    Toast.makeText(this, getResources().getString(R.string.PermisionDenied), Toast.LENGTH_SHORT).show();
                }
        }
    }
}