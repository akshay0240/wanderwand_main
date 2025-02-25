package com.example.wanderwand;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.wanderwand.login.LoginActivity;
import com.example.wanderwand.utilities.ShareContactActivity;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import objects.City;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import utils.CircleImageView;
import utils.TravelmateSnackbars;
import utils.Utils;

import static android.view.View.GONE;
import static com.google.android.flexbox.FlexDirection.ROW;
import static com.google.android.flexbox.JustifyContent.FLEX_START;
import static utils.Constants.API_LINK_V2;
import static utils.Constants.CLOUDINARY_API_KEY;
import static utils.Constants.CLOUDINARY_API_SECRET;
import static utils.Constants.CLOUDINARY_CLOUD_NAME;
import static utils.Constants.OTHER_USER_ID;
import static utils.Constants.SHARE_PROFILE_URI;
import static utils.Constants.SHARE_PROFILE_USER_ID_QUERY;
import static utils.Constants.USER_DATE_JOINED;
import static utils.Constants.USER_EMAIL;
import static utils.Constants.USER_ID;
import static utils.Constants.USER_IMAGE;
import static utils.Constants.USER_NAME;
import static utils.Constants.USER_STATUS;
import static utils.Constants.USER_TOKEN;
import static utils.Constants.VERIFICATION_REQUEST_CODE;
import static utils.DateUtils.getDate;
import static utils.DateUtils.rfc3339ToMills;

public class ProfileActivity extends AppCompatActivity implements TravelmateSnackbars {
    @BindView(R.id.horizontalProgressBar)
    ProgressBar horizontalProgressBar;
    @BindView(R.id.display_image)
    CircleImageView displayImage;
    @BindView(R.id.change_image)
    CircleImageView changeImage;
    @BindView(R.id.display_name)
    EditText displayName;
    @BindView(R.id.display_email)
    TextView emailId;
    @BindView(R.id.is_email_verified)
    ImageView isVerified;
    @BindView(R.id.display_joining_date)
    TextView joiningDate;
    @BindView(R.id.display_status)
    EditText displayStatus;
    @BindView(R.id.ib_edit_display_name)
    ImageButton editDisplayName;
    @BindView(R.id.ib_edit_display_status)
    ImageButton editDisplayStatus;
    @BindView(R.id.animation_view)
    LottieAnimationView animationView;
    @BindView(R.id.status_progress_bar)
    ProgressBar statusProgressBar;
    @BindView(R.id.name_progress_bar)
    ProgressBar nameProgressBar;
    @BindView(R.id.layout)
    ConstraintLayout layout;
    @BindView(R.id.status_character_count)
    TextView characterCount;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.cities_travelled_text)
    TextView citiesTravelledHeading;

    private String mToken;
    private Handler mHandler;
    private String mUserStatus;
    private List<City> mCities = new ArrayList<>();
    // Flag for checking the current drawable of the ImageButton
    private boolean mFlagForDrawable = true;
    private SharedPreferences mSharedPreferences;
    private Menu mOptionsMenu;
    //request code for picked image
    private static final int RESULT_PICK_IMAGE = 1;
    //request code for cropped image
    private static final int RESULT_CROP_IMAGE = 2;
    private static final String LOG_TAG = ProfileActivity.class.getSimpleName();
    private String mProfileImageUrl;
    private CitiesTravelledAdapter mCitiesAdapter;
    private AlertDialog mDialog;
    private boolean mIsVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        animationView.setVisibility(View.GONE);
        mHandler = new Handler(Looper.getMainLooper());
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mToken = mSharedPreferences.getString(USER_TOKEN, null);

        Intent intent = getIntent();
        String id = intent.getStringExtra(OTHER_USER_ID);

        if (id != null) {
            editDisplayName.setVisibility(View.INVISIBLE);
            updateOptionsMenu();
        }

        boolean isNetworkConnected = Utils.isNetworkConnected(this);
        if (isNetworkConnected) {
            getUserDetails(id);
            getTravelledCities();
        } else {
            fillProfileOffline();
        }

        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        isVerified.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
            if (!mIsVerified) {
                builder.setTitle(R.string.email_not_verified);
                builder.setPositiveButton(R.string.verify_now, (dialogInterface, i) -> {
                    sendVerificationEmail();
                });
                builder.setNegativeButton(R.string.later, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
            } else {
                builder.setTitle(R.string.text_email_verified);
                builder.setPositiveButton(R.string.positive_button, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
            }
            builder.create().show();

        });

        editDisplayName.setOnClickListener(v -> {
            if (mFlagForDrawable) {
                mFlagForDrawable = false;
                editDisplayName.setImageDrawable(getDrawable(R.drawable.ic_check_black_24dp));
                displayName.setFocusableInTouchMode(true);
                displayName.setCursorVisible(true);
                displayName.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                Objects.requireNonNull(imm).showSoftInput(displayName, InputMethodManager.SHOW_IMPLICIT);
            } else {
                mFlagForDrawable = true;
                editDisplayName.setImageDrawable(getDrawable(R.drawable.ic_edit_black_24dp));
                displayName.setFocusableInTouchMode(false);
                displayName.setCursorVisible(false);
                setUserDetails();
            }
        });

        editDisplayStatus.setOnClickListener(v -> {
            if (mFlagForDrawable) {
                mFlagForDrawable = false;
                editDisplayStatus.setImageDrawable(getDrawable(R.drawable.ic_check_black_24dp));
                displayStatus.setFocusableInTouchMode(true);
                displayStatus.setCursorVisible(true);
                displayStatus.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                Objects.requireNonNull(imm).showSoftInput(displayStatus, InputMethodManager.SHOW_IMPLICIT);
                characterCount.setVisibility(View.VISIBLE);
            } else {
                mFlagForDrawable = true;
                editDisplayStatus.setImageDrawable(getDrawable(R.drawable.ic_edit_black_24dp));
                displayStatus.setFocusableInTouchMode(false);
                displayStatus.setCursorVisible(false);
                setUserStatus();
                characterCount.setVisibility(View.GONE);
            }
        });
        displayStatus.addTextChangedListener(mCountCharacters);
        changeImage.setOnClickListener(v -> {
            Intent galleryIntent = new Intent(
                    Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Intent removeIntent = new Intent(Intent.ACTION_DELETE);
            Intent chooserIntent = Intent.createChooser(removeIntent, getString(R.string.choose_an_option));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{galleryIntent});
            startActivityForResult(chooserIntent, RESULT_PICK_IMAGE);
        });

        //open profile image when clicked on it
        displayImage.setOnClickListener(v -> {
            String imageUri = mSharedPreferences.getString(USER_IMAGE, null);
            String fullname = mSharedPreferences.getString(USER_NAME, null);
            Intent fullScreenIntent = FullScreenImage.getStartIntent(ProfileActivity.this,
                    imageUri, fullname);
            startActivity(fullScreenIntent);
        });
    }


    public void sendVerificationEmail() {
        String uri;
        uri = API_LINK_V2 + "generate-verification-code";
        Log.v("EXECUTING", uri);
        horizontalProgressBar.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
                .header("Authorization", "Token " + mToken)
                .url(uri)
                .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
                mHandler.post(() -> {
                    networkError();
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                mHandler.post(() -> {
                    horizontalProgressBar.setVisibility(View.GONE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    if (response.body() != null && response.isSuccessful()) {
                        try {
                            final String res = Objects.requireNonNull(response.body()).string();
                            if (res.equals("\"Verification code sent\"")) {
                                Toast.makeText(ProfileActivity.this,
                                        "OTP sent on registered email !", Toast.LENGTH_SHORT).show();
                                Intent verifyIntent = new Intent(ProfileActivity.this, VerifyEmailActivity.class);
                                startActivityForResult(verifyIntent, VERIFICATION_REQUEST_CODE);
                            } else {
                                Toast.makeText(ProfileActivity.this,
                                        "There was some error !", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(ProfileActivity.this,
                                    "There was some error. Please, try again !", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

    private final TextWatcher mCountCharacters = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            characterCount.setText(String.valueOf(s.length()) + getString(R.string.status_character_limit));
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VERIFICATION_REQUEST_CODE) {
            recreate();
        }

        if (data == null)
            return;

        //After user has picked the image
        if (requestCode == RESULT_PICK_IMAGE && data.hasExtra("remove_image")) {
            deleteProfilePicture();
        } else if (requestCode == RESULT_PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK) {
            Uri selectedImage = data.getData();
            //startCropIntent(selectedImage);
            CropImage.activity(selectedImage).start(this);
        }
        //After user has cropped the image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri croppedImage = result.getUri();
                getUrlFromCloudinary(croppedImage);
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View view = getCurrentFocus();
            if (view instanceof EditText) {
                Rect outRect = new Rect();
                view.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    view.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    Objects.requireNonNull(imm).hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                finish();
                return true;
            case R.id.action_sign_out:
                signOut();
                return true;
            case R.id.action_share_profile:
                shareProfile();
                return true;
            case R.id.action_qrcode_scan:
                Intent intent;
                intent = ShareContactActivity.getStartIntent(ProfileActivity.this);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void signOut() {
        //set AlertDialog before signout
        ContextThemeWrapper crt = new ContextThemeWrapper(this, R.style.AlertDialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(crt);
        builder.setMessage(R.string.signout_message)
                .setPositiveButton(R.string.positive_button,
                        (dialog, which) -> {
                            mSharedPreferences
                                    .edit()
                                    .putString(USER_TOKEN, null)
                                    .apply();
                            Intent i = LoginActivity.getStartIntent(ProfileActivity.this);
                            startActivity(i);
                            finish();
                        })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> {

                        });
        builder.create().show();
    }

    private void deleteProfilePicture() {
        //set AlertDialog before marking All as read
        ContextThemeWrapper crt = new ContextThemeWrapper(this, R.style.AlertDialog);
        AlertDialog.Builder builder = new AlertDialog.Builder(crt);
        builder.setMessage(R.string.remove_profile_picture)
                .setPositiveButton(R.string.positive_button,
                        (dialog, which) -> {
                            mDialog = new MaterialAlertDialogBuilder(ProfileActivity.this)
                                    .setTitle(R.string.app_name)
                                    .setMessage(R.string.progress_wait)
                                    .setCancelable(true)
                                    .show();

                            String uri;
                            uri = API_LINK_V2 + "remove-profile-image";
                            Log.v("EXECUTING", uri);

                            //Set up client
                            OkHttpClient client = new OkHttpClient();
                            //Execute request
                            Request request = new Request.Builder()
                                    .header("Authorization", "Token " + mToken)
                                    .url(uri)
                                    .build();
                            //Setup callback
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    Log.e("Request Failed", "Message : " + e.getMessage());
                                }

                                @Override
                                public void onResponse(Call call, final Response response) throws IOException {
                                    final String res = Objects.requireNonNull(response.body()).string();
                                    mHandler.post(() -> {
                                        if (response.isSuccessful()) {
                                            TravelmateSnackbars.createSnackBar(findViewById(R.id.layout), res,
                                                    Snackbar.LENGTH_SHORT).show();
                                            Picasso.get().load(R.drawable.default_user_icon)
                                                    .into(displayImage);
                                        } else {
                                            TravelmateSnackbars.createSnackBar(findViewById(R.id.layout), res,
                                                    Snackbar.LENGTH_LONG).show();
                                        }
                                    });
                                    mDialog.dismiss();
                                }
                            });

                        })
                .setNegativeButton(android.R.string.cancel,
                        (dialog, which) -> {

                        });
        builder.create().show();
    }


    private void getUserDetails(final String userId) {

        String uri;
        if (userId != null)
            uri = API_LINK_V2 + "get-user/" + userId;
        else
            uri = API_LINK_V2 + "get-user";
        Log.v("EXECUTING", uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        Request request = new Request.Builder()
                .header("Authorization", "Token " + mToken)
                .url(uri)
                .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
                mHandler.post(() -> networkError());
            }

            @Override
            public void onResponse(Call call, final Response response) {
                mHandler.post(() -> {
                    if (response.isSuccessful() && response.body() != null) {

                        try {
                            final String res = Objects.requireNonNull(response.body()).string();
                            JSONObject object = new JSONObject(res);
                            String userName = object.getString("username");
                            String firstName = object.getString("first_name");
                            String lastName = object.getString("last_name");
                            int id = object.getInt("id");
                            String imageURL = object.getString("image");
                            String dateJoined = object.getString("date_joined");
                            String status = object.getString("status");
                            boolean verified = object.getBoolean("is_verified");
                            String fullName = firstName + " " + lastName;
                            Long dateTime = rfc3339ToMills(dateJoined);
                            String date = getDate(dateTime);

                            fillProfileInfo(fullName, userName, imageURL, date, status);

                            mIsVerified = verified;

                            if (verified) {
                                isVerified.setImageDrawable(getDrawable(R.drawable.ic_done_black_24dp));
                                isVerified.setColorFilter(Color.GREEN);
                            } else {
                                isVerified.setImageDrawable(getDrawable(R.drawable.ic_close_black_24dp));
                                isVerified.setColorFilter(Color.RED);
                            }

                            if (userId == null) {
                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                editor.putString(USER_NAME, fullName);
                                editor.putString(USER_EMAIL, userName);
                                editor.putString(USER_DATE_JOINED, date);
                                editor.putString(USER_IMAGE, imageURL);
                                editor.putString(USER_ID, String.valueOf(id));
                                editor.putString(USER_STATUS, status);
                                editor.apply();
                            } else {
                                updateOptionsMenu();
                            }

                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                            networkError();
                            Log.e("ERROR : ", "Message : " + e.getMessage());
                        }
                    } else {
                        networkError();
                    }
                });
            }
        });
    }

    private void setUserDetails() {
        runOnUiThread(() -> {
            displayName.setVisibility(View.INVISIBLE);
            nameProgressBar.setVisibility(View.VISIBLE);
        });

        // to update user name
        String uri = API_LINK_V2 + "update-user-details";
        Log.v("EXECUTING", uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();

        // Add form parameters
        String fullName = String.valueOf(displayName.getText());
        Log.i("Fullname: ", fullName);

        try {
            String firstName = fullName.substring(0, fullName.indexOf(' '));
            String lastName = fullName.substring(fullName.indexOf(' ') + 1);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("firstname", firstName)
                    .addFormDataPart("lastname", lastName)
                    .build();

            // Create a http request object.
            Request request = new Request.Builder()
                    .header("Authorization", "Token " + mToken)
                    .url(uri)
                    .post(requestBody)
                    .build();

            // Create a new Call object with post method.
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("Request Failed", "Message : " + e.getMessage());
                    mHandler.post(() -> networkError());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String res = Objects.requireNonNull(response.body()).string();
                    mHandler.post(() -> {
                        if (response.isSuccessful()) {
                            TravelmateSnackbars.createSnackBar(findViewById(R.id.layout),
                                    R.string.name_updated, Snackbar.LENGTH_SHORT).show();
                            mSharedPreferences.edit().putString(USER_NAME, fullName).apply();
                        } else {
                            TravelmateSnackbars.createSnackBar(findViewById(R.id.layout), res,
                                    Snackbar.LENGTH_LONG).show();
                            networkError();
                        }
                    });
                    runOnUiThread(() -> {
                        nameProgressBar.setVisibility(View.GONE);
                        displayName.setVisibility(View.VISIBLE);
                    });
                }
            });
        } catch (StringIndexOutOfBoundsException e) {
            displayName.setVisibility(View.VISIBLE);
            nameProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Introduce atleast two names.", Toast.LENGTH_LONG).show();

        }

    }

    private void setUserStatus() {
        runOnUiThread(() -> {
            displayStatus.setVisibility(View.INVISIBLE);
            statusProgressBar.setVisibility(View.VISIBLE);
        });

        // to update user name
        String uri;
        //Set up client
        OkHttpClient client = new OkHttpClient();
        Request request;

        mUserStatus = String.valueOf(displayStatus.getText());
        if (mUserStatus.equals("")) {
            uri = API_LINK_V2 + "remove-user-status";

            request = new Request.Builder()
                    .header("Authorization", "Token " + mToken)
                    .url(uri)
                    .build();
        } else {
            uri = API_LINK_V2 + "update-user-status";

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("status", mUserStatus)
                    .build();

            request = new Request.Builder()
                    .header("Authorization", "Token " + mToken)
                    .url(uri)
                    .post(requestBody)
                    .build();
        }
        Log.v("EXECUTING", uri);
        // Create a new Call object
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
                mHandler.post(() -> networkError());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String res = Objects.requireNonNull(response.body()).string();
                mHandler.post(() -> {
                    if (response.isSuccessful()) {
                        TravelmateSnackbars.createSnackBar(findViewById(R.id.layout),
                                R.string.status_updated, Snackbar.LENGTH_SHORT).show();
                        mSharedPreferences.edit().putString(USER_STATUS, mUserStatus).apply();
                        displayStatus.setText(mUserStatus);

                    } else {
                        TravelmateSnackbars.createSnackBar(findViewById(R.id.layout), res,
                                Snackbar.LENGTH_LONG).show();
                        networkError();
                    }
                });
                runOnUiThread(() -> {
                    statusProgressBar.setVisibility(View.GONE);
                    displayStatus.setVisibility(View.VISIBLE);
                });
            }
        });
    }


    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, ProfileActivity.class);
        return intent;
    }

    public static Intent getStartIntent(Context context, String userId) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(OTHER_USER_ID, userId);
        return intent;
    }

    private void fillProfileInfo(String fullName, String email, String imageURL,
                                 String dateJoined, String status) {
        displayName.setText(fullName);
        emailId.setText(email);
        joiningDate.setText(String.format(getString(R.string.text_joining_date), dateJoined));
        Picasso.get().load(imageURL).placeholder(R.drawable.default_user_icon)
                .error(R.drawable.default_user_icon).into(displayImage);
        setTitle(fullName);

        if (status != null && !status.equals("null")) {
            displayStatus.setText(status);
        }
    }

    /**
     * Fill profile with user information from SharedPreferences when user is offline
     */
    private void fillProfileOffline() {
        //Get user information from SharedPreferences
        String name = mSharedPreferences.getString(USER_NAME, null);
        String email = mSharedPreferences.getString(USER_EMAIL, null);
        String dateJoined = mSharedPreferences.getString(USER_DATE_JOINED, null);
        String status = mSharedPreferences.getString(USER_STATUS, null);

        //Change Views Visibility in offline mode
        displayImage.setVisibility(View.VISIBLE);
        editDisplayName.setVisibility(View.INVISIBLE);
        editDisplayStatus.setVisibility(View.INVISIBLE);
        changeImage.setVisibility(View.INVISIBLE);
        citiesTravelledHeading.setVisibility(View.INVISIBLE);

        displayName.setText(name);
        emailId.setText(email);
        joiningDate.setText(String.format(getString(R.string.text_joining_date), dateJoined));

        Picasso.get()
                .load(R.drawable.default_user_icon)
                .placeholder(R.drawable.default_user_icon)
                .into(displayImage);

        if (status != null && !status.equals("null")) {
            displayStatus.setText(status);
        }
    }

    /**
     * Method to get URL for image using Cloudinary
     *
     * @param croppedImage Uri of cropped image
     **/
    private void getUrlFromCloudinary(Uri croppedImage) {
        Map config = new HashMap();
        config.put("cloud_name", CLOUDINARY_CLOUD_NAME);
        config.put("api_key", CLOUDINARY_API_KEY);
        config.put("api_secret", CLOUDINARY_API_SECRET);
        MediaManager.init(this, config);

        mHandler.post(() -> MediaManager.get().upload(croppedImage).callback(new UploadCallback() {
            @Override
            public void onStart(String requestId) {

            }

            @Override
            public void onSuccess(String requestId, Map resultData) {
                Picasso.get()
                        .load(croppedImage)
                        .error(R.drawable.default_user_icon)
                        .into(displayImage);

                mSharedPreferences.edit().putString(USER_IMAGE, croppedImage.toString()).apply();

                TravelmateSnackbars.createSnackBar(findViewById(R.id.layout), R.string.profile_picture_updated,
                        Snackbar.LENGTH_SHORT).show();

                mProfileImageUrl = resultData.get("url").toString();
                sendURLtoServer(mProfileImageUrl);
            }

            @Override
            public void onError(String requestId, ErrorInfo error) {
                Toast.makeText(ProfileActivity.this, getString(R.string.toast_upload_picture_issue),
                        Toast.LENGTH_SHORT).show();
                Log.e(LOG_TAG, "error uploading to Cloudinary");
            }

            @Override
            public void onReschedule(String requestId, ErrorInfo error) {
                Log.e(LOG_TAG, error.toString());
            }

            @Override
            public void onProgress(String requestId, long bytes, long totalBytes) {

            }
        }).dispatch());
    }

    /**
     * Method for sending URL to server
     *
     * @param imageUrl - Url of image obtained from
     *                 Cloudinary cloud(passed as string)
     */
    private void sendURLtoServer(String imageUrl) {

        String uri = API_LINK_V2 + "update-profile-image";
        //Set up client
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("profile_image_url", imageUrl)
                .build();

        // Create a http request object.
        Request request = new Request.Builder()
                .header("Authorization", "Token " + mToken)
                .url(uri)
                .post(requestBody)
                .build();

        // Create a new Call object with post method.
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Request Failed", "Message : " + e.getMessage());
                mHandler.post(() -> networkError());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String res = Objects.requireNonNull(response.body()).string();
                mHandler.post(() -> {
                    if (response.isSuccessful()) {
                        Log.i(LOG_TAG, "Upload to server successful!");
                    } else {
                        TravelmateSnackbars.createSnackBar(findViewById(R.id.layout), res,
                                Snackbar.LENGTH_LONG).show();
                    }
                });

            }
        });
    }


    /**
     * Fetches list of all cities user has travelled
     */
    private void getTravelledCities() {

        animationView.setVisibility(View.VISIBLE);
        Handler handler = new Handler(Looper.getMainLooper());

        String uri = API_LINK_V2 + "get-visited-city";
        Log.v("EXECUTING", uri);

        //Set up client
        OkHttpClient client = new OkHttpClient();
        //Execute request
        final Request request = new Request.Builder()
                .header("Authorization", "Token " + mToken)
                .url(uri)
                .build();
        //Setup callback
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                Log.e("Request Failed", "Message : " + e.getMessage());
                // handler.post(() -> networkError());
            }

            @Override
            public void onResponse(Call call, final Response response) {

                handler.post(() -> {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONArray arr;
                        try {
                            final String res = response.body().string();
                            Log.v("Response for cities is ", res);
                            arr = new JSONArray(res);
                            for (int i = 0; i < arr.length(); i++) {
                                String id = arr.getJSONObject(i).getString("id");
                                String name = arr.getJSONObject(i).getString("city_name");
                                String image = arr.getJSONObject(i).getString("image");
                                mCities.add(new City(id, name, image));
                            }
                            //display trips only if there exists at least one trip
                            //else hide the view
                            if (!mCities.isEmpty()) {
                                // Specify a layout for RecyclerView
                                // Create a horizontal RecyclerView
                                FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(ProfileActivity.this);
                                layoutManager.setFlexDirection(ROW);
                                layoutManager.setJustifyContent(FLEX_START);
                                recyclerView.setLayoutManager(layoutManager);
                                mCitiesAdapter = new CitiesTravelledAdapter(ProfileActivity.this, mCities);
                                recyclerView.setAdapter(mCitiesAdapter);
                            } else {
                                citiesTravelledHeading.setVisibility(GONE);
                            }
                            displayImage.setVisibility(View.VISIBLE);
                            animationView.setVisibility(View.GONE);
                        } catch (JSONException | IOException | NullPointerException e) {
                            e.printStackTrace();
                            Log.e("ERROR", "Message : " + e.getMessage());
                            networkError();
                        }
                    } else {
                        networkError();
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        mOptionsMenu = menu;
        return true;
    }

    private void updateOptionsMenu() {
        if (mOptionsMenu != null) {
            MenuItem item = mOptionsMenu.findItem(R.id.action_share_profile);
            item.setVisible(false);
            MenuItem qrItem = mOptionsMenu.findItem(R.id.action_qrcode_scan);
            qrItem.setVisible(false);
        }
    }

    /**
     * Plays the network lost animation in the view
     */
    private void networkError() {
        layout.setVisibility(View.INVISIBLE);
        animationView.setVisibility(View.VISIBLE);
        animationView.setAnimation(R.raw.network_lost);
        animationView.playAnimation();
    }

    private void shareProfile() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        Uri profileURI = Uri.parse(SHARE_PROFILE_URI)
                .buildUpon()
                .appendQueryParameter(SHARE_PROFILE_USER_ID_QUERY, mSharedPreferences.getString(USER_ID, null))
                .build();

        Log.v("profile url", profileURI + "");

        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_profile_text) + " " + profileURI);
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.share_chooser)));
        } catch (android.content.ActivityNotFoundException ex) {
            TravelmateSnackbars.createSnackBar(findViewById(R.id.layout), R.string.snackbar_no_share_app,
                    Snackbar.LENGTH_LONG).show();
        }
    }
}