package com.doctorview.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.doctorview.app.R;
import com.doctorview.app.adapters.MessageAdapter;
import com.doctorview.app.firebase.FirebaseHelper;
import com.doctorview.app.models.Message;
import com.doctorview.app.utils.AppUtils;
import com.doctorview.app.utils.Constants;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;

/**
 * The consultation chat room for one doctor.
 * Messages are stored in the Firebase Realtime Database under
 * "chats/{chatId}" and appear instantly on both sides.
 */
public class ChatRoomFragment extends Fragment {

    private final List<Message> messages = new ArrayList<>();

    private MessageAdapter adapter;
    private DatabaseReference chatRef;
    private ChildEventListener childEventListener;
    private TextView tvEmptyChat;
    private TextInputEditText etMessage;

    public ChatRoomFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_room, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser firebaseUser = FirebaseHelper.getAuth().getCurrentUser();
        if (firebaseUser == null) {
            return;
        }

        Bundle args = getArguments();
        // The peer is the doctor when the patient opens the chat,
        // and the patient when the doctor opens it.
        String doctorId = args != null ? args.getString("doctorId", "") : "";
        String doctorName = args != null ? args.getString("doctorName", "") : "";
        String patientId = args != null ? args.getString("patientId", "") : "";
        String patientName = args != null ? args.getString("patientName", "") : "";
        String peerId = !doctorId.isEmpty() ? doctorId : patientId;
        String peerName = !doctorName.isEmpty() ? doctorName : patientName;

        TextView tvDoctorName = view.findViewById(R.id.tvChatDoctorName);
        tvDoctorName.setText(peerName.isEmpty()
                ? getString(R.string.title_online_consultation) : peerName);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        etMessage = view.findViewById(R.id.etMessage);
        tvEmptyChat = view.findViewById(R.id.tvEmptyChat);

        RecyclerView rvMessages = view.findViewById(R.id.rvMessages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true); // newest messages stay visible
        rvMessages.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(firebaseUser.getUid());
        rvMessages.setAdapter(adapter);

        // One shared chat room per patient-doctor pair (same id on both sides)
        String safePeerId = peerId == null || peerId.isEmpty() ? "general" : peerId;
        String chatId = firebaseUser.getUid().compareTo(safePeerId) < 0
                ? firebaseUser.getUid() + "_" + safePeerId
                : safePeerId + "_" + firebaseUser.getUid();
        chatRef = FirebaseHelper.getRealtimeDatabase()
                .child(Constants.RTDB_CHATS)
                .child(chatId);

        // Listen for messages — new ones appear live without any refresh
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, String previousChildName) {
                Message message = snapshot.getValue(Message.class);
                if (message != null) {
                    adapter.add(message);
                    tvEmptyChat.setVisibility(View.GONE);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Not used in this prototype
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // Not used in this prototype
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, String previousChildName) {
                // Not used in this prototype
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded()) {
                    AppUtils.showToast(requireContext(), R.string.chat_load_failed);
                }
            }
        };
        chatRef.addChildEventListener(childEventListener);

        MaterialButton btnSend = view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> sendMessage(firebaseUser));
    }

    /** Writes the typed message to the Realtime Database. */
    private void sendMessage(FirebaseUser firebaseUser) {
        String text = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        if (text.isEmpty()) {
            return;
        }

        String senderName = firebaseUser.getEmail() != null
                ? firebaseUser.getEmail().split("@")[0]
                : "Patient";
        Message message = new Message(firebaseUser.getUid(), senderName,
                text, System.currentTimeMillis());

        chatRef.push().setValue(message)
                .addOnFailureListener(e -> AppUtils.showToast(requireContext(), R.string.chat_send_failed));

        etMessage.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Stop listening when leaving the chat
        if (chatRef != null && childEventListener != null) {
            chatRef.removeEventListener(childEventListener);
        }
    }
}
