package mugambbo.github.com.medmanager.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import mugambbo.github.com.medmanager.R;
import mugambbo.github.com.medmanager.auth.SignInActivity;
import mugambbo.github.com.medmanager.util.SharedPreferencesUtil;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private TextView mName;
    private TextInputEditText mEmail, mPassword;
    private Button mSaveInfo;
    private Uri mProfilePhotoUri = Uri.EMPTY;
    private ProgressBar mUpdateProgress;
    private RelativeLayout mRelPhotoRootView;
    private ImageView mUserPhoto;
    private int GALLERY_REQUEST = 12;
    public static final Uri PROFILE_UPDATED_URI = Uri.parse("com.github.mugambbo.medmanager.home.PROFILE_UPDATED");

    public ProfileFragment() {
        // Required empty public constructor
    }


    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        mName = rootView.findViewById(R.id.et_profile_display_name);
        mEmail = (TextInputEditText) rootView.findViewById(R.id.et_profile_email);
        mPassword = (TextInputEditText) rootView.findViewById(R.id.et_profile_password);
        mSaveInfo = (Button) rootView.findViewById(R.id.btn_profile_save);
        mUpdateProgress = (ProgressBar) rootView.findViewById(R.id.pb_profile_update_progress);
        mRelPhotoRootView = (RelativeLayout) rootView.findViewById(R.id.rel_image_layout);
        mUserPhoto = (ImageView) rootView.findViewById(R.id.iv_user_photo);
        mEmail.requestFocus();
        mSaveInfo.setOnClickListener(this);
        mRelPhotoRootView.setOnClickListener(this);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            mName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
            mEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            mPassword.setText(SharedPreferencesUtil.getInstance(getContext()).getString(SignInActivity.USER_PASSWORD_KEY, ""));
            mProfilePhotoUri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
            Glide.with(getContext()).load(mProfilePhotoUri).apply(RequestOptions.centerCropTransform().error(R.drawable.ic_profile_placeholder)).into(mUserPhoto);
        } else {
            Toast.makeText(getContext(), "Unable to retrieve user info", Toast.LENGTH_LONG).show();
            NavUtils.navigateUpFromSameTask(getActivity());
        }
        return rootView;
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void launchImageChooser (){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        if (photoPickerIntent.resolveActivity(getActivity().getPackageManager()) != null){
            startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST){
                mProfilePhotoUri = data.getData();
                Glide.with(getContext()).load(mProfilePhotoUri).apply(RequestOptions.centerCropTransform().error(R.drawable.ic_profile_placeholder)).into(mUserPhoto);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.btn_profile_save:
                //save
                saveUserInfoToCloud();
                break;
            case R.id.rel_image_layout:
                launchImageChooser();
                break;
        }
    }

    private void saveUserInfoToCloud(){
        final String name = mName.getText().toString();
        String email = mEmail.getText().toString();
        final String password = mPassword.getText().toString();

        if (TextUtils.isEmpty(name.trim()) || TextUtils.isEmpty(email.trim()) || TextUtils.isEmpty(password.trim())){
            Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_LONG).show();
        } else {
            mName.setEnabled(false);
            mEmail.setEnabled(false);
            mPassword.setEnabled(false);
            mSaveInfo.setEnabled(false);
            mUpdateProgress.setVisibility(View.VISIBLE);

            assert FirebaseAuth.getInstance().getCurrentUser() != null;
            FirebaseAuth.getInstance().getCurrentUser().updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    FirebaseAuth.getInstance().getCurrentUser().updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            FirebaseAuth.getInstance().getCurrentUser().updateProfile(
                                    new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .setPhotoUri(mProfilePhotoUri)
                                            .build()).addOnCompleteListener(
                                    new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
//                                                Toast.makeText(getContext(), "Updated", Toast.LENGTH_LONG).show();
                                                final Snackbar snackbar = Snackbar.make(mSaveInfo, "Updated", Snackbar.LENGTH_INDEFINITE);
                                                snackbar.setAction("OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        snackbar.dismiss();
                                                    }
                                                }).show();
                                                onButtonPressed(PROFILE_UPDATED_URI);
                                            } else {
//                                                Toast.makeText(getContext(), "Failed to update profile info", Toast.LENGTH_LONG).show();
                                                final Snackbar snackbar = Snackbar.make(mSaveInfo, "Something went wrong. Try again", Snackbar.LENGTH_INDEFINITE);
                                                snackbar.setAction("OK", new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        snackbar.dismiss();
                                                    }
                                                }).show();
                                            }
                                            mName.setEnabled(true);
                                            mEmail.setEnabled(true);
                                            mPassword.setEnabled(true);
                                            mSaveInfo.setEnabled(true);
                                            mUpdateProgress.setVisibility(View.GONE);
                                        }
                                    });

                        }
                    });
                }
            });

        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
