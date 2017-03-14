package com.android.dishpatch.dover.Controller.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.android.dishpatch.dover.Controller.Fragment.Dialog.DeleteConfirmationDialog;
import com.android.dishpatch.dover.Controller.Fragment.Dialog.NewImageOrCurrentImageDialog;
import com.android.dishpatch.dover.Controller.SharedPreferences.DoverPreferences;
import com.android.dishpatch.dover.Model.DoverCentral;
import com.android.dishpatch.dover.Model.Item;
import com.android.dishpatch.dover.R;
import com.android.dishpatch.dover.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Lenovo on 5/6/2016.
 */
public class StoreEditItemFragment extends Fragment {

    private static final String MENU_ARGS= "MENU_ARGS";
    private static final String EXTRA_SAVED_RESULT = "com.dishpatch.dishpatch.StoreEditItemFragment";
    private static final String ADD_ITEM_URL = "http://insvite.com/php/dover/add_item.php";
    private static final String ADD_ITEM_IMAGE_URL = "http://insvite.com/php/dover/add_image_item.php";
    private static final String UPDATE_ITEM_URL = "http://insvite.com/php/dover/store_update_item.php";
    private static final String DELETE_ITEM_URL = "http://insvite.com/php/dover/delete_item.php";
    private static final String TAG = StoreEditItemFragment.class.getSimpleName() ;
    private static final int REQUEST_PHOTO= 1;
    private static final String DELETE_CONFIRMATION = "REQUEST_DELETE_CONFIRMATION";
    private static final String NEW_OR_EXISTING = "REQUEST_NEW_OR_EXISTING";
    private static final int REQUEST_DELETE_CONFIRMATION = 0;
    private static final int REQUEST_NEW_OR_EXISTING = 2;
    private static final int REQUEST_TAKE_PHOTO = 3;
    private static final String SAVED_MENU = "com.android.dishpatch.dishpatch.Item";
    private static final int CAMERA_PERMISSION = 4 ;
    private static final int WRITE_EXTERNAL_PERMISSION = 5;
    private static final int READ_EXTERNAL_PERMISSION = 6;


    private Item mItem;
    @NotEmpty
    private EditText mItemNameEditText;
    @NotEmpty
    private EditText mPriceEditText;

    private Switch mAvailabilitySwitch;
    private Button mSaveButton;
    private ImageButton mAddImageButton;
    private ImageView mItemImageView;
    private Boolean isSaved=false;
    private Validator mValidator;
    private Boolean isSuccesful=false;
    private Spinner mItemCategorySpinner;
    private Spinner mUnitSpinner;
    private EditText mQuantityEditText;


    private Uri mItemImageUri;
    private Uri tempItemImageUri;
    private Boolean isImageAdded=true;
    private String UUId;
    private Integer itemId =null;
    private boolean isDeleteConfirmed;
    private String category;
    private String unit;
    private Boolean isNewItem;



    private String mCurrentPhotoPath;

    public static StoreEditItemFragment newInstance(int id)
    {
        Bundle args = new Bundle();
        args.putInt(MENU_ARGS,id);

        StoreEditItemFragment fragment = new StoreEditItemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Boolean wasSaved(Intent result){
        return result.getBooleanExtra(EXTRA_SAVED_RESULT,false);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        int id = getArguments().getInt(MENU_ARGS);
        mItem = DoverCentral.get(getActivity()).getMenu(id);


        if(!Util.isNetworkAvailable(getActivity()))
        {
            Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();

        }




        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
        {
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_PERMISSION);
            }else if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){


            }

            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_PERMISSION);

            }else if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Read Permission Granted");
            }
        }



        if(mItem==null)
        {
            isNewItem=true;

            Log.v(TAG,"NEW ITEM");
        }else{
            isNewItem=false;
            Log.v(TAG,"OLD ITEM");
        }
        mValidator = new Validator(this);
        mValidator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {

                Log.v(TAG,"Validation succeeded");
                if(isNewItem)
                {
                    Log.v(TAG,"NEW ITEM");
                    addItem();


                }else if(!isNewItem) {
                    Log.v(TAG,"OLD ITEM");
                    updateItem();


                }
            }

            @Override
            public void onValidationFailed(List<ValidationError> errors) {
                Toast.makeText(getActivity(), "Validation failed", Toast.LENGTH_SHORT).show();
            }
        });

        UUId = DoverPreferences.getPrefStoreUUID(getActivity());

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_item_edit,container,false);

        mItemNameEditText = (EditText) v.findViewById(R.id.item_edit_text);
        mPriceEditText = (EditText) v.findViewById(R.id.price_edit_text);
        mSaveButton = (Button) v.findViewById(R.id.save_menu_button);
        mAvailabilitySwitch= (Switch) v.findViewById(R.id.availability_switch);
        mAddImageButton = (ImageButton) v.findViewById(R.id.add_image_button);
        mItemImageView = (ImageView) v.findViewById(R.id.item_image_view);
        mQuantityEditText = (EditText) v.findViewById(R.id.weight_edit_text);

        mAddImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager manager = getFragmentManager();
                NewImageOrCurrentImageDialog newImageOrCurrentImageDialog = NewImageOrCurrentImageDialog.newInstance();
                newImageOrCurrentImageDialog.setTargetFragment(StoreEditItemFragment.this,REQUEST_NEW_OR_EXISTING);
                newImageOrCurrentImageDialog.show(manager,NEW_OR_EXISTING);
            }
        });

        mItemCategorySpinner = (Spinner) v.findViewById(R.id.food_category_spinner);

        mUnitSpinner = (Spinner) v.findViewById(R.id.weight_unit_spinner);



        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),R.array.grocery_type,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mItemCategorySpinner.setAdapter(adapter);

        category = mItemCategorySpinner.getItemAtPosition(0).toString();

        mItemCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category= parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                unit = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter<CharSequence> mUnitAdapter = ArrayAdapter.createFromResource(getActivity(),R.array.weight_unit,android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mUnitSpinner.setAdapter(mUnitAdapter);

        unit = mUnitSpinner.getItemAtPosition(0).toString();


        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSaved=true;
                setSavedResult(isSaved);

                if(Util.isNetworkAvailable(getActivity()))
                {
                    mValidator.validate();
                }else {
                    Toast.makeText(getActivity(),R.string.internet_not_available, Toast.LENGTH_SHORT).show();
                }

            }
        });



        if(mItem !=null)
        {
            mItemNameEditText.setText(mItem.getItemName());

            String priceText = String.format("%.2f", mItem.getPrice());
            mPriceEditText.setText(priceText);
            itemId = mItem.getItemId();
            if(mItem.getPictureUrl()!=null)
            {
                if(!mItem.getPictureUrl().isEmpty()){
                    Picasso.with(getActivity()).load(mItem.getPictureUrl()).fit().centerCrop().into(mItemImageView);
                }
            }

            if(mItem.getAvailable()!=null)
            {
                mAvailabilitySwitch.setChecked(mItem.getAvailable());
            }

            Log.v(TAG,mItem.getCategory());
            Log.v(TAG,mItem.getUnit());

            mQuantityEditText.setText(mItem.getQuantity()+"");

            int catPos = adapter.getPosition(mItem.getCategory());
            Log.v(TAG,"cat pos="+catPos);

            mItemCategorySpinner.setSelection(catPos,false);

            int unitPos = mUnitAdapter.getPosition(mItem.getUnit());
            Log.v(TAG,"unit pos="+unitPos);

            mUnitSpinner.setSelection(unitPos,false);




        }



        return v;


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVED_MENU, mItem);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState!=null)
        {
            mItem = savedInstanceState.getParcelable(SAVED_MENU);

        }

    }

    private void setSavedResult(boolean b) {
        Intent data = new Intent();
        data.putExtra(EXTRA_SAVED_RESULT,b);
        getActivity().setResult(Activity.RESULT_OK,data);
    }



    private void updateItem(){
        mItem.setItemName(mItemNameEditText.getText().toString());
        mItem.setPrice(Float.parseFloat(mPriceEditText.getText().toString()));
        mItem.setAvailable(mAvailabilitySwitch.isChecked());
        Log.v(TAG,"UPDATE ITEM");

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("item_id", mItem.getItemId()+"")
                .add("item_name", mItem.getItemName())
                .add("price", mItem.getPrice()+"")
                .add("quantity",mItem.getQuantity()+"")
                .add("unit",mItem.getUnit())
                .add("category",mItem.getCategory())
                .add("availability", mItem.getAvailable()+"")
                .build();

        Request request = new Request.Builder().url(UPDATE_ITEM_URL).post(requestBody).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if(response.isSuccessful())
                {
                    Log.v(TAG,"succesfully updated");

                    if(isImageAdded){
                        Log.v(TAG,"Image have been added");
                        while(itemId ==null){
                        }
                        addImageToFirebase();
                    }

                    getActivity().finish();


                }

            }
        });


    }

    private void addItem()
    {
        mItem = new Item();
        mItem.setItemName(mItemNameEditText.getText().toString());
        mItem.setPrice(Float.parseFloat(mPriceEditText.getText().toString()));
        mItem.setAvailable(mAvailabilitySwitch.isChecked());
        mItem.setQuantity(Integer.parseInt(mQuantityEditText.getText().toString()));
        mItem.setCategory(category);
        mItem.setUnit(unit);


        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody =  new FormBody.Builder()
                .add("uuid",UUId +"")
                .add("item_name", mItem.getItemName())
                .add("category",mItem.getCategory())
                .add("price", mItem.getPrice()+"")
                .add("availability", mItem.getAvailable().toString())
                .add("quantity", String.valueOf(mItem.getQuantity()))
                .add("unit",mItem.getUnit())
                .build();

        Request request = new Request.Builder().url(ADD_ITEM_URL).post(requestBody).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                isSuccesful=false;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {



                if(response.isSuccessful())
                {
                    DoverCentral.get(getActivity()).addMenu(mItem);

                    String body = response.body().string();




                    try {
                        JSONObject jsonBody = new JSONObject(body);

                        itemId = Integer.parseInt(jsonBody.getString("id"));
                        Log.v(TAG,"Item Id ="+ itemId);
                        mItem.setItemId(itemId);
                    }catch (JSONException e){

                        Log.v(TAG,e.toString());
                    }

                }else{
                    Log.v(TAG,"failed to add");

                }
            }
        });

        while (itemId==null)
        {
        }

        if(mItemImageUri!=null)
        {


            addImageToFirebase();
        }

        DatabaseReference itemReference = FirebaseDatabase.getInstance().getReference("items");

        itemReference.child(mItem.getItemId()+"").setValue(mItem.getAvailable()+"");





        getActivity().finish();

    }

    private void deleteItem()
    {

        Log.v(TAG,"delete item = "+mItem.getItemId());
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder().add("item_id", mItem.getItemId()+"").build();

        Request request = new Request.Builder().url(DELETE_ITEM_URL).post(requestBody).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful())
                {
                    Log.v(TAG,response.body().string());
                    getActivity().finish();
                }
            }
        });

        DatabaseReference toDeleteReference = FirebaseDatabase.getInstance().getReference("items");

        toDeleteReference.child(mItem.getItemId()+"").setValue(null);
    }

    private void addImageToFirebase()
    {




        if(mItemImageUri !=null)
        {
            FirebaseStorage storage = FirebaseStorage.getInstance();

            final StorageReference reference = storage.getReferenceFromUrl("gs://dover-fd9a6.appspot.com");

            StorageReference foodImageRef = reference.child("stores/items/"+ UUId +"-"+mItem.getItemId()+"-"+ mItem.getItemName());
            UploadTask uploadTask = foodImageRef.putFile(mItemImageUri);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Uri downloadUri = taskSnapshot.getDownloadUrl();

                    OkHttpClient client = new OkHttpClient();
                    Log.v(TAG,downloadUri.toString());
                    RequestBody requestBody = new FormBody.Builder()
                            .add("item_id", itemId +"")
                            .add("image_url",downloadUri.toString())
                            .build();

                    Log.v(TAG,requestBody.toString());

                    Request request = new Request.Builder().url(ADD_ITEM_IMAGE_URL).post(requestBody).build();
                    Call call = client.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {

                            if(response.isSuccessful())
                            {
                                Log.v(TAG,response.body().string());
                            }


                        }
                    });







                }
            });
        }
    }


    private File createImageFile() throws IOException{

        File externalFilesDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_"+".jpg";



        // Save a file: path for use with ACTION_VIEW intents

        if(externalFilesDir==null)
        {
            return null;
        }
        return new File(externalFilesDir,imageFileName);


    }


    private void takePhoto(){

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(takePictureIntent.resolveActivity(getActivity().getPackageManager())!=null)
        {
            File photoFile =null;
            
            try {
                photoFile = createImageFile();
            }catch (IOException e){

                Toast.makeText(getActivity(), "Unable To Create File", Toast.LENGTH_SHORT).show();
            }

            if(photoFile!=null)
            {
                Log.v(TAG,photoFile.getPath());
                tempItemImageUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,mItemImageUri);


                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
                {
                    if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                    {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
                    }else if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

                        startActivityForResult(takePictureIntent,REQUEST_TAKE_PHOTO);

                    }
                }else {
                    startActivityForResult(takePictureIntent,REQUEST_TAKE_PHOTO);
                }
            }

            
        }


    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onCreateOptionsMenu(android.view.Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_edit_menu,menu);

        MenuItem deleteItem = menu.findItem(R.id.menu_delete_food);
        deleteItem.setEnabled(mItem !=null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_PERMISSION:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG,"Permission granted");
                    takePhoto();
                }
                break;
            case WRITE_EXTERNAL_PERMISSION:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Log.d(TAG,"Permission Storage Granted");
                }
                break;
            case READ_EXTERNAL_PERMISSION:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Log.d(TAG,"Write Permission Storage Granted");
                }
                break;

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                setSavedResult(isSaved);
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            case R.id.menu_delete_food:
                FragmentManager manager = getFragmentManager();
                DeleteConfirmationDialog deleteConfirmationDialog = DeleteConfirmationDialog.newInstance();
                deleteConfirmationDialog.setTargetFragment(StoreEditItemFragment.this,REQUEST_DELETE_CONFIRMATION);
                deleteConfirmationDialog.show(manager,DELETE_CONFIRMATION);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode==Activity.RESULT_OK)
        {
            if(requestCode==REQUEST_PHOTO)
            {
                mItemImageUri = data.getData();
                Log.v(TAG,"Gallery : "+mItemImageUri.getPath());
                Picasso.with(getActivity()).load(mItemImageUri).fit().centerCrop().into(mItemImageView);
                isImageAdded=true;
            }else if(requestCode==REQUEST_DELETE_CONFIRMATION) {
                isDeleteConfirmed = data.getBooleanExtra(DeleteConfirmationDialog.EXTRA_CONFIRMATION,false);
                Log.v(TAG,isDeleteConfirmed+"");

                if(isDeleteConfirmed){
                    deleteItem();
                }
            }else if(requestCode==REQUEST_NEW_OR_EXISTING){
                boolean isNew;

                isNew = data.getBooleanExtra(NewImageOrCurrentImageDialog.EXTRA_NEW_OR_EXISTING,false);
                if(isNew){
                    //new image
                    takePhoto();



                }else{
                    //existing image
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent,REQUEST_PHOTO);


                }
            }else if(requestCode==REQUEST_TAKE_PHOTO){

                mItemImageUri = data.getData();



//                Log.v(TAG,"Camera : "+mItemImageUri.getPath());
//
//
//                Uri uri = Uri.fromFile(new File(mItemImageUri.getPath()));
//
//                Log.v(TAG,"Encoded :"+uri.toString());

                //Log.v(TAG,BitmapFactory.decodeFile(mItemImageUri.getPath()).toString());

                //Log.v(TAG,mItemImageUri.getPath());
                Picasso.with(getActivity()).load(mItemImageUri).fit().centerCrop().into(mItemImageView);
            }
        }
    }


}
