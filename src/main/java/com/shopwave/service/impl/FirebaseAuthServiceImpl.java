package com.shopwave.service.impl;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import com.shopwave.dto.UserDto;
import com.shopwave.model.User;
import com.shopwave.service.FirebaseAuthService;
import com.shopwave.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseAuthServiceImpl implements FirebaseAuthService {

    private final FirebaseAuth firebaseAuth;
    private final UserService userService;

    @Override
    public FirebaseToken verifyToken(String idToken) {
        try {
            return firebaseAuth.verifyIdToken(idToken);
        } catch (FirebaseAuthException e) {
            log.error("Failed to verify Firebase token", e);
            throw new RuntimeException("Invalid Firebase token", e);
        }
    }

    @Override
    public UserDto createUserFromFirebaseToken(FirebaseToken token) {
        try {
            UserRecord userRecord = firebaseAuth.getUser(token.getUid());
            
            UserDto userDto = new UserDto();
            userDto.setFirebaseUid(userRecord.getUid());
            userDto.setEmail(userRecord.getEmail());
            userDto.setFirstName(userRecord.getDisplayName());
            userDto.setRole(User.UserRole.USER);
            
            return userService.create(userDto);
        } catch (FirebaseAuthException e) {
            log.error("Failed to get user from Firebase", e);
            throw new RuntimeException("Failed to get user from Firebase", e);
        }
    }

    @Override
    public void deleteUser(String firebaseUid) {
        try {
            firebaseAuth.deleteUser(firebaseUid);
        } catch (FirebaseAuthException e) {
            log.error("Failed to delete user from Firebase", e);
            throw new RuntimeException("Failed to delete user from Firebase", e);
        }
    }

    @Override
    public void updateUserEmail(String firebaseUid, String newEmail) {
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(firebaseUid)
                    .setEmail(newEmail);
            firebaseAuth.updateUser(request);
        } catch (FirebaseAuthException e) {
            log.error("Failed to update user email in Firebase", e);
            throw new RuntimeException("Failed to update user email in Firebase", e);
        }
    }

    @Override
    public void updateUserPassword(String firebaseUid, String newPassword) {
        try {
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(firebaseUid)
                    .setPassword(newPassword);
            firebaseAuth.updateUser(request);
        } catch (FirebaseAuthException e) {
            log.error("Failed to update user password in Firebase", e);
            throw new RuntimeException("Failed to update user password in Firebase", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String email) {
        try {
            firebaseAuth.generatePasswordResetLink(email);
        } catch (FirebaseAuthException e) {
            log.error("Failed to send password reset email", e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
} 