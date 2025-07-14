package com.example.carsale;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carsale.Database.FirebaseHelper;
import com.example.carsale.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int LOCATION_REQUEST_CODE = 1001;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageView ivLocation;
    private TextView tvUserName;
    private LinearLayout ivProfile;
    private FirebaseHelper firebaseHelper;
    private FirebaseAuth auth;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        
        initViews(view);
        initData();
        setupListeners();
        loadUserInfo();
        
        return view;
    }

    private void initViews(View view) {
        ivLocation = view.findViewById(R.id.iv_location);
        tvUserName = view.findViewById(R.id.tv_user_name);
        ivProfile = view.findViewById(R.id.iv_profile);
    }

    private void initData() {
        firebaseHelper = FirebaseHelper.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void setupListeners() {
        ivLocation.setOnClickListener(v -> {
            if (getActivity() != null) {
                try {
                    Intent intent = new Intent(getActivity(), LocationActivity.class);
                    startActivityForResult(intent, LOCATION_REQUEST_CODE);
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        ivProfile.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            firebaseHelper.getUserById(currentUser.getUid(), new FirebaseHelper.OnUserDataListener() {
                @Override
                public void onSuccess(User user) {
                    if (user != null && user.getUsername() != null) {
                        tvUserName.setText(user.getUsername());
                    }
                }

                @Override
                public void onFailure(String error) {
                    // Handle error
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == LOCATION_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                String selectedLocation = data.getStringExtra("selected_location");
                if (selectedLocation != null) {
                    Toast.makeText(getActivity(), "Đã cập nhật vị trí: " + selectedLocation, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}