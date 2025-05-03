package com.shopwave.service;

import com.google.firebase.auth.FirebaseToken;
import com.shopwave.dto.UserDto;

public interface FirebaseAuthService {
    FirebaseToken verifyToken(String idToken);
    UserDto createUserFromFirebaseToken(FirebaseToken token);
    void deleteUser(String firebaseUid);
    void updateUserEmail(String firebaseUid, String newEmail);
    void updateUserPassword(String firebaseUid, String newPassword);
    void sendPasswordResetEmail(String email);
} 